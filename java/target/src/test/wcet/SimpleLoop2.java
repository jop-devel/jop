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

// Used as an example for JTRES 2008 paper!

package wcet;

import com.jopdesign.sys.*;

public class SimpleLoop2 {

	final static boolean MEASURE = false;
	static int ts, te, to;
	
	public static void main(String[] args) {
		int [] a = new int[10];
		int number = 1;
		int val;
		
		ts = Native.rdMem(Const.IO_CNT);
		te = Native.rdMem(Const.IO_CNT);
		to = te-ts;
		
		int cpu_id;
		cpu_id = Native.rdMem(Const.IO_CPU_ID);
		
		if (cpu_id == 0x00000000)
		{
			Native.wrMem(0x00000001, Const.IO_SIGNAL);
			measure(10, a, number);
			val = te-ts-to;
			if (MEASURE) System.out.println(val);			
		}
		else
		{
			for(;;);
		}
	}

	public static void measure(int size, int [] a, int s) {
		
		int i;

		if (MEASURE) ts = Native.rdMem(Const.IO_CNT);
		
		for (i = 0; i < size; i++) { // @WCA loop=10
			a[i] = a[i] + s;
		}
		
		if (MEASURE) te = Native.rdMem(Const.IO_CNT);
	}	
	
}