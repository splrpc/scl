package ch.springcloud.lite.core.model;

import java.util.concurrent.TimeUnit;

import ch.springcloud.lite.core.client.SystemMonitor;

public class MachineInfoTest {

	public static void main(String[] args) throws InterruptedException {
		SystemMonitor monitor = new SystemMonitor();
		TimeUnit.SECONDS.sleep(3);
		monitor.init();
		System.out.println(monitor.info());
		System.exit(0);
	}

}
