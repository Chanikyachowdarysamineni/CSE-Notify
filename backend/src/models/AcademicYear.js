const mongoose = require('mongoose');

const academicYearSchema = new mongoose.Schema({
    name: {
        type: String,
        required: [true, 'Academic year name is required'],
        trim: true,
    },
    session: {
        type: String,
        required: [true, 'Academic session is required'],
        trim: true,
    },
    status: {
        type: String,
        enum: ['active', 'inactive'],
        default: 'active',
    },
    order: {
        type: Number,
        default: 0,
    },
    createdBy: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User',
        required: true,
    },
}, {
    timestamps: true,
});

// Compound index to guarantee uniqueness of name + session combination
academicYearSchema.index({ name: 1, session: 1 }, { unique: true });

module.exports = mongoose.model('AcademicYear', academicYearSchema);
