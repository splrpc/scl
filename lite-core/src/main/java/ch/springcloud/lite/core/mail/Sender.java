package ch.springcloud.lite.core.mail;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties("spring.cloud.lite.app.email.sender")
public class Sender {

	volatile String account;
	volatile String password;
	volatile String smtphost;
	volatile String nickname;

}
