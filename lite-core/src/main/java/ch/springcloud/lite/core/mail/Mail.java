package ch.springcloud.lite.core.mail;

import lombok.Data;

@Data
public class Mail {

	Sender sender;
	Receiver receiver;
	Content content;

}
