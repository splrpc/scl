package ch.springcloud.lite.core.model;

import java.util.List;

import lombok.Data;

@Data
public class CloudClientInfo {

	CloudServerMetaData self;
	List<CloudServerMetaData> servers;
	int version;

}
