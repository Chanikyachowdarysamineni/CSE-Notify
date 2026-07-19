/**
 * Profile Routes
 */
const router = require('express').Router();
const ctrl = require('../controllers/profile.controller');
const { auth } = require('../middleware/auth');
const { allRoles } = require('../middleware/rbac');
const { profileValidation, validate } = require('../middleware/validate');
const { uploadProfile } = require('../config/multer');
const { ROLES } = require('../utils/constants');

router.use(auth);

// Self Profile routes
router.get('/', allRoles, ctrl.getProfile);
router.put('/', allRoles, ctrl.updateProfile);
router.put('/photo', allRoles, uploadProfile.single('photo'), ctrl.updateProfilePhoto);

// Admin/Faculty routes for managing specific student profiles
const adminOrFaculty = require('../middleware/rbac').authorize(ROLES.ADMIN, ROLES.FACULTY);
const adminOnly = require('../middleware/rbac').authorize(ROLES.ADMIN);

router.get('/student/:studentId', adminOrFaculty, ctrl.getStudentProfileById);
router.put('/student/:studentId', adminOnly, ctrl.updateStudentProfileById);

module.exports = router;
