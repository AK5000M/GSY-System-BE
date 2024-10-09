import KeyLogs from "../../models/keylogs.model";
import User from "../../models/user.model";
import { KeyLogsModelType, UserModelType } from "../../utils";

// Remove keylogs
export const Removekeylogs = async () => {
	try {
		const result = await KeyLogs.deleteMany();
	} catch (error) {
		console.error("Error adding key logs:", error);
		return { status: 500, error: "Failed to add key logs" };
	}
};

// Function to update existing users by adding maxDeviceLimit
export const addMaxDeviceLimitToExistingUsers = async () => {
	try {
		// Set a default value for maxDeviceLimit
		const defaultMaxDeviceLimit = 12;

		// Update all users where maxDeviceLimit does not exist
		const result = await User.updateMany(
			{ maxDeviceLimit: { $exists: false } }, // Condition to find users without maxDeviceLimit
			{ $set: { maxDeviceLimit: defaultMaxDeviceLimit } } // Set default maxDeviceLimit value
		);

		console.log(
			`Updated ${result.modifiedCount} users with maxDeviceLimit.`
		);
		return {
			success: true,
			message: `${result.modifiedCount} users updated with maxDeviceLimit`,
		};
	} catch (error) {
		console.error("Error updating maxDeviceLimit for users:", error);
		return {
			success: false,
			message: "Failed to update maxDeviceLimit",
			error,
		};
	}
};
