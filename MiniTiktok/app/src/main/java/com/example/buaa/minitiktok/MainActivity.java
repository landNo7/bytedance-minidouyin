package com.example.buaa.minitiktok;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.buaa.minitiktok.bean.FeedResponse;
import com.example.buaa.minitiktok.newtork.IMiniDouyinService;
import com.example.buaa.minitiktok.utils.NetworkUtils;
import com.example.buaa.minitiktok.utils.RecycleViewAdapter;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.buaa.minitiktok.utils.Utils.hideTitle;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

public class MainActivity extends AppCompatActivity {

    private com.getbase.floatingactionbutton.FloatingActionButton fab_upload;
    private com.getbase.floatingactionbutton.FloatingActionButton fab_camera;
    private FloatingActionsMenu floatingActionsMenu;
    private static final String TAG = "MainActivity";
    private RecyclerView mRv;
    private FeedResponse feedResponse;
    private SwipeRefreshLayout swipeRefreshLayout;
    private static final int REFRESH_COMPLETE = 0;
    private com.example.buaa.minitiktok.monindicator.MonIndicator loading;
    private ImageView face_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hideTitle(getActionBar());

        loading = findViewById(R.id.loading);
        face_image = findViewById(R.id.face_image);

        face_image.postDelayed(new Runnable() {
            @Override
            public void run() {
                face_image.setVisibility(View.GONE);
            }
        },2000);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        feedResponse = new FeedResponse();
        //init RecycleLayout
        mRv = findViewById(R.id.video_list);
        mRv.setLayoutManager(new GridLayoutManager(this,2));
        mRv.setAdapter(new RecycleViewAdapter(feedResponse.getFeeds()));
        //下拉监听
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getVideos();
                Toast.makeText(MainActivity.this,"refresh success",Toast.LENGTH_SHORT).show();
                mHandler.sendEmptyMessageDelayed(REFRESH_COMPLETE,500);
            }
        });
        getVideos();

        ((RecycleViewAdapter) mRv.getAdapter()).setOnItemClickListener(new RecycleViewAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                if(NetworkUtils.isNetConnection(MainActivity.this)){
                    Intent intent = new Intent(MainActivity.this,VedioPlayerActivity.class);
                    intent.putExtra("video_url",feedResponse.getFeeds().get(position).getVideo_url());
                    startActivity(intent);
                }
            }
        });

        getVideos();
        floatingActionsMenu = findViewById(R.id.fab_menu);
        fab_upload = findViewById(R.id.fab_upload);
        fab_camera = findViewById(R.id.fab_camera);
        fab_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(MainActivity.this,"camera",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this,CustomCameraActivity.class);
                startActivity(intent);
            }
        });
        fab_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(MainActivity.this,"upload",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this,UploadActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void getVideos() {
        if(NetworkUtils.isNetConnection(this)) {
            loading.setVisibility(View.VISIBLE);
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://test.androidcamp.bytedance.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            Call<FeedResponse> call = retrofit.create(IMiniDouyinService.class).getFeeds();
            call.enqueue(new Callback<FeedResponse>() {
                @Override
                public void onResponse(Call<FeedResponse> call, Response<FeedResponse> response) {
                    loadVideos(response.body());
                    Log.d(TAG, "onResponse: ");
                }

                @Override
                public void onFailure(Call<FeedResponse> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onFailure: ");
                }
            });
        }
    }

    public void loadVideos(FeedResponse FeedResponse) {
        loading.setVisibility(View.GONE);
        feedResponse = FeedResponse;
        ((RecycleViewAdapter) mRv.getAdapter()).updateFeeds(feedResponse.getFeeds());
        ((RecycleViewAdapter) mRv.getAdapter()).notifyDataSetChanged();
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case REFRESH_COMPLETE:
                    swipeRefreshLayout.setRefreshing(false);
                    break;
            }
        }
    };
}
