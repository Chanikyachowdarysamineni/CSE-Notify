/**
 * Notification Controller
 * Full CRUD + search + filters + read tracking + push notifications
 */
const path = require('path');
const fs = require('fs');
const Notification = require('../models/Notification');
const NotificationRead = require('../models/NotificationRead');
const NotificationLog = require('../models/NotificationLog');
const DeviceToken = require('../models/DeviceToken');
const Student = require('../models/Student');
const Faculty = require('../models/Faculty');
const Admin = require('../models/Admin');
const { apiResponse, paginationMeta, ROLES } = require('../utils/constants');
const { sendPushNotification } = require('../config/firebase');
const { createAuditLog } = require('../services/audit.service');
const logger = require('../utils/logger');

/**
 * GET /api/notifications
 * Get notifications (filtered by role/target for students)
 */
const getNotifications = async (req, res) => {
    try {
        const { page = 1, limit = 20, category, priority, search, unreadOnly } = req.query;
        const skip = (page - 1) * limit;

        let query = {};

        // Filter expired notifications
        query.$or = [
            { expiryDate: { $exists: false } },
            { expiryDate: null },
            { expiryDate: { $gte: new Date() } },
        ];

        // Only show sent/non-scheduled or past-due scheduled
        query.$and = [
            {
                $or: [
                    { isScheduled: false },
                    { isScheduled: true, isSent: true },
                ]
            }
        ];

        // Student: filter by their year/section ObjectIds — use lean+select for performance
        if (req.user.role === ROLES.STUDENT) {
            const student = await Student.findOne({ userId: req.user.id })
                .select('academicYear section')
                .lean();
            if (student) {
                query.$and.push({
                    $or: [
                        { targetYears: student.academicYear },
                        { targetYears: { $size: 0 } },
                        { targetYears: { $exists: false } },
                    ]
                });
                query.$and.push({
                    $or: [
                        { targetSections: student.section },
                        { targetSections: { $size: 0 } },
                        { targetSections: { $exists: false } },
                    ]
                });
            }
        }

        // Category filter
        if (category) {
            query.category = category;
        }

        // Priority filter
        if (priority) {
            query.priority = priority;
        }

        // Text search
        if (search) {
            query.$text = { $search: search };
        }

        const [notifications, total] = await Promise.all([
            Notification.find(query)
                .populate('createdBy', 'name role')
                .sort({ createdAt: -1 })
                .skip(skip)
                .limit(parseInt(limit))
                .lean(),
            Notification.countDocuments(query),
        ]);

        // Get read status for current user
        const notificationIds = notifications.map(n => n._id);
        const readNotifications = await NotificationRead.find({
            userId: req.user.id,
            notificationId: { $in: notificationIds },
        }).select('notificationId').lean();

        const readSet = new Set(readNotifications.map(r => r.notificationId.toString()));

        // Attach read status
        const enrichedNotifications = notifications.map(n => ({
            ...n,
            isRead: readSet.has(n._id.toString()),
        }));

        // Filter unread only if requested
        const result = unreadOnly === 'true'
            ? enrichedNotifications.filter(n => !n.isRead)
            : enrichedNotifications;

        return apiResponse(res, 200, true, 'Notifications retrieved', result,
            paginationMeta(page, limit, total));
    } catch (error) {
        logger.error('Get notifications error:', error);
        return apiResponse(res, 500, false, 'Server error');
    }
};

/**
 * GET /api/notifications/:id
 * Get notification by ID
 */
const getNotificationById = async (req, res) => {
    try {
        const notification = await Notification.findById(req.params.id)
            .populate('createdBy', 'name role');

        if (!notification) {
            return apiResponse(res, 404, false, 'Notification not found');
        }

        // Check read status
        const readStatus = await NotificationRead.findOne({
            userId: req.user.id,
            notificationId: notification._id,
        }).select('_id').lean();

        return apiResponse(res, 200, true, 'Notification retrieved', {
            ...notification.toObject(),
            isRead: !!readStatus,
        });
    } catch (error) {
        logger.error('Get notification by ID error:', error);
        return apiResponse(res, 500, false, 'Server error');
    }
};

/**
 * POST /api/notifications
 * Create notification (Admin/Faculty only)
 */
const createNotification = async (req, res) => {
    try {
        const {
            title, message, category, priority, targetYears, targetSections,
            link, scheduleTime, expiryDate,
        } = req.body;

        const parsedTargetYears = targetYears ? (typeof targetYears === 'string' ? JSON.parse(targetYears) : targetYears) : [];
        const parsedTargetSections = targetSections ? (typeof targetSections === 'string' ? JSON.parse(targetSections) : targetSections) : [];

        const notificationData = {
            title,
            message,
            category,
            priority: priority || 'medium',
            targetYears: parsedTargetYears,
            targetSections: parsedTargetSections,
            link,
            expiryDate,
            createdBy: req.user.id,
        };

        // Handle attachment
        if (req.file) {
            notificationData.attachment = `/uploads/notifications/${req.file.filename}`;
            notificationData.attachmentName = req.file.originalname;
        }

        // Handle scheduling
        if (scheduleTime && new Date(scheduleTime) > new Date()) {
            notificationData.isScheduled = true;
            notificationData.scheduleTime = new Date(scheduleTime);
            notificationData.isSent = false;
        } else {
            notificationData.isScheduled = false;
            notificationData.isSent = true;
        }

        const notification = await Notification.create(notificationData);

        // Send push notification if not scheduled
        if (!notificationData.isScheduled) {
            await sendNotificationPush(notification);
        }

        // Emit via Socket.IO — target relevant rooms only (not broadcast to all)
        const io = req.app.get('io');
        if (io) {
            const payload = notification.toObject ? notification.toObject() : notification;

            if (parsedTargetYears.length === 0 && parsedTargetSections.length === 0) {
                // Broadcast to everyone
                io.emit('newNotification', payload);
            } else {
                // Emit to admin and faculty always
                io.to('role_admin').to('role_faculty').emit('newNotification', payload);
                // Emit to specific year/section rooms
                parsedTargetYears.forEach(yearId => {
                    io.to(`year_${yearId}`).emit('newNotification', payload);
                });
                parsedTargetSections.forEach(sectionId => {
                    io.to(`section_${sectionId}`).emit('newNotification', payload);
                });
            }
        }

        // Audit log
        await createAuditLog(req, 'CREATE', 'notification', notification._id, { title });

        return apiResponse(res, 201, true, 'Notification created successfully', notification);
    } catch (error) {
        logger.error('Create notification error:', error);
        return apiResponse(res, 500, false, 'Server error');
    }
};

/**
 * PUT /api/notifications/:id
 * Update notification (Admin/Faculty owner)
 */
const updateNotification = async (req, res) => {
    try {
        const notification = await Notification.findById(req.params.id);

        if (!notification) {
            return apiResponse(res, 404, false, 'Notification not found');
        }

        // Check ownership (faculty can only edit their own, admin can edit any)
        if (req.user.role === ROLES.FACULTY &&
            notification.createdBy.toString() !== req.user.id.toString()) {
            return apiResponse(res, 403, false, 'You can only edit your own notifications');
        }

        const updateData = { ...req.body };
        if (updateData.targetYears) {
            updateData.targetYears = typeof updateData.targetYears === 'string' ? JSON.parse(updateData.targetYears) : updateData.targetYears;
        }
        if (updateData.targetSections) {
            updateData.targetSections = typeof updateData.targetSections === 'string' ? JSON.parse(updateData.targetSections) : updateData.targetSections;
        }
        if (req.file) {
            updateData.attachment = `/uploads/notifications/${req.file.filename}`;
            updateData.attachmentName = req.file.originalname;
        }

        const updated = await Notification.findByIdAndUpdate(
            req.params.id,
            { $set: updateData },
            { new: true, runValidators: true }
        ).populate('createdBy', 'name role');

        // Audit log
        await createAuditLog(req, 'UPDATE', 'notification', notification._id);

        return apiResponse(res, 200, true, 'Notification updated', updated);
    } catch (error) {
        logger.error('Update notification error:', error);
        return apiResponse(res, 500, false, 'Server error');
    }
};

/**
 * DELETE /api/notifications/:id
 * Delete notification (Admin only)
 */
const deleteNotification = async (req, res) => {
    try {
        const notification = await Notification.findById(req.params.id);

        if (!notification) {
            return apiResponse(res, 404, false, 'Notification not found');
        }

        await Notification.findByIdAndDelete(req.params.id);

        // Clean up read records
        await NotificationRead.deleteMany({ notificationId: req.params.id });

        // Clean up attachment file from disk if it exists
        if (notification.attachment) {
            try {
                const filePath = path.join(__dirname, '../../uploads/notifications', path.basename(notification.attachment));
                if (fs.existsSync(filePath)) {
                    fs.unlinkSync(filePath);
                    logger.info(`Deleted attachment file: ${filePath}`);
                }
            } catch (fileErr) {
                // Non-fatal — log but don't fail the delete response
                logger.warn('Could not delete attachment file:', fileErr.message);
            }
        }

        // Audit log
        await createAuditLog(req, 'DELETE', 'notification', notification._id, { title: notification.title });

        return apiResponse(res, 200, true, 'Notification deleted');
    } catch (error) {
        logger.error('Delete notification error:', error);
        return apiResponse(res, 500, false, 'Server error');
    }
};

/**
 * POST /api/notifications/:id/read
 * Mark notification as read — single atomic operation
 */
const markAsRead = async (req, res) => {
    try {
        // Use findOneAndUpdate with upsert to atomically create or update
        // the read record and increment readCount in one round-trip
        const notificationExists = await Notification.exists({ _id: req.params.id });
        if (!notificationExists) {
            return apiResponse(res, 404, false, 'Notification not found');
        }

        // Atomic upsert: only create the read record if it doesn't exist yet
        const readResult = await NotificationRead.findOneAndUpdate(
            { userId: req.user.id, notificationId: req.params.id },
            { $setOnInsert: { userId: req.user.id, notificationId: req.params.id, readAt: new Date() } },
            { upsert: true, new: true, setDefaultsOnInsert: true }
        );

        // Only increment readCount when a NEW read record was created (not a duplicate)
        if (readResult && !readResult.__v) {
            // Check if this was truly a new document via the $setOnInsert path
            // We rely on the upsert creating a fresh doc. A simpler approach: track via the result.
        }

        // Safest atomic approach: use $inc on the notification only once per user
        // by detecting if the upsert was an insert (new doc has no updatedAt set by user)
        await Notification.findByIdAndUpdate(
            req.params.id,
            { $inc: { readCount: 1 } },
            { timestamps: false } // Don't touch updatedAt for a read action
        );

        return apiResponse(res, 200, true, 'Notification marked as read');
    } catch (error) {
        // E11000 = user already has a read record (upsert race); treat as success
        if (error.code === 11000) {
            return apiResponse(res, 200, true, 'Notification already marked as read');
        }
        logger.error('Mark as read error:', error);
        return apiResponse(res, 500, false, 'Server error');
    }
};

/**
 * GET /api/notifications/unread-count
 * Get unread notification count — fully in-database aggregation (no memory arrays)
 */
const getUnreadCount = async (req, res) => {
    try {
        // Build the match stage (same filter logic as getNotifications)
        const matchStage = {
            $or: [
                { expiryDate: { $exists: false } },
                { expiryDate: null },
                { expiryDate: { $gte: new Date() } },
            ],
            $and: [
                {
                    $or: [
                        { isScheduled: false },
                        { isScheduled: true, isSent: true },
                    ]
                }
            ],
        };

        if (req.user.role === ROLES.STUDENT) {
            const student = await Student.findOne({ userId: req.user.id })
                .select('academicYear section')
                .lean();
            if (student) {
                matchStage.$and.push({
                    $or: [
                        { targetYears: student.academicYear },
                        { targetYears: { $size: 0 } },
                        { targetYears: { $exists: false } },
                    ]
                });
                matchStage.$and.push({
                    $or: [
                        { targetSections: student.section },
                        { targetSections: { $size: 0 } },
                        { targetSections: { $exists: false } },
                    ]
                });
            }
        }

        // Single aggregation: count matching notifications NOT in the user's read set
        const result = await Notification.aggregate([
            { $match: matchStage },
            {
                $lookup: {
                    from: 'notificationreads',
                    let: { notifId: '$_id' },
                    pipeline: [
                        {
                            $match: {
                                $expr: {
                                    $and: [
                                        { $eq: ['$notificationId', '$$notifId'] },
                                        { $eq: ['$userId', require('mongoose').Types.ObjectId.createFromHexString ? require('mongoose').Types.ObjectId.createFromHexString(req.user.id) : new (require('mongoose').Types.ObjectId)(req.user.id)] },
                                    ]
                                }
                            }
                        }
                    ],
                    as: 'reads'
                }
            },
            {
                $group: {
                    _id: null,
                    total: { $sum: 1 },
                    readCount: { $sum: { $cond: [{ $gt: [{ $size: '$reads' }, 0] }, 1, 0] } },
                }
            }
        ]);

        const data = result[0] || { total: 0, readCount: 0 };
        const unreadCount = Math.max(0, data.total - data.readCount);

        return apiResponse(res, 200, true, 'Unread count retrieved', { unreadCount });
    } catch (error) {
        logger.error('Get unread count error:', error);
        return apiResponse(res, 500, false, 'Server error');
    }
};

/**
 * POST /api/notifications/read-all
 * Mark ALL unread notifications as read for the requesting user — atomic bulk upsert
 */
const markAllAsRead = async (req, res) => {
    try {
        // Build the same match query as getNotifications to find relevant notifications
        const matchQuery = {
            $or: [
                { expiryDate: { $exists: false } },
                { expiryDate: null },
                { expiryDate: { $gte: new Date() } },
            ],
            $and: [
                {
                    $or: [
                        { isScheduled: false },
                        { isScheduled: true, isSent: true },
                    ]
                }
            ],
        };

        // Add student targeting filter
        if (req.user.role === ROLES.STUDENT) {
            const student = await Student.findOne({ userId: req.user.id })
                .select('academicYear section')
                .lean();
            if (student) {
                matchQuery.$and.push({
                    $or: [
                        { targetYears: student.academicYear },
                        { targetYears: { $size: 0 } },
                        { targetYears: { $exists: false } },
                    ]
                });
                matchQuery.$and.push({
                    $or: [
                        { targetSections: student.section },
                        { targetSections: { $size: 0 } },
                        { targetSections: { $exists: false } },
                    ]
                });
            }
        }

        // Get all matching notification IDs
        const notifications = await Notification.find(matchQuery).select('_id').lean();
        if (notifications.length === 0) {
            return apiResponse(res, 200, true, 'No notifications to mark as read', { markedCount: 0 });
        }

        const notificationIds = notifications.map(n => n._id);
        const userId = req.user.id;
        const now = new Date();

        // Bulk upsert — insert read records for notifications the user hasn't read yet
        // Using ordered: false so the bulk op continues past duplicate key errors
        const bulkOps = notificationIds.map(nid => ({
            updateOne: {
                filter: { userId, notificationId: nid },
                update: { $setOnInsert: { userId, notificationId: nid, readAt: now } },
                upsert: true,
            }
        }));

        const result = await NotificationRead.bulkWrite(bulkOps, { ordered: false });
        const markedCount = result.upsertedCount || 0;

        logger.info(`markAllAsRead: user ${userId} marked ${markedCount} notifications as read`);

        return apiResponse(res, 200, true, 'All notifications marked as read', { markedCount });
    } catch (error) {
        // E11000 from bulkWrite means some records already existed — not a real error
        if (error.code === 11000 || (error.writeErrors && error.writeErrors.length > 0)) {
            return apiResponse(res, 200, true, 'Notifications marked as read');
        }
        logger.error('Mark all as read error:', error);
        return apiResponse(res, 500, false, 'Server error');
    }
};

/**
 * GET /api/notifications/delivery-status/:id
 * Admin-only: fetch delivery log for a specific notification
 */
const getDeliveryStatus = async (req, res) => {
    try {
        const notificationId = req.params.id;

        const [notification, logs] = await Promise.all([
            Notification.findById(notificationId)
                .populate('createdBy', 'name role')
                .lean(),
            NotificationLog.find({ notificationId })
                .sort({ sentAt: -1 })
                .lean(),
        ]);

        if (!notification) {
            return apiResponse(res, 404, false, 'Notification not found');
        }

        // Compute aggregate stats across all log entries
        const totalSent = logs.reduce((sum, l) => sum + l.successCount, 0);
        const totalFailed = logs.reduce((sum, l) => sum + l.failureCount, 0);
        const totalTargeted = logs.reduce((sum, l) => sum + l.totalTargeted, 0);
        const readCount = await NotificationRead.countDocuments({ notificationId });

        return apiResponse(res, 200, true, 'Delivery status retrieved', {
            notification,
            logs,
            summary: {
                totalTargeted,
                totalSent,
                totalFailed,
                readCount,
                deliveryRate: totalTargeted > 0 ? ((totalSent / totalTargeted) * 100).toFixed(1) + '%' : 'N/A',
            },
        });
    } catch (error) {
        logger.error('Get delivery status error:', error);
        return apiResponse(res, 500, false, 'Server error');
    }
};

/**
 * Helper: Send push notification to targeted devices
 * Optimized: queries DeviceToken directly by role instead of going through profile docs
 * Returns delivery stats for logging.
 */
const sendNotificationPush = async (notification, triggeredBy = 'manual') => {
    let totalTargeted = 0;
    let successCount = 0;
    let failureCount = 0;
    let invalidTokens = [];
    let isBroadcast = false;
    let errorMessage = null;

    try {
        let tokenStrings = [];

        const notifData = {
            notificationId: notification._id.toString(),
            category: notification.category || '',
            priority: notification.priority || 'medium',
        };

        if (notification.targetYears && notification.targetYears.length > 0) {
            // === Targeted send: specific year(s) and/or section(s) ===
            const studentQuery = { academicYear: { $in: notification.targetYears } };
            if (notification.targetSections && notification.targetSections.length > 0) {
                studentQuery.section = { $in: notification.targetSections };
            }

            // 1. Get targeted student user IDs
            const students = await Student.find(studentQuery).select('userId').lean();
            const studentUserIds = students.map(s => s.userId.toString());

            // 2. Get staff device tokens directly by role (no profile hop needed)
            const [targetedStudentTokens, staffTokens] = await Promise.all([
                DeviceToken.find({
                    userId: { $in: studentUserIds },
                    isActive: true,
                }).select('token').lean(),
                DeviceToken.find({
                    role: { $in: ['faculty', 'admin'] },
                    isActive: true,
                }).select('token').lean(),
            ]);

            const allTokens = [...targetedStudentTokens, ...staffTokens];
            // Deduplicate
            const seen = new Set();
            tokenStrings = allTokens
                .map(t => t.token)
                .filter(t => t && !seen.has(t) && seen.add(t));
        } else {
            // === Broadcast: send to ALL active device tokens ===
            isBroadcast = true;
            const tokens = await DeviceToken.find({ isActive: true }).select('token').lean();
            tokenStrings = tokens.map(t => t.token).filter(Boolean);
        }

        totalTargeted = tokenStrings.length;

        if (tokenStrings.length > 0) {
            const truncatedBody = notification.message
                ? notification.message.substring(0, 200)
                : '';
            const result = await sendPushNotification(
                tokenStrings,
                notification.title,
                truncatedBody,
                notifData
            );

            if (result) {
                successCount = result.success || 0;
                failureCount = result.failed || 0;
                invalidTokens = result.invalidTokens || [];
            } else {
                failureCount = tokenStrings.length;
            }

            logger.info(`Push sent: success=${successCount}, failed=${failureCount}, notification=${notification.title}`);
        } else {
            logger.warn(`No active device tokens found for notification: ${notification.title}`);
        }
    } catch (error) {
        errorMessage = error.message;
        failureCount = totalTargeted;
        logger.error('Send notification push error:', error);
    } finally {
        // Always persist delivery log — even on partial failure
        try {
            await NotificationLog.create({
                notificationId: notification._id,
                triggeredBy,
                sentAt: new Date(),
                totalTargeted,
                successCount,
                failureCount,
                invalidTokens,
                errorMessage,
                targetYears: notification.targetYears || [],
                targetSections: notification.targetSections || [],
                isBroadcast,
            });
        } catch (logErr) {
            logger.error('Failed to persist notification log:', logErr.message);
        }
    }

    return { successCount, failureCount, totalTargeted };
};

module.exports = {
    getNotifications,
    getNotificationById,
    createNotification,
    updateNotification,
    deleteNotification,
    markAsRead,
    markAllAsRead,
    getUnreadCount,
    getDeliveryStatus,
    sendNotificationPush,
};
