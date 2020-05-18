package com.sanri.app.servlet;

import com.sanri.app.redis.RedisKeyResult;
import com.sanri.app.redis.RedisService;
import com.sanri.frame.RequestMapping;
import org.apache.commons.collections.CollectionUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            List<RedisKeyResult> redisKeyResults = null;
            switch (envEnum){
                case dev:
                    // 从 redis 中拿取 token 值
                    redisKeyResults = redisService.scan("lo", 0, "AUTH:sso_user_*", 1);
                    break;
                case sit:
                    redisKeyResults = redisService.scan("10.101.70.75",0, "AUTH:sso_user_*", 1);
                    break;
                case uat:
                    break;
            }
            if(CollectionUtils.isNotEmpty(redisKeyResults)){
                RedisKeyResult redisKeyResult = redisKeyResults.get(0);
                String token = redisKeyResult.getKey();

                token = token.split("AUTH:sso_user_")[1];
                String finalToken = token;
                return  new HashMap<String,String>(){{
                    put("token", finalToken);
                }};
            }
        }
        return null;
    }

    public enum Env {
        dev,sit,uat
    }
}
