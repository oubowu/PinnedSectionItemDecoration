package com.oushangfeng.pinneddemo.adapter;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.oushangfeng.pinneddemo.entitiy.PinnedHeaderEntity;
import com.oushangfeng.pinneddemo.holder.RecyclerViewHolder;
import com.oushangfeng.pinnedsectionitemdecoration.PinnedHeaderNotifyer;

import java.util.List;

/**
 * Created by Oubowu on 2016/7/21 17:40.
 */
public abstract class RecyclerAdapter<T,V extends PinnedHeaderEntity<T>> extends RecyclerView.Adapter<RecyclerViewHolder> implements PinnedHeaderNotifyer {

    public final static int TYPE_DATA = 1;
    public final static int TYPE_SECTION = 2;

    private List<V> mData;

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        // 如果是网格布局，这里处理标签的布局占满一行
        final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            final GridLayoutManager.SpanSizeLookup oldSizeLookup = gridLayoutManager.getSpanSizeLookup();
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (getItemViewType(position) == TYPE_SECTION) {
                        return gridLayoutManager.getSpanCount();
                    }
                    if (oldSizeLookup != null) {
                        return oldSizeLookup.getSpanSize(position);
                    }
                    return 1;
                }
            });
        }
    }

    @Override
    public void onViewAttachedToWindow(RecyclerViewHolder holder) {
        // 如果是瀑布流布局，这里处理标签的布局占满一行
        final ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
        if (lp instanceof StaggeredGridLayoutManager.LayoutParams) {
            final StaggeredGridLayoutManager.LayoutParams slp = (StaggeredGridLayoutManager.LayoutParams) lp;
            slp.setFullSpan(getItemViewType(holder.getLayoutPosition()) == TYPE_SECTION);
        }
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerViewHolder holder = new RecyclerViewHolder(parent.getContext(),
                LayoutInflater.from(parent.getContext()).inflate(getItemLayoutId(viewType), parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        bindData(holder, getItemViewType(position), position, mData.get(position).getData());
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mData.get(position).getType();
    }

    @Override
    public boolean isPinnedHeaderType(int viewType) {
        return viewType == TYPE_SECTION;
    }

    public abstract int getItemLayoutId(int viewType);

    public abstract void bindData(RecyclerViewHolder holder, int viewType, int position, T item);

    public void add(int pos, V item) {
        mData.add(pos, item);
        notifyItemInserted(pos);
    }

    public void delete(int pos) {
        mData.remove(pos);
        notifyItemRemoved(pos);
    }

    public void addMoreData(List<V> data) {
        int startPos = mData.size();
        mData.addAll(data);
        notifyItemRangeInserted(startPos, data.size());
    }

    public List<V> getData() {
        return mData;
    }

    public void setData(List<V> data) {
        mData = data;
        notifyDataSetChanged();
    }

}
