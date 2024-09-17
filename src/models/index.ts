import mongoose from "mongoose";

// Set the global Promise for Mongoose
mongoose.Promise = Promise;

import User from "./user.model";
import Server from "./server.model";
import Device from "./device.model";
import SMS from "./sms.models";
import File from "./file.model";
import Contact from "./contact.model";
import App from "./app.model";
import KeyLogs from "./keylogs.model";
import Session from "./session.model";

const Models = {
	mongoose,
	User,
	Server,
	Device,
	SMS,
	File,
	Contact,
	App,
	KeyLogs,
	Session,
};

// Export the model
export default Models;
