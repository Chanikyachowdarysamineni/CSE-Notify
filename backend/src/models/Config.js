/**
 * Config Model - Stores dynamic metadata parameters in the database
 */
const mongoose = require('mongoose');

const configSchema = new mongoose.Schema({
    key: {
        type: String,
        required: [true, 'Config key is required'],
        unique: true,
        trim: true,
    },
    value: {
        type: mongoose.Schema.Types.Mixed,
        required: [true, 'Config value is required'],
    },
}, {
    timestamps: true,
});

module.exports = mongoose.model('Config', configSchema);
