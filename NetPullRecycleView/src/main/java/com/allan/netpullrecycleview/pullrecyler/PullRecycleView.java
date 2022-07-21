package com.allan.netpullrecycleview.pullrecyler;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class PullRecycleView extends RecyclerView {
    protected static final boolean DEBUG_VIEW = false;
    protected static final String TAG = "PullRecycleView";

    public static final int MORE_STATE_NORMAL = 0;
    public static final int MORE_STATE_REFRESHING_INTERMEDIATE = 1;
    public static final int MORE_STATE_REFRESHING = 2;
    public static PullSysConfig mSysConfig;
    private Context mContext;

    private final AdapterDataObserver dataObserver = new DataObserver();
    private PullToRefreshRecyclerViewAdapter myAdapter;
    private IPullListener pullListener;
    private RefreshMoreView mMoreRefreshView;//上拉刷新view
    private int mMoreRefreshState = MORE_STATE_NORMAL;
    private boolean isCanLoadMore = false;//上拉

    private boolean isNoMoreData = false;
    public final void setNoMoreData() {
        isNoMoreData = true;
    }

    public final void hasMoreData() {
        isNoMoreData = false;
        mMoreRefreshView.post(new Runnable() {
            @Override
            public void run() {
                mMoreRefreshView.onResultResetMore();
            }
        });
    }

    public static void setPullSysConfig(PullSysConfig config) {
        mSysConfig = config;
    }

    private final boolean isExistLoadMoreView() {
        return mMoreRefreshView != null;
    }

    public final boolean isCanLoadMore() {
        return isCanLoadMore && isExistLoadMoreView() && !isNoMoreData;
    }

    //是否正在加载更多
    public final boolean isLoadMore() {
        return mMoreRefreshState == MORE_STATE_REFRESHING && isExistLoadMoreView();
    }

    //记录上次最全可见的最后一个item的位置
    private int mLastVisibleItemIndex = -1;
    //todo 是否每次都需要拉2次才能加载
    private static final boolean EnableTwiceLoadMore = true;

    //设置底部刷新是否可用
    public PullRecycleView setUseLoadMore(boolean loadMore) {
        if (isExistLoadMoreView()) {
            int visib = mMoreRefreshView.getVisibility();
            if (visib != (loadMore ? VISIBLE : GONE)) {
                mMoreRefreshView.setVisibility(loadMore ? VISIBLE : GONE);
            }
        }
        isCanLoadMore = loadMore;
        return this;
    }

    //底部刷新控件
    public PullRecycleView setMoreRefreshView(RefreshMoreView moreRefreshView) {
        this.mMoreRefreshView = moreRefreshView;
        return this;
    }

    //上下拉刷新监听
    public PullRecycleView setIPullListener(IPullListener pullListener) {
        this.pullListener = pullListener;
        return this;
    }

    public PullRecycleView setPullLayoutManager(LayoutManager layout) {
        setLayoutManager(layout);
        return this;
    }

    //设置适配器
    public void build(Adapter adapter) {
        setAdapter(adapter);
    }

    public PullRecycleView(Context context) {
        this(context, null);
    }

    public PullRecycleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullRecycleView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        initPullConfig();
        setUseLoadMore(isCanLoadMore());
    }

    private void initPullConfig() {
        if (mSysConfig != null) {
            try {
                Class<? extends RefreshMoreView> moreCls = mSysConfig.getMoreViewClass();
                if (moreCls != null) {
                    Constructor cst = moreCls.getDeclaredConstructor(Context.class);
                    mMoreRefreshView = (RefreshMoreView) cst.newInstance(mContext);
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
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
                        return myAdapter.isLoadMoreFooter(position) ? gridManager.getSpanCount() : 1;
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

    @Override
    public void onScrollStateChanged(int state) {
        if (state == RecyclerView.SCROLL_STATE_DRAGGING) {
            return;
        }

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
            if (lastCompleteVisibleId == myAdapter.getItemCount() - 1) {
                if (!EnableTwiceLoadMore || mLastVisibleItemIndex == lastCompleteVisibleId) {
                    //如果false，就直接进来了；如果是true则需要本次id和上次id一样才行。
                    mMoreRefreshView.onLoadingMore();
                    if (pullListener != null) {
                        pullListener.onLoadMore();
                    }
                    mMoreRefreshState = MORE_STATE_REFRESHING;
                    if (DEBUG_VIEW) Log.d(TAG, "on scroll changed: loading more");
                } else {
                    mMoreRefreshView.onNormalState();
                    mMoreRefreshState = MORE_STATE_NORMAL;
                    if (DEBUG_VIEW) Log.d(TAG, "on scroll changed: loading more but not");
                }
            }
            mLastVisibleItemIndex = lastCompleteVisibleId;
        } else if (state == RecyclerView.SCROLL_STATE_SETTLING) {
            if (lastCompleteVisibleId == myAdapter.getItemCount() - 1) { //如果现在已经触底才显示；如果<的话，证明是刚刚刷新的数据还没有
                mMoreRefreshView.onLoadingDragging();
                mMoreRefreshState = MORE_STATE_REFRESHING_INTERMEDIATE;
                mLastVisibleItemIndex = lastCompleteVisibleId; //当触底标记
                if (DEBUG_VIEW) Log.d(TAG, "on scroll changed: show dragging");
            }
        }
    }

    public static final int ON_COMPLETE_STATE_SUC = 0;
    public static final int ON_COMPLETE_STATE_FAIL = 1;
    public static final int ON_COMPLETE_STATE_NO_MORE = 2;
    /**
     * 上下拉完成
     *
     * @param st 下拉或上滑是否成功,或者是否后台已经没有数据
     */
    public void onComplete(int st) {
//        if (!isLoadMore() || st == ON_COMPLETE_STATE_NO_MORE) {
//            return;
//        }
        //因为可能数据请求比较快。会提前进来，滑动比加载慢也是合理的。

        switch (st) {
            case ON_COMPLETE_STATE_NO_MORE:
                mMoreRefreshView.onResultNoMore();
                break;
            case ON_COMPLETE_STATE_FAIL:
                mMoreRefreshView.onResultFail();
                break;
            case ON_COMPLETE_STATE_SUC:
                mMoreRefreshView.onResultSuccess();
                break;
        }

        mMoreRefreshView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mMoreRefreshState = MORE_STATE_NORMAL;
                mMoreRefreshView.onNormalState();//不能加保护
            }
        }, 600);
    }

    private class PullToRefreshRecyclerViewAdapter extends Adapter<ViewHolder> {
        private static final int TYPE_REFRESH_HEADER = 10000;//头部下拉刷新类型
        private static final int TYPE_LOAD_MORE_FOOTER = 10001;//底部加载更多类型
        private final Adapter adapter;

        private PullToRefreshRecyclerViewAdapter(Adapter adapter) {
            this.adapter = adapter;
        }

        public Adapter getAdapter() {
            return adapter;
        }

        private boolean isLoadMoreFooter(int position) {
            return isExistLoadMoreView() && position == getItemCount() - 1;
        }

        /**
         * 判断是否是PullToRefreshRecyclerView保留的itemViewType
         */
        private boolean isReservedItemViewType(int itemViewType) {
            return itemViewType == TYPE_REFRESH_HEADER || itemViewType == TYPE_LOAD_MORE_FOOTER;
        }

        @Override
        public int getItemCount() {
            int count = 0;

            if (isExistLoadMoreView()) {
                count++;
            }

            if (adapter != null) {
                count += adapter.getItemCount();
            }
            return count;
        }

        @Override
        public int getItemViewType(int position) {
            if (isLoadMoreFooter(position)) {
                return TYPE_LOAD_MORE_FOOTER;
            }
            int adapterCount;
            if (adapter != null) {
                adapterCount = adapter.getItemCount();
                if (position < adapterCount) {
                    int type = adapter.getItemViewType(position);
                    if (isReservedItemViewType(type)) {
                        throw new IllegalStateException("PullToRefreshRecyclerView require itemViewType in adapter should be less than 10000 ");
                    }
                    return type;
                }
            }
            return 0;
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_LOAD_MORE_FOOTER) {
                return new SimpleViewHolder(mMoreRefreshView);
            }
            return adapter.onCreateViewHolder(parent, viewType);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (isLoadMoreFooter(position)) {
                return;
            }
            if (adapter != null) {
                adapter.onBindViewHolder(holder, position);
            }
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position, List<Object> payloads) {
            if (isLoadMoreFooter(position)) {
                return;
            }
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
            if (isLoadMoreFooter(position)) {
                return -1;
            }
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
                        return (isLoadMoreFooter(position)) ? gridManager.getSpanCount() : 1;
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
            if (lp instanceof StaggeredGridLayoutManager.LayoutParams && isLoadMoreFooter(holder.getLayoutPosition())) {
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

