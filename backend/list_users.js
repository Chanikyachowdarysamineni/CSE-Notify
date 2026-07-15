const mongoose = require('mongoose');
const User = require('./src/models/User');
const Student = require('./src/models/Student');
const Faculty = require('./src/models/Faculty');
const dotenv = require('dotenv');

dotenv.config();

const MONGO_URI = process.env.MONGODB_URI || 'mongodb://localhost:27017/cse_hub';

mongoose.connect(MONGO_URI)
    .then(async () => {
        console.log('Connected to MongoDB');
        
        const users = await User.find().limit(10);
        console.log('--- USERS ---');
        users.forEach(u => {
            console.log(`ID: ${u._id}, LoginID: ${u.loginId}, Role: ${u.role}, Email: ${u.email}`);
        });

        const students = await Student.find().limit(5);
        console.log('--- STUDENTS ---');
        students.forEach(s => {
            console.log(`ID: ${s._id}, Name: ${s.name}, RegNo: ${s.regNo}`);
        });

        const faculties = await Faculty.find().limit(5);
        console.log('--- FACULTY ---');
        faculties.forEach(f => {
            console.log(`ID: ${f._id}, Name: ${f.name}, EmployeeId: ${f.employeeId}`);
        });

        mongoose.disconnect();
    })
    .catch(err => {
        console.error(err);
    });
