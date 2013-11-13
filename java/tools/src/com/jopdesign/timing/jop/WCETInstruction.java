/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2006-2008, Martin Schoeberl (martin@jopdesign.com)
  Copyright (C) 2006, Rasmus Ulslev Pedersen
  
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

package com.jopdesign.timing.jop;

import com.jopdesign.common.misc.MiscUtils;
import com.jopdesign.tools.JopInstr;

import java.util.HashMap;
import java.util.Map;


/**
 * WCETInstruction provides WCET info on byte code granularity, using a hardcoded
 * lookup table. For implemented architectures, it is deprecated in favor of
 * implemenations based on microcode timing analysis, {@see SingleCoreTiming}
 */
public class WCETInstruction {

	// indicate that wcet is not available for this bytecode
	public static final int WCETNOTAVAILABLE = -1;

	// hidden load cycles
	public static final int INVOKE_HIDDEN_LOAD_CYCLES = 37;
	public static final int RETURN_HIDDEN_LOAD_CYCLES = 9;
	public static final int ARETURN_HIDDEN_LOAD_CYCLES = 10;
	public static final int FRETURN_HIDDEN_LOAD_CYCLES = 10;
	public static final int IRETURN_HIDDEN_LOAD_CYCLES = 10;
	public static final int LRETURN_HIDDEN_LOAD_CYCLES = 11;
	public static final int DRETURN_HIDDEN_LOAD_CYCLES = 11;
	public static final int MIN_HIDDEN_LOAD_CYCLES = RETURN_HIDDEN_LOAD_CYCLES; 
	
	// Default configuration
	// dspio Board: r=1, w=2
	// DE2 Board: r=3, w=5
	public static final int DEFAULT_R = 1;
	public static final int DEFAULT_W = 2;

	public static int DEFAULT_CPUS = 8;
	public static int DEFAULT_TIMESLOT = 10; // has to be greater r !!

	// the read and write wait states, ram_cnt - 1
	public int r;
	public int w;
	// cache read wait state (r-1)
	public int c() { return r - 1; }
	
	// CMP: Multiprocessing with time sliced memory access (christof)
	public static boolean cmp = false;

	// Arbitration
	public int cpus = 8;
	public int timeslot = 10; // has to be greater r !!

	// Arbitration Array
	public boolean [] arbiter;

	// bh: max cycles seems unnecessary: you should check statically whether
	//     the configuration is valid (e.g. timeslot > delay)
	public static final int MAX_CYCLES = 1000000;
	public int getArbiterPeriod() { return cpus*timeslot; }
	
	// Build Instructions
	public static final int NOP = 0;
	public static final int RD = 1;
	public static final int WR = 2;
	
	// Static Instruction patterns
	public WCETMemInstruction ldc;
	public WCETMemInstruction ldc_w;
	public WCETMemInstruction ldc2_w;
	public WCETMemInstruction xaload;
	public WCETMemInstruction xastore;
	public WCETMemInstruction getstaticx;
	public WCETMemInstruction putstatic;
	public WCETMemInstruction getfield;
	public WCETMemInstruction putfield;
	public WCETMemInstruction arraylength;
	public WCETMemInstruction jopsys_rdx;
	public WCETMemInstruction jopsys_wrx;
	
	// Dynamic Instruction pattern dependent on cache
	public WCETMemInstruction ireturn; 
	public WCETMemInstruction freturn;
	public WCETMemInstruction areturn;
	public WCETMemInstruction lreturn;
	public WCETMemInstruction dreturn;
	public WCETMemInstruction returnx; // return
	public WCETMemInstruction invokevirtual;
	public WCETMemInstruction invokespecial; 
	public WCETMemInstruction invokestatic; // same as invokespecial
	public WCETMemInstruction invokeinterface;

	/** Single-Core timing */
	public WCETInstruction(int rws, int wws) {
		configure(rws,wws);
	}
	
	/** Multi-Core timing */
	public WCETInstruction(int cpus, int timeslot, int rws, int wws) {
		configureCMP(cpus, timeslot, rws, wws);		
	}
	
	public void configure(int rws, int wws) {
		cmp = false;
		r = rws;
		w = wws;
	}

	public void configureCMP(int cpuCount, int timeslot, int rws, int wws) {

		if(cpuCount < 1) throw new AssertionError("WCETInstruction: cpus < 1");
		if(timeslot < r) throw new AssertionError("WCETInstruction (cmp): timeslot < r");
		cmp = true;
		r = rws;
		w = wws;
		cpus = cpuCount;
		initCMP(timeslot);		
	}

	private void initCMP(int timeslot) {
		// Initialize 
		this.timeslot = timeslot;
		initArbiter();
		generateStaticInstr();		
	}
	
	//Native bytecodes (see jvm.asm)
	private static final int JOPSYS_INVAL = 204;
	private static final int JOPSYS_RD = 209;
	private static final int JOPSYS_WR = 210;
	private static final int JOPSYS_RDMEM = 211;
	private static final int JOPSYS_WRMEM = 212;
	private static final int JOPSYS_RDINT = 213;
	private static final int JOPSYS_WRINT = 214;
	private static final int JOPSYS_GETSP = 215;
	private static final int JOPSYS_SETSP = 216;
	private static final int JOPSYS_GETVP = 217;
	private static final int JOPSYS_SETVP = 218;
	private static final int JOPSYS_INT2EXT = 219;
	private static final int JOPSYS_EXT2INT = 220;
	private static final int JOPSYS_NOP = 221;
	private static final int GETSTATIC_REF = 224;	
	private static final int GETFIELD_REF = 226;
	private static final int GETSTATIC_LONG = 228;	
	private static final int PUTSTATIC_LONG = 229;
	private static final int GETFIELD_LONG = 230;	
	private static final int PUTFIELD_LONG = 231;
	private static final int JOPSYS_MEMCPY = 232;
	private static final int JOPSYS_GETFIELD = 233;
	private static final int JOPSYS_PUTFIELD = 234;
	private static final int JOPSYS_GETSTATIC = 238;
	private static final int JOPSYS_PUTSTATIC = 239;
	private static String ILLEGAL_OPCODE = "ILLEGAL_OPCODE";

	/**
	 * Names of opcodes.
	 */
	protected static final String[] OPCODE_NAMES = { "nop", "aconst_null",
			"iconst_m1", "iconst_0", "iconst_1", "iconst_2", "iconst_3",
			"iconst_4", "iconst_5", "lconst_0", "lconst_1", "fconst_0",
			"fconst_1", "fconst_2", "dconst_0", "dconst_1", "bipush", "sipush",
			"ldc", "ldc_w", "ldc2_w", "iload", "lload", "fload", "dload",
			"aload", "iload_0", "iload_1", "iload_2", "iload_3", "lload_0",
			"lload_1", "lload_2", "lload_3", "fload_0", "fload_1", "fload_2",
			"fload_3", "dload_0", "dload_1", "dload_2", "dload_3", "aload_0",
			"aload_1", "aload_2", "aload_3", "iaload", "laload", "faload",
			"daload", "aaload", "baload", "caload", "saload", "istore",
			"lstore", "fstore", "dstore", "astore", "istore_0", "istore_1",
			"istore_2", "istore_3", "lstore_0", "lstore_1", "lstore_2",
			"lstore_3", "fstore_0", "fstore_1", "fstore_2", "fstore_3",
			"dstore_0", "dstore_1", "dstore_2", "dstore_3", "astore_0",
			"astore_1", "astore_2", "astore_3", "iastore", "lastore",
			"fastore", "dastore", "aastore", "bastore", "castore", "sastore",
			"pop", "pop2", "dup", "dup_x1", "dup_x2", "dup2", "dup2_x1",
			"dup2_x2", "swap", "iadd", "ladd", "fadd", "dadd", "isub", "lsub",
			"fsub", "dsub", "imul", "lmul", "fmul", "dmul", "idiv", "ldiv",
			"fdiv", "ddiv", "irem", "lrem", "frem", "drem", "ineg", "lneg",
			"fneg", "dneg", "ishl", "lshl", "ishr", "lshr", "iushr", "lushr",
			"iand", "land", "ior", "lor", "ixor", "lxor", "iinc", "i2l", "i2f",
			"i2d", "l2i", "l2f", "l2d", "f2i", "f2l", "f2d", "d2i", "d2l",
			"d2f", "i2b", "i2c", "i2s", "lcmp", "fcmpl", "fcmpg", "dcmpl",
			"dcmpg", "ifeq", "ifne", "iflt", "ifge", "ifgt", "ifle",
			"if_icmpeq", "if_icmpne", "if_icmplt", "if_icmpge", "if_icmpgt",
			"if_icmple", "if_acmpeq", "if_acmpne", "goto", "jsr", "ret",
			"tableswitch", "lookupswitch", "ireturn", "lreturn", "freturn",
			"dreturn", "areturn", "return", "getstatic", "putstatic",
			"getfield", "putfield", "invokevirtual", "invokespecial",
			"invokestatic", "invokeinterface", ILLEGAL_OPCODE, "new",
			"newarray", "anewarray", "arraylength", "athrow", "checkcast",
			"instanceof", "monitorenter", "monitorexit", "wide",
			"multianewarray", "ifnull", "ifnonnull", "goto_w", "jsr_w",
			"breakpoint", ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE,
			ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE,
			ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE,
			ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE,
			ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE,
			ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE,
			ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE,
			ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE,
			ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE,
			ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE,
			ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE,
			ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE,
			ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE,
			ILLEGAL_OPCODE, ILLEGAL_OPCODE };

	// TODO: make those missing (the rup/ms specific ones, but are they
	// reachable?)

	/**
	 * Get the name using the opcode. Used when WCA toWCAString().
	 * 
	 * @param opcode
	 * @return name or "ILLEGAL_OPCODE"
	 */
	public static String getNameFromOpcode(int opcode) {
		return OPCODE_NAMES[opcode];
	}

	/**
	 * See the WCET values
	 * @return table body of opcodes with info
	 */

	public String toWCAString() {
		StringBuffer sb = new StringBuffer();

		sb.append("Table of WCETInstruction cycles\n");
		sb
				.append("=============================================================\n");
		sb
				.append("Instruction               Hit cycles  Miss cycles  Mich. info\n");
		sb.append("                            n=0/1000     n=0/1000\n");
		sb
				.append("-------------------------------------------------------------\n");

		for (int op = 0; op <= 255; op++) {
			// name (25)
			String str = new String("[" + op + "] " + getNameFromOpcode(op));
			sb.append(MiscUtils.postpad(str, 25));

			//hit n={0,1000}
			String hitstr = getCycles(op, false, 0) + "/"
					+ getCycles(op, false, 1000);
			hitstr = MiscUtils.prepad(hitstr, 12);

			//miss n={0,1000}
			String missstr = getCycles(op, true, 0) + "/"
					+ getCycles(op, true, 1000);
			missstr = MiscUtils.prepad(missstr, 12);

			sb.append(hitstr + missstr + "\n");
		}
		sb
				.append("=============================================================\n");
		sb.append("Info: b(n=1000)=" + calculateB(false, 1000) + " c=" + c() + " r=" + r
				+ " w=" + w + "\n");
		sb
				.append("Signatures: V void, Z boolean, B byte, C char, S short, I int, J long, F float, D double, L class, [ array\n");
		return sb.toString();
	}

	public static void main(String[] args) {
					
		WCETInstruction inst = new WCETInstruction(DEFAULT_R, DEFAULT_W);
		for (int i=0; i<256; ++i) {
			int cnt = inst.getCycles(i, false, 0);
			if (cnt==-1) cnt = 1000;
			System.out.println(i+"\t"+cnt);
		}
	}
	
	/**
	 * Returns the wcet count for the instruction.
	 * 
	 * @see table D.1 in ms thesis
	 * @param opcode
	 * @param pmiss <code>true</code> if the instruction referenced by 
	 * 		  <code>opcode</code> causes a cache miss, and <code>false</code> otherwise 
	 * @param n for invoke/return instructions, the length of the receiver/caller in words,
	 * 		  0 otherwise
	 * @return wcet cycle count or -1 if wcet not available
	 */
	public int getCycles(int opcode, boolean pmiss, int n) {
		int wcet = -1;
		// cache load time
		int loadTime = calculateB(!pmiss, n);
		switch (opcode) {
		// NOP = 0
		case org.apache.bcel.Constants.NOP:
			wcet = 1;
			break;
		// ACONST_NULL = 1
		case org.apache.bcel.Constants.ACONST_NULL:
			wcet = 1;
			break;
		// ICONST_M1 = 2
		case org.apache.bcel.Constants.ICONST_M1:
			wcet = 1;
			break;
		// ICONST_0 = 3
		case org.apache.bcel.Constants.ICONST_0:
			wcet = 1;
			break;
		// ICONST_1 = 4
		case org.apache.bcel.Constants.ICONST_1:
			wcet = 1;
			break;
		// ICONST_2 = 5
		case org.apache.bcel.Constants.ICONST_2:
			wcet = 1;
			break;
		// ICONST_3 = 6
		case org.apache.bcel.Constants.ICONST_3:
			wcet = 1;
			break;
		// ICONST_4 = 7
		case org.apache.bcel.Constants.ICONST_4:
			wcet = 1;
			break;
		// ICONST_5 = 8
		case org.apache.bcel.Constants.ICONST_5:
			wcet = 1;
			break;
		// LCONST_0 = 9
		case org.apache.bcel.Constants.LCONST_0:
			wcet = 2;
			break;
		// LCONST_1 = 10
		case org.apache.bcel.Constants.LCONST_1:
			wcet = 2;
			break;
		// FCONST_0 = 11
		case org.apache.bcel.Constants.FCONST_0:
			wcet = 1;
			break;
		// FCONST_1 = 12
		case org.apache.bcel.Constants.FCONST_1:
			wcet = -1;
			break;
		// FCONST_2 = 13
		case org.apache.bcel.Constants.FCONST_2:
			wcet = -1;
			break;
		// DCONST_0 = 14
		case org.apache.bcel.Constants.DCONST_0:
			wcet = 2;
			break;
		// DCONST_1 = 15
		case org.apache.bcel.Constants.DCONST_1:
			wcet = -1;
			break;
		// BIPUSH = 16
		case org.apache.bcel.Constants.BIPUSH:
			wcet = 2;
			break;
		// SIPUSH = 17
		case org.apache.bcel.Constants.SIPUSH:
			wcet = 3;
			break;
		// LDC = 18
		case org.apache.bcel.Constants.LDC:
			wcet = 7 + r;
			if (cmp==true)
				wcet = ldc.wcet;
			break;
		// LDC_W = 19
		case org.apache.bcel.Constants.LDC_W:
			wcet = 8 + r;
			if (cmp==true)
				wcet = ldc_w.wcet;
			break;
		// LDC2_W = 20
		case org.apache.bcel.Constants.LDC2_W:
			wcet = 17;
			if (r > 2) {
				wcet += r - 2;
			}
			if (r > 1) {
				wcet += r - 1;
			}
			if (cmp==true){
				wcet = ldc2_w.wcet;
			}
			break;
		// ILOAD = 21
		case org.apache.bcel.Constants.ILOAD:
			wcet = 2;
			break;
		// LLOAD = 22
		case org.apache.bcel.Constants.LLOAD:
			wcet = 11;
			break;
		// FLOAD = 23
		case org.apache.bcel.Constants.FLOAD:
			wcet = 2;
			break;
		// DLOAD = 24
		case org.apache.bcel.Constants.DLOAD:
			wcet = 11;
			break;
		// ALOAD = 25
		case org.apache.bcel.Constants.ALOAD:
			wcet = 2;
			break;
		// ILOAD_0 = 26
		case org.apache.bcel.Constants.ILOAD_0:
			wcet = 1;
			break;
		// ILOAD_1 = 27
		case org.apache.bcel.Constants.ILOAD_1:
			wcet = 1;
			break;
		// ILOAD_2 = 28
		case org.apache.bcel.Constants.ILOAD_2:
			wcet = 1;
			break;
		// ILOAD_3 = 29
		case org.apache.bcel.Constants.ILOAD_3:
			wcet = 1;
			break;
		// LLOAD_0 = 30
		case org.apache.bcel.Constants.LLOAD_0:
			wcet = 2;
			break;
		// LLOAD_1 = 31
		case org.apache.bcel.Constants.LLOAD_1:
			wcet = 2;
			break;
		// LLOAD_2 = 32
		case org.apache.bcel.Constants.LLOAD_2:
			wcet = 2;
			break;
		// LLOAD_3 = 33
		case org.apache.bcel.Constants.LLOAD_3:
			wcet = 11;
			break;
		// FLOAD_0 = 34
		case org.apache.bcel.Constants.FLOAD_0:
			wcet = 1;
			break;
		// FLOAD_1 = 35
		case org.apache.bcel.Constants.FLOAD_1:
			wcet = 1;
			break;
		// FLOAD_2 = 36
		case org.apache.bcel.Constants.FLOAD_2:
			wcet = 1;
			break;
		// FLOAD_3 = 37
		case org.apache.bcel.Constants.FLOAD_3:
			wcet = 1;
			break;
		// DLOAD_0 = 38
		case org.apache.bcel.Constants.DLOAD_0:
			wcet = 2;
			break;
		// DLOAD_1 = 39
		case org.apache.bcel.Constants.DLOAD_1:
			wcet = 2;
			break;
		// DLOAD_2 = 40
		case org.apache.bcel.Constants.DLOAD_2:
			wcet = 2;
			break;
		// DLOAD_3 = 41
		case org.apache.bcel.Constants.DLOAD_3:
			wcet = 11;
			break;
		// ALOAD_0 = 42
		case org.apache.bcel.Constants.ALOAD_0:
			wcet = 1;
			break;
		// ALOAD_1 = 43
		case org.apache.bcel.Constants.ALOAD_1:
			wcet = 1;
			break;
		// ALOAD_2 = 44
		case org.apache.bcel.Constants.ALOAD_2:
			wcet = 1;
			break;
		// ALOAD_3 = 45
		case org.apache.bcel.Constants.ALOAD_3:
			wcet = 1;
			break;
		// IALOAD = 46
		case org.apache.bcel.Constants.IALOAD:
			wcet = 6 + 3*r;			
			if (cmp==true)
				wcet = xaload.wcet;
			break;
			
		// LALOAD = 47
		case org.apache.bcel.Constants.LALOAD:
			if(cmp==false){
				wcet = 43+4*r;}
			else{
				wcet = -1;} // not yet implemented
			break;
		// FALOAD = 48
		case org.apache.bcel.Constants.FALOAD:
			wcet = 6 + 3*r;			
			if (cmp==true)
				wcet = xaload.wcet;
			break;
		// DALOAD = 49
		case org.apache.bcel.Constants.DALOAD:
			if(cmp==false){
				wcet = 43+4*r;}
			else{
				wcet = -1;} // not yet implemented
			break;
		// AALOAD = 50
		case org.apache.bcel.Constants.AALOAD:
			wcet = 6 + 3*r;			
			if (cmp==true)
				wcet = xaload.wcet;
			break;
		// BALOAD = 51
		case org.apache.bcel.Constants.BALOAD:
			wcet = 6 + 3*r;			
			if (cmp==true)
				wcet = xaload.wcet;
			break;
		// CALOAD = 52
		case org.apache.bcel.Constants.CALOAD:
			wcet = 6 + 3*r;			
			if (cmp==true)
				wcet = xaload.wcet;
			break;
		// SALOAD = 53
		case org.apache.bcel.Constants.SALOAD:
			wcet = 6 + 3*r;			
			if (cmp==true)
				wcet = xaload.wcet;
			break;
		// ISTORE = 54
		case org.apache.bcel.Constants.ISTORE:
			wcet = 2;
			break;
		// LSTORE = 55
		case org.apache.bcel.Constants.LSTORE:
			wcet = 11;
			break;
		// FSTORE = 56
		case org.apache.bcel.Constants.FSTORE:
			wcet = 2;
			break;
		// DSTORE = 57
		case org.apache.bcel.Constants.DSTORE:
			wcet = 11;
			break;
		// ASTORE = 58
		case org.apache.bcel.Constants.ASTORE:
			wcet = 2;
			break;
		// ISTORE_0 = 59
		case org.apache.bcel.Constants.ISTORE_0:
			wcet = 1;
			break;
		// ISTORE_1 = 60
		case org.apache.bcel.Constants.ISTORE_1:
			wcet = 1;
			break;
		// ISTORE_2 = 61
		case org.apache.bcel.Constants.ISTORE_2:
			wcet = 1;
			break;
		// ISTORE_3 = 62
		case org.apache.bcel.Constants.ISTORE_3:
			wcet = 1;
			break;
		// LSTORE_0 = 63
		case org.apache.bcel.Constants.LSTORE_0:
			wcet = 2;
			break;
		// LSTORE_1 = 64
		case org.apache.bcel.Constants.LSTORE_1:
			wcet = 2;
			break;
		// LSTORE_2 = 65
		case org.apache.bcel.Constants.LSTORE_2:
			wcet = 2;
			break;
		// LSTORE_3 = 66
		case org.apache.bcel.Constants.LSTORE_3:
			wcet = 11;
			break;
		// FSTORE_0 = 67
		case org.apache.bcel.Constants.FSTORE_0:
			wcet = 1;
			break;
		// FSTORE_1 = 68
		case org.apache.bcel.Constants.FSTORE_1:
			wcet = 1;
			break;
		// FSTORE_2 = 69
		case org.apache.bcel.Constants.FSTORE_2:
			wcet = 1;
			break;
		// FSTORE_3 = 70
		case org.apache.bcel.Constants.FSTORE_3:
			wcet = 1;
			break;
		// DSTORE_0 = 71
		case org.apache.bcel.Constants.DSTORE_0:
			wcet = 2;
			break;
		// DSTORE_1 = 72
		case org.apache.bcel.Constants.DSTORE_1:
			wcet = 2;
			break;
		// DSTORE_2 = 73
		case org.apache.bcel.Constants.DSTORE_2:
			wcet = 2;
			break;
		// DSTORE_3 = 74
		case org.apache.bcel.Constants.DSTORE_3:
			wcet = 11;
			break;
		// ASTORE_0 = 75
		case org.apache.bcel.Constants.ASTORE_0:
			wcet = 1;
			break;
		// ASTORE_1 = 76
		case org.apache.bcel.Constants.ASTORE_1:
			wcet = 1;
			break;
		// ASTORE_2 = 77
		case org.apache.bcel.Constants.ASTORE_2:
			wcet = 1;
			break;
		// ASTORE_3 = 78
		case org.apache.bcel.Constants.ASTORE_3:
			wcet = 1;
			break;
		// IASTORE = 79
		case org.apache.bcel.Constants.IASTORE:
			wcet = 10+2*r+w;
			if (cmp==true)
				wcet = xastore.wcet;
			break;
		// LASTORE = 80
		case org.apache.bcel.Constants.LASTORE:
			if(cmp==false){
				wcet = 48+2*r+w;
				if (w > 3) {
					wcet += w - 3;}
			}
			else{
				wcet = -1;} // not yet implemented
			break;
		// FASTORE = 81
		case org.apache.bcel.Constants.FASTORE:
			wcet = 10+2*r+w;
			if (cmp==true)
				wcet = xastore.wcet;
			break;
		// DASTORE = 82
		case org.apache.bcel.Constants.DASTORE:
			if(cmp==false){
				wcet = 48+2*r+w;
				if (w > 3) {
					wcet += w - 3;}
			}
			else{
				wcet = -1;} // not yet implemented
			break;
		// AASTORE = 83
		case org.apache.bcel.Constants.AASTORE:
			// wcet = 10+2*r+w;
			// now with write barrier
			wcet = -1;
			break;
		// BASTORE = 84
		case org.apache.bcel.Constants.BASTORE:
			wcet = 10+2*r+w;
			if (cmp==true)
				wcet = xastore.wcet;
			break;
		// CASTORE = 85
		case org.apache.bcel.Constants.CASTORE:
			wcet = 10+2*r+w;
			if (cmp==true)
				wcet = xastore.wcet;
			break;
		// SASTORE = 86
		case org.apache.bcel.Constants.SASTORE:
			wcet = 10+2*r+w;
			if (cmp==true)
				wcet = xastore.wcet;
			break;
		// POP = 87
		case org.apache.bcel.Constants.POP:
			wcet = 1;
			break;
		// POP2 = 88
		case org.apache.bcel.Constants.POP2:
			wcet = 2;
			break;
		// DUP = 89
		case org.apache.bcel.Constants.DUP:
			wcet = 1;
			break;
		// DUP_X1 = 90
		case org.apache.bcel.Constants.DUP_X1:
			wcet = 5;
			break;
		// DUP_X2 = 91
		case org.apache.bcel.Constants.DUP_X2:
			wcet = 7;
			break;
		// DUP2 = 92
		case org.apache.bcel.Constants.DUP2:
			wcet = 6;
			break;
		// DUP2_X1 = 93
		case org.apache.bcel.Constants.DUP2_X1:
			wcet = 8;
			break;
		// DUP2_X2 = 94
		case org.apache.bcel.Constants.DUP2_X2:
			wcet = 10;
			break;
		// SWAP = 95
		case org.apache.bcel.Constants.SWAP:
			wcet = 4;
			break;
		// IADD = 96
		case org.apache.bcel.Constants.IADD:
			wcet = 1;
			break;
		// LADD = 97
		case org.apache.bcel.Constants.LADD:
			wcet = 26;
			break;
		// FADD = 98
		case org.apache.bcel.Constants.FADD:
			wcet = -1;
			break;
		// DADD = 99
		case org.apache.bcel.Constants.DADD:
			wcet = -1;
			break;
		// ISUB = 100
		case org.apache.bcel.Constants.ISUB:
			wcet = 1;
			break;
		// LSUB = 101
		case org.apache.bcel.Constants.LSUB:
			wcet = 38;
			break;
		// FSUB = 102
		case org.apache.bcel.Constants.FSUB:
			wcet = -1;
			break;
		// DSUB = 103
		case org.apache.bcel.Constants.DSUB:
			wcet = -1;
			break;
		// IMUL = 104
		case org.apache.bcel.Constants.IMUL:
			wcet = 19;
			break;
		// LMUL = 105
		case org.apache.bcel.Constants.LMUL:
			wcet = -1;
			break;
		// FMUL = 106
		case org.apache.bcel.Constants.FMUL:
			wcet = -1;
			break;
		// DMUL = 107
		case org.apache.bcel.Constants.DMUL:
			wcet = -1;
			break;
		// IDIV = 108
		case org.apache.bcel.Constants.IDIV:
			wcet = -1;
			break;
		// LDIV = 109
		case org.apache.bcel.Constants.LDIV:
			wcet = -1;
			break;
		// FDIV = 110
		case org.apache.bcel.Constants.FDIV:
			wcet = -1;
			break;
		// DDIV = 111
		case org.apache.bcel.Constants.DDIV:
			wcet = -1;
			break;
		// IREM = 112
		case org.apache.bcel.Constants.IREM:
			wcet = -1;
			break;
		// LREM = 113
		case org.apache.bcel.Constants.LREM:
			wcet = -1;
			break;
		// FREM = 114
		case org.apache.bcel.Constants.FREM:
			wcet = -1;
			break;
		// DREM = 115
		case org.apache.bcel.Constants.DREM:
			wcet = -1;
			break;
		// INEG = 116
		case org.apache.bcel.Constants.INEG:
			wcet = 4;
			break;
		// LNEG = 117
		case org.apache.bcel.Constants.LNEG:
			wcet = 34;
			break;
		// FNEG = 118
		case org.apache.bcel.Constants.FNEG:
			wcet = -1;
			break;
		// DNEG = 119
		case org.apache.bcel.Constants.DNEG:
			wcet = -1;
			break;
		// ISHL = 120
		case org.apache.bcel.Constants.ISHL:
			wcet = 1;
			break;
		// LSHL = 121
		case org.apache.bcel.Constants.LSHL:
			wcet = 28;
			break;
		// ISHR = 122
		case org.apache.bcel.Constants.ISHR:
			wcet = 1;
			break;
		// LSHR = 123
		case org.apache.bcel.Constants.LSHR:
			wcet = 28;
			break;
		// IUSHR = 124
		case org.apache.bcel.Constants.IUSHR:
			wcet = 1;
			break;
		// LUSHR = 125
		case org.apache.bcel.Constants.LUSHR:
			wcet = 28;
			break;
		// IAND = 126
		case org.apache.bcel.Constants.IAND:
			wcet = 1;
			break;
		// LAND = 127
		case org.apache.bcel.Constants.LAND:
			wcet = 8;
			break;
		// IOR = 128
		case org.apache.bcel.Constants.IOR:
			wcet = 1;
			break;
		// LOR = 129
		case org.apache.bcel.Constants.LOR:
			wcet = 8;
			break;
		// IXOR = 130
		case org.apache.bcel.Constants.IXOR:
			wcet = 1;
			break;
		// LXOR = 131
		case org.apache.bcel.Constants.LXOR:
			wcet = 8;
			break;
		// IINC = 132
		case org.apache.bcel.Constants.IINC:
			wcet = 8;
			break;
		// I2L = 133
		case org.apache.bcel.Constants.I2L:
			wcet = 5;
			break;
		// I2F = 134
		case org.apache.bcel.Constants.I2F:
			wcet = -1;
			break;
		// I2D = 135
		case org.apache.bcel.Constants.I2D:
			wcet = -1;
			break;
		// L2I = 136
		case org.apache.bcel.Constants.L2I:
			wcet = 3;
			break;
		// L2F = 137
		case org.apache.bcel.Constants.L2F:
			wcet = -1;
			break;
		// L2D = 138
		case org.apache.bcel.Constants.L2D:
			wcet = -1;
			break;
		// F2I = 139
		case org.apache.bcel.Constants.F2I:
			wcet = -1;
			break;
		// F2L = 140
		case org.apache.bcel.Constants.F2L:
			wcet = -1;
			break;
		// F2D = 141
		case org.apache.bcel.Constants.F2D:
			wcet = -1;
			break;
		// D2I = 142
		case org.apache.bcel.Constants.D2I:
			wcet = -1;
			break;
		// D2L = 143
		case org.apache.bcel.Constants.D2L:
			wcet = -1;
			break;
		// D2F = 144
		case org.apache.bcel.Constants.D2F:
			wcet = -1;
			break;
		// I2B = 145
		case org.apache.bcel.Constants.I2B:
			wcet = -1;
			break;
		// INT2BYTE = 145 // Old notion
		// case org.apache.bcel.Constants.INT2BYTE : wcet = -1; break;
		// I2C = 146
		case org.apache.bcel.Constants.I2C:
			wcet = 2;
			break;
		// INT2CHAR = 146 // Old notion
		// case org.apache.bcel.Constants.INT2CHAR : wcet = -1; break;
		// I2S = 147
		case org.apache.bcel.Constants.I2S:
			wcet = -1;
			break;
		// INT2SHORT = 147 // Old notion
		// case org.apache.bcel.Constants.INT2SHORT : wcet = -1; break;
		// LCMP = 148
		case org.apache.bcel.Constants.LCMP:
			wcet = 85;
			break;
		// FCMPL = 149
		case org.apache.bcel.Constants.FCMPL:
			wcet = -1;
			break;
		// FCMPG = 150
		case org.apache.bcel.Constants.FCMPG:
			wcet = -1;
			break;
		// DCMPL = 151
		case org.apache.bcel.Constants.DCMPL:
			wcet = -1;
			break;
		// DCMPG = 152
		case org.apache.bcel.Constants.DCMPG:
			wcet = -1;
			break;
		// IFEQ = 153
		case org.apache.bcel.Constants.IFEQ:
			wcet = 4;
			break;
		// IFNE = 154
		case org.apache.bcel.Constants.IFNE:
			wcet = 4;
			break;
		// IFLT = 155
		case org.apache.bcel.Constants.IFLT:
			wcet = 4;
			break;
		// IFGE = 156
		case org.apache.bcel.Constants.IFGE:
			wcet = 4;
			break;
		// IFGT = 157
		case org.apache.bcel.Constants.IFGT:
			wcet = 4;
			break;
		// IFLE = 158
		case org.apache.bcel.Constants.IFLE:
			wcet = 4;
			break;
		// IF_ICMPEQ = 159
		case org.apache.bcel.Constants.IF_ICMPEQ:
			wcet = 4;
			break;
		// IF_ICMPNE = 160
		case org.apache.bcel.Constants.IF_ICMPNE:
			wcet = 4;
			break;
		// IF_ICMPLT = 161
		case org.apache.bcel.Constants.IF_ICMPLT:
			wcet = 4;
			break;
		// IF_ICMPGE = 162
		case org.apache.bcel.Constants.IF_ICMPGE:
			wcet = 4;
			break;
		// IF_ICMPGT = 163
		case org.apache.bcel.Constants.IF_ICMPGT:
			wcet = 4;
			break;
		// IF_ICMPLE = 164
		case org.apache.bcel.Constants.IF_ICMPLE:
			wcet = 4;
			break;
		// IF_ACMPEQ = 165
		case org.apache.bcel.Constants.IF_ACMPEQ:
			wcet = 4;
			break;
		// IF_ACMPNE = 166
		case org.apache.bcel.Constants.IF_ACMPNE:
			wcet = 4;
			break;
		// GOTO = 167
		case org.apache.bcel.Constants.GOTO:
			wcet = 4;
			break;
		// JSR = 168
		case org.apache.bcel.Constants.JSR:
			wcet = -1;
			break;
		// RET = 169
		case org.apache.bcel.Constants.RET:
			wcet = -1; // TODO: Should this be 1?
			break;
		// TABLESWITCH = 170
		case org.apache.bcel.Constants.TABLESWITCH:
			wcet = -1;
			break;
		// LOOKUPSWITCH = 171
		case org.apache.bcel.Constants.LOOKUPSWITCH:
			wcet = -1;
			break;
		// IRETURN = 172
		case org.apache.bcel.Constants.IRETURN:
			wcet = 23;
			if (r > 3) {
				wcet += r - 3;
			}
			if (loadTime > IRETURN_HIDDEN_LOAD_CYCLES) {
				wcet += loadTime - IRETURN_HIDDEN_LOAD_CYCLES;
			}
			
			if(cmp==true){
				WCETMemInstruction ireturn = new WCETMemInstruction();
				ireturn.microcode = new int [wcet];
				ireturn.opcode = 172;
				generateInstruction(ireturn, pmiss, n, loadTime);				
				wcet = wcetOfInstruction(ireturn.microcode);
			}
			break;
		// LRETURN = 173
		case org.apache.bcel.Constants.LRETURN:
			wcet = 25;
			if (r > 3) {
				wcet += r - 3;
			}
			if (loadTime > LRETURN_HIDDEN_LOAD_CYCLES) {
				wcet += loadTime - LRETURN_HIDDEN_LOAD_CYCLES;
			}
			if(cmp==true){
				wcet = -1;}
			break;
		// FRETURN = 174
		case org.apache.bcel.Constants.FRETURN:
			wcet = 23;
			if (r > 3) {
				wcet += r - 3;
			}
			if (loadTime > FRETURN_HIDDEN_LOAD_CYCLES) {
				wcet += loadTime - FRETURN_HIDDEN_LOAD_CYCLES;
			}
			if(cmp==true){
				WCETMemInstruction freturn = new WCETMemInstruction();
				freturn.microcode = new int [wcet];
				freturn.opcode = 174;
				generateInstruction(freturn, pmiss, n, loadTime);
				wcet = wcetOfInstruction(freturn.microcode);}
			break;
		// DRETURN = 175
		case org.apache.bcel.Constants.DRETURN:
			wcet = 25;
			if (r > 3) {
				wcet += r - 3;
			}
			if (loadTime > DRETURN_HIDDEN_LOAD_CYCLES) {
				wcet += loadTime - DRETURN_HIDDEN_LOAD_CYCLES;
			}
			if(cmp==true){
				wcet = -1;}
			break;
		// ARETURN = 176
		case org.apache.bcel.Constants.ARETURN:
			wcet = 23;
			if (r > 3) {
				wcet += r - 3;
			}
			if (loadTime > ARETURN_HIDDEN_LOAD_CYCLES) {
				wcet += loadTime - ARETURN_HIDDEN_LOAD_CYCLES;
			}
			if(cmp==true){
				WCETMemInstruction areturn = new WCETMemInstruction();
				areturn.microcode = new int [wcet];
				areturn.opcode = 176;
				generateInstruction(areturn, pmiss, n, loadTime);
				wcet = wcetOfInstruction(areturn.microcode);}
			break;
		// RETURN = 177
		case org.apache.bcel.Constants.RETURN:
			wcet = 21;
			if (r > 3) {
				wcet += r - 3;
			}
			if (loadTime > RETURN_HIDDEN_LOAD_CYCLES) {
				wcet += loadTime - RETURN_HIDDEN_LOAD_CYCLES;
			}
			
			if(cmp==true){
				WCETMemInstruction returnx = new WCETMemInstruction();
				returnx.microcode = new int [wcet];
				returnx.opcode = 177;
				generateInstruction(returnx, pmiss, n, loadTime);		
				wcet = wcetOfInstruction(returnx.microcode);
			}
			break;
		// GETSTATIC = 178
		case org.apache.bcel.Constants.GETSTATIC:
			wcet = 5 + r;
			if (cmp==true)
				wcet = getstaticx.wcet; 
			break;
		// PUTSTATIC = 179
		case org.apache.bcel.Constants.PUTSTATIC:
			wcet = 5 + w;
			if (cmp==true)
				wcet = putstatic.wcet;
			break;
		// GETFIELD = 180
		case org.apache.bcel.Constants.GETFIELD:
			wcet = 8 + 2 * r;
			if (cmp==true)
				wcet = getfield.wcet;
			break;
		// PUTFIELD = 181
		case org.apache.bcel.Constants.PUTFIELD:
			wcet = 9 + r + w;
			if (cmp==true)
				wcet = putfield.wcet;
			break;
		// INVOKEVIRTUAL = 182
		case org.apache.bcel.Constants.INVOKEVIRTUAL:
			wcet = 98 + 2 * r;
			if (r > 3) {
				wcet += r - 3;
			}
			if (r > 2) {
				wcet += r - 2;
			}
			if (loadTime > INVOKE_HIDDEN_LOAD_CYCLES) {
				wcet += loadTime - INVOKE_HIDDEN_LOAD_CYCLES;
			}
			if(cmp==true){
				WCETMemInstruction invokevirtual = new WCETMemInstruction();
				invokevirtual.microcode = new int [wcet];
				invokevirtual.opcode = 182;
				generateInstruction(invokevirtual, pmiss, n, loadTime);
				//for(int i=0; i<invokevirtual.microcode.length; i++)
				//	System.out.println("Invokevirtual["+i+"] = "+invokevirtual.microcode[i]);
				wcet = wcetOfInstruction(invokevirtual.microcode);}
			break;
		// INVOKESPECIAL = 183
		case org.apache.bcel.Constants.INVOKESPECIAL:
			wcet = 73 + r;
			if (r > 3) {
				wcet += r - 3;
			}
			if (r > 2) {
				wcet += r - 2;
			}
			if (loadTime > INVOKE_HIDDEN_LOAD_CYCLES) {
				wcet += loadTime - INVOKE_HIDDEN_LOAD_CYCLES;
			}
			if(cmp==true){
				WCETMemInstruction invokespecial = new WCETMemInstruction();
				invokespecial.microcode = new int [wcet];
				invokespecial.opcode = 183;
				generateInstruction(invokespecial, pmiss, n, loadTime);
				wcet = wcetOfInstruction(invokespecial.microcode);}
			break;
		// INVOKENONVIRTUAL = 183
		// case org.apache.bcel.Constants.INVOKENONVIRTUAL : wcet = -1; break;
		// INVOKESTATIC = 184
		case org.apache.bcel.Constants.INVOKESTATIC:
			wcet = 73 + r;
			if (r > 3) {
				wcet += r - 3;
			}
			if (r > 2) {
				wcet += r - 2;
			}
			if (loadTime > INVOKE_HIDDEN_LOAD_CYCLES) {
				wcet += loadTime - INVOKE_HIDDEN_LOAD_CYCLES;
			}
			if(cmp==true){
				WCETMemInstruction invokestatic = new WCETMemInstruction();
				invokestatic.microcode = new int [wcet];
				invokestatic.opcode = 184;
				generateInstruction(invokestatic, pmiss, n, loadTime);
				wcet = wcetOfInstruction(invokestatic.microcode);}
			break;
		// INVOKEINTERFACE = 185
		case org.apache.bcel.Constants.INVOKEINTERFACE:
			wcet = 111 + 4 * r;
			if (r > 3) {
				wcet += r - 3;
			}
			if (r > 2) {
				wcet += r - 2;
			}
			if (loadTime > INVOKE_HIDDEN_LOAD_CYCLES) {
				wcet += loadTime - INVOKE_HIDDEN_LOAD_CYCLES;
			}
			if(cmp==true){
				wcet = -1;}
			break;
		// NEW = 187
		case org.apache.bcel.Constants.NEW:
			wcet = -1;
			break;
		// NEWARRAY = 188
		case org.apache.bcel.Constants.NEWARRAY:
			wcet = -1;
			break;
		// ANEWARRAY = 189
		case org.apache.bcel.Constants.ANEWARRAY:
			wcet = -1;
			break;
		// ARRAYLENGTH = 190
		case org.apache.bcel.Constants.ARRAYLENGTH:
			wcet = 6 + r;
			if (cmp==true)
				wcet = arraylength.wcet;
			break;
		// ATHROW = 191
		case org.apache.bcel.Constants.ATHROW:
			wcet = -1;
			break;
		// CHECKCAST = 192
		case org.apache.bcel.Constants.CHECKCAST:
			wcet = -1;
			break;
		// INSTANCEOF = 193
		case org.apache.bcel.Constants.INSTANCEOF:
			wcet = -1;
			break;
		// MONITORENTER = 194
		case org.apache.bcel.Constants.MONITORENTER:
			wcet = 19;
			break;
		// MONITOREXIT = 195
		case org.apache.bcel.Constants.MONITOREXIT:
			wcet = 20;
			break;
		// WIDE = 196
		case org.apache.bcel.Constants.WIDE:
			wcet = -1;
			break;
		// MULTIANEWARRAY = 197
		case org.apache.bcel.Constants.MULTIANEWARRAY:
			wcet = -1;
			break;
		// IFNULL = 198
		case org.apache.bcel.Constants.IFNULL:
			wcet = 4;
			break;
		// IFNONNULL = 199
		case org.apache.bcel.Constants.IFNONNULL:
			wcet = 4;
			break;
		// GOTO_W = 200
		case org.apache.bcel.Constants.GOTO_W:
			wcet = -1;
			break;
		// JSR_W = 201
		case org.apache.bcel.Constants.JSR_W:
			wcet = -1;
			break;
		// JOPSYS_RD = 209   
		case JOPSYS_RD:
			wcet = 4 + r;
			if (cmp==true)
				wcet = jopsys_rdx.wcet;
			break;
		// JOPSYS_WR = 210
		case JOPSYS_WR:
			wcet = 5 + w;
			if (cmp==true)
				wcet = jopsys_wrx.wcet;
			break;
		// JOPSYS_RDMEM = 211
		case JOPSYS_RDMEM:
			wcet = 4 + r;
			if (cmp==true)
				wcet = jopsys_rdx.wcet;
			break;
		// JOPSYS_WRMEM = 212
		case JOPSYS_WRMEM:
			wcet = 5 + w;
			if (cmp==true)
				wcet = jopsys_wrx.wcet;
			break;
		// JOPSYS_RDINT = 213
		case JOPSYS_RDINT:
			wcet = 3;
			break;
		// JOPSYS_WRINT = 214
		case JOPSYS_WRINT:
			wcet = 3;
			break;
		// JOPSYS_GETSP = 215
		case JOPSYS_GETSP:
			wcet = 3;
			break;
		// JOPSYS_SETSP = 216
		case JOPSYS_SETSP:
			wcet = 4;
			break;
		// JOPSYS_GETVP = 217
		case JOPSYS_GETVP:
			wcet = 1;
			break;
		// JOPSYS_SETVP = 218
		case JOPSYS_SETVP:
			wcet = 2;
			break;
		// JOPSYS_INT2EXT = 219
		case JOPSYS_INT2EXT:
			if(cmp==false){
				int wt = 0;
				if (w>8) wt = w-8;
				wcet = 14+r+ n*(23+wt);}
			else{
				wcet = -1;} // not yet implemented
			break;
		// JOPSYS_EXT2INT = 220
		case JOPSYS_EXT2INT:
			if(cmp==false){
				int rt = 0;
				if (r>10) rt = r-10;
				wcet = 14+r+ n*(23+rt);}
			else{
				wcet = -1;} // not yet implemented
			break;
		// JOPSYS_NOP = 221
		case JOPSYS_NOP:
			wcet = 1;
			break;
			
		case 223: // conditional move 
			wcet = 5;
			break;

		// GETSTATIC_REF = 224
		case GETSTATIC_REF:
			wcet = 5 + r;
			if (cmp==true)
				wcet = getstaticx.wcet;
			break;
			
		// GETFIELD_REF = 226
		case GETFIELD_REF:
			wcet = 8 + 2 * r;
			if (cmp==true){
				WCETMemInstruction getfield_ref = new WCETMemInstruction();
				getfield_ref.microcode = new int [wcet];
				getfield_ref.opcode = 226;
				generateInstruction(getfield_ref, pmiss, n, loadTime);
				wcet = wcetOfInstruction(getfield_ref.microcode);
			}
			break;
			
		// GETSTATIC_LONG = 228
		case GETSTATIC_LONG:
			wcet = 16 + r;
			if (r > 3) {
				wcet += r - 3;
			}			
			if (cmp==true)
				wcet = -1;
			break;
		// PUTSTATIC_LONG = 229
		case PUTSTATIC_LONG:
			wcet = 17 + w;
			if (w > 2) {
				wcet += w - 2; // bh, 28.9.11: s/w-1/w-2/ according to micropath analysis
			}			
			if (cmp==true)
				wcet = -1;
			break;

		// GETFIELD_LONG = 230
		case GETFIELD_LONG:
			wcet = 26 + 2*r;
			if (r > 3) {
				wcet += r - 3;
			}			
			if (cmp==true)
				wcet = -1;
			break;
		// PUTFIELD_LONG = 231
		case PUTFIELD_LONG:
			wcet = 30 + r + w;
			if (w > 1) {
				wcet += w - 1;
			}			
			if (cmp==true)
				wcet = -1;
			break;

		// JOPSYS_GETFIELD = 233
		case JOPSYS_GETFIELD:
			// FIXME: perhaps it is 9 + 2r?
			// But MS should check in the HW!
			wcet = 9 + 2*r;
			if (cmp==true)
				wcet = -1;
			break;

		// JOPSYS_PUTFIELD = 234
		case JOPSYS_PUTFIELD:
			wcet = 12 + r + w;
			if (cmp==true)
				wcet = -1;
			break;

		// JOPSYS_GETSTATIC = 238
		case JOPSYS_GETSTATIC:
			wcet = 6 + r;
			if (cmp==true)
				wcet = -1;
			break;

		// JOPSYS_PUTSTATIC = 239
		case JOPSYS_PUTSTATIC:
			wcet = 6 + w;
			if (cmp==true)
				wcet = -1;
			break;

		// JOPSYS_MEMCPY = 232
		case JOPSYS_MEMCPY:
			wcet = -1;
			break;
		
		// JOPSYS_INVAL = 204
		case JOPSYS_INVAL:
			wcet = 4;
			break;

		default:
			wcet = -1;
		}
		// TODO: Add the JOP specific codes?
		if (JopInstr.isInJava(opcode)) {
			return -1;
		}
		return wcet;
	}

	/**
	 * Check to see if there is a valid WCET count for the instruction.
	 * 
	 * @param opcode
	 * @return true if there is a valid wcet value
	 */
	public boolean wcetAvailable(int opcode) {
		if (getCycles(opcode, false, 0) == WCETNOTAVAILABLE)
			return false;
		else
			return true;
	}

	/**
	 * Get an estimation of the bytecode execution time.
	 *
	 * TODO: measure Java implemented bytecodes and add the numbers.
	 * @param opcode
	 * @param pmiss
	 * @param n
	 * @return
	 */
	public int getCyclesEstimate(int opcode, boolean pmiss, int n) {

		int ret = getCycles(opcode, pmiss, n);
		// VERY rough estimate
		if (ret==WCETNOTAVAILABLE) {
			ret = 200;
		}
		return ret;
	}

	public  int getNoImplDispatchCycles() {
		// FIXME (CMP_WCET): invokevirtual should be a conservative approximation to sys_noim for now
		if(cmp) return getCycles(org.apache.bcel.Constants.INVOKEVIRTUAL,false,0);
		else return 85 + Math.max(0,r-3) + Math.max(0,r-2);
	}

	/**
	 * Method load time on invoke or return if there is a cache miss (see pMiss).
	 * 
	 * @see ms thesis p 232
	 */
	public int calculateB(boolean hit, int n) {
		int b = -1;
		if (n == -1) {
			System.err.println("n not set!");
			System.exit(-1);
		} else {
			if (hit) {
				b = 4;
			} else {
				b = 6 + (n+1) * (2+c());
			}
		}
		return b;
	}
	
	// Initializes a couple of periods, where one timeslot is true and the
	// other ones are false
	
	public void initArbiter(){
		
		int i;
		int arb_period = getArbiterPeriod();
		arbiter = new boolean [MAX_CYCLES];
		
		for(i=0;i<MAX_CYCLES;i++){
			if( (i % arb_period) < timeslot ){
				arbiter[i]=true;}
			else{
				arbiter[i]=false;}
		}			
	}
	
	// generates all static instruction patterns and WCETs
	public void generateStaticInstr()
	{
		ldc = new WCETMemInstruction();
		ldc_w = new WCETMemInstruction();
		ldc2_w = new WCETMemInstruction();
		xaload = new WCETMemInstruction();
		xastore = new WCETMemInstruction();
		getstaticx = new WCETMemInstruction();
		putstatic = new WCETMemInstruction();
		getfield = new WCETMemInstruction();
		putfield = new WCETMemInstruction();
		arraylength = new WCETMemInstruction();
		jopsys_rdx = new WCETMemInstruction();
		jopsys_wrx = new WCETMemInstruction();
		
		
		ldc.microcode = new int [7+r];
		ldc.opcode = 18;
		generateInstruction(ldc, false, 0, 0); // Cache is not interesting here
		ldc.wcet = wcetOfInstruction(ldc.microcode);
		
		ldc_w.microcode = new int [8+r];
		ldc_w.opcode = 19;
		generateInstruction(ldc_w, false, 0, 0);
		ldc_w.wcet = wcetOfInstruction(ldc_w.microcode);
		
		int wcet = 17;
		if (r > 2) {
			wcet += r - 2;
		}
		if (r > 1) {
			wcet += r - 1;
		}
		ldc2_w.microcode = new int [wcet];
		ldc2_w.opcode = 20;
		generateInstruction(ldc2_w, false, 0, 0);
		ldc2_w.wcet = wcetOfInstruction(ldc2_w.microcode);
		
		xaload.microcode = new int [7+3*r];
		xaload.opcode = 46;
		generateInstruction(xaload, false, 0, 0);
		xaload.wcet = wcetOfInstruction(xaload.microcode);
		
		xastore.microcode = new int [10+2*r+w];
		xastore.opcode = 79;
		generateInstruction(xastore, false, 0, 0);
		xastore.wcet = wcetOfInstruction(xastore.microcode);
		
		getstaticx.microcode = new int [5+r];
		getstaticx.opcode = 178;
		generateInstruction(getstaticx, false, 0, 0);
		getstaticx.wcet = wcetOfInstruction(getstaticx.microcode);
		
		putstatic.microcode = new int [5+w];
		putstatic.opcode = 179;
		generateInstruction(putstatic, false, 0, 0);
		putstatic.wcet = wcetOfInstruction(putstatic.microcode);
		
		getfield.microcode = new int [8+2*r];
		getfield.opcode = 180;
		generateInstruction(getfield, false, 0, 0);
		getfield.wcet = wcetOfInstruction(getfield.microcode);
		
		putfield.microcode = new int [9+r+w];
		putfield.opcode = 181;
		generateInstruction(putfield, false, 0, 0);
		putfield.wcet = wcetOfInstruction(putfield.microcode);
		
		arraylength.microcode = new int [6+r];
		arraylength.opcode = 190;
		generateInstruction(arraylength, false, 0, 0);
		arraylength.wcet = wcetOfInstruction(arraylength.microcode);
		
		jopsys_rdx.microcode = new int [4+r];
		jopsys_rdx.opcode = 209;
		generateInstruction(jopsys_rdx, false, 0, 0);
		jopsys_rdx.wcet = wcetOfInstruction(jopsys_rdx.microcode);
		
		jopsys_wrx.microcode = new int [5+w];
		jopsys_wrx.opcode = 210;
		generateInstruction(jopsys_wrx, false, 0, 0);
		jopsys_wrx.wcet = wcetOfInstruction(jopsys_wrx.microcode);	
	}
	
	
	// Generates the Instruction patterns
	
	public void generateInstruction(WCETMemInstruction instruction, boolean pmiss, int n, int b){
		
		int cnt = 0;
		int x = 0;
		int y = 0;
		
		switch(instruction.opcode){
		
		
		// Static Instructions
		// ldc
		case 18:
			for(int i=0;i<instruction.microcode.length;i++){
				if(i<=4) instruction.microcode[i]=NOP;
				else if(i==5) instruction.microcode[i]=RD;
				else instruction.microcode[i]=NOP;
			}
			break;
		
		// ldc_w
		case 19:
			for(int i=0;i<instruction.microcode.length;i++){
				if(i<=5) instruction.microcode[i]=NOP;
				else if(i==6) instruction.microcode[i]=RD;
				else instruction.microcode[i]=NOP;
			}
			break; 
			
		// ldc2_w 
		case 20:
			
			if (r <= 2) x = 0;
			else x = r-2; // the 2nd comparison is unnecessary because only only NOPs anyway! 
			
			for(int i=0;i<instruction.microcode.length;i++){
				if(i<=6) instruction.microcode[i]=NOP;
				else if(i==7) instruction.microcode[i]=RD;
				else if(i>=8 && i<=13+x) instruction.microcode[i]=NOP;
				else if(i==14+x) instruction.microcode[i]=RD;
				else instruction.microcode[i]=NOP;
			}
			break;
			
		// xaload = iaload, faload, aaload, baload, caload, saload	
		case 46:
		case 48:
		case 50:
		case 51:
		case 52:
			for(int i=0;i<instruction.microcode.length;i++){
				if(i<=2) instruction.microcode[i]=NOP;
				else if(i==3) instruction.microcode[i]=RD;
				else if(i>3 && i<=3+r) instruction.microcode[i]=NOP;
				else if(i==4+r) instruction.microcode[i]=RD;
				else if(i>4+r && i<=4+2*r) instruction.microcode[i]=NOP;
				else if(i==5+2*r) instruction.microcode[i]=RD;
				else instruction.microcode[i]=NOP;
			}
			break;
			
		// xastore = iastore, fastore, bastore, castore, sastore
		case 79:
		case 81:
		case 84:
		case 85:
		case 86:
			for(int i=0;i<instruction.microcode.length;i++){
				if(i<=3) instruction.microcode[i]=NOP;
				else if(i==4) instruction.microcode[i]=RD;
				else if(i>4 && i<=4+r) instruction.microcode[i]=NOP;
				else if(i==5+r) instruction.microcode[i]=RD;
				else if(i>5+r && i<=7+2*r) instruction.microcode[i]=NOP;
				else if(i==8+2*r) instruction.microcode[i]=WR;
				else instruction.microcode[i]=NOP;
			}
			break;
			
		// getstaticx = getstatic, getstatic_ref
		case 178:
		case 224:
			for(int i=0;i<instruction.microcode.length;i++){
				if(i<=2) instruction.microcode[i]=NOP;
				else if(i==3) instruction.microcode[i]=RD;
				else instruction.microcode[i]=NOP;
			}
			break;
			
		// putstatic
		case 179:
			// TODO: is this pattern correct, but it does not really matter
			// for single bytecode WCET values
			for(int i=0;i<instruction.microcode.length;i++){
				if(i<=2) instruction.microcode[i]=NOP;
				else if(i==3) instruction.microcode[i]=WR;
				else instruction.microcode[i]=NOP;
			}
			break;
			
		// getfield
		case 180:
			for(int i=0;i<instruction.microcode.length;i++){
				if(i<=2) instruction.microcode[i]=NOP;
				else if(i==3) instruction.microcode[i]=RD;
				else if(i>3 && i<=5+r) instruction.microcode[i]=NOP;
				else if(i==6+r) instruction.microcode[i]=RD;
				else instruction.microcode[i]=NOP;
			}
			break;
			
		// putfield	
		case 181:
			for(int i=0;i<instruction.microcode.length;i++){
				if(i<=3) instruction.microcode[i]=NOP;
				else if(i==4) instruction.microcode[i]=RD;
				else if(i>4 && i<=6+r) instruction.microcode[i]=NOP;
				else if(i==7+r) instruction.microcode[i]=WR;
				else instruction.microcode[i]=NOP;
			}
			break;
		
		// arraylength
		case 190:
			for(int i=0;i<instruction.microcode.length;i++){
				if(i<=3) instruction.microcode[i]=NOP;
				else if(i==4) instruction.microcode[i]=RD;
				else instruction.microcode[i]=NOP;
			}
			break;
			
		// jopsys_rdx = jopsys_rd, jopsys_rdmem
		case 209:
		case 211:
			for(int i=0;i<instruction.microcode.length;i++){
				if(i<=1) instruction.microcode[i]=NOP;
				else if(i==2) instruction.microcode[i]=RD;
				else instruction.microcode[i]=NOP;
			}
			break;
			
		// jopsys_wrx = jopsys_wr, jopsys_wrmem
		case 210:
		case 212:
			for(int i=0;i<instruction.microcode.length;i++){
				if(i<=2) instruction.microcode[i]=NOP;
				else if(i==3) instruction.microcode[i]=WR;
				else instruction.microcode[i]=NOP;
			}
			break;

			
		// Dynamic Instructions	
			
		// ireturn, freturn, areturn
		case 172:
		case 174:
		case 176:

			if (r <= 3) x = 0;
			else x = r-3;
			
			for(int i=0;i<instruction.microcode.length;i++){
				if(i<=3) instruction.microcode[i]=NOP;
				else if(i==4) instruction.microcode[i]=RD;
				else if(i>=5 && i<=15+x) instruction.microcode[i]=NOP;
				else{
					if (pmiss == false || n==0)
						instruction.microcode[i]=NOP;
					else{
						if (cnt<(2+c())*(n+1)){ 
							if (cnt % (r+1) == 0){
								instruction.microcode[i]=RD;
								cnt++;}
							else{
								instruction.microcode[i]=NOP;
								cnt++;}
						}
						else
							instruction.microcode[i]=NOP;
					}
				}
			}				
			break;
			
		
		// lreturn	
		case 173:
			break;
			
		// dreturn	
		case 175:
			break;
			
		// return	
		case 177:
			
			if (r <= 3) x = 0;
			else x = r-3;
		
			for(int i=0;i<instruction.microcode.length;i++){
				if(i<=2) instruction.microcode[i]=NOP;
				else if(i==3) instruction.microcode[i]=RD;
				else if(i>=4 && i<=14+x) instruction.microcode[i]=NOP;
				else{
					if (pmiss == false || n==0)
						instruction.microcode[i]=NOP;
					else{
						if (cnt<(2+c())*(n+1)){ 
							if (cnt % (r+1) == 0){
								instruction.microcode[i]=RD;
								cnt++;}
							else{
								instruction.microcode[i]=NOP;
								cnt++;}
						}
						else
							instruction.microcode[i]=NOP;
					}
				}
			}
			break;
			
		// invokevirtual	
		case 182:
			
			if (r <= 3) x = 0;
			else x = r-3;
			if (r <= 2) y = 0;
			else y = r-2;
			
			for(int i=0;i<instruction.microcode.length;i++){
				if(i<=5) instruction.microcode[i]=NOP;
				else if(i==6) instruction.microcode[i]=RD;
				else if(i>=7 && i<=31+r) instruction.microcode[i]=NOP;
				else if(i==32+r) instruction.microcode[i]=RD;
				else if(i>=33+r && i<=40+2*r) instruction.microcode[i]=NOP;
				else if(i==41+2*r) instruction.microcode[i]=RD;
				else if(i>=42+2*r && i<=52+2*r+x) instruction.microcode[i]=NOP;
				else if(i==53+2*r+x) instruction.microcode[i]=RD;
				else if(i>=54+2*r+x && i<=63+2*r+x+y) instruction.microcode[i]=NOP;
				else{
					if (pmiss == false || n==0)
						instruction.microcode[i]=NOP;
					else{
						if (pmiss == false)
							instruction.microcode[i]=NOP;
						else{
							if (cnt<(2+c())*(n+1)){ 
								if (cnt % (r+1) == 0){
									instruction.microcode[i]=RD;
									cnt++;}
								else{
									instruction.microcode[i]=NOP;
									cnt++;}
							}
							else
								instruction.microcode[i]=NOP;
						}
					}
				}	
			}
			break;
			
		// invokespecial, invokestatic
		case 183:
		case 184:

			if (r <= 3) x = 0;
			else x = r-3;
			if (r <= 2) y = 0;
			else y = r-2;
			
			for(int i=0;i<instruction.microcode.length;i++){
				if(i<=5) instruction.microcode[i]=NOP;
				else if(i==6) instruction.microcode[i]=RD;
				else if(i>=7 && i<=16+r) instruction.microcode[i]=NOP;
				else if(i==17+r) instruction.microcode[i]=RD;
				else if(i>=18+r && i<=28+r+x) instruction.microcode[i]=NOP;
				else if(i==29+r+x) instruction.microcode[i]=RD;
				else if(i>=30+r+x && i<=39+r+x+y) instruction.microcode[i]=NOP;
				else{
					if (pmiss == false || n==0)
						instruction.microcode[i]=NOP;
					else{
						if (cnt<(2+c())*(n+1)){ 
							if (cnt % (r+1) == 0){
								instruction.microcode[i]=RD;
								cnt++;}
							else{
								instruction.microcode[i]=NOP;
								cnt++;}
						}
						else
							instruction.microcode[i]=NOP;
					}
				}
			}				
			break;
			
			
		// invokeinterface	
		case 185:
			
			break;

			
		default:
		}
	}
	
	// calculates WCET of instruction
	public int wcetOfInstruction(int [] microcode){
		
		int i = 0;
		int j = 0;
		int wcet=0;
		int exec = 0;	
		int arb_period = getArbiterPeriod();
		for(i=0;i<arb_period;i++){
			exec = calcExecTime(i,microcode);
			if (wcet<exec){
				wcet = exec;
				j = i;
			}
		}
				
		//System.out.println("WCET: " + wcet + " Arbitration position: " + j);
		return wcet;
	}
	
	// Calculates execution time of instruction, starting at a certain position of the
	// arbitration period
	
	public int calcExecTime(int arb_position,  int [] microcode){	
		int arb_period = getArbiterPeriod();
		int i=0;
		int exec_time=0;
			
		for(i=0;i<microcode.length;i++){
			
			switch(microcode[i])
			{
			case NOP:
				exec_time++;
				arb_position++;
				//System.out.println("NOP: " + exec_time);
				break;
					
			case RD:				
				while( (arbiter[arb_position]==false) || 
					   (((arb_position % arb_period ) >=timeslot-r) && 
					    ((arb_position % arb_period ) <=timeslot)) ){
					exec_time++;
					arb_position++;
					//System.out.println("Blocking RD: " + exec_time);
				}
					
				if(arbiter[arb_position]==true){
					exec_time++;
					arb_position++;
					//System.out.println("RD: " + exec_time);
				}
				break;
					
			case WR:
				while( (arbiter[arb_position]==false) || 
					   (((arb_position % arb_period)>=timeslot-w) && 
					    ((arb_position % arb_period)<=timeslot)) ){
					exec_time++;
					arb_position++;
				}
				
				if(arbiter[arb_position]==true){
					exec_time++;
					arb_position++;
				}
				break;
			}
		}
		
		return exec_time;
	}
	
	/* Useful for cache analysis */
	private static final int _HIDDEN_LOAD_CYCLES[][] = 
	{ { org.apache.bcel.Constants.IRETURN, IRETURN_HIDDEN_LOAD_CYCLES },
	  { org.apache.bcel.Constants.FRETURN, FRETURN_HIDDEN_LOAD_CYCLES },
	  { org.apache.bcel.Constants.LRETURN, LRETURN_HIDDEN_LOAD_CYCLES },
	  { org.apache.bcel.Constants.DRETURN, DRETURN_HIDDEN_LOAD_CYCLES },
	  { org.apache.bcel.Constants.ARETURN, ARETURN_HIDDEN_LOAD_CYCLES },
	  { org.apache.bcel.Constants.RETURN, RETURN_HIDDEN_LOAD_CYCLES },
	  { org.apache.bcel.Constants.INVOKEVIRTUAL, INVOKE_HIDDEN_LOAD_CYCLES },
	  { org.apache.bcel.Constants.INVOKESPECIAL, INVOKE_HIDDEN_LOAD_CYCLES },
	  { org.apache.bcel.Constants.INVOKESTATIC, INVOKE_HIDDEN_LOAD_CYCLES },
	  { org.apache.bcel.Constants.INVOKEINTERFACE, INVOKE_HIDDEN_LOAD_CYCLES },
	};
	/** Hidden load cycles for invoke/return instructions */
	public static final Map<Integer,Integer> HIDDEN_LOAD_CYCLES = asHashMap(_HIDDEN_LOAD_CYCLES);

	private static Map<Integer, Integer> asHashMap(int[][] assocArray) {
		HashMap<Integer, Integer> map = new HashMap<Integer,Integer>();
		for(int[] entry : assocArray) {
			map.put(entry[0], entry[1]);
		}
		return map;
	}

}