package com.allan.netpullrecycleview.scrollview;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.EdgeEffect;
import android.widget.FrameLayout;
import android.widget.OverScroller;

import androidx.annotation.RequiresApi;
import androidx.core.view.NestedScrollingChild3;
import androidx.core.view.NestedScrollingParent3;
import androidx.core.view.ViewCompat;
import androidx.core.widget.EdgeEffectCompat;

/**
 * NestedHorizontalScrollView：
 * 因为ScrollView有替代者NestedScrollView，解决子滑动控件冲突问题，而HorizontalScrollView没有类似的替代者。
 * 因此开发参考github的开源源码kt的代码，手改动为java。实现的横滑的ScrollView支持解决子类冲突滑动。
 * 原先是基于androidx的，修改为移植了额外的代码支持了v4包。
 * https://github.com/Tans5/HorizontalNestedScrollView/
 */
public class NestedHorzScrollView extends FrameLayout implements NestedScrollingChild3, NestedScrollingParent3 {
    protected static final boolean DEBUG_VIEW = false;
    protected static final String TAG = "NestHorzSclView";

    private static final boolean overScrollEnable = true; //最后的弹簧效果

    /**
     * Indicates no axis of view scrolling.
     */
    public static final int SCROLL_AXIS_NONE = 0;

    /**
     * Indicates scrolling along the horizontal axis.
     */
    public static final int SCROLL_AXIS_HORIZONTAL = 1 << 0;

    /**
     * Indicates scrolling along the vertical axis.
     */
    public static final int SCROLL_AXIS_VERTICAL = 1 << 1;

    /**
     * Indicates that the input type for the gesture is from a user touching the screen.
     */
    public static final int TYPE_TOUCH = 0;

    /**
     * Indicates that the input type for the gesture is caused by something which is not a user
     * touching a screen. This is usually from a fling which is settling.
     */
    public static final int TYPE_NON_TOUCH = 1;

    private final int touchSlop;
    private final int minVelocity;
    private final int maxVelocity;

    private final OverScroller scroller;
    private VelocityTracker velocityTracker;

    private final EdgeEffect edgeGlowStart;
    private final EdgeEffect edgeGlowEnd;

    private final NestedScrollingParentHelper parentHelper;
    private final NestedScrollingChildHelper childHelper;

    public NestedHorzScrollView(Context context) {
        this(context, null);
    }

    public NestedHorzScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NestedHorzScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        scroller = new OverScroller(getContext());

        parentHelper = new NestedScrollingParentHelper(this);
        childHelper = new NestedScrollingChildHelper(this) {
            @Override
            public boolean isNestedScrollingEnabled() {
                return true;
            }
        };

        edgeGlowStart = new EdgeEffect(context);
        edgeGlowEnd = new EdgeEffect(context);

        ViewConfiguration vc = ViewConfiguration.get(context);
        touchSlop = vc.getScaledTouchSlop();
        minVelocity = vc.getScaledMinimumFlingVelocity();
        maxVelocity = vc.getScaledMaximumFlingVelocity();

        setClickable(true);
        setFocusable(true);
        setWillNotDraw(false);
    }

    private int lastTouchX = -1;
    private int activePointerId = -1;
    private boolean isBeingDragged = false;
    private int lastScrollerX = 0;
    private int nestedXOffset = 0;

    private int scrollRangeX = -1;

    private boolean mIsFixedScrollRangeX = true;
    public final void setIsFixedScrollRangeX(boolean fixed) {
        mIsFixedScrollRangeX = fixed;
    }

//    @Override
//    public boolean isNestedScrollingEnabled() {
//        return childHelper.isNestedScrollingEnabled();
//    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int actionMasked = ev != null ? ev.getActionMasked() : -1;
        if (actionMasked == -1) {
            return false;
        }

        if (actionMasked == MotionEvent.ACTION_MOVE && isBeingDragged) {
            return true;
        }

        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN: {
                int x = (int) (ev.getX() + 0.5);
                int y = (int) (ev.getY() + 0.5);

                lastTouchX = x;
                activePointerId = ev.getPointerId(0);

                if (DEBUG_VIEW) {
                    Log.d(TAG, "----cur scrollX: " + getScrollX() + ", " + getScrollRangeX());
                    Log.d(TAG, "on Intercept ACTION_DOWN");
                }
                boolean interrupt;
                if (!inChild(x, y)) {
                    isBeingDragged = false;
                    recycleVelocityTracker();
                    interrupt = false;
                            //保持false：会导致内部嵌套的子滑动控件（如Rv），在只显示了一部分的时候就开始滑动的小体验问题。
                            //不能改成true；之前在in Child里面追加了逻辑，如果判断子View卡在一半就interrupt为true拦截，
                                 //那么会导致无法把事件继续往子View下发导致停在一半的Rv里面的控件无法点击
                    if (DEBUG_VIEW) {
                        Log.d(TAG, "on Intercept ACTION_DOWN not in Child return: false");
                    }
                } else {
                    initOrResetVelocityTracker();
                    if (velocityTracker != null) {
                        velocityTracker.addMovement(ev);
                    }
                    scroller.computeScrollOffset();
                    isBeingDragged = !scroller.isFinished();
                    startNestedScroll(ViewCompat.SCROLL_AXIS_HORIZONTAL, ViewCompat.TYPE_TOUCH);

                    interrupt = isBeingDragged;
                    if (DEBUG_VIEW) {
                        Log.d(TAG, "on Intercept ACTION_DOWN in Child and start scroll return: " + interrupt);
                    }
                }
                return interrupt;
            }

            case MotionEvent.ACTION_MOVE: {
                boolean interrupt;
                int pointerIndex = ev.findPointerIndex(activePointerId);
                if (pointerIndex < 0) {
                    //此时认为是多手指，有一个手指不见了。导致的获取不到。我们则将使用0 index为准
                    if(DEBUG_VIEW) Log.w(TAG, "pointer index = -1 reset to 0");
                    pointerIndex = 0;
                }

                int x = (int) (ev.getX(pointerIndex) + 0.5);
                int dx = Math.abs(lastTouchX - x);
                boolean isScroll = dx > touchSlop;

                if (isScroll) {
                    boolean isHorzScroll = (parentHelper.getNestedScrollAxes() & SCROLL_AXIS_HORIZONTAL) == 0;
                    if (isHorzScroll) {
                        interrupt = true;
                    } else {
                        //与最后的else不同，这里希望继承类再考虑下，是否return true拦截一下。
                        //因为到了这里表明现在肯定是一个滑动事件；那么肯定不应该触发click，即不用担心，拦截后不传递给子View而丢失点击事件。
                        interrupt = onInterruptWhenScrollCertainly(lastTouchX, x, (int)(ev.getY(pointerIndex) + 0.5));
                    }

                    if (interrupt) {
                        if (DEBUG_VIEW) {
                            Log.d(TAG, "on Intercept ACTION_MOVE scroll And interrupt!");
                        }

                        isBeingDragged = true;
                        interrupt = true; //这里拦截表明的是，我们确认让我们的ScrollView去滑动了，子控件不会滑动。

                        lastTouchX = x;
                        initVelocityTrackerIfNotExists();
                        if (velocityTracker != null) {
                            velocityTracker.addMovement(ev);
                        }
                        nestedXOffset = 0;
                        if (getParent() != null) {
                            getParent().requestDisallowInterceptTouchEvent(true);
                        }
                    }
                } else {
                    interrupt = false; //我们认为他不是滑动或者滑动，但是scrollView不承接，继续让子View去承接。
                    if (DEBUG_VIEW) {
                        Log.d(TAG, "on Intercept ACTION_MOVE isScroll= " + isScroll);
                    }
                }

                return interrupt;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                isBeingDragged = false;
                activePointerId = -1;
                recycleVelocityTracker();
                if (scroller.springBack(getScrollX(), getScrollY(), 0, getScrollRangeX(), 0, 0)) {
                    postInvalidateOnAnimation();
                }
                stopNestedScroll(ViewCompat.TYPE_TOUCH);
                if (DEBUG_VIEW) {
                    Log.d(TAG, "on Intercept ACTION_UP stop scroll");
                }
                return false;
            }

            case MotionEvent.ACTION_POINTER_UP: {
                int actionIndex = ev.getActionIndex();
                int currentId = ev.getPointerId(actionIndex);
                if (currentId == activePointerId) {
                    int newIndex = actionIndex == 0 ? 1 : 0;
                    activePointerId = ev.getPointerId(newIndex);
                    lastTouchX = (int) (ev.getX(newIndex) + 0.5);
                }
                if (DEBUG_VIEW) {
                    Log.d(TAG, "on Intercept ACTION_POINTER_UP ");
                }
                return false;
            }
        }

        return false; //别的都false
    }

    /**
     * 这个回调，是当本ScrollView，确认本次是一个滑动事件；
     * 默认返回false表示我不拦截它，让子View抉择（当然没人消费我再来）。
     * 如果继承本类可以覆盖这个函数，修改逻辑return true 让我们自己类来滑动。
     * @param lastTouchX ACTION_DOWN的位置
     * @param x 现在的位置
     */
    protected boolean onInterruptWhenScrollCertainly(int lastTouchX, int x, int y) {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = true;
        if (event == null) {
            return result;
        }

        initVelocityTrackerIfNotExists();
        MotionEvent velocityEvent = MotionEvent.obtain(event);

        int actionMasked = event.getActionMasked();
        velocityEvent.offsetLocation(actionMasked == MotionEvent.ACTION_DOWN ? 0f : nestedXOffset, 0f);

        int actionIndex = event.getActionIndex();
        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN: {
                nestedXOffset = 0;
                activePointerId = event.getPointerId(actionIndex);
                lastTouchX = (int) (event.getX(event.findPointerIndex(activePointerId)) + 0.5);
                if (!scroller.isFinished()) {
                    if (getParent() != null) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                    abortAnimatedScroll();
                }

                if (getChildCount() == 0) {
                    result = false;
                }

                startNestedScroll(ViewCompat.SCROLL_AXIS_HORIZONTAL, ViewCompat.TYPE_TOUCH);
                if (DEBUG_VIEW) {
                    Log.d(TAG, "on Touch ACTION_DOWN start scroll");
                }
            }
            break;

            case MotionEvent.ACTION_MOVE: {
                int activeActionIndex = event.findPointerIndex(activePointerId);
                int x = (int) (event.getX(activeActionIndex) + 0.5);
                int dx = lastTouchX - x;
                if (!isBeingDragged && Math.abs(dx) > touchSlop) {
                    if (getParent() != null) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                    isBeingDragged = true;
                    if (dx > 0) {
                        dx -= touchSlop;
                    } else {
                        dx += touchSlop;
                    }
                }
                if (DEBUG_VIEW) {
                    Log.d(TAG, "on Touch ACTION_MOVE ");
                }
                if (isBeingDragged) {
                    int[] scrollConsumed = {0, 0};
                    int[] scrollOffset = {0, 0};

                    if (dispatchNestedPreScroll(dx, 0, scrollConsumed, scrollOffset, ViewCompat.TYPE_TOUCH)) {
                        dx -= scrollConsumed[0];
                        nestedXOffset += scrollOffset[0];
                    }
                    lastTouchX = x - scrollOffset[0];

                    int oldScrollX = getScrollX();
                    if (overScrollX(dx, getScrollX(), getScrollRangeX())) {
                        if (velocityTracker != null) {
                            velocityTracker.clear();
                        }
                    }

                    int scrolledDeltaX = getScrollX() - oldScrollX;
                    int unconsumedX = dx - scrolledDeltaX;
                    scrollConsumed[0] = 0;
                    dispatchNestedScroll(scrolledDeltaX, 0, unconsumedX, 0, scrollOffset,
                            ViewCompat.TYPE_TOUCH, scrollConsumed);
                    lastTouchX -= scrollOffset[0];
                    nestedXOffset += scrollOffset[0];
                    if (DEBUG_VIEW) {
                        Log.d(TAG, "on Touch ACTION_MOVE ");
                    }
                    if (overScrollEnable) {
                        int pulledToX = oldScrollX + dx;
                        if (pulledToX < 0) {
                            EdgeEffectCompat.onPull(edgeGlowStart, ((float) dx) / getWidth(),
                                    1f - event.getY(activeActionIndex) / ((float) getHeight()));
                            if (!edgeGlowEnd.isFinished()) {
                                edgeGlowEnd.onRelease();
                            }
                        } else if (pulledToX > getScrollRangeX()) {
                            EdgeEffectCompat.onPull(edgeGlowEnd, ((float) dx) / getWidth(),
                                    event.getY(activeActionIndex) / ((float) getHeight()));
                            if (!edgeGlowStart.isFinished()) {
                                edgeGlowStart.onRelease();
                            }
                        }

                        if (!edgeGlowEnd.isFinished() || !edgeGlowStart.isFinished()) {
                            postInvalidateOnAnimation();
                        }
                    }
                }
            }
            break;

            case MotionEvent.ACTION_UP: {
                VelocityTracker velocityTracker = this.velocityTracker;
                int velocityX = 0;
                if (velocityTracker != null) {
                    velocityTracker.computeCurrentVelocity(1000, (float) maxVelocity);
                    velocityX = (int) velocityTracker.getXVelocity(activePointerId);
                }

                if (Math.abs(velocityX) > minVelocity) {
                    if (!dispatchNestedPreFling((float) -velocityX, 0f)) {
                        dispatchNestedFling((float) -velocityX, 0f, true);
                        fling(-velocityX);
                    }
                } else if (scroller.springBack(getScrollX(), getScrollY(), 0, getScrollRangeX(), 0, 0)) {
                    postInvalidateOnAnimation();
                }
                if (DEBUG_VIEW) {
                    Log.d(TAG, "on Touch ACTION_UP ");
                }
                endDrag();
            }
            break;

            case MotionEvent.ACTION_CANCEL: {
                if (isBeingDragged && getChildCount() > 0) {
                    if (scroller.springBack(getScrollX(), getScrollY(), 0, getScrollRangeX(), 0, 0)) {
                        postInvalidateOnAnimation();
                    }
                }
                if (DEBUG_VIEW) {
                    Log.d(TAG, "on Touch ACTION_CANCEL ");
                }
                endDrag();
            }
            break;

            case MotionEvent.ACTION_POINTER_DOWN: {
                activePointerId = event.getPointerId(actionIndex);
                lastTouchX = (int) (event.getX(event.findPointerIndex(activePointerId)) + 0.5);
            }
            break;

            case MotionEvent.ACTION_POINTER_UP: {
                int currentId = event.getPointerId(actionIndex);
                if (currentId == activePointerId) {
                    int newIndex = (actionIndex == 0) ? 1 : 0;
                    activePointerId = event.getPointerId(newIndex);
                    lastTouchX = (int) (event.getX(newIndex) + 0.5);
                    if (DEBUG_VIEW) {
                        Log.d(TAG, "on Touch ACTION_POINTER_UP");
                    }
                }
            }
            break;
        }
        if (velocityTracker != null) {
            velocityTracker.addMovement(velocityEvent);
        }
        velocityEvent.recycle();
        return result;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (canvas != null) {
            if (!edgeGlowStart.isFinished()) {
                int restoreCount = canvas.save();
                float width = getWidth();
                float height = getHeight();
                canvas.rotate(270f);
                canvas.translate(-height, Math.min(0f, getScrollX()));
                edgeGlowStart.setSize((int) height, (int) width);
                if (edgeGlowStart.draw(canvas)) {
                    postInvalidateOnAnimation();
                }
                canvas.restoreToCount(restoreCount);
            }

            if (!edgeGlowEnd.isFinished()) {
                int restoreCount = canvas.save();
                float width = getWidth();
                float height = getHeight();
                canvas.rotate(90f);
                canvas.translate(0f, -(Math.max(getScrollRangeX(), getScrollX()) + width));
                edgeGlowEnd.setSize((int) height, (int) width);
                if (edgeGlowEnd.draw(canvas)) {
                    postInvalidateOnAnimation();
                }
                canvas.restoreToCount(restoreCount);
            }
        }
    }

    @Override
    public void computeScroll() {
        if (scroller.isFinished()) {
            return;
        }
        scroller.computeScrollOffset();
        int x = scroller.getCurrX();
        int dx = x - lastScrollerX;
        lastScrollerX = x;
        int unconsumed = dx;
        int[] scrollConsumed = {0, 0};
        if (dispatchNestedPreScroll(unconsumed, 0, scrollConsumed, null, ViewCompat.TYPE_NON_TOUCH)) {
            unconsumed -= scrollConsumed[0];
        }

        if (unconsumed != 0) {
            int scrollXBefore = getScrollX();
            overScrollX(dx, scrollXBefore, getScrollRangeX());
            int scrollAfter = getScrollX();
            int scrollByMe = scrollAfter - scrollXBefore;
            unconsumed -= scrollByMe;
            scrollConsumed[0] = 0;
            dispatchNestedScroll(scrollByMe, 0, unconsumed, 0, null,
                    ViewCompat.TYPE_NON_TOUCH, scrollConsumed);
            unconsumed -= scrollConsumed[0];
        }
        if (unconsumed < 0) {
            if (edgeGlowStart.isFinished()) {
                edgeGlowStart.onAbsorb((int) scroller.getCurrVelocity());
            }
            abortAnimatedScroll();
        }

        if (unconsumed > 0) {
            if (edgeGlowEnd.isFinished()) {
                edgeGlowEnd.onAbsorb((int) scroller.getCurrVelocity());
            }
            abortAnimatedScroll();
        }

        if (!scroller.isFinished()) {
            postInvalidateOnAnimation();
        } else {
            stopNestedScroll(ViewCompat.TYPE_NON_TOUCH);
        }
    }

    @Override
    protected int computeHorizontalScrollRange() {
        if (getChildCount() > 1) {
            throw new RuntimeException("HorizontalNestedScrollView only support one child.");
        }
        int parentSpace = getWidth() - getPaddingStart() - getPaddingEnd();
        if (getChildCount() == 0) {
            return parentSpace;
        }
        View child = getChildAt(0);
        final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
        int range = child.getRight() + ((lp != null) ? lp.getMarginEnd() : 0);
        int overScrollRange = Math.max(0, range - parentSpace);
        int currentScrollX = getScrollX();
        if (currentScrollX < 0) {
            range -= currentScrollX;
        } else if (currentScrollX > overScrollRange) {
            range += currentScrollX - overScrollRange;
        }

        return range;
    }

    public int getScrollRangeX() {
        if (mIsFixedScrollRangeX && scrollRangeX > 0) {
            return scrollRangeX;
        }
        View child = getChildAt(0);
        MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
        int childSize = child.getWidth() + lp.leftMargin + lp.rightMargin;
        int parentSpace = getWidth() - getPaddingLeft() - getPaddingRight();
        int scrollRangeX = Math.max(0, childSize - parentSpace);
        if (mIsFixedScrollRangeX) {
            this.scrollRangeX = scrollRangeX;
        }
        return scrollRangeX;
    }

    boolean overScrollX(int deltaX, int scrollX, int maxRange) {
        int newScrollX = deltaX + scrollX;
        if (maxRange < newScrollX) {
            scrollTo(maxRange, 0);
            return true;
        }

        if (newScrollX < 0) {
            scrollTo(0, 0);
            return true;
        }

        scrollTo(newScrollX, 0);
        return false;
    }

    public void fling(int velocityX) {
        if (getChildCount() > 0) {
            scroller.fling(
                    getScrollX(), getScrollY(),
                    velocityX, 0,
                    Integer.MIN_VALUE, Integer.MAX_VALUE,
                    0, 0,
                    0, 0
            );
            runAnimatedScroll(true);
        }
    }

    private void runAnimatedScroll(boolean nestedScrolling) {
        if (nestedScrolling) {
            startNestedScroll(ViewCompat.SCROLL_AXIS_HORIZONTAL, ViewCompat.TYPE_NON_TOUCH);
        } else {
            stopNestedScroll(ViewCompat.TYPE_NON_TOUCH);
        }
        lastScrollerX = getScrollX();
        postInvalidateOnAnimation();
    }

    private void endDrag() {
        lastTouchX = -1;
        activePointerId = -1;
        isBeingDragged = false;
        recycleVelocityTracker();
        edgeGlowStart.onRelease();
        edgeGlowEnd.onRelease();
        stopNestedScroll(ViewCompat.TYPE_TOUCH);
    }

    private void initOrResetVelocityTracker() {
        VelocityTracker velocityTracker = this.velocityTracker;
        if (velocityTracker == null) {
            this.velocityTracker = VelocityTracker.obtain();
        } else {
            velocityTracker.clear();
        }
    }

    private void initVelocityTrackerIfNotExists() {
        if (velocityTracker == null) {
            this.velocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        VelocityTracker velocityTracker = this.velocityTracker;
        if (velocityTracker != null) {
            velocityTracker.recycle();
        }
        this.velocityTracker = null;
    }

    private void abortAnimatedScroll() {
        scroller.abortAnimation();
    }

    protected boolean inChild(int x, int y) {
        if (this.getChildCount() <= 0) {
            return false;
        } else {
            int scrollX = getScrollX();
            View child = getChildAt(0); //因为ScrollView要求只有一个子View。所以只要判断子控件[0]的属性即可
            if (DEBUG_VIEW) {
                Log.d(TAG, "in Child scrollX " + scrollX);
            }
            return !(y < child.getTop() || y >= child.getBottom() || x < child.getLeft() - scrollX || x >= child.getRight() - scrollX);
        }
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
        return new MarginLayoutParams(lp);
    }

    @Override
    protected void measureChild(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
        if (child != null) {
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            int childWidthSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            int childHeightSpec = ViewGroup.getChildMeasureSpec(parentHeightMeasureSpec, getPaddingTop() + getPaddingBottom(), lp.height);
            if (DEBUG_VIEW) Log.w(TAG, "measure Child " + childWidthSpec + ", " + childHeightSpec);
            child.measure(childWidthSpec, childHeightSpec);
        }
    }

    @Override
    protected void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        if (child != null) {
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            int childWidthSpec = MeasureSpec.makeMeasureSpec(lp.leftMargin + lp.bottomMargin, MeasureSpec.UNSPECIFIED);
            int childHeightSpec = ViewGroup.getChildMeasureSpec(parentHeightMeasureSpec,
                    getPaddingTop() + getPaddingBottom() + lp.topMargin + lp.bottomMargin + heightUsed,
                    lp.height);
            if (DEBUG_VIEW)
                Log.w(TAG, "measure Child WithMargins " + childWidthSpec + ", " + childHeightSpec);
            child.measure(childWidthSpec, childHeightSpec);
        }
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes, int type) {
        parentHelper.onNestedScrollAccepted(child, target, axes, type);
        startNestedScroll(ViewCompat.SCROLL_AXIS_HORIZONTAL, type);
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed, int type) {
        dispatchNestedPreScroll(dx, dy, consumed, null, type);
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int axes, int type) {
        return (axes & SCROLL_AXIS_HORIZONTAL) != 0;
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type, int[] consumed) {
        onNestedScrollInternal(dxUnconsumed, type, consumed);
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        onNestedScrollInternal(dxUnconsumed, type, null);
    }

    private void onNestedScrollInternal(int dxUnconsumed, int type, int[] consumed) {
        int oldScrollX = getScrollX();
        // scrollBy(dxUnconsumed, 0)
        overScrollX(dxUnconsumed, getScrollX(), getScrollRangeX());
        int myConsumedX = getScrollX() - oldScrollX;
        if (consumed != null) {
            consumed[0] += myConsumedX;
        }
        int myUnconsumedX = dxUnconsumed - myConsumedX;
        childHelper.dispatchNestedScroll(myConsumedX, 0, myUnconsumedX, 0, null, type, consumed);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onStopNestedScroll(View target) {
        //onStopNestedScroll(target: View, type: Int) 缺少这个函数
        parentHelper.onStopNestedScroll(target);
        stopNestedScroll(ViewCompat.TYPE_TOUCH);
    }

    @Override
    public void onStopNestedScroll(View target, int type) {
        parentHelper.onStopNestedScroll(target, type);
        stopNestedScroll(type);
    }

    @Override
    public void dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow, int type, int[] consumed) {
        childHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type, consumed);
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow, int type) {
        return childHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type);
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return childHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow, int type) {
        return childHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return childHelper.dispatchNestedPreFling(velocityX, velocityY);
    }


    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return childHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        if (!consumed) {
            dispatchNestedFling(velocityX, velocityY, consumed);
            fling((int) -velocityX);
            return true;
        }

        return false;
    }

    public void stopNestedScroll(int type) {
        childHelper.stopNestedScroll(type);
    }

    public boolean hasNestedScrollingParent(int type) {
        return childHelper.hasNestedScrollingParent(type);
    }

    public boolean startNestedScroll(int axes, int type) {
        return childHelper.startNestedScroll(axes, type);
    }
}
