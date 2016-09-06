package com.oushangfeng.pinnedsectionitemdecoration.callback;

import android.view.View;

/**
 * Created by Oubowu on 2016/7/24 23:53.
 * <p>顶部标签点击监听</p>
 */
public interface OnHeaderClickListener<T> {

    void onHeaderClick(View view, int id, int position, T data);

    void onHeaderLongClick(View view, int id, int position, T data);

    void onHeaderDoubleClick(View view, int id, int position, T data);

}
