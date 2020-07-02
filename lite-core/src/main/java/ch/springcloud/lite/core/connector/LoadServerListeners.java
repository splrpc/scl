package ch.springcloud.lite.core.connector;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class LoadServerListeners {

	public LoadServerListeners() {
		super();
		this.listeners = new ArrayList<>();
	}

	List<LoadServerListener> listeners;

	public void addListener(LoadServerListener listener) {
		this.listeners.add(listener);
	}

}
