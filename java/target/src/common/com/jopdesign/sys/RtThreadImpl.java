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

/**
*	RtThread.java
*/

package com.jopdesign.sys;

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;

import joprt.RtThread;

/**
 * @author Martin
 * 
 * Implementation class for the real-time thread RtThread.
 *
 */
public class RtThreadImpl {
	
	/**
	 * Helper class to start the other CPUs in a CMP system
	 * @author martin
	 *
	 */
	static class CMPStart implements Runnable {

		volatile boolean started;
		
		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			
			// Disable all interrupts globally and local - for sure
			Native.wr(0, Const.IO_INT_ENA);
			Native.wr(0, Const.IO_INTMASK);		

			// startThread for all threads that belong to this CPU.
			// This thread is already in state READY and not started.
			Scheduler s = Scheduler.sched[sys.cpuId];
			for (int i=0; i<s.cnt; ++i) {
				s.ref[i].startThread();
			}
			// add scheduler for the core s
			JVMHelp.addInterruptHandler(sys.cpuId, 0, s);

			started = true;

			// clear all pending interrupts (e.g. timer after reset)
			Native.wr(1, Const.IO_INTCLEARALL);
			// schedule timer in 10 ms
			Native.wr(RtThreadImpl.startTime, Const.IO_TIMER);
			Native.wr(1, Const.IO_INTCLEARALL);

			// enable all interrupts
			Native.wr(-1, Const.IO_INTMASK);		
			Native.wr(1, Const.IO_INT_ENA);

			// nothing to do in the main thread for the CMP cores 1 .. n-1
			for (;;) {
				;
			}
		}
	}

	// usual priority levels of java.lang.Thread
	public final static int MIN_PRIORITY = 1;
	public final static int NORM_PRIORITY = 5;
	public final static int MAX_PRIORITY = 10;

	// priority levels above Thread
	protected final static int RT_BASE = 2;
	protected final static int RT_IDLE = 1;


	RtThread rtt;		// reference to RtThread's run method
	private int priority;
	private int period;			// period in us
	private int offset;			// offset in us
	private boolean isEvent;	// it's a software event
	

	// only used in startMission
	final static int CREATED = 0;
	final static int READY = 1;		// READY means ready to run.
	final static int WAITING = 2;		// active is the running thread.
	final static int DEAD = 3;
	int state;

	int cpuId;			// core that the thread is running on
	// index in next, ref and event
	int nr;
	int[] stack;
	int sp;
	
	/**
	 * The scope that the thread is in.
	 * Set to initArea when started.
	 */
	Memory currentArea = null;
	
	/**
	 * The scope for the the initial thread (and between missions).
	 * Null until the Memory object that represents immortal is created.
	 */
	static Memory initArea;

	// linked list of threads in priority order
	// used only at initialization time to collect the threads
	private RtThreadImpl lower;
	private static RtThreadImpl head;


	static boolean initDone;
	static boolean mission;


	static SysDevice sys = IOFactory.getFactory().getSysDevice();


	//	no synchronization necessary:
	//	doInit() is called on first new RtThread() =>
	//	only one (this calling) thread is now runnable.
	//
	//	However, to avoid stack issues we can also call
	//	explicit
	public static void init() {

		if (initDone==true) return;
		initDone = true;
		mission = false;
		

		head = null;

		// We have now more than one thread =>
		// If we have 'normal' Threads we should start the timer!

	}

	RtThreadImpl(int prio, int us) {
	
		this(null, prio, us, 0);
	}

	public RtThreadImpl(RtThread rtt, int prio, int us, int off) {
		if (!initDone) {
			init();
		}

		stack = new int[Const.STACK_SIZE-Const.STACK_OFF];
		sp = Const.STACK_OFF;	// default empty stack for GC before startMission()
		
for (int i=0; i<Const.STACK_SIZE-Const.STACK_OFF; ++i) {
	stack[i] = 1234567;
}

		this.rtt = rtt;
		
		period = us;
		offset = off;
		if (us==0)	{					// this is NOT a RT thread
			priority = prio;
		} else {						// RT priority is above Thread priorities
			priority = prio+MAX_PRIORITY+RT_BASE;
		}
		state = CREATED;
		isEvent = false;

		//	insert in linked list, priority ordered
		//	highest priority first.
		//	same priority is ordered as first created has
		//	'higher' priority.
		RtThreadImpl th = head;
		RtThreadImpl prev = null;
		while (th!=null && priority<=th.priority) {
			prev = th;
			th = th.lower;
		}
		lower = th;
		if (prev!=null) {
			prev.lower = this;
		} else {
			head = this;
		}
	}

	/**
	 * Set the processor number
	 * @param id
	 */
	public void setProcessor(int id) {
		cpuId = id;
	}

	private static void genInt() {
		
		// just schedule an interrupt
		// schedule() gets called.
		Native.wr(0, Const.IO_SWINT);
		for (int j=0;j<10;++j) ;
	}

	private void startThread() {

		if (state!=CREATED) return;		// already called start

		// if we have interrupts enabled we have to synchronize

		if (period==0) {
			state = READY;			// for the idle thread, but we're not using one
		} else {
			state = WAITING;
		}

		// set memory context to the current one
		currentArea = initArea;
		
		createStack();

		// new thread starts right here after first scheduled

		if (mission) {		// main (startMission) falls through
			rtt.run();
			// if we arrive here it's time to delete runtime struct of thread
			// now do nothing!
			state = DEAD;
			for (;;) {
				// This will not work if we change stack like in Thread.java.
				// Then we have no reference to this.
				Scheduler.sched[sys.cpuId].next[nr] = Native.rd(Const.IO_US_CNT) + 2*Scheduler.IDL_TICK;
				genInt();
			}
		}
	}

	/**
	*	Create stack for the new thread.
	*	Copy stack frame of main thread.
	*	Could be reduced to copy only frames from 
	*	createStack() and startThread() and adjust the
	*	frames to new position.
	*/
	private void createStack() {

		int i, j, k;

		i = Native.getSP();					// sp of createStack();
		j = Native.rdIntMem(i-4);			// sp of calling function
		j = Native.rdIntMem(j-4);			// one more level of indirection

		sp = i-j+Const.STACK_OFF;
		k = j;
		for (; j<=i; ++j) {
			stack[j-k] = Native.rdIntMem(j);
		}
		//	adjust stack frames
		k -= Const.STACK_OFF;	// now difference between main stack and new stack
		stack[sp-Const.STACK_OFF-2] -= k;				// saved vp
		stack[sp-Const.STACK_OFF-4] -= k;				// saved sp
		j = stack[sp-Const.STACK_OFF-4];
		stack[j-Const.STACK_OFF-2] -= k;
		stack[j-Const.STACK_OFF-4] -= k;
		
/*	this is the save version
		i = Native.getSP();
		sp = i;
		for (j=Const.STACK_OFF; j<=i; ++j) {
			stack[j-Const.STACK_OFF] = Native.rdIntMem(j);
		}
*/
	}

//	public void run() {
//		;							// nothing to do
//	}

	/**
	 * Static start time of scheduling used by all cores
	 */
	static int startTime;

	public static void startMission() {


		int i, j, c;
		RtThreadImpl th;
		Scheduler s;

		if (!initDone) {
			init();
		}
		
		// TODO: we also disable acquiring the global lock - a running GC 
		// (on a different core) is not
		// protected for the write barriers at this time
		// Disable all interrupts globally and local - for sure
		Native.wr(0, Const.IO_INT_ENA);
		Native.wr(0, Const.IO_INTMASK);		



		// if we have int's enabled for Thread scheduling
		// or using the Scheduler interrupt
		// we have to place a monitorenter here
		
		// Collect number of thread for each core
		th = head;
		for (c=0; th!=null; ++c) {
			Scheduler.sched[th.cpuId].cnt++;
			th = th.lower;
		}

		for (i=0; i<sys.nrCpu; ++i) {
			Scheduler.sched[i].allocArrays();
		}
		
		// list is ordered with increasing priority
		// array is reverse priority ordered per core
		// top priority is last!
		for (th = head; th!=null; th = th.lower) {
			s = Scheduler.sched[th.cpuId];
			s.ref[s.tmp] = th;
			th.nr = s.tmp;
			if (th.isEvent) {
				s.event[s.tmp] = Scheduler.EV_WAITING;
			} else {
				s.event[s.tmp] = Scheduler.NO_EVENT;
			}
			s.tmp--;
		}

		for (i=0; i<sys.nrCpu; ++i) {
			Scheduler.sched[i].addMain();
		}

		// running threads (state!=CREATED)
		// are not started
		// TODO: where are 'normal' Threads placed?
		s = Scheduler.sched[sys.cpuId];
		for (i=0; i<s.cnt; ++i) {
			s.ref[i].startThread();
		}

		// wait 10 ms for the real start if the mission
		startTime = Native.rd(Const.IO_US_CNT)+10000;		
		for (i=0; i<sys.nrCpu; ++i) {
			s = Scheduler.sched[i];
			for (j=0; j<s.cnt; ++j) {
				s.next[j] = startTime+s.ref[j].offset;
			}
		}
		
		// add scheduler for the first core
		JVMHelp.addInterruptHandler(0, 0, Scheduler.sched[0]);


		CMPStart cmps[] = new CMPStart[sys.nrCpu-1];
		// add the Runnables to start the other CPUs
		for (i=0; i<sys.nrCpu-1; ++i) {
			cmps[i] = new RtThreadImpl.CMPStart();
			Startup.setRunnable(cmps[i], i);
			
		}
		
		// start the other CPUs
		sys.signal = 1;

		// busy wait for start threads of other cores
		for (;;) {
			boolean ready = true;
			for (i=0; i<sys.nrCpu-1; ++i) {
				ready = ready && cmps[i].started;
			}
			if (ready) {
				break;
			}
		}
		
		mission = true;

		// clear all pending interrupts (e.g. timer after reset)
		Native.wr(1, Const.IO_INTCLEARALL);
		// schedule timer in 10 ms
		Native.wr(startTime, Const.IO_TIMER);

		// enable all interrupts
		Native.wr(-1, Const.IO_INTMASK);		
		Native.wr(1, Const.IO_INT_ENA);
	}


	public boolean waitForNextPeriod() {

		int nxt, now;
		Scheduler s = Scheduler.sched[sys.cpuId];

		Native.wr(0, Const.IO_INT_ENA);

		nxt = s.next[nr] + period;

		now = Native.rd(Const.IO_US_CNT);
		if (nxt-now < 0) {					// missed time!
			s.next[nr] = now;				// correct next
//			next[nr] = nxt;					// without correction!
			Native.wr(1, Const.IO_INT_ENA);
			return false;
		} else {
			s.next[nr] = nxt;
		}
		// state is not used in scheduling!
		// state = WAITING;

		// just schedule an interrupt
		// schedule() gets called.
		Native.wr(0, Const.IO_SWINT);
		// will arrive before return statement,
		// just after interrupt enable
		// TODO: do we really need this loop?
		for (int j=0;j<10;++j) ;
		Native.wr(1, Const.IO_INT_ENA);

		// This return should only be executed when we are
		// scheduled again
		return true;			
	}

	public void setEvent() {
		isEvent = true;
	}

	public void fire() {
		Scheduler.sched[this.cpuId].event[this.nr] = Scheduler.EV_FIRED;
		// if prio higher...
// should not be allowed befor startMission
		// TODO: for cross CPU event fire we need to generate the interrupt
		// for the other core!
		genInt();

	}
	
	public void blockEvent() {
		Scheduler.sched[this.cpuId].event[this.nr] = Scheduler.EV_WAITING;
		// TODO: for cross CPU event fire we need to generate the interrupt
		// for the other core!
		genInt();

	}
	/**
	*	dummy yield() for compatibility reason.
	*/
//	public static void yield() {}


	/**
	*	for 'soft' rt threads.
	*/

	public static void sleepMs(int millis) {
	
		int next = Native.rd(Const.IO_US_CNT)+millis*1000;
		while (Native.rd(Const.IO_US_CNT)-next < 0) {
			genInt();
		}
	}
	final static int MIN_US = 10;

	/**
	 * Waste CPU cycles to simulate work.
	 * @param us execution time in us
	 */
	public static void busyWait(int us) {

		int t1, t2, t3;
		int cnt;
		
		cnt = 0;	
		t1 = Native.rd(Const.IO_US_CNT);

		for (;;) {
			t2 = Native.rd(Const.IO_US_CNT);
			t3 = t2-t1;
//			System.out.println(cnt+" "+t3);
			t1 = t2;
			if (t3<MIN_US) {
				cnt += t3;
			}
			if (cnt>=us) {
				return;
			}
		}
	}

//	 TODO: Decide how to protect the total root set (ref[].stack and active
//  stack while assembling it. Then some writebarrier should protect the 
//  references and downgrade the GC state from black to grey?

	// TODO: make it CMP aware
	static int[] getStack(int num) {
		return Scheduler.sched[sys.cpuId].ref[num].stack;
	}

	static int getSP(int num) {
		return Scheduler.sched[sys.cpuId].ref[num].sp;
	}

	static int getCnt() {
		return Scheduler.sched[sys.cpuId].cnt;
	}
	
	static int getActive() {
		return Scheduler.sched[sys.cpuId].active;
	}
	
	public static RtThread currentRtThread() {
		
		Scheduler s = Scheduler.sched[sys.cpuId];
		if (s.ref==null) {
			return null;
		}
		return s.ref[s.active].rtt;
	}
	
	static Memory getCurrentScope() {
		
//		JVMHelp.wr("getCurrent");
		// we call it only when the mission is already started
		Scheduler s = Scheduler.sched[sys.cpuId];
		return s.ref[s.active].currentArea;
		
		// but we could use that in general to encapsulate
		// scope set/get into this class

//		RtThreadImpl rtt = null;
//		if (Scheduler.sched==null) {
//			return null;
//		}
//		Scheduler s = Scheduler.sched[sys.cpuId];
//		if (s!=null || s.ref!=null) {
//			int nr = s.active;
//			rtt = s.ref[nr];
//		}
//		if (rtt==null) {
//			// we don't have started the mission
//			return null;
//		} else {
//			return rtt.currentArrea;			
//		}
	}
	
//	static void setCurrentScope(Scope sc) {
//		RtThreadImpl rtt = null;
//		Scheduler s = Scheduler.sched[sys.cpuId];
//		if (s!=null || s.ref!=null) {
//			int nr = s.active;
//			rtt = s.ref[nr];
//		}
//		rtt.currentArrea = sc;
//	}

	

static void trace(int[] stack, int sp) {

	int fp, mp, vp, addr, loc, args;
	int val;

	fp = sp-4;		// first frame point is easy, since last sp points to the end of the frame

	while (fp>Const.STACK_OFF+5) {	// stop befor 'fist' method
		mp = stack[fp+4-Const.STACK_OFF];
		vp = stack[fp+2-Const.STACK_OFF];
		val = Native.rdMem(mp);
		addr = val>>>10;			// address of callee
		util.Dbg.intVal(addr);

		val = Native.rdMem(mp+1);	// cp, locals, args
		args = val & 0x1f;
		loc = (val>>>5) & 0x1f;
		fp = vp+args+loc;			// new fp can be calc. with vp and count of local vars
	}
}





}
