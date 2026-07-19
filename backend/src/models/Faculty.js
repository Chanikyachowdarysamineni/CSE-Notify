/**
 * Faculty Model - Extended profile for faculty users
 */
const mongoose = require('mongoose');

const facultySchema = new mongoose.Schema({
    userId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User',
        required: true,
        unique: true,
    },
    name: {
        type: String,
        required: [true, 'Name is required'],
        trim: true,
    },
    employeeId: {
        type: String,
        required: [true, 'Employee ID is required'],
        unique: true,
        trim: true,
        uppercase: true,
    },
    designation: {
        type: String,
        required: [true, 'Designation is required'],
        trim: true,
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
    photo: {
        type: String,
        default: '',
    },
    subjects: [{
        type: String,
        trim: true,
    }],
}, {
    timestamps: true,
});

// Indexes (employeeId and userId have unique:true which already creates indexes)
facultySchema.index({ name: 1 }); // Optimize name-based searches

module.exports = mongoose.model('Faculty', facultySchema);
