package rttm.tests;

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;
import rttm.atomic;
import rttm.Diagnostics;

public class EarlyCommitTest {

	protected static SysDevice sys = IOFactory.getFactory().getSysDevice();
	
	public static void main(String[] args) {
		for (int i = 0; i < ITERATIONS; i++) {
			increment();
		}
			
		for (int j = 0; j < data.length; j++) {
			System.out.println(data[j]);
		}
		
		Diagnostics.saveStatistics();
		Diagnostics.stat(0);
	}
	
	private static final int ITERATIONS = 17;
	private static final int ADDRS = 1<<6;
	private static volatile int[] data = new int[ADDRS];
	
	@atomic private static void increment() {
		for (int i = 0; i < data.length; i++) {
			data[i]++;
		}
	}
}
