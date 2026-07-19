/**
 * NotificationLog Model
 *
 * Persists the delivery outcome of every FCM push attempt.
 * Enables admins to audit "was this notification delivered?" and diagnose failures.
 */
const mongoose = require('mongoose');

const notificationLogSchema = new mongoose.Schema({
    notificationId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Notification',
        required: true,
    },
    triggeredBy: {
        type: String,
        enum: ['manual', 'scheduled', 'birthday', 'timetable', 'system'],
        default: 'manual',
    },
    sentAt: {
        type: Date,
        default: Date.now,
    },
    totalTargeted: {
        type: Number,
        default: 0,
    },
    successCount: {
        type: Number,
        default: 0,
    },
    failureCount: {
        type: Number,
        default: 0,
    },
    invalidTokens: [{
        type: String,
    }],
    errorMessage: {
        type: String,
        default: null,
    },
    // Snapshot of targeting at send time
    targetYears: [{
        type: mongoose.Schema.Types.ObjectId,
        ref: 'AcademicYear',
    }],
    targetSections: [{
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Section',
    }],
    isBroadcast: {
        type: Boolean,
        default: false,
    },
}, {
    timestamps: true,
});

// Indexes for delivery audit queries
notificationLogSchema.index({ notificationId: 1 });
notificationLogSchema.index({ sentAt: -1 });
notificationLogSchema.index({ triggeredBy: 1, sentAt: -1 });

// TTL: auto-expire logs older than 90 days
notificationLogSchema.index({ sentAt: 1 }, { expireAfterSeconds: 90 * 24 * 60 * 60 });

module.exports = mongoose.model('NotificationLog', notificationLogSchema);
