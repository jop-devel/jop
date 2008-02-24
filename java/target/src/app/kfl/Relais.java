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

package kfl;

/**
*	functions to set and reset the two relais.
*/

public class Relais {

	public static void resLu() {
		JopSys.wr(BBSys.BIT_RES_LU, BBSys.IO_RELAIS);
		Timer.sleep(10);
		JopSys.wr(0, BBSys.IO_RELAIS);
	}
	public static void setLu() {
		JopSys.wr(BBSys.BIT_SET_LU, BBSys.IO_RELAIS);
		Timer.sleep(10);
		JopSys.wr(0, BBSys.IO_RELAIS);
	}
	public static void resLo() {		// should be Lo
		JopSys.wr(BBSys.BIT_RES_LO, BBSys.IO_RELAIS);
		Timer.sleep(10);
		JopSys.wr(0, BBSys.IO_RELAIS);
	}
	public static void setLo() {
		JopSys.wr(BBSys.BIT_SET_LO, BBSys.IO_RELAIS);
		Timer.sleep(10);
		JopSys.wr(0, BBSys.IO_RELAIS);
	}
}
