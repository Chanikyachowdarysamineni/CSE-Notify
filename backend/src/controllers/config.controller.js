/**
 * Config Controller
 * Serves dynamic metadata fetched solely from MongoDB
 */
const Config = require('../models/Config');
const { apiResponse } = require('../utils/constants');
const logger = require('../utils/logger');

// Default templates for self-healing automatic seeding
const DEFAULT_METADATA = {
    notificationCategories: [
        "General", "Academic", "Exam", "Placement", "Event",
        "Workshop", "Holiday", "Sports", "Cultural", "Technical",
        "Birthday", "Timetable", "Emergency"
    ],
    eventTypes: [
        "Workshop", "Seminar", "Hackathon", "Cultural", "Sports",
        "Technical", "Placement", "Guest Lecture", "Competition",
        "Exhibition", "Other"
    ],
    galleryCategories: [
        "Achievements", "Workshops", "Placements", "Campus Life", "Events", "Sports"
    ],
    fileCategories: [
        "Notes", "Assignments", "Previous Papers", "Lab Manuals",
        "Syllabus", "Circulars", "Forms", "Others"
    ]
};

const getMetadata = async (req, res, next) => {
    try {
        const metadataKeys = ['notificationCategories', 'eventTypes', 'galleryCategories', 'fileCategories'];
        const responseData = {};

        for (const key of metadataKeys) {
            let configItem = await Config.findOne({ key });
            if (!configItem) {
                // If not found in DB, seed it on-the-fly to be self-healing
                logger.info(`Config key "${key}" not found in DB. Seeding defaults...`);
                configItem = await Config.create({
                    key,
                    value: DEFAULT_METADATA[key]
                });
            }
            responseData[key] = configItem.value;
        }

        return apiResponse(res, 200, true, 'Metadata retrieved successfully from database', responseData);
    } catch (error) {
        logger.error('Error fetching metadata config from database:', error);
        next(error);
    }
};

module.exports = {
    getMetadata,
};
