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

public class Latency {

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
	
	static class MFThread extends HFThread {

		int nr;

		public MFThread(int prio, int us) {
			super(prio, us);
		}
		
		void work() {
			sl.append(new Integer(nr));
//			synchronized (v) {
//				v.addElement(new Integer(nr));				
//			}
			++nr;
		}
	}
	static class LFThread extends HFThread {

		int expNr;
		
		public LFThread(int prio, int us) {
			super(prio, us);
		}
		
		void work() {
			
			Object o;
			while ((o = sl.remove())!=null) {
				if (((Integer) o).intValue()!=expNr) {
					System.out.println("List problem");					
				}
				++expNr;
			}
//			int size;
//			synchronized (v) {
//				size = v.size();
//			}
//			while (size!=0) {
//				Object o;
//				synchronized (v) {
//					o = v.remove(0);
//				}
//				if (((Integer) o).intValue()!=expNr) {
//					System.out.println("Vector problem");					
//				}
//				++expNr;
//				synchronized (v) {
//					size = v.size();
//				}
//			}
		}
	}
	
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
				Dbg.lf();
				if (hft!=null) {
					Dbg.wr("hft max=", hft.max);
					Dbg.wr("hft min=", hft.min);
				}
				if (mft!=null) {
					Dbg.wr("mft max=", mft.max);
					Dbg.wr("mft min=", mft.min);
				}
			}
		}

	}
	
	static HFThread hft;
	static MFThread mft;

	// Changes in the scheduler to get low latency values:
	//		TIM_OFF to 2
	//		omit time adjustments on a missed deadline in
	//		waitForNextPeriod()
	// Results for 100 MHz:
	// 		107 is without jitter when running it alone
	// TODO verify the following results:
	// with output thread 10 us
	// with prod/cons threads (no GC) 16 us
	// with GC 72 us (77 us)
	public static final int PERIOD_HIGH = 107; // 211; // 107;
	public static final int PERIOD_MEDIUM = 1009;
	public static final int PERIOD_LOW = 10853;
	public static final int PERIOD_GC = 200183;
	
	// for slower JOP versions (<100MHz)
//	public static final int PERIOD_HIGH = 200;
//	public static final int PERIOD_MEDIUM = 2000;
//	public static final int PERIOD_LOW = 20000;
//	public static final int PERIOD_GC = 400000;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Dbg.initSerWait();
//		v = new Vector(20);
		sl = new SimpleList();
		
		hft = new HFThread(5, PERIOD_HIGH);
		new LogThread (2, 1000*1000);
		
		mft = new MFThread(4, PERIOD_MEDIUM);
		new LFThread(3, PERIOD_LOW);
		new GCThread();
		
				
		RtThread.startMission();
		
		// that one is mandatory to get low latency!
		// check RtThreadImpl why.
		for (;;);
	}

}
