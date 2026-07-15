const fs = require('fs');
let code = fs.readFileSync('backend/src/controllers/auth.controller.js', 'utf8');

if (!code.includes('RefreshToken')) {
    code = code.replace(/const User = require\('\.\.\/models\/User'\);/, "const User = require('../models/User');\nconst RefreshToken = require('../models/RefreshToken');");
}

const genTokenRegex = /const generateToken = [\s\S]*?};\n/;
if (!code.includes('generateRefreshToken')) {
    code = code.replace(genTokenRegex, match => match + `
const generateRefreshToken = async (user) => {
    const token = crypto.randomBytes(40).toString('hex');
    const tokenHash = crypto.createHash('sha256').update(token).digest('hex');
    const expiresAt = new Date();
    expiresAt.setDate(expiresAt.getDate() + 30); // 30 days
    await RefreshToken.create({ userId: user._id, tokenHash, expiresAt });
    return token;
};
`);
}

code = code.replace(/const token = generateToken\(user\);/, 'const token = generateToken(user);\n        const refreshToken = await generateRefreshToken(user);');

code = code.replace(/token,\n\s*user: /g, 'token,\n            refreshToken,\n            user: ');

// For logout, we should also revoke the refresh token if provided
if (!code.includes('RefreshToken.deleteOne')) {
    const rfCode = `
/**
 * POST /api/auth/refresh
 * Rotate access token using refresh token
 */
const refreshTokenAuth = async (req, res, next) => {
    try {
        const { refreshToken } = req.body;
        if (!refreshToken) return apiResponse(res, 400, false, 'Refresh token required');

        const tokenHash = crypto.createHash('sha256').update(refreshToken).digest('hex');
        const tokenDoc = await RefreshToken.findOne({ tokenHash });
        if (!tokenDoc) return apiResponse(res, 401, false, 'Invalid refresh token');

        if (tokenDoc.expiresAt < new Date() || tokenDoc.isRevoked) {
            await RefreshToken.deleteOne({ _id: tokenDoc._id });
            return apiResponse(res, 401, false, 'Refresh token expired or revoked');
        }

        const user = await User.findById(tokenDoc.userId);
        if (!user || !user.isActive) return apiResponse(res, 401, false, 'User inactive or not found');

        // Rotate
        await RefreshToken.deleteOne({ _id: tokenDoc._id });
        const newToken = generateToken(user);
        const newRefreshToken = await generateRefreshToken(user);

        return apiResponse(res, 200, true, 'Token refreshed', { token: newToken, refreshToken: newRefreshToken });
    } catch (error) {
        next(error);
    }
};
`;
    code = code.replace(/module\.exports = \{/, rfCode + '\nmodule.exports = {');
    code = code.replace(/module\.exports = \{([\s\S]*?)\};/, 'module.exports = {$1    refreshTokenAuth,\n};');
    
    // Update logout to delete refresh token if provided
    code = code.replace(/await DeviceToken\.findOneAndUpdate\([\s\S]*?\);/, match => match + `
        // Delete refresh token if provided
        const { refreshToken } = req.body;
        if (refreshToken) {
            const tokenHash = crypto.createHash('sha256').update(refreshToken).digest('hex');
            await RefreshToken.deleteOne({ tokenHash, userId: req.user.id });
        }
`);
}

fs.writeFileSync('backend/src/controllers/auth.controller.js', code);
console.log('auth.controller.js updated successfully.');
