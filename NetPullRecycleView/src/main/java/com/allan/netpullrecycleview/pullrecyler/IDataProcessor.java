package com.allan.netpullrecycleview.pullrecyler;

import java.util.List;

public interface IDataProcessor<T> {
    void setData(List<T> data);
    void appendData(List<T> data);
    void clearData();
    void noMoreData();
}
