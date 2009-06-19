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
import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;
import com.jopdesign.sys.Startup;

import jbe.LowLevel;
import jbe.BenchMark;
import jbe.BenchLift;

public class LiftBenchCMP_RamCounter implements Runnable {
 
	public static int cnt = 16384;
	public static int signal = 0;
	static Object lock;
	public static BenchMark [] bm_array;
	public static int [] loop_array;
	
	int cpu_id;
	int loop_cnt;
	BenchMark bm;
	
	public LiftBenchCMP_RamCounter (int identity, int loop_count, BenchMark benchmark){
		cpu_id = identity;
		loop_cnt = loop_count;
		bm = benchmark;
	}

	public static void main(String[] args) {

		// Initialization for Ram access counter 
		int count0 = 0;
		int count1 = 0;
		int us0 = 0;
		int us1 = 0;
		int count_result = 0;
		int us_result = 0;
		
		System.out.println("Bandwidth:");
		
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
			Runnable r = new LiftBenchCMP_RamCounter(i+1, loop_array[i+1], bm_array[i+1]);
			Startup.setRunnable(r, i);
		}
		
		// Startpoint of measuring
		count0 = sys.deadLine;
		//us0 = sys.uscntTimer; 
		us0 = Native.rdMem(Const.IO_CNT); // Clockcycles
		
		// Start of CPU0
		bm_array[0].test(loop_array[0]);

		// Start of all other CPUs
		sys.signal = 1;
		
		while(true){
			synchronized(lock)
			{
				if (signal == sys.nrCpu-1)
					break;
			}
		}
		
		// End of measurement
		us1 = Native.rdMem(Const.IO_CNT); // Clockcycles
		count1 = sys.deadLine;
		
		count_result = count1 - count0;
		us_result = us1 - us0;
			
		LowLevel.msg("RAM Accesses:", count_result);
		LowLevel.lf();
		LowLevel.msg("Time us:", us_result);
		LowLevel.lf();
		LowLevel.msg("in %:", count_result/(us_result/100));
		LowLevel.lf();
		
	}	
	
	public void run() {
		bm.test(loop_cnt);
		
		synchronized(lock){
			signal++;}
		while(true);
	}
}
