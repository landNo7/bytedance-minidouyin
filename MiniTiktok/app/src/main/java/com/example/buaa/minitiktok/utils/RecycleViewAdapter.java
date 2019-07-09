package com.example.buaa.minitiktok.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.buaa.minitiktok.R;
import com.example.buaa.minitiktok.bean.Feed;

import java.util.List;

public class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.ItemHolder> {

    public interface TextViewClickListener {
        void onClick(int position);
    }

    private TextViewClickListener name_listener;

    public void setTextViewClickListener(TextViewClickListener listener) {
        this.name_listener = listener;
    }

    //第一步 定义接口
    public interface OnItemClickListener {
        void onClick(int position);
    }

    private OnItemClickListener listener;


    //第二步， 写一个公共的方法
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemLongClickListener {
        void onClick(int position);
    }

    private OnItemLongClickListener longClickListener;

    public void setOnItemLongClickListener(OnItemLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    private List<Feed> mItems;

    public RecycleViewAdapter(List<Feed> items) {
        mItems = items;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, final int position) {
        //设置Video封面
        holder.bind(mItems.get(position),position);

    }

    public void updateFeeds(List<Feed> feeds) {
        this.mItems = feeds;
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.video_item, parent,false));
    }

    class ItemHolder extends RecyclerView.ViewHolder {

        private ImageView image;
        private TextView author_name;
        private TextView date;

        ItemHolder(View item) {
            super(item);
            image = item.findViewById(R.id.video_image);
            author_name = item.findViewById(R.id.author_name);
            date = item.findViewById(R.id.text_date);
        }

        public void bind(final Feed feed,final int position) {
            String url = feed.getImage_url();
            Glide.with(image.getContext()).load(url).into(image);
            //设置Video作者
            author_name.setText(feed.getUser_name());
            date.setText(feed.getUpdatedAt());
            //点击封面播放视频
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onClick(position);
                    }
                }
            });

        }
    }

}
