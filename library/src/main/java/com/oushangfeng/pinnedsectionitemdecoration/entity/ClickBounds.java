package com.oushangfeng.pinnedsectionitemdecoration.entity;

import android.view.View;

/**
 * Created by Oubowu on 2016/7/27 23:52.
 * <p>点击范围实体类，用于点击标签时做点击判断</p>
 */
public class ClickBounds {

    private View mView;

    private int mLeft;
    private int mTop;
    private int mRight;
    private int mBottom;

    // 记录第一次Top和Bottom，用于后面减偏差
    private int mFirstTop;
    private int mFirstBottom;

    public ClickBounds(View view, int left, int top, int right, int bottom) {
        mView = view;
        mLeft = left;
        mTop = top;
        mRight = right;
        mBottom = bottom;

        mFirstTop = top;
        mFirstBottom = bottom;
    }

    public void setBounds(int left, int top, int right, int bottom) {
        mLeft = left;
        mTop = top;
        mRight = right;
        mBottom = bottom;

        mFirstTop = top;
        mFirstBottom = bottom;
    }

    public int getLeft() {
        return mLeft;
    }

    public int getTop() {
        return mTop;
    }

    public int getRight() {
        return mRight;
    }

    public int getBottom() {
        return mBottom;
    }

    public void setBottom(int bottom) {
        mBottom = bottom;
    }

    public void setLeft(int left) {
        mLeft = left;
    }

    public void setTop(int top) {
        mTop = top;
    }

    public void setRight(int right) {
        mRight = right;
    }

    public int getFirstBottom() {
        return mFirstBottom;
    }

    public int getFirstTop() {
        return mFirstTop;
    }

    public View getView() {
        return mView;
    }
}
