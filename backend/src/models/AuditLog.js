/**
 * Audit Log Model - Security audit trail
 */
const mongoose = require('mongoose');

const auditLogSchema = new mongoose.Schema({
    userId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User',
    },
    action: {
        type: String,
        required: [true, 'Action is required'],
        enum: [
            'LOGIN', 'LOGOUT', 'PASSWORD_CHANGE', 'PASSWORD_RESET',
            'CREATE', 'UPDATE', 'DELETE',
            'UPLOAD', 'DOWNLOAD',
            'NOTIFICATION_SENT', 'NOTIFICATION_READ',
            'CSV_IMPORT', 'CSV_EXPORT',
            'PROFILE_UPDATE',
            'USER_ACTIVATE', 'USER_DEACTIVATE',
        ],
    },
    resource: {
        type: String,
        required: true,
        enum: [
            'auth', 'user', 'student', 'faculty', 'admin',
            'notification', 'event', 'timetable', 'file', 'gallery', 'profile',
        ],
    },
    resourceId: {
        type: mongoose.Schema.Types.ObjectId,
    },
    details: {
        type: mongoose.Schema.Types.Mixed,
    },
    ipAddress: {
        type: String,
    },
    userAgent: {
        type: String,
    },
}, {
    timestamps: true,
});

// Indexes
auditLogSchema.index({ userId: 1 });
auditLogSchema.index({ action: 1 });
auditLogSchema.index({ resource: 1 });
auditLogSchema.index({ createdAt: -1 });

// Auto-expire old logs after 90 days
auditLogSchema.index({ createdAt: 1 }, { expireAfterSeconds: 90 * 24 * 60 * 60 });

module.exports = mongoose.model('AuditLog', auditLogSchema);
