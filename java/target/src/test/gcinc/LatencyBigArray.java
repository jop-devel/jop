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

package gcinc;

import java.util.Vector;

import util.Dbg;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.GC;
import com.jopdesign.sys.Native;

import joprt.RtThread;

public class LatencyBigArray {

	final static int ARRAY_LENGTH = 256*1024/4;
	final static int ARRAY_SIZE = ARRAY_LENGTH*4;
	final static int HEAP_SIZE = 300*1024;
	
	static class HFThread extends RtThread {

		public HFThread(int prio, int us) {
			super(prio, us);
			period = us;
		}

		int period;
		int expected;
		int max, min;
		int cnt;
		boolean notFirst;
		
		public void run() {

			for (;;) {
				waitForNextPeriod();
				int t = Native.rdMem(Const.IO_US_CNT);
				if (!notFirst) {
					expected = t+period;
					notFirst = true;
				} else {
					int diff = t-expected;
					if (diff>max) max = diff;
					if (diff<min) min = diff;
//					if (++cnt==1000000) {
//						result();
//					}				
					expected += period;					
				}
				work();
			}
		}
		
		void work() {
			// nothing for the HF thread
		}
		
		void result() {
			Dbg.wr("max=", max);
			Dbg.wr("max=", min);
			for (;;);
		}
	}
	
//	static Vector v;
	static SimpleList sl;
	
	
	static class GCThread extends RtThread {

		public GCThread() {
			super(1, PERIOD_GC);
			GC.setConcurrent();
		}
		
		public void run() {
			for (;;) {
				Dbg.wr('G');
				GC.gc();
				waitForNextPeriod();
			}
		}
	}
	
	/**
	 * Use Dbg instead 
	 * @author martin
	 *
	 */
	static class LogThread extends RtThread {
	
		public LogThread(int prio, int us) {
			super(prio, us);
		}

		public void run() {
			for (;;) {
				waitForNextPeriod();
				if (hft!=null) {
					Dbg.wr("hft max=", hft.max);
					Dbg.wr("hft min=", hft.min);
				}
			}
		}

	}
	
	static HFThread hft;

	// Changes in the scheduler to get low latency values:
	//		TIM_OFF to 2
	//		omit time adjustments on a missed deadline in
	//		waitForNextPeriod()
	// Results for 100 MHz:
	// 		107 is without jitter when running it alone
	public static final int PERIOD_HIGH = 107;
	public static final int PERIOD_GC = 1000; 
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Dbg.initSerWait();
//		v = new Vector(20);
		sl = new SimpleList();
		
		System.out.print("Number of arrays: ");
		System.out.println(HEAP_SIZE/ARRAY_SIZE);
		for (int i=0; i<HEAP_SIZE/ARRAY_SIZE; ++i) {
			sl.append(new int[ARRAY_LENGTH]);
		}
		
		
		hft = new HFThread(5, PERIOD_HIGH);
		new LogThread (2, 1000*1000);
		
		new GCThread();
		
		RtThread.startMission();
		
		// that one is mandatory to get low latency!
		// check RtThreadImpl why.
		for (;;);
	}

}
