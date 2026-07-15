/**
 * Timetable Controller
 * CRUD + CSV Import/Export + Daily/Weekly views + Duplication
 */
const Timetable = require('../models/Timetable');
const Student = require('../models/Student');
const Faculty = require('../models/Faculty');
const AcademicYear = require('../models/AcademicYear');
const Section = require('../models/Section');
const { apiResponse, ROLES, DAYS } = require('../utils/constants');
const { createAuditLog } = require('../services/audit.service');
const logger = require('../utils/logger');
const fs = require('fs');
const path = require('path');

/**
 * GET /api/timetable/daily
 * Get daily timetable
 */
const getDailyTimetable = async (req, res) => {
    try {
        let { academicYear, section, day } = req.query;

        // Default to today
        if (!day) {
            const today = new Date().getDay();
            day = DAYS[today - 1] || 'Monday'; // 0=Sunday
        }

        // Student: use their assigned academicYear/section ObjectIds
        if (req.user.role === ROLES.STUDENT) {
            const student = await Student.findOne({ userId: req.user.id });
            if (!student) return apiResponse(res, 404, false, 'Student profile not found');
            academicYear = student.academicYear;
            section = student.section;
        }

        // Faculty: show their assigned periods
        if (req.user.role === ROLES.FACULTY) {
            const faculty = await Faculty.findOne({ userId: req.user.id });
            if (!faculty) return apiResponse(res, 404, false, 'Faculty profile not found');

            const timetable = await Timetable.find({ faculty: faculty._id, day })
                .populate('faculty', 'name')
                .populate('academicYear', 'name session')
                .populate('section', 'name')
                .sort({ period: 1 })
                .lean();

            return apiResponse(res, 200, true, 'Faculty daily timetable', timetable);
        }

        if (!academicYear || !section) {
            return apiResponse(res, 400, false, 'Academic Year and Section are required');
        }

        const timetable = await Timetable.find({ academicYear, section, day })
            .populate('faculty', 'name')
            .populate('academicYear', 'name session')
            .populate('section', 'name')
            .sort({ period: 1 })
            .lean();

        return apiResponse(res, 200, true, 'Daily timetable retrieved', timetable);
    } catch (error) {
        logger.error('Get daily timetable error:', error);
        return apiResponse(res, 500, false, 'Server error');
    }
};

/**
 * GET /api/timetable/weekly
 * Get weekly timetable
 */
const getWeeklyTimetable = async (req, res) => {
    try {
        let { academicYear, section } = req.query;

        if (req.user.role === ROLES.STUDENT) {
            const student = await Student.findOne({ userId: req.user.id });
            if (!student) return apiResponse(res, 404, false, 'Student profile not found');
            academicYear = student.academicYear;
            section = student.section;
        }

        if (req.user.role === ROLES.FACULTY) {
            const faculty = await Faculty.findOne({ userId: req.user.id });
            if (!faculty) return apiResponse(res, 404, false, 'Faculty profile not found');

            const timetable = await Timetable.find({ faculty: faculty._id })
                .populate('faculty', 'name')
                .populate('academicYear', 'name session')
                .populate('section', 'name')
                .sort({ day: 1, period: 1 })
                .lean();

            // Group by day
            const grouped = {};
            DAYS.forEach(d => grouped[d] = []);
            timetable.forEach(t => {
                if (grouped[t.day]) grouped[t.day].push(t);
            });

            return apiResponse(res, 200, true, 'Faculty weekly timetable', grouped);
        }

        if (!academicYear || !section) {
            return apiResponse(res, 400, false, 'Academic Year and Section are required');
        }

        const timetable = await Timetable.find({ academicYear, section })
            .populate('faculty', 'name')
            .populate('academicYear', 'name session')
            .populate('section', 'name')
            .sort({ day: 1, period: 1 })
            .lean();

        // Group by day
        const grouped = {};
        DAYS.forEach(d => grouped[d] = []);
        timetable.forEach(t => {
            if (grouped[t.day]) grouped[t.day].push(t);
        });

        return apiResponse(res, 200, true, 'Weekly timetable retrieved', grouped);
    } catch (error) {
        logger.error('Get weekly timetable error:', error);
        return apiResponse(res, 500, false, 'Server error');
    }
};

/**
 * POST /api/timetable
 * Create timetable entry (Admin only)
 */
const createTimetableEntry = async (req, res) => {
    try {
        const { academicYear, section, day, period } = req.body;

        // Verify if year & section exist
        const yearDoc = await AcademicYear.findById(academicYear);
        const sectionDoc = await Section.findById(section);
        if (!yearDoc || !sectionDoc) {
            return apiResponse(res, 400, false, 'Invalid Academic Year or Section reference');
        }

        const entry = await Timetable.create(req.body);

        await createAuditLog(req, 'CREATE', 'timetable', entry._id);

        return apiResponse(res, 201, true, 'Timetable entry created', entry);
    } catch (error) {
        if (error.code === 11000) {
            return apiResponse(res, 400, false, 'A timetable entry already exists for this year/section/day/period');
        }
        logger.error('Create timetable error:', error);
        return apiResponse(res, 500, false, 'Server error');
    }
};

/**
 * PUT /api/timetable/:id
 * Update timetable entry (Admin only)
 */
const updateTimetableEntry = async (req, res) => {
    try {
        const entry = await Timetable.findByIdAndUpdate(
            req.params.id,
            { $set: req.body },
            { new: true, runValidators: true }
        );

        if (!entry) {
            return apiResponse(res, 404, false, 'Timetable entry not found');
        }

        await createAuditLog(req, 'UPDATE', 'timetable', entry._id);

        return apiResponse(res, 200, true, 'Timetable entry updated', entry);
    } catch (error) {
        logger.error('Update timetable error:', error);
        return apiResponse(res, 500, false, 'Server error');
    }
};

/**
 * DELETE /api/timetable/:id
 * Delete timetable entry (Admin only)
 */
const deleteTimetableEntry = async (req, res) => {
    try {
        const entry = await Timetable.findByIdAndDelete(req.params.id);
        if (!entry) {
            return apiResponse(res, 404, false, 'Timetable entry not found');
        }

        await createAuditLog(req, 'DELETE', 'timetable', entry._id);

        return apiResponse(res, 200, true, 'Timetable entry deleted');
    } catch (error) {
        logger.error('Delete timetable error:', error);
        return apiResponse(res, 500, false, 'Server error');
    }
};

/**
 * POST /api/timetable/import-csv
 * Import timetable from CSV (Admin only)
 * CSV format: yearName,session,sectionName,day,period,subject,subjectCode,facultyName,room,startTime,endTime,type
 */
const importCSV = async (req, res) => {
    try {
        if (!req.file) {
            return apiResponse(res, 400, false, 'CSV file is required');
        }

        const csvParser = require('csv-parser');
        const results = [];

        await new Promise((resolve, reject) => {
            fs.createReadStream(req.file.path)
                .pipe(csvParser())
                .on('data', (row) => {
                    results.push(row);
                })
                .on('end', resolve)
                .on('error', reject);
        });

        // Load all Academic Years and Sections for cache lookup
        const years = await AcademicYear.find({ status: 'active' }).lean();
        const sections = await Section.find({ status: 'active' }).lean();

        const yearsMap = {}; // key: "name_session"
        years.forEach(y => yearsMap[`${y.name.toLowerCase()}_${y.session}`] = y._id);

        const sectionsMap = {}; // key: "yearId_name"
        sections.forEach(s => sectionsMap[`${s.academicYear.toString()}_${s.name.toLowerCase()}`] = s._id);

        const toInsert = [];
        const errors = [];
        let line = 1;

        for (const row of results) {
            line++;
            const yName = row.yearName?.trim().toLowerCase();
            const ySession = row.session?.trim();
            const sName = row.sectionName?.trim().toLowerCase();
            const day = row.day?.trim();
            const period = parseInt(row.period);

            if (!yName || !ySession || !sName || !day || isNaN(period)) {
                errors.push(`Row ${line}: Missing required structure fields (yearName, session, sectionName, day, period)`);
                continue;
            }

            const yearId = yearsMap[`${yName}_${ySession}`];
            if (!yearId) {
                errors.push(`Row ${line}: Active Academic Year '${row.yearName}' with session '${row.session}' not found`);
                continue;
            }

            const sectionId = sectionsMap[`${yearId.toString()}_${sName}`];
            if (!sectionId) {
                errors.push(`Row ${line}: Active Section '${row.sectionName}' not found under Year '${row.yearName}'`);
                continue;
            }

            if (!DAYS.includes(day)) {
                errors.push(`Row ${line}: Invalid day '${day}' (Must be one of: ${DAYS.join(', ')})`);
                continue;
            }

            toInsert.push({
                academicYear: yearId,
                section: sectionId,
                day,
                period,
                subject: row.subject?.trim() || 'Free Class',
                subjectCode: row.subjectCode?.trim() || '',
                facultyName: row.facultyName?.trim() || '',
                room: row.room?.trim() || '',
                startTime: row.startTime?.trim() || '09:00 AM',
                endTime: row.endTime?.trim() || '09:50 AM',
                type: row.type?.trim() || 'Theory',
            });
        }

        // If there are critical structural validation errors, reject import
        if (errors.length > 0) {
            // Clean up uploaded file
            fs.unlinkSync(req.file.path);
            return apiResponse(res, 400, false, `Import validation failed with ${errors.length} errors`, { errors });
        }

        // Perform upserts
        let inserted = 0;
        for (const entry of toInsert) {
            await Timetable.findOneAndUpdate(
                { academicYear: entry.academicYear, section: entry.section, day: entry.day, period: entry.period },
                { $set: entry },
                { upsert: true, new: true, runValidators: true }
            );
            inserted++;
        }

        // Clean up uploaded file
        fs.unlinkSync(req.file.path);

        await createAuditLog(req, 'CSV_IMPORT', 'timetable', null, { inserted });

        return apiResponse(res, 200, true, `CSV imported successfully. Processed ${inserted} entries.`, {
            inserted, errors: []
        });
    } catch (error) {
        if (req.file && fs.existsSync(req.file.path)) {
            fs.unlinkSync(req.file.path);
        }
        logger.error('Import CSV error:', error);
        return apiResponse(res, 500, false, 'Server error during CSV import');
    }
};

/**
 * GET /api/timetable/export-csv
 * Export timetable to CSV (Admin only)
 */
const exportCSV = async (req, res) => {
    try {
        const { academicYear, section } = req.query;
        let query = {};
        if (academicYear) query.academicYear = academicYear;
        if (section) query.section = section;

        const entries = await Timetable.find(query)
            .populate('faculty', 'name')
            .populate('academicYear', 'name session')
            .populate('section', 'name')
            .sort({ day: 1, period: 1 })
            .lean();

        const formatted = entries.map(e => ({
            yearName: e.academicYear?.name || '',
            session: e.academicYear?.session || '',
            sectionName: e.section?.name || '',
            day: e.day,
            period: e.period,
            subject: e.subject,
            subjectCode: e.subjectCode,
            facultyName: e.facultyName,
            room: e.room,
            startTime: e.startTime,
            endTime: e.endTime,
            type: e.type,
        }));

        const { Parser } = require('json2csv');
        const fields = ['yearName', 'session', 'sectionName', 'day', 'period', 'subject', 'subjectCode', 'facultyName', 'room', 'startTime', 'endTime', 'type'];
        const parser = new Parser({ fields });
        const csv = parser.parse(formatted);

        res.header('Content-Type', 'text/csv');
        res.attachment(`timetable_${academicYear || 'all'}_${section || 'all'}.csv`);
        return res.send(csv);
    } catch (error) {
        logger.error('Export CSV error:', error);
        return apiResponse(res, 500, false, 'Server error during CSV export');
    }
};

/**
 * POST /api/timetable/duplicate
 * Duplicate timetable from one Year/Section to another (Admin only)
 */
const duplicateTimetable = async (req, res) => {
    try {
        const { sourceYear, sourceSection, targetYear, targetSection } = req.body;

        if (!sourceYear || !sourceSection || !targetYear || !targetSection) {
            return apiResponse(res, 400, false, 'Source and Target year & section are required');
        }

        // Fetch source entries
        const sourceEntries = await Timetable.find({ academicYear: sourceYear, section: sourceSection }).lean();
        if (sourceEntries.length === 0) {
            return apiResponse(res, 400, false, 'No timetable entries found for source year & section');
        }

        // Delete existing entries in target
        await Timetable.deleteMany({ academicYear: targetYear, section: targetSection });

        // Copy source entries to target
        const targetEntries = sourceEntries.map(entry => {
            const copy = { ...entry };
            delete copy._id;
            delete copy.createdAt;
            delete copy.updatedAt;
            copy.academicYear = targetYear;
            copy.section = targetSection;
            return copy;
        });

        await Timetable.insertMany(targetEntries);

        await createAuditLog(req, 'DUPLICATE', 'timetable', null, {
            fromYear: sourceYear,
            fromSection: sourceSection,
            toYear: targetYear,
            toSection: targetSection,
            copied: targetEntries.length
        });

        return apiResponse(res, 200, true, `Timetable duplicated successfully. Copied ${targetEntries.length} entries.`);
    } catch (error) {
        logger.error('Duplicate timetable error:', error);
        return apiResponse(res, 500, false, 'Server error');
    }
};

/**
 * DELETE /api/timetable/bulk
 * Delete all timetable entries for a year/section (Admin only)
 */
const bulkDelete = async (req, res) => {
    try {
        const { academicYear, section } = req.query;
        if (!academicYear || !section) {
            return apiResponse(res, 400, false, 'Academic Year and Section are required');
        }

        const result = await Timetable.deleteMany({ academicYear, section });

        await createAuditLog(req, 'DELETE', 'timetable', null, { academicYear, section, count: result.deletedCount });

        return apiResponse(res, 200, true, `${result.deletedCount} timetable entries deleted`);
    } catch (error) {
        logger.error('Bulk delete timetable error:', error);
        return apiResponse(res, 500, false, 'Server error');
    }
};

module.exports = {
    getDailyTimetable,
    getWeeklyTimetable,
    createTimetableEntry,
    updateTimetableEntry,
    deleteTimetableEntry,
    importCSV,
    exportCSV,
    duplicateTimetable,
    bulkDelete,
};
