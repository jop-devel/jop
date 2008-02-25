/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2007, Peter Hilber and Alexander Dejaco

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

package lego;

import lego.lib.*;

//import util.Timer;
//import com.jopdesign.sys.Const;
//import com.jopdesign.sys.Native;
/*
 * 	Multiple tones are output, the frequency is increased and decreased chronologically
 *  The rate of change can be set with Buttons 0 and 1
 */

public class sound1 {

	static boolean flag, up;
	static final int MAX = 50;
	static final int MIN = 10;
	static int value, counter, counter1, SPEED;

	public static void init() {
		up = true;
		flag = false;	
		value = 10;
		SPEED = 0x100;
	}


	public static void main(String[] agrgs) {
		System.out.println("Initializing...");



		init();


		while (true) {
			while (DigitalInputs.getDigitalInput(2)) {

				if (Buttons.getButton(0)) {
					while (Buttons.getButton(0) == true) ;
					SPEED = SPEED + 0x50;
				}
				if (Buttons.getButton(1)) {
					while (Buttons.getButton(1) == true) ;
					if (SPEED > 0x50) {
						SPEED = SPEED - 0x50;
					} 
				}

				counter++;

				if ((counter % value) == 0) {
					if (flag) {
						flag = false;
					} else {
						flag = true;
					}
				}

				counter1++;

				if ((counter1 % SPEED) == 0) {
					if (up) {
						value++;
						if (value >= MAX) {
							up = false;
							value--;
						}
					} else
					{
						value--;
						if (value <= MIN) {
							up = true;
							value++;
						}
					}

				}

				if (flag) {
					Speaker.write(true);
				} else
					Speaker.write(false);

			}
		}
	}



}
