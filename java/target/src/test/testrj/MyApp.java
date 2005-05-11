/*
 * Created on 22.10.2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package testrj;

import javax.realtime.AbsoluteTime;
import javax.realtime.RelativeTime;

import util.Dbg;

/**
 * @author martin
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class MyApp extends Initializer {

	public void run() {
		// init
		PeriodicThread per = new PeriodicThread(
			new PriorityParameters(10),
			new PeriodicParameters(
				new AbsoluteTime(0, 0),
				new RelativeTime(100, 0)
			),
			new Runnable() {
				public void run() {
					Dbg.wr('*');
				}
			}
		);
	}
	public static void main(String[] args) {

		util.Dbg.initSerWait();

		MyApp ma = new MyApp();
		ma.start();
	}
}
