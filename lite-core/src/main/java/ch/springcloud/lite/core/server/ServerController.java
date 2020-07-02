package ch.springcloud.lite.core.server;

import java.util.concurrent.locks.Lock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.google.common.util.concurrent.RateLimiter;

import ch.springcloud.lite.core.client.ClientRefreshListener;
import ch.springcloud.lite.core.event.ServerRefreshEvent;
import ch.springcloud.lite.core.model.AliveRequest;
import ch.springcloud.lite.core.model.CloudClientMetadata;
import ch.springcloud.lite.core.model.CloudServerMetaData;
import ch.springcloud.lite.core.model.RemoteRequest;
import ch.springcloud.lite.core.model.RemoteResponse;
import ch.springcloud.lite.core.properties.CloudServerProperties;
import ch.springcloud.lite.core.type.AliveStatus;

@RestController(ServerController.SERVERCONTROLLER)
@RequestMapping(ServerController.SERVERCONTROLLER)
public class ServerController {

	public final static String SERVERCONTROLLER = "/cloud-lite-server";
	public final static String INVOKEPATH = "/scl-invoke";
	public final static String MATADATA = "/metadata";
	public final static String HEARTBEAT = "/heart-beat";
	public final static String CHANGEPRIORITY = "/changepriority";
	public final static String CHANGERUNNING = "/changerunning";
	public final static String CHANGEQPSLIMIT = "/changeqpslimit";

	@Autowired
	CloudServerMetaData metadata;
	@Autowired
	CloudServerProperties properties;
	@Autowired
	RemoteRequestHandler handler;
	@Autowired
	RemoteResponse timeoutResponse;
	@Autowired
	CloudClientMetadata clientMetadata;
	@Autowired
	ApplicationContext ctx;
	@Autowired
	ServerRefreshListener listener;
	@Autowired
	ClientRefreshListener clientListener;
	@Autowired
	Lock clientReadLock;
	@Autowired
	Lock clientWriteLock;
	@Autowired
	Lock serverReadLock;
	@Autowired
	Lock serverWriteLock;
	@Autowired
	RateLimiter limiter;

	@GetMapping(MATADATA)
	public CloudServerMetaData metadata() {
		serverReadLock.lock();
		try {
			return metadata.copy();
		} finally {
			serverReadLock.unlock();
		}
	}

	@PostMapping(HEARTBEAT)
	public DeferredResult<AliveStatus> alive(@RequestBody AliveRequest request) {
		DeferredResult<AliveStatus> status = new DeferredResult<>(properties.getAlivetimeout(), AliveStatus.Healthy);
		if (metadata.getServerid().equals(request.getSid())) {
			serverReadLock.lock();
			clientReadLock.lock();
			try {
				if (clientMetadata.getVersion().get() != request.getClientVersion()) {
					status.setResult(AliveStatus.ClientOutmoded);
				}
				if (metadata.getVersion() != request.getServerVersion()) {
					status.setResult(AliveStatus.ServerOutmoded);
				}
				listener.regist(status, request, properties.getAlivetimeout());
				clientListener.regist(status, properties.getAlivetimeout());
			} finally {
				serverReadLock.unlock();
				clientReadLock.unlock();
			}
		} else {
			status.setResult(AliveStatus.NotMe);
		}
		return status;
	}

	@PostMapping(INVOKEPATH)
	public DeferredResult<RemoteResponse> invoke(@RequestBody RemoteRequest request) {
		Long timeout = request.getTimeout();
		if (timeout <= 0) {
			timeout = (long) properties.getTimeout();
		}
		DeferredResult<RemoteResponse> result = new DeferredResult<>(timeout, timeoutResponse);
		handler.handle(request, result);
		return result;
	}

	@PostMapping(CHANGEPRIORITY)
	public void changepriority(int priority) {
		if (priority < 1 || priority > 1000) {
			throw new IllegalArgumentException();
		}
		serverWriteLock.lock();
		try {
			metadata.setPriority(priority);
			metadata.setVersion(metadata.getVersion() + 1);
			ctx.publishEvent(new ServerRefreshEvent(metadata));
		} finally {
			serverWriteLock.unlock();
		}
	}

	@PostMapping(CHANGERUNNING)
	public void changerunning(boolean shutdown) {
		serverWriteLock.lock();
		try {
			metadata.setShutdown(shutdown);
			metadata.setVersion(metadata.getVersion() + 1);
			ctx.publishEvent(new ServerRefreshEvent(metadata));
		} finally {
			serverWriteLock.unlock();
		}
	}

	@PostMapping(CHANGEQPSLIMIT)
	public void changeqpslimit(int qps) {
		serverWriteLock.lock();
		try {
			limiter.setRate(qps);
			metadata.setQpslimit(qps);
			metadata.setVersion(metadata.getVersion() + 1);
			ctx.publishEvent(new ServerRefreshEvent(metadata));
		} finally {
			serverWriteLock.unlock();
		}
	}

}
