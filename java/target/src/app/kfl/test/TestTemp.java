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

package kfl.test;

import kfl.*;
/**
*	test program for sigma delta ADC with NTC.
*/

public class TestTemp {

	private static int[] tab;		// starts with -55o C for real value

	public static void main(String[] args) {

		Timer.init();			// wd

		Display.init();
// Display.line1();
		Temp.init();

		forever();
	}

/**
*	main loop.
*/
	private static void forever() {

		int val, old_val;

		old_val = 0;

		for (;;) {

			val = JopSys.rd(BBSys.IO_ADC);

			if (val!=old_val) {
				old_val = val;

				Display.cls();
				val = 46000-val;		// thinking in the other direction
				Display.intVal(val);
				int t = Temp.calc(val);
				Display.line2();
				if (t<0) {
					Display.data('-');
					Display.intVal(-t);
				} else {
					Display.data(' ');
					Display.intVal(t);
				}
				Timer.sleepWd(100);
			}

			Timer.wd();
		}
	}
}
