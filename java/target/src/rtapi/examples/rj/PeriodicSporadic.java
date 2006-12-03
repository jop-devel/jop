/*
 * Created on 22.10.2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package examples.rj;

import javax.realtime.AbsoluteTime;
import javax.realtime.RelativeTime;
import ravenscar.*;

public class PeriodicSporadic extends Initializer {

	static boolean stopRequest = false;

	public void run() {

		SporadicEventHandler evh = new SporadicEventHandler(
			new PriorityParameters(9),		// lower then periodic thread
			new SporadicParameters(
				new RelativeTime(2000, 0),
				1							// what means buffer size?
			)) {

			public void handleAsyncEvent() {

				System.out.println("SW event fired");
			}
		};

		final SporadicEvent ev = new SporadicEvent(evh);


		//
		//	one periodic Thread
		//
		new PeriodicThread(
			new PriorityParameters(10),
			new PeriodicParameters(
				new AbsoluteTime(0, 0),
				new RelativeTime(1000, 0)
			),
			new Runnable() {
				int counter = 0;
				boolean stopped = false;

				public void run() {
					if (!stopRequest) {
						System.out.println("P1");
						++counter;
						if (counter%2==1) {
							ev.fire();
						}
						if (counter==10) {
							stopRequest = true;
						}
					} else if (!stopped) {
						stopped = cleanup();
					}
				}
				
				protected boolean cleanup() {
					System.out.println("cleanup invoked!");
					return true;
				}
			}
		);
	}


	public static void main(String[] args) {

		new PeriodicSporadic().start();
	}
}
