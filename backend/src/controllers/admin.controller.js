/**
 * Admin Controller
 * Complete CRUD for all entities + CSV upload + statistics
 */
const User = require('../models/User');
const Student = require('../models/Student');
const Faculty = require('../models/Faculty');
const Admin = require('../models/Admin');
const Notification = require('../models/Notification');
const Event = require('../models/Event');
const Timetable = require('../models/Timetable');
const File = require('../models/File');
const Gallery = require('../models/Gallery');
const AuditLog = require('../models/AuditLog');
const { apiResponse, paginationMeta } = require('../utils/constants');
const { createAuditLog } = require('../services/audit.service');
const logger = require('../utils/logger');

// ============================================
// Student Management
// ============================================

const getStudents = async (req, res, next) => {
    try {
        const { page = 1, limit = 20, academicYear, section, search } = req.query;
        const skip = (page - 1) * limit;

        let query = {};
        if (academicYear) query.academicYear = academicYear;
        if (section) query.section = section;
        if (search) {
            query.$or = [
                { name: { $regex: search, $options: 'i' } },
                { regNo: { $regex: search, $options: 'i' } },
            ];
        }

        const [students, total] = await Promise.all([
            Student.find(query)
                .populate('userId', 'email isActive lastLogin')
                .populate('academicYear', 'name session')
                .populate('section', 'name')
                .sort({ regNo: 1 })
                .skip(skip)
                .limit(parseInt(limit))
                .lean(),
            Student.countDocuments(query),
        ]);

        return apiResponse(res, 200, true, 'Students retrieved', students,
            paginationMeta(page, limit, total));
    } catch (error) {
        next(error);
    }
};

const createStudent = async (req, res, next) => {
    try {
        const { email, password, name, regNo, academicYear, section, mobile, personalEmail, collegeEmail, dob, dayScholarHosteller, cgpa } = req.body;

        // Create user account
        const user = await User.create({
            loginId: regNo,
            email,
            password: password || regNo, // Default password is registration number
            name,
            role: 'student',
        });

        // Create student profile
        const student = await Student.create({
            userId: user._id,
            name,
            regNo,
            academicYear,
            section,
            mobile,
            personalEmail,
            collegeEmail,
            dob,
            dayScholarHosteller,
            cgpa,
        });

        await createAuditLog(req, 'CREATE', 'student', student._id, { regNo, name });

        return apiResponse(res, 201, true, 'Student created successfully', { user, student });
    } catch (error) {
        logger.error('Create student error:', error);
        if (error.code === 11000) {
            return apiResponse(res, 400, false, 'Email or registration number already exists');
        }
        return apiResponse(res, 500, false, 'Server error');
    }
};

const updateStudent = async (req, res, next) => {
    try {
        const student = await Student.findById(req.params.id);
        if (!student) {
            return apiResponse(res, 404, false, 'Student not found');
        }

        const updated = await Student.findByIdAndUpdate(
            req.params.id,
            { $set: req.body },
            { new: true, runValidators: true }
        );

        // Update user name if changed
        if (req.body.name) {
            await User.findByIdAndUpdate(student.userId, { name: req.body.name });
        }

        await createAuditLog(req, 'UPDATE', 'student', student._id);

        return apiResponse(res, 200, true, 'Student updated', updated);
    } catch (error) {
        next(error);
    }
};

const deleteStudent = async (req, res, next) => {
    try {
        const student = await Student.findById(req.params.id);
        if (!student) {
            return apiResponse(res, 404, false, 'Student not found');
        }

        // Deactivate user account (soft delete)
        await User.findByIdAndUpdate(student.userId, { isActive: false });
        await Student.findByIdAndDelete(req.params.id);

        await createAuditLog(req, 'DELETE', 'student', student._id, { regNo: student.regNo });

        return apiResponse(res, 200, true, 'Student deleted');
    } catch (error) {
        next(error);
    }
};

// ============================================
// Faculty Management
// ============================================

const getFaculty = async (req, res, next) => {
    try {
        const { page = 1, limit = 20, search } = req.query;
        const skip = (page - 1) * limit;

        let query = {};
        if (search) {
            query.$or = [
                { name: { $regex: search, $options: 'i' } },
                { employeeId: { $regex: search, $options: 'i' } },
            ];
        }

        const [faculty, total] = await Promise.all([
            Faculty.find(query)
                .populate('userId', 'email isActive lastLogin')
                .sort({ name: 1 })
                .skip(skip)
                .limit(parseInt(limit))
                .lean(),
            Faculty.countDocuments(query),
        ]);

        return apiResponse(res, 200, true, 'Faculty retrieved', faculty,
            paginationMeta(page, limit, total));
    } catch (error) {
        next(error);
    }
};

const createFaculty = async (req, res, next) => {
    try {
        const { email, password, name, employeeId, designation, mobile, personalEmail, collegeEmail } = req.body;

        const user = await User.create({
            loginId: employeeId,
            email,
            password: password || employeeId,
            name,
            role: 'faculty',
        });

        const faculty = await Faculty.create({
            userId: user._id,
            name,
            employeeId,
            designation,
            mobile,
            personalEmail,
            collegeEmail,
        });

        await createAuditLog(req, 'CREATE', 'faculty', faculty._id, { employeeId, name });

        return apiResponse(res, 201, true, 'Faculty created successfully', { user, faculty });
    } catch (error) {
        logger.error('Create faculty error:', error);
        if (error.code === 11000) {
            return apiResponse(res, 400, false, 'Email or employee ID already exists');
        }
        return apiResponse(res, 500, false, 'Server error');
    }
};

const updateFaculty = async (req, res, next) => {
    try {
        const faculty = await Faculty.findById(req.params.id);
        if (!faculty) {
            return apiResponse(res, 404, false, 'Faculty not found');
        }

        const updated = await Faculty.findByIdAndUpdate(
            req.params.id,
            { $set: req.body },
            { new: true, runValidators: true }
        );

        if (req.body.name) {
            await User.findByIdAndUpdate(faculty.userId, { name: req.body.name });
        }

        await createAuditLog(req, 'UPDATE', 'faculty', faculty._id);

        return apiResponse(res, 200, true, 'Faculty updated', updated);
    } catch (error) {
        next(error);
    }
};

const deleteFaculty = async (req, res, next) => {
    try {
        const faculty = await Faculty.findById(req.params.id);
        if (!faculty) {
            return apiResponse(res, 404, false, 'Faculty not found');
        }

        await User.findByIdAndUpdate(faculty.userId, { isActive: false });
        await Faculty.findByIdAndDelete(req.params.id);

        await createAuditLog(req, 'DELETE', 'faculty', faculty._id, { employeeId: faculty.employeeId });

        return apiResponse(res, 200, true, 'Faculty deleted');
    } catch (error) {
        next(error);
    }
};

// ============================================
// Statistics & Audit
// ============================================

const getStatistics = async (req, res, next) => {
    try {
        const [students, faculty, notifications, events, files, gallery, users] = await Promise.all([
            Student.countDocuments(),
            Faculty.countDocuments(),
            Notification.countDocuments(),
            Event.countDocuments(),
            File.countDocuments(),
            Gallery.countDocuments(),
            User.countDocuments(),
        ]);

        // Year-wise student count
        const yearWise = await Student.aggregate([
            { $group: { _id: '$academicYear', count: { $sum: 1 } } },
            { $lookup: { from: 'academicyears', localField: '_id', foreignField: '_id', as: 'yearInfo' } },
            { $unwind: { path: '$yearInfo', preserveNullAndEmptyArrays: true } },
            { $project: { _id: 1, count: 1, name: '$yearInfo.name', session: '$yearInfo.session' } },
            { $sort: { name: 1 } },
        ]);

        // Section-wise student count
        const sectionWise = await Student.aggregate([
            { $group: { _id: { academicYear: '$academicYear', section: '$section' }, count: { $sum: 1 } } },
            { $lookup: { from: 'academicyears', localField: '_id.academicYear', foreignField: '_id', as: 'yearInfo' } },
            { $lookup: { from: 'sections', localField: '_id.section', foreignField: '_id', as: 'sectionInfo' } },
            { $unwind: { path: '$yearInfo', preserveNullAndEmptyArrays: true } },
            { $unwind: { path: '$sectionInfo', preserveNullAndEmptyArrays: true } },
            { $project: { _id: 1, count: 1, yearName: '$yearInfo.name', sectionName: '$sectionInfo.name' } },
            { $sort: { yearName: 1, sectionName: 1 } },
        ]);

        return apiResponse(res, 200, true, 'Statistics retrieved', {
            totals: { students, faculty, notifications, events, files, gallery, users },
            yearWise,
            sectionWise,
        });
    } catch (error) {
        next(error);
    }
};

const getAuditLogs = async (req, res, next) => {
    try {
        const { page = 1, limit = 50, action, resource } = req.query;
        const skip = (page - 1) * limit;

        let query = {};
        if (action) query.action = action;
        if (resource) query.resource = resource;

        const [logs, total] = await Promise.all([
            AuditLog.find(query)
                .populate('userId', 'name email role')
                .sort({ createdAt: -1 })
                .skip(skip)
                .limit(parseInt(limit))
                .lean(),
            AuditLog.countDocuments(query),
        ]);

        return apiResponse(res, 200, true, 'Audit logs retrieved', logs,
            paginationMeta(page, limit, total));
    } catch (error) {
        next(error);
    }
};

module.exports = {
    getStudents, createStudent, updateStudent, deleteStudent,
    getFaculty, createFaculty, updateFaculty, deleteFaculty,
    getStatistics, getAuditLogs,
};
