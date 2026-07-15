const Section = require('../models/Section');
const Student = require('../models/Student');
const Timetable = require('../models/Timetable');
const { apiResponse } = require('../utils/constants');
const logger = require('../utils/logger');

// Create Section (supports Single or Bulk)
const createSection = async (req, res) => {
    try {
        const { name, academicYear, capacity, order, names } = req.body;

        if (!academicYear) {
            return apiResponse(res, 400, false, 'Academic Year is required');
        }

        // Bulk Create
        if (names && Array.isArray(names) && names.length > 0) {
            const created = [];
            const errors = [];

            for (let i = 0; i < names.length; i++) {
                const sName = names[i].trim().toUpperCase();
                try {
                    // Check uniqueness for this academic year
                    const exists = await Section.findOne({ name: sName, academicYear });
                    if (exists) {
                        errors.push(`Section '${sName}' already exists for this year`);
                        continue;
                    }

                    const sec = await Section.create({
                        name: sName,
                        academicYear,
                        capacity: capacity || 60,
                        order: (order || 0) + i,
                    });
                    created.push(sec);
                } catch (err) {
                    errors.push(`Error creating section '${sName}': ${err.message}`);
                }
            }

            return apiResponse(res, 201, true, `Bulk sections processed. Created ${created.length}, Errors: ${errors.length}`, {
                created, errors
            });
        }

        // Single Create
        if (!name) {
            return apiResponse(res, 400, false, 'Section name is required');
        }

        const sName = name.trim().toUpperCase();
        const exists = await Section.findOne({ name: sName, academicYear });
        if (exists) {
            return apiResponse(res, 400, false, `Section '${sName}' already exists for this year`);
        }

        const sec = await Section.create({
            name: sName,
            academicYear,
            capacity: capacity || 60,
            order: order || 0,
        });

        return apiResponse(res, 201, true, 'Section created successfully', sec);
    } catch (error) {
        logger.error('Create section error:', error);
        return apiResponse(res, 500, false, 'Server error');
    }
};

// Get Sections
const getSections = async (req, res) => {
    try {
        const { academicYear, status } = req.query;
        const query = {};
        if (academicYear) query.academicYear = academicYear;
        if (status) query.status = status;

        const sections = await Section.find(query)
            .populate('academicYear', 'name session')
            .sort({ order: 1, name: 1 })
            .lean();

        return apiResponse(res, 200, true, 'Sections retrieved', sections);
    } catch (error) {
        logger.error('Get sections error:', error);
        return apiResponse(res, 500, false, 'Server error');
    }
};

// Update Section
const updateSection = async (req, res) => {
    try {
        const { name, academicYear, capacity, status, order } = req.body;
        const sectionId = req.params.id;

        const section = await Section.findById(sectionId);
        if (!section) {
            return apiResponse(res, 404, false, 'Section not found');
        }

        const newName = name ? name.trim().toUpperCase() : section.name;
        const newYear = academicYear || section.academicYear;

        // Validate uniqueness if name or year is changing
        if (newName !== section.name || newYear.toString() !== section.academicYear.toString()) {
            const exists = await Section.findOne({
                name: newName,
                academicYear: newYear,
                _id: { $ne: sectionId }
            });
            if (exists) {
                return apiResponse(res, 400, false, `Section '${newName}' already exists for this year`);
            }
        }

        const updated = await Section.findByIdAndUpdate(
            sectionId,
            { $set: req.body },
            { new: true, runValidators: true }
        );

        return apiResponse(res, 200, true, 'Section updated', updated);
    } catch (error) {
        logger.error('Update section error:', error);
        return apiResponse(res, 500, false, 'Server error');
    }
};

// Delete Section (single)
const deleteSection = async (req, res) => {
    try {
        const sectionId = req.params.id;

        const section = await Section.findById(sectionId);
        if (!section) {
            return apiResponse(res, 404, false, 'Section not found');
        }

        // Check dependencies
        const studentsCount = await Student.countDocuments({ section: sectionId });
        if (studentsCount > 0) {
            return apiResponse(res, 400, false, 'Cannot delete: Dependent students exist');
        }

        const timetableCount = await Timetable.countDocuments({ section: sectionId });
        if (timetableCount > 0) {
            return apiResponse(res, 400, false, 'Cannot delete: Dependent timetable entries exist');
        }

        await Section.findByIdAndDelete(sectionId);
        return apiResponse(res, 200, true, 'Section deleted successfully');
    } catch (error) {
        logger.error('Delete section error:', error);
        return apiResponse(res, 500, false, 'Server error');
    }
};

// Bulk Delete Sections
const bulkDeleteSections = async (req, res) => {
    try {
        const { ids } = req.body;
        if (!ids || !Array.isArray(ids) || ids.length === 0) {
            return apiResponse(res, 400, false, 'Section IDs are required');
        }

        const deleted = [];
        const blocked = [];

        for (const id of ids) {
            const studentsCount = await Student.countDocuments({ section: id });
            const timetableCount = await Timetable.countDocuments({ section: id });

            if (studentsCount > 0 || timetableCount > 0) {
                const sec = await Section.findById(id).lean();
                blocked.push(sec ? sec.name : id);
                continue;
            }

            await Section.findByIdAndDelete(id);
            deleted.push(id);
        }

        let msg = `Deleted ${deleted.length} sections.`;
        if (blocked.length > 0) {
            msg += ` Blocked deletion of ${blocked.length} sections due to dependencies: ${blocked.join(', ')}`;
        }

        return apiResponse(res, 200, true, msg, { deleted, blocked });
    } catch (error) {
        logger.error('Bulk delete sections error:', error);
        return apiResponse(res, 500, false, 'Server error');
    }
};

module.exports = {
    createSection,
    getSections,
    updateSection,
    deleteSection,
    bulkDeleteSections,
};
