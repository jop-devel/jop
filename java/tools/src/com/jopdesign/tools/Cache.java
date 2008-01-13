
/**
*	Cache.java
*
*	Simulation of Cache for JOP.
*
*/

package com.jopdesign.tools;

import java.util.*;
import java.text.*;

public class Cache {

	static final int MAX_BC = 1024;		// per function
	static final int MAX_BC_MASK = 0x3ff;
	byte[] bc = new byte[MAX_BC];

	int[] mem;
	JopSim sim;

	//
	//	only for statistics
	//

	int memRead = 0;
	int memTrans = 0;
	int cacheRead = 0;

	LinkedList test = new LinkedList();
	Cache use;

	// dummy constructor for child classes
	Cache() {
	}

	Cache(int[] main, JopSim js) {

		mem = main;
		sim = js;

//
//	lookupswitch works only with 0 based pc values!!!
//
//	don't use direct mapped now!!!
//
// we can't use all cache variants in one run as the pc is used differently!!!
/*
		test.add(new PrefetchBuffer(main, js));
		test.add(new SimpleCache(main, js));
		test.add(new TwoBlockCache(main, js));
//		test.add(new LRUBlockCache(main, js, 2));
		test.add(new LRUBlockCache(main, js, 4));
*/
/*
		test.add(new LRUBlockCache(main, js, 8));
		test.add(new LRUBlockCache(main, js, 16));
		test.add(new LRUBlockCache(main, js, 32));
//		test.add(new LRUBlockCache(main, js, 64));
//		test.add(new LRUBlockCache(main, js, 128));
		test.add(new TwoWay(main, js));
*/
/*
		test.add(new DirectMapped(main, js, 1, 8));
		test.add(new DirectMapped(main, js, 1, 16));
		test.add(new DirectMapped(main, js, 1, 32));
		test.add(new DirectMapped(main, js, 2, 8));
*/
//		test.add(new DirectMapped(main, js, 2, 16));
/*
		test.add(new DirectMapped(main, js, 2, 32));
		test.add(new DirectMapped(main, js, 4, 8));
		test.add(new DirectMapped(main, js, 4, 16));
		test.add(new DirectMapped(main, js, 4, 32));
*/
//		test.add(new VarBlockCache(main, js, 1, 8, false));
		test.add(new VarBlockCache(main, js, 4, 16, false));
//		test.add(new VarBlockCache(main, js, 1, 32, false));
//		test.add(new VarBlockCache(main, js, 1, 64, false));
//		test.add(new VarBlockCache(main, js, 2, 8, false));
/*
		test.add(new VarBlockCache(main, js, 2, 16, false));
		test.add(new VarBlockCache(main, js, 2, 32, false));
		test.add(new VarBlockCache(main, js, 2, 16, true));
		test.add(new VarBlockCache(main, js, 2, 32, true));
*/
/*
		test.add(new VarBlockCache(main, js, 2, 64, false));
		test.add(new VarBlockCache(main, js, 4, 8, false));
		test.add(new VarBlockCache(main, js, 4, 16, false));
		test.add(new VarBlockCache(main, js, 4, 32, false));
		test.add(new VarBlockCache(main, js, 4, 64, false));
*/
		use = (Cache) test.get(0);
	}

	int cnt() {
		return test.size();
	}
	void use(int nr) {
		use = (Cache) test.get(nr);
	}



	int ret(int start, int len, int pc) {

		return use.ret(start, len, pc);
	}

	int corrPc(int pc) {

		return use.corrPc(pc);
	}

	int invoke(int start, int len) {

		return use.invoke(start, len);
	}


	byte bc(int addr) {

		return use.bc(addr);
	}


	void stat() {

		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setDecimalSeparator('.');
		DecimalFormat mbf = new DecimalFormat("0.00", dfs);
		DecimalFormat mbt = new DecimalFormat("0.000", dfs);

		float mbib = (float) use.memRead/use.cacheRead;
		float mtib = (float) use.memTrans/use.cacheRead;

		String delim = " & ";

		System.out.print(use);
		System.out.print(delim);
		//System.out.print("Inst.bytes "+use.cacheRead);
		//System.out.print(" mem read "+use.memRead);
		//System.out.print(" mem trans "+use.memTrans);
		//System.out.println();
		//System.out.print("MBIB=");
		System.out.print(mbf.format(mbib));
		System.out.print(delim);
		//System.out.print("MTIB=");
		System.out.print(mbt.format(mtib));
		System.out.print(delim);

/*
		//
		//	simulate 32-bits (DDR) SDRAM
		//
		//	SDRAM:	5 cycle latency: 3 cycle row address and 2 cycle CAS latency 
		//			4 Bytes / cycle (0.25 / Byte)
		//
		//	DDR:	4.5 cycle latency: 2 cycle row address and 2.5 cycle CAS latency
		//			4 Bytes / 0.5 cycle (0.125 / Byte)
		//
		// SRAM 15 ns at 100 MHz: 1 cycle latency, 2 cycles / word

		//System.out.print("SRAM=");
		System.out.print(mbf.format((mbib*0.5 + mtib*1)));
		System.out.print(delim);
		//System.out.print("SDR=")
		System.out.print(mbf.format((mbib*0.25 + mtib*5)));
		System.out.print(delim);
		//System.out.print("DDR=");
		System.out.print(mbf.format((mbib*0.125 + mtib*4.5)));
*/
		System.out.print(" \\\\");
		System.out.println();

	}


	/**
	*	reset performance counter.
	*/
	void resetCnt() {
		use.memRead = 0;
		use.memTrans = 0;
		use.cacheRead = 0;
	}

	void rawData() {
/*
		System.out.print(use.cacheRead+" ;");
		System.out.print(use.memRead+" ;");
		System.out.println(use.memTrans);
		System.out.println(use.memRead);
*/
		System.out.println(use.memRead/4+use.memTrans*5);
	}

	public String toString() {

		String s = getClass().toString();
		s = s.substring(s.lastIndexOf('.')+1);
		return s;
	}

	int instrBytes() {

		return use.cacheRead;
	}

}
