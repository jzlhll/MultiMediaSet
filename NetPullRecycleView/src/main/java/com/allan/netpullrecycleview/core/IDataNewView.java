package com.allan.netpullrecycleview.core;

import androidx.annotation.MainThread;
import androidx.annotation.WorkerThread;

import java.util.List;

/**
 * author     ：zhonglun.jzl
 * date       ：Created in 2021/11/18 8:39 下午
 * description：core2的架构中指代界面用于显示
 */
public interface IDataNewView<T> {
    @WorkerThread
    void onDataSuccess(List<T> data, int pageNum);
    @MainThread
    void onDataError(String errorMsg, int pageNum);
    @MainThread
    void onDataNoMore();
}
