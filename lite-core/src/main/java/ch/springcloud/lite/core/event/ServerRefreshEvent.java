package ch.springcloud.lite.core.event;

import org.springframework.context.ApplicationEvent;

public class ServerRefreshEvent extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4638011403092159536L;

	public ServerRefreshEvent(Object source) {
		super(source);
	}

}
