package ch.examples;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import ch.springcloud.lite.core.anno.EnableCloudClient;

/**
 * Hello world!
 *
 */
@SpringBootApplication(scanBasePackages = "ch.examples.service")
@EnableCloudClient
public class ServerApp3 {

	public static void main(String[] args) {
		SpringApplication.run(ServerApp3.class, args);
	}

}
