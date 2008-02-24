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
*	RST.java: 
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

public class RST extends RtThread {

/**
*	period for thread in us.
*/
	// TODO find a schedule whith correct priorities
	private static final int PRIORITY = 20;
	private static final int PERIOD = 5000;

	private static int[] sinTab;
/**
*	The one and only reference to this object.
*/
	private static RST single;

/**
*	private because it's a singleton Thread.
*/
	private RST(int us) {
		super(PRIORITY, us);
	}


	public static void init() {

		if (single != null) return;			// allready called init()

		// for 7 Bit DAC
		sinTab = new int[] { 64, 95, 119, 127, 119, 96, 64, 32, 8, 0, 8, 31 };
		//
		//	start my own thread
		//
		single = new RST(PERIOD);

	}


/**
*	Genreate RST and output.
*/
	public void run() {

		int r, s, t;

		r = 0;
		s = r + 4;
		t = s + 4;

		for (;;) {
			++r;
			++s;
			++t;
			if (r==12) r = 0;
			if (s==12) s = 0;
			if (t==12) t = 0;
			

			Pwm.vals[0] = sinTab[r];
			Pwm.vals[1] = sinTab[s];
			Pwm.vals[2] = sinTab[t];

			Native.wr(Pwm.vals[0], Const.IO_PWM);
			Native.wr(Pwm.vals[1], Const.IO_PWM+1);
			Native.wr(Pwm.vals[2], Const.IO_PWM+2);



			waitForNextPeriod();

		}
	}

}
