/**
*	RtThread.java
*/

package joprt;

import com.jopdesign.sys.Native;

public class RtThread {

	private static PriorityScheduler scheduler;

	// priority levels above Thread
	protected final static int RT_BASE = 2;
	protected final static int RT_IDLE = 1;


	private int priority;
	private int period;			// period in us
	int offset;			// offset in us

	// index in next, ref and event
	int nr;
/********************** changes for UserSchduler ************/
	// private int[] stack;
	// private int sp;
	int[] stack;
	int sp;

/********************** changes for UserSchduler ************/

	boolean isEvent;

	// linked list of threads in priority order
	public RtThread next;
	public static RtThread head;

	// only used in startMission
	final static int CREATED = 0;
	final static int READY = 1;		// READY means ready to run.
	final static int WAITING = 2;		// active is the running thread.
	final static int DEAD = 3;
	int state;

	private final static int MAX_STACK = 128;

	private static boolean init;


	protected static Object monitor;

	//	no synchronization necessary:
	//	doInit() is called on first new Thread() =>
	//	only one (this calling) thread is now runnable.
	private static void doInit() {

		init = true;

		monitor = new Object();
		head = null;

		scheduler = new PriorityScheduler();

	}

	public RtThread(int prio, int us) {
	
		this(prio, us, 0);
	}

	public RtThread(int prio, int us, int off) {

		if (!init) {
			doInit();
		}

		stack = new int[MAX_STACK];

		period = us;
		offset = off;
		if (us==0)	{					// this is NOT a RT thread
			priority = prio;
		} else {						// RT prio is above Thread prios.
			priority = prio+Thread.MAX_PRIORITY+RT_BASE;
		}
		state = CREATED;
		isEvent = false;

		//	insert in linked list, priority ordered
		//	highest priority first.
		//	same priority is ordered as first created has
		//	'higher' priority.
		RtThread th = head;
		RtThread prev = null;
		while (th!=null && priority<=th.priority) {
			prev = th;
			th = th.next;
		}
		next = th;
		if (prev!=null) {
			prev.next = this;
		} else {
			head = this;
		}
	}


	static int getCnt() {

		int c;

		RtThread th = RtThread.head;
		for (c=0; th!=null; ++c) {	// count number of threads
			th = th.next;
		}

		return c;
	}


//	time stamps:
public static int ts0, ts1, ts2, ts3, ts4;

	void start() {

		if (state!=CREATED) return;		// allread called start

		// if we have int enabled we have to synchronize

		createStack();

		// new thread starts right here after first scheduled

		// This will not work is we change stack like in Thread.java (setSP() befor run()).
		// Then we have no reference to this.
		if (state!=CREATED) {	// calling thread falls through

// was the 'old' way to distingish between the two threads
// if (mission) {		// main (startMission) falls through
			run();
			// if we arrive here it's time to delete runtime struct of thread
			// now do nothing!
			// but we have a static var active which points in the ref
			state = DEAD;
			for (;;) {
				PriorityScheduler.next[nr] = Native.rd(Native.IO_US_CNT) + 2*10000;
				Scheduler.genInt();
			}
		}

		// change state for the thread to call run, when it get's 
		// scheduled
		// However, state is not used anywhere else
		if (period==0) {
			state = READY;			// for the idle thread
		} else {
			state = WAITING;
		}

	}

	/**
	*	Create stack for the new thread.
	*	Copy stack frame of main.
	*	Could be reduced to copy only frames from 
	*	createStack() and start() and adjust the
	*	frames to new position.
	*/
	private void createStack() {

		int i, j, k;

		i = Native.getSP();					// sp of createStack();
		j = Native.rdIntMem(i-4);			// sp of calling function
		j = Native.rdIntMem(j-4);			// one more level of indirection

		sp = i-j+128;
		k = j;
		for (; j<=i; ++j) {
			stack[j-k] = Native.rdIntMem(j);
		}
		//	adjust stack frames
		k -= 128;	// now difference between main stack and new stack
		stack[sp-128-2] -= k;				// saved vp
		stack[sp-128-4] -= k;				// saved sp
		j = stack[sp-128-4];
		stack[j-128-2] -= k;
		stack[j-128-4] -= k;
		
/*	this is the save version
		i = Native.getSP();
		sp = i;
		for (j=128; j<=i; ++j) {
			stack[j-128] = Native.rdIntMem(j);
		}
*/
	}

	public void run() {
		;							// nothing to do
	}


	public static void startMission() {

		scheduler.start();
	}













	public boolean waitForNextPeriod() {

		synchronized(monitor) {

			int nxt, now;

			nxt = PriorityScheduler.next[nr] + period;

			now = Native.rd(Native.IO_US_CNT);
			if (nxt-now < 0) {					// missed time!
				PriorityScheduler.next[nr] = now;					// correct next
				return false;
			} else {
				PriorityScheduler.next[nr] = nxt;
			}
			// state is not used in scheduling!
			// state = WAITING;

			// just schedule an interrupt
			// schedule() gets called.
			Native.wr(1, Native.IO_SWINT);
			// will arrive befor return statement,
			// just after monitorexit
		}
		return true;
	}


	/**
	*	dummy yield() for compatibility reason.
	*/
	public static void yield() {}


	/**
	*	for 'soft' rt threads.
	*/

	public static void sleepMs(int millis) {
	
		int next = Native.rd(Native.IO_US_CNT)+millis*1000;
		while (Native.rd(Native.IO_US_CNT)-next < 0) {
			Scheduler.genInt();
		}
	}

}
