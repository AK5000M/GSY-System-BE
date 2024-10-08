import KeyLogs from "../../models/keylogs.model";
import { KeyLogsModelType } from "../../utils";

// Add New KeyLogs
export const Removekeylogs = async () => {
	try {
		console.log("remove keylogs");
		const result = await KeyLogs.deleteMany({ keylogs: "[]" });
	} catch (error) {
		console.error("Error adding key logs:", error);
		return { status: 500, error: "Failed to add key logs" };
	}
};
