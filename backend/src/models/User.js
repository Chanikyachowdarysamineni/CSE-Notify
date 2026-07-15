/**
 * User Model - Base authentication entity
 * 
 * All users (Admin, Faculty, Student) have a corresponding User document
 * for authentication purposes. Role-specific data is stored in separate collections.
 */
const mongoose = require('mongoose');
const bcrypt = require('bcryptjs');

const userSchema = new mongoose.Schema({
    loginId: {
        type: String,
        required: [true, 'Login ID is required'],
        unique: true,
        trim: true,
        uppercase: true, // Typically regNo and employeeId are uppercase
    },
    email: {
        type: String,
        trim: true,
        lowercase: true,
        match: [/^\S+@\S+\.\S+$/, 'Please provide a valid email'],
        sparse: true,
    },
    password: {
        type: String,
        required: [true, 'Password is required'],
        minlength: [6, 'Password must be at least 6 characters'],
        select: false, // Don't include password in queries by default
    },
    name: {
        type: String,
        required: [true, 'Name is required'],
        trim: true,
        maxlength: [100, 'Name must be under 100 characters'],
    },
    role: {
        type: String,
        enum: ['admin', 'faculty', 'student'],
        required: [true, 'Role is required'],
    },
    isActive: {
        type: Boolean,
        default: true,
    },
    resetPasswordToken: String,
    resetPasswordExpires: Date,
    lastLogin: Date,
    fcmToken: String,
}, {
    timestamps: true,
});

// Index for faster lookups
userSchema.index({ loginId: 1 });
userSchema.index({ email: 1 }, { sparse: true });
userSchema.index({ role: 1 });

// Hash password before saving
userSchema.pre('save', async function (next) {
    if (!this.isModified('password')) return next();
    
    try {
        const salt = await bcrypt.genSalt(12);
        this.password = await bcrypt.hash(this.password, salt);
        next();
    } catch (error) {
        next(error);
    }
});

// Compare password method
userSchema.methods.comparePassword = async function (candidatePassword) {
    return bcrypt.compare(candidatePassword, this.password);
};

// Remove sensitive fields from JSON output
userSchema.methods.toJSON = function () {
    const obj = this.toObject();
    delete obj.password;
    delete obj.resetPasswordToken;
    delete obj.resetPasswordExpires;
    delete obj.__v;
    return obj;
};

module.exports = mongoose.model('User', userSchema);
