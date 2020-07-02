package ch.springcloud.lite.core.configuration;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;

import ch.springcloud.lite.core.connector.LoadServerListeners;
import ch.springcloud.lite.core.server.DefaultLoadServerListener;
import ch.springcloud.lite.core.server.ServerController;

public class DefaultCloudServerAutoConfiguration {

	@Bean
	ReadWriteLock serverLock() {
		return new ReentrantReadWriteLock();
	}

	@Bean
	Lock serverReadLock(ReadWriteLock serverLock) {
		return serverLock.readLock();
	}

	@Bean
	Lock serverWriteLock(ReadWriteLock serverLock) {
		return serverLock.writeLock();
	}

	@Bean
	@DependsOn("remoteRequestHandler")
	ServerController serverController() {
		return new ServerController();
	}

	@Bean
	LoadServerListeners listeners(DefaultLoadServerListener defaultLoadServerListener) {
		LoadServerListeners listeners = new LoadServerListeners();
		listeners.addListener(defaultLoadServerListener);
		return listeners;
	}

	@Bean
	DefaultLoadServerListener defaultLoadServerListener() {
		return new DefaultLoadServerListener();
	}

}
