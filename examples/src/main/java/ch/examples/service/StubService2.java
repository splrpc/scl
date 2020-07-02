package ch.examples.service;

import org.springframework.stereotype.Service;

import ch.springcloud.lite.core.anno.Remote;
import ch.springcloud.lite.core.anno.Stub;

@Service
@Stub("newService2")
public class StubService2 implements NewService {

	@Remote
	NewService newService2;
	@Remote("newService2")
	NewService newService3;

	@Override
	public Result call(int i) {
		System.out.println("STUB2 !");
		return newService2.call(2);
	}

}
