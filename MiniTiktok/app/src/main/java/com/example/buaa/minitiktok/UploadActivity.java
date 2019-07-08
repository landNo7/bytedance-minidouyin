package com.example.buaa.minitiktok;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Constraints;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.example.buaa.minitiktok.bean.Feed;
import com.example.buaa.minitiktok.bean.FeedResponse;
import com.example.buaa.minitiktok.bean.PostVideoResponse;
import com.example.buaa.minitiktok.newtork.IMiniDouyinService;
import com.example.buaa.minitiktok.newtork.RetrofitManager;
import com.example.buaa.minitiktok.utils.ResourceUtils;
import com.example.buaa.minitiktok.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;
import retrofit2.Retrofit;

public class UploadActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private static final int PICK_VIDEO = 2;
    private static final int POST_VIDEO = 0;
    private static final int GET_FEEDS = 1;
    private static final String TAG = "Solution2C2Activity";
    private RecyclerView mRv;
    private List<Feed> mFeeds = new ArrayList<>();
    public Uri mSelectedImage;
    private Uri mSelectedVideo;
    public Button mBtn;
    private Button mBtnRefresh;
    private PostVideoResponse postVideoResponse;
    private VideoView videoView;
    private ImageView imageView;
    private ImageView playIconView;
    private int type;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        initBtns();
    }

    private void initBtns() {
        videoView = findViewById(R.id.video_view);
        imageView = findViewById(R.id.image_view);
        imageView.setVisibility(View.GONE);
        playIconView = findViewById(R.id.play_icon);
        playIconView.setAlpha(0.5f);

        videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(videoView.isPlaying()){
                    videoView.pause();
                    playIconView.setVisibility(View.VISIBLE);
                }else{
                    videoView.start();
                    playIconView.setVisibility(View.GONE);
                }
            }
        });
        mBtn = findViewById(R.id. btn);
        mBtn.setText(R.string.select_a_video);

        type = getIntent().getIntExtra("upload_type",0);
        if(type == Utils.UPLOAD_CAMERA){
            mBtn.setText(R.string.post_it);

            String videoPath = getIntent().getStringExtra("video_path");
            videoView.setVideoPath(videoPath);
            Bitmap bmp = Utils.getVideoThumb(videoPath);
            mSelectedImage = Utils.getImageUri(this,bmp);
            mSelectedVideo = (Uri.fromFile(new File(videoPath)));
            Log.d(TAG, "selectedImage = " + mSelectedImage);
            Log.d(TAG, "mSelectedVideo = " + mSelectedVideo);

            mBtn.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    String s = mBtn.getText().toString();
                    if (getString(R.string.post_it).equals(s)) {
                        if (mSelectedVideo != null && mSelectedImage != null) {
                            postVideo();
                        } else {
                            throw new IllegalArgumentException("error data uri, mSelectedVideo = " + mSelectedVideo + ", mSelectedImage = " + mSelectedImage);
                        }
                    } else if ((getString(R.string.success).equals(s))) {
                        finish();
                    }
                }
            });
        }
        else{
            mBtn.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    String s = mBtn.getText().toString();
                    if (getString(R.string.select_a_video).equals(s)) {
                        chooseVideo();
                    } else if (getString(R.string.select_an_image).equals(s)) {
                        videoView.setVisibility(View.GONE);
                        playIconView.setVisibility(View.GONE);
                        imageView.setVisibility(View.VISIBLE);
                        chooseImage();
                    } else if (getString(R.string.post_it).equals(s)) {
                        if (mSelectedVideo != null && mSelectedImage != null) {
                            postVideo();
                        } else {
                            throw new IllegalArgumentException("error data uri, mSelectedVideo = " + mSelectedVideo + ", mSelectedImage = " + mSelectedImage);
                        }
                    } else if ((getString(R.string.success).equals(s))) {
                        finish();
                    }
                }
            });
            chooseVideo();
        }





    }


    public void chooseImage() {
        // TODO-C2 (4) Start Activity to select an image
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }
    public void chooseVideo() {
        // TODO-C2 (5) Start Activity to select a video
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Video"), PICK_VIDEO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult() called with: requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], data = [" + data + "]");

        if (resultCode == RESULT_OK && null != data) {

            if (requestCode == PICK_IMAGE) {
                mSelectedImage = data.getData();
                Log.d(TAG, "selectedImage = " + mSelectedImage);
                imageView.setImageURI(mSelectedImage);
                mBtn.setText(R.string.post_it);

            } else if (requestCode == PICK_VIDEO) {
                mSelectedVideo = data.getData();
                Log.d(TAG, "mSelectedVideo = " + mSelectedVideo);
                videoView.setVideoURI(mSelectedVideo);
                videoView.start();
                playIconView.setVisibility(View.GONE);
                mBtn.setText(R.string.select_an_image);

            }
        }
    }

    private MultipartBody.Part getMultipartFromUri(String name, Uri uri) {
        // if NullPointerException thrown, try to allow storage permission in system settings
        File f = new File(ResourceUtils.getRealPath(this, uri));
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), f);
        return MultipartBody.Part.createFormData(name, f.getName(), requestFile);
    }

    private void postVideo() {
        mBtn.setText("POSTING...");
        mBtn.setEnabled(false);
        // TODO-C2 (6) Send Request to post a video with its cover image
        // if success, make a text Toast and show
        new Thread(new Runnable() {
            @Override
            public void run() {
                Retrofit retrofit = RetrofitManager.get("http://test.androidcamp.bytedance.com/");
                try {
                    Response<PostVideoResponse> response = retrofit.create(IMiniDouyinService.class).createVideo(
                            "12138",
                            "Bill",
                            getMultipartFromUri("cover_image",mSelectedImage),
                            getMultipartFromUri("video",mSelectedVideo)).
                            execute();
                    postVideoResponse = response.body();
                    mHandler.sendMessage(Message.obtain(mHandler,POST_VIDEO));

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case POST_VIDEO:
                    Log.d(TAG, "handleMessage: "+postVideoResponse.getSuccess());
                    if(postVideoResponse.getSuccess()){
                        Toast.makeText(UploadActivity.this, "上传成功", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(UploadActivity.this, "上传失败"+ postVideoResponse.getError(), Toast.LENGTH_SHORT).show();
                    }
                    mBtn.setText(R.string.success);
                    mBtn.setEnabled(true);
                    finish();
                    break;

            }
        }
    };
}
