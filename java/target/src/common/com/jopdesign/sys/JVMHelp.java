package com.jopdesign.sys;

import joprt.RtThread;

class JVMHelp {

	//
	// DON'T change order of first functions!!!
	//	interrupt gets called from jvm.asm
	//
	public static void interrupt() {
// RtThread.ts0 = Native.rd(Const.IO_US_CNT);
		RtThread.schedule();
// Scheduler.schedInt();
	}


	public static void nullPoint() {

Object o = new Object();
synchronized (o) {
		int i;
		wr('n');
		wr('p');
		wr(' ');

		int sp = Native.getSP();			// sp of ();
		int pc = Native.rdIntMem(sp-3);		// pc is not exact (depends on instruction)
		i = Native.rdIntMem(sp);			// mp
wrSmall(i);
wr(' ');
		int start = Native.rdMem(i)>>>10;	// address of method
wrSmall(start);
wr(' ');
wrByte(pc);

		trace();

		for (;;);
}
	}

	public static void arrayBound() {

Object o = new Object();
synchronized (o) {
		int i;
		wr('a');
		wr('b');
		wr(' ');

		int sp = Native.getSP();			// sp of ();
		int pc = Native.rdIntMem(sp-3);		// pc is not exact (depends on instruction)
		i = Native.rdIntMem(sp);			// mp
wrSmall(i);
wr(' ');
		int start = Native.rdMem(i)>>>10;	// address of method
wrSmall(start);
wr(' ');
wrByte(pc);

		trace();

		for (;;);
}
	}

	static void noim() {

Object o = new Object();
synchronized (o) {
		int i;
		wr('n');
		wr('i');
		wr(' ');
		i = Native.getSP();					// sp of noim();
		int sp = Native.rdIntMem(i-4);		// sp of calling function
		int pc = Native.rdIntMem(sp-3)-1;	// one to high
		i = Native.rdIntMem(sp);			// mp
wrSmall(i);
wr(' ');
		int start = Native.rdMem(i)>>>10;
wrSmall(start);
wr(' ');
wrByte(pc);
wr(' ');

		int val = Native.rdMem(start+(pc>>2));
		for (i=(pc&0x03); i<3; ++i) val >>= 8;
		wrByte(val & 0xff);

		trace();

		for (;;);
}
	}


	static void trace() {

		int fp, mp, vp, pc, addr, loc, args;
		int val;

		int sp = Native.getSP();			// sp after call of trace();
		fp = sp-4;		// first frame point is easy, since last sp points to the end of the frame

		wr('\n');

		while (fp>128+5) {	// stop befor 'fist' method
			mp = Native.rdIntMem(fp+4);
			vp = Native.rdIntMem(fp+2);
			pc = Native.rdIntMem(fp+1);
			val = Native.rdMem(mp);
			addr = val>>>10;			// address of callee

			wrSmall(mp);
			wrSmall(addr);
			wrSmall(pc);
			wr('\n');

			val = Native.rdMem(mp+1);	// cp, locals, args
			args = val & 0x1f;
			loc = (val>>>5) & 0x1f;
			fp = vp+args+loc;			// new fp can be calc. with vp and count of local vars
		}
		wr('\n');
for (fp=128; fp<=sp; ++fp) {
	wrSmall(Native.rdIntMem(fp));
}
		wr('\n');
/*
for (fp=10530; fp<=10700; ++fp) {
	wrSmall(Native.rdMem(fp));
}
*/
	}


	static void wrByte(int i) {

		wr('0'+i/100);
		wr('0'+i/10%10);
		wr('0'+i%10);
		wr(' ');
	}

	static void wrSmall(int i) {

		wr('0'+i/10000%10);
		wr('0'+i/1000%10);
		wr('0'+i/100%10);
		wr('0'+i/10%10);
		wr('0'+i%10);
		wr(' ');
	}


	static void wr(int c) {
		while ((Native.rd(Const.IO_STATUS)&1)==0) ;
		Native.wr(c, Const.IO_UART);
	}
	
	static void wr(String s) {

		int i = s.length();
		for (int j=0; j<i; ++j) {
			wr(s.charAt(j));
		}
	}

	private static final int MAX_TMP = 32;
	private static int[] tmp;			// a generic buffer

	static void intVal(int val) {

		if (tmp==null) tmp = new int[MAX_TMP];
		int i;
		if (val<0) {
			wr('-');
			val = -val;
		}
		for (i=0; i<MAX_TMP-1; ++i) {
			tmp[i] = (val%10)+'0';
			val /= 10;
			if (val==0) break;
		}
		for (val=i; val>=0; --val) {
			wr(tmp[val]);
		}
		wr(' ');
	}

}
