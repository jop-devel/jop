package examples.sc1;

import safetycritical.RtEvent;
import safetycritical.RtSystem;

public class TwoPeriodic {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		new RtEvent(1000000) {
			protected boolean run() {
				System.out.println("P1");
				return true;
			}		
		};

		new MyEvent();
		
		RtSystem.start();
		
	}

}

class MyEvent extends RtEvent {
	
	int counter;
	
	public MyEvent() {
		super(2000000);
		counter = 0;
	}

	protected boolean run() {
		System.out.println("P2");
		++counter;
		if (counter==5) {
			RtSystem.stop();
		}
		return true;
	}
	
	protected boolean cleanup() {
		System.out.println("cleanup invoked!");
		return true;
	}
	
}