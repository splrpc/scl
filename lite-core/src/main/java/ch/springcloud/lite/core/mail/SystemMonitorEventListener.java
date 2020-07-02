package ch.springcloud.lite.core.mail;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;

import ch.springcloud.lite.core.event.SystemMonitorEvent;
import ch.springcloud.lite.core.model.MachineInfo;
import ch.springcloud.lite.core.util.CloudUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SystemMonitorEventListener {

	@Autowired
	MailSender mailSender;
	@Autowired
	MailRepository repository;
	Map<String, Long> lastTryTimes = new ConcurrentHashMap<>();
	Map<String, Long> lastSendTimes = new ConcurrentHashMap<>();
	LinkedList<MachineInfo> infos = new LinkedList<MachineInfo>();

	@EventListener(classes = SystemMonitorEvent.class)
	public void listener(SystemMonitorEvent event) {
		EmailInfo mailinfo = repository.get();
		MachineInfo info = (MachineInfo) event.getSource();
		long duration = Integer.max(3, mailinfo.getDuration());
		String account = mailinfo.getSender().getAccount();
		synchronized (infos) {
			infos.add(info);
			boolean hit = false;
			while (infos.getFirst().getFreshTime() < System.currentTimeMillis() - (duration + 1) * 60000) {
				infos.removeFirst();
				hit = true;
			}
			log.info("Monitor hit {} of {} infos", hit, infos.size());
			if (hit && !infos.isEmpty() && mailinfo.isOpen()) {
				Long lastSendTime = lastSendTimes.get(account);
				Long lastTryTime = lastTryTimes.get(account);
				if (lastSendTime != null && lastSendTime > System.currentTimeMillis() - 120 * 60 * 1000) {
					return;
				}
				if (lastTryTime != null && lastTryTime > System.currentTimeMillis() - 30 * 60 * 1000) {
					return;
				}
				List<Double> cpurates = infos.stream().map(m -> m.getCpu().getUserate() * 100.0)
						.collect(Collectors.toList());
				List<Double> memrates = infos.stream()
						.map(m -> m.getMemory().getUsed() * 100.0 / m.getMemory().getTotal())
						.collect(Collectors.toList());
				double cpurate = average(cpurates);
				double memrate = average(memrates);
				if (cpurate > mailinfo.getCpuratio() || memrate > mailinfo.getMemoryratio()) {
					lastTryTimes.put(account, System.currentTimeMillis());
					sendMail(cpurate, memrate);
					lastSendTimes.put(account, System.currentTimeMillis());
				}
			}
		}
	}

	private void sendMail(double cpurate, double memrate) {
		EmailInfo emailinfo = repository.get();
		Mail mail = new Mail();
		mail.setReceiver(emailinfo.getReceiver());
		mail.setSender(emailinfo.getSender());
		Content content = new Content();
		content.setSubject("服务器负载预警");
		List<String> localhosts = CloudUtils.localhosts();
		content.setContent(String
				.format("机器" + localhosts.toString().replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(" ", "")
						+ "<br>当前CPU使用率:%.2f%%<br>内存使用率:%.2f%%", cpurate, memrate));
		mail.setContent(content);
		mailSender.send(mail);
	}

	double average(List<Double> rates) {
		double sum = rates.stream().mapToDouble(r -> r).sum();
		return sum / rates.size();
	}

}
