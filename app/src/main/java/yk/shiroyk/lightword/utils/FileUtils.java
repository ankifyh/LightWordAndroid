/*
 * Copyright (c) 2020 All right reserved.
 * Created by shiroyk, https://github.com/shiroyk
 */

package yk.shiroyk.lightword.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.channels.FileChannel;

public class FileUtils {
    private static final String TAG = FileUtils.class.getSimpleName();

    public static Integer countLines(Context context, Uri uri) {
        int lines = 0;
        try {
            InputStream is = context.getContentResolver().openInputStream(uri);
            LineNumberReader lineNumberReader = new LineNumberReader(new InputStreamReader(is));
            lineNumberReader.skip(Long.MAX_VALUE);
            lines = lineNumberReader.getLineNumber();
            lineNumberReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines + 1;
    }

    public static void copyFile(File sourceFile, File destFile) {
        try {
            if (!destFile.exists()) {
                destFile.createNewFile();
            }

            FileChannel source = new FileInputStream(sourceFile).getChannel();
            FileChannel dest = new FileOutputStream(destFile).getChannel();
            dest.transferFrom(source, 0, source.size());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static String getFileName(ContentResolver resolver, Uri uri) {
        String result = null;
        Log.d(TAG, "Uri - " + uri);
        if (uri.getScheme().equals("content")) {
            Cursor cursor = resolver.query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result.split("\\.(?=[^\\.]+$)")[0];
    }
}
