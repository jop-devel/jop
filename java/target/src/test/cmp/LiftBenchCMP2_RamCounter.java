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

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;
import jbe.LowLevel;
import jbe.BenchMark;
import jbe.BenchLift;

public class LiftBenchCMP2_RamCounter {

	public static boolean signal = false; 
	public static int lift0 = 0;
	public static int lift1 = 0;	
	public static BenchMark bm0;
	public static BenchMark bm1;
	public static int cnt = 16384;
	static Object lock;

	public static void main(String[] args) {

		int cpu_id;
		cpu_id = Native.rdMem(Const.IO_CPU_ID);
		
		if (cpu_id == 0x00000000)
		{
			int count0 = 0;
			int count1 = 0;
			int us0 = 0;
			int us1 = 0;
			int count_result = 0;
			int us_result = 0;
			
			bm0 = new BenchLift();
			bm1 = new BenchLift();
			
			System.out.println("Bandwidth:");
			
			// Startpoint of measuring
			count0 = Native.rdMem(Const.IO_RAMCNT);
			us0 = Native.rdMem(Const.IO_CNT); // Clockcycles
			
			Native.wrMem(0x00000001, Const.IO_SIGNAL);
			bm0.test(cnt/2);
			while(true){
				synchronized(lock)
				{
					if (signal == true)
						break;
				}
			}
			// Endpoint of measuring
			us1 = Native.rdMem(Const.IO_CNT); // Clockcycles
			count1 = Native.rdMem(Const.IO_RAMCNT);
			
			count_result = count1 - count0;
			us_result = us1 - us0;
				
			LowLevel.msg("RAM Accesses:", count_result);
			LowLevel.lf();
			LowLevel.msg("Time us:", us_result);
			LowLevel.lf();
			LowLevel.msg("in %:", count_result/(us_result/100));
			LowLevel.lf();
		}
		else
		{		
			if (cpu_id == 0x00000001)
			{	
				bm1.test(cnt/2);
				synchronized(lock){
					signal=true;}
				
				while(true);
			}
		}	
	}		
}
