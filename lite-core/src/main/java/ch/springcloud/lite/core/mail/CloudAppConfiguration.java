package ch.springcloud.lite.core.mail;

import org.springframework.context.annotation.Bean;

import ch.springcloud.lite.core.mail.Receiver;
import ch.springcloud.lite.core.mail.Sender;

public class CloudAppConfiguration {

	@Bean
	Sender sender() {
		return new Sender();
	}

	@Bean
	Receiver receiver() {
		return new Receiver();
	}

	@Bean
	SystemMonitorEventListener SystemMonitorEventListener() {
		return new SystemMonitorEventListener();
	}

	@Bean
	EmailInfo emailInfo() {
		return new EmailInfo();
	}

	@Bean
	MailSender mailSender() {
		return new DefaultMailSender();
	}

	@Bean
	EmailController emailController() {
		return new EmailController();
	}

	@Bean
	MailRepository mailRepository() {
		return new MailRepository();
	}

}
