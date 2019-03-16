package de.rnd7.ws;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.RaspiPin;

import de.rnd7.ws.mqtt.MLMqttClient;

public class Main {
	public static void main(String[] args) {
		new Main();
	}

	private AtomicInteger rpsCounter = new AtomicInteger(0);
	private MLMqttClient mqtt = new MLMqttClient();
	private DescriptiveStatistics statistics = new DescriptiveStatistics();

	private Object mutex = new Object();

	public Main() {
		final GpioController controller = GpioFactory.getInstance();
		Button button = Button.create(controller, RaspiPin.GPIO_00);
		button.onPressed(this::triggered);

		final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		executor.scheduleAtFixedRate(this::takeValues, 1, 1, TimeUnit.SECONDS);
		executor.scheduleAtFixedRate(this::pushMqtt, 10, 10, TimeUnit.SECONDS);

		while (true) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
				Thread.currentThread().interrupt();
			}
		}
	}

	private void triggered() {
		this.rpsCounter.incrementAndGet();
	}

	private void takeValues() {
		double rps = this.rpsCounter.getAndSet(0) / 2;

		double windspeed = 1.761f / (1 + rps) + 3.013f * rps;
		if (windspeed <= 1.762) {
			windspeed = 0;
		}

		synchronized (mutex) {
			this.statistics.addValue(windspeed);
		}
	}

	private void pushMqtt() {
		final DescriptiveStatistics old = resetStatistics();
				
		int max = (int) Math.round(old.getMax());
		int mean = (int) Math.round(old.getMean());

		System.out.println(String.format("wind speed: %s km/h (%s km/h)", mean, max));

		this.mqtt.send(mean, max);
	}

	private DescriptiveStatistics resetStatistics() {
		synchronized (mutex) {
			DescriptiveStatistics result = this.statistics;
			this.statistics = new DescriptiveStatistics();
			return result;
		}
	}
}
