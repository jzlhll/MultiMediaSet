package com.allan.netpullrecycleview.pullrecyler;

/**
 * author     ：zhonglun.jzl
 * date       ：Created in 2021/11/18 9:16 下午
 * description：精简头尾的pull
 */
import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.util.ArrayList;
import java.util.List;

public class SimplePullRecycleView extends RecyclerView {
    protected static final boolean DEBUG_VIEW = true;
    protected static final String TAG = "PullRecycleView";

    public static final int MORE_STATE_NORMAL = 0;
    public static final int MORE_STATE_REFRESHING_INTERMEDIATE = 1;
    public static final int MORE_STATE_REFRESHING = 2;

    private final List<Integer> lastExpose = new ArrayList<>();

    public void setUserVisibleListener(IRecyclerViewUserVisibleListener mUserVisibleListener) {
        this.mUserVisibleListener = mUserVisibleListener;
    }

    private IRecyclerViewUserVisibleListener mUserVisibleListener;

    private long mLastScrollTime = 0L;
    public final long getLastScrollTime() {return mLastScrollTime;}

    private final AdapterDataObserver dataObserver = new DataObserver();
    private PullToRefreshRecyclerViewAdapter myAdapter;
    private IPullListener pullListener;
    private int mMoreRefreshState = MORE_STATE_NORMAL;
    private boolean isCanLoadMore = false;

    private boolean isNoMoreData = false;
    public final void setNoMoreData() {
        isNoMoreData = true;
    }

    public final void hasMoreData() {
        isNoMoreData = false;
    }

    public final boolean isCanLoadMore() {
        return isCanLoadMore && !isNoMoreData;
    }

    //是否正在加载更多
    public final boolean isLoadMore() {
        return mMoreRefreshState == MORE_STATE_REFRESHING;
    }

    //记录上次最全可见的最后一个item的位置
    private int mLastVisibleItemIndex = -1;
    //todo 是否每次都需要拉2次才能加载
    private static final boolean EnableTwiceLoadMore = false;

    //设置底部刷新是否可用
    public SimplePullRecycleView setUseLoadMore(boolean loadMore) {
        isCanLoadMore = loadMore;
        return this;
    }

    //上下拉刷新监听
    public SimplePullRecycleView setIPullListener(IPullListener pullListener) {
        this.pullListener = pullListener;
        return this;
    }

    //设置适配器
    public void build(Adapter adapter) {
        setAdapter(adapter);
    }

    public SimplePullRecycleView(Context context) {
        this(context, null);
    }

    public SimplePullRecycleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimplePullRecycleView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        setUseLoadMore(isCanLoadMore());
    }

    @Override
    public void setAdapter(Adapter adapter) {
        if(adapter != null) {
            myAdapter = new PullToRefreshRecyclerViewAdapter(adapter);
            super.setAdapter(myAdapter);
            adapter.registerAdapterDataObserver(dataObserver);
            dataObserver.onChanged();
        } else {
            super.setAdapter(null);
        }
    }

    @Override
    public Adapter getAdapter() {
        if (myAdapter != null) {
            return myAdapter.getAdapter();
        }
        return null;
    }

    @Override
    public void setLayoutManager(LayoutManager layout) {
        super.setLayoutManager(layout);
        if (myAdapter != null) {
            if (layout instanceof GridLayoutManager) {
                final GridLayoutManager gridManager = ((GridLayoutManager) layout);
                gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        return 1;
                    }
                });

            }
        }
    }

    private int findMax(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    private int getLastCompleteVisibleId() {
        LayoutManager layoutManager = getLayoutManager();
        if (layoutManager.getItemCount() <= 0) {
            return 0;
        }

        int lastCompleteVisibleId;
        if (layoutManager instanceof GridLayoutManager) {
            lastCompleteVisibleId = ((GridLayoutManager) layoutManager)
                    .findLastCompletelyVisibleItemPosition();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            int[] into = new int[((StaggeredGridLayoutManager) layoutManager).getSpanCount()];
            ((StaggeredGridLayoutManager) layoutManager).findLastCompletelyVisibleItemPositions(into);
            lastCompleteVisibleId = findMax(into);
        } else {
            lastCompleteVisibleId = ((LinearLayoutManager) layoutManager)
                    .findLastCompletelyVisibleItemPosition();
        }
        return lastCompleteVisibleId;
    }

    private void onComplete() {
        postDelayed(() -> mMoreRefreshState = MORE_STATE_NORMAL, 1000);
    }

    @Override
    public void onScrollStateChanged(int state) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) getLayoutManager();
        if (layoutManager != null) {
            int firstVisible = layoutManager.findFirstVisibleItemPosition();
            int lastVisible = layoutManager.findLastVisibleItemPosition();
            int visibleItemCount = lastVisible - firstVisible;
            if (lastVisible <= 0) {
                visibleItemCount = 0;
            }
            if (visibleItemCount != 0) {
                if (mUserVisibleListener != null) {
                    for (int i = firstVisible; i <=lastVisible; i++) {
                        if (!lastExpose.contains(i)){
                            mUserVisibleListener.onUserVisible(i);
                        }
                    }
                    lastExpose.clear();
                    for (int i = firstVisible; i <=lastVisible; i++) {
                        lastExpose.add(i);
                    }
                }
            }
        }

        if (state == RecyclerView.SCROLL_STATE_DRAGGING) {
            return;
        }

        mLastScrollTime = SystemClock.elapsedRealtime();
        if (!isCanLoadMore()) {
            return;
        }

        if (isLoadMore()) {
            return;
        }

        int lastCompleteVisibleId = getLastCompleteVisibleId();
        if (DEBUG_VIEW) Log.d(TAG, "on scroll changed: st " + state + ", lastId:" + lastCompleteVisibleId + ", savedId: " + mLastVisibleItemIndex
                + ", itemCount:" + myAdapter.getItemCount());

        if (state == RecyclerView.SCROLL_STATE_IDLE) {
            //看到了最后一个就提示加载
            if (lastCompleteVisibleId >= myAdapter.getItemCount() - 4) {
                if (!EnableTwiceLoadMore || mLastVisibleItemIndex == lastCompleteVisibleId) {
                    //如果false，就直接进来了；如果是true则需要本次id和上次id一样才行。
                    if (pullListener != null) {
                        pullListener.onLoadMore();
                    }
                    mMoreRefreshState = MORE_STATE_REFRESHING;
                    //TODO 简单一点；在触发了这个条件以后1s后还原状态位即可保证恢复。
                    onComplete();
                    if (DEBUG_VIEW) Log.d(TAG, "on scroll changed: loading more");
                } else {
                    mMoreRefreshState = MORE_STATE_NORMAL;
                    if (DEBUG_VIEW) Log.d(TAG, "on scroll changed: loading more but not");
                }
            }
            mLastVisibleItemIndex = lastCompleteVisibleId;
        } else if (state == RecyclerView.SCROLL_STATE_SETTLING) {
            if (lastCompleteVisibleId == myAdapter.getItemCount() - 1) { //如果现在已经触底才显示；如果<的话，证明是刚刚刷新的数据还没有
                mMoreRefreshState = MORE_STATE_REFRESHING_INTERMEDIATE;
                mLastVisibleItemIndex = lastCompleteVisibleId; //当触底标记
                if (DEBUG_VIEW) Log.d(TAG, "on scroll changed: show dragging");
            }
        }
    }

    private class PullToRefreshRecyclerViewAdapter extends Adapter<ViewHolder> {
        private final Adapter adapter;

        private PullToRefreshRecyclerViewAdapter(Adapter adapter) {
            this.adapter = adapter;
        }

        public Adapter getAdapter() {
            return adapter;
        }

        @Override
        public int getItemCount() {
            int count = 0;
            if (adapter != null) {
                count += adapter.getItemCount();
            }
            return count;
        }

        @Override
        public int getItemViewType(int position) {
            int adapterCount;
            if (adapter != null) {
                adapterCount = adapter.getItemCount();
                if (position < adapterCount) {
                    return adapter.getItemViewType(position);
                }
            }
            return 0;
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return adapter.onCreateViewHolder(parent, viewType);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (adapter != null) {
                adapter.onBindViewHolder(holder, position);
            }
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position, List<Object> payloads) {
            if (adapter != null) {
                if (payloads.isEmpty()) {
                    adapter.onBindViewHolder(holder, position);
                } else {
                    adapter.onBindViewHolder(holder, position, payloads);
                }
            }
        }

        @Override
        public long getItemId(int position) {
            if (adapter != null) {
                return adapter.getItemId(position);
            }
            return -1;
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
            LayoutManager manager = recyclerView.getLayoutManager();
            if (manager instanceof GridLayoutManager) {
                final GridLayoutManager gridManager = ((GridLayoutManager) manager);
                gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        return 1;
                    }
                });
            }
            adapter.onAttachedToRecyclerView(recyclerView);
        }

        @Override
        public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
            adapter.onDetachedFromRecyclerView(recyclerView);
        }

        @Override
        public void onViewAttachedToWindow(ViewHolder holder) {
            super.onViewAttachedToWindow(holder);
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            if (lp instanceof StaggeredGridLayoutManager.LayoutParams) {
                StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
                p.setFullSpan(true);
            }
            adapter.onViewAttachedToWindow(holder);
        }

        @Override
        public void onViewDetachedFromWindow(ViewHolder holder) {
            adapter.onViewDetachedFromWindow(holder);
        }

        @Override
        public void onViewRecycled(ViewHolder holder) {
            adapter.onViewRecycled(holder);
        }

        @Override
        public boolean onFailedToRecycleView(ViewHolder holder) {
            return adapter.onFailedToRecycleView(holder);
        }

        @Override
        public void unregisterAdapterDataObserver(AdapterDataObserver observer) {
            adapter.unregisterAdapterDataObserver(observer);
        }

        @Override
        public void registerAdapterDataObserver(AdapterDataObserver observer) {
            adapter.registerAdapterDataObserver(observer);
        }

        private class SimpleViewHolder extends ViewHolder {
            private SimpleViewHolder(View itemView) {
                super(itemView);
            }
        }
    }

    private class DataObserver extends AdapterDataObserver {

        @Override
        public void onChanged() {
            if (myAdapter != null) {
                myAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            myAdapter.notifyItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            myAdapter.notifyItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            myAdapter.notifyItemRangeChanged(positionStart, itemCount, payload);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            myAdapter.notifyItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            myAdapter.notifyItemMoved(fromPosition, toPosition);
        }
    }
}



