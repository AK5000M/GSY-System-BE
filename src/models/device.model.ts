import { model, Schema } from "mongoose";
import { DeviceModelType } from "../utils";

// Create a new Mongoose model for the device document
const Device = model(
	"device",
	// Define the schema for the device document
	new Schema<DeviceModelType>({
		deviceId: {
			type: String,
			required: true,
			unique: true,
		},
		userId: {
			type: String,
			required: true,
		},
		socketId: {
			type: String,
			default: "none",
		},
		deviceInfo: {
			type: String,
			default: null,
		},
		hwid: {
			type: String,
			default: null,
		},
		installationDate: {
			type: String,
			default: null,
		},
		manufacturer: {
			type: String,
			default: null,
		},
		models: {
			type: String,
			default: null,
		},
		online: {
			type: Boolean,
			default: false,
		},
		permissions: {
			type: String,
			default: null,
		},
		lastseen: {
			type: String,
			default: null,
		},
		nodelogs: {
			type: String,
			default: null,
		},
		screenCapture: {
			type: String,
			default: null,
		},
		geoFencing: {
			type: String,
			default: null,
		},
		deviceSetting: {
			type: String,
			default: null,
		},
		batteryStatus: {
			type: String,
			default: "100",
		},
		remoteControl: {
			type: String,
			default: null,
		},
		remoteWipe: {
			type: String,
			default: null,
		},
		version: {
			type: String,
			default: null,
		},
		connectStatus: {
			type: String,
			default: "4",
		},
		botStatus: {
			type: Boolean,
			default: false,
		},
		userType: {
			type: String,
			default: "mine", // child, employee, mine
		},
		wsRoomId: {
			type: String,
			default: null,
		},
		blackScreen: {
			type: Boolean,
			default: false,
		},
		lockScreen: {
			type: Boolean,
			default: false,
		},
		securityData: {
			type: Array,
			default: null,
		},
		securityStatus: {
			type: Boolean,
			default: false,
		},
		created_at: {
			type: Date,
			required: true,
			default: Date.now(),
		},
	})
);

export default Device;
