/**
 * Profile Controller
 * Get and update profile for authenticated user
 */
const User = require('../models/User');
const Student = require('../models/Student');
const Faculty = require('../models/Faculty');
const Admin = require('../models/Admin');
const { apiResponse, ROLES } = require('../utils/constants');
const { createAuditLog } = require('../services/audit.service');
const logger = require('../utils/logger');

/**
 * GET /api/profile
 */
const getProfile = async (req, res, next) => {
    try {
        const user = await User.findById(req.user.id);
        if (!user) {
            return apiResponse(res, 404, false, 'User not found');
        }

        let profile = null;
        switch (user.role) {
            case ROLES.STUDENT:
                profile = await Student.findOne({ userId: user._id })
                    .populate('academicYear', 'name session')
                    .populate('section', 'name');
                break;
            case ROLES.FACULTY:
                profile = await Faculty.findOne({ userId: user._id });
                break;
            case ROLES.ADMIN:
                profile = await Admin.findOne({ userId: user._id });
                break;
        }

        return apiResponse(res, 200, true, 'Profile retrieved', {
            user: {
                id: user._id,
                email: user.email,
                name: user.name,
                role: user.role,
                lastLogin: user.lastLogin,
            },
            profile,
        });
    } catch (error) {
        next(error);
    }
};

/**
 * PUT /api/profile
 * Update profile (role-specific editable fields)
 */
const updateProfile = async (req, res, next) => {
    try {
        const user = await User.findById(req.user.id);
        if (!user) {
            return apiResponse(res, 404, false, 'User not found');
        }

        let updated = null;

        if (user.role === ROLES.STUDENT) {
            // Students can edit: mobile, personalEmail, collegeEmail, dob, dayScholarHosteller
            const allowed = {};
            const editableFields = ['mobile', 'personalEmail', 'collegeEmail', 'dob', 'dayScholarHosteller', 'section'];
            editableFields.forEach(field => {
                if (req.body[field] !== undefined) allowed[field] = req.body[field];
            });

            updated = await Student.findOneAndUpdate(
                { userId: user._id },
                { $set: allowed },
                { new: true, runValidators: true }
            );
        } else if (user.role === ROLES.FACULTY) {
            // Faculty can edit: mobile, personalEmail
            const allowed = {};
            const editableFields = ['mobile', 'personalEmail'];
            editableFields.forEach(field => {
                if (req.body[field] !== undefined) allowed[field] = req.body[field];
            });

            updated = await Faculty.findOneAndUpdate(
                { userId: user._id },
                { $set: allowed },
                { new: true, runValidators: true }
            );
        }

        await createAuditLog(req, 'PROFILE_UPDATE', 'profile', user._id);

        return apiResponse(res, 200, true, 'Profile updated', updated);
    } catch (error) {
        next(error);
    }
};

/**
 * PUT /api/profile/photo
 * Update profile photo
 */
const updateProfilePhoto = async (req, res, next) => {
    try {
        if (!req.file) {
            return apiResponse(res, 400, false, 'Photo is required');
        }

        const photoPath = `/uploads/profiles/${req.file.filename}`;
        const user = await User.findById(req.user.id);

        if (user.role === ROLES.STUDENT) {
            await Student.findOneAndUpdate(
                { userId: user._id },
                { profilePhoto: photoPath }
            );
        } else if (user.role === ROLES.FACULTY) {
            await Faculty.findOneAndUpdate(
                { userId: user._id },
                { photo: photoPath }
            );
        } else if (user.role === ROLES.ADMIN) {
            await Admin.findOneAndUpdate(
                { userId: user._id },
                { photo: photoPath }
            );
        }

        await createAuditLog(req, 'PROFILE_UPDATE', 'profile', user._id, { field: 'photo' });

        return apiResponse(res, 200, true, 'Profile photo updated', { photo: photoPath });
    } catch (error) {
        next(error);
    }
};

module.exports = { getProfile, updateProfile, updateProfilePhoto };
