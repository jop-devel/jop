package cmp;

import jbe.LowLevel;
import jbe.BenchMark;
import jbe.BenchLift;

public class LiftBenchCPU {
 
	public static int lift0 = 0;
	public static BenchMark bm0;
	public static int cnt = 16384;

	public static void main(String[] args) {

		// Initialization for benchmarking 
		int start = 0;
		int stop = 0;
		int time = 0;
		
		bm0 = new BenchLift();
				
		System.out.println("Application benchmarks:");
			
		start = LowLevel.timeMillis();		
		bm0.test(cnt);
		stop = LowLevel.timeMillis();
		
		System.out.println("StartTime: " + start);
		System.out.println("StopTime: " + stop);
		time = stop-start;
		System.out.println("TimeSpent: " + time);
		
	}		
}
