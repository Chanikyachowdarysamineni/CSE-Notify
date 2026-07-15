/**
 * Gallery Controller
 * Instagram-style gallery CRUD with category filtering
 */
const Gallery = require('../models/Gallery');
const { apiResponse, paginationMeta, ROLES } = require('../utils/constants');
const { createAuditLog } = require('../services/audit.service');
const logger = require('../utils/logger');
const fs = require('fs');
const path = require('path');

/**
 * GET /api/gallery
 */
const getGalleryPosts = async (req, res) => {
    try {
        const { page = 1, limit = 20, category } = req.query;
        const skip = (page - 1) * limit;

        let query = {};
        if (category) query.category = category;

        const [posts, total] = await Promise.all([
            Gallery.find(query)
                .populate('postedBy', 'name role')
                .sort({ createdAt: -1 })
                .skip(skip)
                .limit(parseInt(limit))
                .lean(),
            Gallery.countDocuments(query),
        ]);

        return apiResponse(res, 200, true, 'Gallery posts retrieved', posts,
            paginationMeta(page, limit, total));
    } catch (error) {
        logger.error('Get gallery error:', error);
        return apiResponse(res, 500, false, 'Server error');
    }
};

/**
 * POST /api/gallery
 */
const createGalleryPost = async (req, res) => {
    try {
        if (!req.file) {
            return apiResponse(res, 400, false, 'Image is required');
        }

        const post = await Gallery.create({
            image: `/uploads/gallery/${req.file.filename}`,
            caption: req.body.caption || '',
            category: req.body.category,
            postedBy: req.user.id,
        });

        await createAuditLog(req, 'CREATE', 'gallery', post._id, { category: post.category });

        return apiResponse(res, 201, true, 'Gallery post created', post);
    } catch (error) {
        logger.error('Create gallery post error:', error);
        return apiResponse(res, 500, false, 'Server error');
    }
};

/**
 * PUT /api/gallery/:id
 */
const updateGalleryPost = async (req, res) => {
    try {
        const post = await Gallery.findById(req.params.id);
        if (!post) {
            return apiResponse(res, 404, false, 'Gallery post not found');
        }

        if (req.user.role === ROLES.FACULTY &&
            post.postedBy.toString() !== req.user.id.toString()) {
            return apiResponse(res, 403, false, 'You can only edit your own posts');
        }

        const updateData = {};
        if (req.body.caption !== undefined) updateData.caption = req.body.caption;
        if (req.body.category) updateData.category = req.body.category;
        if (req.file) {
            updateData.image = `/uploads/gallery/${req.file.filename}`;
            // Delete old image
            const oldPath = path.join(__dirname, '../../', post.image);
            if (fs.existsSync(oldPath)) fs.unlinkSync(oldPath);
        }

        const updated = await Gallery.findByIdAndUpdate(
            req.params.id,
            { $set: updateData },
            { new: true, runValidators: true }
        ).populate('postedBy', 'name role');

        await createAuditLog(req, 'UPDATE', 'gallery', post._id);

        return apiResponse(res, 200, true, 'Gallery post updated', updated);
    } catch (error) {
        logger.error('Update gallery post error:', error);
        return apiResponse(res, 500, false, 'Server error');
    }
};

/**
 * DELETE /api/gallery/:id
 */
const deleteGalleryPost = async (req, res) => {
    try {
        const post = await Gallery.findById(req.params.id);
        if (!post) {
            return apiResponse(res, 404, false, 'Gallery post not found');
        }

        if (req.user.role === ROLES.FACULTY &&
            post.postedBy.toString() !== req.user.id.toString()) {
            return apiResponse(res, 403, false, 'You can only delete your own posts');
        }

        // Delete image file
        const imagePath = path.join(__dirname, '../../', post.image);
        if (fs.existsSync(imagePath)) fs.unlinkSync(imagePath);

        await Gallery.findByIdAndDelete(req.params.id);

        await createAuditLog(req, 'DELETE', 'gallery', post._id);

        return apiResponse(res, 200, true, 'Gallery post deleted');
    } catch (error) {
        logger.error('Delete gallery post error:', error);
        return apiResponse(res, 500, false, 'Server error');
    }
};

module.exports = { getGalleryPosts, createGalleryPost, updateGalleryPost, deleteGalleryPost };
