package com.oushangfeng.pinneddemo.entitiy;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.util.List;

/**
 * Created by Oubowu on 2016/7/27 12:59.
 */
public class StockEntity {

    // 振幅榜
    public List<StockInfo> amplitude_list;

    // 跌幅榜
    public List<StockInfo> down_list;

    // 换手率
    public List<StockInfo> change_list;

    // 涨幅榜
    public List<StockInfo> increase_list;

    public static class StockInfo implements MultiItemEntity {

        public static final int TYPE_HEADER = 1;
        public static final int TYPE_DATA = 2;

        private int itemType;

        public String pinnedHeaderName;

        public double rate;
        public String current_price;
        public String stock_code;
        public String stock_name;

        public StockInfo(int itemType) {
            this.itemType = itemType;
        }

        public StockInfo(int itemType, String pinnedHeaderName) {
            this(itemType);
            this.pinnedHeaderName = pinnedHeaderName;
        }

        @Override
        public int getItemType() {
            return itemType;
        }

        public void setItemType(int itemType) {
            this.itemType = itemType;
        }

        public boolean check;

    }

}
