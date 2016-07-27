package com.oushangfeng.pinnedsectionitemdecoration.callback;

/**
 * Created by Oubowu on 2016/7/24 23:53.
 * <p>
 * 顶部标签点击监听
 */
public interface OnHeaderClickListener<T> {

    void onHeaderClick(int position, T data);

    void onHeaderLongClick(int position, T data);

    void onHeaderDoubleClick(int position, T data);

}
