/**
 * Device Token Model - FCM token management
 */
const mongoose = require('mongoose');

const deviceTokenSchema = new mongoose.Schema({
    userId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User',
        required: true,
    },
    role: {
        type: String,
        enum: ['admin', 'faculty', 'student'],
        required: [true, 'User role is required'],
    },
    token: {
        type: String,
        required: [true, 'Device token is required'],
        unique: true,
    },
    platform: {
        type: String,
        enum: ['android', 'ios', 'web'],
        default: 'android',
    },
    isActive: {
        type: Boolean,
        default: true,
    },
    lastUsed: {
        type: Date,
        default: Date.now,
    },
    // Device identification for multi-device tracking
    deviceId: {
        type: String,
        trim: true,
        default: null,
    },
    deviceModel: {
        type: String,
        trim: true,
        default: null,
    },
    appVersion: {
        type: String,
        trim: true,
        default: null,
    },
}, {
    timestamps: true,
});

// Indexes
deviceTokenSchema.index({ userId: 1, isActive: 1 }); // Compound for active token lookups
deviceTokenSchema.index({ role: 1, isActive: 1 });    // Targeted delivery by role
deviceTokenSchema.index({ deviceId: 1 }, { sparse: true }); // Multi-device lookup
// Note: token has unique:true which already creates an index

module.exports = mongoose.model('DeviceToken', deviceTokenSchema);
