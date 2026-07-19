/**
 * Dashboard Controller
 * Role-based dashboard widgets data — all queries parallelized
 */
const mongoose = require('mongoose');
const Notification = require('../models/Notification');
const NotificationRead = require('../models/NotificationRead');
const Event = require('../models/Event');
const Timetable = require('../models/Timetable');
const File = require('../models/File');
const Gallery = require('../models/Gallery');
const Student = require('../models/Student');
const Faculty = require('../models/Faculty');
const User = require('../models/User');
const { apiResponse, ROLES, DAYS } = require('../utils/constants');
const logger = require('../utils/logger');

/**
 * GET /api/dashboard
 */
const getDashboard = async (req, res, next) => {
    try {
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        const dayName = DAYS[new Date().getDay() - 1] || 'Monday';

        const dashboard = {};

        // Base notification query (today's)
        const notifQuery = {
            createdAt: { $gte: today },
            $or: [
                { isScheduled: false },
                { isScheduled: true, isSent: true },
            ],
        };

        // Role-specific profile fetch (needed for timetable)
        let profilePromise = Promise.resolve(null);
        if (req.user.role === ROLES.STUDENT) {
            profilePromise = Student.findOne({ userId: req.user.id })
                .select('academicYear section')
                .lean();
        } else if (req.user.role === ROLES.FACULTY) {
            profilePromise = Faculty.findOne({ userId: req.user.id })
                .select('_id')
                .lean();
        }

        // Run all independent top-level queries in parallel
        const [
            todayNotifications,
            upcomingEvents,
            recentFiles,
            galleryHighlights,
            profile,
        ] = await Promise.all([
            Notification.find(notifQuery)
                .populate('createdBy', 'name')
                .sort({ createdAt: -1 })
                .limit(10)
                .lean(),
            Event.find({ date: { $gte: today }, isActive: true })
                .sort({ date: 1 })
                .limit(5)
                .lean(),
            File.find()
                .sort({ createdAt: -1 })
                .limit(5)
                .lean(),
            Gallery.find()
                .sort({ createdAt: -1 })
                .limit(6)
                .lean(),
            profilePromise,
        ]);

        dashboard.todayNotifications = todayNotifications;
        dashboard.upcomingEvents = upcomingEvents;
        dashboard.recentFiles = recentFiles;
        dashboard.galleryHighlights = galleryHighlights;

        // Today's timetable (requires profile)
        if (req.user.role === ROLES.STUDENT && profile) {
            dashboard.todayTimetable = await Timetable.find({
                academicYear: profile.academicYear,
                section: profile.section,
                day: dayName,
            }).sort({ period: 1 }).lean();
        } else if (req.user.role === ROLES.FACULTY && profile) {
            dashboard.todayTimetable = await Timetable.find({
                faculty: profile._id,
                day: dayName,
            }).sort({ period: 1 }).lean();
        }

        // Unread count — use aggregation to avoid loading IDs into memory
        const userId = new mongoose.Types.ObjectId(req.user.id);
        const unreadAgg = await Notification.aggregate([
            { $match: notifQuery },
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
                                        { $eq: ['$userId', userId] },
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
        const unreadData = unreadAgg[0] || { total: 0, readCount: 0 };
        dashboard.unreadCount = Math.max(0, unreadData.total - unreadData.readCount);

        // Quick statistics (Admin only) — fully parallel
        if (req.user.role === ROLES.ADMIN) {
            const [totalStudents, totalFaculty, totalNotifications, totalEvents, totalFiles, totalGalleryPosts] =
                await Promise.all([
                    Student.countDocuments(),
                    Faculty.countDocuments(),
                    Notification.countDocuments(),
                    Event.countDocuments(),
                    File.countDocuments(),
                    Gallery.countDocuments(),
                ]);

            dashboard.statistics = {
                totalStudents,
                totalFaculty,
                totalNotifications,
                totalEvents,
                totalFiles,
                totalGalleryPosts,
            };
        }

        return apiResponse(res, 200, true, 'Dashboard data retrieved', dashboard);
    } catch (error) {
        next(error);
    }
};

module.exports = { getDashboard };
