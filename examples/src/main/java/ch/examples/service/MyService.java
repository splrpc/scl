package ch.examples.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

import ch.springcloud.lite.core.anno.Remote;
import ch.springcloud.lite.core.loadBalance.RoundRobinLoadBalance;
import ch.springcloud.lite.core.properties.CloudServerProperties;

@Service
public class MyService {

	@Remote(name = "newService2", timeout = 1000, loadBalance = RoundRobinLoadBalance.class)
	NewService newService;

	public Date name(String name, int num, CloudServerProperties properties, Map<String, String> hash, Set<String> set,
			List<String> list) {
		return new Date();
	}

	AtomicInteger id = new AtomicInteger();

	public Object call() {
		int tid = id.getAndIncrement();
		System.out.println(tid);
		return newService.call(tid);
	}

}
