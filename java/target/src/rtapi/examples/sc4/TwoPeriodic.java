package examples.sc4;

import sc4.*;

public class TwoPeriodic {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		new PeriodicThread(500000) {
			protected boolean run() {
				System.out.println("P1");
				return true;
			}		
			protected boolean cleanup() {
				System.out.println("cleanup in P1 invoked!");
				return true;
			}
		};

		new PeriodicThread(1000000) {
			
			int counter = 0;
			
			protected boolean run() {
				System.out.println("P2");
				++counter;
				if (counter==5) {
					System.out.println("Stop request from P2");
					RealtimeSystem.stop();
				}
				return true;
			}		
		};

		RealtimeSystem.start();
		
	}

}