const mongoose = require('mongoose');
const User = require('./src/models/User');
const bcrypt = require('bcryptjs');
const dotenv = require('dotenv');

dotenv.config();

const MONGO_URI = process.env.MONGODB_URI;

mongoose.connect(MONGO_URI)
    .then(async () => {
        console.log('Connected to MongoDB');
        
        // Find faculty user by loginId '163'
        const user = await User.findOne({ loginId: '163' });
        if (!user) {
            console.log('User 163 not found');
            mongoose.disconnect();
            return;
        }

        // Set password to 'Faculty@123'
        // User model has pre-save hook that hashes it using bcrypt
        user.password = 'Faculty@123';
        await user.save();
        
        console.log('Password for faculty user 163 successfully reset to "Faculty@123"');
        mongoose.disconnect();
    })
    .catch(err => {
        console.error(err);
    });
