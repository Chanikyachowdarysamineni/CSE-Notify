/**
 * Event Controller
 */
const Event = require('../models/Event');
const Student = require('../models/Student');
const { apiResponse, paginationMeta, ROLES } = require('../utils/constants');
const { createAuditLog } = require('../services/audit.service');
const logger = require('../utils/logger');

/**
 * GET /api/events
 */
const getEvents = async (req, res, next) => {
    try {
        const { page = 1, limit = 20, eventType, search, upcoming } = req.query;
        const skip = (page - 1) * limit;

        let query = { isActive: true };

        // Student: filter by year/section ObjectIds
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

        if (eventType) query.eventType = eventType;
        if (upcoming === 'true') query.date = { $gte: new Date() };
        if (search) query.$text = { $search: search };

        const [events, total] = await Promise.all([
            Event.find(query)
                .populate('createdBy', 'name role')
                .sort({ date: upcoming === 'true' ? 1 : -1 })
                .skip(skip)
                .limit(parseInt(limit))
                .lean(),
            Event.countDocuments(query),
        ]);

        return apiResponse(res, 200, true, 'Events retrieved', events,
            paginationMeta(page, limit, total));
    } catch (error) {
        next(error);
    }
};

/**
 * GET /api/events/:id
 */
const getEventById = async (req, res, next) => {
    try {
        const event = await Event.findById(req.params.id)
            .populate('createdBy', 'name role');

        if (!event) {
            return apiResponse(res, 404, false, 'Event not found');
        }

        return apiResponse(res, 200, true, 'Event retrieved', event);
    } catch (error) {
        next(error);
    }
};

const createEvent = async (req, res, next) => {
    try {
        const targetYears = req.body.targetYears ? (typeof req.body.targetYears === 'string' ? JSON.parse(req.body.targetYears) : req.body.targetYears) : [];
        const targetSections = req.body.targetSections ? (typeof req.body.targetSections === 'string' ? JSON.parse(req.body.targetSections) : req.body.targetSections) : [];

        const eventData = {
            ...req.body,
            createdBy: req.user.id,
            targetYears,
            targetSections,
        };

        if (req.file) {
            eventData.bannerImage = `/uploads/events/${req.file.filename}`;
        }

        const event = await Event.create(eventData);

        // Emit via Socket.IO
        const io = req.app.get('io');
        if (io) io.emit('newEvent', event);

        await createAuditLog(req, 'CREATE', 'event', event._id, { title: event.title });

        // Trigger FCM Push Asynchronously
        sendEventPush(event);

        return apiResponse(res, 201, true, 'Event created successfully', event);
    } catch (error) {
        next(error);
    }
};

/**
 * Send FCM push notification for a new event
 */
const sendEventPush = async (event) => {
    try {
        const DeviceToken = require('../models/DeviceToken');
        const Faculty = require('../models/Faculty');
        const Admin = require('../models/Admin');
        const { sendPushNotification } = require('../config/firebase');

        let tokenStrings = [];
        const pushData = {
            eventId: event._id.toString(),
            category: 'Event',
            priority: 'medium',
        };

        if (event.targetYears && event.targetYears.length > 0) {
            const studentQuery = { academicYear: { $in: event.targetYears } };
            if (event.targetSections && event.targetSections.length > 0) {
                studentQuery.section = { $in: event.targetSections };
            }

            // Get targeted students
            const students = await Student.find(studentQuery).select('userId');
            const studentUserIds = students.map(s => s.userId);

            // Staff (Faculty/Admin) always get notifications
            const facultyUsers = await Faculty.find({}).select('userId');
            const adminUsers = await Admin.find({}).select('userId');
            const staffUserIds = [
                ...facultyUsers.map(f => f.userId),
                ...adminUsers.map(a => a.userId),
            ];

            const allTargetUserIds = [...new Set([...studentUserIds.map(String), ...staffUserIds.map(String)])];

            const tokens = await DeviceToken.find({
                userId: { $in: allTargetUserIds },
                isActive: true,
            }).select('token');

            tokenStrings = tokens.map(t => t.token).filter(Boolean);
        } else {
            // Broadcast to all active tokens
            const tokens = await DeviceToken.find({ isActive: true }).select('token');
            tokenStrings = tokens.map(t => t.token).filter(Boolean);
        }

        if (tokenStrings.length > 0) {
            const title = `📅 New Event: ${event.title}`;
            const body = event.description ? event.description.substring(0, 200) : '';
            await sendPushNotification(tokenStrings, title, body, pushData);
            logger.info(`Push sent to ${tokenStrings.length} device(s) for event: ${event.title}`);
        } else {
            logger.warn(`No active device tokens found for event: ${event.title}`);
        }
    } catch (error) {
        logger.error('Send event push error:', error);
    }
};

/**
 * PUT /api/events/:id
 */
const updateEvent = async (req, res, next) => {
    try {
        const event = await Event.findById(req.params.id);
        if (!event) {
            return apiResponse(res, 404, false, 'Event not found');
        }

        if (req.user.role === ROLES.FACULTY &&
            event.createdBy.toString() !== req.user.id.toString()) {
            return apiResponse(res, 403, false, 'You can only edit your own events');
        }

        const updateData = { ...req.body };
        if (updateData.targetYears) {
            updateData.targetYears = typeof updateData.targetYears === 'string' ? JSON.parse(updateData.targetYears) : updateData.targetYears;
        }
        if (updateData.targetSections) {
            updateData.targetSections = typeof updateData.targetSections === 'string' ? JSON.parse(updateData.targetSections) : updateData.targetSections;
        }
        if (req.file) {
            updateData.bannerImage = `/uploads/events/${req.file.filename}`;
        }

        const updated = await Event.findByIdAndUpdate(
            req.params.id,
            { $set: updateData },
            { new: true, runValidators: true }
        ).populate('createdBy', 'name role');

        await createAuditLog(req, 'UPDATE', 'event', event._id);

        return apiResponse(res, 200, true, 'Event updated', updated);
    } catch (error) {
        next(error);
    }
};

/**
 * DELETE /api/events/:id
 */
const deleteEvent = async (req, res, next) => {
    try {
        const event = await Event.findById(req.params.id);
        if (!event) {
            return apiResponse(res, 404, false, 'Event not found');
        }

        if (req.user.role === ROLES.FACULTY &&
            event.createdBy.toString() !== req.user.id.toString()) {
            return apiResponse(res, 403, false, 'You can only delete your own events');
        }

        await Event.findByIdAndDelete(req.params.id);

        await createAuditLog(req, 'DELETE', 'event', event._id, { title: event.title });

        return apiResponse(res, 200, true, 'Event deleted');
    } catch (error) {
        next(error);
    }
};

module.exports = { getEvents, getEventById, createEvent, updateEvent, deleteEvent };
