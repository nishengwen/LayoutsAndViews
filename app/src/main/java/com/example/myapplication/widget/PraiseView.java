package com.example.myapplication.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HeartView extends View {
    public HeartView(Context context) {
        super(context);
    }
    public HeartView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initPaint();
        interpolator = new AccelerateDecelerateInterpolator();
        addOtherPic();
    }
    //一个完整动画的时长
    private float mLeafFloatTime = 2500;
    private List<BitmapDes> targetBitmapDes=new ArrayList<>();
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBitmapDes(canvas);
        if(targetBitmapDes.isEmpty())return;
        postInvalidate();
    }

    public void addHeart() {
        //如果绘图循环已死则开启
        if(targetBitmapDes.isEmpty()) {
            postInvalidate();
        }
        targetBitmapDes.add(bitmapDesFactory.generateHeart());
    }

    //动画差值器
    private Interpolator interpolator ;
    public void setInterpolator(Interpolator interpolator){
        if(interpolator==null)return;
        this.interpolator=interpolator;
    }

    private void getNowBitmapPosition(BitmapDes bitmapDes) {
        long intervalTime = System.currentTimeMillis() - bitmapDes.addTime;
        //如果动画播放的间隔0~mLeafFloatTime 就为有效      300是弥补隐藏时间
        if (intervalTime < 0) {
            //没到时间播放
            return;
        } else if (intervalTime > mLeafFloatTime) {
            //已经过时了，移除就好了
            targetBitmapDes.remove(bitmapDes);
            return;
        }
        //时间百分比就是时间因子
        float fraction = (float) intervalTime / mLeafFloatTime;
        //可动态改变大小
        bitmapDes.scale = 1f;
        //时间因子换成路程因子
        fraction = fraction * interpolator.getInterpolation(fraction);
        bitmapDes.y = (int) (mTotalHeight -bitmapDes.scale*bitmapDes.h()*1.5f)*(1-fraction);
        // mTotalWidth / 2f -(bitmapDes.scale * bitmapWidth / 2f) 为原点的marginleft值   加上实际的正玄函数值就是实际位置
        bitmapDes.x = mTotalWidth / 2f -(bitmapDes.scale * bitmapDes.w() / 2f)+ center2centerDistance(bitmapDes);
        //0-0.5  255   //0.5-1     255-0
        if (fraction < 0.8) {
            bitmapDes.alpha=255;
        }else{
            //两点求直线公式
            bitmapDes.alpha= (int) ((fraction-0.8f)/0.2*-255+255);
        }
    }
    private int center2centerDistance(BitmapDes bitmapDes) {
        //w频率   t是周期
        float w = (float) Math.PI / bitmapDes.t;
        //a振幅（图片不变形的最大振幅）
        float a = (mTotalWidth - bitmapDes.w()) / 2f;
        //随机振幅
        a = bitmapDes.randomA * a;
        //余弦公式    y= a*sin(w*x)    w=2pi/t   这里w缩小了一半就是振动慢一半 只是一半的余弦
        return (int) (a * Math.sin(w * (mTotalHeight -bitmapDes.y-bitmapDes.scale * bitmapDes.h()*1.5f)));
    }

    /**
     * 绘制心脏
     *
     * @param canvas
     */
    private void drawBitmapDes(Canvas canvas) {
        for (int i = targetBitmapDes.size() - 1; i >= 0; i--) {
            BitmapDes bitmapDes = targetBitmapDes.get(i);
            getNowBitmapPosition(bitmapDes);
        }

        for (BitmapDes bitmapDes : targetBitmapDes) {
            canvas.save();
            Matrix matrix = new Matrix();
            matrix.setScale(bitmapDes.scale, bitmapDes.scale);
            matrix.postTranslate(bitmapDes.x, bitmapDes.y);
            mBitmapPaint.setAlpha(bitmapDes.alpha);
            canvas.drawBitmap(bitmapDes.bitmap, matrix, mBitmapPaint);
            canvas.restore();
        }
    }

    float mTotalWidth, mTotalHeight;
    private Paint mBitmapPaint;
    private BitmapDesFactory bitmapDesFactory;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mTotalWidth = w;
        mTotalHeight = h;
    }
    private void initPaint() {
        mBitmapPaint = new Paint();
        //抗锯齿
        mBitmapPaint.setAntiAlias(true);
        //防抖动
        mBitmapPaint.setDither(true);
        mBitmapPaint.setFilterBitmap(true);
        bitmapDesFactory = new BitmapDesFactory();
    }

    private Bitmap[] pics;
    public void addOtherPic() {
        pics=new Bitmap[6];
        pics[0]= ((BitmapDrawable) getResources().getDrawable(R.mipmap.gift1_pop)).getBitmap();
        pics[1] = ((BitmapDrawable) getResources().getDrawable(R.mipmap.gift2_pop)).getBitmap();
        pics[2] = ((BitmapDrawable) getResources().getDrawable(R.mipmap.gift3_pop)).getBitmap();
        pics[3] = ((BitmapDrawable) getResources().getDrawable(R.mipmap.gift4_pop)).getBitmap();
        pics[4] = ((BitmapDrawable) getResources().getDrawable(R.mipmap.gift5_pop)).getBitmap();
        pics[5] = ((BitmapDrawable) getResources().getDrawable(R.mipmap.gift6_pop)).getBitmap();
    }

    //被绘制位图的描述文件位置大小形变等等。
    private class BitmapDes {
        // 在绘制部分的位置
        public BitmapDes(){
            addTime=System.currentTimeMillis();
        }
        float x, y;
        int alpha = 255;
        float scale;
        // 图片添加的时间
        long addTime;
        //随机性 控制飘动的幅度 0-1
        float randomA=1;
        float t;
        Bitmap bitmap;

        public float w(){
            return bitmap.getWidth();
        }

        public float h(){
            return bitmap.getHeight();
        }
    }


    private class BitmapDesFactory {
        Random random = new Random();
        // 生成一个心信息
        public BitmapDes generateHeart() {
            BitmapDes bitmapDes = new BitmapDes();
            int rightOrLeft = random.nextInt(2);
            if (rightOrLeft == 0) {
                bitmapDes.randomA = -random.nextFloat();
            } else {
                bitmapDes.randomA = random.nextFloat();
            }
            bitmapDes.bitmap = pics[random.nextInt(5)];
            //0.8倍高度 -1.2倍高度之间。一个周期的路程
            bitmapDes.t = ((0.5f * random.nextFloat()) + 0.7f) * mTotalHeight;
            return bitmapDes;
        }

    }
}
