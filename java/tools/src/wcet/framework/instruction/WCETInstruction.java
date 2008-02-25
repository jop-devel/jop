package wcet.framework.instruction;

import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;

/**
 * It has wcet info on byte code instruction granlularity. Should we consider
 * making a class that wraps the microcodes into objects?
 */
public class WCETInstruction {
	// indicate that wcet is not available for this bytecode
	public static final int WCETNOTAVAILABLE = -1;

public static final int a = -1; // should be removed from WCETAnalyser!
	// the read and write wait states
	// ram_cnt - 1
	public static final int r = 1;

	public static final int w = 1;
	
	// cache read wait state (r-1)
	public static final int c = 0;

	//Native bytecodes (see jvm.asm)
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

	// TODO: make those missing (the rup/ms speciffic ones, but are they
	// reachable?)

	/**
	 * Same as getWCET, but using the handle.
	 * 
	 * @param ih
	 * @param pmiss true if the cache is missed and false if there is a cache hit
	 * @return wcet or WCETNOTAVAILABLE (-1)
	 */
	static int getCyclesFromHandle(InstructionHandle ih, boolean pmiss, int n) {
		Instruction ins = ih.getInstruction();
		int opcode = ins.getOpcode();

		return getCycles(opcode, pmiss, n);
	}

	/**
	 * Get the name using the opcode. Used when WCA toWCAString().
	 * 
	 * @param opcode
	 * @return name or "ILLEGAL_OPCODE"
	 */
	static String getNameFromOpcode(int opcode) {
		return OPCODE_NAMES[opcode];
	}

	/**
	 * See the WCET values
	 * @return table body of opcodes with info
	 */

	static String toWCAString() {
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
			sb.append(WU.postpad(str, 25));

			//hit n={0,1000}
			String hitstr = getCycles(op, false, 0) + "/"
					+ getCycles(op, false, 1000);
			hitstr = WU.prepad(hitstr, 12);

			//miss n={0,1000}
			String missstr = getCycles(op, true, 0) + "/"
					+ getCycles(op, true, 1000);
			missstr = WU.prepad(missstr, 12);

			sb.append(hitstr + missstr + "\n");
		}
		sb
				.append("=============================================================\n");
		sb.append("Info: b(n=1000)=" + calculateB(false, 1000) + " c=" + c + " r=" + r
				+ " w=" + w + "\n");
		sb
				.append("Signatures: V void, Z boolean, B byte, C char, S short, I int, J long, F float, D double, L class, [ array\n");
		return sb.toString();
	}

	public static void main(String[] args) {
		
		for (int i=0; i<256; ++i) {
			int cnt = getCycles(i, false, 0);
			if (cnt==-1) cnt = 0;
			System.out.println(cnt);
		}
	}
	
	/**
	 * Returns the wcet count for the instruction.
	 * 
	 * @see table D.1 in ms thesis
	 * @param opcode
	 * @param pmiss true if cacle is misses and false if a cache hit
	 * @return wcet cycle count or -1 if wcet not available
	 */
	static int getCycles(int opcode, boolean pmiss, int n) {
		int wcet = 0;
		int b = -1;

		// cache load time
		b = calculateB(!pmiss, n);

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
			wcet = -1;
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
			wcet = -1;
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
			break;
		// LDC_W = 19
		case org.apache.bcel.Constants.LDC_W:
			wcet = 8 + r;
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
			wcet = 7 + 3*r;
			break;
		// LALOAD = 47
		case org.apache.bcel.Constants.LALOAD:
			wcet = 43+4*r;
			break;
		// FALOAD = 48
		case org.apache.bcel.Constants.FALOAD:
			wcet = 7 + 3*r;
			break;
		// DALOAD = 49
		case org.apache.bcel.Constants.DALOAD:
			wcet = -1;
			break;
		// AALOAD = 50
		case org.apache.bcel.Constants.AALOAD:
			wcet = 7 + 3*r;
			break;
		// BALOAD = 51
		case org.apache.bcel.Constants.BALOAD:
			wcet = 7 + 3*r;
			break;
		// CALOAD = 52
		case org.apache.bcel.Constants.CALOAD:
			wcet = 7 + 3*r;
			break;
		// SALOAD = 53
		case org.apache.bcel.Constants.SALOAD:
			wcet = 7 + 3*r;
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
			wcet = 9+2*r+w;
			break;
		// LASTORE = 80
		case org.apache.bcel.Constants.LASTORE:
			wcet = 48+2*r+w;
			if (w > 3) {
				wcet += w - 3;
			}
			break;
		// FASTORE = 81
		case org.apache.bcel.Constants.FASTORE:
			wcet = 9+2*r+w;
			break;
		// DASTORE = 82
		case org.apache.bcel.Constants.DASTORE:
			wcet = -1;
			break;
		// AASTORE = 83
		case org.apache.bcel.Constants.AASTORE:
			wcet = 9+2*r+w;
			break;
		// BASTORE = 84
		case org.apache.bcel.Constants.BASTORE:
			wcet = 9+2*r+w;
			break;
		// CASTORE = 85
		case org.apache.bcel.Constants.CASTORE:
			wcet = 9+2*r+w;
			break;
		// SASTORE = 86
		case org.apache.bcel.Constants.SASTORE:
			wcet = 9+2*r+w;
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
			wcet = -1;
			break;
		// IADD = 96
		case org.apache.bcel.Constants.IADD:
			wcet = 1;
			break;
		// LADD = 97
		case org.apache.bcel.Constants.LADD:
			wcet = -1;
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
			wcet = -1;
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
			wcet = 35;
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
			wcet = -1;
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
			wcet = -1;
			break;
		// ISHR = 122
		case org.apache.bcel.Constants.ISHR:
			wcet = 1;
			break;
		// LSHR = 123
		case org.apache.bcel.Constants.LSHR:
			wcet = -1;
			break;
		// IUSHR = 124
		case org.apache.bcel.Constants.IUSHR:
			wcet = 1;
			break;
		// LUSHR = 125
		case org.apache.bcel.Constants.LUSHR:
			wcet = -1;
			break;
		// IAND = 126
		case org.apache.bcel.Constants.IAND:
			wcet = 1;
			break;
		// LAND = 127
		case org.apache.bcel.Constants.LAND:
			wcet = -1;
			break;
		// IOR = 128
		case org.apache.bcel.Constants.IOR:
			wcet = 1;
			break;
		// LOR = 129
		case org.apache.bcel.Constants.LOR:
			wcet = -1;
			break;
		// IXOR = 130
		case org.apache.bcel.Constants.IXOR:
			wcet = 1;
			break;
		// LXOR = 131
		case org.apache.bcel.Constants.LXOR:
			wcet = -1;
			break;
		// IINC = 132
		case org.apache.bcel.Constants.IINC:
			wcet = 8;
			break;
		// I2L = 133
		case org.apache.bcel.Constants.I2L:
			wcet = -1;
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
			wcet = -1;
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
			if (b > 10) {
				wcet += b - 10;
			}
			break;
		// LRETURN = 173
		case org.apache.bcel.Constants.LRETURN:
			wcet = 25;
			if (r > 3) {
				wcet += r - 3;
			}
			if (b > 11) {
				wcet += b - 11;
			}
			break;
		// FRETURN = 174
		case org.apache.bcel.Constants.FRETURN:
			wcet = 23;
			if (r > 3) {
				wcet += r - 3;
			}
			if (b > 10) {
				wcet += b - 10;
			}
			break;
		// DRETURN = 175
		case org.apache.bcel.Constants.DRETURN:
			wcet = 25;
			if (r > 3) {
				wcet += r - 3;
			}
			if (b > 11) {
				wcet += b - 11;
			}
			break;
		// ARETURN = 176
		case org.apache.bcel.Constants.ARETURN:
			wcet = 23;
			if (r > 3) {
				wcet += r - 3;
			}
			if (b > 10) {
				wcet += b - 10;
			}
			break;
		// RETURN = 177
		case org.apache.bcel.Constants.RETURN:
			wcet = 21;
			if (r > 3) {
				wcet += r - 3;
			}
			if (b > 9) {
				wcet += b - 9;
			}
			break;
		// GETSTATIC = 178
		case org.apache.bcel.Constants.GETSTATIC:
			wcet = 12 + 2 * r;
			break;
		// PUTSTATIC = 179
		case org.apache.bcel.Constants.PUTSTATIC:
			wcet = 13 + r + w;
			break;
		// GETFIELD = 180
		case org.apache.bcel.Constants.GETFIELD:
			wcet = 17 + 2 * r;
			break;
		// PUTFIELD = 181
		case org.apache.bcel.Constants.PUTFIELD:
			wcet = 20 + r + w;
			break;
		// INVOKEVIRTUAL = 182
		case org.apache.bcel.Constants.INVOKEVIRTUAL:
			wcet = 100 + 2 * r;
			if (r > 3) {
				wcet += r - 3;
			}
			if (r > 2) {
				wcet += r - 2;
			}
			if (b > 37) {
				wcet += b - 37;
			}
			break;
		// INVOKESPECIAL = 183
		case org.apache.bcel.Constants.INVOKESPECIAL:
			wcet = 74 + r;
			if (r > 3) {
				wcet += r - 3;
			}
			if (r > 2) {
				wcet += r - 2;
			}
			if (b > 37) {
				wcet += b - 37;
			}
			break;
		// INVOKENONVIRTUAL = 183
		// case org.apache.bcel.Constants.INVOKENONVIRTUAL : wcet = -1; break;
		// INVOKESTATIC = 184
		case org.apache.bcel.Constants.INVOKESTATIC:
			wcet = 74 + r;
			if (r > 3) {
				wcet += r - 3;
			}
			if (r > 2) {
				wcet += r - 2;
			}
			if (b > 37) {
				wcet += b - 37;
			}
			break;
		// INVOKEINTERFACE = 185
		case org.apache.bcel.Constants.INVOKEINTERFACE:
			wcet = 114 + 4 * r;
			if (r > 3) {
				wcet += r - 3;
			}
			if (r > 2) {
				wcet += r - 2;
			}
			if (b > 37) {
				wcet += b - 37;
			}
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
			wcet = 18;
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
			break;
		// JOPSYS_WR = 210
		case JOPSYS_WR:
			wcet = 5 + w;
			break;
		// JOPSYS_RDMEM = 211
		case JOPSYS_RDMEM:
			wcet = 4 + r;
			break;
		// JOPSYS_WRMEM = 212
		case JOPSYS_WRMEM:
			wcet = 5 + w;
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
			int wt = 0;
			if (w>8) wt = w-8;
			wcet = 14+r+ n*(23+wt);
			break;
		// JOPSYS_EXT2INT = 220
		case JOPSYS_EXT2INT:
			int rt = 0;
			if (r>10) rt = r-10;
			wcet = 14+r+ n*(23+rt);
			break;
		// JOPSYS_NOP = 221
		case JOPSYS_NOP:
			wcet = 1;
			break;

		case 223: // conditional move 
		default:
			wcet = 5;
			break;
		}
		// TODO: Add the JOP speciffic codes?
		if (isInJava(opcode)) {
			return 0;
		}
		return wcet;
	}

	static boolean isInJava(int opcode) {
		
		switch (opcode) {
			case org.apache.bcel.Constants.NEW:
				return true;
			default:
				return false;
		}
	}
	/**
	 * Check to see if there is a valid WCET count for the instruction.
	 * 
	 * @param opcode
	 * @return true if there is a valid wcet value
	 */
	static boolean wcetAvailable(int opcode) {
		if (getCycles(opcode, false, 0) == WCETNOTAVAILABLE)
			return false;
		else
			return true;
	}

	/**
	 * Method load time on invoke or return if there is a cache miss (see pMiss).
	 * 
	 * @see ms thesis p 232
	 */
	public static int calculateB(boolean hit, int n) {
		int b = -1;
		if (n == -1) {
			System.err.println("n not set!");
			System.exit(-1);
		} else {
			if (hit) {
				b = 4;
			} else {
				b = 6 + (n+1) * (2+c);
			}
		}
		return b;
	}
	// should be removed from WCETAnalyser:

}
