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
}, {
    timestamps: true,
});

// Indexes
deviceTokenSchema.index({ userId: 1, isActive: 1 }); // Compound for active token lookups
// Note: token has unique:true which already creates an index

module.exports = mongoose.model('DeviceToken', deviceTokenSchema);
