package ch.springcloud.lite.core.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.web.context.request.async.DeferredResult;

import ch.springcloud.lite.core.event.ServerRefreshEvent;
import ch.springcloud.lite.core.model.AliveRequest;
import ch.springcloud.lite.core.model.CloudServerMetaData;
import ch.springcloud.lite.core.type.AliveStatus;

public class ServerRefreshListener {

	@Autowired
	ScheduledExecutorService service;

	Map<DeferredResult<AliveStatus>, AliveRequest> map = new ConcurrentHashMap<>();

	@EventListener(classes = ServerRefreshEvent.class)
	public void handle(ServerRefreshEvent event) {
		CloudServerMetaData metaData = (CloudServerMetaData) event.getSource();
		map.forEach((key, value) -> {
			if (!key.isSetOrExpired()) {
				if (value.getServerVersion() != metaData.getVersion()) {
					key.setResult(AliveStatus.ServerOutmoded);
					map.remove(key);
				}
			} else {
				map.remove(key);
			}
		});
	}

	public void regist(DeferredResult<AliveStatus> status, AliveRequest request, long timeout) {
		service.schedule(() -> map.remove(status), timeout, TimeUnit.MILLISECONDS);
		map.put(status, request);
	}

}
