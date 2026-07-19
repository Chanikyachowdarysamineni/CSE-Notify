/**
 * Enterprise-grade Rate Limiting Middleware
 * Supports per-IP & per-user limits, granular sensitivity thresholds, and structured logs.
 */
const rateLimit = require('express-rate-limit');
const logger = require('../utils/logger');

// Generic key generator that uses user ID when authenticated, falling back to IP address
const keyGenerator = (req) => {
    return req.user ? `user:${req.user.id}` : `ip:${req.ip}`;
};

// Generic event logger when rate limit is exceeded
const rateLimitHandler = (limiterName) => {
    return (req, res, next, options) => {
        logger.warn({
            message: `Rate limit exceeded on ${limiterName}`,
            limiter: limiterName,
            key: keyGenerator(req),
            ip: req.ip,
            userId: req.user?.id || null,
            method: req.method,
            url: req.originalUrl,
        });

        // Parse duration dynamically to provide a Retry-After response header
        if (options.windowMs) {
            res.setHeader('Retry-After', Math.ceil(options.windowMs / 1000));
        }

        res.status(options.statusCode).json({
            success: false,
            message: options.message,
        });
    };
};

// General API rate limiter (for standard GET endpoints)
const apiLimiter = rateLimit({
    windowMs: 15 * 60 * 1000, // 15 minutes
    max: parseInt(process.env.RATE_LIMIT_API_MAX) || 10000, // 10000 requests per 15 mins for high volume production
    keyGenerator,
    handler: rateLimitHandler('General_API_Limiter'),
    standardHeaders: 'draft-7',
    legacyHeaders: false,
    message: 'Too many API requests. Please try again later.',
});

// Strict limiter for authentication (brute-force prevention)
const authLimiter = rateLimit({
    windowMs: 15 * 60 * 1000, // 15 minutes
    max: parseInt(process.env.RATE_LIMIT_AUTH_MAX) || 15, // 15 login/refresh/forgot attempts per 15 mins
    keyGenerator,
    handler: rateLimitHandler('Auth_Limiter'),
    standardHeaders: 'draft-7',
    legacyHeaders: false,
    message: 'Too many authentication attempts. Please try again in 15 minutes.',
});

// File Upload rate limiter
const uploadLimiter = rateLimit({
    windowMs: 60 * 60 * 1000, // 1 hour
    max: parseInt(process.env.RATE_LIMIT_UPLOAD_MAX) || 10, // Max 10 uploads per hour per user/IP
    keyGenerator,
    handler: rateLimitHandler('Upload_Limiter'),
    standardHeaders: 'draft-7',
    legacyHeaders: false,
    message: 'Upload limit reached. Please try again in an hour.',
});

// Student Search rate limiter (prevent scraping/DB exhaustion)
const searchLimiter = rateLimit({
    windowMs: 15 * 60 * 1000, // 15 minutes
    max: parseInt(process.env.RATE_LIMIT_SEARCH_MAX) || 150, // Max 150 search requests per 15 mins
    keyGenerator,
    handler: rateLimitHandler('Search_Limiter'),
    standardHeaders: 'draft-7',
    legacyHeaders: false,
    message: 'Too many search requests. Please try again later.',
});

// Notification Broadcast rate limiter
const notificationLimiter = rateLimit({
    windowMs: 15 * 60 * 1000, // 15 minutes
    max: parseInt(process.env.RATE_LIMIT_NOTIFICATION_MAX) || 50, // Max 50 notification posts per 15 mins
    keyGenerator,
    handler: rateLimitHandler('Notification_Limiter'),
    standardHeaders: 'draft-7',
    legacyHeaders: false,
    message: 'Too many notification posts. Please try again later.',
});

module.exports = {
    apiLimiter,
    authLimiter,
    uploadLimiter,
    searchLimiter,
    notificationLimiter,
};
