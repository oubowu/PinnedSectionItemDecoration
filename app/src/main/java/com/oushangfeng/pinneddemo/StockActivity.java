package com.oushangfeng.pinneddemo;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.oushangfeng.pinneddemo.adapter.StockAdapter;
import com.oushangfeng.pinneddemo.entitiy.StockEntity;
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
    private StockAdapter mAdapter;

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

                final OnHeaderClickAdapter<StockEntity.StockInfo> clickAdapter = new OnHeaderClickAdapter<StockEntity.StockInfo>() {

                    @Override
                    public void onHeaderClick(int id, int position, StockEntity.StockInfo data) {
                        switch (id) {
                            case R.id.fl:
                                // case OnItemTouchListener.HEADER_ID:
                                Toast.makeText(StockActivity.this, "click, tag: " + data.pinnedHeaderName, Toast.LENGTH_SHORT).show();
                                break;
                            case R.id.iv_more:
                                Toast.makeText(StockActivity.this, "click " + data.pinnedHeaderName + "'s more button", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }

                };

                mRecyclerView.addItemDecoration(new PinnedHeaderItemDecoration.Builder<StockEntity.StockInfo>().setDividerId(R.drawable.divider).enableDivider(true)
                        .setClickIds(R.id.iv_more, R.id.fl).disableHeaderClick(true).setHeaderClickListener(clickAdapter).create());

            }

            @Override
            protected String doInBackground(Void... voids) {
                return getStrFromAssets("rasking.json");
            }

            @Override
            protected void onPostExecute(String result) {

                Gson gson = new Gson();
                
                final StockEntity stockEntity = gson.fromJson(result, StockEntity.class);

                List<StockEntity.StockInfo> data = new ArrayList<>();

                data.add(new StockEntity.StockInfo(StockEntity.StockInfo.TYPE_HEADER, "涨幅榜"));
                for (StockEntity.StockInfo info : stockEntity.increase_list) {
                    info.setItemType(StockEntity.StockInfo.TYPE_DATA);
                    data.add(info);
                }

                data.add(new StockEntity.StockInfo(StockEntity.StockInfo.TYPE_HEADER, "跌幅榜"));
                for (StockEntity.StockInfo info : stockEntity.down_list) {
                    info.setItemType(StockEntity.StockInfo.TYPE_DATA);
                    data.add(info);
                }

                data.add(new StockEntity.StockInfo(StockEntity.StockInfo.TYPE_HEADER, "换手率"));
                for (StockEntity.StockInfo info : stockEntity.change_list) {
                    info.setItemType(StockEntity.StockInfo.TYPE_DATA);
                    data.add(info);
                }

                data.add(new StockEntity.StockInfo(StockEntity.StockInfo.TYPE_HEADER, "振幅榜"));
                for (StockEntity.StockInfo info : stockEntity.amplitude_list) {
                    info.setItemType(StockEntity.StockInfo.TYPE_DATA);
                    data.add(info);
                }

                mAdapter = new StockAdapter(data);
                mRecyclerView.setAdapter(mAdapter);

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

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

}
