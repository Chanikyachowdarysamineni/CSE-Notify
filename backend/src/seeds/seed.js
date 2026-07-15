/**
 * Database Seed Script
 * Creates default admin, sample faculty, and sample students
 * 
 * Usage: node src/seeds/seed.js
 */
require('dotenv').config();
const mongoose = require('mongoose');
const User = require('../models/User');
const Student = require('../models/Student');
const Faculty = require('../models/Faculty');
const Admin = require('../models/Admin');

const seedData = async () => {
    try {
        await mongoose.connect(process.env.MONGODB_URI || 'mongodb://localhost:27017/cse_hub');
        console.log('Connected to MongoDB');

        // Clear existing data (careful in production!)
        if (process.env.NODE_ENV !== 'production') {
            await User.deleteMany({});
            await Student.deleteMany({});
            await Faculty.deleteMany({});
            await Admin.deleteMany({});
            
            try {
                await mongoose.connection.db.collection('students').dropIndexes();
                await mongoose.connection.db.collection('faculties').dropIndexes();
                await mongoose.connection.db.collection('admins').dropIndexes();
                await mongoose.connection.db.collection('users').dropIndexes();
                console.log('Stale database indexes dropped successfully');
            } catch (e) {
                // If collections are empty/new, index dropping might throw an error which we catch silently
            }

            console.log('Cleared existing data');
        }

        // Create Admin
        const adminUser = await User.create({
            loginId: 'ADM001',
            email: 'admin@csehub.edu',
            password: 'Admin@123',
            name: 'CSE Admin',
            role: 'admin',
        });

        await Admin.create({
            userId: adminUser._id,
            name: 'CSE Admin',
            employeeId: 'ADM001',
            designation: 'HOD - CSE Department',
        });
        console.log('Admin created: admin@csehub.edu / Admin@123');

        // Create Faculty
        const facultyData = [
            { name: 'Dr. Rajesh Kumar', employeeId: 'FAC001', designation: 'Professor', email: 'rajesh@csehub.edu' },
            { name: 'Dr. Priya Sharma', employeeId: 'FAC002', designation: 'Associate Professor', email: 'priya@csehub.edu' },
            { name: 'Mr. Suresh Babu', employeeId: 'FAC003', designation: 'Assistant Professor', email: 'suresh@csehub.edu' },
        ];

        for (const f of facultyData) {
            const user = await User.create({
                loginId: f.employeeId,
                email: f.email,
                password: 'Faculty@123',
                name: f.name,
                role: 'faculty',
            });

            await Faculty.create({
                userId: user._id,
                name: f.name,
                employeeId: f.employeeId,
                designation: f.designation,
                mobile: '9876543210',
                collegeEmail: f.email,
            });
        }
        console.log('Faculty created (3 members) - password: Faculty@123');

        // Create Students
        const years = ['I', 'II', 'III', 'IV'];
        const sections = ['A', 'B'];
        let studentCount = 0;

        for (const year of years) {
            for (const section of sections) {
                for (let i = 1; i <= 3; i++) {
                    const regNo = `22CSE${year === 'I' ? '1' : year === 'II' ? '2' : year === 'III' ? '3' : '4'}${section}${String(i).padStart(3, '0')}`;
                    const email = `${regNo.toLowerCase()}@csehub.edu`;

                    const user = await User.create({
                        loginId: regNo,
                        email,
                        password: 'Student@123',
                        name: `Student ${regNo}`,
                        role: 'student',
                    });

                    await Student.create({
                        userId: user._id,
                        name: `Student ${regNo}`,
                        regNo,
                        year,
                        section,
                        mobile: `98765${String(studentCount).padStart(5, '0')}`,
                        collegeEmail: email,
                        dayScholarHosteller: i % 2 === 0 ? 'Hosteller' : 'Day Scholar',
                        cgpa: (6 + Math.random() * 4).toFixed(2),
                        dob: new Date(2002, Math.floor(Math.random() * 12), Math.floor(Math.random() * 28) + 1),
                    });

                    studentCount++;
                }
            }
        }
        console.log(`Students created (${studentCount} students) - password: Student@123`);

        console.log('\n=== Seed Complete ===');
        console.log('Admin:   admin@csehub.edu / Admin@123');
        console.log('Faculty: rajesh@csehub.edu / Faculty@123');
        console.log('Student: 22cse1a001@csehub.edu / Student@123');
        
        process.exit(0);
    } catch (error) {
        console.error('Seed error:', error);
        process.exit(1);
    }
};

seedData();
