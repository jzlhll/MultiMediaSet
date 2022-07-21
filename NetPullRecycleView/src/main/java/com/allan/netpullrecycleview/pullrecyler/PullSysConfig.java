package com.allan.netpullrecycleview.pullrecyler;

public final class PullSysConfig {
    private Class<? extends RefreshMoreView> moreViewClass;

    public PullSysConfig moreViewClass(Class<? extends RefreshMoreView> val) {
        moreViewClass = val;
        return this;
    }

    Class<? extends RefreshMoreView> getMoreViewClass() {
        return moreViewClass;
    }
}
