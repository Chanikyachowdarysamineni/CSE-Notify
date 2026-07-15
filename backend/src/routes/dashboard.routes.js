/**
 * Dashboard Routes
 */
const router = require('express').Router();
const { getDashboard } = require('../controllers/dashboard.controller');
const { auth } = require('../middleware/auth');
const { allRoles } = require('../middleware/rbac');

router.use(auth);

router.get('/', allRoles, getDashboard);

module.exports = router;
