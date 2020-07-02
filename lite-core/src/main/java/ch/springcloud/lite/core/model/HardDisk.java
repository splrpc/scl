package ch.springcloud.lite.core.model;

import lombok.Data;

@Data
public class HardDisk {

	String path;
	long used;
	long total;

}
