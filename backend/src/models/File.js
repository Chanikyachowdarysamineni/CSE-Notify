/**
 * File Model - Uploaded documents and media
 */
const mongoose = require('mongoose');

const fileSchema = new mongoose.Schema({
    name: {
        type: String,
        required: [true, 'File name is required'],
        trim: true,
    },
    originalName: {
        type: String,
        required: true,
        trim: true,
    },
    type: {
        type: String,
        required: [true, 'File type is required'],
        trim: true,
    },
    mimeType: {
        type: String,
        required: true,
    },
    size: {
        type: Number,
        required: [true, 'File size is required'],
    },
    path: {
        type: String,
        required: [true, 'File path is required'],
    },
    category: {
        type: String,
        default: 'Others',
    },
    description: {
        type: String,
        trim: true,
        maxlength: [500, 'Description must be under 500 characters'],
    },
    targetYears: [{
        type: mongoose.Schema.Types.ObjectId,
        ref: 'AcademicYear',
    }],
    targetSections: [{
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Section',
    }],
    uploadedBy: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User',
        required: true,
    },
    downloadCount: {
        type: Number,
        default: 0,
    },
}, {
    timestamps: true,
});

// Indexes
fileSchema.index({ category: 1 });
fileSchema.index({ type: 1 });
fileSchema.index({ uploadedBy: 1 });
fileSchema.index({ createdAt: -1 });
fileSchema.index({ name: 'text', description: 'text' });

module.exports = mongoose.model('File', fileSchema);
