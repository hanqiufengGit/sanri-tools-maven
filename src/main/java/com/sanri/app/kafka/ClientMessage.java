package com.sanri.app.kafka;

/**
 * 客户端需要监听哪个主题
 */
public class ClientMessage {
    private String clusterName;
    private String topic;
    private String serializable;
    private String classloader;
    // action : play pause
    private String action;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getSerializable() {
        return serializable;
    }

    public void setSerializable(String serializable) {
        this.serializable = serializable;
    }

    public String getClassloader() {
        return classloader;
    }

    public void setClassloader(String classloader) {
        this.classloader = classloader;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
