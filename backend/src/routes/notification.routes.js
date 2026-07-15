/**
 * Notification Routes
 */
const router = require('express').Router();
const ctrl = require('../controllers/notification.controller');
const { auth } = require('../middleware/auth');
const { adminOrFaculty, adminOnly, allRoles } = require('../middleware/rbac');
const { notificationValidation, validate, idValidation, paginationValidation } = require('../middleware/validate');
const { uploadNotification } = require('../config/multer');

// All routes require authentication
router.use(auth);

router.get('/', allRoles, paginationValidation, validate, ctrl.getNotifications);
router.get('/unread-count', allRoles, ctrl.getUnreadCount);
router.get('/:id', allRoles, idValidation, validate, ctrl.getNotificationById);
router.post('/', adminOrFaculty, uploadNotification.single('attachment'), notificationValidation.create, validate, ctrl.createNotification);
router.put('/:id', adminOrFaculty, uploadNotification.single('attachment'), idValidation, validate, ctrl.updateNotification);
router.delete('/:id', adminOnly, idValidation, validate, ctrl.deleteNotification);
router.post('/:id/read', allRoles, idValidation, validate, ctrl.markAsRead);

module.exports = router;
