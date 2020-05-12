//package com.sanri.app.servlet;
//
//import com.sanri.app.postman.RedisInfo;
//import com.sanri.app.postman.RedisService;
//import com.sanri.app.redis.RedisService;
//import com.sanri.frame.RequestMapping;
//import org.apache.commons.collections.CollectionUtils;
//import redis.clients.jedis.Jedis;
//import redis.clients.jedis.JedisCluster;
//import redis.clients.jedis.ScanParams;
//import redis.clients.jedis.ScanResult;
//
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//@RequestMapping("/company")
//public class CompanyServlet {
//    RedisService redisService =  new RedisService();
//
//    /**
//     * 公司系统登录
//     * @param env 可选值，dev,sit,uat
//     * @return
//     */
//    public Map<String,String> token(String env) throws IOException {
//        // 目前公司使用 redis 登录
//        Env envEnum = Env.valueOf(env);
//        if(envEnum != null){
//            Jedis redisInfo = null;
//            switch (envEnum){
//                case dev:
//                    // 从 redis 中拿取 token 值
//                    redisInfo = redisService.jedis("lo");
//                    break;
//                case sit:
//                    redisInfo = redisService.jedis("10.101.70.75");
//                    break;
//                case uat:
//                    break;
//            }
//            if(redisInfo != null){
//                boolean cluster = redisInfo.mode();
//                String token = "";
//                if(cluster){
//                    JedisCluster jedisCluster = redisInfo.getJedisCluster();
//                    long cursor = 0 ;
//                    do {
//                        ScanParams scanParams = new ScanParams();
//                        scanParams.count(1000).match("AUTH:sso_user_*");
//                        ScanResult<String> scanResult = jedisCluster.scan(cursor + "", scanParams);
//                        List<String> result = scanResult.getResult();
//                        if(CollectionUtils.isNotEmpty(result)){
//                            token =  result.get(0);
//                            break;
//                        }
//                    }while (cursor != 0);
//                }else{
//                    Jedis jedis = redisInfo.getJedis();
//                    Set<String> keys = jedis.keys("AUTH:sso_user_*");
//                    if(CollectionUtils.isNotEmpty(keys)){
//                        token = keys.iterator().next();
//                    }
//                }
//
//                token = token.split("AUTH:sso_user_")[1];
//                String finalToken = token;
//                return  new HashMap<String,String>(){{
//                    put("token", finalToken);
//                }};
//            }
//        }
//        return null;
//    }
//
//    public enum Env {
//        dev,sit,uat
//    }
//}
