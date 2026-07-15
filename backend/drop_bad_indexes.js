const mongoose = require('mongoose');
const dotenv = require('dotenv');

dotenv.config();

const MONGO_URI = process.env.MONGODB_URI;

mongoose.connect(MONGO_URI)
    .then(async () => {
        console.log('Connected to MongoDB');
        
        try {
            console.log('Dropping index targetYears_1_targetSections_1 from notifications...');
            await mongoose.connection.db.collection('notifications').dropIndex('targetYears_1_targetSections_1');
            console.log('Successfully dropped from notifications');
        } catch (e) {
            console.log('Failed or index does not exist in notifications:', e.message);
        }

        try {
            console.log('Dropping index targetYears_1_targetSections_1 from events...');
            await mongoose.connection.db.collection('events').dropIndex('targetYears_1_targetSections_1');
            console.log('Successfully dropped from events');
        } catch (e) {
            console.log('Failed or index does not exist in events:', e.message);
        }

        mongoose.disconnect();
    })
    .catch(err => {
        console.error(err);
    });
