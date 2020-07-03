package ch.examples.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.springcloud.lite.core.model.CloudServerMetaData;

@Service
public class NewService2 implements NewService {

	@Autowired
	InsertTestBeanRepository repository;
	@Autowired(required = false)
	CloudServerMetaData cloudServerMetadata;

	@Override
	@Transactional
	public Result call(int i) {
		Result result = new Result();
		InsertTestBean bean = new InsertTestBean();
		bean.setName(cloudServerMetadata == null ? "Server" : cloudServerMetadata.getName() + " - bean - " + i);
		result.setResult(repository.save(bean).toString());
		return result;
	}

}
