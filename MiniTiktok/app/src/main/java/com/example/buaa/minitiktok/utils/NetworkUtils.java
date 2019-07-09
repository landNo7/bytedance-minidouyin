package com.example.buaa.minitiktok.utils;


import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * @author Xavier.S
 * @date 2019.01.15 13:27
 */
public class NetworkUtils {

    public static boolean isNetConnection(Context mContext) {
        if (mContext!=null){
            //isNet = false;
            final int[] isConnected = {0};
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // TODO
                    // 在这里进行 http request.网络请求相关操作
                    URL url = null;
                    try {
                        url = new URL("https://www.baidu.com");
                        InputStream stream = url.openStream();
                        Log.d("----http----","successful");
                        isConnected[0] = 1;
                    } catch (MalformedURLException e) {
                        Log.d("----http----","failed");
                        e.printStackTrace();
                        isConnected[0] = 0;
                    } catch (IOException e) {
                        Log.d("----http----","failed");
                        e.printStackTrace();
                        isConnected[0] = 0;
                    }catch (Exception e){
                        Log.d("----http----","failed");
                        e.printStackTrace();
                        isConnected[0] = 0 ;
                    }
                }
            }).start();

            long time = System.currentTimeMillis();
            while(isConnected[0] == 0){
                if(System.currentTimeMillis()-time<1000){
                    continue;
                }else{
                    Toast.makeText(mContext, "网络超时或无连接", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
            return true;
        }else{
            Toast.makeText(mContext, "网络无连接", Toast.LENGTH_SHORT).show();
            return false;
        }
    }


    public static String getResponseWithHttpURLConnection(String url) {
        String result = null;
        InputStream in = null;
        HttpURLConnection urlConnection = null;
        try {
            URL netUrl = new URL(url);
            urlConnection = (HttpURLConnection) netUrl.openConnection();
            urlConnection.setRequestMethod("GET");
            in = new BufferedInputStream(urlConnection.getInputStream());
            result = readStream(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    private static String readStream(final InputStream inputStream) {
        final Scanner scanner = new Scanner(inputStream);
        scanner.useDelimiter("\\A");
        final String data = scanner.next();
        return data;
    }

    private static String readStreamBuffer(InputStream in) {
        StringBuilder result = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String s;
            while ((s = reader.readLine()) != null) {
                result.append(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.toString();
    }
}
