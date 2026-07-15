require('dotenv').config();
const mongoose = require('mongoose');
const User = require('../models/User');
const Student = require('../models/Student');
const Timetable = require('../models/Timetable');
const Notification = require('../models/Notification');
const Event = require('../models/Event');
const File = require('../models/File');
const AcademicYear = require('../models/AcademicYear');
const Section = require('../models/Section');

const migrate = async () => {
    try {
        await mongoose.connect(process.env.MONGODB_URI || 'mongodb://localhost:27017/cse_hub');
        console.log('Connected to MongoDB');

        // 1. Find Admin user to assign as creator
        const admin = await User.findOne({ role: 'admin' });
        if (!admin) {
            console.error('No admin user found. Please run the seed script first.');
            process.exit(1);
        }

        console.log(`Using Admin user ${admin.name} (${admin._id}) for migration references`);

        // 2. Initialize default Academic Years
        const yearNames = ['I', 'II', 'III', 'IV'];
        const session = '2026-27';
        const yearsMap = {};

        for (let i = 0; i < yearNames.length; i++) {
            const yName = yearNames[i];
            let yearDoc = await AcademicYear.findOne({ name: yName, session });
            if (!yearDoc) {
                yearDoc = await AcademicYear.create({
                    name: yName,
                    session,
                    status: 'active',
                    order: i + 1,
                    createdBy: admin._id,
                });
                console.log(`Created Academic Year: ${yName} (${session})`);
            }
            yearsMap[yName] = yearDoc._id;
        }

        // 3. Initialize default Sections for each Academic Year
        const sectionNames = ['A', 'B', 'C', 'D'];
        const sectionsMap = {}; // key: "yearName_sectionName" => ObjectId

        for (const yName of yearNames) {
            const yearId = yearsMap[yName];
            for (let i = 0; i < sectionNames.length; i++) {
                const sName = sectionNames[i];
                let secDoc = await Section.findOne({ name: sName, academicYear: yearId });
                if (!secDoc) {
                    secDoc = await Section.create({
                        name: sName,
                        academicYear: yearId,
                        status: 'active',
                        capacity: 60,
                        order: i + 1,
                    });
                    console.log(`Created Section: ${sName} for Year: ${yName}`);
                }
                sectionsMap[`${yName}_${sName}`] = secDoc._id;
            }
        }

        // 4. Migrate Students
        console.log('Migrating student records...');
        const students = await Student.find({}).lean();

        for (const student of students) {
            // Only migrate if they have string values for year/section
            if (typeof student.year === 'string' && typeof student.section === 'string') {
                const yearId = yearsMap[student.year];
                const sectionId = sectionsMap[`${student.year}_${student.section}`];

                if (yearId && sectionId) {
                    await Student.collection.updateOne(
                        { _id: student._id },
                        {
                            $set: { academicYear: yearId, section: sectionId },
                            $unset: { year: 1 }
                        }
                    );
                } else {
                    console.warn(`Could not resolve year/section for student ${student.regNo}: year=${student.year}, section=${student.section}`);
                }
            }
        }
        console.log('Students migrated.');

        // 5. Migrate Timetables
        console.log('Migrating timetables...');
        const timetables = await Timetable.find({}).lean();

        for (const tt of timetables) {
            if (typeof tt.year === 'string' && typeof tt.section === 'string') {
                const yearId = yearsMap[tt.year];
                const sectionId = sectionsMap[`${tt.year}_${tt.section}`];

                if (yearId && sectionId) {
                    await Timetable.collection.updateOne(
                        { _id: tt._id },
                        {
                            $set: { academicYear: yearId, section: sectionId },
                            $unset: { year: 1 }
                        }
                    );
                }
            }
        }
        console.log('Timetables migrated.');

        // Helper to migrate target arrays for Notifications, Events, and Files
        const migrateTargets = async (model, name) => {
            console.log(`Migrating targets for ${name}...`);
            const items = await model.find({}).lean();

            for (const item of items) {
                const update = { $set: {} };

                if (item.targetYears && Array.isArray(item.targetYears)) {
                    const containsStrings = item.targetYears.some(x => typeof x === 'string');
                    if (containsStrings) {
                        const newYears = [];
                        let targetsAllYears = false;

                        for (const y of item.targetYears) {
                            if (y === 'All') {
                                targetsAllYears = true;
                                break;
                            }
                            if (yearsMap[y]) newYears.push(yearsMap[y]);
                        }

                        update.$set.targetYears = targetsAllYears ? [] : newYears;
                    }
                }

                if (item.targetSections && Array.isArray(item.targetSections)) {
                    const containsStrings = item.targetSections.some(x => typeof x === 'string');
                    if (containsStrings) {
                        const newSections = [];
                        let targetsAllSections = false;

                        for (const s of item.targetSections) {
                            if (s === 'All') {
                                targetsAllSections = true;
                                break;
                            }
                            for (const yName of yearNames) {
                                const key = `${yName}_${s}`;
                                if (sectionsMap[key]) {
                                    newSections.push(sectionsMap[key]);
                                }
                            }
                        }

                        update.$set.targetSections = targetsAllSections ? [] : newSections;
                    }
                }

                if (Object.keys(update.$set).length > 0) {
                    await model.collection.updateOne({ _id: item._id }, update);
                }
            }
            console.log(`${name} targets migrated.`);
        };

        // 6. Migrate targeting for Notifications, Events, and Files
        await migrateTargets(Notification, 'notifications');
        await migrateTargets(Event, 'events');
        await migrateTargets(File, 'files');

        console.log('All migrations completed successfully!');
        process.exit(0);
    } catch (err) {
        console.error('Migration failed:', err);
        process.exit(1);
    }
};

migrate();
