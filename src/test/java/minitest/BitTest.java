package minitest;

import com.sanri.app.dtos.kafka.BrokerTopicMetrics;
import org.apache.commons.beanutils.MethodUtils;
import org.junit.Test;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.core.Constants;
import org.springframework.util.ReflectionUtils;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Map;

public class BitTest {

    @Test
    public void testRightBit(){
        int a = 15;
        int count = 1 ;
        while( a != 1){
            count++;
            a = a >> 1;
        }
        System.out.println(count);
    }

    public static void main(String[] args) {
        byte b = (byte) 888;
        System.out.println(b);

        System.out.println(888 % 128);

    }

    @Test
    public void test2(){
        String a = "\u0000abcde";
        System.out.println(a.replace('\u0000',' '));
    }

    @Test
    public void test5(){
        File file = new File("/m/n");
        File targetFile = new File(new File("/a/b"),file.toString());
        System.out.println(targetFile);
    }

    @Test
    public void test3(){
        for (;;);
    }
}
