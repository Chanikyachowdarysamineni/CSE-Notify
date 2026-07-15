require('dotenv').config();
const mongoose = require('mongoose');

const resetDb = async () => {
    try {
        await mongoose.connect(process.env.MONGODB_URI || 'mongodb://localhost:27017/cse_hub');
        console.log('Connected to MongoDB');

        // Drop the entire database to remove ALL existing collections (including garbage ones)
        await mongoose.connection.db.dropDatabase();
        console.log('Database dropped successfully! All collections deleted.');

        process.exit(0);
    } catch (error) {
        console.error('Reset error:', error);
        process.exit(1);
    }
};

resetDb();
