
package com.jopdesign.sys;

/**
*	native functions in JOP JVM.
*/

public class Native {
//
// Reihenfolge so lassen!!!
// ergibt static funktionsnummern:
//		1 rd
//		2 wr
//		...
//

	public static native int rd(int adr);
	public static native void wr(int val, int adr);
	public static native int rdMem(int adr);
	public static native void wrMem(int val, int adr);
	public static native int rdIntMem(int adr);
	public static native void wrIntMem(int val, int adr);
	public static native int getSP();
	public static native void setSP(int val);
	public static native int getVP();
	public static native void setVP(int val);
	public static native void int2extMem(int intAdr, int[] extAdr, int cnt);
	public static native void ext2intMem(int[] extAdr, int intAdr, int cnt);
	public static native long makeLong(int hWord, int lWord);

	public static final int IO_CNT = 0;
	public static final int IO_INT_ENA = 0;
	public static final int IO_US_CNT = 1;
	public static final int IO_TIMER = 1;
	public static final int IO_SWINT = 2;
	public static final int IO_WD = 3;

	public static final int IO_STATUS = 4;
	public static final int IO_UART = 5;

	public static final int MSK_UA_TDRE = 1;
	public static final int MSK_UA_RDRF = 2;

// BG263
	public static final int IO_STATUS2 = 6;
	public static final int IO_UART2 = 7;
	public static final int IO_STATUS3 = 8;
	public static final int IO_UART3 = 9;

	public static final int IO_DISP = 10;
	public static final int IO_BG = 12;

// TAL
	public static final int IO_IN = 10;
	public static final int IO_LED = 10;
	public static final int IO_OUT = 11;

	public static final int IO_ADC1 = 12;
	public static final int IO_ADC2 = 13;

	public static final int IO_CTRL = 14;
	public static final int IO_DATA = 15;

// OSSI
	public static final int IO_PWM = 6;


}
