package com.example.camerademo;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.media.Image;
import android.net.Uri;
import android.opengl.Matrix;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.io.DataOutputStream;
import java.net.URL;
import java.io.ByteArrayOutputStream;

import com.example.camerademo.Base64Util;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private TextView wordsIdentity;
    private ImageView imageView;
    private Button btnCamera, btnPhoto, ideButton, audioButton;
    //拍照的照片的存储位置
    private String mTempPhotoPath=null, photoPath=null;
    private static int playinterrupt=0;
    private  static String comwords;
    //照片所在的Uri地址
    private Uri imageUri,audioUri;
    private String cropPicPath = Environment.getExternalStorageDirectory() + File.separator + "photo.jpeg";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.iv);
        btnCamera = findViewById(R.id.btn_camera);
        btnPhoto = findViewById(R.id.btn_photo);
        ideButton = findViewById(R.id.identity_button);
        audioButton = findViewById(R.id.btn_audio);
        requestPermission();
        wordsIdentity = findViewById(R.id.textView);
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photoPath = openCamera();

            }
        });

        btnPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePhoto();
                photoPath = cropPicPath;
            }
        });

        ideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                   Identity(photoPath);

            }
        });

        audioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    playAudio();//语音播放
                } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            }
        });


    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) { //权限还没有授予，需要在这里写申请权限的代码

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }


    private String openCamera() {
        //跳转到系统的拍照界面
        Intent intentToTakePhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //存储为sd卡目录
        //保存temp图
        mTempPhotoPath = Environment.getExternalStorageDirectory() + File.separator + "photo.jpeg";
        imageUri = FileProvider.getUriForFile(MainActivity.this, MainActivity.this.getApplicationContext().getPackageName() + ".fileprovider", new File(mTempPhotoPath));
        //file = new File(mTempPhotoPath);
        intentToTakePhoto.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        intentToTakePhoto.putExtra("return-data", false);
        intentToTakePhoto.putExtra("noFaceDetection", true);
        startActivityForResult(intentToTakePhoto, 3);// CAMERA_OK是用作判断返回结果的标识
        return mTempPhotoPath;
    }

    private String choosePhoto() {
        Intent intentToPickPic = new Intent(Intent.ACTION_PICK, null);
        intentToPickPic.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/jpeg");
        startActivityForResult(intentToPickPic, 4);
        return intentToPickPic.getDataString();

    }

    private void playAudio(){//播放音频

        Audiocomplex Aplay = new Audiocomplex();
        try {
            Aplay.play();
            Aplay.delay(100);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    //当拍摄照片完成时会回调到onActivityResult 在这里处理照片的裁剪
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == MainActivity.RESULT_OK) {
            switch (requestCode) {
                case 3: {
                    // 获得图片
                    try { //该uri就是照片文件夹对应的uri

                        //Bitmap bit = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        Bitmap bit = BitmapFactory.decodeFile(mTempPhotoPath);
                        //android.graphics.Matrix m = new android.graphics.Matrix();
                        //m.setRotate(90,bit.getWidth()/2,bit.getHeight()/2); //旋转图片90°
                        //Bitmap bit2 = Bitmap.createBitmap(bit ,0,0,bit.getWidth(),bit.getHeight(),m,true);
                        // 给相应的ImageView设置图片 未裁剪
                        imageView.setImageBitmap(bit);


                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    break;
                }
                case 4: { // 获取图片剪裁
                    cropPic(data, cropPicPath);
                    break;
                }
                case 5: {
                    ContentResolver resolver = getContentResolver();
                    Uri imgUri = Uri.fromFile(new File(cropPicPath));
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(resolver, imgUri);
                        imageView.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }

            }
        }
        else Toast.makeText(this,"没有图片!",Toast.LENGTH_LONG).show();
        super.onActivityResult(requestCode, resultCode, data);
    }

    private static String getWords(String s) {    //裁剪字符串，提取汉字
        String onlyChi = s.replaceAll("[^a-zA-Z0-9\\u4E00-\u9FA5]", "");
        onlyChi = onlyChi.replaceAll("words", "");
        onlyChi = onlyChi.replaceAll("wordsresult", "");
        onlyChi = onlyChi.replaceAll("result", "");
        //onlyChi.substring(40);
        return onlyChi;
    }


    private void cropPic(Intent data, String cropPicPath) {
        Uri imgUri = Uri.fromFile(new File(cropPicPath));
        int x = imageView.getMeasuredWidth() / 4;
        int y = imageView.getMeasuredHeight() / 4;
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(data.getData(), "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 0.1);//宽高的比例
        intent.putExtra("aspectY", 0.1);
        intent.putExtra("outputX", 4 * x);//剪裁的宽高
        intent.putExtra("outputY", 4 * y);
        intent.putExtra("return-data", false);//通过intent返回数据
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());//输出图片
        intent.putExtra("noFaceDetection", true);//人脸检测
        startActivityForResult(intent, 5);

    }

    public void Identity(String photopath) {

        File file = new File(photopath);
        String imageBase = encodeImgageToBase64(file);
        imageBase = imageBase.replaceAll("\r\n", "");
        imageBase = imageBase.replaceAll("\\+", "%2B");
        final String httpUrl = "https://aip.baidubce.com/rest/2.0/ocr/v1/accurate_basic?access_token=24.244df7d5a6ac8e95fbb24c682f1831f3.2592000.1541057474.282335-14273053";
        final String httpArg = "fromdevice=pc&clientip=10.10.10.0&detecttype=LocateRecognize&languagetype=CHN_ENG&imagetype=1&image=" + imageBase;
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String jsonResult = request(httpUrl, httpArg);
                comwords=getWords(jsonResult.substring(55));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        wordsIdentity.setText(comwords);
                        try {
                            audioPlay();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
            }
        }).start();

    }

    public static String request(String httpUrl, String httpArg) {
        BufferedReader reader = null;
        String result = null;
        StringBuffer sbf = new StringBuffer();

        try {
            URL url = new URL(httpUrl);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            /* 填入apikey到HTTP header*/
            //System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

            connection.setRequestProperty("apikey", "8eX9HnKDVGtOaWSNLxCYuL1h");
            connection.setDoOutput(true);
            connection.getOutputStream().write(httpArg.getBytes("UTF-8"));
            connection.connect();
            InputStream is = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String strRead = null;
            while ((strRead = reader.readLine()) != null) {
                sbf.append(strRead);
                sbf.append("\r\n");
            }
            reader.close();
            result = sbf.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String encodeImgageToBase64(File imageFile) {// 将图片文件转化为字节数组字符串，并对其进行Base64编码处理
        // 其进行Base64编码处理
        byte[] data = null;
// 读取图片字节数组
        try {
            InputStream in = new FileInputStream(imageFile);
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 对字节数组Base64编码
        Base64Util Encoder = new Base64Util();
        return Encoder.encode(data);// 返回Base64编码过的字节数组字符串
    }

    public static void audioPlay() throws Exception{
        Audiocomplex audioplay = new Audiocomplex();
        String str = comwords;
        String random = audioplay.getRandomStringByLength(60);
        audioplay.text2Audio(str, "24.94c40386636b89d2eb29de1abbd8b442.2592000.1541964723.282335-14415500", "1",random);
    }


}


