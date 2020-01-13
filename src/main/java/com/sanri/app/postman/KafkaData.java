package com.sanri.app.postman;

public class KafkaData implements Comparable<KafkaData> {
    private Long offset;
    private Object data;

    public KafkaData(Long offset, Object data) {
        this.offset = offset;
        this.data = data;
    }

    public KafkaData() {
    }

    public Long getOffset() {
        return offset;
    }

    public void setOffset(Long offset) {
        this.offset = offset;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public int compareTo(KafkaData o) {
        return o.offset.compareTo(this.offset);
    }
}
