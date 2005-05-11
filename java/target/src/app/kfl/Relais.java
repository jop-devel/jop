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
