package examples.sc3;

import sc3.*;

public class PeriodicSporadic {


	public static void main(String[] args) {

		final RTlet rtl = new RTlet();

		final SporadicThread rte = new SporadicThread(
				new SporadicParamters(
						new RelativeTime(2000, 0),	// intervall
						new RelativeTime(0, 0),		// we don't know the cost
						new RelativeTime(2000, 0)	// deadline
						),
				"SWEVENT"
				) {
			public void run() {
				System.out.println("SW event fired");
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
				System.out.println("P1");
				++counter;
				if (counter%2==1) {
					rte.fire();
				}
				if (counter==10) {
					rtl.stopRT();
				}
			}
			
			public void cleanup() {
				System.out.println("cleanup invoked!");
			}
			
		};
		
		rtl.initializeRT();
		
		rtl.startRT();
	}

}