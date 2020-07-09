package com.example.myapplication.ui.home;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import java.util.LinkedList;

/**
 * 结构有点乱但是满足需求
 * 应该整理下
 */
public class PopTextView extends androidx.appcompat.widget.AppCompatTextView {
    public static final String HI_POP_CLICK = "hiPopClick";
    public static final String ANNOUNCEMENT_CLICK = "announcementClick";
    private CountDownTimer loopTimer;
    boolean isFirst=false;
    public LinkedList<String> dataLongShow =new LinkedList<String>();
    public LinkedList<String> dataShortShow =new LinkedList<String>();
    public PopTextView(Context context) {
        super(context);
    }

    public boolean isShowing(){
        return getVisibility()== View.VISIBLE;
    }
    public PopTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PopTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        loopTimer = new CountDownTimer(10_000, 10_000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                if (isShowing()) {
                    dismiss();
                }
                //死循环断开点
                if(dataLongShow.isEmpty()&& dataShortShow.isEmpty()){
                    return;
                }
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        show();
                    }
                },200);
            }
        };
    }

    public void showFirstPopText(String text){
        if(TextUtils.isEmpty(text))return;
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isShowing()) {
                    dismiss();
                    loopTimer.onFinish();
                }
            }
        });
        dataLongShow.add(text);
        if(!isShowing()){
            show();
        }
    }

    public void showAnnouncement(String text){
        if(TextUtils.isEmpty(text))return;
        if(text.equals(getText().toString()))return;
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isShowing()) {
                    dismiss();
                    loopTimer.onFinish();
                }
            }
        });

        //没在展示就启动循环，添加数据
        if(!isShowing()){
            dataShortShow.add(text);
            show();
        }
        //在展示 是第一种直接加入数据即可
        else if(isFirst){
            dataShortShow.add(text);
        }
        //在展示并且不是第一种 直接替换文字重新计时
        else {
            setText(text);
            loopTimer.cancel();
            loopTimer.start();
        }
    }

    public void show(){
        if(!isShowing()){
            PropertyValuesHolder propertyValuesHolder= PropertyValuesHolder.ofFloat("alpha",0.0f,1.0f);
            ObjectAnimator objectAnimator= ObjectAnimator.ofPropertyValuesHolder(this,propertyValuesHolder);
            objectAnimator.setDuration(200);
            objectAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    setVisibility(VISIBLE);
                }
            });
            objectAnimator.start();
        }

        if(!dataLongShow.isEmpty()){
            setText(dataLongShow.removeFirst());
            isFirst=true;
        }else if(!dataShortShow.isEmpty()){
            isFirst=false;
            //只取最新的一条其他的都放弃
            setText(dataShortShow.removeLast());
            dataShortShow.clear();
        }
        loopTimer.start();
    }


    public void dismiss(){
        if(isShowing()){
            PropertyValuesHolder propertyValuesHolder=PropertyValuesHolder.ofFloat("alpha",1.0f,0.0f);
            ObjectAnimator objectAnimator=ObjectAnimator.ofPropertyValuesHolder(this,propertyValuesHolder);
            objectAnimator.setDuration(100);
            objectAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    setVisibility(View.GONE);
                }
            });
            objectAnimator.start();
        }
    }
}
