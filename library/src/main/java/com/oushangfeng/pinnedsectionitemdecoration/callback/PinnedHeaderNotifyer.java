package com.oushangfeng.pinnedsectionitemdecoration.callback;

/**
 * Created by Oubowu on 2016/7/21 15:44.
 * <p>
 * Recycler的Adapter必须继承此接口来告诉ItemDecoration粘性标签的类型和某个位置粘性标签的信息
 */
public interface PinnedHeaderNotifyer<T> {

    boolean isPinnedHeaderType(int viewType);

    T getPinnedHeaderInfo(int position);

}
