package com.example.buaa.minitiktok.bean;

import android.util.Log;

import java.util.Date;

/**
 * @author Xavier.S
 * @date 2019.01.20 14:18
 */
public class Feed {

    // TODO-C2 (1) Implement your Feed Bean here according to the response json
    private String student_id;

    private String user_name;

    private String image_url;

    private String video_url;

    private String updatedAt;


    public void setStudent_id(String student_id){
        this.student_id = student_id;
    }

    public String getStudent_id(){
        return this.student_id;
    }

    public void setUser_name(String user_name){
        this.user_name = user_name;
    }

    public String getUser_name(){
        return this.user_name;
    }

    public void setImage_url(String image_url){
        this.image_url = image_url;
    }

    public String getImage_url(){
        return this.image_url;
    }

    public void setVideo_url(String video_url){
        this.video_url = video_url;
    }

    public String getVideo_url(){
        return this.video_url;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedAt() {
        try {
            String[] date;
            date = updatedAt.split("\\.");
            Log.d("feeddate", "getUpdatedAt: "+date[0]);
            return date[0];
        } catch (Exception e) {
            Log.d("feeddate", "getUpdatedAt: catch");
            return updatedAt;
        }
        //return updatedAt;
    }
}
