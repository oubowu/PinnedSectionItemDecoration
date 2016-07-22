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
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.oushangfeng.pinneddemo.adapter.RecyclerAdapter;
import com.oushangfeng.pinneddemo.entitiy.PinnedHeaderEntity;
import com.oushangfeng.pinneddemo.holder.RecyclerViewHolder;
import com.oushangfeng.pinnedsectionitemdecoration.PinnedHeaderItemDecoration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerview;
    private RecyclerAdapter<String, PinnedHeaderEntity<String>> mAdapter;

    private int[] mDogs = {R.mipmap.dog0, R.mipmap.dog1, R.mipmap.dog2, R.mipmap.dog3, R.mipmap.dog4, R.mipmap.dog5, R.mipmap.dog6, R.mipmap.dog7, R.mipmap.dog8};
    private int[] mCats = {R.mipmap.cat0, R.mipmap.cat1, R.mipmap.cat2, R.mipmap.cat3, R.mipmap.cat4, R.mipmap.cat5, R.mipmap.cat6, R.mipmap.cat7, R.mipmap.cat8};
    private int[] mRabbits = {R.mipmap.rabbit0, R.mipmap.rabbit1, R.mipmap.rabbit2, R.mipmap.rabbit3, R.mipmap.rabbit4, R.mipmap.rabbit5, R.mipmap.rabbit6, R.mipmap.rabbit7, R.mipmap.rabbit8};
    private int[] mPandas = {R.mipmap.panda0, R.mipmap.panda1, R.mipmap.panda2, R.mipmap.panda3, R.mipmap.panda4, R.mipmap.panda5, R.mipmap.panda6, R.mipmap.panda7, R.mipmap.panda8};

    private Random mRandom = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("BigPinnedHeader");

        List<PinnedHeaderEntity<String>> data = new ArrayList<>();
        data.add(new PinnedHeaderEntity<>("狗狗", RecyclerAdapter.TYPE_SECTION));
        for (int dog : mDogs) {
            data.add(new PinnedHeaderEntity<>(dog + "", RecyclerAdapter.TYPE_DATA));
        }
        data.add(new PinnedHeaderEntity<>("猫咪", RecyclerAdapter.TYPE_SECTION));
        for (int cat : mCats) {
            data.add(new PinnedHeaderEntity<>(cat + "", RecyclerAdapter.TYPE_DATA));
        }
        data.add(new PinnedHeaderEntity<>("兔子", RecyclerAdapter.TYPE_SECTION));
        for (int rabbit : mRabbits) {
            data.add(new PinnedHeaderEntity<>(rabbit + "", RecyclerAdapter.TYPE_DATA));
        }
        data.add(new PinnedHeaderEntity<>("熊猫", RecyclerAdapter.TYPE_SECTION));
        for (int panda : mPandas) {
            data.add(new PinnedHeaderEntity<>(panda + "", RecyclerAdapter.TYPE_DATA));
        }

        mAdapter = new RecyclerAdapter<String, PinnedHeaderEntity<String>>() {
            @Override
            public int getItemLayoutId(int viewType) {
                switch (viewType) {
                    case RecyclerAdapter.TYPE_SECTION:
                        return R.layout.item_pinned_header;
                    case RecyclerAdapter.TYPE_DATA:
                        return R.layout.item_data;
                }
                return 0;
            }

            @Override
            public void bindData(RecyclerViewHolder holder, int viewType, int position, String item) {
                switch (viewType) {
                    case RecyclerAdapter.TYPE_SECTION:
                        holder.setText(R.id.tv_animal, item);
                        break;
                    case RecyclerAdapter.TYPE_DATA:
                        if (mRecyclerview.getLayoutManager() instanceof StaggeredGridLayoutManager) {
                            final ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
                            lp.height = dip2px(MainActivity.this, mRandom.nextInt(101) + 80);
                        }
                        holder.setText(R.id.tv_pos, position + "");
                        Glide.with(MainActivity.this).load(Integer.parseInt(item)).into(holder.getImageView(R.id.iv_animal));
                        break;
                }
            }
        };
        mAdapter.setData(data);

        mRecyclerview = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerview.addItemDecoration(new PinnedHeaderItemDecoration());
        mRecyclerview.setAdapter(mAdapter);
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
                mRecyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
                mAdapter.notifyDataSetChanged();
                break;
            case R.id.grid_layout:
                mRecyclerview.setLayoutManager(new GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false));
                mAdapter.onAttachedToRecyclerView(mRecyclerview);
                mAdapter.notifyDataSetChanged();
                break;
            case R.id.staggered_grid_layout:
                mRecyclerview.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
                mAdapter.notifyDataSetChanged();
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
