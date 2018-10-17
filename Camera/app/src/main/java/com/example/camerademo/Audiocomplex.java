package com.example.camerademo;
import android.media.MediaPlayer;
import android.os.Environment;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class Audiocomplex{
    private static String filePath,mName;
    private MediaPlayer mediaPlayer=new MediaPlayer();

    public static void postVoice(String requestUrl,String params) throws Exception {
        String workspace = Environment.getExternalStorageDirectory().toString();
        String path = workspace+"/text2audio/";
        try {
            if (!(new File(path).isDirectory())) {
                new File(path).mkdir();
            }
            else deleteFile(new File(path));
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        mName="VOICE"+new Date().getTime()/1000+".mp3";
        filePath = path+mName;
        String generalUrl = requestUrl;
        URL url = new URL(generalUrl);
        // 打开和URL之间的连接
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        // 设置通用的请求属性
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setUseCaches(false);
        connection.setDoOutput(true);
        connection.setDoInput(true);

        //得到请求的输出流对象
        DataOutputStream out = new DataOutputStream(connection.getOutputStream());
        out.writeBytes(params);
        out.flush();
        out.close();

        //建立连接
        connection.connect();
        //获取响应头
        Map<String, List<String>> headers = connection.getHeaderFields();
        //遍历所有的响应头字段
        for (String key : headers.keySet()) {
            System.out.println(key + "--->" + headers.get(key));
        }
        java.io.InputStream inputStream = connection.getInputStream();
        FileOutputStream outputStream = new FileOutputStream(filePath);
        byte[] buffer = new byte[1024];
        int len = -1;
        while ((len=inputStream.read(buffer))!=-1) {
            outputStream.write(buffer,0,len);
        }
        outputStream.close();
    }

    public static String getRandomStringByLength(int length) {
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }


    public static void deleteFile(File file) {//删除缓存音频
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                f.delete();
            }
        }
    }


    public static String TEXT2AUDIO_URL = "http://tsn.baidu.com/text2audio";
    /*public static void main(String[] args) throws Exception {
        String tex = "你好朋友，我喜欢你";
        text2Audio(tex, "24.94c40386636b89d2eb29de1abbd8b442.2592000.1541964723.282335-14415500", "1", getRandomStringByLength(60));
    }*/

    /**
     * 必填参数方法
     * @Title text2Audio
     * @param tex	必填	合成的文本，使用UTF-8编码，请注意文本长度必须小于1024字节
     * @param lan	必填	语言选择,填写zh
     * @param tok	必填	开放平台获取到的开发者access_token
     * @param ctp	必填	客户端类型选择，web端填写1
     * @param cuid	必填	用户唯一标识，用来区分用户，填写机器 MAC 地址或 IMEI 码，长度为60以内
     * @author 小帅丶
     * @throws Exception
     * @date 2017-5-26
     */
    @SuppressWarnings("static-access")
    public static void text2Audio(String tex,String tok,String ctp,String cuid) throws Exception{
        final String params = "tex=" + URLEncoder.encode(tex, "UTF-8")
                + "&lan=zh&cuid=" + cuid + "&ctp=1&tok=" + tok;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    postVoice(TEXT2AUDIO_URL,params);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public void play() throws Exception{

            mediaPlayer.reset();//从新设置要播放的音乐
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();//预加载音频
            mediaPlayer.start();//播放音乐

    }

    public void del(){
        File f = new File(filePath);
        f.delete();
    }


    public void delay(int ms) throws Exception{//延时函数
        try {
            Thread.currentThread();
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
