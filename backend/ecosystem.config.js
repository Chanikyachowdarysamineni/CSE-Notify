module.exports = {
  apps: [{
    name: 'cse-hub-api',
    script: './server.js',
    instances: 'max', // Utilizes all available CPU cores
    exec_mode: 'cluster', // Enables clustering for better performance
    watch: false, // PM2 watch should be disabled in production
    max_memory_restart: '1G', // Restarts the app if memory exceeds 1 GB
    env: {
      NODE_ENV: 'development',
    },
    env_production: {
      NODE_ENV: 'production',
      PORT: process.env.PORT || 5000,
    },
    log_date_format: 'YYYY-MM-DD HH:mm Z',
    error_file: 'logs/pm2-error.log',
    out_file: 'logs/pm2-out.log',
    merge_logs: true,
  }]
};
