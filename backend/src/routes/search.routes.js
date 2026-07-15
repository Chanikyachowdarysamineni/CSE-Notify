/**
 * Search Routes
 */
const router = require('express').Router();
const { searchStudent } = require('../controllers/search.controller');
const { auth } = require('../middleware/auth');
const { adminOrFaculty } = require('../middleware/rbac');

router.use(auth);

router.get('/student', adminOrFaculty, searchStudent);

module.exports = router;
