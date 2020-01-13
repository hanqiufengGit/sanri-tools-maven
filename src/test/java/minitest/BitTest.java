package minitest;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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
