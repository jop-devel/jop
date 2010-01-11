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

package jbe;

// JOP system functions for higher accurate timers
// import com.jopdesign.sys.Const;
// import com.jopdesign.sys.Native;

/**
 * LowLevel time and output for the benchmark.
 * 
 * Special versions for aJile, lejos,... are in comments.
 */
public class LowLevel {

	/**
	 * Clock frequency (in MHz) for the target to calculate clock cycles
	 * of the micro benchmarks. Set to 0 if not known.
	 * Clock cycle calculation works only up to 2 GHz (integer overflow).
	 * For > 2 GHz leave it 0 and do the clock cycle calculation offline.
	 */
	public static final int FREQ = 0;

	static boolean init;

	// Performance counter for the PC JVM
	// static Perf p = Perf.getPerf();
	// static long freq = p.highResFrequency();

	/**
	 * Get the current time in milli seconds (can be relative) as an integer.
	 * 
	 */
	public static int timeMillis() {

		// JOP version with the us timer
		// return Native.rd(Const.IO_US_CNT)/1000;
		
		return (int) System.currentTimeMillis();
	}

	/**
	 * Print a String message and append a ' '
	 * @param msg output message
	 */
	public static void msg(String msg) {

		// lejos version
		// TextLCD.print(msg);
		// try { Thread.sleep(1500); } catch (Exception e) {}
		
		System.out.print(msg);
		System.out.print(" ");
	}

	/**
	 * Print an integer.
	 * @param val output value
	 */
	public static void msg(int val) {

		// lejos version
		// LCD.showNumber(val/1000);
		// try { Thread.sleep(1500); } catch (Exception e) {}
		// LCD.showNumber(val%1000);
		// try { Thread.sleep(1500); } catch (Exception e) {}

		System.out.print(val);
		System.out.print(" ");
	}

	/**
	 * Print a message and an integer
	 * @param msg String message
	 * @param val integer value
	 */
	public static void msg(String msg, int val) {

		msg(msg);
		msg(val);
	}

	/**
	 * Start a new line.
	 *
	 */
	public static void lf() {

		// no lf on lejos version
		//
		
		System.out.println();
	}
}
