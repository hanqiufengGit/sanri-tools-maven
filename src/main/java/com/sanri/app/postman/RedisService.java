package com.sanri.app.postman;

import com.alibaba.fastjson.JSONObject;
import com.sanri.app.servlet.FileManagerServlet;
import com.sanri.frame.DispatchServlet;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.Jedis;
import sanri.utils.NumberUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RedisService {
    private String modul = "redis";
    static Map<String,Jedis> jedisMap = new HashMap<String, Jedis>();

    /**
     * 获取 jedis 实例
     * @param name
     * @return
     * @throws IOException
     */
    public Jedis jedis(String name) throws IOException {
        Jedis jedis = jedisMap.get(name);
        if(jedis == null){
            FileManagerServlet fileManagerServlet = DispatchServlet.getServlet(FileManagerServlet.class);
            String redisConnInfo = fileManagerServlet.readConfig(modul, name);

            JSONObject jsonObject = JSONObject.parseObject(redisConnInfo);
            String address = jsonObject.getString("connectStrings");
            String auth = jsonObject.getString("auth");

            String[] split = StringUtils.split(address, ':');
            jedis = new Jedis(split[0], NumberUtil.toInt(split[1]), 1000, 60000);
            if(StringUtils.isNotBlank(auth)){
                jedis.auth(auth);
            }
            jedisMap.put(name,jedis);
        }
        return jedis;
    }
}
