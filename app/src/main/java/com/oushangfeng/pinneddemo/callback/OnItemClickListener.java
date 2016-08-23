package com.oushangfeng.pinneddemo.callback;

import android.view.View;

/**
 * Created by Oubowu on 2016/7/24 19:01.
 * <p>
 * 点击监听
 */
@Deprecated
public interface OnItemClickListener<T> {

    void onItemClick(View view, T data, int position);

}
