import dotenv from 'dotenv';
import Models from '../../models';
import userDbModule from './user.module';

import fs from 'fs';
import path from 'path';
import bcrypt from 'bcryptjs';

const { Server, User } = Models;

// Load environment variables from .env file
dotenv.config({
  path: process.env.NODE_ENV === 'production' ? '.env' : '.env.dev',
});

const { DB_HOST, SEED_PATH } = process.env;

/**
 * Initialize the Mongo Database
 */
const startMongoDB = async () => {
  try {
    console.log(DB_HOST);
    console.log('Connecting...');
    await Models.mongoose.connect(`${DB_HOST}`);
    console.log('✔︎ Successfully connected to MongoDB.');

    // seed process
    await initialServerCollection();
    await seedAdminUser();
  } catch (err) {
    console.error(`MongoDB Connection failed. Error: ${err}`);
    process.exit(1);
  }
};

/**
 * Initialize the Server Database if it is empty
 */
const initialServerCollection = async () => {
  // Check if there are any existing server documents
  try {
    const countServer = Server.estimatedDocumentCount();
    const count = await countServer.exec();
    if (count === 0) {
      // Create a new server document
      const newServer = new Server();
      // Save the new server document
      await newServer.save();
      console.log('Initialize Server collection.');
    }
  } catch (err) {
    console.log(err);
  }
};

/**
 * Seed the Admin User
 */
const seedAdminUser = async () => {
  const dataPath = path.resolve(__dirname, `${SEED_PATH}/seed-admin.json`);
  try {
    const data = fs.readFileSync(dataPath, 'utf8');
    const adminData = JSON.parse(data);

    const existingAdmin = await User.findOne({ email: adminData.email });
    if (existingAdmin) {
      return;
    }

    const hashedPassword = await bcrypt.hash(adminData.password, 10);

    const adminUser = new User({
      ...adminData,
      password: hashedPassword,
    });

    await adminUser.save();
    console.log('Admin user created successfully.');
  } catch (err) {
    console.error('Error seeding admin user:', err);
  }
};

export { startMongoDB, userDbModule };
