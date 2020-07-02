package ch.springcloud.lite.core.model;

import java.util.List;

import lombok.Data;

@Data
public class CloudClientSnapshot {

	CloudServerSnapshot self;
	List<CloudServerSnapshot> servers;
	int version;

}
