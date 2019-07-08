package com.example.buaa.minitiktok.utils;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.example.buaa.minitiktok.R;

public class MyAnimation {

    private static final long duration = 300L;
    public static final int PLAY_TO_PAUSE = 0;
    public static final int PAUSE_TO_PLAY = 1;
    public static final int DISPARE = 2;
    public static final int SHOW = 3;

    public static void faded(final Context mContext, final ImageView target, final int video_type,final int animationType){

        int scaleFrom,scaleTo;
        float alphaFrom,alphaTo;

        if(animationType==DISPARE){
            scaleFrom = 1;
            scaleTo = 2;
            alphaFrom = 1.0f;
            alphaTo = 0f;
        }else {
            scaleFrom = 2;
            scaleTo = 1;
            alphaFrom = 0f;
            alphaTo = 1.0f;
        }

        ObjectAnimator animator2X = ObjectAnimator.ofFloat(target,
                "scaleX",scaleFrom,scaleTo);
        animator2X.setDuration(duration);

        ObjectAnimator animator2Y = ObjectAnimator.ofFloat(target,
                "scaleY",scaleFrom,scaleTo);
        animator2Y.setInterpolator(new LinearInterpolator());
        animator2Y.setDuration(duration);

        ObjectAnimator animator3 = ObjectAnimator.ofFloat(target,
                "alpha", alphaFrom,alphaTo);
        animator3.setDuration(duration);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animator2X,animator2Y,animator3);
        animatorSet.start();
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

                if(animationType==DISPARE){
                    if(video_type==PAUSE_TO_PLAY){
                        //pause to play
                        target.setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_record_pause));

                    }else {
                        //play to pause
                        target.setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_record_paly));
                    }
                    faded(mContext,target,video_type,SHOW);
                }

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }
}
