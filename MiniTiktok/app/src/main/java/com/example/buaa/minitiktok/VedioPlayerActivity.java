package com.example.buaa.minitiktok;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Constraints;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;


import com.example.buaa.minitiktok.Player.RawDataSourceProvider;
import com.example.buaa.minitiktok.utils.Like;
import com.example.buaa.minitiktok.utils.MyClickListener;
import com.example.buaa.minitiktok.utils.Utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.InvalidMarkException;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;


public class VedioPlayerActivity extends AppCompatActivity {

    private SeekBar seekBar;
   // private IjkMediaPlayer ijkMediaPlayer;
    private IjkMediaPlayer ijkMediaPlayer;
    private SurfaceView surfaceView;
    private Like like;
    private Spinner playSpeedSpinner;
    private ImageView playImage;
    private static final int UPDATE_SEEKBAR=1;
    private static final String TAG = VedioPlayerActivity.class.getSimpleName();

    private Long progressContext = 0l;
    private boolean isPlaying = true;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vedio_player);
        Log.d(TAG, "onCreate: ");

        Utils.hideTitle(getActionBar());

        surfaceView = findViewById(R.id.ijkPlayer);
        createPlayer();

        //loadVideo(getIntent().getStringExtra("videoPath"));
        loadVideo(getVideoPath());

        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 1);
        ijkMediaPlayer.setOnVideoSizeChangedListener(new IMediaPlayer.OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int i, int i1, int i2, int i3) {
                changeVideoSize();
            }
        });
        ijkMediaPlayer.setLooping(true);


        playImage = findViewById(R.id.play_icon);
        playImage.setAlpha(0.5f);

        like = findViewById(R.id.like);
        like.setOnClickListener(new MyClickListener(new MyClickListener.MyClickCallBack() {
            @Override
            public void oneClick() {
                Log.d(TAG, "oneClick: ");
                if(ijkMediaPlayer.isPlaying()){
                    ijkMediaPlayer.pause();
                    playImage.setVisibility(View.VISIBLE);
                }else {
                    ijkMediaPlayer.start();
                    playImage.setVisibility(View.GONE);
                }
                //Toast.makeText(VedioPlayerActivity.this,"单击事件",Toast.LENGTH_SHORT).show();
            }
            @Override
            public void doubleClick() {
                //Toast.makeText(VedioPlayerActivity.this,"双击或多击事件",Toast.LENGTH_SHORT).show();
            }
        }));

        seekBar = findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                ijkMediaPlayer.seekTo(seekBar.getProgress());
            }
        });
        updateHandler.sendMessageDelayed(Message.obtain(updateHandler,UPDATE_SEEKBAR),100);

        String[] playSpeedName = {"0.5x","1x","1.5x","2x"};
        final float[] playSpeedValue = {0.5f,1f,1.5f,2f};
        SpinnerAdapter adapter1 = new com.example.buaa.minitiktok.utils.SpinnerAdapter(this,playSpeedName);
        playSpeedSpinner = findViewById(R.id.play_speed_sp);
        playSpeedSpinner.setAdapter(adapter1);
        playSpeedSpinner.setSelection(1);
        playSpeedSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ijkMediaPlayer.setSpeed(playSpeedValue[position]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });




    }




    @Override
    protected void onDestroy() {
        if (ijkMediaPlayer != null) {
            ijkMediaPlayer.stop();
            ijkMediaPlayer.setDisplay(null);
            ijkMediaPlayer.release();
        }
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        progressContext = savedInstanceState.getLong("progress");
        isPlaying = savedInstanceState.getBoolean("isPlaying");
        Log.d(TAG, "onRestoreInstanceState() called with: newpos = [" + progressContext+" ], newstate =[ "+ isPlaying  + "]");
        super.onRestoreInstanceState(savedInstanceState);

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putLong("progress",ijkMediaPlayer.getCurrentPosition());
        outState.putBoolean("isPlaying",ijkMediaPlayer.isPlaying());
        Log.d(TAG, "onSaveInstanceState() called with: oldpos = [" + ijkMediaPlayer.getCurrentPosition() + "], oldstate = [" + ijkMediaPlayer.isPlaying() + "]");
        super.onSaveInstanceState(outState);
    }

    private void createPlayer() {
        if (ijkMediaPlayer != null) {
            ijkMediaPlayer.stop();
            ijkMediaPlayer.setDisplay(null);
            ijkMediaPlayer.release();
        }


        ijkMediaPlayer = new IjkMediaPlayer();
        IjkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_DEBUG);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
        //setSurfaceView
        SurfaceHolder holder = surfaceView.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                ijkMediaPlayer.setDisplay(holder);
                ijkMediaPlayer.prepareAsync();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });

        ijkMediaPlayer.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer iMediaPlayer) {
                ijkMediaPlayer.seekTo(progressContext);
                Log.d(TAG, "onPrepared() called with: PlayerState = [" + iMediaPlayer.isPlaying()+ "] , savedState = ["+isPlaying+"]");
                if(isPlaying){
                    Log.d(TAG, "onPrepared: play");
                    ijkMediaPlayer.start();
                    playImage.setVisibility(View.GONE);
                }else {
                    Log.d(TAG, "onPrepared: stop");
                    ijkMediaPlayer.pause();
                    playImage.setVisibility(View.VISIBLE);
                }
                seekBar.setMax((int)iMediaPlayer.getDuration());
                seekBar.setProgress((int)iMediaPlayer.getCurrentPosition());
            }
        });



    }

    private String getVideoPath() {
        return "https://lf3-hscdn-tos.pstatp.com/obj/developer-baas/baas/tt7217xbo2wz3cem41/d073f49ef494a574_1562587722241.mp4";
    }

    private void loadVideo(int id){
        AssetFileDescriptor fileDescriptor = getResources().openRawResourceFd(id);
        RawDataSourceProvider provider = new RawDataSourceProvider(fileDescriptor);
        ijkMediaPlayer.setDataSource(provider);
    }

    private boolean loadVideo(){
        Uri url = getIntent().getData();
        try {
            if(url!=null){
                ijkMediaPlayer.setDataSource(this,url);
                Log.d(TAG, "loadVideo() called successfully");
                return true;
            }else {
                Log.d(TAG, "loadVideo() failed to call");
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    private boolean loadVideo(String path){
        try {
            ijkMediaPlayer.setDataSource(path);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void changeVideoSize() {
        int videoWidth = ijkMediaPlayer.getVideoWidth();
        int videoHeight = ijkMediaPlayer.getVideoHeight();

        int surfaceWidth = surfaceView.getWidth();
        int surfaceHeight = surfaceView.getHeight();
        Log.d(TAG, "changeVideoSize() called Video Width="+videoWidth+ ",Video Height="+videoHeight);
        Log.d(TAG, "changeVideoSize() called View Width="+surfaceWidth+ ",View Height="+surfaceHeight);

        //根据视频尺寸去计算->视频可以在sufaceView中放大的最大倍数。
        float max;
        max = Math.max((float) videoWidth / (float) surfaceWidth, (float) videoHeight / (float) surfaceHeight);

        //视频宽高分别/最大倍数值 计算出放大后的视频尺寸
        videoWidth = (int) Math.ceil((float) videoWidth / max);
        videoHeight = (int) Math.ceil((float) videoHeight / max);

        //无法直接设置视频尺寸，将计算出的视频尺寸设置到surfaceView 让视频自动填充。

        ConstraintLayout.LayoutParams params = new Constraints.LayoutParams(videoWidth, videoHeight);
        params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        params.horizontalBias = 0.5f;
        params.verticalBias = 0.5f;

        surfaceView.setLayoutParams(params);
        Log.d(TAG, "changeVideoSize() called new width="+videoWidth+ ", new height="+videoHeight);
    }


    @SuppressLint("HandlerLeak")
    Handler updateHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case UPDATE_SEEKBAR:
                    seekBar.setMax((int)ijkMediaPlayer.getDuration());
                    seekBar.setProgress((int)ijkMediaPlayer.getCurrentPosition());
                    sendMessageDelayed(Message.obtain(updateHandler,UPDATE_SEEKBAR),100);
            }
        }
    };
}
