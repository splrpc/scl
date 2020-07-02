package ch.examples;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import ch.springcloud.lite.core.anno.EnableCloudServer;

/**
 * Hello world!
 *
 */
@SpringBootApplication(scanBasePackages = "ch.examples.service")
@EnableCloudServer
public class ServerApp5 {

	public static void main(String[] args) {
		SpringApplication.run(ServerApp5.class, args);
	}

}
