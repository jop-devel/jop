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
*	Definition of JVM instructions for JopSim, Jopa,...
*
*	2005-05-27	added Native to jopsys_* mapping for Flavius JOPizer
*/


package com.jopdesign.tools;

import java.util.*;

public class JopInstr{

	public final static int IMP_ASM = 0;
	public final static int IMP_JAVA = 1;
	public final static int IMP_NO = 2;

	private String name;
	private int len;		// in byte codes (0 means variable length!)
	private int imp;		// is implemented in JOP
	private int cnt;
	private static Map map = new HashMap();

	// mapping of native methods
	private static Map natMap = new HashMap();

	public static int get(String s) {

		Integer i = (Integer) map.get(s);
		if (i==null) {
			return -1;
		} else {
			return i.intValue();
		}
	}
	
	public static int getNative(String s) {
		
		s = (String) natMap.get(s);
		if (s==null) {
			return -1;
		} else {
			return get(s);
		}
	}

	private JopInstr(String s, int i, int j, int k) {
		name = s;
		len = i;
		imp = j;
		cnt = k;
	}

	public static String name(int i) {

		return ia[i].name;
	}

	public static int cnt(int i) {

		return ia[i].cnt;
	}

	public static int len(int i) {

		return ia[i].len;
	}

	public static int imp(int i) {

		return ia[i].imp;
	}

	private static JopInstr[] ia = new JopInstr[] 
	{
		new JopInstr("nop", 1, IMP_ASM, 1),			// 0x00
		new JopInstr("aconst_null", 1, IMP_ASM, 1),	// 0x01
		new JopInstr("iconst_m1", 1, IMP_ASM, 1),	// 0x02
		new JopInstr("iconst_0", 1, IMP_ASM, 1),		// 0x03
		new JopInstr("iconst_1", 1, IMP_ASM, 1),		// 0x04
		new JopInstr("iconst_2", 1, IMP_ASM, 1),		// 0x05
		new JopInstr("iconst_3", 1, IMP_ASM, 1),		// 0x06
		new JopInstr("iconst_4", 1, IMP_ASM, 1),		// 0x07

		new JopInstr("iconst_5", 1, IMP_ASM, 1),		// 0x08
		new JopInstr("lconst_0", 1, IMP_ASM, 2),		// 0x09
		new JopInstr("lconst_1", 1, IMP_ASM, 2),		// 0x0A
		new JopInstr("fconst_0", 1, IMP_NO, 1),		// 0x0B
		new JopInstr("fconst_1", 1, IMP_NO, 1),		// 0x0C
		new JopInstr("fconst_2", 1, IMP_NO, 1),		// 0x0D
		new JopInstr("dconst_0", 1, IMP_NO, 1),		// 0x0E
		new JopInstr("dconst_1", 1, IMP_NO, 1),		// 0x0F

		new JopInstr("bipush", 2, IMP_ASM, 2),		// 0x10
		new JopInstr("sipush", 3, IMP_ASM, 3),		// 0x11
		new JopInstr("ldc", 2, IMP_ASM, 2),			// 0x12
		new JopInstr("ldc_w", 3, IMP_NO, 1),		// 0x13
		new JopInstr("ldc2_w", 3, IMP_ASM, 17),		// 0x14
		new JopInstr("iload", 2, IMP_ASM, 2),		// 0x15
		new JopInstr("lload", 2, IMP_NO, 1),		// 0x16
		new JopInstr("fload", 2, IMP_NO, 1),		// 0x17

		new JopInstr("dload", 2, IMP_NO, 1),		// 0x18
		new JopInstr("aload", 2, IMP_ASM, 2),		// 0x19
		new JopInstr("iload_0", 1, IMP_ASM, 1),		// 0x1A
		new JopInstr("iload_1", 1, IMP_ASM, 1),		// 0x1B
		new JopInstr("iload_2", 1, IMP_ASM, 1),		// 0x1C
		new JopInstr("iload_3", 1, IMP_ASM, 1),		// 0x1D
		new JopInstr("lload_0", 1, IMP_ASM, 2),		// 0x1E
		new JopInstr("lload_1", 1, IMP_ASM, 2),		// 0x1F

		new JopInstr("lload_2", 1, IMP_ASM, 2),		// 0x20
		new JopInstr("lload_3", 1, IMP_ASM, 12),		// 0x21
		new JopInstr("fload_0", 1, IMP_NO, 1),		// 0x22
		new JopInstr("fload_1", 1, IMP_NO, 1),		// 0x23
		new JopInstr("fload_2", 1, IMP_NO, 1),		// 0x24
		new JopInstr("fload_3", 1, IMP_NO, 1),		// 0x25
		new JopInstr("dload_0", 1, IMP_NO, 1),		// 0x26
		new JopInstr("dload_1", 1, IMP_NO, 1),		// 0x27

		new JopInstr("dload_2", 1, IMP_NO, 1),		// 0x28
		new JopInstr("dload_3", 1, IMP_NO, 1),		// 0x29
		new JopInstr("aload_0", 1, IMP_ASM, 1),		// 0x2A
		new JopInstr("aload_1", 1, IMP_ASM, 1),		// 0x2B
		new JopInstr("aload_2", 1, IMP_ASM, 1),		// 0x2C
		new JopInstr("aload_3", 1, IMP_ASM, 1),		// 0x2D
		new JopInstr("iaload", 1, IMP_ASM, 17),		// 0x2E
		new JopInstr("laload", 1, IMP_NO, 1),		// 0x2F

		new JopInstr("faload", 1, IMP_ASM, 17),		// 0x30
		new JopInstr("daload", 1, IMP_NO, 1),		// 0x31
		new JopInstr("aaload", 1, IMP_ASM, 17),		// 0x32
		new JopInstr("baload", 1, IMP_ASM, 17),		// 0x33
		new JopInstr("caload", 1, IMP_ASM, 17),		// 0x34
		new JopInstr("saload", 1, IMP_ASM, 17),		// 0x35
		new JopInstr("istore", 2, IMP_ASM, 2),		// 0x36
		new JopInstr("lstore", 2, IMP_NO, 1),		// 0x37

		new JopInstr("fstore", 2, IMP_NO, 1),		// 0x38
		new JopInstr("dstore", 2, IMP_NO, 1),		// 0x39
		new JopInstr("astore", 2, IMP_ASM, 2),		// 0x3A
		new JopInstr("istore_0", 1, IMP_ASM, 1),		// 0x3B
		new JopInstr("istore_1", 1, IMP_ASM, 1),		// 0x3C
		new JopInstr("istore_2", 1, IMP_ASM, 1),		// 0x3D
		new JopInstr("istore_3", 1, IMP_ASM, 1),		// 0x3E
		new JopInstr("lstore_0", 1, IMP_ASM, 2),		// 0x3F

		new JopInstr("lstore_1", 1, IMP_ASM, 2),		// 0x40
		new JopInstr("lstore_2", 1, IMP_ASM, 2),		// 0x41
		new JopInstr("lstore_3", 1, IMP_ASM, 12),		// 0x42
		new JopInstr("fstore_0", 1, IMP_NO, 1),		// 0x43
		new JopInstr("fstore_1", 1, IMP_NO, 1),		// 0x44
		new JopInstr("fstore_2", 1, IMP_NO, 1),		// 0x45
		new JopInstr("fstore_3", 1, IMP_NO, 1),		// 0x46
		new JopInstr("dstore_0", 1, IMP_NO, 1),		// 0x47

		new JopInstr("dstore_1", 1, IMP_NO, 1),		// 0x48
		new JopInstr("dstore_2", 1, IMP_NO, 1),		// 0x49
		new JopInstr("dstore_3", 1, IMP_NO, 1),		// 0x4A
		new JopInstr("astore_0", 1, IMP_ASM, 1),		// 0x4B
		new JopInstr("astore_1", 1, IMP_ASM, 1),		// 0x4C
		new JopInstr("astore_2", 1, IMP_ASM, 1),		// 0x4D
		new JopInstr("astore_3", 1, IMP_ASM, 1),		// 0x4E
		new JopInstr("iastore", 1, IMP_ASM, 18),		// 0x4F

		new JopInstr("lastore", 1, IMP_NO, 1),		// 0x50
		new JopInstr("fastore", 1, IMP_ASM, 18),		// 0x51
		new JopInstr("dastore", 1, IMP_NO, 1),		// 0x52
		new JopInstr("aastore", 1, IMP_ASM, 18),		// 0x53
		new JopInstr("bastore", 1, IMP_ASM, 18),		// 0x54
		new JopInstr("castore", 1, IMP_ASM, 18),		// 0x55
		new JopInstr("sastore", 1, IMP_ASM, 18),		// 0x56
		new JopInstr("pop", 1, IMP_ASM, 1),			// 0x57

		new JopInstr("pop2", 1, IMP_NO, 1),			// 0x58
		new JopInstr("dup", 1, IMP_ASM, 1),			// 0x59
		new JopInstr("dup_x1", 1, IMP_ASM, 5),		// 0x5A
		new JopInstr("dup_x2", 1, IMP_NO, 1),		// 0x5B
		new JopInstr("dup2", 1, IMP_ASM, 6),			// 0x5C
		new JopInstr("dup2_x1", 1, IMP_NO, 1),		// 0x5D
		new JopInstr("dup2_x2", 1, IMP_NO, 1),		// 0x5E
		new JopInstr("swap", 1, IMP_NO, 1),			// 0x5F

		new JopInstr("iadd", 1, IMP_ASM, 1),		// 0x60
		new JopInstr("ladd", 1, IMP_NO, 1),		// 0x61
		new JopInstr("fadd", 1, IMP_NO, 1),		// 0x62
		new JopInstr("dadd", 1, IMP_NO, 1),		// 0x63
		new JopInstr("isub", 1, IMP_ASM, 1),		// 0x64
		new JopInstr("lsub", 1, IMP_NO, 1),		// 0x65
		new JopInstr("fsub", 1, IMP_NO, 1),		// 0x66
		new JopInstr("dsub", 1, IMP_NO, 1),		// 0x67

		new JopInstr("imul", 1, IMP_ASM, 19),		// 0x68
		new JopInstr("lmul", 1, IMP_NO, 1),		// 0x69
		new JopInstr("fmul", 1, IMP_NO, 1),		// 0x6A
		new JopInstr("dmul", 1, IMP_NO, 1),		// 0x6B
		new JopInstr("idiv", 1, IMP_ASM, 1300),		// 0x6C
		new JopInstr("ldiv", 1, IMP_NO, 1),		// 0x6D
		new JopInstr("fdiv", 1, IMP_NO, 1),		// 0x6E
		new JopInstr("ddiv", 1, IMP_NO, 1),		// 0x6F

		new JopInstr("irem", 1, IMP_ASM, 1300),		// 0x70
		new JopInstr("lrem", 1, IMP_NO, 1),		// 0x71
		new JopInstr("frem", 1, IMP_NO, 1),		// 0x72
		new JopInstr("drem", 1, IMP_NO, 1),		// 0x73
		new JopInstr("ineg", 1, IMP_ASM, 4),		// 0x74
		new JopInstr("lneg", 1, IMP_NO, 1),		// 0x75
		new JopInstr("fneg", 1, IMP_NO, 1),		// 0x76
		new JopInstr("dneg", 1, IMP_NO, 1),		// 0x77

		new JopInstr("ishl", 1, IMP_ASM, 1),		// 0x78
		new JopInstr("lshl", 1, IMP_NO, 1),		// 0x79
		new JopInstr("ishr", 1, IMP_ASM, 1),		// 0x7A
		new JopInstr("lshr", 1, IMP_NO, 1),		// 0x7B
		new JopInstr("iushr", 1, IMP_ASM, 1),	// 0x7C
		new JopInstr("lushr", 1, IMP_NO, 1),	// 0x7D
		new JopInstr("iand", 1, IMP_ASM, 1),		// 0x7E
		new JopInstr("land", 1, IMP_NO, 1),		// 0x7F

		new JopInstr("ior", 1, IMP_ASM, 1),		// 0x80
		new JopInstr("lor", 1, IMP_NO, 1),		// 0x81
		new JopInstr("ixor", 1, IMP_ASM, 1),		// 0x82
		new JopInstr("lxor", 1, IMP_NO, 1),		// 0x83
		new JopInstr("iinc", 3, IMP_ASM, 11),		// 0x84
		new JopInstr("i2l", 1, IMP_NO, 1),		// 0x85
		new JopInstr("i2f", 1, IMP_NO, 1),		// 0x86
		new JopInstr("i2d", 1, IMP_NO, 1),		// 0x87

		new JopInstr("l2i", 1, IMP_ASM, 3),		// 0x88
		new JopInstr("l2f", 1, IMP_NO, 1),		// 0x89
		new JopInstr("l2d", 1, IMP_NO, 1),		// 0x8A
		new JopInstr("f2i", 1, IMP_NO, 1),		// 0x8B
		new JopInstr("f2l", 1, IMP_NO, 1),		// 0x8C
		new JopInstr("f2d", 1, IMP_NO, 1),		// 0x8D
		new JopInstr("d2i", 1, IMP_NO, 1),		// 0x8E
		new JopInstr("d2l", 1, IMP_NO, 1),		// 0x8F

		new JopInstr("d2f", 1, IMP_NO, 1),		// 0x90
		new JopInstr("i2b", 1, IMP_NO, 1),		// 0x91
		new JopInstr("i2c", 1, IMP_ASM, 2),		// 0x92
		new JopInstr("i2s", 1, IMP_NO, 1),		// 0x93
		new JopInstr("lcmp", 1, IMP_NO, 1),		// 0x94
		new JopInstr("fcmpl", 1, IMP_NO, 1),	// 0x95
		new JopInstr("fcmpg", 1, IMP_NO, 1),	// 0x96
		new JopInstr("dcmpl", 1, IMP_NO, 1),	// 0x97

		new JopInstr("dcmpg", 1, IMP_NO, 1),	// 0x98
		new JopInstr("ifeq", 3, IMP_ASM, 4),		// 0x99
		new JopInstr("ifne", 3, IMP_ASM, 4),		// 0x9A
		new JopInstr("iflt", 3, IMP_ASM, 4),		// 0x9B
		new JopInstr("ifge", 3, IMP_ASM, 4),		// 0x9C
		new JopInstr("ifgt", 3, IMP_ASM, 4),		// 0x9D
		new JopInstr("ifle", 3, IMP_ASM, 4),		// 0x9E
		new JopInstr("if_icmpeq", 3, IMP_ASM, 4),	// 0x9F

		new JopInstr("if_icmpne", 3, IMP_ASM, 4),	// 0xA0
		new JopInstr("if_icmplt", 3, IMP_ASM, 4),	// 0xA1
		new JopInstr("if_icmpge", 3, IMP_ASM, 4),	// 0xA2
		new JopInstr("if_icmpgt", 3, IMP_ASM, 4),	// 0xA3
		new JopInstr("if_icmple", 3, IMP_ASM, 4),	// 0xA4
		new JopInstr("if_acmpeq", 3, IMP_ASM, 4),	// 0xA5
		new JopInstr("if_acmpne", 3, IMP_ASM, 4),	// 0xA6
		new JopInstr("goto", 3, IMP_ASM, 4),			// 0xA7

		new JopInstr("jsr", 3, IMP_NO, 1),			// 0xA8
		new JopInstr("ret", 2, IMP_NO, 1),			// 0xA9
		new JopInstr("tableswitch", 0, IMP_NO, 1),	// 0xAA
		new JopInstr("lookupswitch", 0, IMP_NO, 1),	// 0xAB
		new JopInstr("ireturn", 1, IMP_ASM, 12),		// 0xAC
		new JopInstr("lreturn", 1, IMP_NO, 1),		// 0xAD
		new JopInstr("freturn", 1, IMP_NO, 1),		// 0xAE
		new JopInstr("dreturn", 1, IMP_NO, 1),		// 0xAF

		new JopInstr("areturn", 1, IMP_ASM, 1),		// 0xB0
		new JopInstr("return", 1, IMP_ASM, 10),		// 0xB1
		new JopInstr("getstatic", 3, IMP_ASM, 14),	// 0xB2		// derzeit!!!
		new JopInstr("putstatic", 3, IMP_ASM, 15),	// 0xB3
		new JopInstr("getfield", 3, IMP_ASM, 13),		// 0xB4
		new JopInstr("putfield", 3, IMP_ASM, 15),		// 0xB5
		new JopInstr("invokevirtual", 3, IMP_ASM, 30),	// 0xB6
		new JopInstr("invokespecial", 3, IMP_ASM, 30),	// 0xB7

		new JopInstr("invokestatic", 3, IMP_ASM, 30),		// 0xB8	cnt ????
		new JopInstr("invokeinterface", 5, IMP_ASM, 30),	// 0xB9
		new JopInstr("unused_ba", 1, IMP_NO, 1),		// 0xBA
		new JopInstr("new", 3, IMP_ASM, 30),				// 0xBB
		new JopInstr("newarray", 2, IMP_ASM, 26),			// 0xBC	// mit mem!!
		new JopInstr("anewarray", 3, IMP_JAVA, 1),		// 0xBD
		new JopInstr("arraylength", 1, IMP_ASM, 18),		// 0xBE		// mit mem!!
		new JopInstr("athrow", 1, IMP_NO, 1),			// 0xBF

		new JopInstr("checkcast", 3, IMP_NO, 1),		// 0xC0
		new JopInstr("instanceof", 3, IMP_NO, 1),		// 0xC1
		new JopInstr("monitorenter", 1, IMP_ASM, 9),		// 0xC2
		new JopInstr("monitorexit", 1, IMP_ASM, 12),		// 0xC3
		new JopInstr("wide", 0, IMP_NO, 1),				// 0xC4
		new JopInstr("multianewarray", 4, IMP_NO, 1),	// 0xC5
		new JopInstr("ifnull", 3, IMP_ASM, 1),			// 0xC6
		new JopInstr("ifnonnull", 3, IMP_ASM, 1),		// 0xC7

		new JopInstr("goto_w", 5, IMP_NO, 1),			// 0xC8
		new JopInstr("jsr_w", 5, IMP_NO, 1),			// 0xC9
		new JopInstr("breakpoint", 1, IMP_NO, 1),		// 0xCA

//
//	reserved instructions
//
		new JopInstr("resCB", 1, IMP_NO, 1),			// 0xCB
		new JopInstr("resCC", 1, IMP_NO, 1),			// 0xCC
		new JopInstr("resCD", 1, IMP_NO, 1),			// 0xCD
		new JopInstr("resCE", 1, IMP_NO, 1),			// 0xCE
		new JopInstr("resCF", 1, IMP_NO, 1),			// 0xCF

		new JopInstr("jopsys_null", 1, IMP_NO, 1),			// 0xD0
		new JopInstr("jopsys_rd", 1, IMP_ASM, 3),			// 0xD1
		new JopInstr("jopsys_wr", 1, IMP_ASM, 3),			// 0xD2
		new JopInstr("jopsys_rdmem", 1, IMP_ASM, 15),			// 0xD3
		new JopInstr("jopsys_wrmem", 1, IMP_ASM, 15),			// 0xD4
		new JopInstr("jopsys_rdint", 1, IMP_ASM, 8),			// 0xD5
		new JopInstr("jopsys_wrint", 1, IMP_ASM, 8),			// 0xD6
		new JopInstr("jopsys_getsp", 1, IMP_ASM, 3),			// 0xD7
		new JopInstr("jopsys_setsp", 1, IMP_ASM, 4),			// 0xD8
		new JopInstr("jopsys_getvp", 1, IMP_ASM, 1),			// 0xD9
		new JopInstr("jopsys_setvp", 1, IMP_ASM, 2),			// 0xDA
		new JopInstr("jopsys_int2ext", 1, IMP_ASM, 100),			// 0xDB
		new JopInstr("jopsys_ext2int", 1, IMP_ASM, 100),			// 0xDC
		new JopInstr("jopsys_nop", 1, IMP_NO, 1),			// 0xDD
		new JopInstr("jopsys_invoke", 1, IMP_NO, 1),			// 0xDE
//		new JopInstr("jopsys_cond_move", 1, IMP_NO, 1),			// 0xDF
		new JopInstr("resDF", 1, IMP_NO, 1),			// 0xDF

		new JopInstr("getstatic_ref", 3, IMP_ASM, 14),   // 0xE0
		new JopInstr("putstatic_ref", 3, IMP_ASM, 30),			// 0xE1
		new JopInstr("getfield_ref", 3, IMP_ASM, 13),			// 0xE2
		new JopInstr("putfield_ref", 3, IMP_ASM, 30),			// 0xE3
		new JopInstr("getstatic_long", 3, IMP_ASM, 30),			// 0xE4
		new JopInstr("putstatic_long", 3, IMP_ASM, 30),			// 0xE5
		new JopInstr("getfield_long", 3, IMP_ASM, 30),			// 0xE6
		new JopInstr("putfield_long", 3, IMP_ASM, 30),			// 0xE7
		new JopInstr("jopsys_memcpy", 1, IMP_ASM, 100),			// 0xE8
		new JopInstr("jopsys_getfield", 1, IMP_ASM, 1),			// 0xE9
		new JopInstr("jopsys_putfield", 1, IMP_ASM, 1),			// 0xEA
		new JopInstr("resEB", 1, IMP_NO, 1),			// 0xEB
		new JopInstr("invokesuper", 3, IMP_ASM, 30),			// 0xEC
		new JopInstr("resED", 1, IMP_NO, 1),			// 0xED
		new JopInstr("resEE", 1, IMP_NO, 1),			// 0xEE
		new JopInstr("resEF", 1, IMP_NO, 1),			// 0xEF

		new JopInstr("sys_int", 1, IMP_ASM, 1),			// 0xF0
		new JopInstr("sys_exc", 1, IMP_ASM, 1),			// 0xF1
		new JopInstr("resF2", 1, IMP_NO, 1),			// 0xF2
		new JopInstr("resF3", 1, IMP_NO, 1),			// 0xF3
		new JopInstr("resF4", 1, IMP_NO, 1),			// 0xF4
		new JopInstr("resF5", 1, IMP_NO, 1),			// 0xF5
		new JopInstr("resF6", 1, IMP_NO, 1),			// 0xF6
		new JopInstr("resF7", 1, IMP_NO, 1),			// 0xF7
		new JopInstr("resF8", 1, IMP_NO, 1),			// 0xF8
		new JopInstr("resF9", 1, IMP_NO, 1),			// 0xF9
		new JopInstr("resFA", 1, IMP_NO, 1),			// 0xFA
		new JopInstr("resFB", 1, IMP_NO, 1),			// 0xFB
		new JopInstr("resFC", 1, IMP_NO, 1),			// 0xFC
		new JopInstr("resFD", 1, IMP_NO, 1),			// 0xFD
		new JopInstr("sys_noim", 1, IMP_ASM, 1),			// 0xFE
		new JopInstr("sys_init", 1, IMP_ASM, 1),			// 0xFF
	};

	//
	//	Mapping of 'native' methods from Native.java
	//	to special bytecodes
	//	With JCC the index in Native was used, but with JOPizer
	//	and BCEL we need the expilicit mapping.
	
	private static String[] nativeMapping = {
			"rd", "jopsys_rd",
			"wr", "jopsys_wr",
			"rdMem", "jopsys_rdmem",
			"wrMem", "jopsys_wrmem",
			"rdIntMem", "jopsys_rdint",
			"wrIntMem", "jopsys_wrint",
			"getSP", "jopsys_getsp",
			"setSP", "jopsys_setsp",
			"getVP", "jopsys_getvp",
			"setVP", "jopsys_setvp",
			"int2extMem", "jopsys_int2ext",
			"ext2intMem", "jopsys_ext2int",
			"makeLong", "jopsys_nop",
			"invoke", "jopsys_invoke",
			"toInt", "jopsys_nop",
			"toFloat", "jopsys_nop",
			"toObject", "jopsys_nop",
			"toIntArray", "jopsys_nop",
			"toLong", "jopsys_nop",
			"toInt", "jopsys_nop",
			"toDouble", "jopsys_nop",
			"monitorExit", "monitorexit",
//			"condMove", "jopsys_cond_move",
			"memCopy", "jopsys_memcpy",
			"putField", "jopsys_putfield",
			"getField", "jopsys_getfield",
			"arrayLoad", "iaload",
			"arrayStore", "iastore",
			"arrayLength", "arraylength"
	};

	
	static {
		int i;
		for (i=0; i<ia.length; ++i) {
			map.put(ia[i].name, new Integer(i));
		}
		for (i=0; i<nativeMapping.length; i+=2) {
			natMap.put(nativeMapping[i], nativeMapping[i+1]);
		}
	}

	public static void main(String[] args) {

		for (int i=0; i<256/8; ++i) {
			System.out.print("\t\t\t");
			for (int j=0; j<8; ++j) {
				System.out.print(name(i*8+j)+", ");
			}
			System.out.println();
		}
		for (int i=0; i<256; ++i) {
			System.out.print(i+"; ");
			System.out.print(JopInstr.name(i)+"; ");
			System.out.println(JopInstr.cnt(i));
		}
/*
		for (int i=0; i<256; ++i) {
			System.out.print(i+"\t");
			System.out.print(JopInstr.name(i)+"\t");
			int imp = JopInstr.imp(i);
			if (imp==IMP_ASM) {
				if (JopInstr.cnt(i)==1) {
					System.out.println("hw");
				} else {
					System.out.println("mc");
				}
			} else if (imp==IMP_JAVA) {
				System.out.println("Java");
			} else {
				System.out.println("NI");
			}
		}
*/
	}
}
