package ch.springcloud.lite.core.model;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import lombok.Data;

@Data
public class CloudServer {

	volatile CloudServerMetaData meta;
	Set<CloudInvocation> invocations;
	AtomicInteger errorcount = new AtomicInteger();
	volatile String activeUrl;
	AtomicLong freshtime = new AtomicLong();
	AtomicBoolean inactive = new AtomicBoolean();
	AtomicInteger version = new AtomicInteger();
	
}
