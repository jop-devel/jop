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
*	system functions and constants.
*	Version for BB KFL (clock frequ)!
*/

public class JopSys {
//
// Reihenfolge so lassen!!!
// ergibt static funktionsnummern:
//		1 rd
//		2 wr
//		...
//

	public static native int rd(int adr);
	public static native void wr(int val, int adr);
	static native int rdMem(int adr);
	static native void wrMem(int val, int adr);
	static native int rdIntMem(int adr);
	static native void wrIntMem(int val, int adr);
	static native int getSP();
	static native void setSP(int val);
	static native int getVP();
	static native void setVP(int val);

	public static final int IO_CNT = 10;
	public static final int IO_DISP = 12;

	public static final int CLOCK_FREQ = 7372800;
	public static final int ONE_MINUTE = 7372800*60;
	public static final int ONE_SECOND = 7470257;	// clock ist to fast
	public static final int INTERVAL = 73728/2;	// five ms
	public static final int MS = 7373;			// one ms
	public static final int MS10 = 73728;		// ten ms
	public static final int MS20 = 73728*2;
	public static final int USEC = 8;			// one us
}
