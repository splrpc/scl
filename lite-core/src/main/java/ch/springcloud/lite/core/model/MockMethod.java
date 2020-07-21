package ch.springcloud.lite.core.model;

import java.lang.reflect.Method;

import lombok.Data;

@Data
public class MockMethod {

	int originId;
	String originService;
	String mockService;
	String originMethod;
	Method mockMethod;

}
