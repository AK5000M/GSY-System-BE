import { model, Schema } from "mongoose";
import { AppModelType } from "../utils";

// Create a new Mongoose model for the app document
const App = model(
	"app",
	// Define the schema for the app document
	new Schema<AppModelType>({
		userId: {
			type: String,
			required: true,
		},
		name: {
			type: String,
			default: null,
		},
		path: {
			type: String,
			default: null,
		},
		apkVersion: {
			type: String,
			default: "V1.0", // update version
		},
		created_at: {
			type: Date,
			required: true,
			default: Date.now(),
		},
	})
);

export default App;
