/**
 * Admin Routes - Admin only CRUD for all entities
 */
const router = require('express').Router();
const ctrl = require('../controllers/admin.controller');
const { auth } = require('../middleware/auth');
const { adminOnly } = require('../middleware/rbac');
const { validate, idValidation, paginationValidation } = require('../middleware/validate');

router.use(auth, adminOnly);

// Student management
router.get('/students', paginationValidation, validate, ctrl.getStudents);
router.post('/students', ctrl.createStudent);
router.put('/students/:id', idValidation, validate, ctrl.updateStudent);
router.delete('/students/:id', idValidation, validate, ctrl.deleteStudent);

// Faculty management
router.get('/faculty', paginationValidation, validate, ctrl.getFaculty);
router.post('/faculty', ctrl.createFaculty);
router.put('/faculty/:id', idValidation, validate, ctrl.updateFaculty);
router.delete('/faculty/:id', idValidation, validate, ctrl.deleteFaculty);

// Statistics & Audit
router.get('/statistics', ctrl.getStatistics);
router.get('/audit-logs', paginationValidation, validate, ctrl.getAuditLogs);

module.exports = router;
