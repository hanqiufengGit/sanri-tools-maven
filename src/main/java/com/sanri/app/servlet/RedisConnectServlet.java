package com.sanri.app.servlet;

import com.sanri.app.BaseServlet;
import com.sanri.app.redis.*;
import com.sanri.frame.RequestMapping;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

/**
 * redis 比较重要的监控数据为:
 * 查看当前的模式 单机,主从树状结构,集群(每个节点的槽位信息)            已实现
 * 查看内存使用                                                 未实现
 * 查看连接数,哪些主机占用多少连接                                 未实现
 * 模糊搜索某个 key ,查看 key 的数据,注意集群模式下 key 的搜索       已实现
 */
@RequestMapping("/redis")
public class RedisConnectServlet extends BaseServlet {
    RedisService redisService = new RedisService();

    public List<RedisNode> redisNodes(String connName) throws IOException {
        return redisService.redisNodes(connName);
    }

    public List<RedisKeyResult> scan(String connName, int index, String pattern,int limit) throws IOException {
        return redisService.scan(connName,index,pattern,limit);
    }

    public Object data(DataQueryParam dataQueryParam) throws IOException, ClassNotFoundException {
        return redisService.loadData(dataQueryParam);
    }

    /**
     * 查询 List 的数据长度
     * @param connName
     * @param index
     * @param key
     * @return
     */
    public Long listLength(String connName, int index,String key) throws IOException {
        return redisService.listLength(connName,index,key);
    }

    /**
     * 查询 hash 的所有 key 列表
     * @param hashKeysQueryParam
     * @return
     */
    public List<Object> hashKeys(DataQueryParam dataQueryParam) throws IOException {
        return redisService.hashKeys(dataQueryParam);
    }
}
