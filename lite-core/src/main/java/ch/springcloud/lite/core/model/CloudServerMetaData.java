package ch.springcloud.lite.core.model;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Setter
@Getter
public class CloudServerMetaData extends CloudServerSnapshot {

	List<CloudService> services;

	AtomicReference<CloudServerSnapshot> snapshot = new AtomicReference<>();

	public CloudServerSnapshot snapshot() {
		if (snapshot.get() == null) {
			CloudServerSnapshot serverSnapshot = new CloudServerSnapshot();
			serverSnapshot.serverid = serverid;
			serverSnapshot.hosts = hosts;
			serverSnapshot.port = port;
			serverSnapshot.startTime = startTime;
			snapshot.compareAndSet(null, serverSnapshot);
		}
		return snapshot.get();
	}

	public CloudServerMetaData copy() {
		CloudServerMetaData copy = new CloudServerMetaData();
		copy.services = services;
		copy.version = version;
		copy.shutdown = shutdown;
		copy.startTime = startTime;
		copy.hosts = hosts;
		copy.name = name;
		copy.port = port;
		copy.snapshot = snapshot;
		copy.serverid = serverid;
		copy.priority = priority;
		return copy;
	}

}
