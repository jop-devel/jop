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

/*
 * Created on 24.05.2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.jopdesign.sys;

/**
 * @author Martin martin@jopdesign.com
 *
 * Starup code and very simple JVM interpreter for large <clinit>
 * methods.
 */
public class Startup {
	
	// use static vars, don't waste stack space
	static int var;
	/** Size of main memory in 32-bit words */
	static int mem_size;
	/** Size of scratchpad memory in 32-bit words */
	static int spm_size;

	// a stack for the interpreter mode
	static int[] stack;
	final static int MAX_STACK = 100;
	static int sp, pc, cp;
	
	/**
	 * How needs this field, and why?
	 */
	static boolean started;
	
	/**
	 * The start method for CPU 1 to n-1.
	 * 
	 * Public just for a quick test.
	 */
	static Runnable[] cpuStart = new Runnable[Native.rdMem(Const.IO_CPUCNT)-1];
	
	/**
	 * called from jvm.asm as first method.
	 * Do all initialization here and call main method.
	 */
	static void boot() {
		
		// use local variable - statics are not CMP save!
		int val;
		
		// disable all interrupts locally
		// global enable and disable on monitorenter/exit don't hurt
		Native.wr(0, Const.IO_INTMASK);
		
		// only CPU 0 does the initialization stuff
		if (Native.rdMem(Const.IO_CPU_ID) == 0)	{
			started = false;
			msg();
			spm_size = getRamSize(Const.SCRATCHPAD_ADDRESS);
			mem_size = getRamSize(0);
			// mem(0) is the length of the application
			// or in other words the heap start
			val = Native.rdMem(1);		// pointer to 'special' pointers
			// first initialize the GC with the address of static ref. fields
			GC.init(mem_size, val+4);
			// place for some initialization:
			// could be placed in <clinit> in the future
			System.init();
			version();
			started = true;
			clazzinit();
			// not in <clinit> as first methods are special and palcement
			// of <clinit> depends on compiler
			JVMHelp.init();
		}
		
		// clear all pending interrupts (e.g. timer after reset)
		Native.wr(1, Const.IO_INTCLEARALL);
		// set global enable
		Native.wr(1, Const.IO_INT_ENA);
		
		// request CPU id
		val = Native.rdMem(Const.IO_CPU_ID);
		
		if (val==0) {
			// only CPU 0 invokes main()
			val = Native.rdMem(1);		// pointer to 'special' pointers
			val = Native.rdMem(val+3);	// pointer to main method structure
			Native.invoke(0, val);		// call main (with null pointer on TOS
			exit();			
		} else {
			// other CPUs invoke a Runnable
			if (cpuStart[val-1]!=null) {
				cpuStart[val-1].run();
			}
			for (;;) {
				;			// busy loop for other CPUs exit
			}
		}
	}
	

	static void msg() {

		JVMHelp.wr("JOP start");
		
	}
	
	/**
	 * Add a Runnable for the other CPUs
	 * @param r
	 * @param index
	 */
	public static void setRunnable(Runnable r, int index) {
		cpuStart[index] = r;
	}
	
	/**
	 * @return RAM size in 32 bit words
	 */
	static int getRamSize(int offset) {
		
		int size = 0;
		int firstWord = Native.rd(offset+0);
		int val;
		
		// increment in 512 Bytes
		for (size=0; ; size+=128) {
			val = Native.rd(offset+size);
			Native.wr(0xaaaa5555, offset+size);
			if (Native.rd(offset+size)!=0xaaaa5555) break;
			Native.wr(0x12345678, offset+size);
			if (Native.rd(offset+size)!=0x12345678) break;
			if (size!=0) {
				if (Native.rd(offset+0)!=firstWord) break;				
			}
			// restore current word
			Native.wr(val, offset+size);
		}
		// restore the first word
		Native.wr(firstWord, offset+0);
		return size;
	}
	
	
	/**
	 * @return Processor speed in MHz
	 */
	static int getSpeed() {
		
		int start=0, end=0;
		int val = Native.rd(Const.IO_US_CNT) + 5;
		
		while (Native.rd(Const.IO_US_CNT)-val<0) {
			;
		}
		start = Native.rd(Const.IO_CNT);
		val += 32;	// wait 32 us
		while (Native.rd(Const.IO_US_CNT)-val<0) {
			;
		}
		end = Native.rd(Const.IO_CNT);
		
		// round and divide by 32
		return (end-start+16)>>5;
	}
	
	static void version() {

		// BTW: why not using System.out.println()?
		int version = Native.rdIntMem(64-2);
		if (version==0x12345678) {
			// not in the new location, try the old one
			version = Native.rdIntMem(64);
		}
		JVMHelp.wr(" V ");
		// take care with future GC - JVMHelp.intVal allocates
		// a buffer!
		if (version==0x12345678) {
			JVMHelp.wr("pre2005");
		} else {
			JVMHelp.intVal(version);
		}
		JVMHelp.wr("\r\n");
		int speed = getSpeed();
		JVMHelp.intVal(speed);
		JVMHelp.wr("MHz, ");
		JVMHelp.intVal(mem_size/1024*4);
		JVMHelp.wr("KB RAM");
		if (spm_size!=0) {
			JVMHelp.wr(", ");
			JVMHelp.intVal(spm_size*4);
			JVMHelp.wr("Byte on-chip RAM");
		}
		JVMHelp.wr(", ");
		JVMHelp.intVal(Native.rdMem(Const.IO_CPUCNT));
		JVMHelp.wr("CPUs");
		JVMHelp.wr("\r\n");
	}

	public static void exit() {
		
		for (;RtThreadImpl.mission;) {
			RtThreadImpl.sleepMs(1000);
		}
		JVMHelp.wr("\r\nJVM exit!\r\n");
		synchronized (stack) {
			for (;;) ;
		}
	}
	
	public static int getSPMSize() {
		return spm_size*4;
	}
	static void clazzinit() {

		stack = new int[MAX_STACK];

		int table = Native.rdMem(1)+6;		// start of clinit table
		int cnt = Native.rdMem(table);		// number of methods
		++table;
		for (int i=0; i<cnt; ++i) {
			int addr = Native.rdMem(table+i);
//			System.out.print("<clinit> ");
//			System.out.println(addr);
			// reuse static var for 
			var = Native.rdMem(addr);
			int len = var & 0x03ff;
			var >>>= 10;
			cp = Native.rdMem(addr+1);
			// int locals = (cp>>>5) & 0x01f;
			// int args = cp & 0x01f;
			cp >>>= 10;
//			System.out.print("start=");
//			System.out.println(var);
//			System.out.println("len=");
//			System.out.println(len);
			if (len<256 && len!=0) {	// see JOPizer constant on max. method length
				Native.invoke(addr);
			// len=0 means interpret it
			} else {
				interpret();				
			}
		}
	}


	// start address is in var
	static void interpret() {

		pc = 0;
		sp = 0;
		int instr, val, ref, idx;

		for (;;) {

//			System.out.print(pc);
			instr = readBC8u();
//			System.out.print(" ");
//			System.out.println(instr);

			switch (instr) {
				
				case 0 :		// nop
					break;
				case 1 :		// aconst_null
					stack[++sp] = 0;
					break;
				case 2 :		// iconst_m1
					stack[++sp] = -1;
					break;
				case 3 :		// iconst_0
					stack[++sp] = 0;
					break;
				case 4 :		// iconst_1
					stack[++sp] = 1;
					break;
				case 5 :		// iconst_2
					stack[++sp] = 2;
					break;
				case 6 :		// iconst_3
					stack[++sp] = 3;
					break;
				case 7 :		// iconst_4
					stack[++sp] = 4;
					break;
				case 8 :		// iconst_5
					stack[++sp] = 5;
					break;
				case 11 :		// fconst_0
					stack[++sp] = 0;
					break;
				case 12 :		// fconst_1
					stack[++sp] = 0x3f800000;
					break;
				case 13 :		// fconst_2
					stack[++sp] = 0x40000000;
					break;
				case 16 :		// bipush
					stack[++sp] = readBC8s();
					break;
				case 17 :		// sipush
					stack[++sp] = readBC16s();
					break;
				case 18 :		// ldc
					stack[++sp] = Native.rdMem(cp+readBC8u());
					break;
				case 83 :		// aastore
				case 84 :		// bastore
				case 85 :		// castore
				case 81 :		// fastore
				case 79 :		// iastore
				case 86 :		// sastore
					val = stack[sp--];	// value
					idx = stack[sp--];	// index
					ref = stack[sp--];	// ref
					// handle:
					ref = Native.rdMem(ref);
					Native.wrMem(val, ref+idx);
					break;
				case 89 :		// dup
					val = stack[sp];
					stack[++sp] = val;
					break;
				case 177 :		// return
					return;
				case 178 :		// getstatic
				case 224 :		// resE0 - getstatic_ref
					getstatic();
					break;
				case 221 :		// jopsys_nop
					break;
				case 179 :		// putstatic
				case 225 :		// resE1 - putstatic_ref
					putstatic();
					break;
				case 188 :		// newarray
					newarray();
					break;
				case 189 :		// anewarray
					anewarray();
					break;
				default:
					System.out.print("JVM interpreter: bytecode ");
					System.out.print(instr);
					System.out.println(" not implemented");
					for (;;);

			}
		}

	}

	static int readBC8u() {

		int val = Native.rdMem(var + (pc>>2));
		val >>= (3-(pc&0x03))<<3;
		val &= 0xff;
		++pc;
		return val;
	}

	static byte readBC8s() {

		int val = Native.rdMem(var + (pc>>2));
		val >>= (3-(pc&0x03))<<3;
		++pc;
		return (byte) val;
	}

	static short readBC16s() {

		short val = readBC8s();
		val <<= 8;
		val |= readBC8s() & 0x0ff;
		return val;
	}

	static int readBC16u() {

		int idx = readBC16s();
		return idx & 0xffff;
	}

	static void putstatic() {

		int addr = readBC16u();
		Native.wrMem(stack[sp--], addr);
	}

	static void getstatic() {

		int addr = readBC16u();
		stack[++sp] = Native.rdMem(addr);
	}

	static void putfield() {

		int off = readBC16u();
		int val = stack[sp--];
		int ref = stack[sp--];
		// handle indirection:
		ref = Native.rdMem(ref);

		Native.wrMem(val, ref+off);
	}

	static void getfield() {

		int off = readBC16u();
		int ref = stack[sp];
		// handle indirection:
		ref = Native.rdMem(ref);

		stack[sp] = Native.rdMem(ref+off);


	}

	static void newarray() {

		int type = readBC8u();			// use typ
		int val = stack[sp];			// count from stack
		stack[sp] = JVM.f_newarray(val, type);
	}
	
	static void anewarray() {
		
		int cons = readBC16u();
		int val = stack[sp];			// count from stack
		stack[sp] = JVM.f_anewarray(val, cons);
		
	}
}
