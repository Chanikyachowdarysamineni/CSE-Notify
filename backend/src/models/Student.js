/**
 * Student Model - Extended profile for student users
 */
const mongoose = require('mongoose');

const studentSchema = new mongoose.Schema({
    userId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User',
        required: true,
        unique: true,
    },
    regNo: {
        type: String,
        required: [true, 'Registration number is required'],
        unique: true,
        trim: true,
        uppercase: true,
    },
    name: {
        type: String,
        required: [true, 'Name is required'],
        trim: true,
    },
    gender: {
        type: String,
        enum: ['Male', 'Female', 'Other'],
    },
    branch: {
        type: String,
        trim: true,
    },
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
    mobile: {
        type: String,
        trim: true,
        match: [/^[6-9]\d{9}$/, 'Please provide a valid 10-digit mobile number'],
    },
    personalEmail: {
        type: String,
        trim: true,
        lowercase: true,
    },
    collegeEmail: {
        type: String,
        trim: true,
        lowercase: true,
    },
    dob: {
        type: Date,
    },
    dayScholarHosteller: {
        type: String,
        enum: ['Day Scholar', 'Hosteller'],
        default: 'Day Scholar',
    },
    cgpa: {
        type: Number,
        min: 0,
        max: 10,
        default: 0,
    },
    profilePhoto: {
        type: String, // URL/path to photo
        default: '',
    },
}, {
    timestamps: true,
});

// Indexes
studentSchema.index({ regNo: 1 });
studentSchema.index({ academicYear: 1, section: 1 });
studentSchema.index({ userId: 1 });

module.exports = mongoose.model('Student', studentSchema);
