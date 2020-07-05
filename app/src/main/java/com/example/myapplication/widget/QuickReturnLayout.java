package com.example.myapplication.widget;

import android.content.Context;
import android.content.res.TypedArray;

import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.example.myapplication.R;

import androidx.annotation.Nullable;
import androidx.core.view.NestedScrollingChild;
import androidx.core.view.NestedScrollingChildHelper;
import androidx.core.view.NestedScrollingParent;
import androidx.core.view.NestedScrollingParentHelper;
import androidx.core.view.ViewCompat;


public class QuickReturnLayout extends LinearLayout implements NestedScrollingParent, NestedScrollingChild {
    private static final String TAG = "QuickReturnLayout";
    private static final int INVALID_ID = -1;
    private final NestedScrollingChildHelper mNestedScrollingChildHelper;
    private final NestedScrollingParentHelper mNestedScrollingParentHelper;
    private View mHeaderView;
    private final int mHeaderViewId;
    private View mScrollableView;
    private final int mScrollableViewId,visibleViewId;
    @Nullable
    private ViewOffsetHelper mHeaderViewOffsetHelper;
    private ViewOffsetHelper mScrollViewOffsetHelper;
    private int mHeaderViewHeight;
    private static final int MINIMUM_FLING_VELOCITY = 2000;
    private View visibleView;
    public QuickReturnLayout(Context context) {
        this(context, null);
    }

    public QuickReturnLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QuickReturnLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typeArray = context.obtainStyledAttributes(attrs,
                R.styleable.QuickReturnLayout);
        mHeaderViewId = typeArray.getResourceId(R.styleable.QuickReturnLayout_quickReturnHeaderChild, INVALID_ID);
        mScrollableViewId = typeArray.getResourceId(R.styleable.QuickReturnLayout_quickReturnScrollableChild, INVALID_ID);
        visibleViewId=typeArray.getResourceId(R.styleable.QuickReturnLayout_quickReturnHeaderChildVisible,INVALID_ID);

        setOrientation(VERTICAL);
        mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        mNestedScrollingChildHelper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);
    }


    protected void onFinishInflate() {
        super.onFinishInflate();
        if(visibleViewId!=INVALID_ID){
            visibleView= findViewById(visibleViewId);
        }

        if (mHeaderViewId != INVALID_ID) {
            mHeaderView = findViewById(mHeaderViewId);
            mHeaderView.addOnLayoutChangeListener(new OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if(visibleView!=null) {
                        mHeaderViewHeight = mHeaderView.getHeight() - visibleView.getHeight();
                    }else{
                        mHeaderViewHeight = mHeaderView.getHeight() ;
                    }
                    adjustScrollableViewHeight();
                    post(new Runnable() {
                        @Override
                        public void run() {
                            scroll(getHeaderTopAndBottomOffset());
                        }
                    });
                }
            });
            mHeaderViewOffsetHelper = new ViewOffsetHelper(mHeaderView);
        }

        if (mScrollableViewId != INVALID_ID) {
            mScrollableView = findViewById(mScrollableViewId);
            mScrollViewOffsetHelper = new ViewOffsetHelper(mScrollableView);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mHeaderViewOffsetHelper != null) {
            mHeaderViewOffsetHelper.onViewLayout();
        }
        if (mScrollViewOffsetHelper != null) {
            mScrollViewOffsetHelper.onViewLayout();
        }
        if(visibleView!=null) {
            mHeaderViewHeight = mHeaderView.getHeight() - visibleView.getHeight();
        }else{
            mHeaderViewHeight = mHeaderView.getHeight() ;
        }
        adjustScrollableViewHeight();
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, null);
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL && mScrollableView != null;
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        int offset = getHeaderTopAndBottomOffset();
        dispatchNestedPreScroll(dx, dy, consumed, null);
        int deltaY = dy - consumed[1];
        if (offset > -mHeaderViewHeight && deltaY > 0) {
            consumed[1] += scroll((offset - deltaY));
        } else if (offset < 0 && deltaY < 0) {
            consumed[1] += scroll((offset - deltaY));
        }
    }

    private int scroll(int offset) {
        int originOffset = mHeaderViewOffsetHelper == null ? 0 : mHeaderViewOffsetHelper.getTopAndBottomOffset();
        int newOffset = constrain(offset, -mHeaderViewHeight, 0);
        if (mHeaderViewOffsetHelper != null) {
            mHeaderViewOffsetHelper.setTopAndBottomOffset(newOffset);
            Log.e(TAG, "mHeaderViewOffsetHelper.getTopAndBottomOffset() = " + mHeaderViewOffsetHelper.getTopAndBottomOffset());
        }
        if (mScrollViewOffsetHelper != null) {
            mScrollViewOffsetHelper.setTopAndBottomOffset(newOffset);
            Log.e(TAG, "mScrollViewOffsetHelper.getTopAndBottomOffset() = " + mScrollViewOffsetHelper.getTopAndBottomOffset());
        }
        return originOffset - newOffset;
    }

    public void showHeader() {
        if (mHeaderView != null && mHeaderViewOffsetHelper != null) {
            scroll(-mHeaderViewOffsetHelper.getLayoutTop());
        }
    }

    private void hideHeader() {
        if (mHeaderView != null && mHeaderViewOffsetHelper != null) {
            scroll(-mHeaderViewHeight);
        }
    }

    private void adjustScrollableViewHeight() {
        if (mHeaderView != null && mScrollableView != null && mScrollableView.getLayoutParams() != null) {
            MarginLayoutParams layoutParams = (MarginLayoutParams) mScrollableView.getLayoutParams();
            if (layoutParams.height != getHeight()) {
                layoutParams.height = getHeight();
                Log.e(TAG, "layoutParams.height = " + layoutParams.height);
                Log.e(TAG, "getHeight() = " + getHeight());
                Log.e(TAG, "mHeaderViewHeight = " + mHeaderViewHeight);
                mScrollableView.setLayoutParams(layoutParams);
            }
        }
    }


    private int getHeaderTopAndBottomOffset() {
        return mHeaderViewOffsetHelper != null ? mHeaderViewOffsetHelper.getTopAndBottomOffset() : 0;
    }

    private int constrain(int amount, int low, int high) {
        return amount < low ? low : (amount > high ? high : amount);
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, nestedScrollAxes);
        startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
    }

    @Override
    public int getNestedScrollAxes() {
        return mNestedScrollingParentHelper.getNestedScrollAxes();
    }

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        mNestedScrollingChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return mNestedScrollingChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return mNestedScrollingChildHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        Log.e(TAG, "stopNestedScroll execute!");
        mNestedScrollingChildHelper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return mNestedScrollingChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed,
                                        int dyUnconsumed, int[] offsetInWindow) {
        return mNestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return mNestedScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mNestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mNestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        if (dispatchNestedPreFling(velocityX, velocityY)) {
            return true;
        }
        Log.e(TAG, "onNestedPreFling: execute and velocityY = " + velocityY);
        if (Math.abs(velocityY) > MINIMUM_FLING_VELOCITY) {
            if (velocityY > 0) {
                Log.e(TAG, "onNestedFling: execute hideHeader");
                hideHeader();
            } else {
                Log.e(TAG, "onNestedFling: execute showHeader");
                showHeader();
            }
        }
        return false;
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        Log.e(TAG, "onNestedFling: execute");
        return dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public void onStopNestedScroll(View child) {
        mNestedScrollingParentHelper.onStopNestedScroll(child);
        stopNestedScroll();
    }

}