package com.example.buaa.minitiktok.ItemLimit;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.buaa.minitiktok.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

public class Itemlimit extends AppCompatActivity {

    private static int REQUEST_CODE_STORAGE_PERMISSION = 1001;
    private static final String fileName = "test.txt";
    private static String limit = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itemlimit);

        setTitle(R.string.Limit);
        final Button limitBtn = findViewById(R.id.btn_add);
        final TextView limitText = findViewById(R.id.edit_text);
        limitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence content = limitText.getText();
                if (TextUtils.isEmpty(content)) {
                    Toast.makeText(Itemlimit.this,
                            "No content to limit", Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean succeed = Limit_Video(content.toString().trim());
                if (succeed) {
                    Toast.makeText(Itemlimit.this,
                            "Note added", Toast.LENGTH_SHORT).show();
                    limit = ReadFile();
                    setResult(Activity.RESULT_OK);
                } else {
                    Toast.makeText(Itemlimit.this,
                            "Error", Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });

    }

    public String ReadFile() {
        FileInputStream inputStream;
        String read = null;
        try {
            inputStream = this.openFileInput(fileName);
            read = getReadString(inputStream);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return read;
    }

    public boolean WriteFile(String content) {
        try {

            FileOutputStream writefile = this.openFileOutput(fileName, MODE_PRIVATE);//获得FileOutputStream
            byte[]  bytes = content.getBytes();
            writefile.write(bytes);//将byte数组写入文件
            writefile.close();//关闭文件输出流
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getReadString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while((length = inputStream.read(buffer))!=-1){
            outStream.write(buffer,0,length);
        }
        byte[] data = outStream.toByteArray();
        outStream.close();
        inputStream.close();
        return new String(data);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissions.length == 0 || grantResults.length == 0) {
            return;
        }
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            int state = grantResults[0];
            if (state == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(Itemlimit.this, "permission granted",
                        Toast.LENGTH_SHORT).show();
            } else if (state == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(Itemlimit.this, "permission denied",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getInternalPath() {
        Map<String, File> dirMap = new LinkedHashMap<>();
        dirMap.put("cacheDir", getCacheDir());
        dirMap.put("filesDir", getFilesDir());
        dirMap.put("customDir", getDir("custom", MODE_PRIVATE));
        return getCanonicalPath(dirMap);
    }

    private String getExternalPrivatePath() {
        Map<String, File> dirMap = new LinkedHashMap<>();
        dirMap.put("cacheDir", getExternalCacheDir());
        dirMap.put("filesDir", getExternalFilesDir(null));
        dirMap.put("picturesDir", getExternalFilesDir(Environment.DIRECTORY_PICTURES));
        return getCanonicalPath(dirMap);
    }

    private String getExternalPublicPath() {
        Map<String, File> dirMap = new LinkedHashMap<>();
        dirMap.put("rootDir", Environment.getExternalStorageDirectory());
        dirMap.put("picturesDir",
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
        return getCanonicalPath(dirMap);
    }

    private static String getCanonicalPath(Map<String, File> dirMap) {
        StringBuilder sb = new StringBuilder();
        try {
            for (String name : dirMap.keySet()) {
                sb.append(name)
                        .append(": ")
                        .append(dirMap.get(name).getCanonicalPath())
                        .append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public boolean Limit_Video(String limiticontent) {
        return WriteFile(limiticontent);
    }

    public static String getLimitString() {
        return limit;
    }
}
