package com.allan.netpullrecycleview.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

/**
 * @Author allan.jiang
 * @Date: 2022/07/18 16:46
 * @Description
 */
public class ViewPagerFixed extends ViewPager {
    public ViewPagerFixed(@NonNull Context context) {
        super(context);
    }

    public ViewPagerFixed(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    //禁用滑动
    private static final boolean NO_SCROLL = true;

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (NO_SCROLL) {
            return false;
        }
        try {
            return super.onTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (NO_SCROLL) {
            return false;
        }

        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
