package examples.sc3;

import sc3.*;

public class TwoPeriodic {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		final RTlet rtl = new RTlet();

		new PeriodicThread(
				new PeriodicParameters(
						new RelativeTime(0,0),		// start
						new RelativeTime(500, 0),	// intervall
						new RelativeTime(0, 0),		// we don't know the cost
						new RelativeTime(500, 0)	// deadline
						)
				) {
			
			public void run() {
				System.out.println("P1");
			}		
			public void cleanup() {
				System.out.println("cleanup in P1 invoked!");
			}
		};

		new PeriodicThread(
				new PeriodicParameters(
						new RelativeTime(0,0),		// startt
						new RelativeTime(1000, 0),	// intervall
						new RelativeTime(0, 0),		// we don't know the cost
						new RelativeTime(1000, 0)	// deadline
						)
				) {
			
			int counter = 0;
			
			public void run() {
				System.out.println("P2");
				++counter;
				if (counter==5) {
					System.out.println("Stop request from P2");
					rtl.stopRT();
				}
			}		
		};

		rtl.initializeRT();
		
		rtl.startRT();
		
	}

}