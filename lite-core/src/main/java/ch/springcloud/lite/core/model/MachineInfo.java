package ch.springcloud.lite.core.model;

import java.util.List;

import lombok.Data;

@Data
public class MachineInfo {

	String name;
	Memory memory;
	JVMHeap heap;
	long startTime;
	Cpu cpu;
	List<HardDisk> disks;
	long freshTime;

}
