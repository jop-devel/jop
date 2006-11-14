package safetycritical;

import java.util.*;

import com.jopdesign.sys.RtThreadImpl;

import joprt.RtThread;
import joprt.SwEvent;

public class RtSystem {

	private RtSystem() {
		// no RtSystem object
	}
	
	static Vector rtaLst, rsaLst;
	
	/**
	 * Starts the real-time system (teh mission).
	 * All periodic and sporadic RT-events are scheduled. 
	 *
	 */
	public static void start() {
		
		RtThreadImpl.init();
		
		rtaLst = new Vector();
		rsaLst = new Vector();
		int cnt = RtEvent.eventList.size();
		for (int i=0; i<cnt; ++i) {
			RtEvent re = (RtEvent) RtEvent.eventList.elementAt(i);
			if (re.event==null) {
				// a normal periodic thread
				RtThreadAdapter rta = new RtThreadAdapter(re, cnt-i, re.period, re.offset);
				rtaLst.addElement(rta);
			} else {
				// a SW (or HW) event)
				RtSwEventAdapter rsa = new RtSwEventAdapter(re, cnt-i, re.period);
				rsaLst.addElement(rsa);
			}
		}
		
		RtThread.startMission();
	}
	/**
	 * Stop the real-time system (mission). When the event run() method returns
	 * true the cleanup() methods are invoked until returning true
	 * for a clean shutdown.
	 *
	 */
	public static void stop() {
		for (int i=0; i<rtaLst.size(); ++i) {
			((RtThreadAdapter) rtaLst.elementAt(i)).shutdown();
		}
		for (int i=0; i<rsaLst.size(); ++i) {
			((RtSwEventAdapter) rsaLst.elementAt(i)).shutdown();
			((RtSwEventAdapter) rsaLst.elementAt(i)).handle();
		}

	}
	
	/**
	 * Schedule a software event. 
	 * @param event
	 */
	public static void fire(String event) {
		
		// a simple linear search :-(
		for (int i=0; i<rsaLst.size(); ++i) {
			RtSwEventAdapter rsa = (RtSwEventAdapter) rsaLst.elementAt(i);
			if (rsa.re.event.equals(event)) {
				rsa.fire();
				break;
			}
		}
	}
	/**
	 * Schedule a software event.
	 * TODO: decide on which version is better: String or RtEvent
	 * @param re
	 */
	public static void fire(RtEvent re) {
		
		// a simple linear search :-(
		// could be better if not using the adapter stuff
		for (int i=0; i<rsaLst.size(); ++i) {
			RtSwEventAdapter rsa = (RtSwEventAdapter) rsaLst.elementAt(i);
			if (rsa.re==re) {
				rsa.fire();
				break;
			}
		}
	}

	/**
	 * Return the elapsed time from system startup in micro seconds.
	 * Wraps around all 4295 seconds.
	 * 
	 * @return
	 */
	public static int currentTimeMicro() {
		return 0;
	}
}

class RtThreadAdapter extends RtThread {
	
	boolean runReturn;
	boolean cleanupReturn;
	boolean shutdown;
	RtEvent re;
	
	public RtThreadAdapter(RtEvent re, int prio, int us, int off) {
		super(prio, us, off);
		this.re = re;
	};
	
	void shutdown() {
		shutdown = true;
	}
	
	public void run() {
		
		for (;;) {
			if (runReturn && shutdown) {
				if (!cleanupReturn) {
					cleanupReturn = re.cleanup();
				}
			} else {
				runReturn = re.run();				
			}
			waitForNextPeriod();
		}
	}
}

class RtSwEventAdapter extends SwEvent {
	
	boolean runReturn;
	boolean cleanupReturn;
	boolean shutdown;
	RtEvent re;
	
	public RtSwEventAdapter(RtEvent re, int prio, int us) {
		super(prio, us);
		this.re = re;
		// we assume true for a handler that was never called
		runReturn = true;
	};
	
	void shutdown() {
		shutdown = true;
	}
	
	public void handle() {

		// who invokes it for cleanup?
		if (runReturn && shutdown) {
			if (!cleanupReturn) {
				cleanupReturn = re.cleanup();
			}
		} else {
			runReturn = re.run();				
		}
	}
}
