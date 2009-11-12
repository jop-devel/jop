/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2009, Martin Schoeberl (martin@jopdesign.com)

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


/**
 * 
 */
package ptolemy.deadline;

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;

/**
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */
public class DeadLine {

	static SysDevice sys = IOFactory.getFactory().getSysDevice();
	static final int PERIOD = 60000000;
	/**
	 * @param args
	 */
	public static void main(String[] args) {


		// deadline();
		busyWait();
		
	}

	static void deadline() {

		int time = sys.cntInt;

		for (;;) {
			time += PERIOD;
			sys.deadLine = time;
			System.out.print("*");
		}
	}
	
	static void busyWait() {

		int time = sys.cntInt;

		for (;;) {
			time += PERIOD;
			while (time - sys.cntInt >= 0) {
				;
			}
			System.out.print("*");
		}		
	}
	
	static class DeadlineMiss extends RuntimeException {}
	
	static void excpetionExample() {
	
		int time;
		
		time = sys.cntInt+1000;
		try {
			// sys.setTimeout = time;
			// do the work
			sys.deadLine = time;
		} catch (DeadlineMiss dm) {
			// handle deadline miss
		}
		
	}

}
