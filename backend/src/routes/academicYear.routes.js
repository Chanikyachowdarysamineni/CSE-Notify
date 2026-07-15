const router = require('express').Router();
const ctrl = require('../controllers/academicYear.controller');
const { auth } = require('../middleware/auth');
const { adminOnly, allRoles } = require('../middleware/rbac');

router.use(auth);

router.post('/', adminOnly, ctrl.createAcademicYear);
router.get('/', allRoles, ctrl.getAcademicYears);
router.put('/:id', adminOnly, ctrl.updateAcademicYear);
router.delete('/:id', adminOnly, ctrl.deleteAcademicYear);

module.exports = router;
