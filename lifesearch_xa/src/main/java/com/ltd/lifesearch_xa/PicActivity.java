package com.ltd.lifesearch_xa;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
//import android.support.annotation.Nullable;
//import android.support.annotation.RequiresApi;
//import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Objects;

public class PicActivity extends AppCompatActivity {
    ImageView v;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pic);
        v= findViewById(R.id.data_pic);
        Intent data = getIntent();
        if (data != null) {
            String imageUri = data.getStringExtra("pic");
            System.err.println("url: " + imageUri);
            try {
                BitmapFactory.Options op = new BitmapFactory.Options();
                op.inSampleSize = 2;
                FileInputStream fis = new FileInputStream(imageUri);
                Rect rect = new Rect(0, 0, v.getWidth(), v.getHeight());
                Bitmap bitmap = BitmapFactory.decodeStream(fis, rect, op);  ///把流转化为Bitmap图片
                v.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("data == null");
        }
    }

}
