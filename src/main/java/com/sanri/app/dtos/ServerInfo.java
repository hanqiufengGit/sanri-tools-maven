package com.sanri.app.dtos;

public class ServerInfo {
    private String host;
    private int port;
    private String requestURI;
    private String sessionId;

    public ServerInfo() {
    }

    public ServerInfo(String host, int port, String requestURI, String sessionId) {
        this.host = host;
        this.port = port;
        this.requestURI = requestURI;
        this.sessionId = sessionId;
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

    public String getRequestURI() {
        return requestURI;
    }

    public void setRequestURI(String requestURI) {
        this.requestURI = requestURI;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
