/**
 * File Controller
 * Upload, download, preview, search, delete
 */
const File = require('../models/File');
const Student = require('../models/Student');
const { apiResponse, paginationMeta, ROLES } = require('../utils/constants');
const { createAuditLog } = require('../services/audit.service');
const logger = require('../utils/logger');
const path = require('path');
const fs = require('fs');

/**
 * GET /api/files
 */
const getFiles = async (req, res, next) => {
    try {
        const { page = 1, limit = 20, category, type, search } = req.query;
        const skip = (page - 1) * limit;

        let query = {};
        if (category) query.category = category;
        if (type) query.type = type;
        if (search) query.$text = { $search: search };

        // Student targeting: Only see files assigned to their own Year/Section or department-wide
        if (req.user.role === ROLES.STUDENT) {
            const student = await Student.findOne({ userId: req.user.id });
            if (student) {
                query.$and = [
                    {
                        $or: [
                            { targetYears: student.academicYear },
                            { targetYears: { $size: 0 } },
                            { targetYears: { $exists: false } },
                        ]
                    },
                    {
                        $or: [
                            { targetSections: student.section },
                            { targetSections: { $size: 0 } },
                            { targetSections: { $exists: false } },
                        ]
                    }
                ];
            }
        }

        const [files, total] = await Promise.all([
            File.find(query)
                .populate('uploadedBy', 'name role')
                .sort({ createdAt: -1 })
                .skip(skip)
                .limit(parseInt(limit))
                .lean(),
            File.countDocuments(query),
        ]);

        return apiResponse(res, 200, true, 'Files retrieved', files,
            paginationMeta(page, limit, total));
    } catch (error) {
        next(error);
    }
};

/**
 * POST /api/files/upload
 */
const uploadFile = async (req, res, next) => {
    try {
        if (!req.file) {
            return apiResponse(res, 400, false, 'File is required');
        }

        const ext = path.extname(req.file.originalname).toLowerCase().replace('.', '');

        const fileData = {
            name: req.body.name || req.file.originalname,
            originalName: req.file.originalname,
            type: ext,
            mimeType: req.file.mimetype,
            size: req.file.size,
            path: `/uploads/files/${req.file.filename}`,
            category: req.body.category || 'Others',
            description: req.body.description || '',
            targetYears: req.body.targetYears ? JSON.parse(req.body.targetYears) : [],
            targetSections: req.body.targetSections ? JSON.parse(req.body.targetSections) : [],
            uploadedBy: req.user.id,
        };

        const file = await File.create(fileData);

        await createAuditLog(req, 'UPLOAD', 'file', file._id, { name: file.name, size: file.size });

        return apiResponse(res, 201, true, 'File uploaded successfully', file);
    } catch (error) {
        next(error);
    }
};

/**
 * GET /api/files/download/:id
 */
const downloadFile = async (req, res, next) => {
    try {
        const file = await File.findById(req.params.id);
        if (!file) {
            return apiResponse(res, 404, false, 'File not found');
        }

        const filePath = path.join(__dirname, '../../', file.path);
        if (!fs.existsSync(filePath)) {
            return apiResponse(res, 404, false, 'File not found on server');
        }

        // Increment download count
        await File.findByIdAndUpdate(req.params.id, { $inc: { downloadCount: 1 } });

        await createAuditLog(req, 'DOWNLOAD', 'file', file._id, { name: file.name });

        res.download(filePath, file.originalName);
    } catch (error) {
        next(error);
    }
};

/**
 * DELETE /api/files/:id
 */
const deleteFile = async (req, res, next) => {
    try {
        const file = await File.findById(req.params.id);
        if (!file) {
            return apiResponse(res, 404, false, 'File not found');
        }

        // Check ownership for faculty
        if (req.user.role === ROLES.FACULTY &&
            file.uploadedBy.toString() !== req.user.id.toString()) {
            return apiResponse(res, 403, false, 'You can only delete your own files');
        }

        // Delete physical file
        const filePath = path.join(__dirname, '../../', file.path);
        if (fs.existsSync(filePath)) {
            fs.unlinkSync(filePath);
        }

        await File.findByIdAndDelete(req.params.id);

        await createAuditLog(req, 'DELETE', 'file', file._id, { name: file.name });

        return apiResponse(res, 200, true, 'File deleted');
    } catch (error) {
        next(error);
    }
};

module.exports = { getFiles, uploadFile, downloadFile, deleteFile };
