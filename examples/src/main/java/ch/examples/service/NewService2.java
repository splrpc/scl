package ch.examples.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.springcloud.lite.core.mail.Sender;

@Service
public class NewService2 implements NewService {

	@Autowired
	Sender sender;

	@Override
	public Result call(int i) {
		Result result = new Result();
		result.setResult(sender.toString());
		return result;
	}

}
