package com.oushangfeng.pinneddemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.oushangfeng.pinneddemo.adapter.BaseHeaderAdapter;
import com.oushangfeng.pinneddemo.entitiy.PinnedHeaderEntity;
import com.oushangfeng.pinnedsectionitemdecoration.PinnedHeaderItemDecoration;
import com.oushangfeng.pinnedsectionitemdecoration.callback.OnHeaderClickListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;

    private BaseHeaderAdapter<PinnedHeaderEntity<Integer>> mAdapter;

    private int[] mDogs = {R.mipmap.dog0, R.mipmap.dog1, R.mipmap.dog2, R.mipmap.dog3, R.mipmap.dog4, R.mipmap.dog5, R.mipmap.dog6, R.mipmap.dog7, R.mipmap.dog8};
    private int[] mCats = {R.mipmap.cat0, R.mipmap.cat1, R.mipmap.cat2, R.mipmap.cat3, R.mipmap.cat4, R.mipmap.cat5, R.mipmap.cat6, R.mipmap.cat7, R.mipmap.cat8};
    private int[] mRabbits = {R.mipmap.rabbit0, R.mipmap.rabbit1, R.mipmap.rabbit2, R.mipmap.rabbit3, R.mipmap.rabbit4, R.mipmap.rabbit5, R.mipmap.rabbit6, R.mipmap.rabbit7, R.mipmap.rabbit8};
    private int[] mPandas = {R.mipmap.panda0, R.mipmap.panda1, R.mipmap.panda2, R.mipmap.panda3, R.mipmap.panda4, R.mipmap.panda5, R.mipmap.panda6, R.mipmap.panda7, R.mipmap.panda8};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("BigPinnedHeader");

        List<PinnedHeaderEntity<Integer>> data = new ArrayList<>();
        data.add(new PinnedHeaderEntity<>(0, BaseHeaderAdapter.TYPE_HEADER, "Dog"));
        for (int dog : mDogs) {
            data.add(new PinnedHeaderEntity<>(dog, BaseHeaderAdapter.TYPE_DATA, "Dog"));
        }
        data.add(new PinnedHeaderEntity<>(0, BaseHeaderAdapter.TYPE_HEADER, "Cat"));
        for (int cat : mCats) {
            data.add(new PinnedHeaderEntity<>(cat, BaseHeaderAdapter.TYPE_DATA, "Cat"));
        }
        data.add(new PinnedHeaderEntity<>(0, BaseHeaderAdapter.TYPE_HEADER, "Rabbit"));
        for (int rabbit : mRabbits) {
            data.add(new PinnedHeaderEntity<>(rabbit, BaseHeaderAdapter.TYPE_DATA, "Rabbit"));
        }
        data.add(new PinnedHeaderEntity<>(0, BaseHeaderAdapter.TYPE_HEADER, "Panda"));
        for (int panda : mPandas) {
            data.add(new PinnedHeaderEntity<>(panda, BaseHeaderAdapter.TYPE_DATA, "Panda"));
        }

        mAdapter = new BaseHeaderAdapter<PinnedHeaderEntity<Integer>>(data) {

            private SparseIntArray mRandomHeights;

            @Override
            protected void addItemTypes() {
                addItemType(BaseHeaderAdapter.TYPE_HEADER, R.layout.item_pinned_header);
                addItemType(BaseHeaderAdapter.TYPE_DATA, R.layout.item_data);
            }

            @Override
            protected void convert(BaseViewHolder holder, PinnedHeaderEntity<Integer> item) {
                switch (holder.getItemViewType()) {
                    case BaseHeaderAdapter.TYPE_HEADER:
                        holder.setText(R.id.tv_animal, item.getPinnedHeaderName());
                        break;
                    case BaseHeaderAdapter.TYPE_DATA:

                        int position = holder.getLayoutPosition();

                        if (mRecyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager) {
                            // 瀑布流布局记录随机高度，就不会导致Item由于高度变化乱跑，导致画分隔线出现问题
                            // 随机高度, 模拟瀑布效果.

                            if (mRandomHeights == null) {
                                mRandomHeights = new SparseIntArray(getItemCount());
                            }

                            if (mRandomHeights.get(position) == 0) {
                                mRandomHeights.put(position, dip2px(MainActivity.this, (int) (100 + Math.random() * 100)));
                            }

                            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
                            lp.height = mRandomHeights.get(position);
                            holder.itemView.setLayoutParams(lp);

                        }

                        holder.setText(R.id.tv_pos, position + "");
                        Glide.with(MainActivity.this).load(item.getData()).into((ImageView) holder.getView(R.id.iv_animal));
                        break;
                }
            }

        };


        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void SimpleOnItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                switch (mAdapter.getItemViewType(i)) {
                    case BaseHeaderAdapter.TYPE_DATA:
                        PinnedHeaderEntity<Integer> entity = mAdapter.getData().get(i);
                        Toast.makeText(MainActivity.this, entity.getPinnedHeaderName() + ", position " + i + ", id " + entity.getData(), Toast.LENGTH_SHORT).show();
                        break;
                    case BaseHeaderAdapter.TYPE_HEADER:
                        entity = mAdapter.getData().get(i);
                        Toast.makeText(MainActivity.this, "click, tag: " + entity.getPinnedHeaderName(), Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        OnHeaderClickListener headerClickListener = new OnHeaderClickListener() {
            @Override
            public void onHeaderClick(View view, int id, int position) {
                Toast.makeText(MainActivity.this, "click, tag: " + mAdapter.getData().get(position).getPinnedHeaderName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onHeaderLongClick(View view, int id, int position) {
                Toast.makeText(MainActivity.this, "long click, tag: " + mAdapter.getData().get(position).getPinnedHeaderName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onHeaderDoubleClick(View view, int id, int position) {
                Toast.makeText(MainActivity.this, "double click, tag: " + mAdapter.getData().get(position).getPinnedHeaderName(), Toast.LENGTH_SHORT).show();
            }
        };
        mRecyclerView.addItemDecoration(new PinnedHeaderItemDecoration.Builder(BaseHeaderAdapter.TYPE_HEADER).setDividerId(R.drawable.divider).enableDivider(true)
                .setHeaderClickListener(headerClickListener).create());

        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.linnear_layout:
                mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
                mAdapter.notifyDataSetChanged();
                break;
            case R.id.grid_layout:
                mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false));
                mAdapter.onAttachedToRecyclerView(mRecyclerView);
                mAdapter.notifyDataSetChanged();
                break;
            case R.id.staggered_grid_layout:
                mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
                mAdapter.notifyDataSetChanged();
                break;
            case R.id.to_stock:
                startActivity(new Intent(this, StockActivity.class));
                break;
            case R.id.to_second:
                startActivity(new Intent(this, SecondActivity.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

}
