/**
 * Search Routes
 */
const router = require('express').Router();
const { searchStudent } = require('../controllers/search.controller');
const { auth } = require('../middleware/auth');
const { adminOrFaculty } = require('../middleware/rbac');

const { searchLimiter } = require('../middleware/rateLimiter');

router.use(auth);

router.get('/student', adminOrFaculty, searchLimiter, searchStudent);

module.exports = router;
