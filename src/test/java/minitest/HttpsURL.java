package minitest;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class HttpsURL {

    @Test
    public void testSkip() throws IOException {
        URL url = new URL("https://10.101.70.202/20191011/1570793159426.png");
        TrustAnyHostnameVerifier trustAnyHostnameVerifier = new TrustAnyHostnameVerifier();
        HttpsURLConnection  httpsURLConnection = (HttpsURLConnection)url.openConnection();
        httpsURLConnection.setHostnameVerifier(trustAnyHostnameVerifier);
        HTTPSTrustManager.allowAllSSL();;
        InputStream inputStream = httpsURLConnection.getInputStream();
        IOUtils.copy(inputStream,new FileOutputStream("d:/test/1570793159426.png"));
    }

    // 定制Verifier
    public class TrustAnyHostnameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    @Test
    public void testNet() throws Exception {
        int sslConnected = ConnectUtils.isSSLConnected("https://10.101.70.202/20191011/1570793159426.png");
        System.out.println(sslConnected);
    }

}
