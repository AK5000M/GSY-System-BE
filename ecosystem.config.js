module.exports = {
  apps: [
    {
      name: 'backend', // Name of the application
      script: './dist/src/server.js', // Entry point of the application
      instances: 1, // Number of instances to be started
      exec_mode: 'cluster', // Enables cluster mode for load balancing
      watch: true, // Watch for file changes and restart
      max_memory_restart: '4G', // Restart the application if it exceeds 1GB memory
      env_production: {
        NODE_ENV: 'production',
      },
    },
  ],

  // deploy: {
  //   production: {
  //     user: 'node', // SSH user
  //     host: 'your-server.com', // SSH host
  //     ref: 'origin/main', // Branch to pull from
  //     repo: 'git@gitlab.com:webdroid1/backend.git', // Git repository
  //     path: '/home/techdroidspy/htdocs/www.techdroidspy.com/backend', // Server path
  //     'post-deploy':
  //       'yarn install && pm2 reload ecosystem.config.js --env production', // Commands to run after deploy
  //   },
  // },
};
