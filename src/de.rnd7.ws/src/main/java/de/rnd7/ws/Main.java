package de.rnd7.ws;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.RaspiPin;

public class Main {
	public static void main(String[] args) {
		new Main();
	}
	
	public Main() {
		final GpioController controller = GpioFactory.getInstance();
		Button button = Button.create(controller, RaspiPin.GPIO_00);
		button.onPressed(this::triggered);

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
		System.out.println("triggered");
	}
}
