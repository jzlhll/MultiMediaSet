package com.allan.netpullrecycleview.scrollview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

/**
 * NestedHorizontalScrollView在包含RecyclerView的情况下，会出现一点点小的体验问题：
 * 在RecyclerView出现了并停在一半的时候，这时候触摸范围inChild，即事件被RecycleView和他的外层获取了滑动事件，
 * 我们的NestedHorizontalScrollView认为子View该滑，所以，rv开始了滑动，但是rv却停在了一半。
 *
 * 而我希望的结果是ScrollView继续滑动到Rv刚好停在最边缘，才是最好的。
 * 因此开发该类。
 *
 * 其实可以开发一个通用的Rv处于任意位置的嵌套，以后再做。
 */
public class LastRvHorzScrollView extends NestedHorzScrollView {
    private static final int LAST_DELTA_WIDTH = 12; //不严格限制到他的位置才允许触发内部的滑动

    public LastRvHorzScrollView(Context context) {
        this(context, null);
    }

    public LastRvHorzScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LastRvHorzScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected boolean onInterruptWhenScrollCertainly(int lastTouchX, int x, int y) {
        if (DEBUG_VIEW) {
            Log.d(TAG, "onInterCertainly lastTouchX " + lastTouchX + " x: " + x);
        }
        if (x > lastTouchX) {
            return false;
        }

        int scrollX = getScrollX();
        int scrollRangeX = getScrollRangeX();
        if (DEBUG_VIEW) {
            Log.d(TAG, "onInterCertainly scrollX " + scrollX + " scrollRangeX " + scrollRangeX);
        }
        //当，当前位置没到ScrollView的最终点；并且，点击的位置在Rv里面; 追加并且是向右滑动才行
        if (scrollX < scrollRangeX - LAST_DELTA_WIDTH && scrollX + x > scrollRangeX) {
            if (DEBUG_VIEW) {
                Log.d(TAG, "onInterCertainly true!");
            }
            return true;
        }
        return false;
    }
}
