package com.allan.netpullrecycleview.util;

/**
 * @Author allan.jiang
 * @Date: 2022/07/18 16:50
 * @Description
 */
public final class NoFastClickUtils {
    private static final int SPACE_TIME_DEFAULT = 500; // 0.8s
    private static long sLastClickTime = 0; // 上次点击的时间

    public static boolean isFastClick() {
        return isFastClick(SPACE_TIME_DEFAULT);
    }

    public static boolean isFastClick(int gap) {
        long currentTime = System.currentTimeMillis();
        boolean isFast; // 是否允许点击
        isFast = currentTime - sLastClickTime <= gap;
        if (!isFast) {
            sLastClickTime = currentTime;
        }
        return isFast;
    }
}
