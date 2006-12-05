package sc4;

import java.util.*;

public abstract class RealtimeThread {
	
	int period;
	int deadline;
	int offset;
	int memSize;
	String event;
	
	static Vector eventList;

	
	/**
	 * A periodic real-time event, equivalent to a periodic thread
	 * @param period
	 * @param deadline
	 * @param offset
	 */
	protected RealtimeThread(int period, int deadline, int offset, int memSize) {

		if (eventList==null) {
			eventList = new Vector();
		}
		this.period = period;
		this.deadline = deadline;
		this.offset = offset;
		this.event = null;
		this.memSize = memSize;
		
		// insert in a deadline monotonic order
		int cnt = eventList.size();
		int pos;
		for (pos=0; pos<cnt; ++pos) {
			RealtimeThread re = (RealtimeThread) eventList.elementAt(pos);
			if (deadline<=re.deadline) {
				break;
			}
		}

		eventList.insertElementAt(this, pos);
	}
	
	
	/**
	 * A sporadic event with a minimum interarrival time
	 * @param event
	 * @param minInterval
	 * @param deadline
	 */
	protected RealtimeThread(String event, int minInterval, int deadline, int memSize) {
		
		this(minInterval, deadline, 0, memSize);
		this.event = event;
	}
	
	
	/**
	 * The logic for the event. run() gets invoked from the
	 * scheduler either periodic, or on a hardware event or
	 * on a software event (fire):
	 * @return true if ready for termination
	 */
	abstract protected boolean run();
	
	/**
	 * Gets invoked in the shutdown phase at the same period as
	 * run (instead of run()). Invoked until return true.
	 * @return true if shutdown is finished.
	 */
	protected boolean cleanup() {
		return true;
	}
}
