/*
 * Created on 22.10.2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package testrj;

import javax.realtime.AbsoluteTime;
import javax.realtime.RelativeTime;
import ravenscar.*;



/**
 * @author martin
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class MyEvent extends Initializer {

	public void run() {

		// init

		//
		//	an event handler
		//
		SporadicEventHandler evh = new SporadicEventHandler(
			new PriorityParameters(15),		// higher then periodic thread
			new SporadicParameters(
				new RelativeTime(33, 0),
				1							// what means buffer size?
			))
		{
			public void handleAsyncEvent() {

				System.out.print("fire!");
			}
		};

		//
		//	an sw event
		//
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
				public void run() {
					System.out.print("befor ");
					ev.fire();
					System.out.println(" after");
				}
			}
		);
	}


	public static void main(String[] args) {

		MyEvent ma = new MyEvent();
		ma.start();
	}
}
