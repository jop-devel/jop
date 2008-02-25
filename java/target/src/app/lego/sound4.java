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
 * A simple version of the tetris soundtrack is played.
 * Some notes use only an approximate frequency.
 * Better programming
 */

public class sound4 {
		
	static boolean flag, on, done;
	static int value, counter;
	
	// notes
	static final int E = 15;
	static final int F = 14;
	static final int H = 20;
	static final int G = 12;
	static final int C = 19;
	static final int D = 17;
	static final int A = 22; 
	static final int A1 = 11; 
	
	// flags
	static final int ON = 90;
	static final int OFF = 91;
	static final int DONE = 92;
	
	// pauses
	static final int PH = 400; // half
	static final int PQ = 200; // quarter
	static final int PT = 600; // three quarter
	static final int PB = 10;  // very short pause
	static final int PL = 800; // long pause
	
	static final int TUNE_LENGTH = 39; // (38 notes+pauses) + 1 (done flag)
	
	static final int[] NOTES = {E, H, C, D, C, H, A, C, E, D, C, H, C, D, E, C, A, OFF, ON, ON, D, F, A1, G, F, E, C, E, D, C, H, C, D, E, C, A, OFF, ON, DONE};
	static final int[] PAUSES = {PH ,PQ ,PQ ,PH ,PQ ,PQ ,PL ,PQ ,PH ,PQ ,PQ ,PT ,PQ ,PH ,PH ,PH ,PH ,PB ,PL ,PQ ,PH ,PQ ,PH ,PQ ,PQ ,PT ,PQ ,PH ,PQ ,PQ ,PT ,PQ ,PH ,PH ,PH ,PH ,PB ,PL};
	
	public static void init() {
	  on = true;
	  flag = false;	
	  value = 10;
	  done = false;
	}
	
	public static void tetris () {
		on = true;
		RtThread.sleepMs(4);
		on = false;
		RtThread.sleepMs(796);
		on = true;
		RtThread.sleepMs(4);
		on= false;
		RtThread.sleepMs(796);
		on = true;
		RtThread.sleepMs(4);
		on= false;
		RtThread.sleepMs(796);
		on = true;
		RtThread.sleepMs(4);
		on = false;
		RtThread.sleepMs(796);
		on = true;
		
		for (int i=0;i<TUNE_LENGTH-1;i++)
		{
			if (NOTES[i] == ON)
			{
				on = true;
			} else
			if (NOTES[i] == OFF)
			{
				on = false;
			} else
			if (NOTES[i] == DONE)
			{
				break;
			} else
			{
				value = NOTES[i];
			}			
			RtThread.sleepMs(PAUSES[i]);
		}
		
	}
	
	public static void loop() {
		if (on) {

			counter++;

			if ((counter % value) == 0) {
				if (flag) {
					flag = false;
				} else {
					flag = true;
				}
			}

			if (flag) {
				Speaker.write(true);
			} else
				Speaker.write(false);

		}
	}


	public static void main(String[] agrgs) {
		System.out.println("Initializing...");
	
	

		init();
		
		
		new RtThread(10, 100) {
			public void run() {
				for (;;) {
				loop();
				waitForNextPeriod();
				}
			}
		};
		
		
		
		RtThread.startMission();
		
		while(true) {
			on = true;
			tetris();	
			while (Buttons.getButtons() == 0);
			while (Buttons.getButtons() != 0);
		}
		
		
	}
	
	

}
