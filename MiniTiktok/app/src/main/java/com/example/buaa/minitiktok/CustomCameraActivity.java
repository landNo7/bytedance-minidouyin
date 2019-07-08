package com.example.buaa.minitiktok;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.buaa.minitiktok.utils.MyAnimation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static com.example.buaa.minitiktok.utils.Utils.MEDIA_TYPE_IMAGE;
import static com.example.buaa.minitiktok.utils.Utils.MEDIA_TYPE_VIDEO;
import static com.example.buaa.minitiktok.utils.Utils.getOutputMediaFile;


public class CustomCameraActivity extends AppCompatActivity {

    private static final String TAG = CustomCameraActivity.class.getSimpleName();
    private SurfaceView mSurfaceView;
    private Camera mCamera;
    private ImageView record;
    private String vedioFilePath;

    private int CAMERA_TYPE = Camera.CameraInfo.CAMERA_FACING_BACK;

    private boolean isRecording = false;

    private int rotationDegree = 0;
    private static final int REQUEST_PERMISSION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_custom_camera);

        if (!checkPermissionTrue()) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO},REQUEST_PERMISSION);
        }else {
            init();
        }

    }
    private boolean checkPermissionTrue(){
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_PERMISSION:
                if (checkPermissionTrue()) {
                    init();
                }
                break;
        }

    }

    private void init(){

        releaseCameraAndPreview();
        Log.d(TAG, "onCreate: camera number " + Camera.getNumberOfCameras());
        mCamera = getCamera(CAMERA_TYPE);


        mSurfaceView = findViewById(R.id.img);
        //todo 给SurfaceHolder添加Callback
        startPreview(mSurfaceView.getHolder());

        findViewById(R.id.btn_picture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.takePicture(null, null, mPicture);
            }
        });

        record = findViewById(R.id.btn_record);
        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording) {
                    //todo 停止录制
                    stopRecord();


                } else {
                    //todo 录制
                    startRecord();
                }
            }
        });

        findViewById(R.id.btn_facing).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo 切换前后摄像头
                if(CAMERA_TYPE == Camera.CameraInfo.CAMERA_FACING_FRONT){
                    mCamera = getCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
                }else if(CAMERA_TYPE == Camera.CameraInfo.CAMERA_FACING_BACK){
                    mCamera = getCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
                }
                try {
                    mCamera.setPreviewDisplay(mSurfaceView.getHolder());
                    mCamera.startPreview();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        findViewById(R.id.btn_zoom).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.autoFocus(autoFocusCallback);
                if (mCamera.getParameters().isSmoothZoomSupported()) {
                    try {
                        Camera.Parameters params = mCamera.getParameters();
                        final int MAX = params.getMaxZoom();
                        if(MAX!=0) {
                            int zoomValue = params.getZoom();
                            //Trace.Log("-----------------MAX:"+MAX+"   params : "+zoomValue);
                            zoomValue += 5;
                            params.setZoom(zoomValue);
                            mCamera.setParameters(params);
                            //Trace.Log("Is support Zoom " + params.isZoomSupported());
                        }
                    } catch (Exception e) {
                        //Trace.Log("--------exception zoom");
                        e.printStackTrace();
                    }
                }
                else{
                    Toast.makeText(CustomCameraActivity.this, "不支持调焦", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void stopRecord(){
        //MyAnimation.faded(CustomCameraActivity.this,record,MyAnimation.PLAY_TO_PAUSE,MyAnimation.DISPARE);
        isRecording = false;
        releaseMediaRecorder();
        scanDirAsync(CustomCameraActivity.this,new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "CameraDemo"));

    }

    private void startRecord(){
        if( prepareVideoRecorder()){
            isRecording = true;
            try{
                mMediaRecorder.prepare();
                mMediaRecorder.start();
            }catch (Exception e){
                releaseMediaRecorder();
                e.printStackTrace();
            }
           // MyAnimation.faded(CustomCameraActivity.this,record,MyAnimation.PAUSE_TO_PLAY,MyAnimation.DISPARE);
        }
    }

    private final Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            //success表示对焦成功
            if (success){
                Log.i("TAG", "myAutoFocusCallback:success...");
                //myCamera.setOneShotPreviewCallback(null);
            } else {
                //未对焦成功
                Log.i("TAG", "myAutoFocusCallback: 失败了...");
            }
        }
    };


    public static final String ACTION_MEDIA_SCANNER_SCAN_DIR = "android.intent.action.MEDIA_SCANNER_SCAN_DIR";
    public void scanDirAsync(Context ctx, File dir) {
        Log.d(TAG, "scanDirAsync: dir = ["+dir.getAbsolutePath()+"]" );
        if (dir.isDirectory()) {
            File file = new File(vedioFilePath);
            if (file.isFile()) {
                String[] paths = new String[2];
                paths[0] = file.getAbsolutePath();
                Log.d(TAG, "scanDirAsync: paths = " + paths[0]);

                MediaScannerConnection.scanFile(ctx, paths, new String[]{"image/*", "vedio/*"}, new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        Log.d(TAG, "scanDirAsync: finished with path = " + path + ", uri = " + uri.toString());
                    }
                });
                Toast.makeText(ctx, paths[0], Toast.LENGTH_SHORT).show();
            }
            Intent scanIntent = new Intent(ACTION_MEDIA_SCANNER_SCAN_DIR);
            scanIntent.setData(FileProvider.getUriForFile(this,"com.bytedance.camera.demo",dir));
            ctx.sendBroadcast(scanIntent);

            Log.d(TAG, "scanDirAsync() called with: ctx = [" + ctx + "], dir = [" + dir.getAbsolutePath()+ "]");
        }

    }


    public Camera getCamera(int position) {
        CAMERA_TYPE = position;
        if (mCamera != null) {
            releaseCameraAndPreview();
        }
        Camera cam = Camera.open(position);

        //todo 摄像头添加属性，例是否自动对焦，设置旋转方向等

        rotationDegree = getCameraDisplayOrientation(position);
        cam.setDisplayOrientation(rotationDegree);

        return cam;
    }


    private static final int DEGREE_90 = 90;
    private static final int DEGREE_180 = 180;
    private static final int DEGREE_270 = 270;
    private static final int DEGREE_360 = 360;

    private int getCameraDisplayOrientation(int cameraId) {
        Camera.CameraInfo info =
                new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = DEGREE_90;
                break;
            case Surface.ROTATION_180:
                degrees = DEGREE_180;
                break;
            case Surface.ROTATION_270:
                degrees = DEGREE_270;
                break;
            default:
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % DEGREE_360;
            result = (DEGREE_360 - result) % DEGREE_360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + DEGREE_360) % DEGREE_360;
        }
        return result;
    }


    private void releaseCameraAndPreview() {
        //todo 释放camera资源
        if(mCamera!=null){
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }




    Camera.Size size;

    private void startPreview(SurfaceHolder holder) {
        //todo 开始预览
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    mCamera.setPreviewDisplay(holder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mCamera.startPreview();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                releaseCameraAndPreview();
            }
        });
    }


    private MediaRecorder mMediaRecorder;

    private boolean prepareVideoRecorder() {
        //todo 准备MediaRecorder

        mMediaRecorder = new MediaRecorder();
        if(mCamera!=null)
            mCamera.unlock();
        else
            mCamera = getCamera(CAMERA_TYPE);
        mMediaRecorder.setCamera(mCamera);

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        vedioFilePath = getOutputMediaFile(MEDIA_TYPE_VIDEO).toString();
        mMediaRecorder.setOutputFile(vedioFilePath);

        mMediaRecorder.setPreviewDisplay(mSurfaceView.getHolder().getSurface());
        mMediaRecorder.setOrientationHint(rotationDegree);


        return true;
    }


    private void releaseMediaRecorder() {
        //todo 释放MediaRecorder、
        if(mMediaRecorder!=null){
            mMediaRecorder.setOnErrorListener(null);
            mMediaRecorder.setOnInfoListener(null);
            mMediaRecorder.setPreviewDisplay(null);
            try{
                mMediaRecorder.stop();
                mMediaRecorder.reset();
                mMediaRecorder.release();
                mMediaRecorder = null;
                mCamera.lock();
            }catch (IllegalStateException e){
                e.printStackTrace();
            }catch (RuntimeException e){
                e.printStackTrace();
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }


    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null) {
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                Log.d(TAG, "pic data = "+data.toString());
                fos.close();
            } catch (IOException e) {
                Log.d("mPicture", "Error accessing file: " + e.getMessage());
            }
            mCamera.startPreview();
        }
    };




    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = Math.min(w, h);

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

}
