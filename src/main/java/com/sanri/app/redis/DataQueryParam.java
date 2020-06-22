package com.sanri.app.redis;

/**
 * redis 数据查询需要传的参数列表
 */
public class DataQueryParam {
    private String connName;
    private int index;
    private String key;

    private String classloaderName;
    private SerializableChose serializables;

    private ExtraQueryParam extraQueryParam;

    public String getConnName() {
        return connName;
    }

    public void setConnName(String connName) {
        this.connName = connName;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getClassloaderName() {
        return classloaderName;
    }

    public void setClassloaderName(String classloaderName) {
        this.classloaderName = classloaderName;
    }

    public SerializableChose getSerializables() {
        return serializables;
    }

    public void setSerializables(SerializableChose serializables) {
        this.serializables = serializables;
    }

    public ExtraQueryParam getExtraQueryParam() {
        return extraQueryParam;
    }

    public void setExtraQueryParam(ExtraQueryParam extraQueryParam) {
        this.extraQueryParam = extraQueryParam;
    }
}
