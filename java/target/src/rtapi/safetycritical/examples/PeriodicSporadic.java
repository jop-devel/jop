package safetycritical.examples;

import safetycritical.RtEvent;
import safetycritical.RtSystem;

public class PeriodicSporadic {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		new RtEvent("SWEVENT", 1000000) {
			protected boolean run() {
				System.out.println("SW event fired");
				return true;
			}		
		};

		new MyPeriodic();
		
		RtSystem.start();
	}

}

class MyPeriodic extends RtEvent {
	
	int counter;
	
	public MyPeriodic() {
		super(1000000);
		counter = 0;
	}

	protected boolean run() {
		System.out.println("P2");
		++counter;
		if (counter%2==1) {
			RtSystem.fire("SWEVENT");
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
	
}