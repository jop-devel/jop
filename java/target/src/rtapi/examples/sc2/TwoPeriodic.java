package examples.sc2;

import sc2.*;

public class TwoPeriodic {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		RtLogic pth1 = new RtLogic() {
			
			int counter;

			public void init() {
				counter = 0;
			}

			public void terminate() {
			}

			public void restart() {
			}

			public void run() {
				System.out.print("P1");
				++counter;
				if (counter==10) {
					RtMission.stop();
				}
			}			
		};

		RtLogic pth2 = new RtLogic() {

			public void init() {
			}

			public void terminate() {
			}

			public void restart() {
			}

			public void run() {
				System.out.print("P2");
			}			
		};

		new RtPeriodic(pth1, 500000);
		new RtPeriodic(pth2, 1000000);
		
		RtMission.prepare();
		
		RtMission.start();
		
		// this thread can terminate
	}

}
