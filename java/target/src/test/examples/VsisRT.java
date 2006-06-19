package examples;

import joprt.*;
import util.*;

public class VsisRT {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		new RtThread(10, 1000000) {
			public void run() {
				System.out.println("I'm a real-time thread");
				for (;;) {
					System.out.println("Ping");
					waitForNextPeriod();
				}
			}
		};
		
		new RtThread(11, 500000) {
			public void run() {
				System.out.println("I'm a 'faster' real-time thread");
				for (;;) {
					System.out.println("Pong");
					waitForNextPeriod();
				}
			}
		};
		System.out.println("Hello VISI class!");

		RtThread.startMission();
		
		// just do blink the LED
		for (;;) {
			RtThread.sleepMs(500);
			Timer.wd();
		}
	}

}
