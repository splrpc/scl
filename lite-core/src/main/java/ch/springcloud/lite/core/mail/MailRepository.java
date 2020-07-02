package ch.springcloud.lite.core.mail;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.springframework.beans.factory.annotation.Autowired;

public class MailRepository {

	@Autowired
	EmailInfo emailinfo;

	ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	ReadLock readLock = readWriteLock.readLock();
	WriteLock writeLock = readWriteLock.writeLock();

	public EmailInfo get() {
		readLock.lock();
		try {
			return copy(emailinfo);
		} finally {
			readLock.unlock();
		}
	}

	private EmailInfo copy(EmailInfo old) {
		EmailInfo copy = new EmailInfo();
		copy.setReceiver(new Receiver());
		copy.setSender(new Sender());
		doCopy(copy, old);
		return copy;
	}

	public void modify(EmailInfo info) {
		writeLock.lock();
		doCopy(emailinfo, info);
		writeLock.unlock();
	}

	private void doCopy(EmailInfo info, EmailInfo old) {
		info.setCpuratio(old.getCpuratio());
		info.setMemoryratio(old.getMemoryratio());
		info.setOpen(old.isOpen());
		info.setDuration(old.getDuration());
		{
			Receiver receiver = info.getReceiver();
			Receiver oldreceiver = old.getReceiver();
			receiver.setAddress(oldreceiver.getAddress());
			receiver.setNickname(oldreceiver.getNickname());
		}
		{
			Sender sender = info.getSender();
			Sender oldsender = old.getSender();
			sender.setAccount(oldsender.getAccount());
			sender.setNickname(oldsender.getNickname());
			sender.setPassword(oldsender.getPassword());
			sender.setSmtphost(oldsender.getSmtphost());
		}
	}

}
