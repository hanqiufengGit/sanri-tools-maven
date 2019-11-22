package minitest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.apache.log4j.Logger;
import org.junit.Test;

public class HttpsTest {
    //    private String url = "https://kyfw.12306.cn/";
    private String url = "https://10.101.70.202/20191011/1570793159426.png";
    private Logger logger;

    public HttpsTest() {
        logger = Logger.getLogger(HttpsTest.class);
    }

    private static void trustAllHttpsCertificates() throws Exception {
        javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
        javax.net.ssl.TrustManager tm = new miTM();
        trustAllCerts[0] = tm;
        javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL", "SunJSSE");
        sc.init(null, trustAllCerts, null);
        javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    }

    public String getData() {
        PrintWriter out = null;
        BufferedReader in = null;
        HttpURLConnection conn = null;
        String result = "";
        try {
            //该部分必须在获取connection前调用
            trustAllHttpsCertificates();
            HostnameVerifier hv = new HostnameVerifier() {
                @Override
                public boolean verify(String urlHostName, SSLSession session) {
                    logger.info("Warning: URL Host: " + urlHostName + " vs. " + session.getPeerHost());
                    return true;
                }
            };
            HttpsURLConnection.setDefaultHostnameVerifier(hv);
            conn = (HttpURLConnection) new URL("http://www.baidu.com").openConnection();
            // 发送GET请求必须设置如下两行
            conn.setDoInput(true);
            conn.setRequestMethod("GET");
//                // 发送POST请求必须设置如下两行
//                conn.setDoOutput(true);
//                conn.setDoInput(true);
//                
//                // 获取URLConnection对象对应的输出流
//                out = new PrintWriter(conn.getOutputStream());
//                // 发送请求参数
////                out.print(params);
//                // flush输出流的缓冲
//                out.flush();

            //flush输出流的缓冲
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            logger.error("发送 GET 请求出现异常！\t请求ID:" + "\n" + e.getMessage() + "\n");
            e.printStackTrace();
        } finally {// 使用finally块来关闭输出流、输入流
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                logger.error("关闭数据流出错了！\n" + ex.getMessage() + "\n");
                ex.printStackTrace();
            }
        }
        // 获得相应结果result,可以直接处理......
        return result;

    }

    static class miTM implements javax.net.ssl.TrustManager, javax.net.ssl.X509TrustManager {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }
        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
        }
        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
        }
    }

    @Test
    public void test() {
        String data = getData();
        System.out.println(data);
    }
}
