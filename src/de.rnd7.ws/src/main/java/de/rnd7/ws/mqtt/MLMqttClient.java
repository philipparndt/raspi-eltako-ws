package de.rnd7.ws.mqtt;

import java.util.Optional;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MLMqttClient {
	private static final String TOPIC_MEAN = "ws/mean";
	private static final String TOPIC_MAX = "ws/max";
	
	private static final int QOS = 2;
	private static final String BROKER = "tcp://192.168.3.3:1883";
	private static final String CLIENTID = "ws";

	private static final Logger LOGGER = LoggerFactory.getLogger(MLMqttClient.class);

	private MemoryPersistence persistence = new MemoryPersistence();
	private Optional<MqttClient> client;

	public MLMqttClient() {
		client = connect();
	}
	
	private Optional<MqttClient> connect() {
		try {
			MqttClient result = new MqttClient(BROKER, CLIENTID, persistence);
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setCleanSession(true);
			result.connect(connOpts);
			return Optional.of(result);
		} catch (MqttException e) {
			LOGGER.error(e.getMessage(), e);
			return Optional.empty();
		}
	}

	public void send(final int windspeedMean, final int windspeedMax) {
		if (!client.isPresent()) {
			client = connect();
		}
		
		this.client.ifPresent(mqttClient -> {
			send(TOPIC_MAX, windspeedMax, mqttClient);
			send(TOPIC_MEAN, windspeedMean, mqttClient);
		});
	}

	private void send(String topic, final int windspeedMax, MqttClient mqttClient) {
		try {
			MqttMessage message = new MqttMessage(("" + windspeedMax).getBytes());
			message.setQos(QOS);
			mqttClient.publish(topic, message);
		} catch (MqttException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}
	
}
