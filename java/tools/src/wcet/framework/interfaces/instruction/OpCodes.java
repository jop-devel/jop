/**
 * 
 */
package wcet.framework.interfaces.instruction;

/**
 * Interface defining names and values of java bytecodes an jop instruction.
 * @author Elena Axamitova
 * @version 0.2 12.02.2007
 */
public interface OpCodes {

    
    String ILLEGAL_OPCODE = "ILLEGAL_OPCODE";
    
    int JOP_INSN_START = 209;
    /**
     * Bytecode names array.
     */
    String[] OPCODE_NAMES = { "nop", "aconst_null", "iconst_m1", "iconst_0",
	    "iconst_1", "iconst_2", "iconst_3", "iconst_4", "iconst_5",
	    "lconst_0", "lconst_1", "fconst_0", "fconst_1", "fconst_2",
	    "dconst_0", "dconst_1", "bipush", "sipush", "ldc", "ldc_w",
	    "ldc2_w", "iload", "lload", "fload", "dload", "aload", "iload_0",
	    "iload_1", "iload_2", "iload_3", "lload_0", "lload_1", "lload_2",
	    "lload_3", "fload_0", "fload_1", "fload_2", "fload_3", "dload_0",
	    "dload_1", "dload_2", "dload_3", "aload_0", "aload_1", "aload_2",
	    "aload_3", "iaload", "laload", "faload", "daload", "aaload",
	    "baload", "caload", "saload", "istore", "lstore", "fstore",
	    "dstore", "astore", "istore_0", "istore_1", "istore_2", "istore_3",
	    "lstore_0", "lstore_1", "lstore_2", "lstore_3", "fstore_0",
	    "fstore_1", "fstore_2", "fstore_3", "dstore_0", "dstore_1",
	    "dstore_2", "dstore_3", "astore_0", "astore_1", "astore_2",
	    "astore_3", "iastore", "lastore", "fastore", "dastore", "aastore",
	    "bastore", "castore", "sastore", "pop", "pop2", "dup", "dup_x1",
	    "dup_x2", "dup2", "dup2_x1", "dup2_x2", "swap", "iadd", "ladd",
	    "fadd", "dadd", "isub", "lsub", "fsub", "dsub", "imul", "lmul",
	    "fmul", "dmul", "idiv", "ldiv", "fdiv", "ddiv", "irem", "lrem",
	    "frem", "drem", "ineg", "lneg", "fneg", "dneg", "ishl", "lshl",
	    "ishr", "lshr", "iushr", "lushr", "iand", "land", "ior", "lor",
	    "ixor", "lxor", "iinc", "i2l", "i2f", "i2d", "l2i", "l2f", "l2d",
	    "f2i", "f2l", "f2d", "d2i", "d2l", "d2f", "i2b", "i2c", "i2s",
	    "lcmp", "fcmpl", "fcmpg", "dcmpl", "dcmpg", "ifeq", "ifne", "iflt",
	    "ifge", "ifgt", "ifle", "if_icmpeq", "if_icmpne", "if_icmplt",
	    "if_icmpge", "if_icmpgt", "if_icmple", "if_acmpeq", "if_acmpne",
	    "goto", "jsr", "ret", "tableswitch", "lookupswitch", "ireturn",
	    "lreturn", "freturn", "dreturn", "areturn", "return", "getstatic",
	    "putstatic", "getfield", "putfield", "invokevirtual",
	    "invokespecial", "invokestatic", "invokeinterface", ILLEGAL_OPCODE,
	    "new", "newarray", "anewarray", "arraylength", "athrow",
	    "checkcast", "instanceof", "monitorenter", "monitorexit", "wide",
	    "multianewarray", "ifnull", "ifnonnull", "goto_w", "jsr_w",
	    "breakpoint", ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE,
	    ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, "jopsys_rd",
	    "jopsys_wr", "jopsys_rdmem", "jopsys_wrmem", "jopsys_rdint",
	    "jopsys_wrint", "jopsys_getsp", "jopsys_setsp", "jopsys_getvp",
	    "jopsys_setvp", "jopsys_int2ext", "jopsys_ext2int", "jopsys_nop",
	    "jopsys_invoke", "jopsys_cond_move", "getstatic_ref", "putstatic_ref",
	    "getfield_ref", "putfield_ref", "getstatic_long", "putstatic_long",
	    "getfield_long", "putfield_long", ILLEGAL_OPCODE, ILLEGAL_OPCODE,
	    ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE,
	    ILLEGAL_OPCODE, ILLEGAL_OPCODE, "sys_int", "sys_exc",
	    ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE,
	    ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE,
	    ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE, ILLEGAL_OPCODE,
	    "sys_noimp", "sys_init" };

    // opcodes // visit method (- = idem)

    int NOP = 0; // visitInsn

    int ACONST_NULL = 1; // -

    int ICONST_M1 = 2; // -

    int ICONST_0 = 3; // -

    int ICONST_1 = 4; // -

    int ICONST_2 = 5; // -

    int ICONST_3 = 6; // -

    int ICONST_4 = 7; // -

    int ICONST_5 = 8; // -

    int LCONST_0 = 9; // -

    int LCONST_1 = 10; // -

    int FCONST_0 = 11; // -

    int FCONST_1 = 12; // -

    int FCONST_2 = 13; // -

    int DCONST_0 = 14; // -

    int DCONST_1 = 15; // -

    int BIPUSH = 16; // visitIntInsn

    int SIPUSH = 17; // -

    int LDC = 18; // visitLdcInsn

    int LDC_W = 19; // -

    int LDC2_W = 20; // -

    int ILOAD = 21; // visitVarInsn

    int LLOAD = 22; // -

    int FLOAD = 23; // -

    int DLOAD = 24; // -

    int ALOAD = 25; // -

    int ILOAD_0 = 26; // -

    int ILOAD_1 = 27; // -

    int ILOAD_2 = 28; // -

    int ILOAD_3 = 29; // -

    int LLOAD_0 = 30; // -

    int LLOAD_1 = 31; // -

    int LLOAD_2 = 32; // -

    int LLOAD_3 = 33; // -

    int FLOAD_0 = 34; // -

    int FLOAD_1 = 35; // -

    int FLOAD_2 = 36; // -

    int FLOAD_3 = 37; // -

    int DLOAD_0 = 38; // -

    int DLOAD_1 = 39; // -

    int DLOAD_2 = 40; // -

    int DLOAD_3 = 41; // -

    int ALOAD_0 = 42; // -

    int ALOAD_1 = 43; // -

    int ALOAD_2 = 44; // -

    int ALOAD_3 = 45; // -

    int IALOAD = 46; // visitInsn

    int LALOAD = 47; // -

    int FALOAD = 48; // -

    int DALOAD = 49; // -

    int AALOAD = 50; // -

    int BALOAD = 51; // -

    int CALOAD = 52; // -

    int SALOAD = 53; // -

    int ISTORE = 54; // visitVarInsn

    int LSTORE = 55; // -

    int FSTORE = 56; // -

    int DSTORE = 57; // -

    int ASTORE = 58; // -

    int ISTORE_0 = 59; // -

    int ISTORE_1 = 60; // -

    int ISTORE_2 = 61; // -

    int ISTORE_3 = 62; // -

    int LSTORE_0 = 63; // -

    int LSTORE_1 = 64; // -

    int LSTORE_2 = 65; // -

    int LSTORE_3 = 66; // -

    int FSTORE_0 = 67; // -

    int FSTORE_1 = 68; // -

    int FSTORE_2 = 69; // -

    int FSTORE_3 = 70; // -

    int DSTORE_0 = 71; // -

    int DSTORE_1 = 72; // -

    int DSTORE_2 = 73; // -

    int DSTORE_3 = 74; // -

    int ASTORE_0 = 75; // -

    int ASTORE_1 = 76; // -

    int ASTORE_2 = 77; // -

    int ASTORE_3 = 78; // -

    int IASTORE = 79; // visitInsn

    int LASTORE = 80; // -

    int FASTORE = 81; // -

    int DASTORE = 82; // -

    int AASTORE = 83; // -

    int BASTORE = 84; // -

    int CASTORE = 85; // -

    int SASTORE = 86; // -

    int POP = 87; // -

    int POP2 = 88; // -

    int DUP = 89; // -

    int DUP_X1 = 90; // -

    int DUP_X2 = 91; // -

    int DUP2 = 92; // -

    int DUP2_X1 = 93; // -

    int DUP2_X2 = 94; // -

    int SWAP = 95; // -

    int IADD = 96; // -

    int LADD = 97; // -

    int FADD = 98; // -

    int DADD = 99; // -

    int ISUB = 100; // -

    int LSUB = 101; // -

    int FSUB = 102; // -

    int DSUB = 103; // -

    int IMUL = 104; // -

    int LMUL = 105; // -

    int FMUL = 106; // -

    int DMUL = 107; // -

    int IDIV = 108; // -

    int LDIV = 109; // -

    int FDIV = 110; // -

    int DDIV = 111; // -

    int IREM = 112; // -

    int LREM = 113; // -

    int FREM = 114; // -

    int DREM = 115; // -

    int INEG = 116; // -

    int LNEG = 117; // -

    int FNEG = 118; // -

    int DNEG = 119; // -

    int ISHL = 120; // -

    int LSHL = 121; // -

    int ISHR = 122; // -

    int LSHR = 123; // -

    int IUSHR = 124; // -

    int LUSHR = 125; // -

    int IAND = 126; // -

    int LAND = 127; // -

    int IOR = 128; // -

    int LOR = 129; // -

    int IXOR = 130; // -

    int LXOR = 131; // -

    int IINC = 132; // visitIincInsn

    int I2L = 133; // visitInsn

    int I2F = 134; // -

    int I2D = 135; // -

    int L2I = 136; // -

    int L2F = 137; // -

    int L2D = 138; // -

    int F2I = 139; // -

    int F2L = 140; // -

    int F2D = 141; // -

    int D2I = 142; // -

    int D2L = 143; // -

    int D2F = 144; // -

    int I2B = 145; // -

    int I2C = 146; // -

    int I2S = 147; // -

    int LCMP = 148; // -

    int FCMPL = 149; // -

    int FCMPG = 150; // -

    int DCMPL = 151; // -

    int DCMPG = 152; // -

    int IFEQ = 153; // visitJumpInsn

    int IFNE = 154; // -

    int IFLT = 155; // -

    int IFGE = 156; // -

    int IFGT = 157; // -

    int IFLE = 158; // -

    int IF_ICMPEQ = 159; // -

    int IF_ICMPNE = 160; // -

    int IF_ICMPLT = 161; // -

    int IF_ICMPGE = 162; // -

    int IF_ICMPGT = 163; // -

    int IF_ICMPLE = 164; // -

    int IF_ACMPEQ = 165; // -

    int IF_ACMPNE = 166; // -

    int GOTO = 167; // -

    int JSR = 168; // -

    int RET = 169; // visitVarInsn

    int TABLESWITCH = 170; // visiTableSwitchInsn

    int LOOKUPSWITCH = 171; // visitLookupSwitch

    int IRETURN = 172; // visitInsn

    int LRETURN = 173; // -

    int FRETURN = 174; // -

    int DRETURN = 175; // -

    int ARETURN = 176; // -

    int RETURN = 177; // -

    int GETSTATIC = 178; // visitFieldInsn

    int PUTSTATIC = 179; // -

    int GETFIELD = 180; // -

    int PUTFIELD = 181; // -

    int INVOKEVIRTUAL = 182; // visitMethodInsn

    int INVOKESPECIAL = 183; // -

    int INVOKESTATIC = 184; // -

    int INVOKEINTERFACE = 185; // -

    // int UNUSED = 186; // NOT VISITED
    int NEW = 187; // visitTypeInsn

    int NEWARRAY = 188; // visitIntInsn

    int ANEWARRAY = 189; // visitTypeInsn

    int ARRAYLENGTH = 190; // visitInsn

    int ATHROW = 191; // -

    int CHECKCAST = 192; // visitTypeInsn

    int INSTANCEOF = 193; // -

    int MONITORENTER = 194; // visitInsn

    int MONITOREXIT = 195; // -

    int WIDE = 196; // NOT VISITED

    int MULTIANEWARRAY = 197; // visitMultiANewArrayInsn

    int IFNULL = 198; // visitJumpInsn

    int IFNONNULL = 199; // -

    int GOTO_W = 200; // -

    int JSR_W = 201; // -

    // Native bytecodes (see jvm.asm)
    int JOPSYS_RD = 209;

    int JOPSYS_WR = 210;

    int JOPSYS_RDMEM = 211;

    int JOPSYS_WRMEM = 212;

    int JOPSYS_RDINT = 213;

    int JOPSYS_WRINT = 214;

    int JOPSYS_GETSP = 215;

    int JOPSYS_SETSP = 216;

    int JOPSYS_GETVP = 217;

    int JOPSYS_SETVP = 218;

    int JOPSYS_INT2EXT = 219;

    int JOPSYS_EXT2INT = 220;

    int JOPSYS_NOP = 221;

    int JOPSYS_INVOKE = 222;

    int JOPSYS_COND_MOVE = 223;

    int GETSTATIC_REF = 224;

    int PUTSTATIC_REF = 225;

    int GETFIELD_REF = 226;

    int PUTFIELD_REF = 227;

    int GETSTATIC_LONG = 228;

    int PUTSTATIC_LONG = 229;

    int GETFIELD_LONG = 230;

    int PUTFIELD_LONG = 231;

    int SYS_INT = 240;

    int SYS_EXC = 241;

    int SYS_NOIMP = 254;

    int SYS_INIT = 255;

}
