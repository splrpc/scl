package ch.examples.service;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import ch.springcloud.lite.core.anno.Remote;
import ch.springcloud.lite.core.anno.Stub;

@Service
@Stub("newService2")
@Transactional
public class StubService2 implements NewService {

	@Remote("newService2")
	NewService newService2;
	@Remote
	NewService newService3;

	@Override	
	public Result call(int i) {
		System.out.println(hashCode());
		System.out.println("STUB2 !");
		return newService2.call(i);
	}

}
