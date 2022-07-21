package com.allan.netpullrecycleview.core;

import android.util.Log;

import com.allan.baselib.ApplicationUtils;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * 架构中的最重要的一环，抽象了mtop的结果并解析后，抛回给UI
 * 子类实现parseData即可，并追加一些自己的方法用于请求。
 */
public abstract class AbstractNewPresenter<T> {
    private static final String TAG = "newPresenter";

    private boolean mIsSuccessOnce = false;
    public boolean isSuccessOnce() { return mIsSuccessOnce;}

    private WeakReference<IDataNewView<T>> mView;

    public AbstractNewPresenter(IDataNewView<T> view) {
        mView = new WeakReference<>(view);
    }

    public AbstractNewPresenter() {
    }

    public void attachView(IDataNewView<?> view) {
        mView = new WeakReference<>((IDataNewView<T>) view);
    }

    private boolean mIsEnd = false; //是否是最后一页
    protected int mPageIndex = 1; //当前页码

    public final void resetPage() {
        Log.d(TAG, "reset page");
        mIsEnd = false;
        mPageIndex = 1;
    }

    final void maskEnd() {
        mIsEnd = true;
    }

    protected boolean isEnd() {
        return mIsEnd;
    }

    protected boolean mIsRequesting = false;
    public void setRequestingStart() {
        mIsRequesting = true;
    }

    protected void onSuccess(List<T> data) {
        int curPageIndex = mPageIndex;
        //Log.d(TAG, "onSuccess: " + data);
        if (mIsSuccessOnce && (data == null || data.size() == 0)) {
            Log.d(TAG, "onResponse Success but is null data and already has first");
            maskEnd(); //TODO 判断end，是否是data null
            onNoMoreData();
        } else {
            if (mView != null && mView.get() != null) {
                mView.get().onDataSuccess(data, curPageIndex);
            }
        }
        mIsSuccessOnce = true;

        mPageIndex++;
        mIsRequesting = false;
    }

    protected void onError(String err, String msg) {
        int curPageIndex = mPageIndex;
        Log.w(TAG, "onError " + err + " msg " + msg);
        if (curPageIndex == 1) {
            ApplicationUtils.post(() -> {
                if (mView != null && mView.get() != null) {
                    mView.get().onDataError(err, curPageIndex);
                }
            });
        } else {//如果第二页开始失败，就直接认为后面所有页都不请求了。
            maskEnd();
            mIsRequesting = false;
        }
    }

    protected void onNoMoreData() {
        Log.d(TAG, "on NoMore Data");
        ApplicationUtils.post(() -> {
            if (mView != null && mView.get() != null) {
                mView.get().onDataNoMore();
            }
        });
    }

    protected void onNoMoreDataDelay() {
        Log.d(TAG, "on NoMore Data");
        ApplicationUtils.getMainHandler().postDelayed(() -> {
            if (mView != null && mView.get() != null) {
                mView.get().onDataNoMore();
            }
        }, 1000);
    }
}
