package com.oushangfeng.pinneddemo;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.widget.Toast;

import com.google.gson.Gson;
import com.oushangfeng.pinneddemo.adapter.RecyclerAdapter;
import com.oushangfeng.pinneddemo.entitiy.PinnedHeaderEntity;
import com.oushangfeng.pinneddemo.entitiy.StockEntity;
import com.oushangfeng.pinneddemo.holder.RecyclerViewHolder;
import com.oushangfeng.pinnedsectionitemdecoration.PinnedHeaderItemDecoration;
import com.oushangfeng.pinnedsectionitemdecoration.callback.OnHeaderClickAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class StockActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerAdapter<StockEntity.StockInfo, PinnedHeaderEntity<StockEntity.StockInfo>> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        new AsyncTask<Void, Void, String>() {

            @Override
            protected void onPreExecute() {

                mRecyclerView.setLayoutManager(new LinearLayoutManager(StockActivity.this, LinearLayoutManager.VERTICAL, false));
                mRecyclerView.addItemDecoration(new PinnedHeaderItemDecoration.Builder<StockEntity.StockInfo>().enableDivider(true)
                        .setHeaderClickListener(new OnHeaderClickAdapter<StockEntity.StockInfo>() {
                            @Override
                            public void onHeaderClick(int position, StockEntity.StockInfo data) {
                                Toast.makeText(StockActivity.this, "标签是：" + mAdapter.getData().get(position).getPinnedHeaderName(), Toast.LENGTH_SHORT).show();
                            }
                        }).create());

                mAdapter = new RecyclerAdapter<StockEntity.StockInfo, PinnedHeaderEntity<StockEntity.StockInfo>>() {
                    @Override
                    public int getItemLayoutId(int viewType) {
                        switch (viewType) {
                            case RecyclerAdapter.TYPE_SECTION:
                                return R.layout.item_stock_header;
                            case RecyclerAdapter.TYPE_DATA:
                                return R.layout.item_stock_data;
                        }
                        return 0;
                    }

                    @Override
                    public void bindData(RecyclerViewHolder holder, int viewType, int position, StockEntity.StockInfo item) {
                        switch (viewType) {
                            case RecyclerAdapter.TYPE_SECTION:
                                holder.setText(R.id.tv_stock_name, getData().get(position).getPinnedHeaderName());
                                break;
                            case RecyclerAdapter.TYPE_DATA:

                                final String stockNameAndCode = item.stock_name + "\n" + item.stock_code;
                                SpannableStringBuilder ssb = new SpannableStringBuilder(stockNameAndCode);
                                ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#a4a4a7")), item.stock_name.length(), stockNameAndCode.length(),
                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                ssb.setSpan(new AbsoluteSizeSpan(dip2px(holder.itemView.getContext(), 13)), item.stock_name.length(), stockNameAndCode.length(),
                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                holder.setText(R.id.tv_stock_name_code, ssb);

                                holder.setText(R.id.tv_current_price, item.current_price);
                                holder.setText(R.id.tv_rate, (item.rate < 0 ? String.format("%.2f", item.rate) : "+" + String.format("%.2f", item.rate)) + "%");
                                break;
                        }
                    }
                };

                mRecyclerView.setAdapter(mAdapter);

            }

            @Override
            protected String doInBackground(Void... voids) {
                return getStrFromAssets("rasking.json");
            }

            @Override
            protected void onPostExecute(String result) {

                Gson gson = new Gson();
                final StockEntity stockEntity = gson.fromJson(result, StockEntity.class);

                List<PinnedHeaderEntity<StockEntity.StockInfo>> data = new ArrayList<>();

                data.add(new PinnedHeaderEntity<StockEntity.StockInfo>(null, RecyclerAdapter.TYPE_SECTION, "涨幅榜"));
                for (StockEntity.StockInfo info : stockEntity.increase_list) {
                    data.add(new PinnedHeaderEntity<>(info, RecyclerAdapter.TYPE_DATA, "涨幅榜"));
                }

                data.add(new PinnedHeaderEntity<StockEntity.StockInfo>(null, RecyclerAdapter.TYPE_SECTION, "跌幅榜"));
                for (StockEntity.StockInfo info : stockEntity.down_list) {
                    data.add(new PinnedHeaderEntity<>(info, RecyclerAdapter.TYPE_DATA, "跌幅榜"));
                }

                data.add(new PinnedHeaderEntity<StockEntity.StockInfo>(null, RecyclerAdapter.TYPE_SECTION, "换手率"));
                for (StockEntity.StockInfo info : stockEntity.change_list) {
                    data.add(new PinnedHeaderEntity<>(info, RecyclerAdapter.TYPE_DATA, "换手率"));
                }

                data.add(new PinnedHeaderEntity<StockEntity.StockInfo>(null, RecyclerAdapter.TYPE_SECTION, "振幅榜"));
                for (StockEntity.StockInfo info : stockEntity.amplitude_list) {
                    data.add(new PinnedHeaderEntity<>(info, RecyclerAdapter.TYPE_DATA, "振幅榜"));
                }

                mAdapter.setData(data);
            }

        }.execute();


    }

    /**
     * @return Json数据（String）
     * @description 通过assets文件获取json数据，这里写的十分简单，没做循环判断。
     */
    private String getStrFromAssets(String name) {

        AssetManager assetManager = getAssets();
        try {
            InputStream is = assetManager.open(name);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String str;
            while ((str = br.readLine()) != null) {
                sb.append(str);
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }

    private int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

}
