/**
 * Models index — centralizes all Mongoose model exports.
 * Allows: const { Faculty, Admin, Student } = require('../models');
 */
module.exports = {
    AcademicYear:     require('./AcademicYear'),
    Admin:            require('./Admin'),
    AuditLog:         require('./AuditLog'),
    DeviceToken:      require('./DeviceToken'),
    Event:            require('./Event'),
    Faculty:          require('./Faculty'),
    File:             require('./File'),
    Gallery:          require('./Gallery'),
    Notification:     require('./Notification'),
    NotificationRead: require('./NotificationRead'),
    RefreshToken:     require('./RefreshToken'),
    Section:          require('./Section'),
    Student:          require('./Student'),
    Timetable:        require('./Timetable'),
    User:             require('./User'),
};
