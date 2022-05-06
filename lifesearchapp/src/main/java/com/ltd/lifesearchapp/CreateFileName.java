package com.ltd.lifesearchapp;

import android.os.Environment;

import java.io.File;

public class CreateFileName {

    public CreateFileName(){

        createFileName();
    }

    private String mResultFileName ="/DetectResults" ;
    private int mNowFileindex = 0;          //ÎÄ¼þË÷Òý
    public void setFileName(String fileName){
        this.mResultFileName= fileName;
    }
    public String getFileName(){
        return mResultFileName;
    }
    public void createFileName(){

        setFileName(creatNewFiles_Index());
    }
    public  String creatNewFiles_Index(){

        String fileName;
        int index = 1;
        do {
            fileName = "/DetectResult" + index + ".txt";
            fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + mResultFileName +
                    fileName;
            File file = new File(fileName);
            if (!file.exists())
                break;
            index++;
        } while (true);
        //
        mNowFileindex = index;
        return fileName;
    }
}
