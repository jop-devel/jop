package com.jopdesign.sys;

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;

/**
 * Represent the scheduler and thread queue for one CPU.
 * Created and set in startMisison()
 * 
 * @author martin
 *
 */
class Scheduler implements Runnable {
	// allocated and set in startMission
	// ordered by priority
	int next[];					// next time to change to state running
	RtThreadImpl[] ref;			// references to threads

	final static int NO_EVENT = 0;
	final static int EV_FIRED = 1;
	final static int EV_WAITING = 2;
	int event[];				// state of an event

	int cnt;					// number of threads
	int active;					// active thread number
	
	int tmp;					// counter to build the thread list
	
	static SysDevice sys = IOFactory.getFactory().getSysDevice();
	static Scheduler[] sched = new Scheduler[sys.nrCpu];
	static {
		// create scheduler objects for all cores
		for (int i=0; i<sys.nrCpu; ++i) {
			new Scheduler(i);

		}
	}
	
	Scheduler(int core) {
		active = 0;			// main thread (or idle thread) is first thread
		cnt = 0;			// stays 0 till startMission

//		next = new int[1];
//		ref = new RtThreadImpl[1];
		
		sched[core] = this;
	}
	
	/**
	 * If no thread gets ready within some time the scheduler is
	 * still invoked after IDL_TICK.
	 * TODO: a cross-core SW event is only detected at a scheduler
	 * invocation due to a thread on this core or at idle tick. 
	 */
	final static int IDL_TICK = 1000000;

	// use local memory for two values
	private final static int TIM_VAL_ADDR = 0x1e;
	private final static int SP_VAL_ADDR = 0x1f;
	
	// timer offset to ensure that no timer interrupt happens just
	// after monitorexit in this method and the new thread
	// has a minimum time to run.
	private final static int TIM_OFF = 200;
//	private final static int TIM_OFF = 20;
//	private final static int TIM_OFF = 2; // for 100 MHz version 20 or even lower
										 // 2 is minimum
	/**
	 * This is the scheduler invoked as a plain interrupt handler
	 * from JVMHelp.interrupt(). It gets invoked with interrupts
	 * globally disabled.
	 * 
	 * This is the one and only function to switch threads
	 * and should NEVER be called from somewhere else.
	 * 
	 * Interrupts (also yield/genInt()) should NEVER occur
	 * before startMission is called (ref and active are set)
	 * 
	 */
	public void run() {
		
		int i, j, k;
		int diff;
		Scheduler s;
		RtThreadImpl th;
		
		// we have not called doInit(), which means
		// we have only one thread => just return
		// Should actually not happen.
		if (!RtThreadImpl.initDone)  return;
		
		// take care to NOT invoke a method with monitorexit
		// can happen on the write barrier on reference assignment

		// save stack
		i = Native.getSP();
		th = ref[active];
		th.sp = i;
		Native.int2extMem(Const.STACK_OFF, th.stack, i-Const.STACK_OFF+1);	// cnt is i-Const.STACK_OFF+1

		// SCHEDULE
		//	cnt should NOT contain idle thread
		//	change this some time
		k = IDL_TICK;

		// this is now
		j = Native.rd(Const.IO_US_CNT);

		for (i=cnt-1; i>0; --i) {

			if (event[i] == EV_FIRED) {
				break;						// a pending event found
			} else if (event[i] == NO_EVENT) {
				diff = next[i]-j;			// check only periodic
				if (diff < TIM_OFF) {
					break;					// found a ready task
				} else if (diff < k) {
					k = diff;				// next interrupt time of higher priority thread
				}
			}
		}
		// i is next ready thread (index into the list)
		// If none is ready i points to idle task or main thread (fist in the list)
		active = i;

		// set next interrupt time to now+(min(diff)) (j, k)
		// use JVM locals to get time and sp over the stack exchange
		Native.wrIntMem(j+k, TIM_VAL_ADDR);

		
		// restore stack
		// We cannot use statics in a CMP setting!
		Native.wrIntMem(ref[i].sp, SP_VAL_ADDR);
		Native.setVP(Native.rdIntMem(SP_VAL_ADDR)+2);		// +2 for sure ???
		// +7 locals, take care to use only the first 1+6!!
		// so +9 should be ok, but it works only with +10 when
		// using all locals
		Native.setSP(Native.rdIntMem(SP_VAL_ADDR)+10);

		// all locals are lost now, even this - reassign them
		// get this back form the array of Schedulers		
		s = sched[sys.cpuId];
		th = s.ref[s.active];
		i = Native.rdIntMem(SP_VAL_ADDR);		
		
		// can't use s1-127 as count,
		// don't know why I have to store it in a local.
		
		Native.ext2intMem(th.stack, Const.STACK_OFF, i-Const.STACK_OFF+1);		// cnt is i-Const.STACK_OFF+1

		j = Native.rd(Const.IO_US_CNT);
		// check if next timer value is too early (or already missed)
		// ack timer interrupt and schedule timer
		if (Native.rdIntMem(TIM_VAL_ADDR)-j<TIM_OFF) {
			// set timer to now plus some short time
			Native.wr(j+TIM_OFF, Const.IO_TIMER);
		} else {
			Native.wr(Native.rdIntMem(TIM_VAL_ADDR), Const.IO_TIMER);
		}
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

	/**
	 * Allocate arrays for the thread list. One more than cnt for
	 * the initial main/Runnable
	 */
	void allocArrays() {

		// change active if a lower priority
		// thread is before main
//		tq.active = tq.ref[0].nr;		// this was our main thread

		// cnt one higher for start thread (main or Runnable)
		++cnt;
		ref = new RtThreadImpl[cnt];
		next = new int[cnt];
		event = new int[cnt];
		tmp = cnt-1;
	}

	/**
	 * Add main or Runnable to the list as last thread.
	 */
	public void addMain() {
		
		//	thread structure for main and the start Runnables
		// TODO: do we need to do a startThread for the CMP start Runnable?
		ref[0] = new RtThreadImpl(RtThreadImpl.NORM_PRIORITY, 0);
		ref[0].state = RtThreadImpl.READY;		// main thread is READY
		next[0] = 0;
		ref[0].nr = 0;
	}
}