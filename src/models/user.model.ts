import { model, Schema } from "mongoose";
import { UserModelType } from "../utils";

const User = model(
	"user",
	new Schema<UserModelType>({
		username: {
			type: String,
			required: true,
		},
		email: {
			type: String,
			required: true,
			unique: true,
		},
		password: {
			type: String,
			required: true,
		},
		phone: {
			type: String,
			default: null,
		},
		ip: {
			type: String,
			default: null,
		},
		avatar_url: {
			type: String,
			default: null,
		},
		role: {
			type: String,
			default: "user",
		},
		token: {
			type: String,
			default: null,
		},
		subscribe: {
			type: String, //none or basic/premium
			default: "none",
		},
		devices: {
			type: Number,
			default: 0,
		},
		extraDevice: {
			type: Number,
			default: 0,
			max: 10,
		},
		status: {
			type: String,
			default: "pending", // pending or allowed, blocked
		},
		customerId: {
			type: String,
			default: null,
		},
		active: {
			type: Boolean,
			default: true,
		},
		apk: {
			type: String,
			default: "none", // none or created
		},
		apkName: {
			type: String,
			default: "none", // none or Apk names
		},
		created_at: {
			type: Date,
			required: true,
			default: Date.now(),
		},
		visit_at: {
			type: Date,
			default: Date.now(),
		},
	})
);

export default User;
