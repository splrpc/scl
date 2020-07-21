package ch.springcloud.lite.core.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ch.springcloud.lite.core.model.MockMethod;

public class MockMethodRepository {

	Map<Integer, MockMethod> methods = new ConcurrentHashMap<>();

	public void add(MockMethod method) {
		this.methods.put(method.getOriginId(), method);
	}

	public MockMethod findOne(int id) {
		return methods.get(id);
	}

}
