package com.sanri.app.kafka;

public class ExtendConsumerRecord {
    private Object value;
    private int partition;
    private long offset;
    private String timestamp;

    public ExtendConsumerRecord() {
    }

    public ExtendConsumerRecord(Object value, int partition, long offset, String timestamp) {
        this.value = value;
        this.partition = partition;
        this.offset = offset;
        this.timestamp = timestamp;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public int getPartition() {
        return partition;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
