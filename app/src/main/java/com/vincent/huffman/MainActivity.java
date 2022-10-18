package com.vincent.huffman;

import static com.vincent.huffman.utils.ImagUtil.getImageStreamFromExternal;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.previewlibrary.GPreviewBuilder;
import com.vincent.huffman.bean.UserViewInfo;
import com.vincent.huffman.utils.CachePathUtils;
import com.vincent.huffman.utils.CommonUtils;
import com.vincent.huffman.utils.Constants;
import com.vincent.huffman.utils.ImagUtil;
import com.vincent.huffman.utils.UriParseUtils;

import java.io.File;
import java.io.FileInputStream;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }


    Bitmap inputBitmap = null;

    private String cameraCachePath; // 拍照源文件路径

    private ImageView image1;
    private ImageView image2;
    private TextView tv1, tv2, tv3, tv4, tv5;
    private String mPath1, mPath2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] perms = {Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (checkSelfPermission(perms[0]) == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(perms[1]) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(perms, 200);
            }
        }

        image1 = findViewById(R.id.image1);
        image2 = findViewById(R.id.image2);

        tv1 = findViewById(R.id.tv1);
        tv2 = findViewById(R.id.tv2);
        tv3 = findViewById(R.id.tv3);
        tv4 = findViewById(R.id.tv4);
        tv5 = findViewById(R.id.tv5);

        image1.setOnClickListener(l -> {
            GPreviewBuilder.from(MainActivity.this)
                    .setSingleData(new UserViewInfo(mPath1))
                    .setCurrentIndex(0)
                    .isDisableDrag(true)
                    .setSingleShowType(false)
                    .start();

        });
        image2.setOnClickListener(l -> {
                    GPreviewBuilder.from(MainActivity.this)
                            .setSingleData(new UserViewInfo(mPath2))
                            .setCurrentIndex(0)
                            .isDisableDrag(true)
                            .setSingleShowType(false)
                            .start();
        });

    }

    // 点击拍照
    public void camera(View view) {
        // FileProvider
        Uri outputUri;
        File file = CachePathUtils.getCameraCacheFile();
        ;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            outputUri = UriParseUtils.getCameraOutPutUri(this, file);
        } else {
            outputUri = Uri.fromFile(file);
        }
        cameraCachePath = file.getAbsolutePath();
        // 启动拍照
        CommonUtils.hasCamera(this, CommonUtils.getCameraIntent(outputUri), Constants.CAMERA_CODE);
    }


    // 点击相册
    public void album(View view) {
        CommonUtils.openAlbum(this, Constants.ALBUM_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 拍照返回
        if (requestCode == Constants.CAMERA_CODE && resultCode == RESULT_OK) {
            Bitmap bitmap = ImagUtil.getCompressBitmap(cameraCachePath);
            mPath1 = cameraCachePath;
            image1.setImageBitmap(bitmap);

            getString(cameraCachePath, 1);

            long startTime = System.currentTimeMillis();
            // 压缩
            preCompress(cameraCachePath);

            long stopTime = System.currentTimeMillis();

            tv5.setText("压缩时间：" + (stopTime - startTime) + "ms");

        }

        // 相册返回
        if (requestCode == Constants.ALBUM_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                image1.setImageURI(uri);

                mPath1 = UriParseUtils.getPath(this, uri);
                getString(mPath1, 1);
                long startTime = System.currentTimeMillis();
                // 压缩
                preCompress(mPath1);
                long stopTime = System.currentTimeMillis();

                tv5.setText("压缩时间：" + (stopTime - startTime) + "ms");

            }
        }
    }

    // 准备压缩，封装图片集合
    private void preCompress(String photoPath) {
        createBitmap(photoPath);
    }


    public void getString(String photoPath, int type) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoPath, options);

        //照片长度
        String photoLength = String.valueOf(options.outHeight);

        //照片宽度
        String photoWidth = String.valueOf(options.outWidth);

        if (type == 1)
            tv1.setText("图片像素:" + photoLength + "*" + photoWidth);
        else
            tv3.setText("图片像素:" + photoLength + "*" + photoWidth);
        File f = new File(photoPath);
        FileInputStream fis = null;
        try {

            fis = new FileInputStream(f);
            //照片大小
            float size = fis.available() / 1000;
            String photoSize = size + "KB";
            if (type == 1)
                tv2.setText("图片大小:" + photoSize);
            else
                tv4.setText("图片大小:" + photoSize);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    private void createBitmap(String path) {
        File input = new File(path);
        inputBitmap = BitmapFactory.decodeFile(input.getAbsolutePath());
        if (inputBitmap != null) {
            mPath2 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath() + "/" + System.currentTimeMillis() + "_compress.jpg";
            compressImage(inputBitmap, mPath2, 50);

            image2.setImageURI(getImageStreamFromExternal(mPath2));
            getString(mPath2, 2);
            Toast.makeText(this, "执行完成", Toast.LENGTH_SHORT).show();

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