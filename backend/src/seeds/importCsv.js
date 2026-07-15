require('dotenv').config();
const mongoose = require('mongoose');
const fs = require('fs');
const path = require('path');
const User = require('../models/User');
const Student = require('../models/Student');
const Faculty = require('../models/Faculty');
const Admin = require('../models/Admin');

const importData = async () => {
    try {
        await mongoose.connect(process.env.MONGODB_URI || 'mongodb://localhost:27017/cse_hub');
        console.log('Connected to MongoDB for CSV Import');

        // Create Admin user so we at least have 1 admin
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
        console.log('Admin account created.');

        // ==========================================
        // Import Faculty
        // ==========================================
        const facultyCsvPath = 'd:\\CSE Notify\\android\\public\\faculty List .csv';
        if (fs.existsSync(facultyCsvPath)) {
            const facultyData = fs.readFileSync(facultyCsvPath, 'utf8');
            const lines = facultyData.split('\n').filter(line => line.trim() !== '');
            let count = 0;
            // Skip header (line 0)
            for (let i = 1; i < lines.length; i++) {
                // Handle commas inside quotes for designation
                const row = lines[i].match(/(".*?"|[^",\s]+)(?=\s*,|\s*$)/g);
                if (!row || row.length < 5) continue;
                
                // Usually: Sl.No, Employee id, Name, Designation, Mobile No, Emails
                // Sometimes parsing simple CSV with regex fails, let's use a simpler approach for the specific file
                // We saw: 1,163,Prof. Dr. K.V.Krishna Kishore,"Professor & Dean, SOCI ",9490647678,
                let currentStr = lines[i];
                let parsedTokens = [];
                let inQuotes = false;
                let token = "";
                for(let c=0; c<currentStr.length; c++) {
                    if(currentStr[c] === '"') {
                        inQuotes = !inQuotes;
                    } else if(currentStr[c] === ',' && !inQuotes) {
                        parsedTokens.push(token);
                        token = "";
                    } else {
                        token += currentStr[c];
                    }
                }
                parsedTokens.push(token); // push last token
                
                if (parsedTokens.length < 5) continue;
                
                const empId = parsedTokens[1].trim();
                const name = parsedTokens[2].trim();
                const designation = parsedTokens[3].trim().replace(/"/g, ''); // remove quotes
                const mobile = parsedTokens[4].trim();
                const email = parsedTokens[5] ? parsedTokens[5].trim() : `${empId.toLowerCase()}@csehub.edu`;
                
                if (!empId) continue;

                try {
                    const user = await User.create({
                        loginId: empId,
                        email: email || `${empId.toLowerCase()}@csehub.edu`,
                        password: mobile, // User requested mobile number as password
                        name: name,
                        role: 'faculty',
                    });

                    await Faculty.create({
                        userId: user._id,
                        name: name,
                        employeeId: empId,
                        designation: designation,
                        mobile: mobile,
                        collegeEmail: email,
                    });
                    count++;
                } catch (err) {
                    console.log(`Failed to insert faculty ${empId}: `, err.message);
                }
            }
            console.log(`Imported ${count} Faculty members.`);
        } else {
            console.log('Faculty CSV not found at: ' + facultyCsvPath);
        }

        // ==========================================
        // Import Students
        // ==========================================
        const studentCsvPath = 'd:\\CSE Notify\\android\\public\\4th years.csv';
        if (fs.existsSync(studentCsvPath)) {
            const studentData = fs.readFileSync(studentCsvPath, 'utf8');
            const lines = studentData.split('\n').filter(line => line.trim() !== '');
            let count = 0;
            // Skip header
            for (let i = 1; i < lines.length; i++) {
                const parts = lines[i].split(',');
                if (parts.length < 4) continue;
                
                const regNo = parts[0].trim();
                const name = parts[1].trim();
                const password = parts[2].trim(); // Registration No is password according to CSV
                const year = 'IV'; // It's 4th years
                
                if (!regNo) continue;

                try {
                    const email = `${regNo.toLowerCase()}@csehub.edu`;
                    const user = await User.create({
                        loginId: regNo,
                        email: email,
                        password: password,
                        name: name,
                        role: 'student',
                    });

                    await Student.create({
                        userId: user._id,
                        name: name,
                        regNo: regNo,
                        year: year,
                        section: 'A', // default
                        mobile: '9999999999', // default
                        collegeEmail: email,
                        dayScholarHosteller: 'Day Scholar',
                    });
                    count++;
                } catch (err) {
                    console.log(`Failed to insert student ${regNo}: `, err.message);
                }
            }
            console.log(`Imported ${count} Students.`);
        } else {
            console.log('Student CSV not found at: ' + studentCsvPath);
        }

        console.log('Import Complete!');
        process.exit(0);
    } catch (error) {
        console.error('Import error:', error);
        process.exit(1);
    }
};

importData();
