package util;

import com.jopdesign.sys.Native;
/**
*	Flash programmer (for AMD Am29LV040).
*		read and write data to address 0x80000.
*
*	timing:
*		byte program: typ 9 us max. 300 us
*		sector erase: typ 0.7 s + 0.6 s max 15 s + 20 s
*
*	asumes Timer.java is initialized
*/


public class Amd {

	public static int read(int addr) {

		return Native.rdMem(addr+0x80000);
	}

	/** program and handle timeout (without wd handling!) */

	public static void program(int addr, int data) {

		int i;

		addr += 0x80000;

		Native.wrMem(0xaa, 0x80555);
		Native.wrMem(0x55, 0x802aa);
		Native.wrMem(0xa0, 0x80555);
		Native.wrMem(data, addr);

		int j = Native.rd(Native.IO_US_CNT);
		j += 350;						// maximum 350 us timeout
		data &= 0xff;

		for (;;) {
			if (Native.rdMem(addr) == data) break;
			if (j-Native.rd(Native.IO_US_CNT) < 0) break;
		}
	}

	/**
	*	erase one sector and handle wd.
	*/
	public static boolean erase(int addr) {

		int i;

		addr += 0x80000;

		Native.wrMem(0xaa, 0x80555);
		Native.wrMem(0x55, 0x802aa);
		Native.wrMem(0x80, 0x80555);
		Native.wrMem(0xaa, 0x80555);
		Native.wrMem(0x55, 0x802aa);
		Native.wrMem(0x30, addr);

		for (i=0; i<400; ++i) {					// maximum 40 s timeout
			joprt.RtThread.sleepMs(100);
Dbg.wr('.');
			Timer.wd();
			if (Native.rdMem(addr) == 0xff) return true;
		}
		return false;
	}
}
