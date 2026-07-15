/**
 * Role-Based Access Control (RBAC) Middleware
 * 
 * Validates that the authenticated user has one of the allowed roles.
 * Must be used after the auth middleware.
 */
const { apiResponse, ROLES } = require('../utils/constants');
const logger = require('../utils/logger');

/**
 * Restrict access to specific roles
 * @param  {...string} allowedRoles - Roles that can access the route
 */
const authorize = (...allowedRoles) => {
    return (req, res, next) => {
        if (!req.user) {
            return apiResponse(res, 401, false, 'Authentication required.');
        }

        if (!allowedRoles.includes(req.user.role)) {
            logger.warn(`RBAC: User ${req.user.id} (${req.user.role}) attempted to access restricted route. Required roles: [${allowedRoles.join(', ')}]`);
            return apiResponse(res, 403, false, 'Access denied. Insufficient permissions.');
        }

        next();
    };
};

/**
 * Admin only access
 */
const adminOnly = authorize(ROLES.ADMIN);

/**
 * Admin and Faculty access
 */
const adminOrFaculty = authorize(ROLES.ADMIN, ROLES.FACULTY);

/**
 * All authenticated users
 */
const allRoles = authorize(ROLES.ADMIN, ROLES.FACULTY, ROLES.STUDENT);

module.exports = {
    authorize,
    adminOnly,
    adminOrFaculty,
    allRoles,
};
