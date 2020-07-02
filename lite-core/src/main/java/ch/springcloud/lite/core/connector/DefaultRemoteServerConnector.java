package ch.springcloud.lite.core.connector;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import ch.springcloud.lite.core.client.ClientController;
import ch.springcloud.lite.core.codec.CloudDecoder;
import ch.springcloud.lite.core.event.ClientRefreshEvent;
import ch.springcloud.lite.core.model.AliveRequest;
import ch.springcloud.lite.core.model.CloudClientMetadata;
import ch.springcloud.lite.core.model.CloudClientSnapshot;
import ch.springcloud.lite.core.model.CloudInvocation;
import ch.springcloud.lite.core.model.CloudMethodParam;
import ch.springcloud.lite.core.model.CloudServer;
import ch.springcloud.lite.core.model.CloudServerMetaData;
import ch.springcloud.lite.core.model.CloudServerSnapshot;
import ch.springcloud.lite.core.model.RemoteRequest;
import ch.springcloud.lite.core.model.RemoteResponse;
import ch.springcloud.lite.core.server.ServerController;
import ch.springcloud.lite.core.type.AliveStatus;
import ch.springcloud.lite.core.type.VariantType;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class DefaultRemoteServerConnector implements RemoteServerConnector {

	@Autowired
	RestTemplate cloudTemplate;
	@Autowired
	ApplicationContext ctx;
	@Autowired
	CloudDecoder decoder;
	@Autowired
	ScheduledThreadPoolExecutor executor;
	Map<String, CloudServer> servers = new ConcurrentHashMap<>(16);
	Map<String, WebClient> clients = new ConcurrentHashMap<>(16);
	@Autowired
	CloudClientMetadata metadata;
	@Autowired(required = false)
	LoadServerListeners listeners;
	@Autowired
	Lock clientReadLock;
	@Autowired
	Lock clientWriteLock;

	int[] errorWaits = { 1, 1, 2, 5, 10, 20, 30, 60 };

	@Override
	public List<CloudServer> getServers(CloudInvocation invocation) {
		return servers.values().stream().filter(server -> !server.getInactive().get() && !server.getMeta().isShutdown()
				&& server.getInvocations().contains(invocation)).collect(Collectors.toList());
	}

	@Override
	public Object invoke(CloudServer remoteServer, RemoteRequest request, Class<?> type) throws Exception {
		String remoteUrl = remoteServer.getActiveUrl();
		if (!remoteUrl.startsWith("http")) {
			remoteUrl = "http://" + remoteUrl;
		}
		remoteUrl += ServerController.SERVERCONTROLLER + ServerController.INVOKEPATH;
		HttpHeaders headers = ctx.getBean(HttpHeaders.class);
		HttpEntity<RemoteRequest> requestEntity = new HttpEntity<>(request, headers);
		RemoteResponse response = cloudTemplate.postForObject(new URI(remoteUrl), requestEntity, RemoteResponse.class);
		log.info("response {}", response);
		if (response.getType() == VariantType.empty) {
			return null;
		}
		return decoder.decode(response.getVal(), type);
	}

	@Override
	@Async
	public CloudServer load(String remoteUrl, boolean update) {
		if (remoteUrl.matches("^[\\w.\\d]+:(\\d+)$")) {
			String matedataPath = metapath(remoteUrl);
			for (int i = 0; i < 3; i++)
				try {
					CloudServerMetaData metadata = cloudTemplate.getForObject(matedataPath, CloudServerMetaData.class);
					CloudServer cloudServer = new CloudServer();
					cloudServer.setMeta(metadata);
					cloudServer.setActiveUrl(remoteUrl);
					cloudServer.getFreshtime().set(System.currentTimeMillis());
					cloudServer.setInvocations(getInvocations(metadata));
					if (update) {
						servers.put(metadata.getServerid(), cloudServer);
					} else {
						servers.putIfAbsent(metadata.getServerid(), cloudServer);
					}
					keep(cloudServer);
					if (listeners != null) {
						try {
							listeners.getListeners().forEach(listener -> listener.onLoad(cloudServer));
						} catch (Exception e) {
							log.info("Invoke listener fail for {}!", e);
						}
					}
					this.metadata.getVersion().getAndIncrement();
					publishClientRefreshEvent();
					log.info("load server {}", cloudServer.getMeta().getServerid());
					return cloudServer;
				} catch (Throwable e) {
					log.info("load Remote Url {} Error! for {}", matedataPath, e.getMessage());
				}
		}
		return null;
	}

	private String metapath(String remoteUrl) {
		String matedataPath = "http://" + remoteUrl + ServerController.SERVERCONTROLLER + ServerController.MATADATA;
		return matedataPath;
	}

	void keep(CloudServer cloudServer) {
		String id = cloudServer.getMeta().getServerid();
		if (!clients.containsKey(id)) {
			WebClient webClient = WebClient.create("http://" + cloudServer.getActiveUrl()
					+ ServerController.SERVERCONTROLLER + ServerController.HEARTBEAT);
			WebClient current = clients.putIfAbsent(id, webClient);
			if (current == null) {
				subscribe(webClient, cloudServer);
			}
		}
	}

	void subscribe(WebClient webClient, CloudServer cloudServer) {
		try {
			if (!clients.containsKey(cloudServer.getMeta().getServerid())) {
				return;
			}
			AliveRequest request = new AliveRequest();
			request.setSid(cloudServer.getMeta().getServerid());
			request.setClientVersion(cloudServer.getVersion().get());
			request.setServerVersion(cloudServer.getMeta().getVersion());
			Mono<AliveStatus> mono = webClient.post().bodyValue(request).retrieve().bodyToMono(AliveStatus.class);
			mono.onErrorReturn(AliveStatus.ConnectError).subscribe(status -> handle(status, webClient, cloudServer));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void handle(AliveStatus status, WebClient webClient, CloudServer cloudServer) {
		log.info("Alive Status of {} is {}", cloudServer.getMeta().getServerid(), status);
		switch (status) {
		case ConnectError: {
			handleConnectError(webClient, cloudServer);
			return;
		}
		case Healthy: {
			cloudServer.getErrorcount().set(0);
			if (cloudServer.getInactive().get()) {
				cloudServer.getInactive().set(false);
				metadata.getVersion().getAndIncrement();
				publishClientRefreshEvent();
			}
			subscribe(webClient, cloudServer);
			return;
		}
		case NotMe: {
			unregister(webClient, cloudServer);
			return;
		}
		case ClientOutmoded: {
			fetchNewest(cloudServer);
			subscribe(webClient, cloudServer);
			return;
		}
		case ServerOutmoded: {
			freshServer(cloudServer);
			subscribe(webClient, cloudServer);
			return;
		}
		}
	}

	void freshServer(CloudServer cloudServer) {
		try {
			String activeUrl = cloudServer.getActiveUrl();
			String url = metapath(activeUrl);
			CloudServerMetaData metadata = cloudTemplate.getForObject(url, CloudServerMetaData.class);
			CloudServer oldServer = servers.get(metadata.getServerid());
			oldServer.setMeta(metadata);
			publishClientRefreshEvent();
		} catch (Exception e) {
			log.info("Fresh Server fail {}", e);
		}
	}

	void fetchNewest(CloudServer cloudServer) {
		try {
			String activeUrl = cloudServer.getActiveUrl();
			String url = "http://" + activeUrl + ClientController.CLIENTCONTROLLER + ClientController.SNAPSHOT;
			CloudClientSnapshot snapshot = cloudTemplate.getForObject(url, CloudClientSnapshot.class);
			snapshot.getServers().forEach(this::attach);
			cloudServer.getVersion().set(snapshot.getVersion());
		} catch (Exception e) {
			log.info("Fetch newest fail {}", e);
		}
	}

	boolean isServer() {
		return ctx.containsBean("serverController");
	}

	void publishClientRefreshEvent() {
		clientWriteLock.lock();
		try {
			metadata.getClusterVersion().getAndIncrement();
			ctx.publishEvent(new ClientRefreshEvent(metadata));
		} finally {
			clientWriteLock.unlock();
		}
	}

	public void attach(CloudServerSnapshot server) {
		if (!servers.containsKey(server.getServerid())) {
			for (String host : server.getHosts()) {
				String remoteUrl = host + ":" + server.getPort();
				CloudServer newserver = load(remoteUrl, false);
				if (newserver != null) {
					return;
				}
			}
			throw new RuntimeException("Fail to load " + server + "!");
		}
	}

	void handleConnectError(WebClient webClient, CloudServer cloudServer) {
		int errorCount = cloudServer.getErrorcount().getAndIncrement();
		cloudServer.getFreshtime().set(System.currentTimeMillis());
		if (errorCount > errorWaits.length / 2 - 1) {
			cloudServer.getInactive().set(true);
			publishClientRefreshEvent();
		}
		int errorWaitIndex = errorCount >= errorWaits.length ? errorWaits.length - 1 : errorCount;
		int errorWait = errorWaits[errorWaitIndex];
		delayReconnect(webClient, cloudServer, errorWait);
	}

	private void delayReconnect(WebClient webClient, CloudServer cloudServer, int seconds) {
		AtomicBoolean run = new AtomicBoolean();
		try {
			executor.schedule(() -> {
				tryReconnect(run, webClient, cloudServer);
			}, seconds, TimeUnit.SECONDS);
		} catch (Exception e) {
			e.printStackTrace();
			try {
				Thread.sleep(seconds * 1000);
			} catch (InterruptedException e1) {

			} finally {
				tryReconnect(run, webClient, cloudServer);
			}
		}
	}

	void tryReconnect(AtomicBoolean run, WebClient webClient, CloudServer cloudServer) {
		for (;;) {
			if (run.get()) {
				return;
			}
			if (run.compareAndSet(false, true)) {
				subscribe(webClient, cloudServer);
				return;
			}
		}
	}

	void unregister(WebClient webClient, CloudServer cloudServer) {
		for (int i = 0; i < 3; i++) {
			try {
				CloudServer newServer = load(cloudServer.getActiveUrl(), false);
				if (newServer != null) {
					String id = cloudServer.getMeta().getServerid();
					servers.remove(id);
					clients.remove(id);
					log.info("Unregister {} successful!", cloudServer.getActiveUrl());
					metadata.getVersion().getAndIncrement();
					publishClientRefreshEvent();
					return;
				}
			} catch (Throwable e) {
				log.info("Unregister {} error {}", cloudServer.getActiveUrl(), e);
			}
		}
		subscribe(webClient, cloudServer);
	}

	Set<CloudInvocation> getInvocations(CloudServerMetaData metadata) {
		return metadata.getServices().stream().flatMap(service -> service.getMethods().stream().map(method -> {
			CloudInvocation invocation = new CloudInvocation();
			invocation.setService(service.getName());
			invocation.setMethod(method.getName());
			invocation.setTypes(method.getParams().stream().map(CloudMethodParam::getType).collect(Collectors.toList())
					.toArray(new VariantType[method.getParams().size()]));
			return invocation;
		})).collect(Collectors.toSet());
	}

	@Override
	public List<CloudServerMetaData> healthyServers() {
		return servers.values().stream().filter(server -> !server.getInactive().get()).map(CloudServer::getMeta)
				.sorted((s1, s2) -> Long.compare(s1.getStartTime(), s2.getStartTime())).collect(Collectors.toList());
	}

	@PreDestroy
	public void destory() {
		clients.clear();
		servers.clear();
	}

	@Override
	public void addRemote(String url) {
		Assert.isTrue(load(url, false) != null, "添加失败!");
	}

}
