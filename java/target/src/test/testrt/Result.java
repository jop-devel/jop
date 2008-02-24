/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)

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

package testrt;
import joprt.RtThread;
import util.Dbg;

public class Result {

	static int last_per;
	static int max_jitter;

	public static void printPeriod(int ts_old, int ts) {

		int per = ts-ts_old;
		if (last_per==0) last_per = per;	// first time
		System.out.print(per);
		System.out.print(" ");
		int tim1 = last_per-per;
		last_per = per;
		if (tim1<0) tim1 = -tim1;
		if (tim1>max_jitter) max_jitter = tim1;
		System.out.print(max_jitter);
		System.out.print(" ");

		/*- not available at the moment
		 * 
		 * use System.out instead of Dbg! We've removed
		 * Dbg. init.
		 *  
		int tim1 = RtThread.ts1-RtThread.ts0;
		int tim2 = RtThread.ts2-RtThread.ts1;
		int tim3 = RtThread.ts3-RtThread.ts2;
		int tim4 = RtThread.ts4-RtThread.ts3;
		int tim5 = ts-RtThread.ts4;
		int tim6 = ts-RtThread.ts0;
		Dbg.intVal(tim1);
		Dbg.intVal(tim2);
		Dbg.intVal(tim3);
		Dbg.intVal(tim4);
		Dbg.intVal(tim5);
		Dbg.intVal(tim6);
		
		Dbg.wr('\n');
		*/
	}

}
