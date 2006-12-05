package examples.sc4;

import sc4.*;

public class PeriodicSporadic {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		final SporadicThread rte = new SporadicThread("SWEVENT", 2000000) {
			protected boolean run() {
				System.out.println("SW event fired");
				return true;
			}		
		};

		new PeriodicThread(1000000) {

			int counter = 0;
			
			protected boolean run() {
				System.out.println("P1");
				++counter;
				if (counter%2==1) {
					rte.fire();
				}
				if (counter==10) {
					RealtimeSystem.stop();
				}
				return true;
			}
			
			protected boolean cleanup() {
				System.out.println("cleanup invoked!");
				return true;
			}
			
		};
		
		RealtimeSystem.start();
	}

}