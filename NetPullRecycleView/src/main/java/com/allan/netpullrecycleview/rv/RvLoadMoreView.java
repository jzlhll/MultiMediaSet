package com.allan.netpullrecycleview.rv;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.allan.netpullrecycleview.pullrecyler.RefreshMoreView;

public final class RvLoadMoreView extends RefreshMoreView {
    private TextView mTvTip;
    private boolean mIsNoMore = false;
    private boolean mIsLoadFailed = false;

    public RvLoadMoreView(Context context) {
        super(context);
    }

    @Override
    protected void onNormalState() {
        if (mIsNoMore) {
            mTvTip.setText("这里是底线啦");
        } else if (mIsLoadFailed) {
            mTvTip.setText("加载失败，稍后重试");
        } else {
            mTvTip.setText("滑动加载更多");
        }
    }

    @Override
    protected void onLoadingDragging() {
        mIsLoadFailed = false;
        mTvTip.setText("即将加载");
    }

    @Override
    protected void onLoadingMore() {
        mIsLoadFailed = false;
        mTvTip.setText("正在加载");
    }

    @Override
    protected void onResultSuccess() {
        mIsLoadFailed = false;
        mTvTip.setText("加载成功");
    }

    @Override
    protected void onResultNoMore() {
        mIsLoadFailed = false;
        mIsNoMore = true;
    }

    @Override
    protected void onResultResetMore() {
        mIsLoadFailed = false;
        mIsNoMore = false;
    }

    @Override
    protected void onResultFail() {
        mIsLoadFailed = true;
        mTvTip.setText("加载失败，稍后重试");
    }

    @Override
    protected View onCreateView(Context context) {
        throw new RuntimeException();
        //return LayoutInflater.from(context).inflate(R.layout.hf_douban_load_more_view, null);
    }

    @Override
    protected void initView() {
        throw new RuntimeException();
        //mTvTip = (TextView) findViewFromId(R.id.tv_tip);
    }
}
