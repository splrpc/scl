package ch.springcloud.lite.core.mail;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties("spring.cloud.lite.app.email.receiver")
public class Receiver {

	volatile String address;
	volatile String nickname;

}
