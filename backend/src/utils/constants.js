/**
 * Utility Constants and Helpers
 */

// User roles
const ROLES = {
    ADMIN: 'admin',
    FACULTY: 'faculty',
    STUDENT: 'student',
};

// Notification categories
const NOTIFICATION_CATEGORIES = [
    'General',
    'Academic',
    'Exam',
    'Placement',
    'Event',
    'Workshop',
    'Holiday',
    'Sports',
    'Cultural',
    'Technical',
    'Birthday',
    'Timetable',
    'Emergency',
];

// Notification priorities
const NOTIFICATION_PRIORITIES = ['low', 'medium', 'high', 'urgent'];

// Event types
const EVENT_TYPES = [
    'Workshop',
    'Seminar',
    'Hackathon',
    'Cultural',
    'Sports',
    'Technical',
    'Placement',
    'Guest Lecture',
    'Competition',
    'Exhibition',
    'Other',
];

// Gallery categories
const GALLERY_CATEGORIES = [
    'Achievements',
    'Workshops',
    'Placements',
    'Campus Life',
    'Events',
    'Sports',
];

// Academic years
const YEARS = ['I', 'II', 'III', 'IV'];

// Sections
const SECTIONS = ['A', 'B', 'C', 'D'];

// Days of the week
const DAYS = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];

// File categories
const FILE_CATEGORIES = [
    'Notes',
    'Assignments',
    'Previous Papers',
    'Lab Manuals',
    'Syllabus',
    'Circulars',
    'Forms',
    'Others',
];

/**
 * Create a standardized API response
 */
const apiResponse = (res, statusCode, success, message, data = null, meta = null) => {
    const response = { success, message };
    if (data !== null) response.data = data;
    if (meta !== null) response.meta = meta;
    return res.status(statusCode).json(response);
};

/**
 * Create pagination metadata
 */
const paginationMeta = (page, limit, total) => {
    return {
        page: parseInt(page),
        limit: parseInt(limit),
        total,
        totalPages: Math.ceil(total / limit),
        hasNext: page * limit < total,
        hasPrev: page > 1,
    };
};

module.exports = {
    ROLES,
    NOTIFICATION_CATEGORIES,
    NOTIFICATION_PRIORITIES,
    EVENT_TYPES,
    GALLERY_CATEGORIES,
    YEARS,
    SECTIONS,
    DAYS,
    FILE_CATEGORIES,
    apiResponse,
    paginationMeta,
};
