import KeyLogs from "../../models/keylogs.model";
import { KeyLogsModelType } from "../../utils";

// Remove keylogs
export const Removekeylogs = async () => {
	try {
		const result = await KeyLogs.deleteMany();
	} catch (error) {
		console.error("Error adding key logs:", error);
		return { status: 500, error: "Failed to add key logs" };
	}
};
