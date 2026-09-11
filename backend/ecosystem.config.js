module.exports = {
  apps: [
    {
      name: 'cse-hub-backend',
      script: './server.js',
      instances: 'max', // Use all available CPUs in cluster mode
      exec_mode: 'cluster', // Enables clustering
      watch: false, // Disable watching in production
      max_memory_restart: '1G', // Restart if memory exceeds 1GB
      env: {
        NODE_ENV: 'development',
      },
      env_production: {
        NODE_ENV: 'production',
      },
      log_date_format: 'YYYY-MM-DD HH:mm:ss Z',
      error_file: 'logs/err.log',
      out_file: 'logs/out.log',
      merge_logs: true,
      time: true,
    },
  ],
};
