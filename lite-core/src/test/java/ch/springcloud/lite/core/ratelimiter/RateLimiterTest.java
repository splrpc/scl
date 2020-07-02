package ch.springcloud.lite.core.ratelimiter;

import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.util.concurrent.RateLimiter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RateLimiterTest {

	@Test
	public void test() throws InterruptedException {
		RateLimiter limiter = RateLimiter.create(100);
		TimeUnit.SECONDS.sleep(1);
		int missnum = 0;
		for (int i = 0; i < 200; i++) {
			boolean acquire = limiter.tryAcquire();
			log.info("Try acquire index {} of {}", i, acquire);
			if (i < 100) {
				Assert.assertTrue(acquire);
			}
			if (!acquire) {
				missnum++;
			}
		}
		Assert.assertTrue(missnum > 95);
	}

}
