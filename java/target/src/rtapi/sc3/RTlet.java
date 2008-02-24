/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package sc3;

import java.util.*;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;
import com.jopdesign.sys.RtThreadImpl;

import joprt.RtThread;
import joprt.SwEvent;

public class RTlet {

	private static RTlet single;
	public RTlet() {
		if (single!=null) {
			throw new Error("Only a single RTlet is allowed");
		}
		single = this;
		// What are we now doing with this object?
	}
	
	static Vector rtaLst, rsaLst;
	
	public void pauseRT() {
		// I cannot and will not stop the threads in my applicatoin
	}
	
	boolean init = false;
	public void initializeRT() {
		// we could move some code here.
		// But it's also fine to do it in startRT().
		init = true;
	}
	
	/**
	 * Starts the real-time system (teh mission).
	 * All periodic and sporadic RT-events are scheduled. 
	 *
	 */
	public void startRT() {
		
		if (!init) {
			throw new Error("RTlet not initialized");
		}
		
		RtThreadImpl.init();
		
		rtaLst = new Vector();
		rsaLst = new Vector();
		int cnt = RealtimeTask.eventList.size();
		for (int i=0; i<cnt; ++i) {
			RealtimeTask re = (RealtimeTask) RealtimeTask.eventList.elementAt(i);
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
	public static void stopRT() {
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
	 * That's an implementation method, not part of the API
	 * @param re
	 */
	static void fire(RealtimeTask re) {
		
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
		return Native.rdMem(Const.IO_US_CNT);
	}
}

class RtThreadAdapter extends RtThread {
	
	boolean shutdown;
	RealtimeTask re;
	
	public RtThreadAdapter(RealtimeTask re, int prio, int us, int off) {
		super(prio, us, off);
		this.re = re;
	};
	
	void shutdown() {
		shutdown = true;
	}
	
	public void run() {
		
		boolean cleanupInvoked = false;
		for (;;) {
/* we cannot do it this way
			if (runReturn && shutdown) {
				if (!cleanupReturn) {
					cleanupReturn = re.cleanup();
				}
			} else {
				runReturn = re.run();				
			}
*/
			if (shutdown) {
				if (!cleanupInvoked) {
					re.cleanup();
					cleanupInvoked = true;
				}
			} else {
				re.run();
			}
			waitForNextPeriod();
		}
	}
}

class RtSwEventAdapter extends SwEvent {
	
	boolean shutdown;
	RealtimeTask re;
	
	public RtSwEventAdapter(RealtimeTask re, int prio, int us) {
		super(prio, us);
		this.re = re;
	};
	
	void shutdown() {
		shutdown = true;
	}
	
	public void handle() {

		// who invokes it for cleanup?
		if (!shutdown) {
			re.run();
		}
	}
}
