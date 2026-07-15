const AcademicYear = require('../models/AcademicYear');
const Section = require('../models/Section');
const Student = require('../models/Student');
const Timetable = require('../models/Timetable');
const { apiResponse } = require('../utils/constants');
const logger = require('../utils/logger');

// Create Academic Year
const createAcademicYear = async (req, res, next) => {
    try {
        const { name, session, status, order } = req.body;

        if (!name || !session) {
            return apiResponse(res, 400, false, 'Name and session are required');
        }

        // Check uniqueness of name + session
        const exists = await AcademicYear.findOne({ name, session });
        if (exists) {
            return apiResponse(res, 400, false, 'Academic year with this name and session already exists');
        }

        const year = await AcademicYear.create({
            name,
            session,
            status: status || 'active',
            order: order || 0,
            createdBy: req.user.id,
        });

        return apiResponse(res, 201, true, 'Academic year created successfully', year);
    } catch (error) {
        next(error);
    }
};

// Get Academic Years (Active or All depending on role or query)
const getAcademicYears = async (req, res, next) => {
    try {
        const { status } = req.query;
        const query = {};
        if (status) query.status = status;

        const years = await AcademicYear.find(query).sort({ order: 1, name: 1 }).lean();
        return apiResponse(res, 200, true, 'Academic years retrieved', years);
    } catch (error) {
        next(error);
    }
};

// Update Academic Year
const updateAcademicYear = async (req, res, next) => {
    try {
        const { name, session, status, order } = req.body;
        const yearId = req.params.id;

        const year = await AcademicYear.findById(yearId);
        if (!year) {
            return apiResponse(res, 404, false, 'Academic year not found');
        }

        // Validate uniqueness if name/session are being changed
        if ((name && name !== year.name) || (session && session !== year.session)) {
            const exists = await AcademicYear.findOne({
                name: name || year.name,
                session: session || year.session,
                _id: { $ne: yearId }
            });
            if (exists) {
                return apiResponse(res, 400, false, 'Academic year with this name and session already exists');
            }
        }

        const updated = await AcademicYear.findByIdAndUpdate(
            yearId,
            { $set: req.body },
            { new: true, runValidators: true }
        );

        return apiResponse(res, 200, true, 'Academic year updated', updated);
    } catch (error) {
        next(error);
    }
};

// Delete Academic Year (if no dependencies)
const deleteAcademicYear = async (req, res, next) => {
    try {
        const yearId = req.params.id;

        const year = await AcademicYear.findById(yearId);
        if (!year) {
            return apiResponse(res, 404, false, 'Academic year not found');
        }

        // Check dependencies
        const sectionsCount = await Section.countDocuments({ academicYear: yearId });
        if (sectionsCount > 0) {
            return apiResponse(res, 400, false, 'Cannot delete: Dependent sections exist');
        }

        const studentsCount = await Student.countDocuments({ academicYear: yearId });
        if (studentsCount > 0) {
            return apiResponse(res, 400, false, 'Cannot delete: Dependent students exist');
        }

        const timetableCount = await Timetable.countDocuments({ academicYear: yearId });
        if (timetableCount > 0) {
            return apiResponse(res, 400, false, 'Cannot delete: Dependent timetable entries exist');
        }

        await AcademicYear.findByIdAndDelete(yearId);
        return apiResponse(res, 200, true, 'Academic year deleted successfully');
    } catch (error) {
        next(error);
    }
};

module.exports = {
    createAcademicYear,
    getAcademicYears,
    updateAcademicYear,
    deleteAcademicYear,
};
