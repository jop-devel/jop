
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

