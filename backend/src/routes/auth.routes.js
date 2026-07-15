/**
 * Auth Routes
 */
const router = require('express').Router();
const { login, forgotPassword, resetPassword, changePassword, logout, refreshFCMToken } = require('../controllers/auth.controller');
const { auth } = require('../middleware/auth');
const { authValidation, validate } = require('../middleware/validate');
const { authLimiter } = require('../middleware/rateLimiter');

router.post('/login', authLimiter, authValidation.login, validate, login);
router.post('/forgot-password', authLimiter, authValidation.forgotPassword, validate, forgotPassword);
router.post('/reset-password', authLimiter, authValidation.resetPassword, validate, resetPassword);
router.put('/change-password', auth, authValidation.changePassword, validate, changePassword);
router.post('/logout', auth, logout);
router.post('/refresh-token', auth, refreshFCMToken);

module.exports = router;
