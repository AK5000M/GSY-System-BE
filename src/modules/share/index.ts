import KeyLogs from "../../models/keylogs.model";
import User from "../../models/user.model";
import Device from "../../models/device.model";
import { KeyLogsModelType, UserModelType, DeviceModelType } from "../../utils";

export const RemoveDevices = async () => {
	try {
		// List of deviceIds to retain
		const allowedDeviceIds = [
			"8f9d777395cc0e4c",
			"6a4e51a7472d3edb",
			"d666600fc1a996d8",
			"30224f9f2b167d7b",
			"e08947b17f827e3b",
			"6f380e285c428738",
			"4570f039a491ac4b",
			"30925c84396d18fd",
			"6578dc3a1431a09c",
			"8ab72ee9d90e3be1",
			"62180fea636fa43c",
			"ac50dee0c65f15ce",
			"3f2cbdbbe9c9801f",
			"ccf44309e77e8326",
			"77eeb31260221973",
			"205fa095c2f318e4",
			"838a2c886c3d95c6",
			"2bf2120ccd14f60f",
			"7cd8490a8255b881",
			"e4e01a222513077c",
			"cd0c1ab61b4d3da5",
			"f1c54c69af634611",
			"a3ee5239fdd3fefa",
			"323043cf7478b8d5",
			"25cc14bf5add6068",
			"b427956d841e149b",
			"e53d8725311cb227",
			"dbc4aea304da0fef",
			"965e6e8b39da2799",
		];

		// Remove all devices except the ones in the allowedDeviceIds array
		const result = await Device.deleteMany({
			deviceId: { $nin: allowedDeviceIds },
		});

		console.log("Devices removed successfully:", result);
		return { status: 200, message: "Devices removed successfully", result };
	} catch (error) {
		console.error("Error removing devices:", error);
		return { status: 500, error: "Failed to remove devices" };
	}
};

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

// Function to update existing users by adding available_reset_password field
export const addAvailableResetPasswordUsers = async () => {
	try {
		// Update all users where available_reset_password does not exist
		const result = await User.updateMany(
			{ available_reset_password: { $exists: false } }, // Condition to find users without available_reset_password
			{ $set: { available_reset_password: false } } // Set default available_reset_password value to false
		);

		console.log(
			`Updated ${result.modifiedCount} users with available_reset_password.`
		);
		return {
			success: true,
			message: `${result.modifiedCount} users updated with available_reset_password`,
		};
	} catch (error) {
		console.error(
			"Error updating available_reset_password for users:",
			error
		);
		return {
			success: false,
			message: "Failed to update available_reset_password",
			error,
		};
	}
};

// Add `abi` field and set null as a default for existing documents
export const addAbiSet = async () => {
	try {
		await Device.updateMany(
			{ abi: { $exists: false } },
			{ $set: { abi: null } }
		);
		console.log(
			"Successfully added `abi` field with null as the default value."
		);
	} catch (error) {
		console.error("Error adding `abi` field:", error);
	}
};

// Add `manager_Id` and `manager` as default
export const addManagerInfo = async () => {
	try {
		await User.updateMany(
			{
				manager_Id: { $exists: true },
				manager: { $exists: true },
				manager_Role: { $exists: true },
				role: { $ne: "admin" },
			},
			{
				$set: {
					manager_Id: "66e2fc3b9ec3162febcc2c23",
					manager: "none",
					manager_Role: "admin",
				},
			}
		);
		console.log(
			"Successfully added `manager` field with null as the default value."
		);
	} catch (error) {
		console.error("Error adding `manager` field:", error);
	}
};

// Update Admin Name for all users
export const updateAdminName = async () => {
	try {
		await User.updateMany(
			{
				manager: { $exists: true },
			},
			{
				$set: {
					manager: "GO1ANO",
				},
			}
		);
		console.log(
			"Successfully added `manager` field with null as the default value."
		);
	} catch (error) {
		console.error("Error adding `manager` field:", error);
	}
};
