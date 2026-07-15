/**
 * Multer File Upload Configuration
 * 
 * Supports: PDF, DOC, DOCX, PPT, PPTX, XLS, XLSX, ZIP, Images, Videos, APK, CSV
 */
const multer = require('multer');
const path = require('path');
const fs = require('fs');

// Ensure upload directories exist
const uploadDirs = ['uploads', 'uploads/files', 'uploads/gallery', 'uploads/profiles', 'uploads/events', 'uploads/notifications'];
uploadDirs.forEach(dir => {
    const fullPath = path.join(__dirname, '../../', dir);
    if (!fs.existsSync(fullPath)) {
        fs.mkdirSync(fullPath, { recursive: true });
    }
});

// Allowed MIME types
const ALLOWED_MIME_TYPES = {
    // Documents
    'application/pdf': '.pdf',
    'application/msword': '.doc',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document': '.docx',
    'application/vnd.ms-powerpoint': '.ppt',
    'application/vnd.openxmlformats-officedocument.presentationml.presentation': '.pptx',
    'application/vnd.ms-excel': '.xls',
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': '.xlsx',
    // Archives
    'application/zip': '.zip',
    'application/x-zip-compressed': '.zip',
    // Images
    'image/jpeg': '.jpg',
    'image/png': '.png',
    'image/gif': '.gif',
    'image/webp': '.webp',
    'image/svg+xml': '.svg',
    // Videos
    'video/mp4': '.mp4',
    'video/mpeg': '.mpeg',
    'video/quicktime': '.mov',
    'video/x-msvideo': '.avi',
    // Android
    'application/vnd.android.package-archive': '.apk',
    // CSV
    'text/csv': '.csv',
    'application/csv': '.csv',
};

// Image-only MIME types
const IMAGE_MIME_TYPES = {
    'image/jpeg': '.jpg',
    'image/png': '.png',
    'image/gif': '.gif',
    'image/webp': '.webp',
};

/**
 * Create multer storage engine for a given subdirectory
 */
const createStorage = (subDir) => {
    return multer.diskStorage({
        destination: (req, file, cb) => {
            const uploadPath = path.join(__dirname, '../../uploads', subDir);
            cb(null, uploadPath);
        },
        filename: (req, file, cb) => {
            const uniqueSuffix = `${Date.now()}-${Math.round(Math.random() * 1E9)}`;
            const ext = path.extname(file.originalname).toLowerCase();
            cb(null, `${subDir}-${uniqueSuffix}${ext}`);
        }
    });
};

/**
 * File filter factory
 */
const createFileFilter = (allowedTypes) => {
    return (req, file, cb) => {
        if (allowedTypes[file.mimetype]) {
            cb(null, true);
        } else {
            cb(new Error(`File type not allowed. Allowed types: ${Object.values(allowedTypes).join(', ')}`), false);
        }
    };
};

// Max file size (50MB default)
const maxFileSize = parseInt(process.env.MAX_FILE_SIZE) || 52428800;

// Upload configurations for different modules
const uploadFile = multer({
    storage: createStorage('files'),
    fileFilter: createFileFilter(ALLOWED_MIME_TYPES),
    limits: { fileSize: maxFileSize }
});

const uploadGallery = multer({
    storage: createStorage('gallery'),
    fileFilter: createFileFilter(IMAGE_MIME_TYPES),
    limits: { fileSize: 10 * 1024 * 1024 } // 10MB for images
});

const uploadProfile = multer({
    storage: createStorage('profiles'),
    fileFilter: createFileFilter(IMAGE_MIME_TYPES),
    limits: { fileSize: 5 * 1024 * 1024 } // 5MB for profile photos
});

const uploadEvent = multer({
    storage: createStorage('events'),
    fileFilter: createFileFilter(IMAGE_MIME_TYPES),
    limits: { fileSize: 10 * 1024 * 1024 } // 10MB for event banners
});

const uploadNotification = multer({
    storage: createStorage('notifications'),
    fileFilter: createFileFilter(ALLOWED_MIME_TYPES),
    limits: { fileSize: maxFileSize }
});

const uploadCSV = multer({
    storage: createStorage('files'),
    fileFilter: (req, file, cb) => {
        if (file.mimetype === 'text/csv' || file.mimetype === 'application/csv' ||
            path.extname(file.originalname).toLowerCase() === '.csv') {
            cb(null, true);
        } else {
            cb(new Error('Only CSV files are allowed'), false);
        }
    },
    limits: { fileSize: 10 * 1024 * 1024 } // 10MB for CSV
});

module.exports = {
    uploadFile,
    uploadGallery,
    uploadProfile,
    uploadEvent,
    uploadNotification,
    uploadCSV,
    ALLOWED_MIME_TYPES,
    IMAGE_MIME_TYPES,
};
