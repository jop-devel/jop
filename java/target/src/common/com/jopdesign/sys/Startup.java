/*
 * Created on 24.05.2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.jopdesign.sys;

/**
 * @author martin
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Startup {
	
	// use static vars, don't waste stack space
	static int var;
	static int mem_size;

	// a stack for the interpreter mode
	static int[] stack;
	final static int MAX_STACK = 100;
	static int sp, pc, cp;
	
	static boolean started;
	
	/**
	 * called from jvm.asm as first method.
	 * Do all initialization here and call main method.
	 */
	static void boot() {
		
		// only CPU 0 does the initialization stuff
		if (Native.rdMem(Const.IO_CPU_ID) == 0)	{
			started = false;
			msg();
			mem_size = getRamSize();
			// mem(0) is the length of the application
			// or in other words the heap start
			var = Native.rdMem(1);		// pointer to 'special' pointers
			// first initialize the GC with the address of static ref. fields
			GC.init(mem_size, var+4);
			// place for some initialization:
			// could be placed in <clinit> in the future
			System.init();
			version();
			started = true;
			clazzinit();
		}
		
		// call main()
		var = Native.rdMem(1);		// pointer to 'special' pointers
		var = Native.rdMem(var+3);	// pointer to main method struct
		Native.invoke(0, var);		// call main (with null pointer on TOS
		exit();
	}
	

	static void msg() {

		JVMHelp.wr("JOP start");
		
	}
	
	/**
	 * @return RAM size in 32 bit words
	 */
	static int getRamSize() {
		
		int size = 0;
		int firstWord = Native.rd(0);
		int val;
		
		// increment in 1024 Bytes
		for (size=256; ; size+=256) {
			val = Native.rd(size);
			Native.wr(0xaaaa5555, size);
			if (Native.rd(size)!=0xaaaa5555) break;
			Native.wr(0x12345678, size);
			if (Native.rd(size)!=0x12345678) break;
			if (Native.rd(0)!=firstWord) break;
			// restore current word
			Native.wr(val, size);
		}
		// restore the first word
		Native.wr(firstWord, 0);
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
		int version = Native.rdIntMem(64);
		JVMHelp.wr(" V ");
		// take care with future GC - JVMHelp.intVal allocates
		// a buffer!
		if (version==0x12345678) {
			JVMHelp.wr("pre2005");
		} else {
			JVMHelp.intVal(version);
		}
		JVMHelp.wr("- ");
		int speed = getSpeed();
		JVMHelp.intVal(speed);
		JVMHelp.wr("MHz, ");
		JVMHelp.intVal(mem_size/1024*4);
		JVMHelp.wr("KB RAM\r\n");
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
			if (len<256) {	// see JOPizer constant on max. method length
				Native.invoke(addr);
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

		int idx = readBC16u();
		int addr = Native.rdMem(cp+idx);
		Native.wrMem(stack[sp--], addr);
	}

	static void getstatic() {

		int idx = readBC16u();
		int addr = Native.rdMem(cp+idx);
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
