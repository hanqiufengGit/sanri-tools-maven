package com.sanri.app.servlet;

import com.sanri.app.BaseServlet;
import com.sanri.app.redis.RedisKeyResult;
import com.sanri.app.redis.RedisNode;
import com.sanri.app.redis.RedisService;
import com.sanri.frame.RequestMapping;

import java.io.IOException;
import java.util.List;

/**
 * redis 比较重要的监控数据为
 * 查看当前的模式 单机,主从树状结构,集群(每个节点的槽位信息)
 * 查看内存使用
 * 查看连接数,哪些主机占用多少连接
 * 模糊搜索某个 key ,查看 key 的数据,注意集群模式下 key 的搜索
 */
@RequestMapping("/redis")
public class RedisConnectServlet extends BaseServlet {
    RedisService redisService = new RedisService();

    public List<RedisNode> redisNodes(String connName) throws IOException {
        return redisService.redisNodes(connName);
    }

    public List<RedisKeyResult> scan(String connName, int index, String pattern, int cursor, int limit) throws IOException {
        return redisService.scan(connName,index,pattern,cursor,limit);
    }

}
