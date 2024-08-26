import { FilterQuery, UpdateQuery } from "mongoose";
import Models from "../../models";
import { ServerModelType } from "../../utils";
const { Server } = Models;

const getSever = async (
	filter: FilterQuery<ServerModelType> = {}
): Promise<ServerModelType | null> => {
	try {
		const server = await Server.findOne(filter).exec();
		return server;
	} catch (err) {
		console.log(err);
		throw err;
	}
};

const updateServer = async (
	filter: FilterQuery<ServerModelType>,
	updateQuery: UpdateQuery<ServerModelType>
): Promise<ServerModelType | null> => {
	try {
		const server = await Server.findOneAndUpdate(
			filter,
			updateQuery
		).exec();
		return server;
	} catch (err) {
		console.log(err);
		throw err;
	}
};

const updateWhenRegister = async (): Promise<ServerModelType | null> => {
	try {
		const server = await updateServer(
			{},
			{ $inc: { registered_users: 1 } }
		);
		return server;
	} catch (err) {
		console.log(err);
		throw err;
	}
};

const updateWhenSignIn = async (): Promise<ServerModelType | null> => {
	try {
		const server = await updateServer({}, { $inc: { signin_users: 1 } });
		return server;
	} catch (err) {
		console.log(err);
		throw err;
	}
};

const updateWhenSignOut = async (): Promise<ServerModelType | null> => {
	try {
		const server = await updateServer({}, { $inc: { signin_users: -1 } });
		return server;
	} catch (err) {
		console.log(err);
		throw err;
	}
};

const updateWhenDeleteUser = async (): Promise<ServerModelType | null> => {
	try {
		const server = await updateServer(
			{},
			{ $inc: { deleted_users: 1, registered_users: -1 } }
		);
		return server;
	} catch (err) {
		console.log(err);
		throw err;
	}
};

const serverDbModule = {
	updateWhenRegister,
	updateWhenSignIn,
	updateWhenSignOut,
	updateWhenDeleteUser,
	updateServer,
	getSever,
};

export default serverDbModule;
