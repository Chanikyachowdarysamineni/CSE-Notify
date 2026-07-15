/**
 * Timetable Model
 */
const mongoose = require('mongoose');

const timetableSchema = new mongoose.Schema({
    academicYear: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'AcademicYear',
        required: [true, 'Academic year is required'],
    },
    section: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Section',
        required: [true, 'Section is required'],
    },
    day: {
        type: String,
        required: [true, 'Day is required'],
        enum: ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'],
    },
    period: {
        type: Number,
        required: [true, 'Period is required'],
        min: 1,
        max: 10,
    },
    subject: {
        type: String,
        required: [true, 'Subject is required'],
        trim: true,
    },
    subjectCode: {
        type: String,
        trim: true,
    },
    faculty: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Faculty',
    },
    facultyName: {
        type: String,
        trim: true,
    },
    room: {
        type: String,
        trim: true,
    },
    startTime: {
        type: String,
        required: [true, 'Start time is required'],
    },
    endTime: {
        type: String,
        required: [true, 'End time is required'],
    },
    type: {
        type: String,
        enum: ['Theory', 'Lab', 'Tutorial', 'Break', 'Free'],
        default: 'Theory',
    },
}, {
    timestamps: true,
});

// Indexes for efficient timetable queries
timetableSchema.index({ academicYear: 1, section: 1, day: 1 });
timetableSchema.index({ faculty: 1, day: 1 });
timetableSchema.index({ academicYear: 1, section: 1, day: 1, period: 1 }, { unique: true });

module.exports = mongoose.model('Timetable', timetableSchema);
