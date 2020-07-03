package ch.examples;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import ch.springcloud.lite.core.anno.EnableCloudServer;

/**
 * Hello world!
 *
 */
@SpringBootApplication(scanBasePackages = "ch.examples.service")
@EnableCloudServer
@EnableTransactionManagement
public class ServerApp {

	public static void main(String[] args) {
		SpringApplication.run(ServerApp.class, args);
	}

}
