package ch.springcloud.lite.core.connector;

import ch.springcloud.lite.core.model.CloudServer;

public interface LoadServerListener {

	void onLoad(CloudServer server);

}
