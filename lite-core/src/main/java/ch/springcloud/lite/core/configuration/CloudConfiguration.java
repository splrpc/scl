package ch.springcloud.lite.core.configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import ch.springcloud.lite.core.client.SystemMonitor;
import ch.springcloud.lite.core.codec.DefaultCloudCodec;
import ch.springcloud.lite.core.model.CloudClientMetadata;

@EnableAsync
@EnableScheduling
@ConditionalOnMissingBean(CloudConfiguration.class)
public class CloudConfiguration {

	@Bean
	ReadWriteLock clientLock() {
		return new ReentrantReadWriteLock();
	}

	@Bean
	Lock clientReadLock(ReadWriteLock clientLock) {
		return clientLock.readLock();
	}

	@Bean
	Lock clientWriteLock(ReadWriteLock clientLock) {
		return clientLock.writeLock();
	}

	@Bean
	ObjectMapper cloudMapper() {
		ObjectMapper mapper = new ObjectMapper().disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		return mapper;
	}

	@Bean
	CloudClientMetadata clientMetaData() {
		CloudClientMetadata metadata = new CloudClientMetadata();
		return metadata;
	}

	@Bean
	DefaultCloudCodec codec(ObjectMapper mapper) {
		return new DefaultCloudCodec(mapper);
	}

	@Bean
	ScheduledThreadPoolExecutor scheduledExecutorService() {
		ScheduledThreadPoolExecutor service = new ScheduledThreadPoolExecutor(
				Runtime.getRuntime().availableProcessors() * 2);
		return service;
	}

	@Bean
	SystemMonitor monitor() {
		return new SystemMonitor();
	}

	@Bean
	AsyncConfigurer asyncConfigurer() {
		return new AsyncConfigurer() {
			public Executor getAsyncExecutor() {
				int corePoolSize = Runtime.getRuntime().availableProcessors();
				int maximumPoolSize = corePoolSize;
				long keepAliveTime = 30000L;
				TimeUnit unit = TimeUnit.MICROSECONDS;
				BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(maximumPoolSize * 100);
				AtomicInteger threadid = new AtomicInteger(1);
				ThreadFactory factory = new ThreadFactory() {
					public Thread newThread(Runnable runnable) {
						Thread thread = new Thread(runnable);
						thread.setDaemon(true);
						thread.setName("TaskPool-thread-" + threadid.getAndIncrement());
						return thread;
					}
				};
				RejectedExecutionHandler policy = new AbortPolicy();
				ExecutorService pool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit,
						workQueue, factory, policy);
				return pool;
			}
		};
	}

}
