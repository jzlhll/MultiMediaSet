package com.allan.netpullrecycleview.scrollview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

/**
 * 首先查看LastHorzScrollView
 * 进而查看这里。
 * 我们只希望对于下部分包含Rv的情况来处理一下。
 */
public class LastRvNoHeadHorzScrollView extends NestedHorzScrollView {
    private final int deltaY;
    private static final int LAST_DELTA_WIDTH = 12; //不严格限制到他的位置才允许触发内部的滑动
    private static final float MARGIN_TOP = 66f; //hf_douban_onetop的recycleview的marginTop高度

    public LastRvNoHeadHorzScrollView(Context context) {
        this(context, null);
    }

    public LastRvNoHeadHorzScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LastRvNoHeadHorzScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        deltaY = dip2px(context, MARGIN_TOP);
    }

    public static int dip2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dpValue * scale + 0.5F);
    }

    public static int px2dip(Context context, float pxValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int)(pxValue / scale + 0.5F);
    }

    @Override
    protected boolean onInterruptWhenScrollCertainly(int lastTouchX, int x, int y) {
        if (y > deltaY && x < lastTouchX) { //比那个还要继续抛掉一个marginTop的高度来保证RecycleView之上的能点击
            int scrollX = getScrollX();
            int scrollRangeX = getScrollRangeX();
            if (DEBUG_VIEW) {
                Log.d(TAG, "Certainly1: scrollX " + scrollX + " scrollRangeX " + scrollRangeX + " x: " + x);
            }
            //当，当前位置没到ScrollView的最终点；并且，点击的位置在Rv里面
            if (scrollX < scrollRangeX - LAST_DELTA_WIDTH && scrollX + x > scrollRangeX) {
                if (DEBUG_VIEW) {
                    Log.d(TAG, "Certainly2: scrollX " + scrollX + " scrollRangeX " + scrollRangeX + " " + x);
                }
                return true;
            }
        }
        return false;
    }
}
