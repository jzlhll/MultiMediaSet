package com.allan.netpullrecycleview.core2;

import android.os.Looper;
import android.util.Log;
import android.widget.Toast;


import androidx.annotation.WorkerThread;

import com.allan.baselib.ApplicationUtils;
import com.allan.netpullrecycleview.util.ClickViewByTagListener;
import com.allan.netpullrecycleview.util.OnResponseListener;

import java.lang.ref.WeakReference;

/**
 * 架构中的最重要的一环，抽象了mtop的结果并解析后，抛回给UI
 * 子类实现parseData即可，并追加一些自己的方法用于请求。
 */
public abstract class AbstractPresenter<T> implements OnResponseListener, ClickViewByTagListener.IOnItemDataClicked<T> {
    private static final String TAG = "douban";

    private boolean mIsSuccessOnce = false;
    public boolean isSuccessOnce() { return mIsSuccessOnce;}

    protected final WeakReference<ILoadDataView> mView;

    public AbstractPresenter(ILoadDataView view) {
        mView = new WeakReference<>(view);
    }

    private boolean mIsEnd = false; //是否是最后一页
    protected int mPageIndex = 1; //当前页码

    protected final void resetPage() {
        mIsEnd = false;
        mPageIndex = 1;
    }

    protected final void maskEnd() {
        mIsEnd = true;
    }

    protected boolean isEnd() {
        return mIsEnd;
    }

    @WorkerThread
    protected abstract Object parseData(Object out);

    protected void onDemoData(Object demoData) {
        if (mView.get() != null) {
            mView.get().onDataSucOnWork(101, demoData, null);
        }
    }

    @Override
    public void onResponseSuccess(final Object baseOutDo, final int i, Object o) {
        Log.d(TAG, "onResponse Success baseOutDo = [" + baseOutDo + "], i = [" + i + "], o = [" + o + "]");
        ThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                Object parser = parseData(baseOutDo);
                if (mIsSuccessOnce && parser == null) {
                    Log.d(TAG, "onResponse Success but is null data and already has first");
                    onNoMoreData();
                } else {
                    if (mView.get() != null) {
                        mView.get().onDataSucOnWork(i, parser, o);
                    }
                }
                mIsSuccessOnce = true;
            }
        });
    }

    @Override
    public void onResponseFailed(final int userFlag, String errorCode, final String errorMessage, Object reqContext) {
        Log.w(TAG, "onResponse Failed i = [" + userFlag + "], s = [" + errorCode + "], s1 = [" + errorMessage + "]");
        ApplicationUtils.post(new Runnable() {
            @Override
            public void run() {
                if (mView.get() != null) {
                    mView.get().onDataFail(userFlag, errorMessage);
                }
            }
        });
    }

    protected void onNoMoreData() {
        Log.d(TAG, "on NoMore Data");
        ApplicationUtils.post(new Runnable() {
            @Override
            public void run() {
                if (mView.get() != null) {
                    mView.get().onNoMoreData();
                }
            }
        });
    }

    protected void onNoMoreDataDelay() {
        Log.d(TAG, "on NoMore Data");
        ApplicationUtils.getMainHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mView.get() != null) {
                    mView.get().onNoMoreData();
                }
            }
        }, 1000);
    }

    /**
     * @param uri 跳转到这个uri连接打开
     */
    public final void jumpUri(String uri) {
//        Intent intent = new Intent();
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.setAction(Intent.ACTION_VIEW);
//        intent.setData(Uri.parse(uri));
        try {
            //ApplicationUtils.getApplication().startActivity(intent);
        } catch (android.content.ActivityNotFoundException e) {
            e.printStackTrace();
            if (Looper.getMainLooper() == Looper.myLooper()) {
                Toast.makeText(ApplicationUtils.getApplication(), "打开失败！", Toast.LENGTH_LONG).show();
            }
        }
    }
}