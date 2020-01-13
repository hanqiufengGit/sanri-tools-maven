package com.sanri.app.servlet;

import com.sanri.app.postman.RedisService;
import com.sanri.frame.RequestMapping;
import org.apache.commons.collections.CollectionUtils;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RequestMapping("/company")
public class CompanyServlet {
    RedisService redisService =  new RedisService();

    /**
     * 公司系统登录
     * @param env 可选值，dev,sit,uat
     * @return
     */
    public Map<String,String> token(String env) throws IOException {
        // 目前公司使用 redis 登录
        Env envEnum = Env.valueOf(env);
        if(envEnum != null){
            Jedis jedis =  null;
            switch (envEnum){
                case dev:
                    // 从 redis 中拿取 token 值
                    jedis = redisService.jedis("lo");
                    break;
                case sit:
                    jedis = redisService.jedis("10.101.70.75");
                    break;
                case uat:
                    break;
            }
            if(jedis != null){
                Set<String> keys = jedis.keys("AUTH:sso_user_*");
                if(CollectionUtils.isNotEmpty(keys)){
                    String firstValue = keys.iterator().next();
                    String token = firstValue.split("AUTH:sso_user_")[1];
                    return  new HashMap<String,String>(){{
                        put("token",token);
                    }};
                }
            }
        }
        return null;
    }

    public enum Env {
        dev,sit,uat
    }
}
