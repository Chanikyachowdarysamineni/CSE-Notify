/**
 * Gallery Model - Instagram-style posts
 */
const mongoose = require('mongoose');

const gallerySchema = new mongoose.Schema({
    image: {
        type: String,
        required: [true, 'Image is required'],
    },
    caption: {
        type: String,
        trim: true,
        maxlength: [500, 'Caption must be under 500 characters'],
    },
    category: {
        type: String,
        required: [true, 'Category is required'],
    },
    postedBy: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User',
        required: true,
    },
}, {
    timestamps: true,
});

// Indexes
gallerySchema.index({ category: 1 });
gallerySchema.index({ createdAt: -1 });
gallerySchema.index({ postedBy: 1 });

module.exports = mongoose.model('Gallery', gallerySchema);
