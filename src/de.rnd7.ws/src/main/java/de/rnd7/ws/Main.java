package de.rnd7.ws;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.RaspiPin;

public class Main {
	public static void main(String[] args) {
		new Main();
	}
	
	private AtomicInteger counter = new AtomicInteger(0);
	
	public Main() {
		final GpioController controller = GpioFactory.getInstance();
		Button button = Button.create(controller, RaspiPin.GPIO_00);
		button.onPressed(this::triggered);

		final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		executor.scheduleAtFixedRate(this::takeValues, 1, 1, TimeUnit.SECONDS);
		
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
		counter.incrementAndGet();
	}
	
	private void takeValues() {
		double current = counter.getAndSet(0) * 2;
		
        // formula from: http://zieren.de/ip-anemometer/ 
        double windspeed = 1.761 / (1 + current) + 3.013 * current;  
        if (windspeed<=1.761) { 
            windspeed = 0; 
        } 
		
		System.out.println(String.format("wind speed: %s km/h", Math.round(windspeed)));
	}
}
