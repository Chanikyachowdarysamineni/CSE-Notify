/**
 * CSE HUB Backend Server
 * 
 * Enterprise-grade Node.js backend for the CSE Department communication platform.
 * Handles authentication, notifications, events, timetable, files, gallery, and admin operations.
 */

require('dotenv').config();
const express = require('express');
const http = require('http');
const cors = require('cors');
const helmet = require('helmet');
const morgan = require('morgan');
const path = require('path');
const { Server } = require('socket.io');
const mongoSanitize = require('express-mongo-sanitize');
const rateLimit = require('express-rate-limit');

const connectDB = require('./src/config/db');
const { errorHandler, notFound } = require('./src/middleware/errorHandler');
const logger = require('./src/utils/logger');

// Import routes
const authRoutes = require('./src/routes/auth.routes');
const notificationRoutes = require('./src/routes/notification.routes');
const eventRoutes = require('./src/routes/event.routes');
const timetableRoutes = require('./src/routes/timetable.routes');
const fileRoutes = require('./src/routes/file.routes');
const galleryRoutes = require('./src/routes/gallery.routes');
const profileRoutes = require('./src/routes/profile.routes');
const searchRoutes = require('./src/routes/search.routes');
const adminRoutes = require('./src/routes/admin.routes');
const dashboardRoutes = require('./src/routes/dashboard.routes');
const academicYearRoutes = require('./src/routes/academicYear.routes');
const sectionRoutes = require('./src/routes/section.routes');

// Import cron jobs
const { initCronJobs } = require('./src/jobs');

// Import Firebase
const { initFirebase } = require('./src/config/firebase');

const app = express();
const server = http.createServer(app);

// Socket.IO setup
const io = new Server(server, {
    cors: {
        origin: process.env.CLIENT_URL || '*',
        methods: ['GET', 'POST']
    }
});

// Make io accessible to routes
app.set('io', io);

// ============================================
// Middleware Stack
// ============================================

// Security headers
app.use(helmet({
    crossOriginResourcePolicy: false // Allow loading images from same origin (for uploads)
}));

// Rate limiting (Global)
const globalLimiter = rateLimit({
    windowMs: 15 * 60 * 1000, // 15 minutes
    max: 1000, // Limit each IP to 1000 requests per `window`
    standardHeaders: true,
    legacyHeaders: false,
    message: { success: false, message: 'Too many requests from this IP, please try again after 15 minutes' }
});
app.use('/api/', globalLimiter);

// CORS configuration
app.use(cors({
    origin: process.env.CLIENT_URL || '*',
    methods: ['GET', 'POST', 'PUT', 'DELETE', 'PATCH'],
    allowedHeaders: ['Content-Type', 'Authorization'],
    credentials: true
}));

// Request logging
if (process.env.NODE_ENV === 'development') {
    app.use(morgan('dev'));
} else {
    app.use(morgan('combined', {
        stream: { write: (message) => logger.info(message.trim()) }
    }));
}

// Body parsing
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true, limit: '10mb' }));

// Data sanitization against NoSQL query injection
app.use(mongoSanitize());

// Static files (uploads)
app.use('/uploads', express.static(path.join(__dirname, 'uploads')));

// ============================================
// API Routes
// ============================================

app.use('/api/auth', authRoutes);
app.use('/api/notifications', notificationRoutes);
app.use('/api/events', eventRoutes);
app.use('/api/timetable', timetableRoutes);
app.use('/api/files', fileRoutes);
app.use('/api/gallery', galleryRoutes);
app.use('/api/profile', profileRoutes);
app.use('/api/search', searchRoutes);
app.use('/api/admin', adminRoutes);
app.use('/api/dashboard', dashboardRoutes);
app.use('/api/academic-years', academicYearRoutes);
app.use('/api/sections', sectionRoutes);

// Health check endpoint
app.get('/api/health', (req, res) => {
    res.json({
        status: 'ok',
        timestamp: new Date().toISOString(),
        uptime: process.uptime(),
        environment: process.env.NODE_ENV
    });
});

// API version info
app.get('/api', (req, res) => {
    res.json({
        name: 'CSE HUB API',
        version: '1.0.0',
        description: 'Enterprise communication platform for CSE Department'
    });
});

// ============================================
// Error Handling
// ============================================

app.use(notFound);
app.use(errorHandler);

// ============================================
// Socket.IO Connection Handling
// ============================================

io.on('connection', (socket) => {
    logger.info(`Socket connected: ${socket.id}`);

    // Join room based on user role and year/section
    socket.on('join', (data) => {
        if (data.role) socket.join(`role_${data.role}`);
        if (data.year) socket.join(`year_${data.year}`);
        if (data.section) socket.join(`section_${data.section}`);
        if (data.userId) socket.join(`user_${data.userId}`);
        logger.info(`Socket ${socket.id} joined rooms:`, data);
    });

    socket.on('disconnect', () => {
        logger.info(`Socket disconnected: ${socket.id}`);
    });
});

// ============================================
// Server Startup
// ============================================

const PORT = process.env.PORT || 5000;

const startServer = async () => {
    try {
        // Connect to MongoDB
        await connectDB();
        logger.info('MongoDB connected successfully');

        // Initialize Firebase
        initFirebase();

        // Initialize cron jobs
        initCronJobs();
        logger.info('Cron jobs initialized');

        // Start server
        server.listen(PORT, () => {
            logger.info(`CSE HUB Backend running on port ${PORT}`);
            logger.info(`Environment: ${process.env.NODE_ENV || 'development'}`);
            logger.info(`API: http://localhost:${PORT}/api`);
        });
    } catch (error) {
        logger.error('Failed to start server:', error);
        process.exit(1);
    }
};

startServer();

// Graceful shutdown
process.on('SIGTERM', () => {
    logger.info('SIGTERM received. Shutting down gracefully...');
    server.close(() => {
        logger.info('Server closed');
        process.exit(0);
    });
});

process.on('unhandledRejection', (err) => {
    logger.error('Unhandled Rejection:', err);
});

module.exports = { app, server, io };
