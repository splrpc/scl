package ch.springcloud.lite.core.model;

import java.util.List;
import lombok.Data;

@Data
public class CloudServerSnapshot {

	String serverid;
	String name;
	List<String> hosts;
	int port;
	long startTime;
	volatile int priority;
	boolean shutdown;
	volatile int version;
	volatile int qpslimit;

}
