package ch.examples.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.springcloud.lite.core.anno.LimitMock;
import ch.springcloud.lite.core.model.CloudServerMetaData;

@Service
public class NewService2 implements NewService {

	@Autowired(required = false)
	CloudServerMetaData cloudServerMetadata;

	@Override
	public Result call(int i) {
		Result result = new Result();
		result.setResult(UUID.randomUUID().toString());
		return result;
	}

	@LimitMock(method = "call")
	public Result callMock(int j) {
		Result result = new Result();
		result.setResult("MOCK!");
		return result;
	}

}
