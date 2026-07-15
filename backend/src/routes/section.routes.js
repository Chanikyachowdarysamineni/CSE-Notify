const router = require('express').Router();
const ctrl = require('../controllers/section.controller');
const { auth } = require('../middleware/auth');
const { adminOnly, allRoles } = require('../middleware/rbac');
const { sectionValidation, validate, idValidation } = require('../middleware/validate');

router.use(auth);

router.post('/', adminOnly, sectionValidation.createOrUpdate, validate, ctrl.createSection);
router.get('/', allRoles, ctrl.getSections);
router.put('/:id', adminOnly, idValidation, sectionValidation.createOrUpdate, validate, ctrl.updateSection);
router.delete('/bulk', adminOnly, ctrl.bulkDeleteSections);
router.delete('/:id', adminOnly, idValidation, validate, ctrl.deleteSection);

module.exports = router;
