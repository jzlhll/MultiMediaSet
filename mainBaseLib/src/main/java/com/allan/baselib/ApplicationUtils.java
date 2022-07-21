package com.allan.baselib;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

/**
 * @Author allan.jiang
 * @Date: 2022/07/18 16:24
 * @Description
 */
public class ApplicationUtils {
    private static volatile Context mApplication;

    private static Handler mMainHandler;

    public static void setApplication(Context application) {
        mApplication = application;
    }

    public static Context getApplication() {
        return mApplication;
    }

    public static Handler getMainHandler() {
        if (null == mMainHandler) {
            synchronized (ApplicationUtils.class) {
                if (null == mMainHandler) {
                    mMainHandler = new Handler(Looper.getMainLooper());
                }
            }
        }
        return mMainHandler;
    }

    public static boolean post(Runnable runable) {
        return getMainHandler().post(runable);
    }


}
