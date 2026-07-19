/**
 * Notification Routes
 */
const router = require('express').Router();
const ctrl = require('../controllers/notification.controller');
const { auth } = require('../middleware/auth');
const { adminOrFaculty, adminOnly, allRoles } = require('../middleware/rbac');
const { notificationValidation, validate, idValidation, paginationValidation } = require('../middleware/validate');
const { uploadNotification } = require('../config/multer');

const { notificationLimiter } = require('../middleware/rateLimiter');

// All routes require authentication
router.use(auth);

// IMPORTANT: specific named routes MUST come before /:id to prevent route shadowing
router.get('/', allRoles, paginationValidation, validate, ctrl.getNotifications);
router.get('/unread-count', allRoles, ctrl.getUnreadCount);
router.post('/read-all', allRoles, ctrl.markAllAsRead);                                          // Mark ALL as read
router.get('/delivery-status/:id', adminOnly, idValidation, validate, ctrl.getDeliveryStatus);   // Admin delivery audit
router.get('/:id', allRoles, idValidation, validate, ctrl.getNotificationById);
router.post('/', adminOrFaculty, notificationLimiter, uploadNotification.single('attachment'), notificationValidation.create, validate, ctrl.createNotification);
router.put('/:id', adminOrFaculty, notificationLimiter, uploadNotification.single('attachment'), idValidation, validate, ctrl.updateNotification);
router.delete('/:id', adminOnly, notificationLimiter, idValidation, validate, ctrl.deleteNotification);
router.post('/:id/read', allRoles, idValidation, validate, ctrl.markAsRead);

module.exports = router;

