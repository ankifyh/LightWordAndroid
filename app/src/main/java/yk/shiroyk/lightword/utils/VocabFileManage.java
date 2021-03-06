/*
 * Copyright (c) 2020 All right reserved.
 * Created by shiroyk, https://github.com/shiroyk
 */

package yk.shiroyk.lightword.utils;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

public class VocabFileManage {
    private final String datafile = "vocabulary";
    private final Context context;

    public VocabFileManage(Context context) {
        this.context = context;
    }

    private File dataDir(Long VType) {
        File dataDir = new File(context.getFilesDir(),
                datafile + "/" + VType);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        return dataDir;
    }

    public void overWriteFile(String data, Long VType, String filename) {
        File newFile = new File(dataDir(VType), filename + ".json");
        try {
            FileOutputStream outputStream = new FileOutputStream(newFile);
            outputStream.write(data.getBytes());
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeFile(String data, Long VType, String filename) {
        File newFile = new File(dataDir(VType), filename + ".json");
        if (!newFile.exists()) {
            overWriteFile(data, VType, filename);
        }
    }

    public void deleteFile(Long VType, String filename) {
        File file = new File(dataDir(VType), filename + ".json");
        if (file.exists()) {
            file.delete();
        }
    }

    public void copyFile(Long oldVType, Long newVType, String oldName) {
        File oldFile = new File(dataDir(oldVType), oldName + ".json");
        File newFile = new File(dataDir(newVType), oldName + ".json");
        FileUtils.copyFile(oldFile, newFile);
    }

    public String readFile(Long VType, String filename) {
        File file = new File(dataDir(VType), filename + ".json");
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
