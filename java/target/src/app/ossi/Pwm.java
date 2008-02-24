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

package ossi;

/**
*	Pwm.java: 
*
*	Author: Martin Schoeberl (martin.schoeberl@chello.at)
*			Ossi
*
*   Changelog:
*
*/

import joprt.RtThread;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

public class Pwm extends RtThread {

/**
*	period for thread in us.
*/
	// TODO find a schedule whith correct priorities
	private static final int PRIORITY = 30;
	private static final int PERIOD = 1000;

	public static int[] vals;
/**
*	The one and only reference to this object.
*/
	private static Pwm single;

/**
*	private because it's a singleton Thread.
*/
	private Pwm(int us) {
		super(PRIORITY, us);
	}


	public static void init() {

		if (single != null) return;			// allready called init()

		vals = new int[3];
		//
		//	start my own thread
		//
		single = new Pwm(PERIOD);
	}


/**
*	Genreate PWM and output.
*/
	public void run() {

		int i, j;

		i = 0;

		for (;;) {
			++i;
			i &= 0x03;

			int led = 0;
			if (i<(vals[0]>>>5)) {
				led += 1;
			}
			if (i<(vals[1]>>>5)) {
				led += 2;
			}
			if (i<(vals[2]>>>5)) {
				led += 4;
			}
			Native.wr(led, Const.IO_OUT);

			waitForNextPeriod();

		}
	}

}
