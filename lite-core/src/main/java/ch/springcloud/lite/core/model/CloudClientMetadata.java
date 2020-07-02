package ch.springcloud.lite.core.model;

import java.util.concurrent.atomic.AtomicInteger;

import lombok.Data;

@Data
public class CloudClientMetadata {

	AtomicInteger version = new AtomicInteger();
	AtomicInteger clusterVersion = new AtomicInteger();

}
