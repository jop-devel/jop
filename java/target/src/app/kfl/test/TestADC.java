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
*	test program for sigma delta ADC.
*/

public class TestADC {

	public static void main(String[] args) {

		Timer.init();			// wd

		Display.init();
// Display.line1();

		forever();
	}


/**
*	main loop.
*/
	private static void forever() {

		int val, old_val;
		int i;

		old_val = 0;

		for (;;) {

			val = JopSys.rd(BBSys.IO_ADC);

			if (val!=old_val) {
				old_val = val;

				Display.cls();
				Display.intVal(val);
				Display.line2();

//
//	46.000 = 3.26V
//	41880 = 3V
//	300 = 0V
//
				i = val-300;
				i /= 139;					// 10mV (41.880-300)/300
//
//	without comerator:
//	11820 = 0V
//	39870 = 2V
//
				i = val-11820;
				i /= 140;

				if (i<0) i = 0;
				Display.intVal(i/100);
				i %= 100;
				Display.data('.');
				Display.data('0'+i/10);
				Display.data('0'+i%10);
			}

			Timer.wd();
		}
	}
}
