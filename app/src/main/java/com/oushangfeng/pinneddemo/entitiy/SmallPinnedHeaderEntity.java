package com.oushangfeng.pinneddemo.entitiy;

/**
 * Created by Oubowu on 2016/7/21 17:51.
 * <p>
 * 小标签实体类
 */
public class SmallPinnedHeaderEntity<T> extends PinnedHeaderEntity<T> {

    private String pinnedHeaderName;

    public SmallPinnedHeaderEntity(T data, int type, String pinnedHeaderName) {
        super(data, type);
        this.pinnedHeaderName = pinnedHeaderName;
    }

    public String getPinnedHeaderName() {
        return pinnedHeaderName;
    }

    public void setPinnedHeaderName(String pinnedHeaderName) {
        this.pinnedHeaderName = pinnedHeaderName;
    }
}
