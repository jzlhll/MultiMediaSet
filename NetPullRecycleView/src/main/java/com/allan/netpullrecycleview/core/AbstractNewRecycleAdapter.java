package com.allan.netpullrecycleview.core;

import androidx.annotation.MainThread;
import androidx.recyclerview.widget.RecyclerView;

import com.allan.netpullrecycleview.pullrecyler.IDataProcessor;
import com.allan.netpullrecycleview.pullrecyler.SimplePullRecycleView;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author allan.jiang
 * @Date: 2022/07/18 16:26
 * @Description
 */

public abstract class AbstractNewRecycleAdapter<T, VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> implements IDataProcessor<T> {
    protected List<T> mData = new ArrayList<>(32);
    public List<T> getList() {
        return mData;
    }

    protected final SimplePullRecycleView mPull;

    public AbstractNewRecycleAdapter(SimplePullRecycleView p) {
        mPull = p;
    }

    @Override
    @MainThread
    public void setData(List<T> data) {
        mData = data;
        fixData();
        notifyDataSetChanged();
    }

    public void removeData(List<T> data) {
        mData.removeAll(data);
        notifyDataSetChanged();
    }

    protected void fixData() {}

    @Override
    @MainThread
    public void clearData() {
        mPull.hasMoreData();
        mData.clear();
        notifyDataSetChanged();
    }

    @Override
    @MainThread
    public void appendData(List<T> data) {
        mData.addAll(data);
        notifyItemChanged(mData.size() - data.size(), data.size());
    }

    @Override
    public void noMoreData() {
        mPull.setNoMoreData();
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}
