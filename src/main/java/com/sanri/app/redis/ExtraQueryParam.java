package com.sanri.app.redis;

public class ExtraQueryParam {
    // 作用于 hash ,查某一个 key, 可以使用正则
    private String hashKey;

    // 作用于 List ,范围查询
    private Long begin;
    private Long end;

    public String getHashKey() {
        return hashKey;
    }

    public void setHashKey(String hashKey) {
        this.hashKey = hashKey;
    }

    public Long getBegin() {
        return begin;
    }

    public void setBegin(Long begin) {
        this.begin = begin;
    }

    public Long getEnd() {
        return end;
    }

    public void setEnd(Long end) {
        this.end = end;
    }
}
