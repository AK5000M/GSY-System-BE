import { Request, Response } from "express";
import axios from "axios";

import { AsaasClient } from "asaas";

const apiUrl = process.env.AsaasAPI_URL;

const AccessToken = process.env.Access_token as string;
const asaas = new AsaasClient(AccessToken, {
	sandbox: false,
	//sandboxUrl?: string (default: https://sandbox.asaas.com/api/v3);
	//baseUrl?: string (default: https://api.asaas.com/v3);
});

// Checking and Add Customer
export const customerData = async (customer: any) => {
	try {
		// check the customer
		const res = await asaas.customers.list({
			email: customer?.email,
		});

		if (res.totalCount === 0) {
			const newCustomer = await asaas.customers.new(customer);
			return newCustomer;
		} else {
			const currentCustomer = res.data[0];
			return currentCustomer;
		}
	} catch (error) {
		console.error("Error getting customers:", error);
		throw new Error("Failed to get customers");
	}
};

// Create New Subscribe
export const newSubscription = async (data: any) => {
	try {
		const options = {
			method: "POST",
			url: `${apiUrl}/v3/subscriptions`,
			headers: {
				accept: "application/json",
				"content-type": "application/json",
				access_token: AccessToken,
			},
			data: {
				customer: data.customer,
				billingType: data.billingType,
				value: data.value,
				cycle: data.cycle,
				description: data.description,
				callback: data.callback,
			},
		};

		const response = await axios.request(options);

		return response;
	} catch (error) {
		console.error("Error getting customers:", error);
		throw error;
	}
};

// Get New Subscription
export const getSubscription = async (subscriptionId: string) => {
	try {
		console.log("subscription Id:", subscriptionId);
		const options = {
			method: "GET",
			url: `${apiUrl}/v3/subscriptions/${subscriptionId}/payments`,
			headers: {
				accept: "application/json",
				"content-type": "application/json",
				access_token: AccessToken,
			},
		};

		const response = await axios.request(options);

		return response;
	} catch (error) {
		console.error("Error getting customers:", error);
		throw error;
	}
};

// Create Credit Card Token
export const createCardToken = async (data: any) => {
	try {
		const options = {
			method: "POST",
			url: `${apiUrl}/v3/creditCard/tokenize`,
			headers: {
				accept: "application/json",
				"content-type": "application/json",
				access_token: AccessToken, // Replace with your actual access token
			},
			data: {
				customer: data.customer,
				creditCard: data.creditCard,
				creditCardHolderInfo: data.creditCardHolderInfo,
				remoteIp: data.remoteIp,
			},
		};

		const response = await axios.request(options);
		return response.data;
	} catch (error) {
		console.error("Error credit card token:", error);
		throw new Error("Failed to get credit card token");
	}
};

//  ********** I don't use these function currently *****************

// export const createNewSubscribe = async (data: any) => {
// 	try {
// 		const options = {
// 			method: "POST",
// 			url: "https://sandbox.asaas.com/api/v3/subscriptions/",
// 			headers: {
// 				accept: "application/json",
// 				"content-type": "application/json",
// 				access_token: AccessToken,
// 			},
// 			data: {
// 				customer: data.customer,
// 				billingType: data.billingType,
// 				value: data.value,
// 				cycle: data.cycle,
// 				description: data.description,
// 				creditCard: data.creditCard,
// 				creditCardHolderInfo: data.creditCardHolderInfo,
// 				callback: data.callback,
// 			},
// 		};

// 		const response = await axios.request(options);
// 		// return response;
// 	} catch (error) {
// 		console.error("Error getting customers:", error);
// 		throw error;
// 	}
// };

// Create PIX payment
// export const createNewPIXPayment = async (data: any) => {
// 	try {
// 		const options = {
// 			method: "POST",
// 			url: "https://sandbox.asaas.com/api/v3/payments",
// 			headers: {
// 				accept: "application/json",
// 				"content-type": "application/json",
// 				access_token: AccessToken,
// 			},
// 			data: {
// 				customer: data.customer,
// 				billingType: data.billingType,
// 				dueDate: data.dueDate,
// 				value: data.value,
// 				daysAfterDueDateToRegistrationCancellation:
// 					data.daysAfterDueDateToRegistrationCancellation,
// 				description: data.description,
// 				// callback: data.callback,
// 			},
// 		};

// 		const response = await axios.request(options);

// 		return response;
// 	} catch (error) {
// 		console.error("Error getting customers:", error);
// 		throw error;
// 	}
// };

// // Get PIX Code
// export const getNewPIXCode = async (paymentId: string) => {
// 	try {
// 		const options = {
// 			method: "GET",
// 			url: `https://sandbox.asaas.com/api/v3/payments/${paymentId}/pixQrCode`,
// 			headers: {
// 				accept: "application/json",
// 				"content-type": "application/json",
// 				access_token: AccessToken,
// 			},
// 		};

// 		const response = await axios.request(options);

// 		return response;
// 	} catch (error) {
// 		console.error("Error getting customers:", error);
// 		throw error;
// 	}
// };

// // Create PIX Payment URL
// export const createNewPixPaymentURL = async (data: any) => {
// 	try {
// 		const options = {
// 			method: "POST",
// 			url: "https://sandbox.asaas.com/api/v3/paymentLinks",
// 			headers: {
// 				accept: "application/json",
// 				"content-type": "application/json",
// 				access_token: AccessToken,
// 			},
// 			data: {
// 				name: data.name,
// 				description: data.description,
// 				endDate: data.endDate,
// 				value: data.value,
// 				billingType: data.billingType,
// 				chargeType: data.chargeType,
// 				dueDateLimitDays: data.dueDateLimitDays,
// 				subscriptionCycle: data.subscriptionCycle,
// 				daysAfterDueDateToRegistrationCancellation:
// 					data.daysAfterDueDateToRegistrationCancellation,
// 				notificationEnabled: data.notificationEnabled,
// 				callback: data.callback,
// 				// callback: data.callback,
// 			},
// 		};

// 		const response = await axios.request(options);

// 		return response;
// 	} catch (error) {
// 		console.error("Error getting customers:", error);
// 		throw error;
// 	}
// };

//  ********************* End *****************
