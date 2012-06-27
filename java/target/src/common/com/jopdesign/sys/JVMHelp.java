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

import com.jopdesign.io.IOFactory;
import com.jopdesign.io.SysDevice;


//
//	I don't like to make JVMHelp public, but I need it in java.io.PrintStream
//
// class JVMHelp {
public class JVMHelp {

	// don't use static initializers <clinit> in this class as some
	// methods depend on the order.
	static Runnable ih[][];
	static Runnable dh;
	
	// Cannot be used here as <clinit> of the factory depends on this
	// class for the helper method. Would result in a cyclic dependency
	// of <clinit>.
//	static SysDevice sys = IOFactory.getFactory().getSysDevice();
	static SysDevice sys;
	
	static StackOverflowError SOError;
	static NullPointerException NPExc;
	static ArrayIndexOutOfBoundsException ABExc;
	static ArithmeticException ArithExc;
	static ClassCastException CCExc;
	
	static RetryException RetryExc;

	//
	// DON'T change order of first functions!!!
	//	interrupt gets called from jvm.asm
	//

	/**
	 * Dispatch an interrupt to the handler according to the core
	 * and the interrupt number. Interrupt 0 is the scheduler.
	 */
	static void interrupt() {
		
		// the clean way
		// ih[sys.cpuId][sys.intNr].run();
		// a little bit faster
		ih[Native.rd(Const.IO_CPU_ID)][Native.rd(Const.IO_INTNR)].run();
		// Enable interrupts again - we could have invoked a dummy handler
		Native.wr(1, Const.IO_INT_ENA);
//		wr('!');
//		wr('0'+nr);
	}


	public static void nullPoint() {

		throw NPExc;
	}

	// this is used for arrays of longs
	public static void arrayBound() {

		throw ABExc;
	}
	
	static int saved_sp;
	/**
	 * Invoked on a hardware generated exception.
	 */
	static void except() {
		saved_sp = Native.getSP();
		if (Native.rdMem(Const.IO_EXCPT)==Const.EXC_SPOV) {
			// reset stack pointer
			Native.setSP(Const.STACK_OFF);
		}
		// we have more stack available now for the stack overflow
		handleException();
	}
	
	static void noim() {

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
		val &= 0xff;
		wrByte(val);

Object o = new Object();
synchronized (o) {

		System.out.println();
		System.out.print("JOP: bytecode ");
		System.out.print(val);
		System.out.println(" not implemented");

		trace(sp);

		for (;;);
}
	}

	static void handleException() {
		
		if (Const.USE_RTTM) {
			// abort transaction to avoid invoking f_athrow() twice
			Native.wrMem(Const.TM_ABORTED, Const.MEM_TM_MAGIC);
		}
		
		int i;
		i = Native.rdMem(Const.IO_EXCPT);
		if (i==Const.EXC_SPOV) {
			throw SOError;
		} else if (i==Const.EXC_NP) {
			throw NPExc;
		} else if (i==Const.EXC_AB) {
			throw ABExc;
		} else if (i==Const.EXC_DIVZ) {
			throw ArithExc;
		} else if (i==Const.EXC_ROLLBACK) {
			throw RetryExc;
		}

		for (;;);
	}

	/**
	 * Create interrupt handler and preallocated exceptions 
	 * in a static method instead of <clinit>.
	 * Jikes puts <clinit> as first methods into the table (instead of
	 * interrupt()), javac as last method. 
	 * 
	 * We could use HWO here.
	 */
	static void init() {
		ih = new Runnable[Native.rdMem(Const.IO_CPUCNT)][Const.NUM_INTERRUPTS];
		dh = new DummyHandler();
		for (int core=0; core<Native.rdMem(Const.IO_CPUCNT); ++core) {
			for (int var=0; var<Const.NUM_INTERRUPTS; ++var) {
				JVMHelp.addInterruptHandler(core, var, dh);
			}								
		}	
		
		sys = IOFactory.getFactory().getSysDevice();

		SOError = new StackOverflowError();
		NPExc = new NullPointerException();
		ABExc = new ArrayIndexOutOfBoundsException();
		ArithExc = new ArithmeticException();
		CCExc = new ClassCastException();
		
		RetryExc = RetryException.instance;

	}


	static void trace(int sp) {

		int fp, mp, vp, pc, addr, loc, args;
		int val;

//		for (int i=0; i<1024; ++i) {
//			wrSmall(i);
//			wrSmall(Native.rdIntMem(i));
//			wr('\n');
//		}
		wr("saved sp=");
		wrSmall(sp);
		wr('\n');

		fp = sp-4;		// first frame point is easy, since last sp points to the end of the frame

		wr("  mp     pc     fp");
		wr('\n');
		

		while (fp>Const.STACK_OFF+5) {
			mp = Native.rdIntMem(fp+4);
			vp = Native.rdIntMem(fp+2);
			pc = Native.rdIntMem(fp+1);
			val = Native.rdMem(mp);
			addr = val>>>10;			// address of callee

			wrSmall(mp);
//			wrSmall(addr);
			wrSmall(pc);
			wrSmall(fp);
			wr('\n');

			val = Native.rdMem(mp+1);	// cp, locals, args
			args = val & 0x1f;
			loc = (val>>>5) & 0x1f;
			fp = vp+args+loc;			// new fp can be calc. with vp and count of local vars
		}
		wr('\n');
	}

	/**
	 * Install a handle in two static fields for a hardware object
	 * @param o a 'real' instance of the HW object for the class reference
	 * @param address IO address of the hardware device
	 * @param idx index of the static fields
	 * @param cp address of constant pool of the factory class
	 * @return reference to the HW object
	 */
	public static Object makeHWObject(Object o, int address, int idx, int cp) {
		int ref = Native.toInt(o);
		int pcl = Native.rdMem(ref+1);
		int p = Native.rdMem(cp-1);
		p = Native.rdMem(p+1);
		p += idx*2;
		Native.wrMem(address, p);
		Native.wrMem(pcl, p+1);
		return Native.toObject(p);
	}

	public static int[] makeHWArray(int len, int address, int idx, int cp) {
		int p = Native.rdMem(cp-1);
		p = Native.rdMem(p+1);
		p += idx*2;
		Native.wrMem(address, p);
		Native.wrMem(len, p+1);
		return Native.toIntArray(p);
	}
	
	/**
	 * Add a Runnable as a first level interrupt handler.
	 * Use the current core.
	 * @param nr interrupt number
	 * @param r Runnable the represents the interrupt handler
	 */
	public static void addInterruptHandler(int nr, Runnable r) {
		addInterruptHandler(Native.rdMem(Const.IO_CPU_ID), nr, r);
	}
	/**
	 * Add a Runnable as first level interrupt handler for an individual core.
	 * @param core
	 * @param nr
	 * @param r
	 */
	public static void addInterruptHandler(int core, int nr, Runnable r) {
		if (nr>=0 && nr<ih[core].length) {
			ih[core][nr] = r;
		}
	}
	/**
	 * Remove the interrupt handler
	 * @param nr interrupt number
	 */
	public static void removeInterruptHandler(int nr) {
		removeInterruptHandler(Native.rdMem(Const.IO_CPU_ID), nr);

	}
	
	/**
	 * Remove the interrupt handler
	 * @param nr interrupt number
	 */
	public static void removeInterruptHandler(int core, int nr) {
		if (nr>=0 && nr<ih[core].length) {
			ih[core][nr] = dh;
		}
	}

	static void wrByte(int i) {

		wr('0'+i/100);
		wr('0'+i/10%10);
		wr('0'+i%10);
		wr(' ');
	}

	static void wrSmall(int i) {

		wr('0'+i/100000%10);
		wr('0'+i/10000%10);
		wr('0'+i/1000%10);
		wr('0'+i/100%10);
		wr('0'+i/10%10);
		wr('0'+i%10);
		wr(' ');
	}


	public static void wr(int c) {
		
		// busy wait on free tx buffer
		// but ncts is not used anymore =>
		// no wait on an open serial line, just wait
		// on the baud rate
		while ((Native.rd(Const.IO_STATUS)&1)==0) {
			;
		}
		Native.wr(c, Const.IO_UART);
		// this is the USB port
		/* we will NOT wait for the USB device to be compatible
		   with other configurations. The UART limits the transfer rate
		   to about 10kB/s.
		   
		// while ((Native.rdMem(Const.IO_USB_STATUS) & Const.MSK_UA_TDRE)==0) {
			;
		}
		*/
		// disable for OEBB project
		Native.wrMem(c, Const.IO_USB_DATA);
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

		// tmp is used before clazzinit runs
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
	
//	public static void scopeCheck(int ref, int val) {		
//
//		/** val has to be in a longer lived memory region than ref. This means that val can be in: 
//		 * 
//		 *   1. Immortal or Mission memory.
//		 *   2. A scope with a level smaller than the level of ref. (i.e. ref is in a deeper nested
//		 *   	scope than val).
//		 * 
//		 */
//
//		int ref_level; 
//		int val_level; 
//	
//		if (Config.ADD_REF_INFO) {
//			ref_level = (ref & 0x3E000000) >>> 25;
//			val_level = (val & 0x3E000000) >>> 25;
//			
//		} else {
//			ref_level = Native.rdMem(ref + GC.OFF_SCOPE_LEVEL);
//			val_level = Native.rdMem(val + GC.OFF_SCOPE_LEVEL);
//		}
//			
//		if (val_level == 0){
//			if (ref_level != 0) { // ref is in scoped memory
//				GC.log("Illegal Assignment Exception: Shorter lived object references longer lived object!");
//			}
//		} else { //val is in scope
//			if (ref_level != 0){ // ref is in scope
//				if (ref_level > val_level) { // ref is deeper nested than val
//					GC.log("Illegal Assignment Exception: Scope level missmatch");
//				}
//			}
//		}
//	}


}

class DummyHandler implements Runnable {

	public void run() {
		// do nothing
	}
	
}