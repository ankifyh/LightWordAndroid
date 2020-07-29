package yk.shiroyk.lightword.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

public class FileUtils {
    private Context context;

    public FileUtils(Context context) {
        this.context = context;
    }

    public Integer countLines(Uri uri) {
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

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
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
