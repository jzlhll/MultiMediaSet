package com.allan.netpullrecycleview.util;

import android.view.View;

/**
 * @Author allan.jiang
 * @Date: 2022/07/18 16:50
 * @Description
 */
public final class ClickViewByTagListener<T> implements View.OnClickListener{
    public interface IOnItemDataClicked<T> {
        void onClickedItem(T bean);
    }

    private final IOnItemDataClicked<T> mClicked;
    public ClickViewByTagListener(IOnItemDataClicked<T> clicked) {
        mClicked = clicked;
    }

    @Override
    public void onClick(View v) {
        if (NoFastClickUtils.isFastClick()) {
            return;
        }

        Object o = v.getTag(R.id.tag_uri_jump);
        if (o == null) {
            return;
        }
        mClicked.onClickedItem((T) o);
    }
}
