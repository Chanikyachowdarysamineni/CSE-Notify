const mongoose = require('mongoose');

const sectionSchema = new mongoose.Schema({
    name: {
        type: String,
        required: [true, 'Section name is required'],
        trim: true,
        uppercase: true,
    },
    academicYear: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'AcademicYear',
        required: [true, 'Academic year reference is required'],
    },
    status: {
        type: String,
        enum: ['active', 'inactive'],
        default: 'active',
    },
    capacity: {
        type: Number,
        default: 60,
    },
    order: {
        type: Number,
        default: 0,
    },
}, {
    timestamps: true,
});

// Compound index to guarantee uniqueness of section name per academic year
sectionSchema.index({ name: 1, academicYear: 1 }, { unique: true });

module.exports = mongoose.model('Section', sectionSchema);
