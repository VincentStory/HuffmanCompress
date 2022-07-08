package com.vincent.huffman;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }


    Bitmap inputBitmap = null;
    private int REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
//        TextView tv = findViewById(R.id.sample_text);
//        tv.setText(stringFromJNI());

        findViewById(R.id.btn_compress).setOnClickListener(v -> {
            if (inputBitmap != null) {
                compressImage(inputBitmap, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath() + "/l_girl.jpg", 50);
                Toast.makeText(this, "执行完成", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "没有加载图片", Toast.LENGTH_SHORT).show();
            }
        });


        requestPermission();

    }

    public void click(View view) {
        createBitmap();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            if (permissions[0].equals(Manifest.permission.DELETE_CACHE_FILES)
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //用户同意使用write
                createBitmap();
            } else {
                //用户不同意，自行处理即可
                Toast.makeText(this, "没有获取到权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                //没有权限则申请权限
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE);

            } else {
                //有权限直接执行,docode()不用做处理

                createBitmap();
            }

        } else {
            //小于6.0，不用申请权限，直接执行
            createBitmap();

        }
    }


    private void createBitmap() {
        File input = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath(), "girl.jpg");
        inputBitmap = BitmapFactory.decodeFile(input.getAbsolutePath());
        if (inputBitmap != null) {

            Toast.makeText(this, "加载完成", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "加载失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    //    public native String stringFromJNI();
    public native void compressImage(Bitmap bitmap, String path, int quality);


}