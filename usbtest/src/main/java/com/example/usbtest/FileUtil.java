package com.example.usbtest;


import android.os.Environment;

import com.github.mjdev.libaums.fs.UsbFile;
import com.github.mjdev.libaums.fs.UsbFileOutputStream;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.os.Environment.getExternalStorageDirectory;

public final class FileUtil {
    public static final String DEFAULT_BIN_DIR = "usb";

    public static boolean checkSDcard() {
        return Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState());
    }

    static File getSaveFile(String folderPath, String fileName) {
        File file = new File(getSavePath(folderPath) + File.separator
                + fileName);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }


    public static String getSavePath(String folderName) {
        return getSaveFolder(folderName).getAbsolutePath();
    }


    public static File getSaveFolder(String folderName) {
        File file = new File(getExternalStorageDirectory()
                .getAbsoluteFile()
                + File.separator
                + folderName
                + File.separator);
        file.mkdirs();
        return file;
    }


    public static void closeIO(Closeable... closeables) {
        if (null == closeables || closeables.length <= 0) {
            return;
        }
        for (Closeable cb : closeables) {
            try {
                if (null == cb) {
                    continue;
                }
                cb.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void redFileStream(OutputStream os, InputStream is) throws IOException {
        int bytesRead = 0;
        byte[] buffer = new byte[1024 * 8];
        while ((bytesRead = is.read(buffer)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
        os.flush();
        os.close();
        is.close();
    }


    public static void saveSDFile2OTG(final File f, final UsbFile usbFile) {
        UsbFile uFile = null;
        FileInputStream fis = null;
        StringBuffer newFileName = null;
        try {//开始写入
            fis = new FileInputStream(f);//读取选择的文件的
            if (usbFile.isDirectory()) {
                //如果选择是个文件夹
//                UsbFile[] usbFiles = usbFile.listFiles();
//                if (usbFiles != null && usbFiles.length > 0) {
//                    for (UsbFile file : usbFiles) {
//                        if (file.getName().equals(f.getName())) {
////                            file.delete();
                            StringBuffer fileName = new StringBuffer(f.getName());
                            int index = fileName.indexOf(".");
                            newFileName = fileName.insert(index, getDate());
//                            for (UsbFile file1 : usbFiles) {
//                                if (file1.getName().equals(newFileName.toString())) {
//                                    file1.delete();
//                                }
//                            }
////                            String fileName = f.getName().split(".")
//                            uFile = usbFile.createFile(newFileName.toString());
//                        }
//                    }
//                } else {
//                String[] fileName = f.getName().split(".");
//                String str = fileName[0]+getDate()+fileName[1];
                uFile = usbFile.createFile(newFileName.toString());
//                }
                UsbFileOutputStream uos = new UsbFileOutputStream(uFile);
                try {
                    redFileStream(uos, fis);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private static SimpleDateFormat df;
    private static String getDate() {
        df = new SimpleDateFormat("yyyyMMddHHmmss");
        return "_"+df.format(new Date());
    }
}