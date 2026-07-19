/**
 * Refresh Token Model
 * Maps hashed refresh tokens to users, enabling secure token rotation.
 */
const mongoose = require('mongoose');

const refreshTokenSchema = new mongoose.Schema({
    userId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User',
        required: true,
    },
    tokenHash: {
        type: String,
        required: true,
    },
    expiresAt: {
        type: Date,
        required: true,
    },
    isRevoked: {
        type: Boolean,
        default: false,
    },
    deviceInfo: {
        type: String, // Store basic device info if possible
    }
}, { timestamps: true });

// TTL index to automatically delete expired refresh tokens
refreshTokenSchema.index({ expiresAt: 1 }, { expireAfterSeconds: 0 });
refreshTokenSchema.index({ userId: 1 }); // Optimize token invalidation queries

module.exports = mongoose.model('RefreshToken', refreshTokenSchema);
