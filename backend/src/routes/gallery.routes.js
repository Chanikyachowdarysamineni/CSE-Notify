/**
 * Gallery Routes
 */
const router = require('express').Router();
const ctrl = require('../controllers/gallery.controller');
const { auth } = require('../middleware/auth');
const { adminOrFaculty, allRoles } = require('../middleware/rbac');
const { validate, idValidation, paginationValidation } = require('../middleware/validate');
const { uploadGallery } = require('../config/multer');

router.use(auth);

router.get('/', allRoles, paginationValidation, validate, ctrl.getGalleryPosts);
router.post('/', adminOrFaculty, uploadGallery.single('image'), ctrl.createGalleryPost);
router.put('/:id', adminOrFaculty, uploadGallery.single('image'), idValidation, validate, ctrl.updateGalleryPost);
router.delete('/:id', adminOrFaculty, idValidation, validate, ctrl.deleteGalleryPost);

module.exports = router;
