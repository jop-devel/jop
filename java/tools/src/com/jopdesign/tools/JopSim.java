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


/**
*	JopSim.java
*
*	Simulation of JOP JVM.
*
*		difference between JOP and JopSim:
*			loadBc (and invokestatic)
*
*		2001-12-03	I don't need a fp!?
*/

package com.jopdesign.tools;

import com.jopdesign.sys.Const;
import com.jopdesign.timing.jop.WCETInstruction;

import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;

public class JopSim {

	public class JopSimRtsException extends RuntimeException {
		
		private static final long serialVersionUID = 1L;
		private int errCode;

		public JopSimRtsException(String message, int reason) {
			super(message);
			this.errCode = reason;
		}
		public int getReason() {
			return errCode;
		}
	}
	public class JopSimFatalError extends RuntimeException {

		private static final long serialVersionUID = 1L;
		public JopSimFatalError(String message) {
			super(message);
		}
	}
	
	/**
	 * Classify memory access type for cache and TM experiments.
	 */
	enum Access {
		/**
		 * General class info
		 */
		CLINFO,
		/**
		 * Method table
		 */
		MTAB,
		/**
		 * Constant pool
		 */
		CONST,
		/**
		 * Interface table
		 */
		IFTAB,
		/**
		 * Handle indirection
		 */
		HANDLE,
		/**
		 * Array length
		 */
		ALEN,
		/**
		 * Method vector base (= class reference)
		 */
		MVB,
		/**
		 * Field access
		 */
		FIELD,
		/**
		 * Static field access
		 */
		STATIC,
		/**
		 * Array access
		 */
		ARRAY,
		/**
		 * JVM internal access
		 */
		INTERN;
		
		private int rdCnt;
		private int wrCnt;
		
		void incrRd() {
			rdCnt++;
		}
		void incrWr() {
			wrCnt++;
		}
		int getRdCnt() {
			return rdCnt;
		}
		int getWrCnt() {
			return wrCnt;
		}
		
		void reset() {
			rdCnt = wrCnt = 0;
		}
	};

	static final int MAX_MEM = 1024*1024/4;
	// static final int MAX_MEM = 2*1024*1024/4; // 2MB for de2-70 board
	static final int MAX_STACK = Const.STACK_SIZE;	// with internal memory
	static final int MEM_TEST_OFF = 256;
	static final int MAX_SCRATCHPAD = 256;	// 1 KB scratchpad memory
	
	static final int MIN_IO_ADDRESS = -128;

	static final int SYS_INT = 0xf0;
	static final int SYS_EXC = 0xf1;

	static boolean log = false;
	static int nrCpus = 1;

	// references to all simulation instances
	static JopSim js[];


	// static fields for shared heap
	static int[] mem_load = new int[MAX_MEM];
	static int[] mem = new int[MAX_MEM];
	static int heap;
	static int empty_heap;
	
	// local fields for each CPU
	int[] stack = new int[MAX_STACK];
	int[] scratchMem = new int[MAX_SCRATCHPAD];
	Cache cache;
	IOSimMin io;

	int pc, cp, vp, sp, mp;
	int jjp;
	int jjhp;

	//
	// exception handling
	//
	boolean intExcept;
	int exceptReason;
	
	static boolean exit = false;
	static boolean stopped = false;
	
	//
	//	only for statistics
	//
	private WCETInstruction timing;
	static int[] bcTiming = new int[256];
	int[] bcStat = new int[256];
	int rdMemCnt;
	int wrMemCnt;
	int maxInstr;
	int instrCnt;
	long clkCnt;
	int localCnt;
	int maxSp;
	int cacheCost;

	public JopSim(String binaryFile, IOSimMin ioSim, int maxInstructions) {
		
		ioSim.setJopSimRef(this);		
		maxInstr = maxInstructions;
		
		// only first simulation object loads the memory
		if (ioSim.cpuId==0) {
			heap = 0;
			
			try {
				StreamTokenizer in = new StreamTokenizer(new FileReader(binaryFile));
			
				in.wordChars( '_', '_' );
				in.wordChars( ':', ':' );
				in.eolIsSignificant(true);
				in.slashStarComments(true);
				in.slashSlashComments(true);
				in.lowerCaseMode(true);
			
				
				while (in.nextToken()!=StreamTokenizer.TT_EOF) {
					if (in.ttype == StreamTokenizer.TT_NUMBER) {
						mem_load[heap++] = (int) in.nval;
					}
				}
			
			} catch (IOException e) {
				System.out.println(e.getMessage());
				System.exit(-1);
			}

			int instr = mem_load[0];
			System.out.println("Program: "+binaryFile);
			System.out.println(instr + " instruction word ("+(instr*4/1024)+" KB)");
			System.out.println(heap + " words mem read ("+(heap*4/1024)+" KB)");
			empty_heap = heap;
			
			// FIXME: timing should be configurable
			if(nrCpus == 1) {
				timing = new WCETInstruction(WCETInstruction.DEFAULT_R, WCETInstruction.DEFAULT_W);
			} else {
				timing = new WCETInstruction(nrCpus, WCETInstruction.DEFAULT_TIMESLOT, WCETInstruction.DEFAULT_R, WCETInstruction.DEFAULT_W);
			}
			
			for (int i=0; i<256; ++i) {
				int j = timing.getCycles(i, false, 0);
				if (j==-1) j = 80; // rough estimate for invokation of Java implementation
				bcTiming[i] = j;
			}
		}
		
		
		cache = new Cache(mem, this);

		int ocAssoc;
		if(System.getenv("OC_ASSOC") != null) {
			ocAssoc = Integer.parseInt(System.getenv("OC_ASSOC"));
		} else {
			ocAssoc = OBJECT_CACHE_ASSOC;
		}
		objectCacheSim = new ObjectCacheSim(ocAssoc, OBJECT_CACHE_FIELDS);
		io = ioSim;
		
	}

	JopSim(String fn, IOSimMin ioSim) {
		
		this(fn, ioSim, 0);
	}

	/**
	 * An extra (re)start method to test several cache strategies.
	 */
	void start() {

		rdMemCnt = 0;
		wrMemCnt = 0;
		instrCnt = 0;
		clkCnt = 0;
		localCnt = 0;
		maxSp = 0;
		cacheCost = 0;
		for (int i=0; i<256; ++i) bcStat[i] = 0;

		if (io.cpuId==0) {
			heap = empty_heap;
			for (int i=0; i<heap; ++i) mem[i] = mem_load[i];			
		}

		pc = vp = 0;
		sp = Const.STACK_OFF;
		int ptr = readMem(1, Access.INTERN);
		jjp = readMem(ptr+1, Access.INTERN);
		jjhp = readMem(ptr+2, Access.INTERN);

		invokestatic(ptr);			// load main()
	}

/**
*	'debug' functions.
*/
	void noim(int instr) {

		
		invoke(jjp+(instr<<1));
/*
		System.out.println("byte code "+JopInstr.name(instr)+" ("+instr+") not implemented");
System.out.println(mp+" "+pc);
		System.exit(-1);
*/
	}

/**
*	call function in JVM.java with constant on stack
*/
	void jjvmConst(int instr) {

		int idx = readOpd16u();
		int val = readMem(cp+idx, Access.CONST);			// read constant
		// System.out.println("jjvmConst: "+instr+" "+(cp+idx)+" "+val);
		stack[++sp] = val;					// push on stack
		invoke(jjp+(instr<<1));
	}

	/**
	*	call function in JVM.java with index in constant pool on stack
	*/
	void jjvmIdx(int instr) {

		int idx = readOpd16u();
		stack[++sp] = idx;					// push on stack
		invoke(jjp+(instr<<1));
	}

	void dump() {
		
		System.out.print("cp="+cp+" vp="+vp+" sp="+sp+" pc="+pc);
		try {
			System.out.println(" Stack=[..., "+stack[sp-2]+", "+stack[sp-1]+", "+stack[sp]+"]");
		} catch(ArrayIndexOutOfBoundsException ex) {
			System.out.println(" Stack="+stack);
		}
	}

	/**
	 *	helper functions.
	 */
	int readInstrMem(int addr) {

		// System.out.println(addr+" "+mem[addr]);

		if (addr>MAX_MEM || addr<0) throw new JopSimFatalError("readInstrMem: wrong address: "+addr);

		return mem[addr];
	}
	
	int copy_src = 0;
	int copy_dest = 0;
	int copy_pos = 0;
	
	public static final int OBJECT_CACHE_ASSOC = 16;
	public static final int OBJECT_CACHE_FIELDS = 32;
	ObjectCacheSim objectCacheSim;

	/**
	 * a plain memory read
	 * @param addr
	 * @return
	 */
	int readMem(int addr, Access type) {
		
		// System.out.println(addr+" "+mem[addr]);
		rdMemCnt++;
		type.incrRd();

		// translate addresses
		if (addr >= copy_src && addr < copy_src+copy_pos) {
			addr = addr - copy_src + copy_pos;
		}

		// that's an access to our scratchpad memory
		if (addr >= Const.SCRATCHPAD_ADDRESS && addr <= Const.SCRATCHPAD_ADDRESS+MEM_TEST_OFF) {
			return scratchMem[addr%MAX_SCRATCHPAD];
		}
		if (addr == Const.IO_EXCPT) {
			return exceptReason;
		}
		
		if (addr>MAX_MEM+MEM_TEST_OFF || addr<MIN_IO_ADDRESS) {
			throw new JopSimFatalError("readMem: wrong address: "+addr);
		}
		if (addr<0) {
			return io.read(addr);
		}

		return mem[addr%MAX_MEM];
	}
	void writeMem(int addr, int data, Access type) {

		wrMemCnt++;
		type.incrWr();

		// that's an access to our scratchpad memory
		if (addr >= Const.SCRATCHPAD_ADDRESS && addr <= Const.SCRATCHPAD_ADDRESS+MEM_TEST_OFF) {
			scratchMem[addr%MAX_SCRATCHPAD] = data;
			return;
		}
		if (addr>MAX_MEM+MEM_TEST_OFF || addr<MIN_IO_ADDRESS) {
			throw new JopSimFatalError("writeMem: wrong address: "+addr);
		}

		if (addr<0) {
			io.write(addr, data);
			return; // no Simulation of the Wishbone devices
		}
		mem[addr%MAX_MEM] = data;
	}

	void invalCache() {
	}
	
	int readOpd16u() {

		int idx = ((cache.bc(pc)<<8) | (cache.bc(pc+1)&0x0ff)) & 0x0ffff;
		pc += 2;
		return idx;
	}

	int readOpd16s() {

		int i = readOpd16u();
		if ((i&0x8000) != 0) {
			i |= 0xffff0000;
		}
		return i;
	}

	int readOpd8s() {

		return cache.bc(pc++);
	}

	int readOpd8u() {

		return cache.bc(pc++)&0x0ff;
	}

	
//
//	start of JVM :-)
//
	void invokespecial() {
		invokestatic();				// what's the difference?
	}
	
	void checkNullPointer(int addr) throws JopSimRtsException {
		if(addr == 0) throw new JopSimRtsException("Null-Pointer Exception",Const.EXC_NP);
	}
	
	void invokesuper() throws JopSimRtsException {

		int idx = readOpd16u();
		int off = readMem(cp+idx, Access.CONST);	// index in vt and arg count (-1)
		int args = off & 0xff;		// this is args count without obj-ref
		off >>>= 8;
		int ref = stack[sp-args];
		
		checkNullPointer(ref);
		
		// pointer to class info at cp-1
		int vt = readMem(cp-1, Access.CLINFO);
		// pointer to super class at offset 3
		// == Const.CLASS_SUPER
		int sup = readMem(vt+3, Access.CLINFO);
		// the real VT is located at offset 5
		// == Const.CLASS_HEADR
		vt = sup+5;

// System.err.println("invsuper: cp: "+cp+" off: "+off+" args: "+args+" ref: "+ref+" vt: "+vt+" addr: "+(vt+off));
		invoke(vt+off);
	}

	void invokevirtual() throws JopSimRtsException {

		int idx = readOpd16u();
		int off = readMem(cp+idx, Access.CONST);	// index in vt and arg count (-1)
		int args = off & 0xff;		// this is args count without obj-ref
		off >>>= 8;
		int ref = stack[sp-args];
		checkNullPointer(ref);
		
		// pointer to method table in handle at offset 1
		int vt = readMem(ref+1, Access.MVB);
		// System.out.println("invvirt: off: "+off+" args: "+args+" ref: "+ref+" vt: "+vt+" addr: "+(vt+off));
		invoke(vt+off);
	}

	void invokeinterface() throws JopSimRtsException {

		int idx = readOpd16u();
		readOpd16u();				// read historical argument count and 0

		int off = readMem(cp+idx, Access.CONST);			// index in interface table

		int args = off & 0xff;				// this is args count without obj-ref
		off >>>= 8;
		int ref = stack[sp-args];
		checkNullPointer(ref);
		
		// pointer to method table in handle at offset 1
		int vt = readMem(ref+1, Access.MVB);	// pointer to virtual table in obj-1
		int it = readMem(vt-1, Access.CLINFO);	// pointer to interface table one befor vt

		int mp = readMem(it+off, Access.IFTAB);
// System.out.println("invint: off: "+off+" args: "+args+" ref: "+ref+" vt: "+vt+" mp: "+(mp));
		invoke(mp);
	}
/**
*	invokestatic wie es in der JVM sein soll!!!
*/
	void invokestatic() {
		
		int idx = readOpd16u();
		invokestatic(cp+idx);
	}


	void invokestatic(int ptr) {
		
		invoke(readMem(ptr, Access.CONST));
	}

/**
*	do the real invoke. called with a pointer to method struct.
*/
	void invoke(int new_mp) {

		if (log) {
			System.out.println("addr. of meth.struct="+new_mp);		
		}
		int old_vp = vp;
		int old_cp = cp;
		int old_mp = mp;

		mp = new_mp;

		int start = readMem(mp, Access.MTAB);
		int len = start & 0x03ff;
		start >>>= 10;
		cp = readMem(mp+1, Access.MTAB);
		int locals = (cp>>>5) & 0x01f;
		int args = cp & 0x01f;
		cp >>>= 10;

		int old_sp = sp-args;
		vp = old_sp+1;
		sp += locals;
// System.out.println("inv: start: "+start+" len: "+len+" locals: "+locals+" args: "+args+" cp: "+cp);

		stack[++sp] = old_sp;
		stack[++sp] = cache.corrPc(pc);
		stack[++sp] = old_vp;
		stack[++sp] = old_cp;
		stack[++sp] = old_mp;

		pc = cache.invoke(start, len);
	}

/**
*	return wie es sein sollte (oder doch nicht?)
*/
	void vreturn() {

		mp = stack[sp--];
		cp = stack[sp--];
		vp = stack[sp--];
		pc = stack[sp--];
		sp = stack[sp--];

		int start = readMem(mp, Access.MTAB);
		int len = start & 0x03ff;
		start >>>= 10;
		// cp = readMem(mp+1)>>>10;

		pc = cache.ret(start, len, pc);
	}

	void ireturn() {

		int val = stack[sp--];
		vreturn();
		stack[++sp] = val;
	}

	void lreturn() {

		int val1 = stack[sp--];
		int val2 = stack[sp--];
		vreturn();
		stack[++sp] = val2;
		stack[++sp] = val1;
	}
	
	void waitCache(int hiddenCycles) {
		
		int penalty = timing.calculateB(cache.lastAccessWasHit(),cache.wordsLastRead);
		penalty = Math.max(0, penalty-hiddenCycles);
		this.cacheCost += penalty;
		this.clkCnt += penalty;
		this.localCnt += penalty;
	}
	
	void putstatic() {

		int addr = readOpd16u();
		writeMem(addr, stack[sp--], Access.STATIC);
	}

	void getstatic() {

		int addr = readOpd16u();
		stack[++sp] = readMem(addr, Access.STATIC);
	}

	void putstatic_long() {

		int addr = readOpd16u();
		writeMem(addr+1, stack[sp--], Access.STATIC);
		writeMem(addr, stack[sp--], Access.STATIC);
	}

	void getstatic_long() {

		int addr = readOpd16u();
		stack[++sp] = readMem(addr, Access.STATIC);
		stack[++sp] = readMem(addr+1, Access.STATIC);
	}

	void putfield() {

		int off = readOpd16u();
		int val = stack[sp--];
		int ref = stack[sp--];
		checkNullPointer(ref);		
		ref = readMem(ref, Access.HANDLE);
		writeMem(ref+off, val, Access.FIELD);						
	}

	void getfield() {

		int off = readOpd16u();
		int ref = stack[sp];
		checkNullPointer(ref);		
		// handle needs indirection		
		ref = readMem(ref, Access.HANDLE);
		objectCacheSim.accessField(ref,off);
		stack[sp] = readMem(ref+off, Access.FIELD);		
	}
	
	void jopsys_putfield() {

		int val = stack[sp--];
		int off = stack[sp--];
		int ref = stack[sp--];
		checkNullPointer(ref);		
		// handle needs indirection
		ref = readMem(ref, Access.HANDLE);
		writeMem(ref+off, val, Access.FIELD);
	}

	void jopsys_getfield() {

		int off = stack[sp--];
		int ref = stack[sp];
		checkNullPointer(ref);		
		// handle needs indirection
		ref = readMem(ref, Access.HANDLE);
		stack[sp] = readMem(ref+off, Access.FIELD);
	}

	void putfield_long() {

		int off = readOpd16u();
		int val_l = stack[sp--];
		int val_h = stack[sp--];
		int ref = stack[sp--];
		checkNullPointer(ref);		
		// handle needs indirection
		ref = readMem(ref, Access.HANDLE);
		writeMem(ref+off, val_h, Access.FIELD);
		writeMem(ref+off+1, val_l, Access.FIELD);			
	}

	void getfield_long() {

		int off = readOpd16u();
		int ref = stack[sp];
		// handle needs indirection
		checkNullPointer(ref);		
		ref = readMem(ref, Access.HANDLE);
		stack[sp] = readMem(ref+off, Access.FIELD);
		stack[++sp] = readMem(ref+off+1, Access.FIELD);
	}

/**
*	the simulaton.
*
*	sp points to TOS
*/
	void interpret() {

		int new_pc;		// for cond. branches
		int ref, val, idx, val2;
		int a, b, c, d;
		long la, lb;

		if (localCnt>0) {
			--localCnt;
			return;
		}
		if (maxInstr!=0 && instrCnt>=maxInstr) {
			exit=true;
		}

		//
		//	statistic
		//
		++instrCnt;
		if (sp > maxSp) maxSp = sp;


		int instr = cache.bc(pc++) & 0x0ff;

		//
		// exception and interrupt handling
		//
		if (intExcept) {
			instr = SYS_EXC;
			intExcept = false;
		} else {
			// check all 16 instructions for a pending interrupt
			if ((instrCnt&0xf)==0) {
				if (io.intPending()) {
					instr = SYS_INT;					
				}					
			}

		}

		bcStat[instr]++;
		// TODO add cache miss timing and a different timing info for
		// Java implemented bytecodes
		localCnt = bcTiming[instr];
		clkCnt += localCnt;
		--localCnt;

		if (log) {
			String spc = (pc-1)+" ";
			while (spc.length()<4) spc = " "+spc;
			String s = spc+JopInstr.name(instr);
			System.out.print(s+"\t");
			dump();
		}
		try {
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
				case 9 :		// lconst_0
					stack[++sp] = 0;
					stack[++sp] = 0;
					break;
				case 10 :		// lconst_1
					stack[++sp] = 0;
					stack[++sp] = 1;
					break;
				case 11 :		// fconst_0
					stack[++sp] = 0;
					break;
				case 12 :		// fconst_1
					noim(12);
					break;
				case 13 :		// fconst_2
					noim(13);
					break;
				case 14 :		// dconst_0
					stack[++sp] = 0;
					stack[++sp] = 0;
					break;
				case 15 :		// dconst_1
					noim(15);
					break;
				case 16 :		// bipush
					stack[++sp] = readOpd8s();
					break;
				case 17 :		// sipush
					stack[++sp] = readOpd16s();
					break;
				case 18 :		// ldc
					stack[++sp] = readMem(cp+readOpd8u(), Access.CONST);
					break;
				case 19 :		// ldc_w
					stack[++sp] = readMem(cp+readOpd16u(), Access.CONST);
					break;
				case 20 :		// ldc2_w
					idx = readOpd16u();
					stack[++sp] = readMem(cp+idx, Access.CONST);
					stack[++sp] = readMem(cp+idx+1, Access.CONST);
					break;
				case 21 :		// iload
				case 23 :		// fload
				case 25 :		// aload
					idx = readOpd8u();
					stack[++sp] = stack[vp+idx];
					break;
				case 22 :		// lload
					idx = readOpd8u();
					stack[++sp] = stack[vp+idx];
					stack[++sp] = stack[vp+idx+1];
					break;
				case 24 :		// dload
					idx = readOpd8u();
					stack[++sp] = stack[vp+idx];
					stack[++sp] = stack[vp+idx+1];
					break;
				case 42 :		// aload_0
				case 34 :		// fload_0
				case 26 :		// iload_0
					stack[++sp] = stack[vp];
					break;
				case 43 :		// aload_1
				case 35 :		// fload_1
				case 27 :		// iload_1
					stack[++sp] = stack[vp+1];
					break;
				case 44 :		// aload_2
				case 36 :		// fload_2
				case 28 :		// iload_2
					stack[++sp] = stack[vp+2];
					break;
				case 45 :		// aload_3
				case 37 :		// fload_3
				case 29 :		// iload_3
					stack[++sp] = stack[vp+3];
					break;
				case 30 :		// lload_0
				case 38 :		// dload_0
					stack[++sp] = stack[vp];
					stack[++sp] = stack[vp+1];
					break;
				case 31 :		// lload_1
				case 39 :		// dload_1
					stack[++sp] = stack[vp+1];
					stack[++sp] = stack[vp+2];
					break;
				case 32 :		// lload_2
				case 40 :		// dload_2
					stack[++sp] = stack[vp+2];
					stack[++sp] = stack[vp+3];
					break;
				case 33 :		// lload_3
				case 41 :		// dload_3
					stack[++sp] = stack[vp+3];
					stack[++sp] = stack[vp+4];
					break;
				case 50 :		// aaload
				case 51 :		// baload
				case 52 :		// caload
				case 48 :		// faload
				case 46 :		// iaload
				case 53 :		// saload
					idx = stack[sp--];	// index
					ref = stack[sp--];	// ref
					checkNullPointer(ref);
					a = readMem(ref+1, Access.ALEN);
					if (idx<0 || idx>=a) throw new JopSimRtsException("saload: index out of bounds",Const.EXC_AB);
					// handle needs indirection
					ref = readMem(ref, Access.HANDLE);
					stack[++sp] = readMem(ref+idx, Access.ARRAY);							
					break;
				case 47 :		// laload
				case 49 :		// daload
					idx = stack[sp--];	// index
					ref = stack[sp--];	// ref
					checkNullPointer(ref);
					a = readMem(ref+1, Access.ALEN);
					if (idx<0 || idx>=a) throw new JopSimRtsException("laload: index out of bounds",Const.EXC_AB);
					// handle needs indirection
					ref = readMem(ref, Access.HANDLE);
					stack[++sp] = readMem(ref+idx*2, Access.ARRAY);
					stack[++sp] = readMem(ref+idx*2+1, Access.ARRAY);							
					break;
				case 58 :		// astore
				case 56 :		// fstore
				case 54 :		// istore
					idx = readOpd8u();
					stack[vp+idx] = stack[sp--];
					break;
				case 55 :		// lstore
					idx = readOpd8u();
					stack[vp+idx+1] = stack[sp--];
					stack[vp+idx] = stack[sp--];
					break;
				case 57 :		// dstore
					idx = readOpd8u();
					stack[vp+idx+1] = stack[sp--];
					stack[vp+idx] = stack[sp--];
					break;
				case 75 :		// astore_0
				case 67 :		// fstore_0
				case 59 :		// istore_0
					stack[vp] = stack[sp--];
					break;
				case 76 :		// astore_1
				case 68 :		// fstore_1
				case 60 :		// istore_1
					stack[vp+1] = stack[sp--];
					break;
				case 77 :		// astore_2
				case 69 :		// fstore_2
				case 61 :		// istore_2
					stack[vp+2] = stack[sp--];
					break;
				case 78 :		// astore_3
				case 70 :		// fstore_3
				case 62 :		// istore_3
					stack[vp+3] = stack[sp--];
					break;
				case 63 :		// lstore_0
				case 71 :		// dstore_0
					stack[vp+1] = stack[sp--];
					stack[vp] = stack[sp--];
					break;
				case 64 :		// lstore_1
				case 72 :		// dstore_1
					stack[vp+2] = stack[sp--];
					stack[vp+1] = stack[sp--];
					break;
				case 65 :		// lstore_2
				case 73 :		// dstore_2
					stack[vp+3] = stack[sp--];
					stack[vp+2] = stack[sp--];
					break;
				case 66 :		// lstore_3
				case 74 :		// dstore_3
					stack[vp+4] = stack[sp--];
					stack[vp+3] = stack[sp--];
					break;
				case 83 :		// aastore
					noim(83);
					break;
				case 84 :		// bastore
				case 85 :		// castore
				case 81 :		// fastore
				case 79 :		// iastore
				case 86 :		// sastore
					val = stack[sp--];	// value
					idx = stack[sp--];	// index
					ref = stack[sp--];	// ref
					checkNullPointer(ref);
					a = readMem(ref+1, Access.ALEN);
					if (idx<0 || idx>=a) throw new JopSimRtsException("sastore: index out of bounds",Const.EXC_AB);
					// handle needs indirection
					ref = readMem(ref, Access.HANDLE);
					writeMem(ref+idx, val, Access.ARRAY);							
					break;
				case 80 :		// lastore
				case 82 :		// dastore
					val = stack[sp--];	// value
					val2 = stack[sp--];	// value
					idx = stack[sp--];	// index
					ref = stack[sp--];	// ref
					checkNullPointer(ref);
					a = readMem(ref+1, Access.ALEN);
					if (idx<0 || idx>=a) throw new JopSimRtsException("lastore: index out of bounds",Const.EXC_AB);
					// handle needs indirection
					ref = readMem(ref, Access.HANDLE);
					writeMem(ref+idx*2, val2, Access.ARRAY);					
					writeMem(ref+idx*2+1, val, Access.ARRAY);												
					break;
				case 87 :		// pop
					sp--;
					break;
				case 88 :		// pop2
					sp--;
					sp--;
					break;
				case 89 :		// dup
					val = stack[sp];
					stack[++sp] = val;
					break;
				case 90 :		// dup_x1
					a = stack[sp--];
					b = stack[sp--];
					stack[++sp] = a;
					stack[++sp] = b;
					stack[++sp] = a;
					break;
				case 91 :		// dup_x2
					a = stack[sp--];
					b = stack[sp--];
					c = stack[sp--];
					stack[++sp] = a;
					stack[++sp] = c;
					stack[++sp] = b;
					stack[++sp] = a;
					break;
				case 92 :		// dup2
					a = stack[sp--];
					b = stack[sp--];
					stack[++sp] = b;
					stack[++sp] = a;
					stack[++sp] = b;
					stack[++sp] = a;
					break;
				case 93 :		// dup2_x1
					a = stack[sp--];
					b = stack[sp--];
					c = stack[sp--];
					stack[++sp] = b;
					stack[++sp] = a;
					stack[++sp] = c;
					stack[++sp] = b;
					stack[++sp] = a;
					break;
				case 94 :		// dup2_x2
					a = stack[sp--];
					b = stack[sp--];
					c = stack[sp--];
					d = stack[sp--];
					stack[++sp] = b;
					stack[++sp] = a;
					stack[++sp] = d;
					stack[++sp] = c;
					stack[++sp] = b;
					stack[++sp] = a;
					break;
				case 95 :		// swap
					a = stack[sp--];
					b = stack[sp--];
					stack[++sp] = a;
					stack[++sp] = b;
					break;
				case 96 :		// iadd
					val = stack[sp-1] + stack[sp];
					stack[--sp] = val;
					break;
				case 97 :		// ladd
					la = ((long)stack[sp-1] << 32) | ((long)stack[sp] & 0xffffffffL);
					lb = ((long)stack[sp-3] << 32) | ((long)stack[sp-2] & 0xffffffffL);
					la += lb;
					stack[sp-3] = (int)(la >>> 32);
					stack[sp-2] = (int)la;
					sp-=2;
					break;
				case 98 :		// fadd
					noim(98);
					break;
				case 99 :		// dadd
					noim(99);
					break;
				case 100 :		// isub
					val = stack[sp-1] - stack[sp];
					stack[--sp] = val;
					break;
				case 101 :		// lsub
					la = ((long)stack[sp-1] << 32) | ((long)stack[sp] & 0xffffffffL);
					lb = ((long)stack[sp-3] << 32) | ((long)stack[sp-2] & 0xffffffffL);
					la = lb - la;
					stack[sp-3] = (int)(la >>> 32);
					stack[sp-2] = (int)la;
					sp-=2;
					break;
				case 102 :		// fsub
					noim(102);
					break;
				case 103 :		// dsub
					noim(103);
					break;
				case 104 :		// imul
					val = stack[sp-1] * stack[sp];
					stack[--sp] = val;
					break;
				case 105 :		// lmul
					noim(105);
					break;
				case 106 :		// fmul
					noim(106);
					break;
				case 107 :		// dmul
					noim(107);
					break;
				case 108 :		// idiv
					val = stack[sp-1] / stack[sp];
					stack[--sp] = val;
					break;
				case 109 :		// ldiv
					noim(109);
					break;
				case 110 :		// fdiv
					noim(110);
					break;
				case 111 :		// ddiv
					noim(111);
					break;
				case 112 :		// irem
					val = stack[sp-1] % stack[sp];
					stack[--sp] = val;
					break;
				case 113 :		// lrem
					noim(113);
					break;
				case 114 :		// frem
					noim(114);
					break;
				case 115 :		// drem
					noim(115);
					break;
				case 116 :		// ineg
					stack[sp] = -stack[sp];
					break;
				case 117 :		// lneg
					la = ((long)stack[sp-1] << 32) | ((long)stack[sp] & 0xffffffffL);
					la = -la;
					stack[sp-1] = (int)(la >>> 32);
					stack[sp] = (int)la;
					break;
				case 118 :		// fneg
					noim(118);
					break;
				case 119 :		// dneg
					noim(119);
					break;
				case 120 :		// ishl
					val = stack[sp-1] << stack[sp];
					stack[--sp] = val;
					break;
				case 121 :		// lshl
					la = ((long)stack[sp-2] << 32) | ((long)stack[sp-1] & 0xffffffffL);
					la <<= stack[sp];
					stack[sp-2] = (int)(la >>> 32);
					stack[sp-1] = (int)la;
					sp--;
					break;
				case 122 :		// ishr
					val = stack[sp-1] >> stack[sp];
					stack[--sp] = val;
					break;
				case 123 :		// lshr
					la = ((long)stack[sp-2] << 32) | ((long)stack[sp-1] & 0xffffffffL);
					la >>= stack[sp];
					stack[sp-2] = (int)(la >>> 32);
					stack[sp-1] = (int)la;
					sp--;
					break;
				case 124 :		// iushr
					val = stack[sp-1] >>> stack[sp];
					stack[--sp] = val;
					break;
				case 125 :		// lushr
					la = ((long)stack[sp-2] << 32) | ((long)stack[sp-1] & 0xffffffffL);
					la >>>= stack[sp];
					stack[sp-2] = (int)(la >>> 32);
					stack[sp-1] = (int)la;
					sp--;
					break;
				case 126 :		// iand
					val = stack[sp-1] & stack[sp];
					stack[--sp] = val;
					break;
				case 127 :		// land
					la = ((long)stack[sp-1] << 32) | ((long)stack[sp] & 0xffffffffL);
					lb = ((long)stack[sp-3] << 32) | ((long)stack[sp-2] & 0xffffffffL);
					la &= lb;
					stack[sp-3] = (int)(la >>> 32);
					stack[sp-2] = (int)la;
					sp-=2;
					break;
				case 128 :		// ior
					val = stack[sp-1] | stack[sp];
					stack[--sp] = val;
					break;
				case 129 :		// lor
					la = ((long)stack[sp-1] << 32) | ((long)stack[sp] & 0xffffffffL);
					lb = ((long)stack[sp-3] << 32) | ((long)stack[sp-2] & 0xffffffffL);
					la |= lb;
					stack[sp-3] = (int)(la >>> 32);
					stack[sp-2] = (int)la;
					sp-=2;
					break;
				case 130 :		// ixor
					val = stack[sp-1] ^ stack[sp];
					stack[--sp] = val;
					break;
				case 131 :		// lxor
					la = ((long)stack[sp-1] << 32) | ((long)stack[sp] & 0xffffffffL);
					lb = ((long)stack[sp-3] << 32) | ((long)stack[sp-2] & 0xffffffffL);
					la ^= lb;
					stack[sp-3] = (int)(la >>> 32);
					stack[sp-2] = (int)la;
					sp-=2;
					break;
				case 132 :		// iinc
					idx = readOpd8u();
					stack[vp+idx] = stack[vp+idx]+readOpd8s();
					break;
				case 133 :		// i2l
					val = stack[sp];
					stack[sp] = (val >> 16) >> 16;
					stack[sp+1] = val;
					sp++;
					break;
				case 134 :		// i2f
					noim(134);
					break;
				case 135 :		// i2d
					noim(135);
					break;
				case 136 :		// l2i
					val = stack[sp];	// low part
					--sp;				// drop high word
					stack[sp] = val;	// low on stack
					break;
				case 137 :		// l2f
					noim(137);
					break;
				case 138 :		// l2d
					noim(138);
					break;
				case 139 :		// f2i
					noim(139);
					break;
				case 140 :		// f2l
					noim(140);
					break;
				case 141 :		// f2d
					noim(141);
					break;
				case 142 :		// d2i
					noim(142);
					break;
				case 143 :		// d2l
					noim(143);
					break;
				case 144 :		// d2f
					noim(144);
					break;
				case 145 :		// i2b
					noim(145);
					break;
				case 146 :		// i2c
					stack[sp] = stack[sp] & 0x0ffff;
					break;
				case 147 :		// i2s
					noim(147);
					break;
				case 148 :		// lcmp
					la = ((long)stack[sp-1] << 32) | ((long)stack[sp] & 0xffffffffL);
					lb = ((long)stack[sp-3] << 32) | ((long)stack[sp-2] & 0xffffffffL);
					stack[sp-3] = la > lb ? -1 : la < lb ? 1 : 0;
					sp-=3;
					break;
				case 149 :		// fcmpl
					noim(149);
					break;
				case 150 :		// fcmpg
					noim(150);
					break;
				case 151 :		// dcmpl
					noim(151);
					break;
				case 152 :		// dcmpg
					noim(152);
					break;
				case 153 :		// ifeq
				case 198 :		// ifnull
					new_pc = pc-1;
					new_pc += readOpd16s();
					sp--;
					if (stack[sp+1] == 0) pc = new_pc;
					break;
				case 154 :		// ifne
				case 199 :		// ifnonnull
					new_pc = pc-1;
					new_pc += readOpd16s();
					sp--;
					if (stack[sp+1] != 0) pc = new_pc;
					break;
				case 155 :		// iflt
					new_pc = pc-1;
					new_pc += readOpd16s();
					sp--;
					if (stack[sp+1] < 0) pc = new_pc;
					break;
				case 156 :		// ifge
					new_pc = pc-1;
					new_pc += readOpd16s();
					sp--;
					if (stack[sp+1] >= 0) pc = new_pc;
					break;
				case 157 :		// ifgt
					new_pc = pc-1;
					new_pc += readOpd16s();
					sp--;
					if (stack[sp+1] > 0) pc = new_pc;
					break;
				case 158 :		// ifle
					new_pc = pc-1;
					new_pc += readOpd16s();
					sp--;
					if (stack[sp+1] <= 0) pc = new_pc;
					break;
				case 159 :		// if_icmpeq
				case 165 :		// if_acmpeq
					new_pc = pc-1;
					new_pc += readOpd16s();
					sp -= 2;
					if (stack[sp+1] == stack[sp+2]) pc = new_pc;
					break;
				case 160 :		// if_icmpne
				case 166 :		// if_acmpne
					new_pc = pc-1;
					new_pc += readOpd16s();
					sp -= 2;
					if (stack[sp+1] != stack[sp+2]) pc = new_pc;
					break;
				case 161 :		// if_icmplt
					new_pc = pc-1;
					new_pc += readOpd16s();
					sp -= 2;
					if (stack[sp+1] < stack[sp+2]) pc = new_pc;
					break;
				case 162 :		// if_icmpge
					new_pc = pc-1;
					new_pc += readOpd16s();
					sp -= 2;
					if (stack[sp+1] >= stack[sp+2]) pc = new_pc;
					break;
				case 163 :		// if_icmpgt
					new_pc = pc-1;
					new_pc += readOpd16s();
					sp -= 2;
					if (stack[sp+1] > stack[sp+2]) pc = new_pc;
					break;
				case 164 :		// if_icmple
					new_pc = pc-1;
					new_pc += readOpd16s();
					sp -= 2;
					if (stack[sp+1] <= stack[sp+2]) pc = new_pc;
					break;
				case 167 :		// goto
					new_pc = pc-1;
					new_pc += readOpd16s();
					pc = new_pc;
					break;
				case 168 :		// jsr
					noim(168);
					break;
				case 169 :		// ret
					noim(169);
					break;
				case 170 :		// tableswitch
					noim(170);
					break;
				case 171 :		// lookupswitch
					noim(171);
					break;
				case 176 :		// areturn
				case 172 :		// ireturn
				case 174 :		// freturn
					ireturn();
					waitCache(WCETInstruction.IRETURN_HIDDEN_LOAD_CYCLES);					
					break;
				case 173 :		// lreturn
					lreturn();
					waitCache(WCETInstruction.LRETURN_HIDDEN_LOAD_CYCLES);					
					break;
				case 175 :		// dreturn
					lreturn();
					waitCache(WCETInstruction.DRETURN_HIDDEN_LOAD_CYCLES);
					break;
				case 177 :		// return
					vreturn();
					waitCache(WCETInstruction.RETURN_HIDDEN_LOAD_CYCLES);
					break;
				case 178 :		// getstatic
					getstatic();
					break;
				case 179 :		// putstatic
					putstatic();
					break;
				case 180 :		// getfield
					getfield();
					break;
				case 181 :		// putfield
					putfield();
					break;
				case 182 :		// invokevirtual
					invokevirtual();
					waitCache(WCETInstruction.INVOKE_HIDDEN_LOAD_CYCLES);
					break;
				case 183 :		// invokespecial
					invokespecial();
					waitCache(WCETInstruction.INVOKE_HIDDEN_LOAD_CYCLES);
					break;
				case 184 :		// invokestatic
					invokestatic();
					waitCache(WCETInstruction.INVOKE_HIDDEN_LOAD_CYCLES);
					break;
				case 185 :		// invokeinterface
					invokeinterface();
					waitCache(WCETInstruction.INVOKE_HIDDEN_LOAD_CYCLES);
					break;
				case 186 :		// unused_ba
					noim(186);
					break;
				case 187 :		// new
					jjvmConst(187);

/*	use function in JVM.java
					idx = readOpd16u();
					val = readMem(cp+idx);	// pointer to class struct
					writeMem(heap, val+2);	// pointer to method table on objectref-1
					++heap;
					val = readMem(val);		// instance size
// TODO init object to zero
					stack[++sp] = heap;		// objectref
					heap += val;
System.out.println("new heap: "+heap);
*/
					break;
				case 188 :		// newarray
					stack[++sp]=readOpd8u();		// use typ info
					// invoke JVM.f_newarray(int count,val);
					invoke(jjp+(188<<1));
					/*

					val = stack[sp--];	// count from stack
					writeMem(heap, val);
					++heap;
					stack[++sp] = heap;	// ref to first element
					heap += val;
// System.out.println("newarray heap: "+heap);
 * 
 */
					break;
				case 189 :		// anewarray
					jjvmConst(189);
					break;
				case 190 :		// arraylength
					ref = stack[sp--];	// ref from stack
					checkNullPointer(ref);
					// lenght in handle at offset 1
					stack[++sp] = readMem(ref+1, Access.ALEN);
					break;
				case 191 :		// athrow
					noim(191);
					break;
				case 192 :		// checkcast
					jjvmConst(192);
					break;
				case 193 :		// instanceof
					jjvmConst(193);
					break;
				case 194 :		// monitorenter
					if (io.monEnter()) {
						sp--;	// we don't use the objref
					} else {
						pc--;	// restart if we don't get the global lock
					}
					invalCache();
					break;
				case 195 :		// monitorexit
					sp--;		// we don't use the objref
					io.monExit();
					break;
				case 196 :		// wide
					noim(196);
					break;
				case 197 :		// multianewarray
					noim(197);
/* 
					stack[++sp] = readOpd8u();		// push dimenensions onto the stack
					// invoke JVM.f_multianewarray(int dim);
					invoke(jjp+(197<<1));
					readOpd16u();	// ignore type information
*/
					break;
				case 200 :		// goto_w
					noim(200);
					break;
				case 201 :		// jsr_w
					noim(201);
					break;
				case 202 :		// breakpoint
					noim(202);
					break;
				case 203 :		// resCB
					noim(203);
					break;
				case 204 :		// jopsys_inval
					invalCache();
					break;
				case 205 :		// resCD
					noim(205);
					break;
				case 206 :		// resCE
					noim(206);
					break;
				case 207 :		// resCF
					noim(207);
					break;
				case 208 :		// jopsys_null
					noim(208);
					break;
				case 209 :		// jopsys_rd
					ref = stack[sp--];
					stack[++sp] = readMem(ref, Access.INTERN);
					break;
				case 210 :		// jopsys_wr
					ref = stack[sp--];
					val = stack[sp--];
					writeMem(ref, val, Access.INTERN);
					break;
				case 211 :		// jopsys_rdmem
					ref = stack[sp--];
					stack[++sp] = readMem(ref, Access.INTERN);
					break;
				case 212 :		// jopsys_wrmem
					ref = stack[sp--];
					val = stack[sp--];
					writeMem(ref, val, Access.INTERN);
					break;
				case 213 :		// jopsys_rdint
					ref = stack[sp--];
//
//	first variables in jvm.asm
//
//	mp		?		// pointer to method struct
//	cp		?		// pointer to constants
//	heap	?		// start of heap
//
//	jjp		?		// pointer to meth. table of Java JVM functions
//	jjhp	?		// pointer to meth. table of Java JVM help functions
//
//	moncnt	?		// counter for monitor

					if (ref==0) {
						val = mp;
					} else if (ref==1) {
						val = cp;
					} else if (ref==2) {
						val = heap;
					} else if (ref==3) {
						val = jjp;
					} else if (ref==4) {
						val = jjhp;
					} else if (ref==5) {
						val = io.moncnt;
					} else {
						val = stack[ref];
					}
					stack[++sp] = val;
					break;
				case 214 :		// jopsys_wrint
					ref = stack[sp--];
					val = stack[sp--];
					if (ref==0) {
						mp = val;
					} else if (ref==1) {
						cp = val;
					} else if (ref==2) {
						heap = val;
// System.out.println("jopsys_wrint: heap "+heap);
					} else if (ref==3) {
						jjp = val;
					} else if (ref==4) {
						jjhp = val;
					} else if (ref==5) {
						io.moncnt = val;
					} else {
						stack[ref] = val;
					}
					break;
				case 215 :		// jopsys_getsp
					val = sp;
					stack[++sp] = val;
					break;
				case 216 :		// jopsys_setsp
					val = stack[sp--];
					sp = val;
					break;
				case 217 :		// jopsys_getvp
					stack[++sp] = vp;
					break;
				case 218 :		// jopsys_setvp
					vp = stack[sp--];
					break;
				case 219 :		// jopsys_int2ext
// public static native void int2extMem(int intAdr, int extAdr, int cnt);
					a = stack[sp--];
					b = stack[sp--];
					// handle needs indirection
					b = readMem(b, Access.HANDLE);
					c = stack[sp--];
					for(; a>=0; --a) {
						writeMem(b+a, stack[c+a], Access.ARRAY);
					}
					break;
				case 220 :		// jopsys_ext2int
// public static native void ext2intMem(int extAdr, int intAdr, int cnt);
					a = stack[sp--];
					b = stack[sp--];
					c = stack[sp--];
					// handle needs indirection
					c = readMem(c, Access.HANDLE);
					for(; a>=0; --a) {
						stack[b+a] = readMem(c+a, Access.ARRAY);
					}
					break;
				case 221 :		// jopsys_nop
					break;
				case 222 :		// jopsys_invoke
					a = stack[sp--];
					invoke(a);
					break;
				case 223 :		// jopsys_cond_move
					a = stack[sp--];
					b = stack[sp--];
					c = stack[sp--];
					stack[++sp] = a!=0 ? c : b;
//					noim(223);
					break;
				case 224 :		// resE0 - getstatic_ref
					getstatic();
					break;
				case 225 :		// resE1 - putstatic_ref
					jjvmIdx(225);	// use JVM.java version
					//putstatic();
					break;
				case 226 :		// resE2 - getfield_ref
					getfield();
					break;
				case 227 :		// resE3 - putfield_ref
					jjvmIdx(227);	// use JVM.java version
					//putfield();
					break;
				case 228 :		// resE4 - getstatic_long
					getstatic_long();
					break;
				case 229 :		// resE5 - putstatic_long
					putstatic_long();
					break;
				case 230 :		// resE6 - getfield_long
					getfield_long();
					break;
				case 231 :		// resE7 - putfield_long
					putfield_long();
					break;
				case 232 :		// resE8 - jopsys_memcpy
					a = stack[sp--];
					b = stack[sp--];
					c = stack[sp--];
					if (a >= 0) {
						writeMem(c+a, readMem(b+a, Access.INTERN), Access.INTERN);
						copy_src = b;
						copy_dest = c;
						copy_pos = a+1;
					} else {
						copy_src = b;
						copy_dest = c;
						copy_pos = 0;
					}
					break;
				case 233 :		// resE9 - jopsys_getfield
					jopsys_getfield();
					break;
				case 234 :		// resEA - jopsys_putfield
					jopsys_putfield();
					break;
				case 235 :		// resEB
					noim(235);
					break;
				case 236 :		// invokesuper
					invokesuper();
					waitCache(WCETInstruction.INVOKE_HIDDEN_LOAD_CYCLES);
					break;
				case 237 :		// resED
					noim(237);
					break;
				case 238 :		// resEE - jopsys_getstatic
					stack[sp] = readMem(stack[sp], Access.STATIC);
					break;
				case 239 :		// resEF - jopsys_putstatic
					writeMem(stack[sp], stack[sp-1], Access.STATIC);
					sp -= 2;
					break;
				case 240 :		// sys_int
					--pc;		// correct wrong increment on jpc
					invoke(jjhp);	// interrupt() is at offset 0
					break;
				case 241 :		// sys_exc exception handling
					--pc;		// correct wrong increment on jpc
					invoke(jjhp+6);	// exception() is at offset 3*2
					break;
				case 242 :		// resF2
					noim(242);
					break;
				case 243 :		// resF3
					noim(243);
					break;
				case 244 :		// resF4
					noim(244);
					break;
				case 245 :		// resF5
					noim(245);
					break;
				case 246 :		// resF6
					noim(246);
					break;
				case 247 :		// resF7
					noim(247);
					break;
				case 248 :		// resF8
					noim(248);
					break;
				case 249 :		// resF9
					noim(249);
					break;
				case 250 :		// resFA
					noim(250);
					break;
				case 251 :		// resFB
					noim(251);
					break;
				case 252 :		// resFC
					noim(252);
					break;
				case 253 :		// resFD
					noim(253);
					break;
				case 254 :		// sys_noim
					noim(254);
					break;
				case 255 :		// sys_init
					noim(255);
					break;

				default:
					noim(instr);
			}
		} catch(JopSimRtsException rtsEx) {
			this.intExcept = true;
			this.exceptReason = rtsEx.getReason();
		}
	}

	/**
	 * Reset all simulation counters. We are not interested in the
	 * startup code (GC memory clean). Maybe in class initializers?
	 * Invoked due to an I/O access just before invoking main().
	 */
	void resetStat() {
		
		for (Access a : Access.values()) {
			a.reset();
		}
		for (int i=0; i<bcStat.length; ++i) {
			bcStat[i] = 0;
		}
		instrCnt = 0;
		rdMemCnt = 0;
		wrMemCnt = 0;
		cache.resetCnt();
	}
	/**
	 * Print execution statistics.
	 */
	void stat() {

		System.out.println();
		System.out.println("CPU "+io.cpuId+":");
		int sum = 0;
		int sumcnt = 0;
//		// that's the JopInstr version
//		for (int i=0; i<256; ++i) {
//			if (bcStat[i] > 0) {
//				System.out.println(bcStat[i]+"\t"+(bcStat[i]*JopInstr.cnt(i))+"\t"+JopInstr.name(i));
//				sum += bcStat[i];
//				sumcnt += bcStat[i]*JopInstr.cnt(i);
//			}
//		}
//		System.out.println();
//		System.out.println(sum+" instructions, "+sumcnt+" cycles");
		// That's the version with WCETInstruction
		sum = sumcnt = 0;
		for (int i=0; i<256; ++i) {
			if (bcStat[i] > 0) {
				int cnt = timing.getCyclesEstimate(i, false, 0);
//				System.out.println(bcStat[i]+"\t"+(bcStat[i]*cnt)+"\t"+JopInstr.name(i));
				sum += bcStat[i];
				sumcnt += bcStat[i]*cnt;
			}
		}
		System.out.println();
		System.out.println(sum+" instructions, "+sumcnt+" cycles (no M$ cycles)");
		System.out.println("CPI: "+ ((float) sumcnt/sum));

		System.out.println(maxSp+" maximum sp");
//		System.out.println(heap+" heap"); not the heap pointer anymore
//		System.out.println();
		System.out.println(instrCnt+" Instructions executed");
		int insByte = cache.instrBytes();
		System.out.println(insByte+" Instructions bytes");
		System.out.println(((float) insByte/instrCnt)+" average Instruction length");
		
		System.out.println();
		System.out.println("\tType \t&       Load &        &      Store &        \\\\");
		int ld = 0, st=0;
		for (Access a : Access.values()) {
			ld += a.rdCnt; st += a.wrCnt;
//			System.out.printf("\t%s\t& %10d & %2d\\%% & %10d & %2d\\%% \\\\%n",
//					a.name(), a.rdCnt, (a.rdCnt*1000/rdMemCnt+5)/10, a.wrCnt, (a.wrCnt*1000/wrMemCnt+5)/10);
			int rdPerc = (int) ((double) a.rdCnt/(rdMemCnt + wrMemCnt)*1000);
			int wrPerc = (int) ((double) a.wrCnt/(rdMemCnt + wrMemCnt)*1000);
			System.out.printf("\t%s\t& %10d & %2d.%1d\\%% & %10d & %2d.%1d\\%% \\\\%n",
					a.name(), a.rdCnt, rdPerc/10, rdPerc%10, 
					a.wrCnt, wrPerc/10, wrPerc%10);
		}
		System.out.println("\t\\midrule");
		System.out.printf("\tSum\t& %10d &        & %10d &        \\\\%n", ld, st);
		
		System.out.println();
		System.out.println("memory word: "+rdMemCnt+" load "+wrMemCnt+" store");
		System.out.println("memory word per instruction: "+
			((float) rdMemCnt/instrCnt)+" load "+
			((float) wrMemCnt/instrCnt)+" store");
		System.out.println("total method cache load cycles: "+this.cacheCost);
		System.out.println();


	}
	
	/**
	 * Stop the simulation (from the VSIS plugin)
	 */
	public static void cancel() {
		exit = true;
		stopped = true;
	}

	/**
	 * Signal detection of JVM exit!
	 */
	public static void exit() {
		exit = true;
	}
	
	public static int getArgs(String args[]) {
		log = System.getProperty("log", "false").equals("true");
		nrCpus = Integer.parseInt(System.getProperty("cpucnt", "1"));
		js = new JopSim[nrCpus];

		int maxInstr=0;
		
		if (args.length==1) {
			maxInstr=0;
		} else if (args.length==2) {
			maxInstr = Integer.parseInt(args[1]);
		} else {
			System.out.println("usage: java JopSim file.bin [max instr]");
			System.exit(-1);
		}
		return maxInstr;
	}
	
	/** simple runner: 1 cpu, 1 cache implementation */
	public void runSim() {
		cache.use(0);
		start();
		while(! exit) { interpret(); }
		if (stopped) {
			System.out.println();
			System.out.println("JopSim stopped");
		}
		System.out.println();
		stat();
		cache.stat();
	}
	
	/** run simulation for multicore JOP */
	public static void runSimulation() {
		
		// loop over all cache simulations
		for (int i=0; i<js[0].cache.cnt(); ++i) {
			for (int j=0; j<nrCpus; ++j) {
				js[j].cache.use(i);
				js[j].start();				
			}
			while (!exit) {
				js[0].interpret();
				if (nrCpus != 1 && IOSimMin.startCMP) {
					for (int j = 1; j < nrCpus; ++j) {
						js[j].interpret();
					}
				}
			}
			if (stopped) {
				System.out.println();
				System.out.println("JopSim stopped");
			}
			System.out.println();
			for (int j=0; j<nrCpus; ++j) {
				if (i==0) js[j].stat();
				js[j].cache.stat();				
			}
		}
	}

	public static void main(String args[]) {

		IOSimMin io;

		int maxInstr = getArgs(args);
		
		String ioDevice = System.getProperty("ioclass");
		
		for (int i=0; i<nrCpus; ++i) {
			// select the IO simulation
			if (ioDevice!=null) {
				try {
					io = (IOSimMin) Class.forName("com.jopdesign.tools."+ioDevice).newInstance();
				} catch (Exception e) {
					e.printStackTrace();
					io = new IOSimMin();
				}			
			} else {
				io = new IOSimMin();			
			}
			io.setCpuId(i);
			js[i] = new JopSim(args[0], io, maxInstr);
		}
		
		runSimulation();
	}
	/* interface for IOSim */
	
	public long getClkCnt() {
		return clkCnt;
	}

	public int getExceptReason() {
		return this.exceptReason;
	}
	
	public void setException(int val) {
		intExcept = true;
		exceptReason = val;		
	}

	public Cache getCache() {
		return cache;
	}

	public int getCacheCost() {
		return this.cacheCost;
	}

}
