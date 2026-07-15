/**
 * Timetable Routes
 */
const router = require('express').Router();
const ctrl = require('../controllers/timetable.controller');
const { auth } = require('../middleware/auth');
const { adminOnly, allRoles } = require('../middleware/rbac');
const { validate, idValidation } = require('../middleware/validate');
const { uploadCSV } = require('../config/multer');

router.use(auth);

router.get('/daily', allRoles, ctrl.getDailyTimetable);
router.get('/weekly', allRoles, ctrl.getWeeklyTimetable);
router.post('/', adminOnly, ctrl.createTimetableEntry);
router.put('/:id', adminOnly, idValidation, validate, ctrl.updateTimetableEntry);
router.delete('/:id', adminOnly, idValidation, validate, ctrl.deleteTimetableEntry);
router.delete('/bulk', adminOnly, ctrl.bulkDelete);
router.post('/import-csv', adminOnly, uploadCSV.single('file'), ctrl.importCSV);
router.get('/export-csv', adminOnly, ctrl.exportCSV);
router.post('/duplicate', adminOnly, ctrl.duplicateTimetable);

module.exports = router;
