package com.allan.netpullrecycleview.scrollview;

import android.view.View;
import android.view.ViewGroup;

import androidx.core.view.ViewCompat;

class NestedScrollingParentHelper {
    private int mNestedScrollAxesTouch;
    private int mNestedScrollAxesNonTouch;

    /**
     * Construct a new helper for a given ViewGroup
     */
    public NestedScrollingParentHelper(ViewGroup viewGroup) {
    }

    public void onNestedScrollAccepted(View child, View target,
                                       int axes) {
        onNestedScrollAccepted(child, target, axes, ViewCompat.TYPE_TOUCH);
    }

    public void onNestedScrollAccepted(View child, View target,
                                       int axes, int type) {
        if (type == ViewCompat.TYPE_NON_TOUCH) {
            mNestedScrollAxesNonTouch = axes;
        } else {
            mNestedScrollAxesTouch = axes;
        }
    }

    public int getNestedScrollAxes() {
        return mNestedScrollAxesTouch | mNestedScrollAxesNonTouch;
    }

    public void onStopNestedScroll(View target) {
        onStopNestedScroll(target, ViewCompat.TYPE_TOUCH);
    }

    public void onStopNestedScroll(View target, int type) {
        if (type == ViewCompat.TYPE_NON_TOUCH) {
            mNestedScrollAxesNonTouch = ViewGroup.SCROLL_AXIS_NONE;
        } else {
            mNestedScrollAxesTouch = ViewGroup.SCROLL_AXIS_NONE;
        }
    }
}

