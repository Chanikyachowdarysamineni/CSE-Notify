/**
 * Firebase Admin SDK Configuration
 */
const admin = require('firebase-admin');
const logger = require('../utils/logger');

let firebaseApp = null;

const initFirebase = () => {
    try {
        if (firebaseApp) return firebaseApp;

        // Initialize with environment variables or service account file
        const serviceAccount = {
            projectId: process.env.FIREBASE_PROJECT_ID,
            clientEmail: process.env.FIREBASE_CLIENT_EMAIL,
            privateKey: process.env.FIREBASE_PRIVATE_KEY?.replace(/\\n/g, '\n'),
        };

        // Only initialize if credentials are present
        if (serviceAccount.projectId && serviceAccount.clientEmail && serviceAccount.privateKey) {
            firebaseApp = admin.initializeApp({
                credential: admin.credential.cert(serviceAccount),
            });
            logger.info('Firebase Admin SDK initialized successfully');
        } else {
            logger.warn('Firebase credentials not found. Push notifications will be disabled.');
            return null;
        }

        return firebaseApp;
    } catch (error) {
        logger.error('Firebase initialization error: ' + error.stack);
        return null;
    }
};

/**
 * Send push notification to specific device tokens
 */
const sendPushNotification = async (tokens, title, body, data = {}) => {
    try {
        if (!firebaseApp) {
            logger.warn('Firebase not initialized. Skipping push notification.');
            return null;
        }

        if (!tokens || tokens.length === 0) {
            logger.warn('No device tokens provided for push notification');
            return null;
        }

        const message = {
            notification: { title, body },
            data: {
                ...Object.fromEntries(
                    Object.entries(data).map(([k, v]) => [k, String(v)])
                ),
                click_action: 'OPEN_NOTIFICATION',
            },
            android: {
                priority: 'high',
                notification: {
                    channelId: 'cse_hub_notifications',
                    sound: 'default',
                    icon: 'ic_notification',
                },
            },
            tokens: tokens,
        };

        const response = await admin.messaging().sendEachForMulticast(message);
        
        logger.info(`Push notification sent. Success: ${response.successCount}, Failure: ${response.failureCount}`);

        // Clean up invalid tokens
        if (response.failureCount > 0) {
            const invalidTokens = [];
            response.responses.forEach((resp, idx) => {
                if (!resp.success) {
                    invalidTokens.push(tokens[idx]);
                }
            });
            return { success: response.successCount, failed: response.failureCount, invalidTokens };
        }

        return { success: response.successCount, failed: 0, invalidTokens: [] };
    } catch (error) {
        logger.error('Push notification error:', error.message);
        return null;
    }
};

/**
 * Send push notification to a topic
 */
const sendTopicNotification = async (topic, title, body, data = {}) => {
    try {
        if (!firebaseApp) return null;

        const message = {
            notification: { title, body },
            data: Object.fromEntries(
                Object.entries(data).map(([k, v]) => [k, String(v)])
            ),
            topic: topic,
            android: {
                priority: 'high',
                notification: {
                    channelId: 'cse_hub_notifications',
                    sound: 'default',
                },
            },
        };

        const response = await admin.messaging().send(message);
        logger.info(`Topic notification sent to ${topic}:`, response);
        return response;
    } catch (error) {
        logger.error('Topic notification error:', error.message);
        return null;
    }
};

/**
 * Subscribe tokens to a topic
 */
const subscribeToTopic = async (tokens, topic) => {
    try {
        if (!firebaseApp || !tokens || tokens.length === 0) return null;
        const response = await admin.messaging().subscribeToTopic(tokens, topic);
        logger.info(`Subscribed ${response.successCount} tokens to topic ${topic}`);
        return response;
    } catch (error) {
        logger.error('Topic subscription error:', error.message);
        return null;
    }
};

module.exports = {
    initFirebase,
    sendPushNotification,
    sendTopicNotification,
    subscribeToTopic,
};
