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
