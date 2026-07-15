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

/**
 * POST /api/events
 */
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

        return apiResponse(res, 201, true, 'Event created successfully', event);
    } catch (error) {
        next(error);
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
