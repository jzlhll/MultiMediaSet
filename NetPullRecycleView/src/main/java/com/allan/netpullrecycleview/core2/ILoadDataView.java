package com.allan.netpullrecycleview.core2;

import androidx.annotation.MainThread;
import androidx.annotation.WorkerThread;

/**
 * 架构中的一环：用于Activity、Fragment等界面UI更新代码实现，并且已经给到了主线程
 */
public interface ILoadDataView {
    @WorkerThread
    void onDataSucOnWork(int flag, Object data, Object object);
    @MainThread
    void onDataFail(int flag, String errorMsg);
    @MainThread
    void onNoMoreData();
}
