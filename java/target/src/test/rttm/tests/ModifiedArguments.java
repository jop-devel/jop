package rttm.tests;

import rttm.atomic;
import rttm.Diagnostics;

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;
import com.jopdesign.sys.Startup;

public class ModifiedArguments implements Runnable {

	protected static SysDevice sys = IOFactory.getFactory().getSysDevice();
	
	protected static int artificialConflict;	
	volatile protected static int[] results = new int[sys.nrCpu];
	
	protected static void main(String[] args) {
		for (int i = 1; i < sys.nrCpu; i++) {
			Runnable r = new ModifiedArguments();
			Startup.setRunnable(r, i-1);
		}
		
		sys.signal = 1;

		for (int i = 0; i < 1000; i++) {
			disquieter();
		}
		Diagnostics.saveStatistics();
		
		Diagnostics.stat();
		
		for (int i = 1; i < sys.nrCpu; i++) {
			System.out.println(results[i]);
		}
	}
	
	public void run() {
		results[sys.cpuId] = atomicMethod(0);
		
		Diagnostics.saveStatistics();
	}
	
	@atomic protected static void disquieter() {
		artificialConflict = 0;
	}
	
	@atomic protected int atomicMethod(int arg) {
		int ignored = artificialConflict;
		
		for (int i = 0; i < 10000; i++) {
			arg++;
		}
		
		return arg;
	}
}
