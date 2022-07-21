package com.allan.netpullrecycleview.core2;

import androidx.annotation.MainThread;
import androidx.recyclerview.widget.RecyclerView;

import com.allan.netpullrecycleview.pullrecyler.IDataProcessor;
import com.allan.netpullrecycleview.pullrecyler.PullRecycleView;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractRecycleAdapter<T, VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH>
        implements IDataProcessor<T> {
    protected List<T> mData = new ArrayList<>(16);
    protected final PullRecycleView mPull;

    protected String listTopName;

    public AbstractRecycleAdapter(PullRecycleView p) {
        mPull = p;
    }

    @Override
    @MainThread
    public void setData(List<T> data) {
        mData = data;
        fixData();
        notifyDataSetChanged();
        mPull.onComplete(mData.size() > 0 ? PullRecycleView.ON_COMPLETE_STATE_SUC : PullRecycleView.ON_COMPLETE_STATE_FAIL);
    }

    protected void fixData() {}

    @Override
    @MainThread
    public void clearData() {
        mPull.hasMoreData();
        mData.clear();
        notifyDataSetChanged();
        mPull.onComplete(PullRecycleView.ON_COMPLETE_STATE_SUC);
    }

    @Override
    @MainThread
    public void appendData(List<T> data) {
        mData.addAll(data);
        notifyItemChanged(mData.size() - data.size(), data.size());
        mPull.onComplete(mData.size() > 0 ? PullRecycleView.ON_COMPLETE_STATE_SUC : PullRecycleView.ON_COMPLETE_STATE_FAIL);
    }

    @Override
    public void noMoreData() {
        mPull.setNoMoreData();
        mPull.onComplete(PullRecycleView.ON_COMPLETE_STATE_NO_MORE);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}
