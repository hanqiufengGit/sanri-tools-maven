package learntest;

import redis.clients.jedis.Jedis;

public class RedisTest {
    public static void main(String[] args) {
        Jedis jedis = new Jedis("10.101.72.43", 7000, 2000);
    }
}
