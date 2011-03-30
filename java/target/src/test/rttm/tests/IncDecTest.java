package rttm.tests;

import rttm.atomic;
import rttm.Diagnostics;

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;
import com.jopdesign.sys.Startup;

public class IncDecTest implements Runnable {

	static SysDevice sys = IOFactory.getFactory().getSysDevice();
	
	public static void main(String[] args) {
		SysDevice sys = IOFactory.getFactory().getSysDevice();
		
		for (int i = 1; i < sys.nrCpu; i++) {
			Runnable r = new IncDecTest();
			Startup.setRunnable(r, i-1);
		}
		
		sys.signal = 1;
		
		new IncDecTest().run();
		
		System.out.println(cnt);
		System.out.println(cnt);
		
		Diagnostics.stat();
		
		System.out.println(cnt);
	}
	
	protected IncDecTest() {
		
	}
	
	static volatile int cnt = 0;
	static final int ITERATIONS = 100000;
	static final int INCREMENTS = 10;
	
	public void run() {
		if ((sys.cpuId % 2) == 0) {
			for (int i = 0; i < ITERATIONS; i++) {
				if ((increment() % INCREMENTS) != 0) {
					throw new RuntimeException();
				}
				
			}
		} else {
			for (int i = 0; i < ITERATIONS; i++) {
				if ((decrement() % INCREMENTS) != 0) {
					throw new RuntimeException();
				}
			}
		}
		
		Diagnostics.saveStatistics();
	}
	
	@atomic protected int increment() {
		int result = cnt;
		for (int i = 0; i < INCREMENTS; i++) {
			cnt++;
		}
		return result;
	}
	
	@atomic protected int decrement() {
		int result = cnt;
		for (int i = 0; i < INCREMENTS; i++) {
			cnt--;
		}
		return result;
	}

}
