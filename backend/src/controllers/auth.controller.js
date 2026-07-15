/**
 * Auth Controller
 * Handles login, forgot password, reset password, change password, logout
 */
const jwt = require('jsonwebtoken');
const crypto = require('crypto');
const User = require('../models/User');
const Student = require('../models/Student');
const Faculty = require('../models/Faculty');
const Admin = require('../models/Admin');
const DeviceToken = require('../models/DeviceToken');
const jwtConfig = require('../config/jwt');
const { apiResponse } = require('../utils/constants');
const { createAuditLog } = require('../services/audit.service');
const logger = require('../utils/logger');

/**
 * Generate JWT token
 */
const generateToken = (user) => {
    return jwt.sign(
        { id: user._id, email: user.email, role: user.role },
        jwtConfig.secret,
        {
            expiresIn: jwtConfig.expiresIn,
            issuer: jwtConfig.issuer,
            audience: jwtConfig.audience,
        }
    );
};

/**
 * POST /api/auth/login
 * Login with loginId (registration no / employee id) or mobile number
 */
const login = async (req, res) => {
    try {
        console.log("LOGIN PAYLOAD RECEIVED: ", req.body);
        const { loginId, password, fcmToken } = req.body;

        if (!loginId || !password) {
            return apiResponse(res, 400, false, 'Please provide login credentials');
        }

        // Find user by loginId
        let user = await User.findOne({ loginId: new RegExp(`^${loginId}$`, 'i') }).select('+password');
        
        // Fallback: check if loginId is a mobile number in profiles
        if (!user) {
            let profile = await Student.findOne({ mobile: loginId });
            if (!profile) profile = await Faculty.findOne({ mobile: loginId });
            if (!profile) profile = await Admin.findOne({ mobile: loginId });

            if (profile && profile.userId) {
                user = await User.findById(profile.userId).select('+password');
            }
        }

        if (!user) {
            return apiResponse(res, 401, false, 'Invalid login credentials');
        }

        // Check if account is active
        if (!user.isActive) {
            return apiResponse(res, 403, false, 'Account is deactivated. Contact administrator.');
        }

        // Verify password
        const isMatch = await user.comparePassword(password);
        if (!isMatch) {
            return apiResponse(res, 401, false, 'Invalid login credentials');
        }

        // Generate token
        const token = generateToken(user);

        // Update last login
        user.lastLogin = new Date();
        if (fcmToken) {
            user.fcmToken = fcmToken;
        }
        await user.save();

        // Save/update device token
        if (fcmToken) {
            await DeviceToken.findOneAndUpdate(
                { token: fcmToken },
                { userId: user._id, platform: 'android', isActive: true, lastUsed: new Date() },
                { upsert: true, new: true }
            );
        }

        // Get role-specific profile
        let profile = null;
        switch (user.role) {
            case 'student':
                profile = await Student.findOne({ userId: user._id });
                break;
            case 'faculty':
                profile = await Faculty.findOne({ userId: user._id });
                break;
            case 'admin':
                profile = await Admin.findOne({ userId: user._id });
                break;
        }

        // Audit log
        await createAuditLog(req, 'LOGIN', 'auth', user._id);

        return apiResponse(res, 200, true, 'Login successful', {
            token,
            user: {
                id: user._id,
                loginId: user.loginId,
                email: user.email,
                name: user.name,
                role: user.role,
            },
            profile,
        });
    } catch (error) {
        logger.error('Login error:', error);
        return apiResponse(res, 500, false, 'Server error during login');
    }
};

/**
 * POST /api/auth/forgot-password
 * Send password reset token
 */
const forgotPassword = async (req, res) => {
    try {
        const { email } = req.body;

        const user = await User.findOne({ email });
        if (!user) {
            // Don't reveal whether email exists
            return apiResponse(res, 200, true, 'If the email exists, a reset link has been sent.');
        }

        // Generate reset token
        const resetToken = crypto.randomBytes(32).toString('hex');
        const hashedToken = crypto.createHash('sha256').update(resetToken).digest('hex');

        user.resetPasswordToken = hashedToken;
        user.resetPasswordExpires = Date.now() + 30 * 60 * 1000; // 30 minutes
        await user.save();

        // In production, send email with reset link
        // For now, return the token (remove in production)
        logger.info(`Password reset token for ${email}: ${resetToken}`);

        return apiResponse(res, 200, true, 'If the email exists, a reset link has been sent.', {
            // Remove resetToken in production - only for development
            ...(process.env.NODE_ENV === 'development' && { resetToken }),
        });
    } catch (error) {
        logger.error('Forgot password error:', error);
        return apiResponse(res, 500, false, 'Server error');
    }
};

/**
 * POST /api/auth/reset-password
 * Reset password using token
 */
const resetPassword = async (req, res) => {
    try {
        const { token, password } = req.body;

        const hashedToken = crypto.createHash('sha256').update(token).digest('hex');

        const user = await User.findOne({
            resetPasswordToken: hashedToken,
            resetPasswordExpires: { $gt: Date.now() },
        }).select('+password');

        if (!user) {
            return apiResponse(res, 400, false, 'Invalid or expired reset token');
        }

        // Set new password
        user.password = password;
        user.resetPasswordToken = undefined;
        user.resetPasswordExpires = undefined;
        await user.save();

        // Audit log
        await createAuditLog(req, 'PASSWORD_RESET', 'auth', user._id);

        return apiResponse(res, 200, true, 'Password reset successful. Please login with your new password.');
    } catch (error) {
        logger.error('Reset password error:', error);
        return apiResponse(res, 500, false, 'Server error');
    }
};

/**
 * PUT /api/auth/change-password
 * Change password (authenticated)
 */
const changePassword = async (req, res) => {
    try {
        const { currentPassword, newPassword } = req.body;

        const user = await User.findById(req.user.id).select('+password');
        if (!user) {
            return apiResponse(res, 404, false, 'User not found');
        }

        // Verify current password
        const isMatch = await user.comparePassword(currentPassword);
        if (!isMatch) {
            return apiResponse(res, 401, false, 'Current password is incorrect');
        }

        // Set new password
        user.password = newPassword;
        await user.save();

        // Generate new token
        const token = generateToken(user);

        // Audit log
        await createAuditLog(req, 'PASSWORD_CHANGE', 'auth', user._id);

        return apiResponse(res, 200, true, 'Password changed successfully', { token });
    } catch (error) {
        logger.error('Change password error:', error);
        return apiResponse(res, 500, false, 'Server error');
    }
};

/**
 * POST /api/auth/logout
 * Logout and invalidate device token
 */
const logout = async (req, res) => {
    try {
        // Deactivate device token
        await DeviceToken.findOneAndUpdate(
            { userId: req.user.id },
            { isActive: false }
        );

        // Audit log
        await createAuditLog(req, 'LOGOUT', 'auth', req.user.id);

        return apiResponse(res, 200, true, 'Logged out successfully');
    } catch (error) {
        logger.error('Logout error:', error);
        return apiResponse(res, 500, false, 'Server error');
    }
};

/**
 * POST /api/auth/refresh-token
 * Refresh device FCM token
 */
const refreshFCMToken = async (req, res) => {
    try {
        const { fcmToken } = req.body;

        if (!fcmToken) {
            return apiResponse(res, 400, false, 'FCM token is required');
        }

        await DeviceToken.findOneAndUpdate(
            { userId: req.user.id },
            { token: fcmToken, isActive: true, lastUsed: new Date() },
            { upsert: true }
        );

        return apiResponse(res, 200, true, 'FCM token updated');
    } catch (error) {
        logger.error('Refresh FCM token error:', error);
        return apiResponse(res, 500, false, 'Server error');
    }
};

module.exports = {
    login,
    forgotPassword,
    resetPassword,
    changePassword,
    logout,
    refreshFCMToken,
};
