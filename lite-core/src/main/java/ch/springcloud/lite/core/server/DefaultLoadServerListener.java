package ch.springcloud.lite.core.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import ch.springcloud.lite.core.client.ClientController;
import ch.springcloud.lite.core.connector.LoadServerListener;
import ch.springcloud.lite.core.model.CloudServer;
import ch.springcloud.lite.core.model.CloudServerMetaData;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultLoadServerListener implements LoadServerListener {

	@Autowired
	RestTemplate cloudTemplate;
	@Autowired
	CloudServerMetaData metadata;

	@Override
	public void onLoad(CloudServer server) {
		String url = "http://" + server.getActiveUrl() + ClientController.CLIENTCONTROLLER + ClientController.ATTACH;
		for (int i = 0; i < 3; i++) {
			try {
				cloudTemplate.put(url, metadata.snapshot());
				return;
			} catch (Exception e) {
				log.info("Attach {} fail for {}!", server.getMeta().getServerid(), e);
			}
		}
	}

}
