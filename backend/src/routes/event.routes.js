/**
 * Event Routes
 */
const router = require('express').Router();
const ctrl = require('../controllers/event.controller');
const { auth } = require('../middleware/auth');
const { adminOrFaculty, adminOnly, allRoles } = require('../middleware/rbac');
const { eventValidation, validate, idValidation, paginationValidation } = require('../middleware/validate');
const { uploadEvent } = require('../config/multer');

router.use(auth);

router.get('/', allRoles, paginationValidation, validate, ctrl.getEvents);
router.get('/:id', allRoles, idValidation, validate, ctrl.getEventById);
router.post('/', adminOrFaculty, uploadEvent.single('bannerImage'), eventValidation.create, validate, ctrl.createEvent);
router.put('/:id', adminOrFaculty, uploadEvent.single('bannerImage'), idValidation, validate, ctrl.updateEvent);
router.delete('/:id', adminOnly, idValidation, validate, ctrl.deleteEvent);

module.exports = router;
