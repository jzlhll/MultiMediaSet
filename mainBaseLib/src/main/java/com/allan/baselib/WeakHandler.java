package com.allan.baselib;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;

public class WeakHandler extends Handler {
    public interface Callback {
        void handleMessage(Message msg);
    }

    private WeakReference<Callback> mRef;

    public WeakHandler(Callback context) {
        super(Looper.getMainLooper());
        mRef = new WeakReference<>(context);
    }
    public WeakHandler(Callback context, Looper looper) {
        super(looper);
        mRef = new WeakReference<>(context);
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        Callback ref = mRef != null ? mRef.get() : null;
        if (ref != null) {
            ref.handleMessage(msg);
        }
    }
}
