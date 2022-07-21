package com.allan.netpullrecycleview.pullrecyler;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.annotation.Nullable;

import com.allan.netpullrecycleview.scrollview.NestedHorzScrollView;

public class LastPullRecycleView extends PullRecycleView{
    private final int touchSlop;

    private boolean mIsFindRootScroll = false;
    private ViewGroup rootScrollView;  //不一定是父View todo 换成你的滑动外层View

    public LastPullRecycleView(Context context) {
        this(context, null);
    }

    public LastPullRecycleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LastPullRecycleView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        ViewConfiguration vc = ViewConfiguration.get(context);
        touchSlop = vc.getScaledTouchSlop();
    }

    private int mLastX = 0;

    @Override
    public boolean dispatchTouchEvent(MotionEvent e) {
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (!mIsFindRootScroll) { //找一次即可
                    int level = 3;
                    ViewParent parent = getParent();
                    do {
                        //todo 换成你的滑动外层View
                        if (parent instanceof NestedHorzScrollView) {
                            rootScrollView = (ViewGroup) parent;
                            break;
                        } else if (parent != null) {
                            parent = parent.getParent();
                        } else {
                            break;
                        }
                    } while(level-- > 0);

                    mIsFindRootScroll = true;
                }
                if(DEBUG_VIEW) Log.d(TAG,  e.getAction() + ": dispatch: " + e.getX());
                mLastX = (int) e.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                if (e.getX() - mLastX > touchSlop) {
                    if(DEBUG_VIEW) Log.d(TAG,  e.getAction() +": dispatch 向右滑动: " + e.getX());
                } else if (mLastX - e.getX() > touchSlop) {
                    if(DEBUG_VIEW) Log.d(TAG,  e.getAction() +": dispatch 向左滑动: " + e.getX());
                    getParent().requestDisallowInterceptTouchEvent(false);
                    if (rootScrollView != null && rootScrollView.canScrollHorizontally(1)) { //todo 这里就是判断
                        return false;
                    }
                }
                break;
        }

        return super.dispatchTouchEvent(e);
    }
}
