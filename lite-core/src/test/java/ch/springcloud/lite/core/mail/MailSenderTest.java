package ch.springcloud.lite.core.mail;

import org.junit.Before;
import org.junit.Test;

import ch.springcloud.lite.core.mail.Content;
import ch.springcloud.lite.core.mail.Mail;
import ch.springcloud.lite.core.mail.MailSender;
import ch.springcloud.lite.core.mail.Receiver;
import ch.springcloud.lite.core.mail.Sender;

public class MailSenderTest {

	MailSender mailSender;

	@Before
	public void init() {
		mailSender = new DefaultMailSender();
	}

	@Test
	public void testSend() throws Exception {
		// 发件人的 邮箱 和 密码（替换为自己的邮箱和密码）
		// PS: 某些邮箱服务器为了增加邮箱本身密码的安全性，给 SMTP 客户端设置了独立密码（有的邮箱称为“授权码”）,
		// 对于开启了独立密码的邮箱, 这里的邮箱密码必需使用这个独立密码（授权码）。
		String myEmailAccount = "aisama@foxmail.com";
		String myEmailPassword = "birwqbkhmfrsddjc";
		// 发件人邮箱的 SMTP 服务器地址, 必须准确, 不同邮件服务器地址不同, 一般(只是一般, 绝非绝对)格式为: smtp.xxx.com
		// 网易126邮箱的 SMTP 服务器地址为: smtp.126.com
		String myEmailSMTPHost = "smtp.qq.com";
		// 收件人邮箱（替换为自己知道的有效邮箱）
		String receiveMailAccount = "1324993036@qq.com";
		Mail mail = new Mail();
		Sender sender = new Sender();
		sender.setAccount(myEmailAccount);
		sender.setNickname("Spring Cloud Lite");
		sender.setPassword(myEmailPassword);
		sender.setSmtphost(myEmailSMTPHost);
		Content content = new Content();
		content.setSubject("系统信息!");
		content.setContent("内容<br>第二行");
		Receiver receiver = new Receiver();
		receiver.setNickname("Dear!");
		receiver.setAddress(receiveMailAccount);
		mail.setSender(sender);
		mail.setContent(content);
		mail.setReceiver(receiver);
		mailSender.send(mail);
	}

}
