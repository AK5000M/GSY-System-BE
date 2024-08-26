import { model, Schema } from "mongoose";
import { ServerModelType } from "../utils";

// Create a new Mongoose model for the server document
const Server = model(
	"server",
	// Define the schema for the server document
	new Schema<ServerModelType>({
		registered_users: {
			type: Number,
			default: 0,
			min: 0, // Minimum value for registered_users field is set to 0
		},
		signin_users: {
			type: Number,
			default: 0,
			min: 0, // Minimum value for signin_users field is set to 0
		},
		deleted_users: {
			type: Number,
			default: 0,
			min: 0, // Minimum value for deleted_users field is set to 0
		},
		purchased_users: {
			type: Number,
			default: 0,
			min: 0, // Minimum value for purchased_users field is set to 0
		},
		stripe_webhook_id: {
			type: String,
			default: null,
		},
		stripe_webhook_secret: {
			type: String,
			default: null,
		},
	})
);

export default Server;
