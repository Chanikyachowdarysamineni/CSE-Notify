/**
 * Audit Log Service
 * Creates audit trail entries for security tracking
 */
const AuditLog = require('../models/AuditLog');
const logger = require('../utils/logger');

/**
 * Create an audit log entry
 */
const createAuditLog = async (req, action, resource, resourceId = null, details = null) => {
    try {
        await AuditLog.create({
            userId: req.user?.id || null,
            action,
            resource,
            resourceId,
            details,
            ipAddress: req.ip || req.connection?.remoteAddress,
            userAgent: req.headers?.['user-agent'],
        });
    } catch (error) {
        // Don't fail the request if audit logging fails
        logger.error('Audit log creation failed:', error.message);
    }
};

module.exports = { createAuditLog };
