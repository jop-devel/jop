package wcet;

import joprt.*;
import util.Timer;
import wcet.kflapp.Mast;
import wcet.lift.LiftControl;
import wcet.lift.TalIo;

/**
 * The example for CPs JOP/DMa paper
 * 
 * @author martin
 *
 */
public class TwoThreads {

	private static LiftControl ctrl;
	private static TalIo io;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// initialization
		Mast.main(null);

		new RtThread(1, 3000) {
			
			public void run() {
				
				for (;;) {
					Mast.loop();
//					System.out.print("*");
					if (!waitForNextPeriod()) {
						System.out.println("Kfl missed a deadline");
					}
				}
			}
		};


		new RtThread(2, 500) {
			

			public void run() {

				ctrl = new LiftControl();
				io = new TalIo();
				waitForNextPeriod();
				
				for (;;) {
					ctrl.setVals();
					ctrl.getVals();
					ctrl.loop(io);
//					System.out.print('.');
					if (!waitForNextPeriod()) {
						System.out.println("Lift missed a deadline");
					}
				}
			}
		};

		System.out.println("Start Mission");
		RtThread.startMission();
		
		for (;;) {
			Timer.wd();
			RtThread.sleepMs(1000);
		}
	}

}
