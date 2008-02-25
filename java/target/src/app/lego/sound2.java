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

import joprt.RtThread;
import lego.lib.*;

//import util.Timer;
//import com.jopdesign.sys.Const;
//import com.jopdesign.sys.Native;

/*
 * A single tone is output, the frequency can be chanced with buttons 0 and 1
 */

public class sound2 {
		
	static boolean flag;

	static int value, counter;
	
	public static void init() {
	  flag = false;	
	  value = 1;
	}


	public static void main(String[] agrgs) {
		System.out.println("Initializing...");
	
	

		init();


		new RtThread(10, 10 * 1000) {
			public void run() {
				while (true) {
					while (DigitalInputs.getDigitalInput(2)) {
						
						
						if (Buttons.getButton(0)) {
							while (Buttons.getButton(0) == true) ;
							value++;
						}
						if (Buttons.getButton(1)) {
							while (Buttons.getButton(1) == true) ;
							value--; 
						}

						
						
							if (flag) {
								Speaker.write(true);
								flag = false;
							} else {
								flag = true;
								Speaker.write(false);
							}
						
						RtThread.sleepMs(value);

						
					}
				}
			}
		};


		RtThread.startMission();
		
	}
	
	

}
