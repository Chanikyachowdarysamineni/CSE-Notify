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
    aadhaarNumber: {
        type: String,
        trim: true,
        match: [/^\d{12}$/, 'Aadhaar must be exactly 12 digits'],
    },
    panNumber: {
        type: String,
        trim: true,
        uppercase: true,
        match: [/^[A-Z]{5}[0-9]{4}[A-Z]{1}$/, 'Invalid PAN format'],
    },
    githubUrl: {
        type: String,
        trim: true,
        match: [/^https?:\/\/(www\.)?github\.com\/[a-zA-Z0-9_-]+\/?$/, 'Invalid GitHub URL'],
    },
    linkedinUrl: {
        type: String,
        trim: true,
        match: [/^https?:\/\/(www\.)?linkedin\.com\/in\/[a-zA-Z0-9_-]+\/?$/, 'Invalid LinkedIn URL'],
    },
    leetcodeUrl: {
        type: String,
        trim: true,
        match: [/^https?:\/\/(www\.)?leetcode\.com\/u?\/?[a-zA-Z0-9_-]+\/?$/, 'Invalid LeetCode URL'],
    },
}, {
    timestamps: true,
});

// Indexes (note: regNo and userId have unique:true which already creates indexes)
studentSchema.index({ academicYear: 1, section: 1 }); // Compound for notification targeting
studentSchema.index({ dob: 1 });                       // For birthday reminder cron job
studentSchema.index({ name: 1 });                      // Optimize search queries
studentSchema.index({ collegeEmail: 1 }, { unique: true, sparse: true }); // Prevent duplicate college emails

module.exports = mongoose.model('Student', studentSchema);
