package com.support.litework.utils;

import android.content.Context;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

public class FileUtils {
    public static void saveArrayListToFile(Context context, ArrayList<String> stringList, String filename) {
        try {
            // Convert ArrayList to a single string with comma separation
            String data = String.join(",", stringList);

            // Write the data to a file in internal storage
            FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            fos.write(data.getBytes());
            fos.close();
            System.out.println("ArrayList saved to file: " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to read an ArrayList<String> from a file
    public static ArrayList<String> getArrayListFromFile(Context context, String filename) {
        ArrayList<String> stringList = new ArrayList<>();
        try {
            // Open the file and read its content
            FileInputStream fis = context.openFileInput(filename);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            fis.close();

            // Convert the content back to an ArrayList
            String data = sb.toString();
            stringList.addAll(Arrays.asList(data.split(","))); // Split and add to ArrayList

        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringList;
    }
}
