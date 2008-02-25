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
import lego.lib.FutureUse;

public class KnightRider {
	public static final int LED0 = 1<<1; 	
	public static final int LED1 = 1<<7;
	public static final int LED2 = 1<<5;
	public static final int LED3 = 1<<4;
	static int val;
	static boolean up;
	
	public static void init() {
		val = LED0;
		up = true;
	}
	
	public static void loop() {
		FutureUse.writePins(val);
		//Native.wr(val, IO_LEDS);
		RtThread.sleepMs(100);
		
		if (up){
		switch (val) {
			case LED0: val = LED1; break;
			case LED1: val = LED2; break;
			case LED2: val = LED3; break;
			case LED3: {
					up = false;
					val = LED2;
					 break;
				}
			default: val = LED0; break;
		}
		} else {
			switch (val) {
				case LED0: {
					up = true;
					val = LED1;
					 break;
				}
				case LED1: val = LED0; break;
				case LED2: val = LED1; break;
				default: val = LED0; break;
		    }
		}
	
		
	
	}
		
		
	public static void main(String[] agrgs) {


		System.out.println("Hello LEGO world!");
				
		init();
		
		new RtThread(10, 100*1000) {
			public void run() {
				for (;;) {
					loop();
					waitForNextPeriod();

				}
			}
		};

		RtThread.startMission();

		for (;;) {
			loop();
			
		}

	}

}
