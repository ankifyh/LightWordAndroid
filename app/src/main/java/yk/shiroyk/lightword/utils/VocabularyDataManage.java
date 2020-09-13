package yk.shiroyk.lightword.utils;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

public class VocabularyDataManage {
    private final String datafile = "vocabluary";
    private Context context;

    public VocabularyDataManage(Context context) {
        this.context = context;
    }

    private File dataDir() {
        File dataDir = new File(context.getFilesDir(), datafile);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        return dataDir;
    }

    public void overWriteFile(String data, String filename) {
        File newFile = new File(dataDir(), filename + ".json");
        try {
            FileOutputStream outputStream = new FileOutputStream(newFile);
            outputStream.write(data.getBytes());
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeFile(String data, String filename) {
        File newFile = new File(dataDir(), filename + ".json");
        if (!newFile.exists()) {
            overWriteFile(data, filename);
        }
    }

    public void deleteFile(String filename) {
        File file = new File(dataDir(), filename + ".json");
        if (file.exists()) {
            file.delete();
        }
    }

    public String readFile(String filename) {
        File file = new File(dataDir(), filename + ".json");
        String line;
        StringBuilder text = new StringBuilder();
        if (file.exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                while ((line = br.readLine()) != null) {
                    text.append(line);
                    text.append('\n');
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return text.toString();
    }

}
