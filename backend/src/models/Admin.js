/**
 * Admin Model - Extended profile for admin users
 */
const mongoose = require('mongoose');

const adminSchema = new mongoose.Schema({
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
        unique: true,
        trim: true,
        uppercase: true,
    },
    designation: {
        type: String,
        default: 'Administrator',
        trim: true,
    },
    mobile: {
        type: String,
        trim: true,
    },
    photo: {
        type: String,
        default: '',
    },
}, {
    timestamps: true,
});

// No additional indexes needed — userId and employeeId have unique:true

module.exports = mongoose.model('Admin', adminSchema);
