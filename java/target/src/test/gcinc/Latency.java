package gcinc;

import java.util.Vector;

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
		
		public void run() {
			int t;

			waitForNextPeriod();
			t = Native.rdMem(Const.IO_US_CNT);
			expected = t+period;
			
			for (;;) {
				waitForNextPeriod();
				t = Native.rdMem(Const.IO_US_CNT);
				int diff = t-expected;
				if (diff>max) {
					max = diff;
				} else if (diff<min) {
					min = diff;
				}
//				if (++cnt==100000) {
//					result();
//				}				
				expected = expected+period;
				work();
			}
		}
		
		void work() {
			// nothing for the HF thread
		}
		
		void result() {
			System.out.println("max="+max);
			System.out.println("min="+min);
			for (;;);
		}
	}
	
	static Vector v;
	
	static class MFThread extends HFThread {

		int nr;

		public MFThread(int prio, int us) {
			super(prio, us);
		}
		
		void work() {
			synchronized (v) {
				v.addElement(new Integer(nr));				
			}
			++nr;
		}
	}
	static class LFThread extends HFThread {

		int expNr;
		
		public LFThread(int prio, int us) {
			super(prio, us);
		}
		
		void work() {
			int size;
			synchronized (v) {
				size = v.size();
			}
			while (size!=0) {
				Object o;
				synchronized (v) {
					o = v.remove(0);
				}
				if (((Integer) o).intValue()!=expNr) {
					System.out.println("Vector problem");					
				}
				++expNr;
				synchronized (v) {
					size = v.size();
				}
			}
		}
	}
	
	static class GCThread extends RtThread {

		public GCThread() {
			super(1, PERIOD_GC);
			GC.setConcurrent();
		}
		
		public void run() {
			for (;;) {
				System.out.print("G");
				GC.gc();
				waitForNextPeriod();
			}
		}
		
	}
	static HFThread hft;
	static MFThread mft;

	// 300 is without jitter when running it alone
	// 500 without jitter when a second dummy thread runs
	public static final int PERIOD_HIGH = 500;
	public static final int PERIOD_MEDIUM = 1234;
	public static final int PERIOD_LOW = 10235;
	public static final int PERIOD_GC = 20000;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		v = new Vector(20);
		
		hft = new HFThread(10, PERIOD_HIGH);
		mft = new MFThread(9, PERIOD_MEDIUM);
		new LFThread(8, PERIOD_LOW);
		
		new GCThread();
		
		new RtThread (2, 5*1000*1000) {
			public void run() {
				for (;;) {
					waitForNextPeriod();
					System.out.println();
					System.out.print("hft max=");
					System.out.println(hft.max);
					System.out.print("hft min=");
					System.out.println(hft.min);					
					System.out.print("mft max=");
					System.out.println(mft.max);
					System.out.print("mft min=");
					System.out.println(mft.min);					
				}
			}
		};
		
		RtThread.startMission();
		
		// that one is mandatary to get low latency!
		// check RtThreadImpl why.
		for (;;);
	}

}
