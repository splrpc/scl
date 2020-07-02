package ch.springcloud.lite.core.model;

import lombok.Data;

@Data
public class HeartBeatRequest {

	String id;
	int version;

}
