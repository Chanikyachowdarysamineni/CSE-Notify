/**
 * JWT Configuration
 */
module.exports = {
    secret: process.env.JWT_SECRET || 'cse_hub_default_secret_change_in_production',
    expiresIn: process.env.JWT_EXPIRES_IN || '7d',
    refreshExpiresIn: process.env.JWT_REFRESH_EXPIRES_IN || '30d',
    issuer: 'cse-hub-api',
    audience: 'cse-hub-app',
};
