/*
 * Created on 22.10.2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package testrj;

import javax.realtime.*;
import ravenscar.*;

import util.*;

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

				Dbg.wr("fire!");
			}
		};

		//
		//	an sw event
		//
		final SporadicEvent ev = new SporadicEvent(evh);


		//
		//	one periodic Thread
		//
		PeriodicThread per = new PeriodicThread(
			new PriorityParameters(10),
			new PeriodicParameters(
				new AbsoluteTime(0, 0),
				new RelativeTime(100, 0)
			),
			new Runnable() {
				public void run() {
					Dbg.wr("befor ");
					ev.fire();
					Dbg.wr(" after\n");
				}
			}
		);
	}


	public static void main(String[] args) {

		util.Dbg.initSerWait();

		MyEvent ma = new MyEvent();
		ma.start();
	}
}
