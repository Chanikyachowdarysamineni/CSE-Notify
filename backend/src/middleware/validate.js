/**
 * Input Validation Middleware
 * 
 * Uses express-validator to validate and sanitize request data.
 */
const { validationResult, body, param, query } = require('express-validator');
const { apiResponse } = require('../utils/constants');

/**
 * Process validation results and return errors if any
 */
const validate = (req, res, next) => {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
        const formattedErrors = errors.array().map(err => ({
            field: err.path,
            message: err.msg,
            value: err.value,
        }));
        return apiResponse(res, 400, false, 'Validation failed', formattedErrors);
    }
    next();
};

const parseJsonArray = (value) => {
    if (typeof value === 'string') {
        try {
            return JSON.parse(value);
        } catch (e) {
            return [];
        }
    }
    return value;
};

// ============================================
// Validation Rules
// ============================================

const authValidation = {
    login: [
        body('loginId')
            .trim()
            .notEmpty()
            .withMessage('Please provide a valid login ID'),
        body('password')
            .notEmpty()
            .withMessage('Password is required')
            .isLength({ min: 6 })
            .withMessage('Password must be at least 6 characters'),
    ],
    forgotPassword: [
        body('email')
            .trim()
            .isEmail()
            .withMessage('Please provide a valid email address')
            .normalizeEmail(),
    ],
    resetPassword: [
        body('token')
            .notEmpty()
            .withMessage('Reset token is required'),
        body('password')
            .notEmpty()
            .withMessage('New password is required')
            .isLength({ min: 6 })
            .withMessage('Password must be at least 6 characters')
            .matches(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/)
            .withMessage('Password must contain at least one uppercase letter, one lowercase letter, and one number'),
    ],
    changePassword: [
        body('currentPassword')
            .notEmpty()
            .withMessage('Current password is required'),
        body('newPassword')
            .notEmpty()
            .withMessage('New password is required')
            .isLength({ min: 6 })
            .withMessage('Password must be at least 6 characters')
            .matches(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/)
            .withMessage('Password must contain at least one uppercase letter, one lowercase letter, and one number'),
    ],
};

const notificationValidation = {
    create: [
        body('title')
            .trim()
            .notEmpty()
            .withMessage('Title is required')
            .isLength({ max: 200 })
            .withMessage('Title must be under 200 characters'),
        body('message')
            .trim()
            .notEmpty()
            .withMessage('Message is required')
            .isLength({ max: 5000 })
            .withMessage('Message must be under 5000 characters'),
        body('category')
            .trim()
            .notEmpty()
            .withMessage('Category is required'),
        body('priority')
            .optional()
            .isIn(['low', 'medium', 'high', 'urgent'])
            .withMessage('Priority must be low, medium, high, or urgent'),
        body('targetYears')
            .optional()
            .customSanitizer(parseJsonArray)
            .isArray()
            .withMessage('Target years must be an array'),
        body('targetSections')
            .optional()
            .customSanitizer(parseJsonArray)
            .isArray()
            .withMessage('Target sections must be an array'),
        body('scheduleTime')
            .optional()
            .isISO8601()
            .withMessage('Schedule time must be a valid date'),
        body('expiryDate')
            .optional()
            .isISO8601()
            .withMessage('Expiry date must be a valid date'),
    ],
};

const eventValidation = {
    create: [
        body('title')
            .trim()
            .notEmpty()
            .withMessage('Title is required')
            .isLength({ max: 200 })
            .withMessage('Title must be under 200 characters'),
        body('description')
            .trim()
            .notEmpty()
            .withMessage('Description is required'),
        body('eventType')
            .trim()
            .notEmpty()
            .withMessage('Event type is required'),
        body('date')
            .notEmpty()
            .withMessage('Event date is required')
            .isISO8601()
            .withMessage('Date must be a valid date'),
        body('time')
            .trim()
            .notEmpty()
            .withMessage('Event time is required'),
        body('venue')
            .trim()
            .notEmpty()
            .withMessage('Venue is required'),
    ],
};

const profileValidation = {
    updateStudent: [
        body('mobile')
            .optional()
            .matches(/^[6-9]\d{9}$/)
            .withMessage('Please provide a valid 10-digit mobile number'),
        body('personalEmail')
            .optional()
            .isEmail()
            .withMessage('Please provide a valid personal email')
            .normalizeEmail(),
        body('collegeEmail')
            .optional()
            .isEmail()
            .withMessage('Please provide a valid college email')
            .normalizeEmail(),
        body('dob')
            .optional()
            .isISO8601()
            .withMessage('Date of birth must be a valid date'),
        body('dayScholarHosteller')
            .optional()
            .isIn(['Day Scholar', 'Hosteller'])
            .withMessage('Must be either Day Scholar or Hosteller'),
    ],
    updateFaculty: [
        body('mobile')
            .optional()
            .matches(/^[6-9]\d{9}$/)
            .withMessage('Please provide a valid 10-digit mobile number'),
        body('personalEmail')
            .optional()
            .isEmail()
            .withMessage('Please provide a valid personal email')
            .normalizeEmail(),
    ],
};

const academicYearValidation = {
    createOrUpdate: [
        body('name')
            .trim()
            .notEmpty()
            .withMessage('Academic Year name is required'),
        body('session')
            .trim()
            .notEmpty()
            .withMessage('Session is required'),
        body('status')
            .optional()
            .isIn(['active', 'inactive'])
            .withMessage('Status must be active or inactive'),
        body('order')
            .optional()
            .isInt()
            .withMessage('Order must be an integer'),
    ],
};

const sectionValidation = {
    createOrUpdate: [
        body('name')
            .trim()
            .notEmpty()
            .withMessage('Section name is required'),
        body('academicYear')
            .isMongoId()
            .withMessage('A valid Academic Year ID is required'),
        body('status')
            .optional()
            .isIn(['active', 'inactive'])
            .withMessage('Status must be active or inactive'),
        body('capacity')
            .optional()
            .isInt({ min: 1 })
            .withMessage('Capacity must be a positive integer'),
    ],
};

const paginationValidation = [
    query('page')
        .optional()
        .isInt({ min: 1 })
        .withMessage('Page must be a positive integer'),
    query('limit')
        .optional()
        .isInt({ min: 1, max: 100 })
        .withMessage('Limit must be between 1 and 100'),
];

const idValidation = [
    param('id')
        .isMongoId()
        .withMessage('Invalid ID format'),
];

module.exports = {
    validate,
    authValidation,
    notificationValidation,
    eventValidation,
    profileValidation,
    academicYearValidation,
    sectionValidation,
    paginationValidation,
    idValidation,
};
