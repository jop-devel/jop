package examples.sc1;

import safetycritical.RtEvent;
import safetycritical.RtSystem;

public class PeriodicSporadic {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		final RtEvent rte = new RtEvent("SWEVENT", 2000000) {
			protected boolean run() {
				System.out.println("SW event fired");
				return true;
			}		
		};

		new RtEvent(1000000) {

			int counter = 0;
			
			protected boolean run() {
				System.out.println("P1");
				++counter;
				if (counter%2==1) {
					RtSystem.fire(rte);
				}
				if (counter==10) {
					RtSystem.stop();
				}
				return true;
			}
			
			protected boolean cleanup() {
				System.out.println("cleanup invoked!");
				return true;
			}
			
		};
		
		RtSystem.start();
	}

}