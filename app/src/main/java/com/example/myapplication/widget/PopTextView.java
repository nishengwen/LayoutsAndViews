package com.example.myapplication.widget;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import java.util.Calendar;
import java.util.LinkedList;

import androidx.appcompat.widget.AppCompatTextView;

/**
 处理逻辑
 dataLongShow 是完整的展示的消息
 dataShortShow是立马展示的消息在dataLongShow展示完的情况下取最新的一条。
 */
public class PopTextView extends AppCompatTextView {
    public static final String HI_POP_CLICK = "hiPopClick";
    public static final String ANNOUNCEMENT_CLICK = "announcementClick";
    //这里的数据不能被覆盖，要依次显示。dimiss就显示下一条
    public LinkedList<String> dataLongShow = new LinkedList<String>();
    //这里的数据可以立即被新数据覆盖显示。
    public LinkedList<String> dataShortShow = new LinkedList<String>();
    boolean isFirst = false;
    private CountDownTimer disTimer;

    //判断是不是第一次弹默认公告,一次生命周期只弹一次
//    boolean isFirstPop=false;
    public PopTextView(Context context) {
        super(context);
    }

    public PopTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PopTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private static long getZeroTime(long timeMills) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeMills);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return calendar.getTimeInMillis();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        disTimer = new CountDownTimer(10_000, 10_000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                if (isShowing()) {
                    disView();
                }
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //死循环断开点
                        if (isDataEmpty()) {
                            setVisibility(GONE);
                            return;
                        }
                        //这块一定有数据
                        startLoopText();
                    }
                }, 200);
            }
        };
        setVisibility(GONE);
    }

    public void startLoopText() {
        String nextText = getNextText();
        if (!TextUtils.isEmpty(nextText)) {
            setVisibility(VISIBLE);
            setText(nextText);
            showView();
            disTimer.start();
        }
    }

    public boolean isDataEmpty() {
        return dataLongShow.isEmpty() && dataShortShow.isEmpty();
    }

    public void showFirstPopText(String text) {
//        if(isFirstPop)return;
//        isFirstPop=true;
        if (TextUtils.isEmpty(text)) return;
        if(!isHiPopPastAday())return;
        setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isShowing()) {
                    recordHiPopClickTime();
                    disView();
                    //立即结束一次显示
                    disTimer.onFinish();
                }
            }
        });
        dataLongShow.add(text);
        if (getVisibility()!=View.VISIBLE) {
            startLoopText();
        }
    }

    public void showAnnouncement(String text) {
        if (TextUtils.isEmpty(text)) return;
        setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isShowing()) {
                    disView();
                    //立即结束一次显示
                    disTimer.onFinish();
                }
            }
        });
        //没在展示就启动循环，添加数据
        if (getVisibility()!=View.VISIBLE) {
            dataShortShow.add(text);
            startLoopText();
        }
        //在展示 是第一种直接加入数据即可
        else if (isFirst) {
            dataShortShow.add(text);
        }
        //在展示并且不是第一种 直接替换文字重新计时
        else {
            setText(text);
            disTimer.cancel();
            disTimer.start();
        }
    }

    public String getNextText() {
        if (!dataLongShow.isEmpty()) {
            //取下一条
            isFirst = true;
            return dataLongShow.removeFirst();
        } else if (!dataShortShow.isEmpty()) {
            //取最后一条然后清除数据
            isFirst = false;
            String lastText = dataShortShow.removeLast();
            dataShortShow.clear();
            return lastText;
            //只取最新的一条其他的都放弃
        } else {
            isFirst = false;
            return "";
        }
    }
    public boolean isShowing() {
        return getAlpha() != 0.0f;
    }

    private void showView() {
        if (!isShowing()) {
            alpha2Target(1.0f);
        }
    }

    private void disView() {
        if (isShowing()) {
            alpha2Target(0.0f);
        }
    }

    private void alpha2Target(float targetValue) {
        PropertyValuesHolder propertyValuesHolder = PropertyValuesHolder.ofFloat("alpha", getAlpha(), targetValue);
        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(this, propertyValuesHolder);
        objectAnimator.setDuration(100);
        objectAnimator.start();

    }

    public void recordHiPopClickTime() {
        saveLong(System.currentTimeMillis());
    }

    private long getHiPopClickTime() {
        return getLong();
    }

    public void saveLong(long data) {
        SharedPreferences sp = getContext().getSharedPreferences("PopTextView", Context.MODE_PRIVATE);
        sp.edit().putLong(HI_POP_CLICK, data).apply();
    }

    public long getLong() {
        SharedPreferences sp = getContext().getSharedPreferences("PopTextView", Context.MODE_PRIVATE);
        return sp.getLong(HI_POP_CLICK, -1);
    }

    private boolean isHiPopPastAday() {
        return System.currentTimeMillis() > getZeroTime(getHiPopClickTime());
    }

}