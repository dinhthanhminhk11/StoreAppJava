/*
 * Copyright 2018 Kaushik N. Sanji
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.kaushiknsanji.storeapp.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v4.os.EnvironmentCompat;

import java.io.File;
import java.io.IOException;


public final class FileStorageUtility {

    private FileStorageUtility() {
        //Suppressing with an error to enforce noninstantiability
        throw new AssertionError("No " + this.getClass().getCanonicalName() + " instances for you!");
    }

    private static boolean createDirectoryIfNotExists(File storageDir) {
        if (!storageDir.exists() && !storageDir.mkdirs()) {
            //Returning FALSE when the directory path could not be created successfully
            return false;
        }
        //Returning TRUE when the directory path exists or was created successfully
        return true;
    }

    @NonNull
    public static File createTempFile(Context context, String authority, String filename, String extension, File[] storageDirs) throws IOException {
        //Stores the chosen directory where the file will be created
        File preferredStorageDir = null;

        //Iterating over the list of directory paths to find the most suitable one
        for (File storageDir : storageDirs) {
            if (EnvironmentCompat.getStorageState(storageDir).equals(Environment.MEDIA_MOUNTED)
                    && checkContentUriPossible(context, authority, filename, extension, storageDir)) {
                //If the directory path is mounted and Content URI generation is possible for a file
                //created in this path, then prefer to use it.
                preferredStorageDir = storageDir;
            }
        }

        if (preferredStorageDir == null || !createDirectoryIfNotExists(preferredStorageDir)) {
            //When the directory could not created or accessed, throw an exception
            throw new IOException(preferredStorageDir + " could not be created or accessed");
        }

        //Returning the Temporary file being created in the chosen directory path
        return File.createTempFile(filename, extension, preferredStorageDir);
    }


    @NonNull
    public static File createFile(Context context, String authority, String filename, String extension, File[] storageDirs) throws IOException {
        //Stores the chosen directory where the file will be created
        File preferredStorageDir = null;

        //Iterating over the list of directory paths to find the most suitable one
        for (File storageDir : storageDirs) {
            if (EnvironmentCompat.getStorageState(storageDir).equals(Environment.MEDIA_MOUNTED)
                    && checkContentUriPossible(context, authority, filename, extension, storageDir)) {
                //If the directory path is mounted and Content URI generation is possible for a file
                //created in this path, then prefer to use it.
                preferredStorageDir = storageDir;
            }
        }

        if (preferredStorageDir == null || !createDirectoryIfNotExists(preferredStorageDir)) {
            //When the directory could not created or accessed, throw an exception
            throw new IOException(preferredStorageDir + " could not be created or accessed");
        }

        //Returning the Temporary file being created in the chosen directory path
        return new File(preferredStorageDir, filename + extension);
    }

    public static boolean isExternalStorageMounted() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }


    public static Uri getContentUriForFile(Context context, String authority, File file) {
        return FileProvider.getUriForFile(context, authority, file);
    }

    private static boolean checkContentUriPossible(Context context, String authority, String filename, String extension, File storageDir) {
        //Creating a File with the directory path, filename and its extension
        File fileToTest = new File(storageDir, filename + extension);
        //Returning the test of Content URI generation for the file created
        return checkContentUriPossible(context, authority, fileToTest);
    }

    private static boolean checkContentUriPossible(Context context, String authority, File fileToTest) {
        //Stores whether the Content URI was generated or not
        boolean uriGenerated;
        try {
            //Trying the generate the Content URI
            getContentUriForFile(context, authority, fileToTest);
            //Marking the Content URI generation as successful
            uriGenerated = true;
        } catch (Exception e) {
            //On Exception, marking the Content URI generation as unsuccessful
            uriGenerated = false;
        }
        //Returning the result of the Content URI generation test
        return uriGenerated;
    }

    @Nullable
    public static File getFileForContentUri(Context context, Uri fileContentUri) {
        //Cursor to store the data retrieved from the Content URI
        Cursor cursor = null;
        try {
            //Querying the Content URI for the "Display Name"
            cursor = context.getContentResolver().query(fileContentUri,
                    null,
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                //When we have the data

                //Retrieving the "Display Name" value
                String displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                //Retrieving the File for the "Display Name"
                File contentUriFile = context.getFileStreamPath(displayName);
                //Returning the File representation of the Content URI if present
                String filePath = Uri.fromFile(contentUriFile).getPath();
                return filePath != null ? new File(filePath) : null;
            }

        } finally {
            //Closing the cursor when done
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        //Returning NULL when the "Display Name" could not be retrieved
        //or the File for the "Display Name" could not be retrieved
        return null;
    }


    public static boolean deleteFile(Uri fileContentUri,
                                     ContentResolver contentResolver, String fileProviderAuthority) {
        //Stores the Number of files deleted successfully
        int deleteResult = 0;
        if (fileContentUri.getAuthority() != null && fileContentUri.getAuthority().contains(fileProviderAuthority)) {
            //Deleting files created only by the app
            deleteResult = contentResolver.delete(fileContentUri, null, null);
        }
        //Returning the result of file deletion
        return (deleteResult == 1);
    }

}
