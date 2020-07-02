package ch.springcloud.lite.core.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import ch.springcloud.lite.core.client.ClientController;
import ch.springcloud.lite.core.connector.DefaultRemoteServerConnector;
import ch.springcloud.lite.core.connector.RemoteServerConnector;
import ch.springcloud.lite.core.client.ClientRefreshListener;
import ch.springcloud.lite.core.server.ServerRefreshListener;

public class DefaultCloudClientAutoConfiguration {

	@Autowired
	HttpServletRequest request;

	@Bean
	RemoteServerConnector connector() {
		return new DefaultRemoteServerConnector();
	}

	@Bean
	ServerRefreshListener serverRefreshListener() {
		return new ServerRefreshListener();
	}

	@Bean
	ClientController clientController() {
		return new ClientController();
	}

	@Bean
	ClientRefreshListener clientRefreshListener() {
		return new ClientRefreshListener();
	}

	@Bean
	RestTemplate cloudTemplate(ClientHttpRequestFactory httpRequestFactory) {
		return new RestTemplate(httpRequestFactory);
	}

	@Bean
	public ClientHttpRequestFactory httpRequestFactory(HttpClient restTemplateConfigHttpClient) {
		return new HttpComponentsClientHttpRequestFactory();
	}

	@Bean
	public HttpClient restTemplateConfigHttpClient() {
		Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
				.register("http", PlainConnectionSocketFactory.getSocketFactory())
				.register("https", SSLConnectionSocketFactory.getSocketFactory()).build();
		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(registry);
		// 设置整个连接池最大连接数 根据自己的场景决定
		// 后面调整从配置中心获取
		connectionManager.setMaxTotal(200);
		// 路由是对maxTotal的细分
		// 后面调整从配置中心获取
		connectionManager.setDefaultMaxPerRoute(100);
		RequestConfig requestConfig = RequestConfig.custom()
				// 服务器返回数据(response)的时间，超过该时间抛出read timeout
				// todo 后面调整从配置中心获取
				.setSocketTimeout(10000)
				// 连接上服务器(握手成功)的时间，超出该时间抛出connect timeout
				// todo 后面调整从配置中心获取
				.setConnectTimeout(5000)
				// 从连接池中获取连接的超时时间，超过该时间未拿到可用连接，
				// 会抛出org.apache.http.conn.ConnectionPoolTimeoutException:
				// Timeout waiting for connection from pool
				// 后面调整从配置中心获取
				.setConnectionRequestTimeout(30000).build();
		return HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).setConnectionManager(connectionManager)
				.build();
	}

	@Bean
	@Scope("request")
	HttpHeaders headers() {
		HttpHeaders header = new HttpHeaders();
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			List<String> cookieList = new ArrayList<String>();
			for (Cookie cookie : cookies) {
				// 将浏览器cookies放入list中
				// System.out.println("当前cookies为:" + cookie.getDomain() + " " +
				// cookie.getName() + ":" + cookie.getValue());
				cookieList.add(cookie.getName() + "=" + cookie.getValue());
			}
			header.put(HttpHeaders.COOKIE, cookieList);
		}
		return header;
	}

}
