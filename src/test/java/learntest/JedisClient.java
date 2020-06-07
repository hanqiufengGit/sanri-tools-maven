package learntest;

import com.sanri.app.redis.CommandReply;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import redis.clients.jedis.*;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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

    @Test
    public void testJedisMulti(){
        String s = UUID.randomUUID().toString();
        System.out.println(s.replace("-",""));
    }

    @Test
    public void testCluster() throws IOException {
        Set<HostAndPort>  hostAndPorts = new HashSet<>();
        hostAndPorts.add(new HostAndPort("10.101.72.43",7000));
        hostAndPorts.add(new HostAndPort("10.101.72.43",7001));
        hostAndPorts.add(new HostAndPort("10.101.72.43",7002));

        JedisCluster jedisCluster = new JedisCluster(hostAndPorts);

        String a = jedisCluster.get("a");
        System.out.println(a);

        System.out.println(jedisCluster.type("a"));

        // 不支持 scan key
//        ScanParams scanParams = new ScanParams().match("*auth*").count(1000);
//        ScanResult<String> scan = jedisCluster.scan("0", scanParams);
//        String stringCursor = scan.getStringCursor();
//        List<String> result = scan.getResult();
//        System.out.println(StringUtils.join(result,'\n'));

        jedisCluster.close();
    }

    @Test
    public void testWrite() throws IOException {
        A abc = new A(1, "abc");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);
        outputStream.writeObject(abc);
        FileOutputStream fileOutputStream = new FileOutputStream("d:/test/abc.byte");
        fileOutputStream.write(byteArrayOutputStream.toByteArray());
        fileOutputStream.flush();
        fileOutputStream.close();
    }


}
class A implements java.io.Serializable {private int age;private String str;public A(){}public A(int age, String str){this.age = age;this.str = str;}}
