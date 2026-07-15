/**
 * Notification Read Status Model
 */
const mongoose = require('mongoose');

const notificationReadSchema = new mongoose.Schema({
    userId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User',
        required: true,
    },
    notificationId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Notification',
        required: true,
    },
    readAt: {
        type: Date,
        default: Date.now,
    },
}, {
    timestamps: true,
});

// Compound unique index - a user can only read a notification once
notificationReadSchema.index({ userId: 1, notificationId: 1 }, { unique: true });
notificationReadSchema.index({ notificationId: 1 });

module.exports = mongoose.model('NotificationRead', notificationReadSchema);
