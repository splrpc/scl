package ch.springcloud.lite.core.event;

import org.springframework.context.ApplicationEvent;

public class ClientRefreshEvent extends ApplicationEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7206204230039397074L;

	public ClientRefreshEvent(Object source) {
		super(source);
	}

}
