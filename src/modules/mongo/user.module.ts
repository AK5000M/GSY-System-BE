import { FilterQuery, UpdateQuery } from "mongoose";
import bcrypt from "bcryptjs";
import { UserModelType } from "../../utils";
import Models from "../../models";
import serverDbModule from "./server.module";

const { User } = Models;

const getUser = (
	filter: FilterQuery<UserModelType>
): Promise<UserModelType | null> => {
	return new Promise((resolve, reject) => {
		User.findOne(filter)
			.then((user) => {
				if (user) {
					user.id = user._id;
				}
				resolve(user);
			})
			.catch((err) => {
				reject(err);
			});
	});
};

// Create a new User
const createUser = (user: UserModelType): Promise<UserModelType> => {
	console.log({ user });
	const newUser = new User({
		...user,
		password: user.password ? bcrypt.hashSync(user.password, 8) : undefined,
	});

	return new Promise((resolve, reject) => {
		newUser
			.save()
			.then((user) => {
				serverDbModule.updateWhenRegister();
				resolve(user);
			})
			.catch((err) => {
				console.log(err);
				reject(err);
			});
	});
};

const userDbModule = {
	getUser,
	createUser,
};

export default userDbModule;
