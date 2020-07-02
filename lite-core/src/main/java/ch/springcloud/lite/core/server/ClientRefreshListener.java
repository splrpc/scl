package ch.springcloud.lite.core.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.web.context.request.async.DeferredResult;

import ch.springcloud.lite.core.event.ClientRefreshEvent;
import ch.springcloud.lite.core.type.AliveStatus;

public class ClientRefreshListener {

	@Autowired
	ScheduledExecutorService service;

	Map<DeferredResult<AliveStatus>, Long> map = new ConcurrentHashMap<>();

	@EventListener(classes = ClientRefreshEvent.class)
	public void handle(ClientRefreshEvent event) {
		map.forEach((key, value) -> {
			if (!key.isSetOrExpired()) {
				key.setResult(AliveStatus.ClientOutmoded);
			} else {
				map.remove(key);
			}
		});
	}

	public void regist(DeferredResult<AliveStatus> status, long timeout) {
		service.schedule(() -> map.remove(status), timeout, TimeUnit.MILLISECONDS);
		map.put(status, System.currentTimeMillis());
	}

}
