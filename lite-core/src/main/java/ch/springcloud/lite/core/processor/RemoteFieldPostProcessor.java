package ch.springcloud.lite.core.processor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import ch.springcloud.lite.core.anno.Remote;
import ch.springcloud.lite.core.anno.Stub;
import ch.springcloud.lite.core.client.SclMethodIntereptor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RemoteFieldPostProcessor implements BeanPostProcessor, ApplicationContextAware, PriorityOrdered {

	ApplicationContext ctx;

	Map<Class<?>, Map<String, Map<Remote, Object>>> remoteBeans = new ConcurrentHashMap<>();

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		Class<?> type = ClassUtils.getUserClass(bean.getClass());
		findRemoteFields(type).forEach((field, remote) -> {
			Object stub = getStubBean(type, field, remote);
			Object remoteBean = stub == null ? getRemoteBean(field, remote) : stub;
			try {
				field.setAccessible(true);
				field.set(bean, remoteBean);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
				throw new IllegalArgumentException(e);
			}
		});
		Field[] fields = type.getDeclaredFields();
		if (findRemoteFields(type).size() > 0)
			for (Field field : fields) {
				try {
					field.setAccessible(true);
					log.info(field.getDeclaringClass() + ":" + field + ":" + field.get(bean) + ":" + bean.hashCode());
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
	}

	Object getStubBean(Class<?> type, Field field, Remote remote) {
		String name = getName(field, remote);
		Map<String, ?> beansOfType = ctx.getBeansOfType(field.getType());
		List<String> rkeys = new ArrayList<>();
		for (String key : beansOfType.keySet()) {
			Object bean = beansOfType.get(key);
			Class<? extends Object> cls = ClassUtils.getUserClass(bean.getClass());
			if (!cls.isAnnotationPresent(Stub.class) || cls == type) {
				rkeys.add(key);
			}
			if (type.isAnnotationPresent(Stub.class) && cls.isAnnotationPresent(Stub.class)) {
				rkeys.add(key);
			}
		}
		rkeys.forEach(beansOfType::remove);
		if (beansOfType == null || beansOfType.isEmpty()) {
			return null;
		}
		if (beansOfType.size() == 1) {
			return beansOfType.values().stream().findAny().get();
		}
		if (beansOfType.containsKey(name)) {
			return beansOfType.get(name);
		}
		List<Object> beans = new ArrayList<>();
		for (Entry<String, ?> entry : beansOfType.entrySet()) {
			Object bean = entry.getValue();
			Class<? extends Object> cls = ClassUtils.getUserClass(bean.getClass());
			Stub[] annotationsByType = cls.getAnnotationsByType(Stub.class);
			for (Stub stub : annotationsByType) {
				if (stub.name().equals(name) || stub.value().equals(name)) {
					beans.add(bean);
				}
			}
		}
		if (beans.size() == 1) {
			return beans.get(0);
		}
		log.warn("Cannot determine stub of {}!", field);
		return null;
	}

	private Object getRemoteBean(Field field, Remote remote) {
		Class<?> type = field.getType();
		if (!remoteBeans.containsKey(type)) {
			remoteBeans.putIfAbsent(type, new ConcurrentHashMap<>());
		}
		Map<String, Map<Remote, Object>> allbeans = remoteBeans.get(type);
		String servicename = getName(field, remote);
		Map<String, Object> remoteAttributes = AnnotationUtils.getAnnotationAttributes(remote);
		remoteAttributes.put("name", servicename);
		if (!allbeans.containsKey(servicename)) {
			allbeans.putIfAbsent(servicename, new ConcurrentHashMap<>());
		}
		Map<Remote, Object> beans = allbeans.get(servicename);
		if (!beans.containsKey(remote)) {
			Enhancer enhancer = new Enhancer();
			enhancer.setSuperclass(type);
			SclMethodIntereptor intereptor = ctx.getBean(SclMethodIntereptor.class);
			try {
				Field remoteField = intereptor.getClass().getDeclaredField("remoteAttributes");
				remoteField.setAccessible(true);
				remoteField.set(intereptor, remoteAttributes);
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
			enhancer.setCallback(intereptor);
			Object remoteBean = enhancer.create();
			beans.putIfAbsent(remote, remoteBean);
		}
		return beans.get(remote);
	}

	String getName(Field field, Remote remote) {
		String servicename = remote.name();
		if (StringUtils.isEmpty(servicename)) {
			servicename = remote.value();
		}
		if (StringUtils.isEmpty(servicename)) {
			servicename = field.getName();
		}
		return servicename;
	}

	private Map<Field, Remote> findRemoteFields(Class<?> type) {
		Field[] fields = type.getDeclaredFields();
		Map<Field, Remote> table = new HashMap<>();
		for (Field field : fields) {
			Remote annotation = AnnotationUtils.getAnnotation(field, Remote.class);
			if (annotation != null) {
				table.put(field, annotation);
			}
		}
		return table;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.ctx = applicationContext;
	}

	@Override
	public int getOrder() {
		return Integer.MIN_VALUE;
	}

}
