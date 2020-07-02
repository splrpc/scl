package ch.springcloud.lite.core.mail;

import org.springframework.beans.factory.annotation.Autowired;

import lombok.Data;

@Data
public class EmailInfo {

	@Autowired
	Sender sender;
	@Autowired
	Receiver receiver;
	volatile int cpuratio = 90;
	volatile int memoryratio = 80;
	volatile int duration = 5;
	volatile boolean open;

}
