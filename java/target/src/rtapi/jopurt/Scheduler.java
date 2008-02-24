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
*	Scheduler.java
*/

package jopurt;

import com.jopdesign.sys.Native;
import com.jopdesign.sys.Const;

public abstract class Scheduler {

	// allocated and set in start
	// ordered by priority (in Prio Sched.)
// should this be private or protected?
	protected static RtUserThread active;					// active thread



	private static boolean init;
	protected static boolean started;
	private static int monCnt;

	private static Scheduler single;

	public Scheduler() {

		// if (init) what shall we do?
		single = this;
		started = false;
		monCnt = 0;
		// Do we have to register the scheduler?
		// I think no.
		// RtUserThread.registerScheduler(this);

		if (init) return;
			// allready called by RtUserThread
			// no additional main Thread!!!

		init = true;

// activities from RtUserThread.doInit()

// should this go to Task some time ???
		// a thread for the main thread
//		active = new RtUserThread(Thread.NORM_PRIORITY, 0);
		active = new RtUserThread(5, 0);
		active.state = RtUserThread.READY;		// main thread is READY


		//	create one idle thread with Thread prio 0
		//	If we have a main thread with 'active' (yielding)
		//	sleep() this is not necessary.
		//
		//	Should be replaced by a Thread scheduler with
		//	RT_IDLE priority
/* main is now our idle task
		new RtUserThread(0, 0) {
			public void run() {
				for (;;) {
					util.Dbg.wr('i');
				}
			}
		};
*/

	}



	//
	//	called by RtUserThread
	//

	public void addTask(Task t) {}

	public void isDead(Task t) {}

	//
	//	called by application
	//
	// should not be static, but super.start() does not work
	// in JOP
	// public void start() {
	public static void start() {

		// running threads (state!=CREATED)
		// are not started
		// TODO: where are 'normal' Threads placed?
		RtUserThread th;
		for (th=RtUserThread.head; th!=null; th = th.next) {
			if (th.state == RtUserThread.CREATED) {
				th.start();
			}
		}

		// set moncnt in jvm.asm to zero to enable int's
		// on monitorexit from now on
		Native.wrIntMem(0, 5);
		// ack any 'pending' int and schedule timer in 10 ms
		Native.wr(Native.rd(Const.IO_US_CNT)+10000, Const.IO_TIMER);
		// enable int
		Native.wr(1, Const.IO_INT_ENA);
	}

	//
	//	called by JVM
	//
	//	this is the one and only function to
	//	switch threads.
	//	schedInt() is called from JVMHelp.interrupt()
	//	and should NEVER be called from somewhere
	//	else.
	//	Interrupts (also yield/genInt()) should NEVER
	//	ocour befor start is called (ref and active are set)
	public static void schedInt() {
		single.schedule();
	}


	public abstract void schedule();

	//
	//	called on a synchronized statement. Hooks to implement
	//	priority inversion avoidance protocol.
	//
	public void monitorEnter(Object o) {
		disableInt();
		++monCnt;
	}

	public void monitorExit(Object o) {
		--monCnt;
		if (monCnt==0) enableInt();
	}


	//
	//	support from JVM, RtUserThread
	//

	//
	//	Generate an interrupt. Schedule gets called
	//	from the JVM.
	//
	protected static final void genInt() {
		
		// just schedule an interrupt
		// schedule() gets called.
		Native.wr(0, Const.IO_SWINT);
	}

	protected static final void enableInt() {
	}

	protected static final void disableInt() {
	}

	//
	//	get number of threads
	//
	protected static final int getCnt() {

		return RtUserThread.getCnt();
	}

	protected static final Task getRunningTask() {

		return active;
	}
	//
	//	get 'system' time in us
	//
	public static final int getNow() {

		return Native.rd(Const.IO_US_CNT);
	}

	private static int s1;		// helper var for dispatch

	//
	//	Dispatch a thread and schedule next timer interrupt.
	//	Time is in 'system' time (us).
	//
	protected static final void dispatch(RtUserThread nextThread, int nextTim) {

		int i;

		Native.wr(0, Const.IO_INT_ENA);

			// ack int and schedule timer
			Native.wr(nextTim, Const.IO_TIMER);

			// save stack
			i = Native.getSP();
			RtUserThread th = active;
			th.sp = i;
			Native.int2extMem(128, th.stack, i-127);	// cnt is i-128+1

			// set new active thread
			active = nextThread;	

			// restore stack
			s1 = active.sp;
			// no 'old' locals are available from this point on
			Native.setVP(s1+2);		// +2 for shure ???
			Native.setSP(s1+7);		// +5 locals, take care to use only the first 5!!

			i = s1;
			// can't use s1-127 as count,
			// don't know why I have to store it in a local.
			Native.ext2intMem(active.stack, 128, i-127);		// cnt is i-128+1

			Native.setSP(i);
			// only return after setSP!
			// WHY should this be true? We need a monitorexit AFTER setSP().
			// It compiles to following:
			//	invokestatic #32 <Method void setSP(int)>
			//	aload 5
			//	monitorexit
			//	goto 283
			//	...
 			//	283 return
			//
			// for a 'real monitor' we have a big problem:
			// aload 5 loads the monitor from the OLD stack!!!
			//
			// we can't access any 'old' locals now
			//
			// a solution: don't use a monitor here!
			// disable and enable INT 'manual'
			// and DON'T call a method with synchronized
			// it would enable the INT on monitorexit

		Native.wr(1, Const.IO_INT_ENA);
	}


	//
	//	called by 'user' thread
	//

	public void block() {

		genInt();
	}

	//
	//	Attach and retrive a data structure to an object for
	//	prio. inv. prot.
	//
	protected static final void attachData(Object obj, Object schedData) {
	}

	protected static final Object getAttachedData(Object obj) {
		return null;
	}

}
