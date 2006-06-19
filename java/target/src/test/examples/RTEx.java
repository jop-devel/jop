package examples;

import joprt.RtThread;
import util.Timer;

public class RTEx {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		new RtThread(10, 1000000) {
			
			public void run() {
				System.out.println("I'm a real-time thread");
				
				for (;;) {
					System.out.println("Ping");
				}
			}
		};
		
		
		new RtThread(11, 500000) {
			
			public void run() {
				System.out.println("I'm a faster real-time thread");
				
				for (;;) {
					System.out.println("Pong");
					waitForNextPeriod();
				}
			}
		};

		
		
		System.out.println("Hello VISI class!");
	
		RtThread.startMission();
		
		for (;;) {
			Timer.wd();
			RtThread.sleepMs(500);
		}
		
	}

}
