package ch.springcloud.lite.core.client;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.sun.management.OperatingSystemMXBean;

import ch.springcloud.lite.core.event.SystemMonitorEvent;
import ch.springcloud.lite.core.model.Cpu;
import ch.springcloud.lite.core.model.HardDisk;
import ch.springcloud.lite.core.model.JVMHeap;
import ch.springcloud.lite.core.model.MachineInfo;
import ch.springcloud.lite.core.model.Memory;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

@SuppressWarnings("restriction")
public class SystemMonitor {

	volatile MachineInfo info;

	int wait = 5;
	AtomicBoolean inited = new AtomicBoolean();
	@Autowired
	ScheduledExecutorService service;
	@Autowired
	ApplicationContext ctx;

	volatile long lastPublishTime;

	public MachineInfo info() {
		return info;
	}

	@PostConstruct
	public SystemMonitor init() {
		while (!inited.get() && inited.compareAndSet(false, true))
			service.scheduleAtFixedRate(() -> {
				try {
					MachineInfo info = new MachineInfo();
					SystemInfo systemInfo = new SystemInfo();
					OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
					MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
					// 椎内存使用情况
					MemoryUsage memoryUsage = memoryMXBean.getHeapMemoryUsage();
					// 初始的总内存
					long initTotalMemorySize = memoryUsage.getInit();
					// 最大可用内存
					long maxMemorySize = memoryUsage.getMax();
					// 已使用的内存
					long usedMemorySize = memoryUsage.getUsed();
					JVMHeap heap = new JVMHeap();
					heap.setTotal(maxMemorySize);
					heap.setUsed(usedMemorySize);
					heap.setInit(initTotalMemorySize);
					info.setHeap(heap);
					// 操作系统
					String osName = System.getProperty("os.name");
					info.setName(osName);
					Memory memory = new Memory();
					// 总的物理内存
					memory.setTotal(osmxb.getTotalPhysicalMemorySize());
					// 剩余的物理内存
					memory.setUsed(osmxb.getTotalPhysicalMemorySize() - osmxb.getFreePhysicalMemorySize());
					info.setMemory(memory);
					List<HardDisk> disks = new ArrayList<>();
					// 磁盘使用情况
					File[] files = File.listRoots();
					for (File file : files) {
						HardDisk disk = new HardDisk();
						disks.add(disk);
						disk.setTotal(file.getTotalSpace());
						disk.setUsed(file.getTotalSpace() - file.getUsableSpace());
						disk.setPath(file.getPath());
					}
					info.setDisks(disks);
					Cpu cpu = new Cpu();
					cpu.setCore(Runtime.getRuntime().availableProcessors());
					CentralProcessor processor = systemInfo.getHardware().getProcessor();
					long[] prevTicks = processor.getSystemCpuLoadTicks();
					TimeUnit.SECONDS.sleep(1);
					long[] ticks = processor.getSystemCpuLoadTicks();
					long nice = ticks[CentralProcessor.TickType.NICE.getIndex()]
							- prevTicks[CentralProcessor.TickType.NICE.getIndex()];
					long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()]
							- prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
					long softirq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()]
							- prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
					long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()]
							- prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
					long cSys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()]
							- prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
					long user = ticks[CentralProcessor.TickType.USER.getIndex()]
							- prevTicks[CentralProcessor.TickType.USER.getIndex()];
					long iowait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()]
							- prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
					long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()]
							- prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
					long totalCpu = user + nice + cSys + idle + iowait + irq + softirq + steal;
					cpu.setUserate(1 - idle * 1.0 / totalCpu);
					info.setCpu(cpu);
					info.setFreshTime(System.currentTimeMillis());
					SystemMonitor.this.info = info;
					publishEvent(info);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}, 0, wait, TimeUnit.SECONDS);
		return this;
	}

	private void publishEvent(MachineInfo info) {
		if (lastPublishTime < System.currentTimeMillis() - 60000L) {
			lastPublishTime = System.currentTimeMillis();
			ctx.publishEvent(new SystemMonitorEvent(info));
		}
	}

}