/**
 * Event Model
 */
const mongoose = require('mongoose');

const eventSchema = new mongoose.Schema({
    title: {
        type: String,
        required: [true, 'Title is required'],
        trim: true,
        maxlength: [200, 'Title must be under 200 characters'],
    },
    description: {
        type: String,
        required: [true, 'Description is required'],
        trim: true,
    },
    eventType: {
        type: String,
        required: [true, 'Event type is required'],
    },
    date: {
        type: Date,
        required: [true, 'Event date is required'],
    },
    time: {
        type: String,
        required: [true, 'Event time is required'],
        trim: true,
    },
    venue: {
        type: String,
        required: [true, 'Venue is required'],
        trim: true,
    },
    targetYears: [{
        type: mongoose.Schema.Types.ObjectId,
        ref: 'AcademicYear',
    }],
    targetSections: [{
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Section',
    }],
    bannerImage: {
        type: String,
        default: '',
    },
    registrationLink: {
        type: String,
        trim: true,
    },
    contactPerson: {
        type: String,
        trim: true,
    },
    createdBy: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User',
        required: true,
    },
    isActive: {
        type: Boolean,
        default: true,
    },
}, {
    timestamps: true,
});

// Indexes
eventSchema.index({ date: -1 });
eventSchema.index({ eventType: 1 });
eventSchema.index({ targetYears: 1 });
eventSchema.index({ targetSections: 1 });
eventSchema.index({ title: 'text', description: 'text' });

module.exports = mongoose.model('Event', eventSchema);
