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

import com.oushangfeng.pinnedsectionitemdecoration.callback.OnHeaderClickListener;
import com.oushangfeng.pinnedsectionitemdecoration.callback.OnItemTouchListener;
import com.oushangfeng.pinnedsectionitemdecoration.callback.PinnedHeaderNotifyer;

/**
 * Created by Oubowu on 2016/7/21 15:38.
 * <p>
 * 这个是单独一个布局的标签
 * <p>
 * porting from https://github.com/takahr/pinned-section-item-decoration
 * <p>
 * 注意：标签所在最外层布局不能设置marginTop，因为往上滚动遮不住真正的标签;marginBottom还有问题待解决
 */
public class PinnedHeaderItemDecoration<T> extends RecyclerView.ItemDecoration {

    private RecyclerView.Adapter mAdapter;

    // 缓存的标签
    private View mPinnedHeaderView;
    // 缓存的标签位置
    int mPinnedHeaderPosition = -1;

    // 顶部标签的Y轴偏移值
    private int mPinnedHeaderOffset;

    // 用于锁定画布绘制范围
    private Rect mClipBounds;

    // 父布局的左间距
    private int mRecyclerViewPaddingLeft;
    // 父布局的顶间距
    private int mRecyclerViewPaddingTop;

    private int mHeaderLeftMargin;
    private int mHeaderTopMargin;
    private int mHeaderRightMargin;
    private int mHeaderBottomMargin;

    // 用于处理头部点击事件屏蔽与响应
    private OnItemTouchListener mItemTouchListener;

    private int mLeft;
    private int mTop;
    private int mRight;
    private int mBottom;

    private OnHeaderClickListener<T> mHeaderClickListener;

    public PinnedHeaderItemDecoration() {
    }

    /**
     * 构造方法
     *
     * @param headerClickListener 头部点击的监听
     */
    public PinnedHeaderItemDecoration(OnHeaderClickListener<T> headerClickListener) {
        mHeaderClickListener = headerClickListener;
    }

    // 当我们调用mRecyclerView.addItemDecoration()方法添加decoration的时候，RecyclerView在绘制的时候，去会绘制decorator，即调用该类的onDraw和onDrawOver方法，

    // 1.onDraw方法先于drawChildren

    // 2.onDrawOver在drawChildren之后，一般我们选择复写其中一个即可。

    // 3.getItemOffsets 可以通过outRect.set()为每个Item设置一定的偏移量，主要用于绘制Decorator。


    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {

        // 检测到标签存在的时候，将标签强制固定在顶部
        createPinnedHeader(parent);

        if (mPinnedHeaderView != null) {

            mClipBounds = c.getClipBounds();
            // getTop拿到的是它的原点(它自身的padding值包含在内)相对parent的顶部距离，加上它的高度后就是它的底部所处的位置
            final int headEnd = mPinnedHeaderView.getTop() + mPinnedHeaderView.getHeight();
            // 根据坐标查找view，headEnd + 1找到的就是mPinnedHeaderView底部下面的view
            final View belowView = parent.findChildViewUnder(c.getWidth() / 2, headEnd + 0.00001f);
            if (isPinnedHeader(parent, belowView)) {
                // 如果是标签的话，缓存的标签就要同步跟此标签移动
                // 根据belowView相对顶部距离计算出缓存标签的位移
                mPinnedHeaderOffset = belowView.getTop() - (mRecyclerViewPaddingTop + mPinnedHeaderView.getHeight() + mHeaderTopMargin);
                // 锁定的矩形顶部为v.getTop(趋势是mPinnedHeaderView.getHeight()->0)
                mClipBounds.top = belowView.getTop();
            } else {
                mPinnedHeaderOffset = 0;
                mClipBounds.top = mRecyclerViewPaddingTop + mPinnedHeaderView.getHeight();
            }
            // 锁定画布绘制范围，记为A
            c.clipRect(mClipBounds);
        }

    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (mPinnedHeaderView != null) {
            c.save();

            mItemTouchListener.setBounds(mLeft, mTop, mRight, mClipBounds.top);

            mClipBounds.top = mRecyclerViewPaddingTop + mHeaderTopMargin;
            // 锁定画布绘制范围，记为B
            // REVERSE_DIFFERENCE，实际上就是求得的B和A的差集范围，即B－A，只有在此范围内的绘制内容才会被显示
            // 因此,只绘制(0,0,parent.getWidth(),belowView.getTop())这个范围，然后画布移动了mPinnedHeaderTop，所以刚好是绘制顶部标签移动的范围
            // 低版本不行，换回Region.Op.UNION并集
            c.clipRect(mClipBounds, Region.Op.UNION);
            c.translate(mRecyclerViewPaddingLeft + mHeaderLeftMargin, mPinnedHeaderOffset + mRecyclerViewPaddingTop + mHeaderTopMargin);
            mPinnedHeaderView.draw(c);


            c.restore();
        }
    }

    /**
     * 查找到view对应的位置从而判断出是否标签类型
     *
     * @param parent
     * @param view
     * @return
     */
    private boolean isPinnedHeader(RecyclerView parent, View view) {
        final int position = parent.getChildAdapterPosition(view);
        if (position == RecyclerView.NO_POSITION) {
            return false;
        }
        final int type = mAdapter.getItemViewType(position);
        return isPinnedHeaderType(type);
    }

    /**
     * 创建标签强制固定在顶部
     *
     * @param parent
     */
    @SuppressWarnings("unchecked")
    private void createPinnedHeader(RecyclerView parent) {
        // 检查缓存
        checkCache(parent);

        final RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();

        // 获取第一个可见的item位置
        int firstVisiblePosition = findFirstVisiblePosition(layoutManager);

        // 获取标签的位置，
        int pinnedHeaderPosition = findPinnedHeaderPosition(firstVisiblePosition);
        if (pinnedHeaderPosition >= 0 && mPinnedHeaderPosition != pinnedHeaderPosition) {

            // 标签位置有效并且和缓存的位置不同
            mPinnedHeaderPosition = pinnedHeaderPosition;
            // 获取标签的type
            final int type = mAdapter.getItemViewType(mPinnedHeaderPosition);

            // 手动调用创建标签
            final RecyclerView.ViewHolder holder = mAdapter.createViewHolder(parent, type);
            mAdapter.bindViewHolder(holder, mPinnedHeaderPosition);
            // 缓存标签
            mPinnedHeaderView = holder.itemView;

            ViewGroup.LayoutParams lp = mPinnedHeaderView.getLayoutParams();
            if (lp == null) {
                // 标签默认宽度占满parent
                lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                mPinnedHeaderView.setLayoutParams(lp);
            }

            // 对高度进行处理
            int heightMode = View.MeasureSpec.getMode(lp.height);
            int heightSize = View.MeasureSpec.getSize(lp.height);

            if (heightMode == View.MeasureSpec.UNSPECIFIED) {
                heightMode = View.MeasureSpec.EXACTLY;
            }

            mRecyclerViewPaddingLeft = parent.getPaddingLeft();
            int recyclerViewPaddingRight = parent.getPaddingRight();
            mRecyclerViewPaddingTop = parent.getPaddingTop();
            int recyclerViewPaddingBottom = parent.getPaddingBottom();

            if (lp instanceof ViewGroup.MarginLayoutParams) {
                final ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;
                mHeaderLeftMargin = mlp.leftMargin;
                mHeaderTopMargin = mlp.topMargin;
                mHeaderRightMargin = mlp.rightMargin;
                mHeaderBottomMargin = mlp.bottomMargin;
            }

            // 最大高度为RecyclerView的高度减去padding
            final int maxHeight = parent.getHeight() - mRecyclerViewPaddingTop - recyclerViewPaddingBottom;
            // 不能超过maxHeight
            heightSize = Math.min(heightSize, maxHeight);

            // 因为标签默认宽度占满parent，所以宽度强制为RecyclerView的宽度减去padding
            final int widthSpec = View.MeasureSpec
                    .makeMeasureSpec(parent.getWidth() - mRecyclerViewPaddingLeft - recyclerViewPaddingRight - mHeaderLeftMargin - mHeaderRightMargin,
                            View.MeasureSpec.EXACTLY);
            final int heightSpec = View.MeasureSpec.makeMeasureSpec(heightSize, heightMode);
            // 强制测量
            mPinnedHeaderView.measure(widthSpec, heightSpec);

            mLeft = mRecyclerViewPaddingLeft + mHeaderLeftMargin;
            mTop = mRecyclerViewPaddingTop + mHeaderTopMargin;
            mRight = mPinnedHeaderView.getMeasuredWidth() + mRecyclerViewPaddingLeft + mHeaderLeftMargin + mHeaderRightMargin;
            mBottom = mPinnedHeaderView.getMeasuredHeight() + mRecyclerViewPaddingTop + mHeaderTopMargin + mHeaderBottomMargin;

            // 位置强制布局在顶部
            mPinnedHeaderView.layout(mLeft, mTop, mRight - mHeaderRightMargin, mBottom - mHeaderBottomMargin);

            if (mItemTouchListener == null) {
                mItemTouchListener = new OnItemTouchListener<T>(parent.getContext(), mLeft, mTop, mRight, mBottom);
                parent.addOnItemTouchListener(mItemTouchListener);
                if (mHeaderClickListener != null) {
                    mItemTouchListener.setHeaderClickListener(mHeaderClickListener);
                }
            }
            if (mHeaderClickListener != null) {
                mItemTouchListener.setClickHeaderInfo(((PinnedHeaderNotifyer) mAdapter).getPinnedHeaderInfo(mPinnedHeaderPosition));
            }

        }

    }

    /**
     * 从传入位置递减找出标签的位置
     *
     * @param formPosition
     * @return
     */
    private int findPinnedHeaderPosition(int formPosition) {

        for (int position = formPosition; position >= 0; position--) {
            // 位置递减，只要查到位置是标签，立即返回此位置
            final int type = mAdapter.getItemViewType(position);
            if (isPinnedHeaderType(type)) {
                return position;
            }
        }

        return 0;
    }

    /**
     * 通过适配器告知类型是否为标签
     *
     * @param type
     * @return
     */
    private boolean isPinnedHeaderType(int type) {
        return ((PinnedHeaderNotifyer) mAdapter).isPinnedHeaderType(type);
    }

    /**
     * 找出第一个可见的Item的位置
     *
     * @param layoutManager
     * @return
     */
    private int findFirstVisiblePosition(RecyclerView.LayoutManager layoutManager) {
        int firstVisiblePosition = 0;
        if (layoutManager instanceof GridLayoutManager) {
            firstVisiblePosition = ((GridLayoutManager) layoutManager).findFirstVisibleItemPosition();
        } else if (layoutManager instanceof LinearLayoutManager) {
            firstVisiblePosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            int[] into = new int[((StaggeredGridLayoutManager) layoutManager).getSpanCount()];
            ((StaggeredGridLayoutManager) layoutManager).findFirstVisibleItemPositions(into);
            firstVisiblePosition = Integer.MAX_VALUE;
            for (int pos : into) {
                firstVisiblePosition = Math.min(pos, firstVisiblePosition);
            }
        }
        return firstVisiblePosition;
    }

    /**
     * 检查缓存
     *
     * @param parent
     */
    private void checkCache(RecyclerView parent) {
        final RecyclerView.Adapter adapter = parent.getAdapter();
        if (mAdapter != adapter) {
            // 适配器为null或者不同，清空缓存
            mPinnedHeaderView = null;
            mPinnedHeaderPosition = -1;
            // 明确了适配器必须继承PinnedHeaderNotifyer接口，因为没有这个就获取不到哪个位置对应的类型是标签类型
            if (adapter instanceof PinnedHeaderNotifyer) {
                mAdapter = adapter;
            } else {
                throw new IllegalStateException("Adapter must implements " + PinnedHeaderNotifyer.class.getSimpleName());
            }
        }
    }

}
