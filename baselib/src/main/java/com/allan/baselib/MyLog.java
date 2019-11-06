package com.allan.baselib;

import android.util.Log;

public class MyLog {
    private static final String TAG = "MMSet";
    private static final boolean DEBUG_LEVEL_E = true;
    private static final boolean DEBUG_LEVEL_W = true && DEBUG_LEVEL_E;
    private static final boolean DEBUG_LEVEL_D = true && DEBUG_LEVEL_E && DEBUG_LEVEL_W;

    public static void d(String tag, String log) {
        if(DEBUG_LEVEL_D) Log.d(TAG, tag + ":" + log);
    }

    public static void e(String tag, String log) {
        if(DEBUG_LEVEL_E) Log.e(TAG, tag + ":" + log);
    }

    public static void w(String tag, String log) {
        if(DEBUG_LEVEL_W) Log.w(TAG, tag + ":" + log);
    }

    public static void d(String log) {
        if(DEBUG_LEVEL_D) Log.d(TAG, log);
    }

    public static void e(String log) {
        if(DEBUG_LEVEL_E) Log.e(TAG, log);
    }

    public static void w(String log) {
        if(DEBUG_LEVEL_W) Log.w(TAG, log);
    }
}
