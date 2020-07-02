package ch.springcloud.lite.core.model;

import lombok.Data;

@Data
public class AliveRequest {

	String sid;
	int clientVersion;
	int serverVersion;

}
