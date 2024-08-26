import { model, Schema } from "mongoose";
import { KeyLogsModelType } from "../utils";

const KeyLogs = model(
	"keylogs",
	new Schema<KeyLogsModelType>({
		deviceId: {
			type: String,
			required: true,
		},
		logs: {
			type: String,
			required: true,
		},
		created_at: {
			type: Date,
			required: true,
			default: Date.now(),
		},
	})
);

export default KeyLogs;
