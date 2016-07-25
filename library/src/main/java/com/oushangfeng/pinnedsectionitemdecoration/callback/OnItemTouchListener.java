package com.oushangfeng.pinnedsectionitemdecoration.callback;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Created by Oubowu on 2016/7/24 20:51.
 * <p>
 * 用来处理标签的点击事件，现在仅仅支持单击，将来也许会实现长按和双击事件
 */
public class OnItemTouchListener<T> implements RecyclerView.OnItemTouchListener {

    private GestureDetector mGestureDetector;

    private int mLeft;
    private int mTop;
    private int mRight;

    private int mBottom;

    private boolean mIntercept;

    private OnHeaderClickListener<T> mHeaderClickListener;

    private T mClickHeaderInfo;

    public OnItemTouchListener(Context context, int left, int top, int right, int bottom) {
        mLeft = left;
        mTop = top;
        mRight = right;
        mBottom = bottom;

        GestureListener gestureListener = new GestureListener();
        mGestureDetector = new GestureDetector(context, gestureListener);
    }

    public void setBounds(int left, int top, int right, int bottom) {

        mLeft = left;
        mTop = top;
        mRight = right;
        mBottom = bottom;
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent event) {
        // 这里处理触摸事件来决定是否自己处理事件
        mGestureDetector.onTouchEvent(event);
        return mIntercept;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }

    public void setClickHeaderInfo(T clickHeaderInfo) {
        mClickHeaderInfo = clickHeaderInfo;
    }

    public void setHeaderClickListener(OnHeaderClickListener<T> headerClickListener) {
        mHeaderClickListener = headerClickListener;
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private boolean mDoubleTap;

        @Override
        public boolean onDown(MotionEvent e) {
            if (!mDoubleTap) {
                mIntercept = false;
            } else {
                // 因为双击会在onDoubleTap后再调用onDown，所以这里要忽略第二次防止mIntercept被影响
                mDoubleTap = false;
            }
            return super.onDown(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            // Log.e("TAG", "GestureListener-76行-onLongPress(): ");
            shouldIntercept(e);

            if (mIntercept && mHeaderClickListener != null) {
                // 自己处理点击标签事件
                mHeaderClickListener.onHeaderLongClick(mClickHeaderInfo);
            }

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            // Log.e("TAG", "GestureListener-81行-onSingleTapUp(): ");
            shouldIntercept(e);

            if (mIntercept && mHeaderClickListener != null) {
                // 自己处理点击标签事件
                mHeaderClickListener.onHeaderClick(mClickHeaderInfo);
            }

            return mIntercept;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {

            // Log.e("TAG", "GestureListener-89行-onDoubleTap(): ");

            mDoubleTap = true;
            shouldIntercept(e);

            return mIntercept;
        }
    }

    private void shouldIntercept(MotionEvent e) {
        float downX = e.getX();
        float downY = e.getY();

        // 如果坐标在标签的范围内的话就屏蔽事件，自己处理
        mIntercept = downX >= mLeft && downX <= mRight && downY >= mTop && downY <= mBottom;

        // Log.e("TAG", "OnRecyclerItemTouchListener-110行-judge(): " + (mIntercept ? "屏蔽" : "不屏蔽"));

    }

}
