package com.sanri.app.redis;

import org.apache.commons.lang3.tuple.Pair;
import redis.clients.jedis.HostAndPort;

import java.util.ArrayList;
import java.util.List;

public class RedisNode {
    private String id;
    private HostAndPort hostAndPort;
    private String role;
    private String master;

    // 槽位列表,只有集群模式才会有
    private int slotStart;
    private int slotEnd;

    public HostAndPort getHostAndPort() {
        return hostAndPort;
    }

    public void setHostAndPort(HostAndPort hostAndPort) {
        this.hostAndPort = hostAndPort;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public int getSlotStart() {
        return slotStart;
    }

    public void setSlotStart(int slotStart) {
        this.slotStart = slotStart;
    }

    public int getSlotEnd() {
        return slotEnd;
    }

    public void setSlotEnd(int slotEnd) {
        this.slotEnd = slotEnd;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
