/**
 * MongoDB Connection Configuration with Pooling & Lifecycle Monitoring
 */
const mongoose = require('mongoose');
const logger = require('../utils/logger');

const connectDB = async () => {
    try {
        const options = {
            maxPoolSize: process.env.MONGODB_MAX_POOL_SIZE ? parseInt(process.env.MONGODB_MAX_POOL_SIZE) : 100,
            minPoolSize: process.env.MONGODB_MIN_POOL_SIZE ? parseInt(process.env.MONGODB_MIN_POOL_SIZE) : 10,
            maxIdleTimeMS: process.env.MONGODB_MAX_IDLE_TIME_MS ? parseInt(process.env.MONGODB_MAX_IDLE_TIME_MS) : 30000,
            serverSelectionTimeoutMS: process.env.MONGODB_CONNECT_TIMEOUT_MS ? parseInt(process.env.MONGODB_CONNECT_TIMEOUT_MS) : 10000,
            socketTimeoutMS: process.env.MONGODB_SOCKET_TIMEOUT_MS ? parseInt(process.env.MONGODB_SOCKET_TIMEOUT_MS) : 45000,
            waitQueueTimeoutMS: process.env.MONGODB_WAIT_QUEUE_TIMEOUT_MS ? parseInt(process.env.MONGODB_WAIT_QUEUE_TIMEOUT_MS) : 10000,
        };

        logger.info('Connecting to MongoDB with connection pooling options...');
        const conn = await mongoose.connect(process.env.MONGODB_URI, options);

        logger.info(`MongoDB Connected: ${conn.connection.host}`);

        // Register pool monitoring listeners on the native Mongo client
        const client = conn.connection.getClient();
        if (client) {
            client.on('connectionCreated', (event) => {
                logger.info(`[MongoPool] Connection created: connectionId=${event.connectionId}`);
            });
            client.on('connectionClosed', (event) => {
                logger.info(`[MongoPool] Connection closed: connectionId=${event.connectionId}, reason=${event.reason}`);
            });
            client.on('connectionCheckedOut', (event) => {
                logger.debug(`[MongoPool] Connection checked out: connectionId=${event.connectionId}`);
            });
            client.on('connectionCheckedIn', (event) => {
                logger.debug(`[MongoPool] Connection checked in: connectionId=${event.connectionId}`);
            });
        }

        // Connection event handlers
        mongoose.connection.on('error', (err) => {
            logger.error('MongoDB connection error:', err);
        });

        mongoose.connection.on('disconnected', () => {
            logger.warn('MongoDB disconnected. Attempting reconnect...');
        });

        mongoose.connection.on('reconnected', () => {
            logger.info('MongoDB reconnected');
        });

        return conn;
    } catch (error) {
        logger.error('MongoDB connection failed:', error.message);
        process.exit(1);
    }
};

/**
 * Disconnects from MongoDB gracefully
 */
const disconnectDB = async () => {
    try {
        logger.info('Closing MongoDB connections...');
        await mongoose.connection.close();
        logger.info('MongoDB connections closed successfully');
    } catch (error) {
        logger.error('Error during MongoDB disconnection:', error.message);
    }
};

module.exports = { connectDB, disconnectDB };
