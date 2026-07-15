const http = require('http');

const options = {
    hostname: 'localhost',
    port: 5000,
    headers: {
        'Content-Type': 'application/json'
    }
};

function postRequest(path, data, token) {
    return new Promise((resolve, reject) => {
        const body = JSON.stringify(data);
        const reqOptions = {
            ...options,
            path,
            method: 'POST',
            headers: {
                ...options.headers,
                'Content-Length': Buffer.byteLength(body)
            }
        };

        if (token) {
            reqOptions.headers['Authorization'] = `Bearer ${token}`;
        }

        const req = http.request(reqOptions, (res) => {
            let responseBody = '';
            res.on('data', (chunk) => { responseBody += chunk; });
            res.on('end', () => {
                try {
                    const parsed = JSON.parse(responseBody);
                    resolve({ statusCode: res.statusCode, data: parsed });
                } catch (e) {
                    resolve({ statusCode: res.statusCode, text: responseBody });
                }
            });
        });

        req.on('error', (err) => {
            reject(err);
        });

        req.write(body);
        req.end();
    });
}

async function testNotification() {
    try {
        console.log('Logging in as Faculty (163)...');
        const loginRes = await postRequest('/api/auth/login', {
            loginId: '163',
            password: 'Faculty@123'
        });

        if (!loginRes.data || !loginRes.data.success) {
            throw new Error('Login failed: ' + (loginRes.data ? loginRes.data.message : 'No data'));
        }

        const token = loginRes.data.data.token;
        console.log('Login successful! JWT Token acquired.');

        console.log('Publishing test notification to students...');
        const notifyRes = await postRequest('/api/notifications', {
            title: '📣 Test Announcement from Faculty',
            message: 'Hello students, this is a test push notification sent to test the foreground/background pop-up behavior. Please verify that you received this alert.',
            category: 'General',
            priority: 'urgent',
            targetYears: [],
            targetSections: []
        }, token);

        if (notifyRes.data && notifyRes.data.success) {
            console.log('Notification published successfully!');
            console.log('Response:', notifyRes.data);
        } else {
            console.log('Failed to publish:', notifyRes.data ? notifyRes.data.message : notifyRes.text);
        }
    } catch (error) {
        console.error('Error during test:', error.message);
    }
}

testNotification();
