/**
 * Profile Routes
 */
const router = require('express').Router();
const ctrl = require('../controllers/profile.controller');
const { auth } = require('../middleware/auth');
const { allRoles } = require('../middleware/rbac');
const { profileValidation, validate } = require('../middleware/validate');
const { uploadProfile } = require('../config/multer');

router.use(auth);

router.get('/', allRoles, ctrl.getProfile);
router.put('/', allRoles, ctrl.updateProfile);
router.put('/photo', allRoles, uploadProfile.single('photo'), ctrl.updateProfilePhoto);

module.exports = router;
