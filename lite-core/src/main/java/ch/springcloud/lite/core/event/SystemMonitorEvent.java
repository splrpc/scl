package ch.springcloud.lite.core.event;

import org.springframework.context.ApplicationEvent;

public class SystemMonitorEvent extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5921562345774876903L;

	public SystemMonitorEvent(Object source) {
		super(source);
	}

}
