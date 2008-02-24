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
*	PrioritySchduler.java
*/

package jopurt;

import com.jopdesign.sys.Native;
import com.jopdesign.sys.Const;

public class PriorityScheduler extends Scheduler {

	protected static int cnt;
	protected static RtUserThread[] ref;		// references to threads

	final static int NO_EVENT = 0;
	final static int EV_FIRED = 1;
	final static int EV_WAITING = 2;
	static int event[];					// state of an event
	// allocated and set in start
	// ordered by priority
	// private static int next[];			// next time to change to state running
	static int next[];			// next time to change to state running

	public PriorityScheduler() {

		next = new int[1];
		ref = new RtUserThread[1];
		ref[0] = Scheduler.active;
		cnt = 1;			// stays 1 till start

	}

	// should not be static, but super.start() does not work
	// in JOP
	// public void start() {
	public static void start() {

		int i, c, startTime;
		RtUserThread th, mth;

		// if we have int's enabled for Thread scheduling
		// we have to place a monitorenter here
		c = RtUserThread.getCnt();

		mth = ref[0];		// this was our main thread

		ref = new RtUserThread[c];
		next = new int[c];
		event = new int[c];

		th = RtUserThread.head;
		// array is order according priority
		// top priority is last!
		for (i=c-1; th!=null; --i) {
			ref[i] = th;
			th.nr = i;
			if (th.isEvent) {
				event[i] = EV_WAITING;
			} else {
				event[i] = NO_EVENT;
			}
			th = th.next;
		}

		// change active if a lower priority
		// thread is befor main
		// active = mth.nr;		// we have set this in Scheduler()

		// wait 100 ms (for main Thread.debug())
		startTime = Native.rd(Const.IO_US_CNT)+100000;
		for (i=0; i<c; ++i) {
			next[i] = startTime+ref[i].offset;
		}

		// Start all threads (enables timer int!)
		Scheduler.start();
/* geht net!!!
	super is called with invokespecial, still not correct in JVM
		super.start();
*/

		cnt = c;
		started = true;
	}


	private final static int IDL_TICK = 10000;
	private final static int TIM_OFF = 300;		// TODO check usefull value
	//
	//	called by JVM
	//
	public void schedule() {

		int i, j, k;
		int diff, tim;

Native.wr(0, Const.IO_INT_ENA);
		// we have not called start(), which means
		// we perhaps have only one thread => just return
		if (!started) return;

		// SCHEDULE
		//	cnt should NOT contain idle thread
		//	change this some time
		k = IDL_TICK;

		// this is now
		j = Native.rd(Const.IO_US_CNT);

		for (i=cnt-1; i>0; --i) {

// can state be used for EVENT, DEAD,... ?
			if (event[i] == EV_FIRED) {
				break;						// a pending event found
			} else if (event[i] == NO_EVENT) {
				diff = next[i]-j;			// check only periodic
				if (diff < TIM_OFF) {
					break;					// found a ready task
				} else if (diff < k) {
					k = diff;				// next int time of higher prio task
				}
			}
		}
		// i is next ready thread (index in new list)
		// If none is ready i points to idle task or main thread (fist in the list)
		// set next int time to now+(min(diff)) (j, k)
		tim = j+k;

		j = Native.rd(Const.IO_US_CNT);
		// check if next timer value is too early (or allready missed)
		// ack int and schedule timer
		if (tim-j<TIM_OFF) {
			// set timer to now plus some short time
			tim = j+TIM_OFF;
		}

		dispatch(ref[i], tim);
		// no access to locals after this point
		// we are running in the NEW context!
	}


}
