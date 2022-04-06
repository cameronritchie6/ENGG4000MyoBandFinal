package com.cmorrell.myobandcompanionapp;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class DataStorage {

    private static final String LOG_TAG = "DataStorage";
    public static void saveToFile(String data) {
            // Find public storage directory
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            File subfolder = new File(path, "MyoBandOutput");
            subfolder.mkdir();  // make directory if it does not exist
            File outputFile = new File(subfolder, MainActivity.OUTPUT_FILE_NAME);

            // Write to file
            try (FileWriter writer = new FileWriter(outputFile, true)) {
                writer.append(data);
            } catch (FileNotFoundException e) {
                Log.e(LOG_TAG, "Could not find file");
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }
}
