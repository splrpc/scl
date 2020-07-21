package ch.examples;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import ch.springcloud.lite.core.anno.EnableCloudServer;

/**
 * Hello world!
 *
 */
@SpringBootApplication(scanBasePackages = "ch.examples.service")
@EnableCloudServer
public class ServerApp {

	public static void main(String[] args) {
		ApplicationContext ctx = SpringApplication.run(ServerApp.class, args);
	}

}
