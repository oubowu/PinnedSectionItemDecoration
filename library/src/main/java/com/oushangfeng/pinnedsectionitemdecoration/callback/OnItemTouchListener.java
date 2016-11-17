package com.oushangfeng.pinnedsectionitemdecoration.callback;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.oushangfeng.pinnedsectionitemdecoration.entity.ClickBounds;

/**
 * Created by Oubowu on 2016/7/24 20:51.
 * <p>用来处理标签的点击事件，现在仅仅支持单击，将来也许会实现长按和双击事件</p>
 */
public class OnItemTouchListener implements RecyclerView.OnItemTouchListener {

    /**
     * 代表的是标签的Id
     */
    public static final int HEADER_ID = -1;

    private ClickBounds mTmpBounds;

    private View mTmpView;

    private int mTmpClickId;

    private GestureDetector mGestureDetector;

    private SparseArray<ClickBounds> mBoundsArray;

    private boolean mIntercept;

    private OnHeaderClickListener mHeaderClickListener;

    private int mPosition;

    private boolean mDisableHeaderClick;
    private boolean mDownInside;
    private RecyclerView.Adapter mAdapter;

    public OnItemTouchListener(Context context) {

        mBoundsArray = new SparseArray<>();

        GestureListener gestureListener = new GestureListener();
        mGestureDetector = new GestureDetector(context, gestureListener);
    }

    /**
     * 设置对应的View的点击范围
     *
     * @param id     View的ID
     * @param bounds 点击范围
     */
    @Deprecated
    public void setClickBounds(int id, ClickBounds bounds) {
        mBoundsArray.put(id, bounds);
    }

    /**
     * 设置对应的View的点击范围
     *
     * @param id   View的ID
     * @param view 点击的View
     */
    public void setClickBounds(int id, View view) {
        ClickBounds bounds;
        if (mBoundsArray.get(id) == null) {
            bounds = new ClickBounds(view, view.getLeft(), view.getTop(), view.getLeft() + view.getMeasuredWidth(), view.getTop() + view.getMeasuredHeight());
            mBoundsArray.put(id, bounds);
        } else {
            bounds = mBoundsArray.get(id);
            bounds.setBounds(view.getLeft(), view.getTop(), view.getLeft() + view.getMeasuredWidth(), view.getTop() + view.getMeasuredHeight());
        }
    }

    /**
     * 更新点击范围的顶部和底部
     *
     * @param offset 偏差
     */
    public void invalidTopAndBottom(int offset) {
        for (int i = 0; i < mBoundsArray.size(); i++) {
            final ClickBounds bounds = mBoundsArray.valueAt(i);
            bounds.setTop(bounds.getFirstTop() + offset);
            bounds.setBottom(bounds.getFirstBottom() + offset);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(final RecyclerView rv, MotionEvent event) {

        if (mAdapter != rv.getAdapter()) {
            mAdapter = rv.getAdapter();
        }

        // 这里处理触摸事件来决定是否自己处理事件
        mGestureDetector.setIsLongpressEnabled(true);
        mGestureDetector.onTouchEvent(event);

        if (event.getAction() == MotionEvent.ACTION_UP && !mIntercept && mDownInside) {
            // 针对在头部滑动然后抬起手指的情况，如果在头部范围内需要拦截
            float downX = event.getX();
            float downY = event.getY();
            final ClickBounds bounds = mBoundsArray.valueAt(0);
            return downX >= bounds.getLeft() && downX <= bounds.getRight() && downY >= bounds.getTop() && downY <= bounds.getBottom();
        }

        return mIntercept;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }

    public void setClickHeaderInfo(int position) {
        mPosition = position;
    }

    public void setHeaderClickListener(OnHeaderClickListener headerClickListener) {
        mHeaderClickListener = headerClickListener;
    }

    public void disableHeaderClick(boolean disableHeaderClick) {
        mDisableHeaderClick = disableHeaderClick;
    }

    private void shouldIntercept(MotionEvent e) {
        float downX = e.getX();
        float downY = e.getY();

        // 如果坐标在标签的范围内的话就屏蔽事件，自己处理
        //  mIntercept = downX >= mLeft && downX <= mRight && downY >= mTop && downY <= mBottom;

        for (int i = 0; i < mBoundsArray.size(); i++) {
            // 逐个View拿出，判断坐标是否落在View的范围里面
            final ClickBounds bounds = mBoundsArray.valueAt(i);
            boolean inside = downX >= bounds.getLeft() && downX <= bounds.getRight() && downY >= bounds.getTop() && downY <= bounds.getBottom();
            if (inside) {
                // 拦截事件成立
                mIntercept = true;
                // 点击范围内
                if (mTmpBounds == null) {
                    mTmpBounds = bounds;
                } else if (bounds.getLeft() >= mTmpBounds.getLeft() && bounds.getRight() <= mTmpBounds.getRight() && bounds.getTop() >= mTmpBounds.getTop() && bounds
                        .getBottom() <= mTmpBounds.getBottom()) {
                    // 与缓存的在点击范围的进行比较，若其点击范围比缓存的更小，它点击响应优先级更高
                    mTmpBounds = bounds;
                }
            }
        }

        if (mIntercept) {
            // 有点击中的，取出其id并清空mTmpBounds
            mTmpClickId = mBoundsArray.keyAt(mBoundsArray.indexOfValue(mTmpBounds));
            mTmpView = mTmpBounds.getView();
            mTmpBounds = null;
        }

        // Log.e("TAG", "OnRecyclerItemTouchListener-110行-judge(): " + (mIntercept ? "屏蔽" : "不屏蔽"));

    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private boolean mDoubleTap;

        @Override
        public boolean onDown(MotionEvent e) {

            // Log.e("TAG", "GestureListener-78行-onDown(): ");

            // 记录手指触碰，是否在头部范围内
            float downX = e.getX();
            float downY = e.getY();
            final ClickBounds bounds = mBoundsArray.valueAt(0);
            mDownInside = downX >= bounds.getLeft() && downX <= bounds.getRight() && downY >= bounds.getTop() && downY <= bounds.getBottom();

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

            if (!mDisableHeaderClick && mIntercept && mHeaderClickListener != null && mAdapter != null && mPosition <= mAdapter.getItemCount() - 1) {
                // 自己处理点击标签事件
                try {
                    mHeaderClickListener.onHeaderLongClick(mTmpView, mTmpClickId, mPosition);
                } catch (IndexOutOfBoundsException e1) {
                    e1.printStackTrace();
                    Log.e("TAG", "GestureListener-156行-onLongPress(): " + e1);
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

            if (!mDisableHeaderClick && mIntercept && mHeaderClickListener != null && mAdapter != null && mPosition <= mAdapter.getItemCount() - 1) {
                // 自己处理点击标签事件
                try {
                    mHeaderClickListener.onHeaderClick(mTmpView, mTmpClickId, mPosition);
                } catch (IndexOutOfBoundsException e1) {
                    e1.printStackTrace();
                    // Log.e("TAG", "GestureListener-183行-onSingleTapConfirmed(): " + e1);
                }
            }

            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {

            // Log.e("TAG", "GestureListener-89行-onDoubleTap(): ");

            mDoubleTap = true;
            shouldIntercept(e);

            if (!mDisableHeaderClick && mIntercept && mHeaderClickListener != null && mAdapter != null && mPosition <= mAdapter.getItemCount() - 1) {
                // 自己处理点击标签事件
                try {
                    mHeaderClickListener.onHeaderDoubleClick(mTmpView, mTmpClickId, mPosition);
                } catch (IndexOutOfBoundsException e1) {
                    e1.printStackTrace();
                    // Log.e("TAG", "GestureListener-207行-onDoubleTap(): " + e1);
                }
            }

            // 有机型在调用onDoubleTap后会接着调用onLongPress，这里这样处理
            mGestureDetector.setIsLongpressEnabled(false);

            return mIntercept;
        }

    }

}
