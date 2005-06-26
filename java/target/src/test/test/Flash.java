package test;

import util.Dbg;

import com.jopdesign.sys.Native;

/**
*	Test.java ... the name implys it
*/

public class Flash {

	public static void main(String[] args) {

		int i, j, k;

		Dbg.initSerWait();
		Dbg.lf();

		/* Read ID from Flash */
		Native.wrMem(0xaa, 0x80555);
		Native.wrMem(0x55, 0x802aa);
		Native.wrMem(0x90, 0x80555);
		i = Native.rdMem(0x80000);
		Native.wrMem(0xaa, 0x80555);
		Native.wrMem(0x55, 0x802aa);
		Native.wrMem(0x90, 0x80555);
		j = Native.rdMem(0x80001);
		Dbg.wr("Flash ");
		Dbg.hexVal(i);
		Dbg.hexVal(j);
		if (i==0x01 & j==0x4f) {
			Dbg.wr("AMD Am29LV040B\n");
		} else {
			Dbg.wr("error reading Flash\n");
		}

/* read ID  and status from NAND */
//		   Native.wrMem(0xff, 0x100001);
		Native.wrMem(0x90, 0x100001);
		Native.wrMem(0x00, 0x100002);
//
//			should read 0x98 and 0x73
//
		i = Native.rdMem(0x100000);
		j = Native.rdMem(0x100000);
		Dbg.wr("NAND ");
		Dbg.hexVal(i);
		Dbg.hexVal(j);
		if (i==0x198) {
			Dbg.wr("Toshiba ");
			if (j==0x173) {
				Dbg.wr("16 MB\n");
			} else if (j==0x175) {
				Dbg.wr("32 MB\n");
			} else if (j==0x176) {
				Dbg.wr("64 MB\n");
			} else if (j==0x179) {
				Dbg.wr("128 MB\n");
			} else {
				Dbg.wr("error reading NAND\n");
			}
		} else {
			Dbg.wr("error reading NAND\n");
		}

//
//			read status, should be 0xc0
//
		Native.wrMem(0x70, 0x100001);
		i = Native.rdMem(0x100000);
		j = Native.rdMem(0x100000);
		Dbg.hexVal(i);
		Dbg.hexVal(j);
		if (i==0x1c0 && j==0x1c0) {
			Dbg.wr("status OK\n");
		} else {
			Dbg.wr("error reading NAND status\n");
		}

	}

}
