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

package test;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

import jbe.BenchSieve;
import jbe.Execute;
import jbe.LowLevel;
import jbe.BenchMark;
import jbe.BenchLift;
import jbe.BenchKfl;
import jbe.BenchUdpIp;

// Counts the memory accesses of a benchmark to the external RAM 
// This program can be used when some comments of the dspio
// project are removed. (jopcyc12.vhd, sc_sys.vhd, scio_dspio.vhd)
// Don't forget to remove the comment in Const.java

public class RamCounter {

	public static int benchmark0 = 0;
	public static BenchMark bm0;

	public static void main(String[] args) {

		int count0 = 0;
		int count1 = 0;
		int us0 = 0;
		int us1 = 0;
		int count_result = 0;
		int us_result = 0;
		
		bm0 = new BenchSieve();
		LowLevel.msg("Application benchmark:");
		LowLevel.lf();
		
		// Startpoint of measuring
		count0 = Native.rdMem(Const.IO_RAMCNT);
		us0 = Native.rdMem(Const.IO_CNT); // Clockcycles
		
		benchmark0 = Execute.performResult(bm0);		
		
		// Endpoint of measuring
		us1 = Native.rdMem(Const.IO_CNT); // Clockcycles
		count1 = Native.rdMem(Const.IO_RAMCNT);
		
		count_result = count1 - count0;
		us_result = us1 - us0;
			
		LowLevel.msg(bm0.toString());
		LowLevel.msg("on JOP0:", benchmark0);
		LowLevel.lf();
		LowLevel.msg("RAM Accesses:", count_result);
		LowLevel.lf();
		LowLevel.msg("Time us:", us_result);
		LowLevel.lf();
		LowLevel.msg("in %:", count_result/(us_result/100));
		LowLevel.lf();
	}		
}
