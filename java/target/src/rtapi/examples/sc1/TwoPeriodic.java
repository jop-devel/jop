package examples.sc1;

import safetycritical.RtEvent;
import safetycritical.RtSystem;

public class TwoPeriodic {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		new RtEvent(500000) {
			protected boolean run() {
				System.out.println("P1");
				return true;
			}		
			protected boolean cleanup() {
				System.out.println("cleanup in P1 invoked!");
				return true;
			}
		};

		new RtEvent(1000000) {
			
			int counter = 0;
			
			protected boolean run() {
				System.out.println("P2");
				++counter;
				if (counter==5) {
					System.out.println("Stop request from P2");
					RtSystem.stop();
				}
				return true;
			}		
		};

		RtSystem.start();
		
	}

}