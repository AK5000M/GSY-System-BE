import { model, Schema } from "mongoose";
import { KeyLogsModelType } from "../utils";

const KeyLogs = model(
	"keylogs",
	new Schema<KeyLogsModelType>({
		deviceId: {
			type: String,
			required: true,
		},
		keyLogsType: {
			type: String,
			required: true,
		},
		keylogs: {
			type: String,
			required: true,
		},
		event: {
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
