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
import safetycritical.RtSystem;


/**
 * A minimal ravenscar Java example.
 * @author martin
 *
 */
public class TwoPeriodic extends Initializer {

	static boolean stopRequest = false;
	
	public void run() {

		new PeriodicThread(				
			new PriorityParameters(10),
			new PeriodicParameters(
				new AbsoluteTime(0, 0),
				new RelativeTime(500, 0)
			),
			new Runnable() {
				boolean stopped = false;
				
				public void run() {
					if (!stopRequest) {
						System.out.println("P1");						
					} else if (!stopped) {
						stopped = cleanup();
					}
				}
				
				protected boolean cleanup() {
					System.out.println("cleanup in P1 invoked!");
					return true;
				}
			}
		);

		new PeriodicThread(
			new PriorityParameters(9),
			new PeriodicParameters(
				new AbsoluteTime(0, 0),
				new RelativeTime(1000, 0)
			),
			new Runnable() {
				int counter = 0;

				public void run() {
					if (!stopRequest) {
						++counter;
						System.out.println("P2");																		
						if (counter==5) {
							System.out.println("Stop request from P2");
							stopRequest = true;
						}
					}
				}
			}
		);

	}
	
	public static void main(String[] args) {

		new TwoPeriodic().start();
	}
}
