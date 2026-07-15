const router = require('express').Router();
const ctrl = require('../controllers/academicYear.controller');
const { auth } = require('../middleware/auth');
const { adminOnly, allRoles } = require('../middleware/rbac');
const { academicYearValidation, validate, idValidation } = require('../middleware/validate');

router.use(auth);

router.post('/', adminOnly, academicYearValidation.createOrUpdate, validate, ctrl.createAcademicYear);
router.get('/', allRoles, ctrl.getAcademicYears);
router.put('/:id', adminOnly, idValidation, academicYearValidation.createOrUpdate, validate, ctrl.updateAcademicYear);
router.delete('/:id', adminOnly, idValidation, validate, ctrl.deleteAcademicYear);

module.exports = router;
