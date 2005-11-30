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
