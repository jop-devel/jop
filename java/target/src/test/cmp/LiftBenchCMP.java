/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008, Christof Pitter

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

package cmp;

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;
import com.jopdesign.sys.Startup;

import jbe.LowLevel;
import jbe.BenchMark;
import jbe.BenchLift;

public class LiftBenchCMP implements Runnable {
 
	public static int cnt = 10000;
	public static int signal = 0;
	static Object lock;
	public static BenchMark [] bm_array;
	public static int [] loop_array;
	
	int cpu_id;
	int loop_cnt;
	BenchMark bm;
	
	public LiftBenchCMP (int identity, int loop_count, BenchMark benchmark){
		cpu_id = identity;
		loop_cnt = loop_count;
		bm = benchmark;
	}

	public static void main(String[] args) {

		// Initialization for benchmarking 
		int start = 0;
		int stop = 0;
		int time = 0;
		
		System.out.println("Lift Benchmark:");
		
		SysDevice sys = IOFactory.getFactory().getSysDevice();
		bm_array = new BenchMark [sys.nrCpu];
		loop_array = new int [sys.nrCpu];
		
		// Calculates the number of loops for each CPU
		int rest = cnt%sys.nrCpu;
		
		for (int j=0; j<sys.nrCpu; j++){			
			if (rest != 0){
				loop_array[j] = cnt/sys.nrCpu+1;
				rest--;}
			else
				loop_array[j] = cnt/sys.nrCpu;	
		}
		
		
		bm_array[0] = new BenchLift();
		
		for (int i=0; i<sys.nrCpu-1; i++) {
			bm_array[i+1] = new BenchLift();
			Runnable r = new LiftBenchCMP(i+1, loop_array[i+1], bm_array[i+1]);
			Startup.setRunnable(r, i);
		}
		
		// Insert for comparison with single CPU performance
		//loop_array[0]=cnt;
		
		// Start of measurement
		start = LowLevel.timeMillis();
		
		// Start of all other CPUs
		sys.signal = 1;
		
		// Start of CPU0
		bm_array[0].test(loop_array[0]);
		
		while(true){
			synchronized(lock)
			{
				if (signal == sys.nrCpu-1)
					break;
			}
		}
		
		// End of measurement
		stop = LowLevel.timeMillis();
		
		System.out.println("StartTime: " + start);
		System.out.println("StopTime: " + stop);
		time = stop-start;
		System.out.println("TimeSpent: " + time);
		
		for (int i=0; i<sys.nrCpu; ++i) {
			System.out.println("CPU "+i+" calculated "+loop_array[i]+" loops");
		}
		
	}	
	
	public void run() {
		bm.test(loop_cnt);
		
		synchronized(lock){
			signal++;}
		while(true);
	}
}
