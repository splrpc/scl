package ch.examples.service;

import org.springframework.stereotype.Service;

import ch.springcloud.lite.core.anno.Remote;
import ch.springcloud.lite.core.anno.Stub;

@Service
@Stub
public class StubService implements NewService {

	@Remote
	NewService newService2;

	@Override
	public Result call(int i) {
		System.out.println("STUB !");
		return newService2.call(2);
	}

}
