package com.sanri.app.servlet;

import com.sanri.app.postman.ServerInfo;
import com.sanri.frame.RequestMapping;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import sanri.utils.DateUtil;
import sanri.utils.NumberUtil;

import javax.mail.internet.InternetAddress;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.Query;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

@RequestMapping("/test")
public class TestServlet {

	public ServerInfo info(HttpServletRequest request,HttpSession session) throws UnknownHostException, MalformedObjectNameException {
		String serverName = request.getServerName();
		String hostAddress = InetAddress.getLocalHost().getHostAddress();
		int serverPort = request.getServerPort();
		String id = session.getId();
		String requestURI = request.getRequestURI();

		MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();
		Set<ObjectName> objectNames = beanServer.queryNames(new ObjectName("*:type=Connector,*"), Query.match(Query.attr("protocol"), Query.value("HTTP/1.1")));
		String serverPortString = objectNames.iterator().next().getKeyProperty("port");

		return new ServerInfo(hostAddress, NumberUtils.toInt(serverPortString),requestURI,id);
	}

	/**
	 *
	 * 作者:sanri <br/>
	 * 时间:2018-8-27上午11:46:50<br/>
	 * 功能:方法执行 10 秒后返回 <br/>
	 * @return
	 */
	public String timeoutMethod(){

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return DateUtil.formatDateTime(new Date(System.currentTimeMillis())) ;
	}

    /**
     * 主要测试空格变 + 号的问题
     * @return
     */
	public String testTextarea(String textarea){
        System.out.println(textarea);
        return textarea;
    }

	/**
	 *
	 * 作者:sanri <br/>
	 * 时间:2018-8-31下午4:00:22<br/>
	 * 功能:nginx 504 测试 <br/>
	 * @return
	 */
	public String method504(){
		try {
			Thread.sleep(10000000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return DateUtil.formatDateTime(new Date(System.currentTimeMillis())) ;
	}

	/**
	 *
	 * 作者:sanri <br/>
	 * 时间:2018-9-5下午12:00:38<br/>
	 * 功能:获取 sessionId <br/>
	 * @param httpSession
	 * @return
	 */
	public String sessionId(HttpSession httpSession){
		httpSession.setAttribute("name","sanri");
		return httpSession.getId();
	}

	/**
	 *
	 * 作者:sanri <br/>
	 * 时间:2018-9-6上午10:10:10<br/>
	 * 功能:获取当前主机和端口 <br/>
	 * @param request
	 * @return
	 * @throws UnknownHostException
	 */
	public String connectString(HttpServletRequest request) throws UnknownHostException{
		InetAddress localHost = Inet4Address.getLocalHost();
		InetAddress localHost2 = Inet6Address.getLocalHost();
		String hostAddress = localHost.getHostAddress();
		System.out.println("localhost(ipv4):"+hostAddress);
		System.out.println("localhost(ipv6):"+localHost2.getHostAddress());

		String localAddr = request.getLocalAddr();
		int localPort = request.getLocalPort();
		return localAddr+":"+localPort;
	}

	public String remoteAddress(HttpServletRequest request){
		String remoteHost = request.getRemoteHost();
		int remotePort = request.getRemotePort();
		String remoteAddr = request.getRemoteAddr();
		String remoteUser = request.getRemoteUser();

		String realIp = request.getHeader("X-real-ip");
		if(StringUtils.isBlank(realIp)){
			realIp = remoteHost;
		}

		return realIp+":"+remotePort+"[remoteAddr:"+remoteAddr+"],[remoteUser:"+remoteUser+"]";
	}

	public Map<String,Object> sessionData(HttpSession httpSession){
		Map<String,Object> data = new HashMap<String,Object>();
		Enumeration<String> attributeNames = httpSession.getAttributeNames();
		while(attributeNames.hasMoreElements()){
			String key = ObjectUtils.toString(attributeNames.nextElement());
			Object attribute = httpSession.getAttribute(key);
			data.put(key, attribute);
		}

		return data;
	}

	public void addSessionData(String key,String value,HttpSession session){
		session.setAttribute(key, value);
	}

	class User {
		private String username;
		private String password;

		public User(String username, String password) {
			super();
			this.username = username;
			this.password = password;
		}

		public String getUsername() {
			return username;
		}
		public void setUsername(String username) {
			this.username = username;
		}
		public String getPassword() {
			return password;
		}
		public void setPassword(String password) {
			this.password = password;
		}


	}

	public void addNoSerializeData(HttpSession session){
		User user = new User("huangzr","123");
		session.setAttribute("user", user);
	}

	final int _1m = 1024*1024;

	public void add1MSessionData(HttpSession session){
		int [] _1mdata = new int [_1m];
		session.setAttribute("m1",_1mdata);
	}

	public void rewriteUrl(HttpServletRequest request,HttpServletResponse response) throws IOException{
		String requestURI = request.getRequestURI();
		StringBuffer requestURL = request.getRequestURL();
		String encodeURL = response.encodeURL(requestURI);
		response.sendRedirect(encodeURL);
	}
}
