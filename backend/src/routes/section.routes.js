const router = require('express').Router();
const ctrl = require('../controllers/section.controller');
const { auth } = require('../middleware/auth');
const { adminOnly, allRoles } = require('../middleware/rbac');

router.use(auth);

router.post('/', adminOnly, ctrl.createSection);
router.get('/', allRoles, ctrl.getSections);
router.put('/:id', adminOnly, ctrl.updateSection);
router.delete('/bulk', adminOnly, ctrl.bulkDeleteSections);
router.delete('/:id', adminOnly, ctrl.deleteSection);

module.exports = router;
