package com.sanri.app.dtos.kafka;

public class BrokerInfo {
    private int id;
    private String host;
    private int port;
    private int jxmPort;

    public BrokerInfo() {
    }

    public BrokerInfo(int id, String host, int port, int jxmPort) {
        this.id = id;
        this.host = host;
        this.port = port;
        this.jxmPort = jxmPort;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getJxmPort() {
        return jxmPort;
    }

    public void setJxmPort(int jxmPort) {
        this.jxmPort = jxmPort;
    }
}
