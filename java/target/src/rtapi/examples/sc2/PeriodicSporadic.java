package examples.sc2;

import sc2.*;

public class PeriodicSporadic {

	/**
	 * @param args
	 */
	public static void main(String[] args) {


		RtLogic sth = new RtLogic() {

			public void init() {
			}

			public void terminate() {
			}

			public void restart() {
			}

			public void run() {
				System.out.print("S fired");
			}			
		};

		RtSporadic rsp = new RtSporadic("", sth, 2000000);
		MyPeriodicLogic mp = new MyPeriodicLogic();
		mp.setEvent(rsp);
		new RtPeriodic(mp, 1000000);
		
		RtMission.prepare();
		
		RtMission.start();
		
		// this thread can terminate
	}

}

class MyPeriodicLogic implements RtLogic {
	
	int counter;
	RtSporadic event;

	public void init() {
		counter = 0;
	}
	
	public void setEvent(RtSporadic se) {
		event = se;
	}

	public void terminate() {
	}

	public void restart() {
	}

	public void run() {
		System.out.print("P");
		++counter;
		if (counter%2==1) {
			event.fire();
		}
		if (counter==10) {
			RtMission.stop();
		}
	}			

}
