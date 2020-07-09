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
import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.LinkedList;
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
        targetBitmapDes.add(heartFactory.generateHeart());
    }

    AccelerateDecelerateInterpolator accelerateDecelerateInterpolator = new AccelerateDecelerateInterpolator();
    private void getNowHeart(BitmapDes bitmapDes, long currentTime) {
        long intervalTime = currentTime - bitmapDes.startTime;
        if (intervalTime < 0) {
            return;
        } else if (intervalTime > mLeafFloatTime - 300) {
            targetBitmapDes.remove(bitmapDes);
            return;
        }
        float fraction = (float) intervalTime / mLeafFloatTime;
        //0～1   对应  0.2~0.5
        bitmapDes.scale = Math.min(2 * Math.max(fraction, 0.2f), 1);
        fraction = fraction * accelerateDecelerateInterpolator.getInterpolation(fraction);
        bitmapDes.x = getLocationX(bitmapDes) + mTotalWidth / 2f - (bitmapDes.scale * bitmapWidth / 2f);
        bitmapDes.y = (int) (mTotalHeight - bitmapDes.t * fraction);
        bitmapDes.alpha = Math.max((int) (-600 * Math.max(fraction, 0.5) + 555), 0);
    }

    // 通过叶子信息获取当前叶子的X值
    private int getLocationX(BitmapDes bitmapDes) {
        float w = (float) Math.PI / bitmapDes.t;
        float a = (mTotalWidth - bitmapWidth) / 2f;
        a = bitmapDes.randomA * a;
        return (int) (a * Math.sin(w * (mTotalHeight - bitmapDes.y)));
    }

    /**
     * 绘制心脏
     *
     * @param canvas
     */
    private void drawBitmapDes(Canvas canvas) {
        long currentTime = System.currentTimeMillis();
        for (int i = targetBitmapDes.size() - 1; i >= 0; i--) {
            BitmapDes bitmapDes = targetBitmapDes.get(i);
            getNowHeart(bitmapDes, currentTime);
        }

        for (BitmapDes bitmapDes : targetBitmapDes) {
            canvas.save();
            Matrix matrix = new Matrix();
            matrix.setScale(bitmapDes.scale, bitmapDes.scale);
            matrix.postTranslate(bitmapDes.x, bitmapDes.y);
            mBitmapPaint.setAlpha(bitmapDes.alpha);

            if (bitmapDes.otherPicType==-1&& bitmapDes.heartType == HeartType.RED) {
                canvas.drawBitmap(mRedBitmap, matrix, mBitmapPaint);
            } else if(bitmapDes.otherPicType==-1&& bitmapDes.heartType == HeartType.GRAY) {
                canvas.drawBitmap(mGrayBitmap, matrix, mBitmapPaint);
            }else if(pics!=null){
                canvas.drawBitmap(pics[bitmapDes.otherPicType], matrix, mBitmapPaint);
            }

            canvas.restore();
        }
    }

    float mTotalWidth, mTotalHeight;
    private Bitmap mRedBitmap, mGrayBitmap;
    private Paint mBitmapPaint;

    HeartFactory heartFactory;
    private Bitmap gift1, gift2, gift3, gift4, gift5, gift6;
    private int bitmapWidth, bitmapHeight;

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
        heartFactory = new HeartFactory();
        targetBitmapDes = heartFactory.generateHearts();

        mRedBitmap = ((BitmapDrawable) getResources().getDrawable(R.mipmap.heart_red)).getBitmap();
        mGrayBitmap = ((BitmapDrawable) getResources().getDrawable(R.mipmap.heart_gray)).getBitmap();
        bitmapWidth = mRedBitmap.getWidth();
        bitmapHeight = mRedBitmap.getHeight();
    }

    private boolean isAddOtherPic = false;
    private Bitmap[] pics;
    public void addOtherPic() {
        isAddOtherPic = true;
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
        float x, y;
        int alpha = 255;
        float scale;
        // 控制心脏飘动的幅度
        //随机性
        float randomA;

        // 起始时间(ms)
        long startTime;
        HeartView.HeartType heartType;
        float t;
        int otherPicType=-1;
    }

    enum HeartType {
        GRAY(0), RED(1);
        int type;

        HeartType(int type) {
            this.type = type;
        }
    }

    private class HeartFactory {
        private static final int MAX_LEAFS = 0;
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
            int hearType = random.nextInt(2);
            if (hearType == 0) {
                bitmapDes.heartType = HeartType.GRAY;
            } else {
                bitmapDes.heartType = HeartType.RED;
            }
            if (isAddOtherPic&&random.nextInt(3) == 0) {
                bitmapDes.otherPicType = random.nextInt(5);
            }
            bitmapDes.t = ((0.4f * random.nextFloat()) + 0.8f) * mTotalHeight;
            bitmapDes.startTime = System.currentTimeMillis();
            return bitmapDes;
        }

        public List<BitmapDes> generateHearts() {
            return generateLeafs(MAX_LEAFS);
        }
        public List<BitmapDes> generateLeafs(int leafSize) {
            List<BitmapDes> leafs = new LinkedList<BitmapDes>();
            for (int i = 0; i < leafSize; i++) {
                leafs.add(generateHeart());
            }
            return leafs;
        }
    }
}
