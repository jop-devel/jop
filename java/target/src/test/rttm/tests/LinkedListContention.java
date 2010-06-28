package rttm.tests;

import joprt.RtThread;

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;
import com.jopdesign.sys.Startup;
import rttm.atomic;
import rttm.common.ExitingRunnable;
import rttm.common.LinkedList;

public class LinkedListContention implements Runnable {

	protected static SysDevice sys = IOFactory.getFactory().getSysDevice();
	
	protected static final int MOVERS = 4;
	protected static final int INITIAL_SIZE = 100;
	
	protected static Object[] lists = new Object[MOVERS]; 
	
	public static void main(String[] args) {
		{
			for (int i = 0; i < lists.length; i++) {
				lists[i] = new LinkedList();
				for (int j = 0; j < INITIAL_SIZE; j++) {
					((LinkedList)lists[i]).insertAtTail(
							new LinkedList.LinkedObject(null));
				}
			}
		}
		{
			for (int i = 1; i < MOVERS; i++) {
				Runnable r = new LinkedListContention();
				Startup.setRunnable(r, i-1);
			}
			for (int i = MOVERS; i < sys.nrCpu; i++) {
				Startup.setRunnable(new ExitingRunnable(), i-1);
			}
			
			sys.signal = 1;
			
		}
				
		for (int i = 0; i < 5; i++) {
			RtThread.busyWait(1000*1000);
			snapshot();
		}
	}
	
	@atomic 
	protected static void snapshot() {
		// grab commit token to avoid being aborted
		rttm.Commands.earlyCommit();
		
		int sum = 0;
		for (int i = 0; i < lists.length; i++) {
			int size = ((LinkedList)lists[i]).size();
			sum += size;
		}
		
		rttm.Commands.earlyCommit();
		System.out.print("Sum: ");
		System.out.println(sum);
	}
	
	public void run() {
		int i = 0;
		while (true) {
			snatch(i++ % MOVERS);
		}
		
//		rttm.Diagnostics.saveStatistics();
	}
	
	@atomic
	void snatch(int from) {
		LinkedList.LinkedObject l = ((LinkedList)lists[from]).removeFromHead();
		if (l != null) {
			((LinkedList)lists[sys.cpuId]).insertAtTail(l);
		}
	}
}

