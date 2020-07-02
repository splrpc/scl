package ch.springcloud.lite.core.client;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import ch.springcloud.lite.core.connector.RemoteServerConnector;
import ch.springcloud.lite.core.model.CloudClientInfo;
import ch.springcloud.lite.core.model.CloudClientMetadata;
import ch.springcloud.lite.core.model.CloudClientSnapshot;
import ch.springcloud.lite.core.model.CloudServerMetaData;
import ch.springcloud.lite.core.model.CloudServerSnapshot;
import ch.springcloud.lite.core.model.MachineInfo;
import ch.springcloud.lite.core.server.ClientRefreshListener;
import ch.springcloud.lite.core.type.AliveStatus;

@RestController(ClientController.CLIENTCONTROLLER)
@RequestMapping(ClientController.CLIENTCONTROLLER)
public class ClientController {

	public final static String CLIENTCONTROLLER = "/cloud-lite-client";
	public final static String CLIENTINFO = "/client-info";
	public final static String SNAPSHOT = "/snapshot";
	public final static String ATTACH = "/attach";
	public final static String ADDREMOTE = "/add-remote";
	public final static String HEARTBEAT = "/heart-beat";
	public final static String MONITORVERSION = "/monitor-version";
	public final static String MACHINEINFO = "/machine-info";

	@Autowired
	RemoteServerConnector connector;
	@Autowired
	CloudClientMetadata metadata;
	@Autowired(required = false)
	CloudServerMetaData self;
	@Autowired
	ClientRefreshListener listener;
	@Autowired
	SystemMonitor monitor;
	@Autowired
	Lock clientReadLock;
	@Autowired
	Lock clientWriteLock;

	@GetMapping(HEARTBEAT)
	public DeferredResult<AliveStatus> alive(int version) {
		long timeout = 600000L;
		DeferredResult<AliveStatus> status = new DeferredResult<>(timeout, AliveStatus.Healthy);
		clientReadLock.lock();
		try {
			if (metadata.getClusterVersion().get() != version) {
				status.setResult(AliveStatus.ClientOutmoded);
			} else {
				listener.regist(status, timeout);
			}
		} finally {
			clientReadLock.unlock();
		}
		return status;
	}

	@GetMapping(MACHINEINFO)
	public MachineInfo info() {
		return monitor.info();
	}

	@GetMapping(MONITORVERSION)
	public int clusterVersion() {
		return metadata.getClusterVersion().get();
	}

	@GetMapping(CLIENTINFO)
	public CloudClientInfo clientinfo() {
		clientReadLock.lock();
		try {
			List<CloudServerMetaData> servers = connector.healthyServers();
			CloudClientInfo info = new CloudClientInfo();
			info.setServers(servers);
			info.setVersion(metadata.getVersion().get());
			info.setSelf(self);
			return info;
		} finally {
			clientReadLock.unlock();
		}
	}

	@GetMapping(SNAPSHOT)
	public CloudClientSnapshot snapshot() {
		clientReadLock.lock();
		try {
			List<CloudServerSnapshot> servers = connector.healthyServers().stream().map(CloudServerMetaData::snapshot)
					.collect(Collectors.toList());
			CloudClientSnapshot snapshot = new CloudClientSnapshot();
			snapshot.setSelf(self);
			snapshot.setServers(servers);
			snapshot.setVersion(metadata.getVersion().get());
			return snapshot;
		} finally {
			clientReadLock.unlock();
		}
	}

	@Autowired
	HttpServletRequest request;

	@PutMapping(ADDREMOTE)
	public void addremote(String url) {
		System.out.println("ADDREMOTE:"+url);
		connector.addRemote(url);
	}

	@PutMapping(ATTACH)
	public void attach(@RequestBody CloudServerSnapshot remote) throws IOException {
		System.out.println("ATTACH:"+remote);
		connector.attach(remote);
	}

}
