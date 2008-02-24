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


package com.jopdesign.sys;

/**
*	Simulate IO functions of JOP on PC!
*/

import simhw.*;

public class Native {
//
// Reihenfolge so lassen!!!
// ergibt static funktionsnummern:
//		1 rd
//		2 wr
//		...
//

	private static BaseSim hw = new TALSim();
 
	public static int rd(int addr) {	
		return hw.rd(addr);
	}

	public static void wr(int val, int addr) {
		hw.wr(val, addr);
	}


	/**
	*	Simulation of JOP memory.
	*/
	public static int rdMem(int addr) {	
		return hw.rdMem(addr);
	}

	public static void wrMem(int val, int addr) {
		hw.wrMem(val, addr);
	}
	public static int rdIntMem(int addr) { return 1; }
	public static void wrIntMem(int val, int addr) { return; }
	public static int getSP() { return 1; }
	public static void setSP(int val) { return; }
	public static int getVP() { return 1; }
	public static void setVP(int val) { return; }

}

