package com.sanri.app.redis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.sanri.app.classloader.ClassLoaderManager;
import com.sanri.app.classloader.ExtendClassloader;
import com.sanri.app.serializer.CustomObjectInputStream;
import com.sanri.app.serializer.KryoSerializer;
import com.sanri.app.serializer.StringSerializer;
import com.sanri.app.servlet.FileManagerServlet;
import com.sanri.app.servlet.ZkServlet;
import com.sanri.frame.DispatchServlet;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;
import sanri.utils.NumberUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

public class RedisService {
    private String modul = "redis";
    static Map<String,Jedis> jedisMap = new HashMap<String, Jedis>();
    private ClassLoaderManager classLoaderManager = ClassLoaderManager.getInstance();
    private Logger logger = LoggerFactory.getLogger(RedisService.class);

    /**
     * 获取 jedis 实例
     * @param connName
     * @return
     * @throws IOException
     */
    public Jedis jedis(String connName) throws IOException {
        Jedis jedis = jedisMap.get(connName);
        if(jedis == null){
            FileManagerServlet fileManagerServlet = DispatchServlet.getServlet(FileManagerServlet.class);
            String redisConnInfo = fileManagerServlet.readConfig(modul, connName);

            JSONObject jsonObject = JSONObject.parseObject(redisConnInfo);
            String address = jsonObject.getString("connectStrings");
            String auth = jsonObject.getString("auth");

            String[] split = StringUtils.split(address, ':');
            jedis = new Jedis(split[0], NumberUtil.toInt(split[1]), 1000, 60000);
            if(StringUtils.isNotBlank(auth)){
                jedis.auth(auth);
            }

            jedisMap.put(connName,jedis);
        }
        return jedis;
    }

    /**
     * 查看 redis 模式 local cluster master-slave
     * 连接的节点一定要是 master
     * @param connName
     * @return
     */
    public String mode(String connName) throws IOException {
        Jedis jedis = jedis(connName);
        String info = jedis.info("Cluster");
        Map<String, String> properties = ColonCommandReply.colonCommandReply.parserKeyValue(info);
        String cluster_enabled = properties.get("cluster_enabled");
        if("1".equals(cluster_enabled)){
            return "cluster";
        }
        String replication = jedis.info("Replication");
        properties = ColonCommandReply.colonCommandReply.parserKeyValue(replication);
        String connected_slaves = properties.get("connected_slaves");
        int slaves = NumberUtils.toInt(connected_slaves);
        if(slaves == 0){return "local";}
        return "master-slave";
    }

    /**
     * 查询 redis 节点列表
     * @param connName
     * @return
     */
    public List<RedisNode> redisNodes(String connName) throws IOException {
        Jedis jedis = jedis(connName);
        // 先看是不是集群
        String info = jedis.info("Cluster");
        Map<String, String> properties = ColonCommandReply.colonCommandReply.parserKeyValue(info);
        String cluster_enabled = properties.get("cluster_enabled");
        if("1".equals(cluster_enabled)){
            Client client = jedis.getClient();
            client.clusterNodes();
            String bulkReply = client.getBulkReply();
            List<String []> nodeCommandLines = CommandReply.spaceCommandReply.parser(bulkReply);
            List<RedisNode> redisNodes = nodeCommandLines.stream().map(line -> {
                RedisNode redisNode = new RedisNode();
                redisNode.setId(line[0]);
                redisNode.setHostAndPort(HostAndPort.parseString(line[1]));
                String flags = line[2];
                redisNode.setRole(flags.replace("myself,", ""));
                redisNode.setMaster(line[3]);
                if ("master".equals(redisNode.getRole())) {
                    String slots = line[8];
                    if(slots.contains("-")){
                        String[] split = StringUtils.split(slots, '-');
                        int start = NumberUtils.toInt(split[0]);int end = NumberUtils.toInt(split[1]);
                        redisNode.setSlotStart(start);redisNode.setSlotEnd(end);
                    }else{
                        int around = NumberUtils.toInt(slots);
                        redisNode.setSlotStart(around);redisNode.setSlotEnd(around);
                    }
                }
                return redisNode;
            }).collect(Collectors.toList());
            return redisNodes;
        }

        //如果不是集群模式,看是否为主从模式,获取主从结构的所有节点
        String replication = jedis.info("Replication");
        properties = ColonCommandReply.colonCommandReply.parserKeyValue(replication);
        String connected_slaves = properties.get("connected_slaves");
        if(StringUtils.isNotBlank(connected_slaves)) {
            int slaves = NumberUtils.toInt(connected_slaves);
            if(slaves == 0){
                // 单机模式
                RedisNode redisNode = new RedisNode();
                String host = jedis.getClient().getHost();
                int port = jedis.getClient().getPort();
                redisNode.setId(host+":"+port);
                redisNode.setRole("master");
                redisNode.setHostAndPort(HostAndPort.parseString(redisNode.getId()));
                return Collections.singletonList(redisNode);
            }

            // 否则就是主从模式,级联获取所有节点
            List<RedisNode> redisNodes = new ArrayList<>();
            Client client = jedis.getClient();
            findSlaves(HostAndPort.parseString(client.getHost()+":"+client.getPort()),redisNodes,null);

            return redisNodes;
        }

        return null;
    }

    public List<RedisKeyResult> scan(String connName, int index, String pattern, int limit) throws IOException {
        Jedis jedis = jedis(connName);if(index != 0){jedis.select(index);};
        String mode = mode(connName);

        List<RedisKeyResult> redisKeyResults = new ArrayList<>();

        if("cluster".equals(mode)){
            JedisCluster jedisCluster = getJedisCluster(connName);
            Map<String, JedisPool> clusterNodes = jedisCluster.getClusterNodes();
            Iterator<JedisPool> iterator = clusterNodes.values().iterator();
            List<String> allKeys = new ArrayList<>();
            while (iterator.hasNext()){
                JedisPool jedisPool = iterator.next();
                Jedis current = jedisPool.getResource();
                List<String> partKeys = scan(current, index, pattern, limit);allKeys.addAll(partKeys);
                if(allKeys.size() > limit){break;}
            }
            // 处理完成后的结果数据
            // 处理结果
            for (String item : allKeys) {
                String type = jedisCluster.type(item);
                Long ttl = jedisCluster.ttl(item);
                Long pttl = jedisCluster.ttl(item);
                RedisKeyResult redisKeyResult = new RedisKeyResult(item, type, ttl, pttl);
                RedisType redisType = RedisType.parse(type);
                switch (redisType){
                    case string:
                        redisKeyResult.setLength(jedisCluster.strlen(item));
                        break;
                    case Set:
                    case ZSet:
                    case List:
                        redisKeyResult.setLength(jedisCluster.llen(item));
                        break;
                    case Hash:
                        redisKeyResult.setLength(jedisCluster.hlen(item));
                }
                redisKeyResults.add(redisKeyResult);
            }

            jedisCluster.close();

            return redisKeyResults;
        }

        List<String> keys = scan(jedis, index, pattern, limit);
        // 处理结果
        for (String item : keys) {
            String type = jedis.type(item);
            Long ttl = jedis.ttl(item);
            Long pttl = jedis.ttl(item);
            RedisKeyResult redisKeyResult = new RedisKeyResult(item, type, ttl, pttl);
            RedisType redisType = RedisType.parse(type);
            switch (redisType){
                case string:
                    redisKeyResult.setLength(jedis.strlen(item));
                    break;
                case Set:
                case ZSet:
                case List:
                    redisKeyResult.setLength(jedis.llen(item));
                    break;
                case Hash:
                    redisKeyResult.setLength(jedis.hlen(item));
            }
            redisKeyResults.add(redisKeyResult);
        }
        return redisKeyResults;
    }

    private JedisCluster getJedisCluster(String connName) throws IOException {
        List<RedisNode> redisNodes = redisNodes(connName);
        Set<HostAndPort> hostAndPorts = redisNodes.stream().map(RedisNode::getHostAndPort).collect(Collectors.toSet());
        return new JedisCluster(hostAndPorts);
    }

    /**
     * 扫描一个节点
     * @param jedis
     * @param index
     * @param pattern
     * @return
     */
    private List<String> scan(Jedis jedis,int index,String pattern,int limit ){
        // 搜索参数
        ScanParams scanParams = new ScanParams();
        scanParams.count(limit);
        if(StringUtils.isNotBlank(pattern)) {
            scanParams.match(pattern);
        }
        String cursor = "0" ;
        // 开始搜索
        ScanResult<String> scanResult = jedis.scan(cursor+"", scanParams);
        List<String> result = scanResult.getResult();
        //如果搜索结果为空,则继续搜索,直到有值或搜索到末尾
        List<String> keyAllReuslts = new ArrayList<>();
        do {
            scanResult = jedis.scan(scanResult.getStringCursor(), scanParams);
            result = scanResult.getResult();keyAllReuslts.addAll(result);
            cursor = scanResult.getStringCursor();
        }while (keyAllReuslts.size() < limit && NumberUtils.toLong(cursor) != 0L);

        return keyAllReuslts;

    }


    enum RedisType{
        string("string"),Set("set"),ZSet("zset"),Hash("hash"),List("list");
        private String value;

        RedisType(String value) {
            this.value = value;
        }

        public static RedisType parse(String type){
            RedisType[] values = RedisType.values();
            for (RedisType value : values) {
                if(value.value.equals(type)){
                    return value;
                }
            }
            return null;
        }
    }

    private void findSlaves(HostAndPort hostAndPort,List<RedisNode> redisNodes,String masterId) {
        // 先添加父节点
        Jedis jedis = new Jedis(hostAndPort.getHost(), hostAndPort.getPort());
        String replication = jedis.info("Replication");jedis.disconnect();
        Map<String, String> properties = ColonCommandReply.colonCommandReply.parserKeyValue(replication);
        RedisNode redisNode = new RedisNode();
        redisNode.setId(hostAndPort.toString());
        redisNode.setRole(properties.get("role"));
        redisNode.setHostAndPort(hostAndPort);
        redisNode.setMaster(masterId);
        redisNodes.add(redisNode);

        // 添加子节点
        Iterator<Map.Entry<String, String>> iterator = properties.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String, String> next = iterator.next();
            String key = next.getKey();
            if(key.startsWith("slave")){
                String value = next.getValue();
                String[] split = StringUtils.split(value, ',');
                String host = split[0].split("=")[1];int port = NumberUtils.toInt(split[1].split("=")[1]);
                findSlaves(HostAndPort.parseString(host+":"+port),redisNodes,hostAndPort.toString());
            }
        }
    }

    /**
     * 获取 redis 中 key 的值,使用对象进行反序列化
     * @param key
     * @param serializable
     * @param classloaderName
     * @return
     */
    public Object loadData(String connName,int index,String key,String serializable,String classloaderName) throws IOException, ClassNotFoundException {
        Jedis jedis = jedis(connName);
        String mode = mode(connName);

        byte [] valueBytes = null;
//        byte[] keyBytes = key.getBytes(Charset.forName("utf-8"));
        StringSerializer stringSerializer = new StringSerializer();
        byte[] keyBytes = stringSerializer.serialize(key);
        if("cluster".equals(mode)){
            JedisCluster jedisCluster = getJedisCluster(connName);
            valueBytes = jedisCluster.get(keyBytes);

        }else {

            if (index != 0) {
                jedis.select(index);
            }
            valueBytes = jedis.get(keyBytes);
        }

        if(valueBytes == null){
            logger.warn("key [{}] 不存在 ",key);
            return null;
        }

        Object object = null;
        ClassLoader extendClassloader = ClassLoader.getSystemClassLoader();
        if(StringUtils.isNotBlank(classloaderName)){
            extendClassloader = classLoaderManager.get(classloaderName);
        }
        switch (serializable){
            case "jdk":
                CustomObjectInputStream objectInputStream = new CustomObjectInputStream(new ByteArrayInputStream(valueBytes),extendClassloader);
                object = objectInputStream.readObject();
                break;
            case "kryo":
                Kryo kryo = KryoSerializer.kryos.get();

                Input input = new Input(valueBytes);
                kryo.setClassLoader(extendClassloader);
                object = kryo.readClassAndObject(input);
                break;
            case "fastJson":
            case "string":
            case "hex":
                object = ZkServlet.zkSerializerMap.get(serializable).deserialize(valueBytes);
                break;
        }
        return object;
    }

}
