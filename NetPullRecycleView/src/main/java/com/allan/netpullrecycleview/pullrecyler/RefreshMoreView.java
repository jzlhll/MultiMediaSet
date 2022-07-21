package com.allan.netpullrecycleview.pullrecyler;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

public abstract class RefreshMoreView extends RefreshView {
    private boolean firstTimeNotShow = false;

    public RefreshMoreView(Context context) {
        super(context);
        mMainView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    public RefreshMoreView(Context context, boolean firstNotShow) {
        super(context);
        mMainView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        firstTimeNotShow = firstNotShow; //如果标记为true；那么，我们就让他一上来为空。直到有数据以后，才显示
        if (firstNotShow) {
            mMainView.setVisibility(View.INVISIBLE);
        }
    }

    protected final void onFirstDataSetter() { //直到有数据以后，才显示
        if (firstTimeNotShow) mMainView.setVisibility(View.VISIBLE);
    }

    //默认状态
    protected abstract void onNormalState();

    //正在滑动拉伸过程中
    protected abstract void onLoadingDragging();

    //请你去刷新。这个时候已经拉伸完成回到了Scroll Adle状态
    protected abstract void onLoadingMore();

    //刷新成功
    protected abstract void onResultSuccess();

    //没有数据了，再也不用继续加载
    protected abstract void onResultNoMore();

    protected abstract void onResultResetMore();

    //刷新失败
    protected abstract void onResultFail();
}

