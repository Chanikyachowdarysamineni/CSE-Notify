/**
 * Dashboard Controller
 * Role-based dashboard widgets data
 */
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
const getDashboard = async (req, res) => {
    try {
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        const tomorrow = new Date(today);
        tomorrow.setDate(tomorrow.getDate() + 1);
        const dayName = DAYS[new Date().getDay() - 1] || 'Monday';

        const dashboard = {};

        // Today's notifications (last 24 hours)
        const notifQuery = {
            createdAt: { $gte: today },
            $or: [
                { isScheduled: false },
                { isScheduled: true, isSent: true },
            ],
        };

        dashboard.todayNotifications = await Notification.find(notifQuery)
            .populate('createdBy', 'name')
            .sort({ createdAt: -1 })
            .limit(10)
            .lean();

        // Upcoming events (next 30 days)
        dashboard.upcomingEvents = await Event.find({
            date: { $gte: today },
            isActive: true,
        })
            .sort({ date: 1 })
            .limit(5)
            .lean();

        // Today's timetable
        if (req.user.role === ROLES.STUDENT) {
            const student = await Student.findOne({ userId: req.user.id });
            if (student) {
                dashboard.todayTimetable = await Timetable.find({
                    academicYear: student.academicYear,
                    section: student.section,
                    day: dayName,
                }).sort({ period: 1 }).lean();
            }
        } else if (req.user.role === ROLES.FACULTY) {
            const faculty = await Faculty.findOne({ userId: req.user.id });
            if (faculty) {
                dashboard.todayTimetable = await Timetable.find({
                    faculty: faculty._id,
                    day: dayName,
                }).sort({ period: 1 }).lean();
            }
        }

        // Recent files
        dashboard.recentFiles = await File.find()
            .sort({ createdAt: -1 })
            .limit(5)
            .lean();

        // Gallery highlights
        dashboard.galleryHighlights = await Gallery.find()
            .sort({ createdAt: -1 })
            .limit(6)
            .lean();

        // Quick statistics (Admin only)
        if (req.user.role === ROLES.ADMIN) {
            const [totalStudents, totalFaculty, totalNotifications, totalEvents] = await Promise.all([
                Student.countDocuments(),
                Faculty.countDocuments(),
                Notification.countDocuments(),
                Event.countDocuments(),
            ]);

            dashboard.statistics = {
                totalStudents,
                totalFaculty,
                totalNotifications,
                totalEvents,
                totalFiles: await File.countDocuments(),
                totalGalleryPosts: await Gallery.countDocuments(),
            };
        }

        // Unread notification count
        const totalNotifs = await Notification.countDocuments(notifQuery);
        const readCount = await NotificationRead.countDocuments({
            userId: req.user.id,
            notificationId: { $in: (await Notification.find(notifQuery).select('_id')).map(n => n._id) },
        });
        dashboard.unreadCount = Math.max(0, totalNotifs - readCount);

        return apiResponse(res, 200, true, 'Dashboard data retrieved', dashboard);
    } catch (error) {
        logger.error('Get dashboard error:', error);
        return apiResponse(res, 500, false, 'Server error');
    }
};

module.exports = { getDashboard };
