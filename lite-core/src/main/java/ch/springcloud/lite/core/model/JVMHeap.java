package ch.springcloud.lite.core.model;

import lombok.Data;

@Data
public class JVMHeap {

	long init;
	long used;
	long total;

}
