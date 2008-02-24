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

/*
 * Created on 23.04.2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package tal;

import util.Timer;

/**
 * @author martin
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Alarm {

	public static final int OFF = 0;
	public static final int ON = 1;
	public static final int OFF_PEND = 2;
	public static final int ON_PEND = 3;
	public static final int OFF_ON_PEND = 4;
	public static final int ON_OFF_PEND = 5;

	public static final int MB_OFF = 0;
	public static final int SB1_OFF = 8;
	public static final int SB2_OFF = 16;
	
	private static volatile boolean init;
	private static int[] pattern;
	private static Alarm list;

	private static void setPattern() {
		pattern = new int[] { 
			0, 0, 0, 0, 0, 0, 0, 0,		// OFF
			1, 1, 1, 1, 1, 1, 1, 1,		// ON
			0, 0, 0, 1, 0, 0, 0, 1,		// OFF_PEND
			1, 1, 1, 0, 1, 1, 1, 0,		// ON_PEND
			1, 1, 1, 0, 1, 0, 1, 0,		// OFF_ON_PEND
			0, 0, 0, 1, 0, 1, 0, 1,		// ON_OFF_PEND
		};
	}

	// construct Alarms befor scheduling
	// so we don't need to synchronize init.	
	private static void doInit() {
		
		setPattern();
		init = true;
		list = null;
	}
	
	public static boolean isPending() {
		
		boolean ret = false;
		for (Alarm ptr = list; ptr!=null; ptr = ptr.next) {
			if (ptr.state!=ON && ptr.state!=OFF) {
				ret = true;
				break;
			}
		}
		return ret;
	}
	public static int getOldMsg() {
		
		int val = 0;
		for (Alarm ptr = list; ptr!=null; ptr = ptr.next) {
			int nr = ptr.nr;
			if (nr>=0) {
				val |= ptr.getOldVal()<<nr;
			}
// Dbg.wr("get old"); Dbg.intVal(nr); Dbg.intVal(ptr.getOldVal());
		}
// Dbg.lf();
		return val;
	}

	public static int getNewMsg() {
		
		int val = 0;
		for (Alarm ptr = list; ptr!=null; ptr = ptr.next) {
			int nr = ptr.nr;
			if (nr>=0) {
				val |= ptr.getNewVal()<<nr;
			}
// Dbg.wr("get new"); Dbg.intVal(nr); Dbg.intVal(ptr.getNewVal());
		}
// Dbg.lf();
		return val;
	}
	
	public static void stateTransmitted(int oldVal, int newVal) {

		for (Alarm ptr = list; ptr!=null; ptr = ptr.next) {
			int nr = ptr.nr;
			if (nr>=0) {
				ptr.stateTransmitted(
						((oldVal>>nr)&1)==1,
						((newVal>>nr)&1)==1
					);
			}
		}
		
	}


	private int state;
	private int nr;
	private int minTime;
	private int timer;
	private Alarm next;
	
	/**
	 * @param number Position in transmitted telegram.
	 * 0..7 mb, 8..15 sb1, 16..23 sb2
	 * @param time 
	 */
	public Alarm(int number, int time) {

		// We don't need to synchronize this as
		// all alarms are allocated during initialization phase.		
// Dbg.wr("new Alarm "); Dbg.intVal(number); Dbg.lf();
		if (!init) doInit();
		state = OFF;
		nr = number;
		minTime = time;
		// for the first time
		timer = Timer.getTimeoutSec(0);
		next = list;
		list = this;
	}
	
	public void setState(boolean on) {

		on = timeFilter(on);

		switch (state) {
			case OFF:
				if (on) {
					state = ON_PEND;
				}
				break;
			case ON:
				if (!on) {
					state = OFF_PEND;
				}
				break;
			case OFF_PEND:
				if (on) {
					state = OFF_ON_PEND;
				}
				break;
			case ON_PEND:
				if (!on) {
					state = ON_OFF_PEND;
				}
				break;
			case OFF_ON_PEND:
				break;
			case ON_OFF_PEND:
				break;
		}
	}

	public int getState() {

		return state;
	}

	public boolean isOn() {
		
		return state==ON || state==ON_PEND || state==OFF_ON_PEND;
	}
	
	public boolean getLedPattern(int cnt) {

		cnt &= 0x07;
		return (pattern[state*8+cnt]==1);
	}
	
	private boolean timeFilter(boolean val) {

		if (minTime==0) return val;
		
		boolean is = state==ON || state==ON_PEND || state==OFF_ON_PEND;
		
		if (is!=val) {
			if (Timer.timeout(timer)) {
				return val;
			} else {
				return is;
			}
		}
		timer = Timer.getTimeoutSec(minTime);
		return val;
	}

	/**
	 * @return
	 */
	private int getOldVal() {
		
		switch (state) {
			case OFF:
				return 0;
			case ON:
				return 1;
			case OFF_PEND:
				return 1;
			case ON_PEND:
				return 0;
			case OFF_ON_PEND:
				return 0;
			case ON_OFF_PEND:
				return 1;
		}
		return 0;
	}

	/**
	 * @return
	 */
	private int getNewVal() {

		switch (state) {
			case OFF:
				return 0;
			case ON:
				return 1;
			case OFF_PEND:
				return 0;
			case ON_PEND:
				return 1;
			case OFF_ON_PEND:
				return 1;
			case ON_OFF_PEND:
				return 0;
		}
		return 0;
	}

	/**
	 * @param on
	 */
	private void stateTransmitted(boolean old, boolean nw) {
		
		switch (state) {
			case OFF:
				// no changes possible
				break;
			case ON:
				// no changes possible
				break;
			case OFF_PEND:
				if (!nw) {
					state = OFF;
				}
				break;
			case ON_PEND:
				if (nw) {
					state = ON;
				}
				break;
			case OFF_ON_PEND:
				if (!old && nw) {
					state = ON;
				}
				break;
			case ON_OFF_PEND:
				if (old && !nw) {
					state = OFF;
				}
				break;
		}
	}
}
