package ch.examples;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import ch.springcloud.lite.core.anno.EnableCloudClient;

/**
 * Hello world!
 *
 */
@SpringBootApplication(scanBasePackages = "ch.examples.service")
@EnableCloudClient
@EnableTransactionManagement
public class ServerApp3 {

	public static void main(String[] args) {
		SpringApplication.run(ServerApp3.class, args);
	}

}
