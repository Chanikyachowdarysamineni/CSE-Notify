/**
 * JWT Authentication Middleware
 * 
 * Verifies the JWT token from the Authorization header and attaches
 * the decoded user payload to req.user.
 */
const jwt = require('jsonwebtoken');
const jwtConfig = require('../config/jwt');
const User = require('../models/User');
const { apiResponse } = require('../utils/constants');
const logger = require('../utils/logger');

const auth = async (req, res, next) => {
    try {
        // Extract token from Authorization header
        const authHeader = req.headers.authorization;
        if (!authHeader || !authHeader.startsWith('Bearer ')) {
            return apiResponse(res, 401, false, 'Access denied. No token provided.');
        }

        const token = authHeader.split(' ')[1];
        if (!token) {
            return apiResponse(res, 401, false, 'Access denied. Invalid token format.');
        }

        // Verify token
        const decoded = jwt.verify(token, jwtConfig.secret, {
            issuer: jwtConfig.issuer,
            audience: jwtConfig.audience,
        });

        // Check if user still exists and is active
        const user = await User.findById(decoded.id).select('-password');
        if (!user) {
            return apiResponse(res, 401, false, 'User not found. Token is invalid.');
        }

        if (!user.isActive) {
            return apiResponse(res, 403, false, 'Account is deactivated. Contact administrator.');
        }

        // Attach user to request
        req.user = {
            id: user._id,
            email: user.email,
            role: user.role,
            name: user.name,
        };

        next();
    } catch (error) {
        if (error.name === 'TokenExpiredError') {
            return apiResponse(res, 401, false, 'Token has expired. Please login again.');
        }
        if (error.name === 'JsonWebTokenError') {
            return apiResponse(res, 401, false, 'Invalid token. Please login again.');
        }
        logger.error('Auth middleware error:', error);
        return apiResponse(res, 500, false, 'Authentication error.');
    }
};

/**
 * Optional auth - does not block if no token, but attaches user if valid
 */
const optionalAuth = async (req, res, next) => {
    try {
        const authHeader = req.headers.authorization;
        if (!authHeader || !authHeader.startsWith('Bearer ')) {
            return next();
        }

        const token = authHeader.split(' ')[1];
        if (!token) return next();

        const decoded = jwt.verify(token, jwtConfig.secret, {
            issuer: jwtConfig.issuer,
            audience: jwtConfig.audience,
        });

        const user = await User.findById(decoded.id).select('-password');
        if (user && user.isActive) {
            req.user = {
                id: user._id,
                email: user.email,
                role: user.role,
                name: user.name,
            };
        }

        next();
    } catch (error) {
        // Silently proceed without auth
        next();
    }
};

module.exports = { auth, optionalAuth };
