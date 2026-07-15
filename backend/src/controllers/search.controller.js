/**
 * Search Controller
 * Search students by registration number (Admin/Faculty only)
 */
const Student = require('../models/Student');
const User = require('../models/User');
const { apiResponse } = require('../utils/constants');
const logger = require('../utils/logger');

/**
 * GET /api/search/student?regNo=
 */
const searchStudent = async (req, res, next) => {
    try {
        const { regNo, name, academicYear, section } = req.query;

        if (!regNo && !name) {
            return apiResponse(res, 400, false, 'Registration number or name is required for search');
        }

        let query = {};
        if (regNo) {
            query.regNo = { $regex: regNo, $options: 'i' };
        }
        if (name) {
            query.name = { $regex: name, $options: 'i' };
        }
        if (academicYear) query.academicYear = academicYear;
        if (section) query.section = section;

        const students = await Student.find(query)
            .populate({
                path: 'userId',
                select: 'email lastLogin isActive',
            })
            .populate('academicYear', 'name session')
            .populate('section', 'name')
            .lean();

        if (students.length === 0) {
            return apiResponse(res, 404, false, 'No students found');
        }

        return apiResponse(res, 200, true, `${students.length} student(s) found`, students);
    } catch (error) {
        next(error);
    }
};

module.exports = { searchStudent };
