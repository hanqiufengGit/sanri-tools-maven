package minitest;
 
import java.net.SocketTimeoutException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
 
@SuppressWarnings("deprecation")
public class ConnectUtils {
	/**
	 * 自定义私有类：绕开HTTPS证书校验
	 */
	private static class EasyTrustManager implements X509TrustManager {
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}                                                                                                                                                                                                                                                                                   
 
		public void checkClientTrusted(X509Certificate[] certs, String authType) {
		}
 
		public void checkServerTrusted(X509Certificate[] certs, String authType) {
		}
	}
	
	public static int isSSLConnected(String httpUrl) throws SocketTimeoutException, Exception {
		int statusCode = 0;
		
		HttpClient hc = new DefaultHttpClient();
		// 连接超时设为6秒
		hc.getParams().setIntParameter("http.connection.timeout", 6000);
		// 连接成功后等待返回的超时设为8秒
		hc.getParams().setIntParameter("http.socket.timeout", 8000);		
		
		HttpGet httpGet = new HttpGet(httpUrl);
		
		try 
		{
			// HTTPS应绕开证书验证
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, new TrustManager[] { new EasyTrustManager() },
					null);
			SSLSocketFactory factory = new SSLSocketFactory(context,
					SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			Scheme https = new Scheme("https", 443, factory);
			hc.getConnectionManager().getSchemeRegistry().register(https);
 
			HttpResponse response = hc.execute(httpGet);
			statusCode = response.getStatusLine().getStatusCode();
		} catch (Exception e) {
			httpGet.abort();
			throw e;
		} finally {
			hc.getConnectionManager().shutdown();
		}
		return statusCode;
	}
}