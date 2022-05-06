package com.ltd.lifesearchapp;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;

public class ParamSaver {

    private String path;

    private JSONObject json = null;

    public ParamSaver(String path) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            if (file.isDirectory()) {
                if (!file.mkdirs())
                    throw new IOException("can not create dir: " + path);
                file = new File(file, "param.par");
                path = file.getAbsolutePath();
            } else {
                int index = path.lastIndexOf(File.separator);
                if (index == -1)
                    throw new IllegalArgumentException("invalid path");
                File dir = new File(path.substring(0, index));
                if (!dir.exists() && !dir.mkdirs())
                    throw new IOException(
                            "can not create dir: " + dir.getAbsolutePath() + " for path: " + path);
            }
            if (!file.createNewFile())
                throw new IOException("can not create file: " + path);
        } else {
            String content = readParamsFile(file);
            if (content != null) {
                try {
                    json = new JSONObject(content);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        this.path = path;
    }

    private String readParamsFile(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            long length = file.length();
            if (length == 0)
                return null;
            if (length > (long) (Integer.MAX_VALUE))
                throw new IOException("file is too large");
            byte[] b = new byte[(int) length];
            int offset = 0;
            int nRead, totalRead = 0;
            while (totalRead < (int) length) {
                nRead = fis.read(b, offset, (int) length - totalRead);
                if (nRead < 0)
                    throw new IOException("read params file error");
                totalRead += nRead;
                offset += nRead;
            }
            return new String(b, 0, (int) length, Charset.forName("UTF-8"));
        }
    }

    public Object getParam(String key) {
        if (json != null) {
            try {
                return json.get(key);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void putParam(String name, Object value) {
        if (json == null)
            json = new JSONObject();
        try {
            json.put(name, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        if (path != null && json != null) {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(path);
                fos.write(json.toString().getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
