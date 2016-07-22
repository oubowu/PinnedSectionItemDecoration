package com.oushangfeng.pinnedsectionitemdecoration;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Region;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Oubowu on 2016/7/21 15:38.
 * <p>
 * 这个是附在数据View的标签，只支持LinearLayoutManager或者GridLayoutManager且一行只有一列的情况，这个比较符合使用场景
 * <p>
 * 注意：标签不能设置marginTop，因为往上滚动遮不住真正的标签
 */
public class SmallPinnedHeaderItemDecoration extends RecyclerView.ItemDecoration {

    // 标签的id值
    private int mSmallPinnedHeaderId;

    // 标签父布局的左间距
    private int mParentPaddingLeft;
    // RecyclerView的左间距
    private int mRecyclerViewPaddingLeft;
    // 标签父布局的顶间距
    private int mParentPaddingTop;
    // RecyclerView的顶间距
    private int mRecyclerViewPaddingTop;

    private int mHeaderLeftMargin;
    private int mHeaderTopMargin;


    /**
     * 构造方法
     *
     * @param smallPinnedHeaderId 的标签的Id
     */
    public SmallPinnedHeaderItemDecoration(int smallPinnedHeaderId) {
        mSmallPinnedHeaderId = smallPinnedHeaderId;
    }

    // 取出Adapter
    RecyclerView.Adapter mAdapter = null;

    private View mPinnedHeaderParentView;
    // 缓存某个标签
    private View mPinnedHeaderView = null;
    // 缓存某个标签的位置
    private int mHeaderPosition = -1;

    // 顶部标签的Y轴偏移值
    private int mPinnedHeaderOffset;

    // 用于锁定画布绘制范围
    private Rect mClipBounds;

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {

        // 只支持LinearLayoutManager或者GridLayoutManager且一行只有一列的情况，这个比较符合使用场景
        if (parent.getLayoutManager() instanceof GridLayoutManager && ((GridLayoutManager) parent.getLayoutManager()).getSpanCount() > 1) {
            return;
        } else if (parent.getLayoutManager() instanceof StaggeredGridLayoutManager) {
            return;
        }

        // 检测到标签存在的时候，将标签强制固定在RecyclerView顶部
        createPinnedHeader(parent);

        if (mPinnedHeaderView != null) {
            // 标签相对parent高度加上自身的高度
            final int headerEndAt = mPinnedHeaderParentView.getTop() + mPinnedHeaderParentView.getMeasuredHeight() + mRecyclerViewPaddingTop;
            // 根据xy坐标查找view
            View v = parent.findChildViewUnder(c.getWidth() / 2, headerEndAt + 0.00001f);
            if (isHeaderView(parent, v) && v.getTop() <= mPinnedHeaderView.getHeight() + mRecyclerViewPaddingTop + mParentPaddingTop) {
                // 如果view是标签的话，那么缓存的标签就要跟随这个真正的标签标签移动了，效果类似于下面的标签把它顶上去一样
                // 得到mPinnedHeaderView为标签跟随移动的位移
                mPinnedHeaderOffset = v.getTop() - (mRecyclerViewPaddingTop + mParentPaddingTop + mPinnedHeaderView.getHeight());
            } else {
                mPinnedHeaderOffset = 0;
            }

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

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {

        if (mPinnedHeaderView != null) {

            c.save();

            mClipBounds.left = mRecyclerViewPaddingLeft + mParentPaddingLeft + mHeaderLeftMargin;
            mClipBounds.right = mRecyclerViewPaddingLeft + mParentPaddingLeft + mHeaderLeftMargin + mPinnedHeaderView.getWidth();
            mClipBounds.top = mRecyclerViewPaddingTop + mParentPaddingTop + mHeaderTopMargin;
            mClipBounds.bottom = mPinnedHeaderOffset + mPinnedHeaderView.getHeight() + mRecyclerViewPaddingTop + mParentPaddingTop + mHeaderTopMargin;
            ;

            // 取AB交集这个就是标签绘制的范围了
            c.clipRect(mClipBounds, Region.Op.INTERSECT);
            c.translate(mRecyclerViewPaddingLeft + mParentPaddingLeft + mHeaderLeftMargin,
                    mPinnedHeaderOffset + mRecyclerViewPaddingTop + mParentPaddingTop + mHeaderTopMargin);
            mPinnedHeaderView.draw(c);

            c.restore();
        }
    }

    // 创建标签
    @SuppressWarnings("unchecked")
    private void createPinnedHeader(RecyclerView parent) {

        // 检查缓存
        checkCache(parent);

        final RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();

        // 获取第一个可见的item位置
        int firstVisiblePosition = 0;
        int headerPosition;

        if (layoutManager instanceof GridLayoutManager) {
            firstVisiblePosition = ((GridLayoutManager) layoutManager).findFirstVisibleItemPosition();
        } else if (layoutManager instanceof LinearLayoutManager) {
            firstVisiblePosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
        }

        // 通过第一个部分可见的item位置获取标签的位置
        headerPosition = findPinnedHeaderPosition(firstVisiblePosition);

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

            // 位置强制在顶部
            mPinnedHeaderView.layout(mRecyclerViewPaddingLeft + mParentPaddingLeft + mHeaderLeftMargin, mRecyclerViewPaddingTop + mParentPaddingTop + mHeaderTopMargin,
                    mPinnedHeaderView.getMeasuredWidth() + mRecyclerViewPaddingLeft + mParentPaddingLeft + mHeaderLeftMargin,
                    mPinnedHeaderView.getMeasuredHeight() + mRecyclerViewPaddingTop + mParentPaddingTop + mHeaderTopMargin);

        }
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

        mRecyclerViewPaddingLeft = /*mPinnedHeaderParentView.getPaddingLeft() +*/ parent.getPaddingLeft();
        mParentPaddingLeft = mPinnedHeaderParentView.getPaddingLeft();

        mRecyclerViewPaddingTop = /*mPinnedHeaderParentView.getPaddingTop() +*/ parent.getPaddingTop();
        mParentPaddingTop = mPinnedHeaderParentView.getPaddingTop();

        if (parentLp instanceof RecyclerView.LayoutParams) {
            mRecyclerViewPaddingLeft += ((RecyclerView.LayoutParams) parentLp).leftMargin;
            mRecyclerViewPaddingTop += ((RecyclerView.LayoutParams) parentLp).topMargin;
        }
    }

    // 测量标签高度
    private void measurePinnedHeader() {

        // 2.测量标签
        mPinnedHeaderView = mPinnedHeaderParentView.findViewById(mSmallPinnedHeaderId);
        // 获取标签的布局属性
        ViewGroup.LayoutParams lp = mPinnedHeaderView.getLayoutParams();
        if (lp == null) {
            lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            mPinnedHeaderView.setLayoutParams(lp);
        }

        // 设置高度
        int heightMode = View.MeasureSpec.getMode(lp.height);
        int heightSize = View.MeasureSpec.getSize(lp.height);

        if (heightMode == View.MeasureSpec.UNSPECIFIED) {
            heightMode = View.MeasureSpec.EXACTLY;
        }

        if (lp instanceof ViewGroup.MarginLayoutParams) {
            final ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;
            mHeaderLeftMargin = mlp.leftMargin;
            mHeaderTopMargin = mlp.topMargin;
        }

        // 最大高度为RecyclerView的高度减去padding
        int maxHeight = mPinnedHeaderParentView.getHeight() - mPinnedHeaderParentView.getPaddingTop() - mPinnedHeaderParentView.getPaddingBottom();
        heightSize = Math.min(heightSize, maxHeight);

        int hs = View.MeasureSpec.makeMeasureSpec(heightSize, heightMode);
        // 强制测量
        mPinnedHeaderView.measure(ViewGroup.LayoutParams.WRAP_CONTENT, hs);
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
    private boolean isHeaderView(RecyclerView parent, View v) {
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
        return ((PinnedHeaderNotifyer) mAdapter).isPinnedHeaderType(viewType);
    }

    // 检查缓存
    private void checkCache(RecyclerView parent) {
        // 取出RecyclerView的适配器
        RecyclerView.Adapter adapter = parent.getAdapter();
        if (mAdapter != adapter) {
            // 适配器有差异，清空缓存
            disableCache();
            if (adapter instanceof PinnedHeaderNotifyer) {
                // 明确了适配器必须继承PinnedHeaderAdapter接口，因为没有这个就获取不到RecyclerView哪个是标签
                mAdapter = adapter;
            } else {
                throw new IllegalStateException("Adapter must implements PinnedHeaderNotifyer");
            }
        }
    }

    // 清空缓存
    private void disableCache() {
        mPinnedHeaderView = null;
        mHeaderPosition = -1;
    }

}
