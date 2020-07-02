package ch.springcloud.lite.core.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.springcloud.lite.core.client.SystemMonitor;
import ch.springcloud.lite.core.model.MachineInfo;

@RestController(EmailController.CONTROLLER)
@RequestMapping(EmailController.CONTROLLER)
public class EmailController {

	public static final String CONTROLLER = "/spring-cloud-lite-monitor";

	@Autowired
	MailRepository repository;
	@Autowired
	MailSender mailSender;
	@Autowired
	SystemMonitor monitor;

	@GetMapping("/email-info")
	public EmailInfo info() {
		return repository.get();
	}

	@PostMapping("/change-value")
	public boolean changevalue(@RequestBody EmailInfo info) {
		repository.modify(info);
		return true;
	}

	@PutMapping("/send-test-email")
	public void sendTestEmail() {
		EmailInfo emailinfo = repository.get();
		Mail mail = new Mail();
		mail.setReceiver(emailinfo.getReceiver());
		mail.setSender(emailinfo.getSender());
		Content content = new Content();
		content.setSubject("监控测试邮件");
		MachineInfo info = monitor.info();
		content.setContent(String.format("机器当前CPU使用率:%.2f%%<br>内存使用率:%.2f%%",
				info.getCpu().getUserate() * 100, info.getMemory().getUsed() * 100.0 / info.getMemory().getTotal()));
		mail.setContent(content);
		mailSender.send(mail);
	}

}
