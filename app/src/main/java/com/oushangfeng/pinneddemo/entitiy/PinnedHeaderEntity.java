package com.oushangfeng.pinneddemo.entitiy;

/**
 * Created by Oubowu on 2016/7/21 17:51.
 */
public class PinnedHeaderEntity<T> {

    private T data;

    private int type;

    public PinnedHeaderEntity(T data, int type) {
        this.data = data;
        this.type = type;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
