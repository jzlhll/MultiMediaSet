package com.allan.netpullrecycleview.scrollview;

import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;

import androidx.core.view.NestedScrollingChild;
import androidx.core.view.NestedScrollingChild3;
import androidx.core.view.NestedScrollingParent;
import androidx.core.view.NestedScrollingParent2;
import androidx.core.view.NestedScrollingParent3;

class NestedScrollingChildHelper {
    private ViewParent mNestedScrollingParentTouch;
    private ViewParent mNestedScrollingParentNonTouch;
    private final View mView;
    private boolean mIsNestedScrollingEnabled;
    private int[] mTempNestedScrollConsumed;

    /**
     * Indicates that the input type for the gesture is from a user touching the screen.
     */
    public static final int TYPE_TOUCH = 0;

    /**
     * Indicates that the input type for the gesture is caused by something which is not a user
     * touching a screen. This is usually from a fling which is settling.
     */
    public static final int TYPE_NON_TOUCH = 1;

    /**
     * Construct a new helper for a given view.
     */
    public NestedScrollingChildHelper(View view) {
        mView = view;
    }

    public void setNestedScrollingEnabled(boolean enabled) {
        if (Build.VERSION.SDK_INT >= 21) {
            mView.stopNestedScroll();
        } else if (mView instanceof NestedScrollingChild) {
            ((NestedScrollingChild) mView).stopNestedScroll();
        }
        mIsNestedScrollingEnabled = enabled;
    }

    public boolean isNestedScrollingEnabled() {
        return mIsNestedScrollingEnabled;
    }

    public boolean hasNestedScrollingParent() {
        return hasNestedScrollingParent(TYPE_TOUCH);
    }

    public boolean hasNestedScrollingParent(int type) {
        return getNestedScrollingParentForType(type) != null;
    }

    public boolean startNestedScroll(int axes) {
        return startNestedScroll(axes, TYPE_TOUCH);
    }

    public boolean startNestedScroll(int axes, int type) {
        if (hasNestedScrollingParent(type)) {
            // Already in progress
            return true;
        }
        if (isNestedScrollingEnabled()) {
            ViewParent p = mView.getParent();
            View child = mView;
            while (p != null) {
                if (onStartNestedScroll(p, child, mView, axes, type)) {
                    setNestedScrollingParentForType(type, p);
                    onNestedScrollAccepted(p, child, mView, axes, type);
                    return true;
                }
                if (p instanceof View) {
                    child = (View) p;
                }
                p = p.getParent();
            }
        }
        return false;
    }

    public static void onNestedScrollAccepted(ViewParent parent, View child, View target,
                                              int nestedScrollAxes, int type) {
        if (parent instanceof NestedScrollingParent2) {
            // First try the NestedScrollingParent2 API
            ((NestedScrollingParent2) parent).onNestedScrollAccepted(child, target,
                    nestedScrollAxes, type);
        } else if (type == TYPE_TOUCH) {
            // Else if the type is the default (touch), try the NestedScrollingParent API
            if (Build.VERSION.SDK_INT >= 21) {
                try {
                    parent.onNestedScrollAccepted(child, target, nestedScrollAxes);
                } catch (AbstractMethodError e) {
                    Log.e("TAG", "ViewParent " + parent + " does not implement interface "
                            + "method onNestedScrollAccepted", e);
                }
            } else if (parent instanceof NestedScrollingParent) {
                ((NestedScrollingParent) parent).onNestedScrollAccepted(child, target,
                        nestedScrollAxes);
            }
        }
    }

    public static boolean onStartNestedScroll(ViewParent parent, View child, View target,
                                              int nestedScrollAxes, int type) {
        if (parent instanceof NestedScrollingParent2) {
            // First try the NestedScrollingParent2 API
            return ((NestedScrollingParent2) parent).onStartNestedScroll(child, target,
                    nestedScrollAxes, type);
        } else if (type == TYPE_TOUCH) {
            // Else if the type is the default (touch), try the NestedScrollingParent API
            if (Build.VERSION.SDK_INT >= 21) {
                try {
                    return parent.onStartNestedScroll(child, target, nestedScrollAxes);
                } catch (AbstractMethodError e) {
                    Log.e("TAG", "ViewParent " + parent + " does not implement interface "
                            + "method onStartNestedScroll", e);
                }
            } else if (parent instanceof NestedScrollingParent) {
                return ((NestedScrollingParent) parent).onStartNestedScroll(child, target,
                        nestedScrollAxes);
            }
        }
        return false;
    }

    public void stopNestedScroll() {
        stopNestedScroll(TYPE_TOUCH);
    }

    public void stopNestedScroll(int type) {
        ViewParent parent = getNestedScrollingParentForType(type);
        if (parent != null) {
            onStopNestedScroll(parent, mView, type);
            setNestedScrollingParentForType(type, null);
        }
    }

    public static void onStopNestedScroll(ViewParent parent, View target, int type) {
        if (parent instanceof NestedScrollingParent2) {
            // First try the NestedScrollingParent2 API
            ((NestedScrollingParent2) parent).onStopNestedScroll(target, type);
        } else if (type == TYPE_TOUCH) {
            // Else if the type is the default (touch), try the NestedScrollingParent API
            if (Build.VERSION.SDK_INT >= 21) {
                try {
                    parent.onStopNestedScroll(target);
                } catch (AbstractMethodError e) {
                    Log.e("TAG", "ViewParent " + parent + " does not implement interface "
                            + "method onStopNestedScroll", e);
                }
            } else if (parent instanceof NestedScrollingParent) {
                ((NestedScrollingParent) parent).onStopNestedScroll(target);
            }
        }
    }

    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed,
                                        int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return dispatchNestedScrollInternal(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                offsetInWindow, TYPE_TOUCH, null);
    }

    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed,
                                        int dyUnconsumed, int[] offsetInWindow, int type) {
        return dispatchNestedScrollInternal(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                offsetInWindow, type, null);
    }

    /**
     * Dispatch one step of a nested scrolling operation to the current nested scrolling parent.
     *
     * <p>This is a delegate method. Call it from your {@link NestedScrollingChild3} interface
     * method with the same signature to implement the standard policy.
     */
    public void dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed,
                                     int dyUnconsumed, int[] offsetInWindow, int type,
                                     int[] consumed) {
        dispatchNestedScrollInternal(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                offsetInWindow, type, consumed);
    }

    private boolean dispatchNestedScrollInternal(int dxConsumed, int dyConsumed,
                                                 int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow,
                                                 int type, int[] consumed) {
        if (isNestedScrollingEnabled()) {
            final ViewParent parent = getNestedScrollingParentForType(type);
            if (parent == null) {
                return false;
            }

            if (dxConsumed != 0 || dyConsumed != 0 || dxUnconsumed != 0 || dyUnconsumed != 0) {
                int startX = 0;
                int startY = 0;
                if (offsetInWindow != null) {
                    mView.getLocationInWindow(offsetInWindow);
                    startX = offsetInWindow[0];
                    startY = offsetInWindow[1];
                }

                if (consumed == null) {
                    consumed = getTempNestedScrollConsumed();
                    consumed[0] = 0;
                    consumed[1] = 0;
                }

                onNestedScroll(parent, mView,
                        dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, consumed);

                if (offsetInWindow != null) {
                    mView.getLocationInWindow(offsetInWindow);
                    offsetInWindow[0] -= startX;
                    offsetInWindow[1] -= startY;
                }
                return true;
            } else if (offsetInWindow != null) {
                // No motion, no dispatch. Keep offsetInWindow up to date.
                offsetInWindow[0] = 0;
                offsetInWindow[1] = 0;
            }
        }
        return false;
    }

    public static void onNestedScroll(ViewParent parent, View target, int dxConsumed,
                                      int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type,
                                      int[] consumed) {
        if (parent instanceof NestedScrollingParent3) {
            ((NestedScrollingParent3) parent).onNestedScroll(target, dxConsumed, dyConsumed,
                    dxUnconsumed, dyUnconsumed, type, consumed);
        } else {
            // If we are calling anything less than NestedScrollingParent3, add the unconsumed
            // distances to the consumed parameter so calling NestedScrollingChild3 implementations
            // are told the entire scroll distance was consumed (for backwards compat).
            consumed[0] += dxUnconsumed;
            consumed[1] += dyUnconsumed;

            if (parent instanceof NestedScrollingParent2) {
                ((NestedScrollingParent2) parent).onNestedScroll(target, dxConsumed, dyConsumed,
                        dxUnconsumed, dyUnconsumed, type);
            } else if (type == TYPE_TOUCH) {
                // Else if the type is the default (touch), try the NestedScrollingParent API
                if (Build.VERSION.SDK_INT >= 21) {
                    try {
                        parent.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed,
                                dyUnconsumed);
                    } catch (AbstractMethodError e) {
                        Log.e("TAG", "ViewParent " + parent + " does not implement interface "
                                + "method onNestedScroll", e);
                    }
                } else if (parent instanceof NestedScrollingParent) {
                    ((NestedScrollingParent) parent).onNestedScroll(target, dxConsumed, dyConsumed,
                            dxUnconsumed, dyUnconsumed);
                }
            }
        }
    }

    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed,
                                           int[] offsetInWindow) {
        return dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, TYPE_TOUCH);
    }

    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed,
                                           int[] offsetInWindow, int type) {
        if (isNestedScrollingEnabled()) {
            final ViewParent parent = getNestedScrollingParentForType(type);
            if (parent == null) {
                return false;
            }

            if (dx != 0 || dy != 0) {
                int startX = 0;
                int startY = 0;
                if (offsetInWindow != null) {
                    mView.getLocationInWindow(offsetInWindow);
                    startX = offsetInWindow[0];
                    startY = offsetInWindow[1];
                }

                if (consumed == null) {
                    consumed = getTempNestedScrollConsumed();
                }
                consumed[0] = 0;
                consumed[1] = 0;
                onNestedPreScroll(parent, mView, dx, dy, consumed, type);

                if (offsetInWindow != null) {
                    mView.getLocationInWindow(offsetInWindow);
                    offsetInWindow[0] -= startX;
                    offsetInWindow[1] -= startY;
                }
                return consumed[0] != 0 || consumed[1] != 0;
            } else if (offsetInWindow != null) {
                offsetInWindow[0] = 0;
                offsetInWindow[1] = 0;
            }
        }
        return false;
    }

    public static void onNestedPreScroll(ViewParent parent, View target, int dx, int dy,
                                         int[] consumed, int type) {
        if (parent instanceof NestedScrollingParent2) {
            // First try the NestedScrollingParent2 API
            ((NestedScrollingParent2) parent).onNestedPreScroll(target, dx, dy, consumed, type);
        } else if (type == TYPE_TOUCH) {
            // Else if the type is the default (touch), try the NestedScrollingParent API
            if (Build.VERSION.SDK_INT >= 21) {
                try {
                    parent.onNestedPreScroll(target, dx, dy, consumed);
                } catch (AbstractMethodError e) {
                    Log.e("TAG", "ViewParent " + parent + " does not implement interface "
                            + "method onNestedPreScroll", e);
                }
            } else if (parent instanceof NestedScrollingParent) {
                ((NestedScrollingParent) parent).onNestedPreScroll(target, dx, dy, consumed);
            }
        }
    }

    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        if (isNestedScrollingEnabled()) {
            ViewParent parent = getNestedScrollingParentForType(TYPE_TOUCH);
            if (parent != null) {
                return onNestedFling(parent, mView, velocityX,
                        velocityY, consumed);
            }
        }
        return false;
    }

    public static boolean onNestedFling(ViewParent parent, View target, float velocityX,
                                        float velocityY, boolean consumed) {
        if (Build.VERSION.SDK_INT >= 21) {
            try {
                return parent.onNestedFling(target, velocityX, velocityY, consumed);
            } catch (AbstractMethodError e) {
                Log.e("TAG", "ViewParent " + parent + " does not implement interface "
                        + "method onNestedFling", e);
            }
        } else if (parent instanceof NestedScrollingParent) {
            return ((NestedScrollingParent) parent).onNestedFling(target, velocityX, velocityY,
                    consumed);
        }
        return false;
    }

    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        if (isNestedScrollingEnabled()) {
            ViewParent parent = getNestedScrollingParentForType(TYPE_TOUCH);
            if (parent != null) {
                return onNestedPreFling(parent, mView, velocityX,
                        velocityY);
            }
        }
        return false;
    }

    public static boolean onNestedPreFling(ViewParent parent, View target, float velocityX,
                                           float velocityY) {
        if (Build.VERSION.SDK_INT >= 21) {
            try {
                return parent.onNestedPreFling(target, velocityX, velocityY);
            } catch (AbstractMethodError e) {
                Log.e("TAG", "ViewParent " + parent + " does not implement interface "
                        + "method onNestedPreFling", e);
            }
        } else if (parent instanceof NestedScrollingParent) {
            return ((NestedScrollingParent) parent).onNestedPreFling(target, velocityX,
                    velocityY);
        }
        return false;
    }
    public static void stopNestedScroll(View view) {
        if (Build.VERSION.SDK_INT >= 21) {
            view.stopNestedScroll();
        } else if (view instanceof NestedScrollingChild) {
            ((NestedScrollingChild) view).stopNestedScroll();
        }
    }

    public void onStopNestedScroll(View child) {
        stopNestedScroll(mView);
    }

    private ViewParent getNestedScrollingParentForType(int type) {
        switch (type) {
            case TYPE_TOUCH:
                return mNestedScrollingParentTouch;
            case TYPE_NON_TOUCH:
                return mNestedScrollingParentNonTouch;
        }
        return null;
    }

    private void setNestedScrollingParentForType(int type, ViewParent p) {
        switch (type) {
            case TYPE_TOUCH:
                mNestedScrollingParentTouch = p;
                break;
            case TYPE_NON_TOUCH:
                mNestedScrollingParentNonTouch = p;
                break;
        }
    }

    private int[] getTempNestedScrollConsumed() {
        if (mTempNestedScrollConsumed == null) {
            mTempNestedScrollConsumed = new int[2];
        }
        return mTempNestedScrollConsumed;
    }
}
