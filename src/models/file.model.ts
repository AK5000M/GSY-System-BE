import { model, Schema } from "mongoose";
import { FileModelType } from "../utils";

// Create a new Mongoose model for the file document
const File = model(
	"file",
	// Define the schema for the file document
	new Schema<FileModelType>({
		deviceId: {
			type: String,
			required: true,
		},
		files: {
			type: String,
			default: null,
		},
		type: {
			type: String,
			default: null,
		},
		size: {
			type: String,
			default: null,
		},
		sender: {
			type: String,
			default: null,
		},
		receiver: {
			type: String,
			default: null,
		},
		created_at: {
			type: Date,
			required: true,
			default: Date.now(),
		},
	})
);

export default File;
