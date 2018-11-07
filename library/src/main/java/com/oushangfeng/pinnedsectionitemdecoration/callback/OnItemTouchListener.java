package com.oushangfeng.pinnedsectionitemdecoration.callback;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.oushangfeng.pinnedsectionitemdecoration.entity.ClickBounds;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by Oubowu on 2016/7/24 20:51.
 * <p>用来处理标签的点击事件，现在仅仅支持单击，将来也许会实现长按和双击事件</p>
 */
public class OnItemTouchListener implements RecyclerView.OnItemTouchListener {
    private static final String TAG = "OnItemTouchListener";
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
    // private boolean mDownInside;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView mRecyclerView;

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
    public boolean onInterceptTouchEvent(@NonNull final RecyclerView rv, @NonNull MotionEvent event) {

        if (mRecyclerView != rv) {
            mRecyclerView = rv;
        }
        if (mAdapter != rv.getAdapter()) {
            mAdapter = rv.getAdapter();
        }

        // 这里处理触摸事件来决定是否自己处理事件
        mGestureDetector.setIsLongpressEnabled(true);
        mGestureDetector.onTouchEvent(event);

        return mIntercept;
    }

    @Override
    public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        Log.i(TAG, "onTouchEvent(): " + e.toString());
        mGestureDetector.onTouchEvent(e);
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

        // Log.i(TAG, " xy坐标: " + downX + ";" + downY);

        for (int i = 0; i < mBoundsArray.size(); i++) {
            // 逐个View拿出，判断坐标是否落在View的范围里面
            final ClickBounds bounds = mBoundsArray.valueAt(i);
            // Log.i(TAG, "  逐个View拿出: " + bounds.toString());

            boolean inside = downX >= bounds.getLeft() && downX <= bounds.getRight() && downY >= bounds.getTop() && downY <= bounds.getBottom();
            if (inside) {
                // Log.i(TAG, "  在点击范围内: " + inside);
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
            } else if (mTmpBounds == null) {
                mIntercept = false;
            }
        }

        if (mIntercept) {
            // 有点击中的，取出其id并清空mTmpBounds
            mTmpClickId = mBoundsArray.keyAt(mBoundsArray.indexOfValue(mTmpBounds));
            mTmpView = mTmpBounds.getView();
            mTmpBounds = null;
            // Log.i(TAG, " 有点击中的: " + mTmpView);
        }

        // Log.i(TAG, "OnRecyclerItemTouchListener-judge(): " + (mIntercept ? "屏蔽" : "不屏蔽"));

    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {

            Log.i(TAG, "GestureListener-onDown(): ");

            shouldIntercept(e);

            return super.onDown(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            Log.i(TAG, "GestureListener-onLongPress(): ");
            shouldIntercept(e);

            if (!mDisableHeaderClick && mIntercept && mHeaderClickListener != null && mAdapter != null && mPosition <= mAdapter.getItemCount() - 1) {
                // 自己处理点击标签事件
                try {
                    mHeaderClickListener.onHeaderLongClick(mTmpView, mTmpClickId, mPosition);
                } catch (IndexOutOfBoundsException e1) {
                    e1.printStackTrace();
                    Log.i(TAG, "GestureListener-onLongPress(): " + e1);
                }
            }

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Log.i(TAG, "GestureListener-onSingleTapUp(): ");
            shouldIntercept(e);

            if (!mDisableHeaderClick && mIntercept && mHeaderClickListener != null && mAdapter != null && mPosition <= mAdapter.getItemCount() - 1) {
                // 自己处理点击标签事件
                try {
                    mHeaderClickListener.onHeaderClick(mTmpView, mTmpClickId, mPosition);
                } catch (IndexOutOfBoundsException e1) {
                    e1.printStackTrace();
                }
            }

            return mIntercept;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {

            Log.i(TAG, "GestureListener-onDoubleTap(): ");

            shouldIntercept(e);

            if (!mDisableHeaderClick && mIntercept && mHeaderClickListener != null && mAdapter != null && mPosition <= mAdapter.getItemCount() - 1) {
                // 自己处理点击标签事件
                try {
                    mHeaderClickListener.onHeaderClick(mTmpView, mTmpClickId, mPosition);
                } catch (IndexOutOfBoundsException e1) {
                    e1.printStackTrace();
                }
            }

            // 有机型在调用onDoubleTap后会接着调用onLongPress，这里这样处理
            mGestureDetector.setIsLongpressEnabled(false);

            return mIntercept;

        }

    }

}
