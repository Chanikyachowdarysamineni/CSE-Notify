/**
 * Notification Controller
 * Full CRUD + search + filters + read tracking + push notifications
 */
const Notification = require('../models/Notification');
const NotificationRead = require('../models/NotificationRead');
const DeviceToken = require('../models/DeviceToken');
const Student = require('../models/Student');
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

        // Student: filter by their year/section ObjectIds
        if (req.user.role === ROLES.STUDENT) {
            const student = await Student.findOne({ userId: req.user.id });
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
        }).select('notificationId');

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
        });

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

        const notificationData = {
            title,
            message,
            category,
            priority: priority || 'medium',
            targetYears: targetYears || [],
            targetSections: targetSections || [],
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

        // Emit via Socket.IO
        const io = req.app.get('io');
        if (io) {
            io.emit('newNotification', notification);
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
 * Mark notification as read
 */
const markAsRead = async (req, res) => {
    try {
        const notification = await Notification.findById(req.params.id);
        if (!notification) {
            return apiResponse(res, 404, false, 'Notification not found');
        }

        // Upsert read status
        await NotificationRead.findOneAndUpdate(
            { userId: req.user.id, notificationId: req.params.id },
            { readAt: new Date() },
            { upsert: true }
        );

        // Increment read count
        await Notification.findByIdAndUpdate(req.params.id, { $inc: { readCount: 1 } });

        return apiResponse(res, 200, true, 'Notification marked as read');
    } catch (error) {
        logger.error('Mark as read error:', error);
        return apiResponse(res, 500, false, 'Server error');
    }
};

/**
 * GET /api/notifications/unread-count
 * Get unread notification count
 */
const getUnreadCount = async (req, res) => {
    try {
        // Get all valid notification IDs for this user
        let query = {
            $or: [
                { expiryDate: { $exists: false } },
                { expiryDate: null },
                { expiryDate: { $gte: new Date() } },
            ],
        };

        if (req.user.role === ROLES.STUDENT) {
            const student = await Student.findOne({ userId: req.user.id });
            if (student) {
                query.$and = [
                    {
                        $or: [
                            { targetYears: student.academicYear },
                            { targetYears: { $size: 0 } },
                            { targetYears: { $exists: false } },
                        ]
                    },
                    {
                        $or: [
                            { targetSections: student.section },
                            { targetSections: { $size: 0 } },
                            { targetSections: { $exists: false } },
                        ]
                    }
                ];
            }
        }

        const totalNotifications = await Notification.countDocuments(query);
        const readCount = await NotificationRead.countDocuments({ userId: req.user.id });
        const unreadCount = Math.max(0, totalNotifications - readCount);

        return apiResponse(res, 200, true, 'Unread count retrieved', { unreadCount });
    } catch (error) {
        logger.error('Get unread count error:', error);
        return apiResponse(res, 500, false, 'Server error');
    }
};

/**
 * Helper: Send push notification to targeted devices
 */
const sendNotificationPush = async (notification) => {
    try {
        let userQuery = {};

        // Build query based on targets
        if (notification.targetYears.length > 0) {
            // Get student user IDs for targeted years
            const studentQuery = { academicYear: { $in: notification.targetYears } };
            if (notification.targetSections.length > 0) {
                studentQuery.section = { $in: notification.targetSections };
            }
            const students = await Student.find(studentQuery).select('userId');
            const userIds = students.map(s => s.userId);

            // Also include faculty and admin
            const tokens = await DeviceToken.find({
                $or: [
                    { userId: { $in: userIds }, isActive: true },
                    // Admin and faculty get all notifications
                ],
                isActive: true,
            }).select('token');

            const tokenStrings = tokens.map(t => t.token).filter(Boolean);
            if (tokenStrings.length > 0) {
                await sendPushNotification(
                    tokenStrings,
                    notification.title,
                    notification.message.substring(0, 200),
                    { notificationId: notification._id.toString(), category: notification.category }
                );
            }
        } else {
            // Send to all
            const tokens = await DeviceToken.find({ isActive: true }).select('token');
            const tokenStrings = tokens.map(t => t.token).filter(Boolean);
            if (tokenStrings.length > 0) {
                await sendPushNotification(
                    tokenStrings,
                    notification.title,
                    notification.message.substring(0, 200),
                    { notificationId: notification._id.toString(), category: notification.category }
                );
            }
        }
    } catch (error) {
        logger.error('Send notification push error:', error);
    }
};

module.exports = {
    getNotifications,
    getNotificationById,
    createNotification,
    updateNotification,
    deleteNotification,
    markAsRead,
    getUnreadCount,
    sendNotificationPush,
};
