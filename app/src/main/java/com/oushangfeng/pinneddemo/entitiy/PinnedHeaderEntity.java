package com.oushangfeng.pinneddemo.entitiy;

/**
 * Created by Oubowu on 2016/7/21 17:51.
 * <p>
 * 大标签实体类，可以将自己想要填充的数据包装进去，同时附带这个数据对应的类型
 */
public class PinnedHeaderEntity<T> {

    private T data;

    private int type;

    private String pinnedHeaderName;

    public PinnedHeaderEntity(T data, int type, String pinnedHeaderName) {
        this.data = data;
        this.type = type;
        this.pinnedHeaderName = pinnedHeaderName;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setPinnedHeaderName(String pinnedHeaderName) {
        this.pinnedHeaderName = pinnedHeaderName;
    }

    public T getData() {
        return data;
    }

    public int getType() {
        return type;
    }

    public String getPinnedHeaderName() {
        return pinnedHeaderName;
    }
}
