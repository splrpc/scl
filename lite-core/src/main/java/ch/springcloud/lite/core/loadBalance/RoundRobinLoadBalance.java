package ch.springcloud.lite.core.loadBalance;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import ch.springcloud.lite.core.model.CloudServer;

public class RoundRobinLoadBalance implements LoadBalance {

	class RoundRobinServer {
		AtomicInteger picknum = new AtomicInteger();
		AtomicInteger priority = new AtomicInteger();
	}

	Map<CloudServer, RoundRobinServer> running_servers = new ConcurrentHashMap<>();

	@Override
	public CloudServer pickOne(List<CloudServer> servers) {
		if (servers.isEmpty()) {
			return null;
		}
		servers = servers.stream().sorted((s1, s2) -> s1.getMeta().getServerid().compareTo(s1.getMeta().getServerid()))
				.collect(Collectors.toList());
		return doPickOne1(servers);
	}

	private CloudServer doPickOne1(List<CloudServer> servers) {
		if (!isCached(servers)) {
			synchronized (this) {
				running_servers.clear();
				servers.forEach(server -> {
					RoundRobinServer running_server = new RoundRobinServer();
					running_server.priority = new AtomicInteger(server.getMeta().getPriority());
					running_servers.put(server, running_server);
				});
				return doPickOne2(servers);
			}
		} else {
			return doPickOne2(servers);
		}
	}

	CloudServer doPickOne2(List<CloudServer> servers) {
		int minnum = Integer.MAX_VALUE;
		double minratio = Double.MAX_VALUE;
		CloudServer minserver = null;
		for (Entry<CloudServer, RoundRobinServer> entry : running_servers.entrySet()) {
			RoundRobinServer value = entry.getValue();
			int picknum = value.picknum.get();
			double ratio = picknum / (double) value.priority.get();
			if (ratio < minratio) {
				minratio = ratio;
				minnum = picknum;
				minserver = entry.getKey();
			}
		}
		if (minserver == null) {
			return doPickOne1(servers);
		}
		RoundRobinServer roundRobinServer = running_servers.get(minserver);
		if (roundRobinServer == null) {
			return doPickOne1(servers);
		}
		boolean ok = roundRobinServer.picknum.compareAndSet(minnum, minnum + 1);
		if (ok) {
			return minserver;
		} else {
			return doPickOne1(servers);
		}
	}

	private boolean isCached(List<CloudServer> servers) {
		if (servers.size() != running_servers.size()) {
			return false;
		}
		for (int i = 0; i < servers.size(); i++) {
			CloudServer server = servers.get(i);
			RoundRobinServer running_server = running_servers.get(server);
			if (running_server == null || (int) running_server.priority.get() != server.getMeta().getPriority()) {
				return false;
			}
		}
		return true;
	}

}
