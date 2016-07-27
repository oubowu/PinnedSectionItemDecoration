package com.oushangfeng.pinnedsectionitemdecoration.callback;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.oushangfeng.pinnedsectionitemdecoration.entity.ClickBounds;

/**
 * Created by Oubowu on 2016/7/24 20:51.
 * <p>
 * 用来处理标签的点击事件，现在仅仅支持单击，将来也许会实现长按和双击事件
 */
public class OnItemTouchListener<T> implements RecyclerView.OnItemTouchListener {

    /**
     * 代表的是标签的Id
     */
    public static final int HEADER_ID = -1;

    private int mLastClickId;

    private GestureDetector mGestureDetector;

    private SparseArray<ClickBounds> mBounds;

    private boolean mIntercept;

    private OnHeaderClickListener<T> mHeaderClickListener;

    private T mClickHeaderInfo;

    private int mPosition;

    private boolean mDisableHeaderClick;

    public OnItemTouchListener(Context context) {

        mBounds = new SparseArray<>();

        GestureListener gestureListener = new GestureListener();
        mGestureDetector = new GestureDetector(context, gestureListener);
    }

    /**
     * 设置对应的View的点击范围
     *
     * @param id     View的ID
     * @param bounds 点击范围
     */
    public void setViewAndBounds(int id, ClickBounds bounds) {
        mBounds.put(id, bounds);
    }

    /**
     * 更新点击范围的顶部和底部
     *
     * @param offset 偏差
     */
    public void invalidTopAndBottom(int offset) {
        for (int i = 0; i < mBounds.size(); i++) {
            final ClickBounds bounds = mBounds.valueAt(i);
            bounds.setTop(bounds.getFirstTop() + offset);
            bounds.setBottom(bounds.getFirstBottom() + offset);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent event) {
        // 这里处理触摸事件来决定是否自己处理事件
        mGestureDetector.setIsLongpressEnabled(true);
        mGestureDetector.onTouchEvent(event);
        return mIntercept;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }

    public void setClickHeaderInfo(int position, T clickHeaderInfo) {
        mPosition = position;
        mClickHeaderInfo = clickHeaderInfo;
    }

    public void setHeaderClickListener(OnHeaderClickListener<T> headerClickListener) {
        mHeaderClickListener = headerClickListener;
    }

    public void disableHeaderClick(boolean disableHeaderClick) {
        mDisableHeaderClick = disableHeaderClick;
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private boolean mDoubleTap;

        @Override
        public boolean onDown(MotionEvent e) {

            // Log.e("TAG", "GestureListener-78行-onDown(): ");

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
                if ((mLastClickId == HEADER_ID && !mDisableHeaderClick) || mLastClickId != HEADER_ID) {
                    // 如果点击的是标签整体并且没有禁掉标签整体点击响应，或者点击的是标签里面的某一个子控件，回调事件
                    mHeaderClickListener.onHeaderLongClick(mLastClickId, mPosition, mClickHeaderInfo);
                }
            }

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            // Log.e("TAG", "GestureListener-81行-onSingleTapUp(): ");
            shouldIntercept(e);

            return mIntercept;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            // Log.e("TAG", "GestureListener-113行-onSingleTapConfirmed(): ");

            if (mIntercept && mHeaderClickListener != null) {
                // 自己处理点击标签事件
                if ((mLastClickId == HEADER_ID && !mDisableHeaderClick) || mLastClickId != HEADER_ID) {
                    // 如果点击的是标签整体并且没有禁掉标签整体点击响应，或者点击的是标签里面的某一个子控件，回调事件
                    mHeaderClickListener.onHeaderClick(mLastClickId, mPosition, mClickHeaderInfo);
                }
            }

            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {

            // Log.e("TAG", "GestureListener-89行-onDoubleTap(): ");

            mDoubleTap = true;
            shouldIntercept(e);

            if (mIntercept && mHeaderClickListener != null) {
                // 自己处理点击标签事件
                if ((mLastClickId == HEADER_ID && !mDisableHeaderClick) || mLastClickId != HEADER_ID) {
                    // 如果点击的是标签整体并且没有禁掉标签整体点击响应，或者点击的是标签里面的某一个子控件，回调事件
                    mHeaderClickListener.onHeaderDoubleClick(mLastClickId, mPosition, mClickHeaderInfo);
                }
            }

            // 有机型在调用onDoubleTap后会接着调用onLongPress，这里这样处理
            mGestureDetector.setIsLongpressEnabled(false);

            return mIntercept;
        }

    }

    private void shouldIntercept(MotionEvent e) {
        float downX = e.getX();
        float downY = e.getY();

        // 如果坐标在标签的范围内的话就屏蔽事件，自己处理
        //  mIntercept = downX >= mLeft && downX <= mRight && downY >= mTop && downY <= mBottom;

        boolean tmp;

        for (int i = 0; i < mBounds.size(); i++) {
            // 逐个View拿出，判断坐标是否落在View的范围里面
            final int id = mBounds.keyAt(i);
            final ClickBounds bounds = mBounds.valueAt(i);
            tmp = downX >= bounds.getLeft() && downX <= bounds.getRight() && downY >= bounds.getTop() && downY <= bounds.getBottom();
            if (tmp) {
                mLastClickId = id;
                mIntercept = true;
            }
        }

        // Log.e("TAG", "OnRecyclerItemTouchListener-110行-judge(): " + (mIntercept ? "屏蔽" : "不屏蔽"));

    }

}
