const router = require('express').Router();
const ctrl = require('../controllers/config.controller');
const { auth } = require('../middleware/auth');
const { allRoles } = require('../middleware/rbac');

// All authenticated roles can fetch metadata config
router.get('/metadata', auth, allRoles, ctrl.getMetadata);

module.exports = router;
