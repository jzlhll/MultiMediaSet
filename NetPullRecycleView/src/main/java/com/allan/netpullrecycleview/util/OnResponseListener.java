package com.allan.netpullrecycleview.util;

/**
 * @Author allan.jiang
 * @Date: 2022/07/18 16:50
 * @Description
 */
public interface OnResponseListener {
    void onResponseSuccess(Object var1, int var2, Object var3);

    void onResponseFailed(int var1, String var2, String var3, Object var4);
}
