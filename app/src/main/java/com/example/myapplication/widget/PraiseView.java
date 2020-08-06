package com.example.myapplication.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BaseInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.WeakHashMap;

public class PraiseView extends View {
    float mTotalWidth, mTotalHeight;
    CountDownTimer countDownTimer;
    //一个完整动画的时长
    private long mAnimTime = 1500;
    private List<BitmapDes> targetBitmapDes = new ArrayList<>();
    //动画差值器
    private Paint mBitmapPaint;
    private BitmapDesFactory bitmapDesFactory;
    private WeakHashMap<String, Bitmap> drawablesMap = new WeakHashMap<>();
    private List<BaseInterpolator> interpolators;
    private int[] ids;

    public PraiseView(Context context) {
        super(context);
    }

    public PraiseView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 根据  控制点 和t 取某一点
     * bezier formula
     *
     * @param allPoints 控制点 包括起点终点
     * @param t         t[0-1]
     * @return
     */
    public static PointF calculateBezier(float t, PointF... allPoints) {
        if (allPoints.length == 1) {
            //最少两个点起点终点；
            return allPoints[0];
        }
        int n = allPoints.length - 1;
        int[] prefix = calculatePrefix(n + 1);
        PointF pointF = new PointF();
        for (int i = 0; i <= n; i++) {
            pointF.x += prefix[i] * allPoints[i].x * Math.pow(1 - t, n - i) * Math.pow(t, i);
            pointF.y += prefix[i] * allPoints[i].y * Math.pow(1 - t, n - i) * Math.pow(t, i);
        }
        return pointF;
    }

    /**
     * 计算二项式系数
     * 杨辉三角
     * n为几阶
     *
     * @param number 项数 为n+1
     * @return
     */
    public static int[] calculatePrefix(int number) {
        int[] result = new int[number];
        for (int n = 0; n <= number - 1; n++) {
            result[0] = 1;
            //确定 1~n-1的值
            int[] temp = Arrays.copyOf(result, n);
            for (int i = 1; i <= n - 1; i++) {
                result[i] = temp[i - 1] + temp[i];
            }
            result[n] = 1;
        }
        return result;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initPaint();
        interpolators = new ArrayList<>();
        interpolators.add(new AccelerateDecelerateInterpolator());
        interpolators.add(new DecelerateInterpolator());
        interpolators.add(new LinearInterpolator());
        interpolators.add(new AccelerateInterpolator());
    }

    private int[] getIdsFormArrXml(int arrId) {
        TypedArray ar = getResources().obtainTypedArray(arrId);
        int[] resIds = new int[ar.length()];
        for (int i = 0; i < ar.length(); i++) {
            resIds[i] = ar.getResourceId(i, 0);
        }
        ar.recycle();
        return resIds;
    }

    public void init(int xmlStringArrId) {
        ids = getIdsFormArrXml(xmlStringArrId);
    }

    public void init(int[] drawableIds) {
        ids = drawableIds;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBitmapDes(canvas);
        if (targetBitmapDes.isEmpty()) return;
        postInvalidate();
    }

    private void addPraise() {
        //如果绘图循环已死则开启
        if (ids == null || ids.length == 0) {
            throw new RuntimeException("you should  call init");
        }
        if (targetBitmapDes.isEmpty()) {
            postInvalidate();
        }
        targetBitmapDes.add(bitmapDesFactory.generateBitmapDes(mAnimTime));
    }

    private void getNowBitmapPosition(BitmapDes bitmapDes) {
        if (bitmapDes.isExpired()) {
            //已经过时了，移除就好了，next
            targetBitmapDes.remove(bitmapDes);
            return;
        }
        //获取路程百分比
        float fraction = bitmapDes.getPathPercent();

        float scale = (mTotalWidth * 0.33f) / bitmapDes.getMinWH();
        //计算缩放
        if (fraction < 0.10) {
            bitmapDes.scale = (7 * fraction + 0.3f) * scale;
        } else {
            bitmapDes.scale = scale;
        }
        //计算位置
        calculatePositionByPercent(bitmapDes, fraction);
//        PointF pointF=calculateBezier()
        //计算透明度
        if (fraction < 0.8) {
            bitmapDes.alpha = 255;
        } else {
            //两点求直线公式
            bitmapDes.alpha = (int) ((fraction - 0.8f) / 0.2 * -255 + 255);
        }
    }

    public void calculatePositionByPercent(BitmapDes bitmapDes, float fraction) {
        // mTotalWidth / 2f -(bitmapDes.scale * bitmapWidth / 2f) 为原点的marginleft值   加上实际的正玄函数值就是实际位置
        center2centerXDistance(bitmapDes, fraction);
        //坐标系转换
        coordinateTransViaVector(bitmapDes);
    }

    /**
     * 坐标系转换
     *
     * @param bitmapDes
     */
    public void coordinateTransViaVector(BitmapDes bitmapDes) {
        PointF drawPosition = new PointF();
        drawPosition.x = -(bitmapDes.y -((mTotalWidth - bitmapDes.getScaleW()) / 2));
        drawPosition.y = -(bitmapDes.x -(mTotalHeight - bitmapDes.getScaleH()));
        bitmapDes.x = drawPosition.x;
        bitmapDes.y = drawPosition.y;
    }

    private void center2centerXDistance(BitmapDes bitmapDes, float fraction) {
        bitmapDes.x = fraction * (mTotalHeight - bitmapDes.getScaleH());
        //w频率   t是周期
        float w = (float) Math.PI / bitmapDes.T;
        //a振幅（最大振幅）
        float a = (mTotalWidth - bitmapDes.getScaleW()) / 2f;
        //随机振幅
        a = bitmapDes.randomA * a;
        //余弦公式    y= a*sin(w*x)    w=2pi/t   这里w缩小了一半就是振动慢一半 只是一半的余弦
        bitmapDes.y = (float) (a * Math.sin(w * (bitmapDes.x)));
    }

    private void center2centerXDistance1(BitmapDes bitmapDes, float fraction) {
        bitmapDes.x = fraction * (mTotalHeight - bitmapDes.getScaleH());
        PointF start = new PointF(0, 0);
        PointF end = new PointF();
        end.x =  mTotalHeight-bitmapDes.getScaleH();
        end.y = bitmapDes.randomA*((mTotalWidth - bitmapDes.getScaleW()) / 2f);
        PointF control1 = new PointF(end.x*0.3f, -end.y );
        PointF control2 = new PointF(end.x*0.6f, end.y );
        PointF result = calculateBezier(fraction, start,control1,control2, end);
        bitmapDes.x = result.x;
        bitmapDes.y = result.y;
    }


    private void drawBitmapDes(Canvas canvas) {
        for (int i = targetBitmapDes.size() - 1; i >= 0; i--) {
            BitmapDes bitmapDes = targetBitmapDes.get(i);
            getNowBitmapPosition(bitmapDes);
        }

        for (BitmapDes bitmapDes : targetBitmapDes) {
            if (bitmapDes.bitmap == null) {
                //防止被回收
                continue;
            }
            canvas.save();
            Matrix matrix = new Matrix();
            matrix.setScale(bitmapDes.scale, bitmapDes.scale);
            matrix.postTranslate(bitmapDes.x, bitmapDes.y);
            mBitmapPaint.setAlpha(bitmapDes.alpha);
            canvas.drawBitmap(bitmapDes.bitmap, matrix, mBitmapPaint);
            canvas.restore();
        }
    }

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

    public void addPraise(int times) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(times * 6 * 200, 200) {
            @Override
            public void onTick(long millisUntilFinished) {
                addPraise();
            }

            @Override
            public void onFinish() {
            }
        };
        countDownTimer.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

    }

    public void startLoopPraise() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(5 * 200, 200) {
            @Override
            public void onTick(long millisUntilFinished) {
                addPraise();
            }

            @Override
            public void onFinish() {
                countDownTimer.start();
            }
        };
        countDownTimer.start();
    }

    //被绘制位图的描述文件位置大小形变等等，还包括位图。
    private class BitmapDes {
        float x, y;
        int alpha = 255;
        float scale;
        // 图片添加的时间
        long addTime;
        //随机性 控制飘动的幅度 0-1
        float randomA = 1;
        float T;
        Bitmap bitmap;
        int index;
        BaseInterpolator interpolator;
        private long livingTime;

        // 在绘制部分的位置
        public BitmapDes(long livingTime) {
            addTime = System.currentTimeMillis();
            this.livingTime = livingTime;
        }

        private long getIntervalTime() {
            return System.currentTimeMillis() - addTime;
        }

        public boolean isExpired() {
            return getIntervalTime() > livingTime;
        }

        public float getTimePercent() {
            return getIntervalTime() * 1.0f / livingTime;
        }

        public float getPathPercent() {
            return interpolator.getInterpolation(getTimePercent());
        }

        public float w() {
            return bitmap.getWidth();
        }

        public float h() {
            return bitmap.getHeight();
        }

        public float getScaleW() {
            return bitmap.getWidth() * scale;
        }

        public float getScaleH() {
            return bitmap.getHeight() * scale;
        }

        public float getMinWH() {
            return Math.min(bitmap.getWidth(), bitmap.getHeight());
        }
    }

    private class BitmapDesFactory {
        Random random = new Random();

        // 生成一个心信息
        public BitmapDes generateBitmapDes(long livingTime) {
            BitmapDes bitmapDes = new BitmapDes(livingTime);
            int rightOrLeft = random.nextInt(2);
            if (rightOrLeft == 0) {
                bitmapDes.randomA = -random.nextFloat();
            } else {
                bitmapDes.randomA = random.nextFloat();
            }
            bitmapDes.interpolator = interpolators.get(random.nextInt(interpolators.size()));
            int targetIndex = random.nextInt(ids.length);
            Bitmap bitmap = drawablesMap.get(targetIndex + "");
            if (bitmap == null) {
                bitmapDes.bitmap = ((BitmapDrawable) getResources().getDrawable(ids[targetIndex])).getBitmap();
                drawablesMap.put(targetIndex + "", bitmapDes.bitmap);
            } else {
                bitmapDes.bitmap = bitmap;
            }
            bitmapDes.index = targetIndex;
            // 一个周期的路程 决定运动的频率  0.7倍高度 -1.2倍高度之间。
            bitmapDes.T = ((0.5f * random.nextFloat()) + 0.7f) * mTotalHeight;
            return bitmapDes;
        }
    }
}
