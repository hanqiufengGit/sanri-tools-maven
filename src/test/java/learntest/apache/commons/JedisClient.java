package learntest.apache.commons;

import com.sanri.app.redis.CommandReply;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import redis.clients.jedis.Client;
import redis.clients.jedis.Jedis;

import java.io.UnsupportedEncodingException;
import java.util.List;

public class JedisClient {
    @Test
    public void testReplay() throws UnsupportedEncodingException {
        Jedis jedis = new Jedis("10.101.72.43",7000,1000);
        jedis.getClient().clusterNodes();
        Client client = jedis.getClient();
//        System.out.println(client.getBulkReply());
        String bulkReply = client.getBulkReply();
        List<String[]> parser = CommandReply.spaceCommandReply.parser(bulkReply);
        for (String[] strings : parser) {
            System.out.println(StringUtils.join(strings,"|"));
        }
        jedis.disconnect();
    }

    @Test
    public void testInfo(){
        Jedis jedis = new Jedis("10.101.72.43",7000,1000);
        String info = jedis.info("Replication");
        System.out.println(info);
        jedis.disconnect();
    }
}
