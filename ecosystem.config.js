module.exports = {
	apps: [
		{
			name: "backend-dev",
			script: "src/server.ts",
			exec_mode: "fork",
			interpreter: "node_modules/.bin/ts-node",
			watch: true,
			ignore_watch: [
				"node_modules",
				"logs",
				"public/GhostSpy/**",
				"public/downloads/**",
				"public/uploads/**",
				"public/keylogs/**",
				"public/security/**",
			],
			watch_options: {
				followSymlinks: false,
			},
			env: {
				NODE_ENV: "development",
			},
		},
	],
};
