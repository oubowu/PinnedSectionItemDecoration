package com.oushangfeng.pinnedsectionitemdecoration;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;

import com.oushangfeng.pinnedsectionitemdecoration.callback.OnHeaderClickListener;
import com.oushangfeng.pinnedsectionitemdecoration.callback.OnItemTouchListener;
import com.oushangfeng.pinnedsectionitemdecoration.utils.DividerHelper;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by Oubowu on 2016/7/21 15:38.
 * <p>这个是附在数据布局的小标签，只支持LinearLayoutManager或者GridLayoutManager且一行只有一列的情况，这个比较符合使用场景</p>
 * <p>注意：标签不能设置marginTop，因为往上滚动遮不住真正的标签</p>
 */
public class SmallPinnedHeaderItemDecoration extends RecyclerView.ItemDecoration {

    // 取出Adapter
    private RecyclerView.Adapter mAdapter = null;
    // 标签的id值
    private int mPinnedHeaderId;
    private OnHeaderClickListener mHeaderClickListener;
    private int[] mClickIds;
    private int mDividerId;
    private boolean mEnableDivider;
    private boolean mDisableHeaderClick;
    private Drawable mDrawable;
    // 标签父布局的左间距
    private int mParentPaddingLeft;
    // RecyclerView的左间距
    private int mRecyclerViewPaddingLeft;
    // 标签父布局的顶间距
    private int mParentPaddingTop;
    // RecyclerView的顶间距
    private int mRecyclerViewPaddingTop;
    private int mHeaderLeftMargin;
    private int mHeaderRightMargin;
    private int mHeaderTopMargin;
    private int mHeaderBottomMargin;
    private OnItemTouchListener mItemTouchListener;
    private int mLeft;
    private int mTop;
    private int mRight;
    private int mBottom;
    private View mPinnedHeaderParentView;

    // 缓存某个标签
    private View mPinnedHeaderView = null;

    // 缓存某个标签的位置
    private int mHeaderPosition = -1;

    // 顶部标签的Y轴偏移值
    private int mPinnedHeaderOffset;
    // 用于锁定画布绘制范围
    private Rect mClipBounds;

    private int mFirstVisiblePosition;
    private int mDataPositionOffset;

    private int mPinnedHeaderType;

    private boolean mDisableDrawHeader;

    private RecyclerView mParent;

    private SmallPinnedHeaderItemDecoration(Builder builder) {
        mEnableDivider = builder.enableDivider;
        mHeaderClickListener = builder.headerClickListener;
        mDividerId = builder.dividerId;
        mPinnedHeaderId = builder.pinnedHeaderId;
        mClickIds = builder.clickIds;
        mDisableHeaderClick = builder.disableHeaderClick;
        mPinnedHeaderType = builder.pinnedHeaderType;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

        checkCache(parent);

        if (!mEnableDivider) {
            return;
        }

        if (mDrawable == null) {
            mDrawable = ContextCompat.getDrawable(parent.getContext(), mDividerId != 0 ? mDividerId : R.drawable.divider);
        }

        outRect.set(0, 0, 0, mDrawable.getIntrinsicHeight());

    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {

        if (mEnableDivider) {
            drawDivider(c, parent);
        }

        // 只支持LinearLayoutManager或者GridLayoutManager且一行只有一列的情况，这个比较符合使用场景
        if (parent.getLayoutManager() instanceof GridLayoutManager && ((GridLayoutManager) parent.getLayoutManager()).getSpanCount() > 1) {
            return;
        } else if (parent.getLayoutManager() instanceof StaggeredGridLayoutManager) {
            return;
        }

        // 检测到标签存在的时候，将标签强制固定在RecyclerView顶部
        createPinnedHeader(parent);

        if (!mDisableDrawHeader && mPinnedHeaderView != null && mFirstVisiblePosition >= mHeaderPosition) {
            // 标签相对parent高度加上自身的高度
            final int headerEndAt = mPinnedHeaderParentView.getTop() + mPinnedHeaderParentView.getMeasuredHeight() + mRecyclerViewPaddingTop;
            // 根据xy坐标查找view
            View v = parent.findChildViewUnder(c.getWidth() / 2, headerEndAt + 1);
            if (isPinnedHeader(parent, v) && v.getTop() <= mPinnedHeaderView.getHeight() + mRecyclerViewPaddingTop + mParentPaddingTop) {
                // 如果view是标签的话，那么缓存的标签就要跟随这个真正的标签标签移动了，效果类似于下面的标签把它顶上去一样
                // 得到mPinnedHeaderView为标签跟随移动的位移
                mPinnedHeaderOffset = v.getTop() - (mRecyclerViewPaddingTop + mParentPaddingTop + mPinnedHeaderView.getHeight());
            } else {
                mPinnedHeaderOffset = 0;
            }

            //            Log.e("TAG", "SmallPinnedHeaderItemDecoration-152行-onDraw(): " + v.getTop() + ";" + mPinnedHeaderOffset);

            // 拿到锁定的矩形
            mClipBounds = c.getClipBounds();

            mClipBounds.left = 0;
            mClipBounds.right = parent.getWidth();
            mClipBounds.top = mRecyclerViewPaddingTop + mParentPaddingTop;
            mClipBounds.bottom = parent.getHeight();

            // 重新锁定
            c.clipRect(mClipBounds);

        }
    }

    private void drawDivider(Canvas c, RecyclerView parent) {

        if (mAdapter == null) {
            // checkCache的话RecyclerView未设置之前mAdapter为空
            return;
        }

        // 不让分割线画出界限
        c.clipRect(parent.getPaddingLeft(), parent.getPaddingTop(), parent.getWidth() - parent.getPaddingRight(), parent.getHeight() - parent.getPaddingBottom());

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            DividerHelper.drawBottomAlignItem(c, mDrawable, child, params);
        }
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {

        if (!mDisableDrawHeader && mPinnedHeaderView != null && mFirstVisiblePosition >= mHeaderPosition) {

            c.save();

            mClipBounds.left = mRecyclerViewPaddingLeft + mParentPaddingLeft + mHeaderLeftMargin;
            mClipBounds.right = mClipBounds.left + mPinnedHeaderView.getWidth();
            mClipBounds.top = mRecyclerViewPaddingTop + mParentPaddingTop + mHeaderTopMargin;
            mClipBounds.bottom = mPinnedHeaderOffset + mPinnedHeaderView.getHeight() + mClipBounds.top;

            mItemTouchListener.invalidTopAndBottom(mPinnedHeaderOffset);

            // 取AB交集这个就是标签绘制的范围了
            c.clipRect(mClipBounds, Region.Op.INTERSECT);
            c.translate(mRecyclerViewPaddingLeft + mParentPaddingLeft + mHeaderLeftMargin,
                    mPinnedHeaderOffset + mRecyclerViewPaddingTop + mParentPaddingTop + mHeaderTopMargin);
            mPinnedHeaderView.draw(c);

            c.restore();
        } else if (mItemTouchListener != null) {
            // 不绘制的时候，把头部的偏移值偏移用户点击不到的程度
            mItemTouchListener.invalidTopAndBottom(-1000);
        }
    }

    // 创建标签
    @SuppressWarnings("unchecked")
    private void createPinnedHeader(RecyclerView parent) {

        if (mAdapter == null) {
            // checkCache的话RecyclerView未设置之前mAdapter为空
            return;
        }

        final RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();

        // 获取第一个可见的item位置
        mFirstVisiblePosition = 0;
        int headerPosition;

        if (layoutManager instanceof GridLayoutManager) {
            mFirstVisiblePosition = ((GridLayoutManager) layoutManager).findFirstVisibleItemPosition();
        } else if (layoutManager instanceof LinearLayoutManager) {
            mFirstVisiblePosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
        }

        // 通过第一个部分可见的item位置获取标签的位置
        headerPosition = findPinnedHeaderPosition(mFirstVisiblePosition);

        if (headerPosition >= 0 && mHeaderPosition != headerPosition) {

            // Log.e("TAG", "创建标签");

            // 缓存位置
            mHeaderPosition = headerPosition;
            // 获取此位置的type
            final int viewType = mAdapter.getItemViewType(headerPosition);

            // 手动调用创建出标签
            final RecyclerView.ViewHolder pinnedViewHolder = mAdapter.createViewHolder(parent, viewType);
            mAdapter.bindViewHolder(pinnedViewHolder, headerPosition);

            mPinnedHeaderParentView = pinnedViewHolder.itemView;

            measurePinedHeaderParent(parent);

            measurePinnedHeader();

            mLeft = mRecyclerViewPaddingLeft + mParentPaddingLeft + mHeaderLeftMargin;
            mRight = mPinnedHeaderView.getMeasuredWidth() + mLeft;
            mTop = mRecyclerViewPaddingTop + mParentPaddingTop + mHeaderTopMargin;
            mBottom = mPinnedHeaderView.getMeasuredHeight() + mTop;

            // 位置强制布局在顶部
            mPinnedHeaderView.layout(mLeft, mTop, mRight, mBottom);

            if (mItemTouchListener == null) {
                mItemTouchListener = new OnItemTouchListener(parent.getContext());
                try {
                    final Field field = parent.getClass().getDeclaredField("mOnItemTouchListeners");
                    field.setAccessible(true);
                    final ArrayList<OnItemTouchListener> touchListeners = (ArrayList<OnItemTouchListener>) field.get(parent);
                    touchListeners.add(0, mItemTouchListener);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                    parent.addOnItemTouchListener(mItemTouchListener);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    parent.addOnItemTouchListener(mItemTouchListener);
                }
                if (mHeaderClickListener != null) {
                    mItemTouchListener.setHeaderClickListener(mHeaderClickListener);
                    mItemTouchListener.disableHeaderClick(mDisableHeaderClick);
                }
                mItemTouchListener.setClickBounds(OnItemTouchListener.HEADER_ID, mPinnedHeaderView);
            }
            if (mHeaderClickListener != null) {
                // -1代表是标签的Id
                mItemTouchListener.setClickBounds(OnItemTouchListener.HEADER_ID, mPinnedHeaderView);
                if (mHeaderClickListener != null && mClickIds != null && mClickIds.length > 0) {
                    for (int mClickId : mClickIds) {
                        final View view = mPinnedHeaderView.findViewById(mClickId);
                        if (view != null && view.getVisibility() == View.VISIBLE) {
                            mItemTouchListener.setClickBounds(mClickId, view);
                        }
                    }
                }
                mItemTouchListener.setClickHeaderInfo(mHeaderPosition - mDataPositionOffset);
            }

        }
    }

    public int getDataPositionOffset() {
        return mDataPositionOffset;
    }

    public void setDataPositionOffset(int offset) {
        mDataPositionOffset = offset;
    }

    // 测量标签父布局的宽高
    private void measurePinedHeaderParent(RecyclerView parent) {
        // 1.测量标签的parent
        ViewGroup.LayoutParams parentLp = mPinnedHeaderParentView.getLayoutParams();
        if (parentLp == null) {
            parentLp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            mPinnedHeaderParentView.setLayoutParams(parentLp);
        }
        int heightMode = View.MeasureSpec.getMode(ViewGroup.LayoutParams.WRAP_CONTENT);
        int heightSize = View.MeasureSpec.getSize(parentLp.height);

        switch (heightMode) {
            case View.MeasureSpec.UNSPECIFIED:
                heightMode = View.MeasureSpec.EXACTLY;
                break;
            case View.MeasureSpec.EXACTLY:
                heightMode = View.MeasureSpec.EXACTLY;
                break;
            case View.MeasureSpec.AT_MOST:
            default:
                heightMode = View.MeasureSpec.AT_MOST;
                break;
        }
        int maxHeight = parent.getHeight() - parent.getPaddingTop() - parent.getPaddingBottom();
        heightSize = Math.min(heightSize, maxHeight);
        int ws = View.MeasureSpec.makeMeasureSpec(parent.getWidth() - parent.getPaddingLeft() - parent.getPaddingRight(), View.MeasureSpec.EXACTLY);
        int hs = View.MeasureSpec.makeMeasureSpec(heightSize, heightMode);
        // 强制测量
        mPinnedHeaderParentView.measure(ws, hs);

        mRecyclerViewPaddingLeft = parent.getPaddingLeft();
        mParentPaddingLeft = mPinnedHeaderParentView.getPaddingLeft();

        mRecyclerViewPaddingTop = parent.getPaddingTop();
        mParentPaddingTop = mPinnedHeaderParentView.getPaddingTop();

        if (parentLp instanceof RecyclerView.LayoutParams) {
            mRecyclerViewPaddingLeft += ((RecyclerView.LayoutParams) parentLp).leftMargin;
            mRecyclerViewPaddingTop += ((RecyclerView.LayoutParams) parentLp).topMargin;
        }
    }

    // 测量标签高度
    private void measurePinnedHeader() {

        // 2.测量标签
        mPinnedHeaderView = mPinnedHeaderParentView.findViewById(mPinnedHeaderId);
        // 获取标签的布局属性
        ViewGroup.LayoutParams lp = mPinnedHeaderView.getLayoutParams();
        if (lp == null) {
            lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            mPinnedHeaderView.setLayoutParams(lp);
        }

        if (lp instanceof ViewGroup.MarginLayoutParams) {
            final ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;
            mHeaderLeftMargin = mlp.leftMargin;
            mHeaderRightMargin = mlp.rightMargin;
            mHeaderTopMargin = mlp.topMargin;
            mHeaderBottomMargin = mlp.bottomMargin;
        }

        // 设置高度
        int heightMode = View.MeasureSpec.getMode(lp.height);
        int heightSize = View.MeasureSpec.getSize(lp.height);

        switch (heightMode) {
            case View.MeasureSpec.UNSPECIFIED:
                heightMode = View.MeasureSpec.EXACTLY;
                break;
            case View.MeasureSpec.EXACTLY:
                heightMode = View.MeasureSpec.EXACTLY;
                break;
            case View.MeasureSpec.AT_MOST:
                heightMode = View.MeasureSpec.AT_MOST;
                break;
            default:
                heightMode = View.MeasureSpec.AT_MOST;
                break;
        }

        // 最大高度为mPinnedHeaderParentView的高度减去padding
        int maxHeight = mPinnedHeaderParentView.getMeasuredHeight() - mPinnedHeaderParentView.getPaddingTop() - mPinnedHeaderParentView.getPaddingBottom();
        heightSize = Math.min(heightSize, maxHeight);

        int hs = View.MeasureSpec.makeMeasureSpec(heightSize, heightMode);

        // 设置宽度
        int widthMode = View.MeasureSpec.getMode(lp.width);
        int widthSize = View.MeasureSpec.getSize(lp.width);

        switch (widthMode) {
            case View.MeasureSpec.UNSPECIFIED:
                widthMode = View.MeasureSpec.EXACTLY;
                break;
            case View.MeasureSpec.EXACTLY:
                widthMode = View.MeasureSpec.EXACTLY;
                break;
            case View.MeasureSpec.AT_MOST:
                widthMode = View.MeasureSpec.AT_MOST;
                break;
            default:
                widthMode = View.MeasureSpec.AT_MOST;
                break;
        }

        int maxWidth = mPinnedHeaderParentView.getMeasuredWidth() - mPinnedHeaderParentView.getPaddingLeft() - mPinnedHeaderParentView.getPaddingRight();
        widthSize = Math.min(widthSize, maxWidth);

        int ws = View.MeasureSpec.makeMeasureSpec(widthSize, widthMode);

        // 强制测量
        mPinnedHeaderView.measure(ws, hs);

    }

    // 查找标签的位置
    private int findPinnedHeaderPosition(int fromPosition) {

        for (int position = fromPosition; position >= 0; position--) {
            // 从这个位置开始递减，只要一查到有位置type为标签，立即返回此标签位置
            final int viewType = mAdapter.getItemViewType(position);
            // 检查是否是标签类型
            if (isPinnedViewType(viewType)) {
                // 是标签类型，返回位置
                return position;
            }
        }

        return -1;
    }

    // 检查传入View是否是标签
    private boolean isPinnedHeader(RecyclerView parent, View v) {
        // 获取View在parent中的位置
        final int position = parent.getChildAdapterPosition(v);
        if (position == RecyclerView.NO_POSITION) {
            return false;
        }
        // 获取View的type
        final int viewType = mAdapter.getItemViewType(position);

        // 检查是否是标签类型
        return isPinnedViewType(viewType);
    }

    // 检查是否是标签类型
    private boolean isPinnedViewType(int viewType) {
        return viewType == mPinnedHeaderType;
    }

    // 检查缓存
    private void checkCache(RecyclerView parent) {
        // 取出RecyclerView的适配器

        if (mParent != parent) {
            mParent = parent;
        }

        RecyclerView.Adapter adapter = parent.getAdapter();
        if (mAdapter != adapter) {
            // 适配器有差异，清空缓存
            mPinnedHeaderView = null;
            mHeaderPosition = -1;
            mAdapter = adapter;
            mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    reset();
                }

                @Override
                public void onItemRangeChanged(int positionStart, int itemCount) {
                    super.onItemRangeChanged(positionStart, itemCount);
                    reset();
                }

                @Override
                public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
                    super.onItemRangeChanged(positionStart, itemCount, payload);
                    reset();
                }

                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    super.onItemRangeInserted(positionStart, itemCount);
                    reset();
                }

                @Override
                public void onItemRangeRemoved(int positionStart, int itemCount) {
                    super.onItemRangeRemoved(positionStart, itemCount);
                    reset();
                }

                @Override
                public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                    super.onItemRangeMoved(fromPosition, toPosition, itemCount);
                    reset();
                }
            });
        }
    }

    private void reset() {
        mHeaderPosition = -1;
        mPinnedHeaderView = null;
    }

    public View getPinnedHeaderView() {
        return mPinnedHeaderView;
    }

    public int getPinnedHeaderPosition() {
        return mHeaderPosition;
    }

    /**
     * 是否禁止绘制粘性头部
     *
     * @return true的话不绘制头部
     */
    public boolean isDisableDrawHeader() {
        return mDisableDrawHeader;
    }

    /**
     * 禁止绘制粘性头部
     *
     * @param disableDrawHeader true的话不绘制头部，默认false绘制头部
     */
    public void disableDrawHeader(boolean disableDrawHeader) {
        mDisableDrawHeader = disableDrawHeader;
        if (mParent != null) {
            mParent.invalidateItemDecorations();
        }
    }

    public static class Builder {

        public boolean disableHeaderClick;
        private OnHeaderClickListener headerClickListener;
        private int dividerId;
        private int pinnedHeaderId;
        private boolean enableDivider;
        private int[] clickIds;

        private int pinnedHeaderType;

        /**
         * 构造方法
         *
         * @param pinnedHeaderId   小标签对应的ID
         * @param pinnedHeaderType 粘性标签的类型
         */
        public Builder(int pinnedHeaderId, int pinnedHeaderType) {
            this.pinnedHeaderId = pinnedHeaderId;
            this.pinnedHeaderType = pinnedHeaderType;
        }

        /**
         * 设置标签和其内部的子控件的监听，若设置点击监听不为null，但是disableHeaderClick(true)的话，还是不会响应点击事件
         *
         * @param headerClickListener 监听，若不设置这个setClickIds无效
         * @return 构建者
         */
        public Builder setHeaderClickListener(OnHeaderClickListener headerClickListener) {
            this.headerClickListener = headerClickListener;
            return this;
        }

        /**
         * 设置分隔线资源ID
         *
         * @param dividerId 资源ID，若不设置这个并且enableDivider=true时，使用默认的分隔线
         * @return 构建者
         */
        public Builder setDividerId(int dividerId) {
            this.dividerId = dividerId;
            return this;
        }

        /**
         * 是否开启绘制分隔线，默认关闭
         *
         * @param enableDivider true为绘制，false不绘制，false时setDividerId无效
         * @return 构建者
         */
        public Builder enableDivider(boolean enableDivider) {
            this.enableDivider = enableDivider;
            return this;
        }

        /**
         * 通过传入包括标签和其内部的子控件的ID设置其对应的点击事件
         *
         * @param clickIds 标签或其内部的子控件的ID
         * @return 构建者
         */
        public Builder setClickIds(int... clickIds) {
            this.clickIds = clickIds;
            return this;
        }

        /**
         * 是否关闭标签点击事件，默认开启
         *
         * @param disableHeaderClick true为关闭标签点击事件，false为开启标签点击事件
         * @return 构建者
         */
        public Builder disableHeaderClick(boolean disableHeaderClick) {
            this.disableHeaderClick = disableHeaderClick;
            return this;
        }

        public SmallPinnedHeaderItemDecoration create() {
            return new SmallPinnedHeaderItemDecoration(this);
        }
    }

}
