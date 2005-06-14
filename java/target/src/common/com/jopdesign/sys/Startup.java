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
	
	// use static vars, don't waste stack
	static int var;

	// a stack for the interpreter mode
	static int[] stack;
	final static int MAX_STACK = 100;
	static int sp, pc, cp;
	
	/**
	 * called from jvm.asm as first method.
	 * Do all initialization here and call main method.
	 */
	static void boot() {
		
// Native.rdMem(0x1234);
// Flash read
// Native.rdMem(0x8000a);
// NAND read
// Native.rdMem(0x10000b);
// Native.wrMem(0xab, 0x1234);
// RAM mirror
// Native.rdMem(0x80000000);
// Native.wrMem(0xcd,0x80000000);
// NAND Flash mirror
// Native.rdMem(-1);
// Native.wrMem(0xef, -1);




		// place for some initialization:
		// could be placed in <clinit> in the future
		System.init();
		msg();
		clazzinit();
		
		// call main()
		var = Native.rdMem(0);		// pointer to 'special' pointers
		var = Native.rdMem(var+3);	// pointer to mein method struct
		Native.invoke(0, var);		// call main (with null pointer on TOS
		JVMHelp.wr("\r\nJVM exit!\r\n");
		for (;;) ;
	}

	static void msg() {

		int version = Native.rdIntMem(64);
		JVMHelp.wr("JOP start V ");
		// take care with future GC - JVMHelp.intVal allocates
		// a buffer!
		if (version==0x12345678) {
			JVMHelp.wr("pre2005");
		} else {
			JVMHelp.intVal(version);
		}
		JVMHelp.wr("\r\n");
	}

	static void clazzinit() {

		stack = new int[MAX_STACK];

		int table = Native.rdMem(0)+4;		// start of clinit table
		int cnt = Native.rdMem(table);		// number of methods
		++table;
		for (int i=0; i<cnt; ++i) {
			int addr = Native.rdMem(table+i);
			System.out.print("<clinit> ");
			System.out.println(addr);
			// reuse static var for 
			var = Native.rdMem(addr);
			int len = var & 0x03ff;
			var >>>= 10;
			cp = Native.rdMem(addr+1);
			// int locals = (cp>>>5) & 0x01f;
			// int args = cp & 0x01f;
			cp >>>= 10;
			System.out.print("start=");
			System.out.print(var);
			System.out.print(" len=");
			System.out.println(len);
			interpret();
		}
	}


	// start address is in var
	static void interpret() {

		pc = 0;
		sp = 0;
		int instr, val, ref;

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
				case 16 :		// bipush
					stack[++sp] = readBC8s();
					break;
				case 17 :		// sipush
					stack[++sp] = readBC16s();
					break;
				case 18 :		// ldc
					stack[++sp] = Native.rdMem(cp+readBC8u());
					break;
				case 79 :		// iastore
					val = stack[sp--];	// value
					ref = stack[sp--];	// index
					ref += stack[sp--];	// ref
					Native.wrMem(val, ref);
					break;
				case 89 :		// dup
					val = stack[sp];
					stack[++sp] = val;
					break;
				case 177 :		// return
					return;
				case 179 :		// putstatic
					putstatic();
					break;
				case 188 :		// newarray
					newarray();
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

		// TODO: handle version!
		int idx = readBC16u();
		int off = Native.rdMem(cp+idx);
		int val = stack[sp--];
		Native.wrMem(val, stack[sp--]+off);
	}

	static void getfield() {

		// TODO: handle version!
		int idx = readBC16u();
		int off = Native.rdMem(cp+idx);
		stack[sp] = Native.rdMem(stack[sp]+off);
	}

	static void newarray() {

		// TODO: handle version!
		readBC8u();			// ignore typ
		int val = stack[sp--];	// count from stack
		int heap = Native.rdIntMem(2);	// get heap pointer
		Native.wrMem(val, heap);
		++heap;
		stack[++sp] = heap;				// ref to first element
		heap += val;
		Native.wrIntMem(heap, 2);		// write heap pointer
	}
}
