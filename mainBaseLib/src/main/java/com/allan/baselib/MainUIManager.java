package com.allan.baselib;

import android.os.Message;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

public class MainUIManager implements WeakHandler.Callback {
    private static MainUIManager mManager;
    private WeakHandler mMainWeakHandler;

    private MainUIManager() {
        mMainWeakHandler = new WeakHandler(this);
    }

    public static MainUIManager get() {
        if (mManager == null) {
            synchronized (MainUIManager.class) {
                if (mManager == null) {
                    mManager = new MainUIManager();
                }
            }
        }

        return mManager;
    }

    public void toastSnackbar(final View view, final String str) {
        mMainWeakHandler.post(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(view, str, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    public void toastSnackbar(final View view, final String str, final View.OnClickListener l) {
        mMainWeakHandler.post(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(view, str, Snackbar.LENGTH_LONG)
                        .setAction("Action", l).show();
            }
        });
    }

    @Override
    public void handleMessage(Message msg) {
        //
    }
}
