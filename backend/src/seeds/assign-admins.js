const mongoose = require('mongoose');
require('dotenv').config();
const User = require('../models/User');
const Faculty = require('../models/Faculty');
const Admin = require('../models/Admin');
const { ROLES } = require('../utils/constants');

const TARGET_IDS = ['163', '675', '189', '1702', '714', '1958', '2181', '1918', '1976'];

async function assignAdmins() {
    try {
        await mongoose.connect(process.env.MONGODB_URI || 'mongodb://localhost:27017/cse_hub');
        console.log('Connected to MongoDB. Running RBAC Migration...');

        // 1. Find all users matching these loginIds
        const users = await User.find({ loginId: { $in: TARGET_IDS } });
        console.log(`Found ${users.length} matching users out of ${TARGET_IDS.length} targets.`);

        for (const user of users) {
            // Upgrade role to admin
            user.role = ROLES.ADMIN;
            await user.save();
            console.log(`Upgraded ${user.loginId} to ADMIN in User collection.`);

            // Ensure Admin profile exists
            let adminProfile = await Admin.findOne({ userId: user._id });
            if (!adminProfile) {
                // If they have a Faculty profile, pull the name from there
                const facultyProfile = await Faculty.findOne({ userId: user._id });
                
                adminProfile = new Admin({
                    userId: user._id,
                    name: facultyProfile ? facultyProfile.name : `Admin ${user.loginId}`,
                    employeeId: user.loginId,
                    roleTitle: facultyProfile ? facultyProfile.designation : 'Administrator',
                    contactNumber: facultyProfile ? facultyProfile.phone : '',
                    department: 'CSE',
                });
                await adminProfile.save();
                console.log(`Created Admin profile for ${user.loginId}`);
            } else {
                console.log(`Admin profile already exists for ${user.loginId}`);
            }
        }

        console.log('RBAC Migration Complete!');
        process.exit(0);
    } catch (error) {
        console.error('Migration failed:', error);
        process.exit(1);
    }
}

assignAdmins();
