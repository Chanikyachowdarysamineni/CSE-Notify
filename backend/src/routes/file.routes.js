/**
 * File Routes
 */
const router = require('express').Router();
const ctrl = require('../controllers/file.controller');
const { auth } = require('../middleware/auth');
const { adminOrFaculty, allRoles } = require('../middleware/rbac');
const { validate, idValidation, paginationValidation } = require('../middleware/validate');
const { uploadFile } = require('../config/multer');
const { uploadLimiter } = require('../middleware/rateLimiter');

router.use(auth);

router.get('/', allRoles, paginationValidation, validate, ctrl.getFiles);
router.post('/upload', adminOrFaculty, uploadLimiter, uploadFile.single('file'), ctrl.uploadFile);
router.get('/download/:id', allRoles, idValidation, validate, ctrl.downloadFile);
router.delete('/:id', adminOrFaculty, idValidation, validate, ctrl.deleteFile);

module.exports = router;
