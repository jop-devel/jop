package rttm.skiplist;

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;
import com.jopdesign.sys.Startup;

public class Test {
	static SysDevice sys = IOFactory.getFactory().getSysDevice();

	private static AbstractSkipList skipList = null;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("started");
		// MS: this example need THREADS+1 CPUs
		// you could reduce it by also doing a SkipList thread in this
		// main thread.
		if (sys.nrCpu <= Const.THREADS) {
			System.out.println("Not enough CPUs for this example");
			System.exit(-1);
		}

		if (Const.run_kind == Const.RUN_KIND.TM)
			skipList = new SkipListTM_2();
		else
			skipList = new SkipListLock();
		
		TestThread[] threads = new TestThread[Const.THREADS];
		for (int i = 0; i < Const.THREADS; i++) {
			threads[i] = new TestThread(skipList);
			Startup.setRunnable(threads[i], i);
		}
		
		sys.signal = 1;

		while (true) {
			// Wait for the threads to complete
			int i = 0;
			for (i = 0; i < Const.THREADS; i++) {
				if (threads[i].finished != true)
					break;
			}
			if (i == Const.THREADS)
				break;
		}

		// Check that dead-reckoned sums agree with what we get
		// traversing the tree
		int s = skipList.Shadow;
		int a = skipList.Actual();

		if (s != a)
			System.out.println("mismatch: s= " + s + ", a = " + a);
		else
			System.out.println("match: s=a= " + s);
			
	}
}
