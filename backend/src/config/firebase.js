/**
 * Firebase Admin SDK Configuration
 *
 * IMPORTANT: We use DATA-ONLY FCM messages (no "notification" block).
 * This ensures onMessageReceived() always fires on the Android client
 * regardless of whether the app is foreground, background, or terminated.
 * The Android client is responsible for building and displaying the notification UI.
 */
const admin = require('firebase-admin');
const DeviceToken = require('../models/DeviceToken');
const logger = require('../utils/logger');

let firebaseApp = null;

const initFirebase = () => {
    try {
        if (firebaseApp) return firebaseApp;

        const serviceAccount = {
            projectId: process.env.FIREBASE_PROJECT_ID,
            clientEmail: process.env.FIREBASE_CLIENT_EMAIL,
            privateKey: process.env.FIREBASE_PRIVATE_KEY?.replace(/\\n/g, '\n'),
        };

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
 * Send a DATA-ONLY push notification to specific device tokens.
 * No "notification" block — the Android client's onMessageReceived() always fires.
 *
 * @param {string[]} tokens - Array of FCM device tokens
 * @param {string} title - Notification title (sent in data payload)
 * @param {string} body - Notification body (sent in data payload)
 * @param {Object} data - Additional data key-value pairs
 * @returns {Promise<{success: number, failed: number, invalidTokens: string[]}>}
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

        // Chunk tokens into batches of 500 (FCM multicast limit)
        const BATCH_SIZE = 500;
        const batches = [];
        for (let i = 0; i < tokens.length; i += BATCH_SIZE) {
            batches.push(tokens.slice(i, i + BATCH_SIZE));
        }

        let totalSuccess = 0;
        let totalFailure = 0;
        const allInvalidTokens = [];

        for (const batch of batches) {
            // Build data-only payload — all fields must be strings
            const dataPayload = {
                title: String(title || ''),
                body: String(body || ''),
                click_action: 'OPEN_NOTIFICATION',
                timestamp: String(Date.now()),
                ...Object.fromEntries(
                    Object.entries(data).map(([k, v]) => [k, String(v)])
                ),
            };

            const message = {
                // NO "notification" block — data only
                data: dataPayload,
                android: {
                    priority: 'high',   // Wakes up device for terminated-state delivery
                    ttl: 86400000,       // 24 hours in milliseconds
                    restrictedPackageName: 'com.vfstr.cse',
                },
                tokens: batch,
            };

            // Exponential backoff retry loop (max 3 attempts)
            const MAX_RETRIES = 3;
            let response = null;
            for (let attempt = 1; attempt <= MAX_RETRIES; attempt++) {
                try {
                    response = await admin.messaging().sendEachForMulticast(message);
                    break; // Exit loop on successful API execution
                } catch (err) {
                    if (attempt === MAX_RETRIES) {
                        logger.error(`FCM multicast totally failed after ${MAX_RETRIES} attempts. Error: ${err.message}`);
                        throw err; // Break out to outer catch
                    }
                    const backoffDelay = Math.pow(2, attempt) * 1000; // 2s, 4s, 8s
                    logger.warn(`FCM API Error (Attempt ${attempt}): ${err.message}. Retrying in ${backoffDelay}ms...`);
                    await new Promise(res => setTimeout(res, backoffDelay));
                }
            }
            totalSuccess += response.successCount;
            totalFailure += response.failureCount;

            // Collect invalid tokens for cleanup
            if (response.failureCount > 0) {
                response.responses.forEach((resp, idx) => {
                    if (!resp.success) {
                        const errorCode = resp.error?.code || '';
                        // Only mark as invalid if the token itself is bad
                        if (
                            errorCode === 'messaging/invalid-registration-token' ||
                            errorCode === 'messaging/registration-token-not-registered'
                        ) {
                            allInvalidTokens.push(batch[idx]);
                        }
                        logger.warn(`FCM send failed for token[${idx}]: ${errorCode}`);
                    }
                });
            }
        }

        logger.info(`Push notification sent. Success: ${totalSuccess}, Failure: ${totalFailure}`);

        // Deactivate invalid tokens in the database
        if (allInvalidTokens.length > 0) {
            await DeviceToken.updateMany(
                { token: { $in: allInvalidTokens } },
                { isActive: false }
            );
            logger.info(`Deactivated ${allInvalidTokens.length} invalid FCM tokens`);
        }

        return { success: totalSuccess, failed: totalFailure, invalidTokens: allInvalidTokens };
    } catch (error) {
        logger.error('Push notification error:', error.message);
        return null;
    }
};

/**
 * Send a data-only push notification to a topic
 */
const sendTopicNotification = async (topic, title, body, data = {}) => {
    try {
        if (!firebaseApp) return null;

        const message = {
            data: {
                title: String(title || ''),
                body: String(body || ''),
                click_action: 'OPEN_NOTIFICATION',
                timestamp: String(Date.now()),
                ...Object.fromEntries(
                    Object.entries(data).map(([k, v]) => [k, String(v)])
                ),
            },
            topic,
            android: {
                priority: 'high',
                ttl: 86400000,
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
