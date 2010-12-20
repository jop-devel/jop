	/*
	  This file is part of JOP, the Java Optimized Processor
	    see <http://www.jopdesign.com/>

	  Copyright (C) 2006-2008, Martin Schoeberl (martin@jopdesign.com)

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
package com.jopdesign.timing.jamuth;

import java.util.Date;

import org.apache.bcel.Constants;

import com.jopdesign.timing.ConsoleTable;
import com.jopdesign.timing.TimingTable;
import com.jopdesign.timing.ConsoleTable.Alignment;
import com.jopdesign.timing.ConsoleTable.TableRow;
import com.jopdesign.tools.JopInstr;

/**
 * It has wcet info on byte code instruction granularity. Should we consider
 * making a class that wraps the microcodes into objects?
 */
public class JamuthTimingTable extends TimingTable<JamuthInstructionInfo> {

	private static final int WCETNOTAVAILABLE = -1;

	// the read and write wait states, ram_cnt - 1
	public int r = 8;
	public int w = 5;

	// cache read wait state (r-1)
	public int c = 0;
	public int fcs = 1;
	public int fca = 4;

	// wait states for a read to the scratchpad
	public int rs = 5;

	// cycles to call a trap routine
	public int t = 15+rs;

	// latency cycles of a jump
	public int j = 4;
	public int fls = 0;
	public int fla = 4;

	// jamuth bytecodes (unused ?)
	public static final short SETHI			   = 237;
	public static final short EXTENDED         = 255;

	public JamuthTimingTable() {
		// constants hardcoded for now
	}

	public boolean hasTimingInfo(int opcode) {
		if (getCycles(new JamuthInstructionInfo(opcode,0)) == WCETNOTAVAILABLE)
			return false;
		else
			return true;
	}

	/**
	 * Returns the wcet count for the instruction.
	 * - FIXME: is the calculation for the alignment delay correct ?
	 * - FIXME: No branchlatency for ifeq  ?
	 * - TODO: pipeline dependencies ?
	 * - TODO: LDC
	 * - TODO: tableswitch, lookupswitch
	 * - TODO: monitorenter, monitorext
	 * - Unsupported: invokeinterface, instanceof, checkcast
	 */
	public long getCycles(JamuthInstructionInfo info) {
		int wcet = -1;

		int branchlatency=fla;
		// Add latency if the jump target isn't aligned
		// TODO @ sascha: is > 0 correct (i.e. 4 byte alignment) ?
		if(! info.hasJumpTargetAddress() || (info.getJumpTargetAddress() & 3) > 0) {
			branchlatency+=fla;
		}
		int latency = 0;

		switch (info.getOpcode()) {
		// NOP = 0
		case Constants.NOP:
			wcet = 1;
			break;
			// ACONST_NULL = 1
		case Constants.ACONST_NULL:
			wcet = 1;
			break;
			// ICONST_M1 = 2
		case Constants.ICONST_M1:
			wcet = 1;
			break;
			// ICONST_0 = 3
		case Constants.ICONST_0:
			wcet = 1;
			break;
			// ICONST_1 = 4
		case Constants.ICONST_1:
			wcet = 1;
			break;
			// ICONST_2 = 5
		case Constants.ICONST_2:
			wcet = 1;
			break;
			// ICONST_3 = 6
		case Constants.ICONST_3:
			wcet = 1;
			break;
			// ICONST_4 = 7
		case Constants.ICONST_4:
			wcet = 1;
			break;
			// ICONST_5 = 8
		case Constants.ICONST_5:
			wcet = 1;
			break;
			// LCONST_0 = 9
		case Constants.LCONST_0:
			wcet = 1;
			break;
			// LCONST_1 = 10
		case Constants.LCONST_1:
			wcet = 1;
			break;
			// FCONST_0 = 11
		case Constants.FCONST_0:
			wcet = 1;
			break;
			// FCONST_1 = 12
		case Constants.FCONST_1:
			wcet = 2;
			break;
			// FCONST_2 = 13
		case Constants.FCONST_2:
			wcet = 2;
			break;
			// DCONST_0 = 14
		case Constants.DCONST_0:
			wcet = 1;
			break;
			// DCONST_1 = 15
		case Constants.DCONST_1:
			wcet = 3;
			break;
			// BIPUSH = 16
		case Constants.BIPUSH:
			wcet = 1;
			break;
			// SIPUSH = 17
		case Constants.SIPUSH:
			wcet = 1;
			break;
			// LDC = 18
		
		case Constants.LDC:
			// TODO: je nach dem, ob String oder Zahl
			wcet = -1; 
			break;
			// LDC_W = 19
		case Constants.LDC_W:
			wcet = 6 + r;
			latency = r;
			break;
			// LDC2_W = 20
		case Constants.LDC2_W:
			wcet = 9+(2*r);
			latency = 2*r;
			break;
			// ILOAD = 21
		case Constants.ILOAD:
			wcet = 1;
			break;
			// LLOAD = 22
		case Constants.LLOAD:
			wcet = 1;
			break;
			// FLOAD = 23
		case Constants.FLOAD:
			wcet = 1;
			break;
			// DLOAD = 24
		case Constants.DLOAD:
			wcet = 1;
			break;
			// ALOAD = 25
		case Constants.ALOAD:
			wcet = 1;
			break;
			// ILOAD_0 = 26
		case Constants.ILOAD_0:
			wcet = 1;
			break;
			// ILOAD_1 = 27
		case Constants.ILOAD_1:
			wcet = 1;
			break;
			// ILOAD_2 = 28
		case Constants.ILOAD_2:
			wcet = 1;
			break;
			// ILOAD_3 = 29
		case Constants.ILOAD_3:
			wcet = 1;
			break;
			// LLOAD_0 = 30
		case Constants.LLOAD_0:
			wcet = 1;
			break;
			// LLOAD_1 = 31
		case Constants.LLOAD_1:
			wcet = 1;
			break;
			// LLOAD_2 = 32
		case Constants.LLOAD_2:
			wcet = 1;
			break;
			// LLOAD_3 = 33
		case Constants.LLOAD_3:
			wcet = 1;
			break;
			// FLOAD_0 = 34
		case Constants.FLOAD_0:
			wcet = 1;
			break;
			// FLOAD_1 = 35
		case Constants.FLOAD_1:
			wcet = 1;
			break;
			// FLOAD_2 = 36
		case Constants.FLOAD_2:
			wcet = 1;
			break;
			// FLOAD_3 = 37
		case Constants.FLOAD_3:
			wcet = 1;
			break;
			// DLOAD_0 = 38
		case Constants.DLOAD_0:
			wcet = 1;
			break;
			// DLOAD_1 = 39
		case Constants.DLOAD_1:
			wcet = 1;
			break;
			// DLOAD_2 = 40
		case Constants.DLOAD_2:
			wcet = 1;
			break;
			// DLOAD_3 = 41
		case Constants.DLOAD_3:
			wcet = 1;
			break;
			// ALOAD_0 = 42
		case Constants.ALOAD_0:
			wcet = 1;
			break;
			// ALOAD_1 = 43
		case Constants.ALOAD_1:
			wcet = 1;
			break;
			// ALOAD_2 = 44
		case Constants.ALOAD_2:
			wcet = 1;
			break;
			// ALOAD_3 = 45
		case Constants.ALOAD_3:
			wcet = 1;
			break;
			// IALOAD = 46
		case Constants.IALOAD:
			wcet = 4 + r;
			latency = r;
			break;
			// LALOAD = 47
		case Constants.LALOAD:
			wcet = 7+(2*r);
			latency = 2*r;
			break;
			// FALOAD = 48
		case Constants.FALOAD:
			wcet = 4 + r;
			latency = r;
			break;
			// DALOAD = 49
		case Constants.DALOAD:
			wcet = 7+(2*r);
			latency = 2*r;
			break;
			// AALOAD = 50
		case Constants.AALOAD:
			wcet = 4 + r;
			latency = r;
			break;
			// BALOAD = 51
		case Constants.BALOAD:
			wcet = 4 + r;
			latency = r;
			break;
			// CALOAD = 52
		case Constants.CALOAD:
			wcet = 4 + r;
			latency = r;
			break;
			// SALOAD = 53
		case Constants.SALOAD:
			wcet = 4 + r;
			latency = r;
			break;
			// ISTORE = 54
		case Constants.ISTORE:
			wcet = 1;
			break;
			// LSTORE = 55
		case Constants.LSTORE:
			wcet = 1;
			break;
			// FSTORE = 56
		case Constants.FSTORE:
			wcet = 1;
			break;
			// DSTORE = 57
		case Constants.DSTORE:
			wcet = 1;
			break;
			// ASTORE = 58
		case Constants.ASTORE:
			wcet = 2;
			break;
			// ISTORE_0 = 59
		case Constants.ISTORE_0:
			wcet = 1;
			break;
			// ISTORE_1 = 60
		case Constants.ISTORE_1:
			wcet = 1;
			break;
			// ISTORE_2 = 61
		case Constants.ISTORE_2:
			wcet = 1;
			break;
			// ISTORE_3 = 62
		case Constants.ISTORE_3:
			wcet = 1;
			break;
			// LSTORE_0 = 63
		case Constants.LSTORE_0:
			wcet = 1;
			break;
			// LSTORE_1 = 64
		case Constants.LSTORE_1:
			wcet = 1;
			break;
			// LSTORE_2 = 65
		case Constants.LSTORE_2:
			wcet = 1;
			break;
			// LSTORE_3 = 66
		case Constants.LSTORE_3:
			wcet = 1;
			break;
			// FSTORE_0 = 67
		case Constants.FSTORE_0:
			wcet = 1;
			break;
			// FSTORE_1 = 68
		case Constants.FSTORE_1:
			wcet = 1;
			break;
			// FSTORE_2 = 69
		case Constants.FSTORE_2:
			wcet = 1;
			break;
			// FSTORE_3 = 70
		case Constants.FSTORE_3:
			wcet = 1;
			break;
			// DSTORE_0 = 71
		case Constants.DSTORE_0:
			wcet = 1;
			break;
			// DSTORE_1 = 72
		case Constants.DSTORE_1:
			wcet = 1;
			break;
			// DSTORE_2 = 73
		case Constants.DSTORE_2:
			wcet = 1;
			break;
			// DSTORE_3 = 74
		case Constants.DSTORE_3:
			wcet = 1;
			break;
			// ASTORE_0 = 75
		case Constants.ASTORE_0:
			wcet = 2+w;
			latency = w;
			break;
			// ASTORE_1 = 76
		case Constants.ASTORE_1:
			wcet = 2+w;
			latency = w;
			break;
			// ASTORE_2 = 77
		case Constants.ASTORE_2:
			wcet = 2+w;
			latency = w;
			break;
			// ASTORE_3 = 78
		case Constants.ASTORE_3:
			wcet = 2+w;
			latency = w;
			break;
			// IASTORE = 79
		case Constants.IASTORE:
			wcet = 7+w;
			latency = w;
			break;
			// LASTORE = 80
		case Constants.LASTORE:
			wcet = 11+(2*w);
			latency = 2*w;
			break;
			// FASTORE = 81
		case Constants.FASTORE:
			wcet = 7+w;
			latency = w;
			break;
			// DASTORE = 82
		case Constants.DASTORE:
			wcet = 11+(2*w);
			latency = 2*w;
			break;
			// AASTORE = 83
		case Constants.AASTORE:
			wcet = 8+(2*w);
			latency = 2*w;
			break;
			// BASTORE = 84
		case Constants.BASTORE:
			wcet = 5+w;
			latency = w;
			break;
			// CASTORE = 85
		case Constants.CASTORE:
			wcet = 7+w;
			latency = w;
			break;
			// SASTORE = 86
		case Constants.SASTORE:
			wcet = 7+w;
			latency = w;
			break;
			// POP = 87
		case Constants.POP:
			wcet = 1;
			break;
			// POP2 = 88
		case Constants.POP2:
			wcet = 1;
			break;
			// DUP = 89
		case Constants.DUP:
			wcet = 1;
			break;
			// DUP_X1 = 90
		case Constants.DUP_X1:
			wcet = 4;
			break;
			// DUP_X2 = 91
		case Constants.DUP_X2:
			wcet = 6;
			break;
			// DUP2 = 92
		case Constants.DUP2:
			wcet = 1;
			break;
			// DUP2_X1 = 93
		case Constants.DUP2_X1:
			wcet = 5;
			break;
			// DUP2_X2 = 94
		case Constants.DUP2_X2:
			wcet = 6;
			break;
			// SWAP = 95
		case Constants.SWAP:
			wcet = -1; //todo
			break;
			// IADD = 96
		case Constants.IADD:
			wcet = 1;
			break;
			// LADD = 97
		case Constants.LADD:
			wcet = -1; //todo
			break;
			// FADD = 98
		case Constants.FADD:
			wcet = -1; //todo
			break;
			// DADD = 99
		case Constants.DADD:
			wcet = -1; //todo
			break;
			// ISUB = 100
		case Constants.ISUB:
			wcet = 1;
			break;
			// LSUB = 101
		case Constants.LSUB:
			wcet = -1; //todo
			break;
			// FSUB = 102
		case Constants.FSUB:
			wcet = -1; //todo
			break;
			// DSUB = 103
		case Constants.DSUB:
			wcet = -1; //todo
			break;
			// IMUL = 104
		case Constants.IMUL:
			wcet = 1;
			break;
			// LMUL = 105
		case Constants.LMUL:
			wcet = -1; //todo
			break;
			// FMUL = 106
		case Constants.FMUL:
			wcet = -1; // todo
			break;
			// DMUL = 107
		case Constants.DMUL:
			wcet = -1; // todo
			break;
			// IDIV = 108
		case Constants.IDIV:
			wcet = -1; // todo
			break;
			// LDIV = 109
		case Constants.LDIV:
			wcet = -1; // todo
			break;
			// FDIV = 110
		case Constants.FDIV:
			wcet = -1; // todo
			break;
			// DDIV = 111
		case Constants.DDIV:
			wcet = -1; // todo
			break;
			// IREM = 112
		case Constants.IREM:
			wcet = -1; // todo
			break;
			// LREM = 113
		case Constants.LREM:
			wcet = -1; // todo
			break;
			// FREM = 114
		case Constants.FREM:
			wcet = -1; // todo
			break;
			// DREM = 115
		case Constants.DREM:
			wcet = -1; // todo
			break;
			// INEG = 116
		case Constants.INEG:
			wcet = 1;
			break;
			// LNEG = 117
		case Constants.LNEG:
			wcet = -1; // todo
			break;
			// FNEG = 118
		case Constants.FNEG:
			wcet = -1; // todo
			break;
			// DNEG = 119
		case Constants.DNEG:
			wcet = -1; // todo
			break;
			// ISHL = 120
		case Constants.ISHL:
			wcet = 1;
			break;
			// LSHL = 121
		case Constants.LSHL:
			wcet = -1; // todo
			break;
			// ISHR = 122
		case Constants.ISHR:
			wcet = 1;
			break;
			// LSHR = 123
		case Constants.LSHR:
			wcet = -1; // todo
			break;
			// IUSHR = 124
		case Constants.IUSHR:
			wcet = 1;
			break;
			// LUSHR = 125
		case Constants.LUSHR:
			wcet = -1; // todo
			break;
			// IAND = 126
		case Constants.IAND:
			wcet = 1;
			break;
			// LAND = 127
		case Constants.LAND:
			wcet = -1; // todo
			break;
			// IOR = 128
		case Constants.IOR:
			wcet = 1;
			break;
			// LOR = 129
		case Constants.LOR:
			wcet = -1; // todo
			break;
			// IXOR = 130
		case Constants.IXOR:
			wcet = 1;
			break;
			// LXOR = 131
		case Constants.LXOR:
			wcet = -1; // todo
			break;
			// IINC = 132
		case Constants.IINC:
			wcet = 1;
			break;
			// I2L = 133
		case Constants.I2L:
			wcet = -1; // todo
			break;
			// I2F = 134
		case Constants.I2F:
			wcet = -1; // todo
			break;
			// I2D = 135
		case Constants.I2D:
			wcet = -1; // todo
			break;
			// L2I = 136
		case Constants.L2I:
			wcet = 1;
			break;
			// L2F = 137
		case Constants.L2F:
			wcet = -1; // todo
			break;
			// L2D = 138
		case Constants.L2D:
			wcet = -1; // todo
			break;
			// F2I = 139
		case Constants.F2I:
			wcet = -1; // todo
			break;
			// F2L = 140
		case Constants.F2L:
			wcet = -1; // todo
			break;
			// F2D = 141
		case Constants.F2D:
			wcet = -1; // todo
			break;
			// D2I = 142
		case Constants.D2I:
			wcet = -1; // todo
			break;
			// D2L = 143
		case Constants.D2L:
			wcet = -1; // todo
			break;
			// D2F = 144
		case Constants.D2F:
			wcet = -1; // todo
			break;
			// I2B = 145
		case Constants.I2B:
			wcet = 1;
			break;
			// INT2BYTE = 145 // Old notion
			// case Constants.INT2BYTE : wcet = -1; break;
			// I2C = 146
		case Constants.I2C:
			wcet = 1;
			break;
			// INT2CHAR = 146 // Old notion
			// case Constants.INT2CHAR : wcet = -1; break;
			// I2S = 147
		case Constants.I2S:
			wcet = 1;
			break;
			// INT2SHORT = 147 // Old notion
			// case Constants.INT2SHORT : wcet = -1; break;
			// LCMP = 148
		case Constants.LCMP:
			wcet = -1; // todo
			break;
			// FCMPL = 149
		case Constants.FCMPL:
			wcet = -1; // todo
			break;
			// FCMPG = 150
		case Constants.FCMPG:
			wcet = -1; // todo
			break;
			// DCMPL = 151
		case Constants.DCMPL:
			wcet = -1; // todo
			break;
			// DCMPG = 152
		case Constants.DCMPG:
			wcet = -1; // todo
			break;
			// IFEQ = 153
		case Constants.IFEQ:
			// - FIXME: No branchlatency for ifeq  ?
			wcet = 1+j;
			latency = j;
			break;
			// IFNE = 154
		case Constants.IFNE:
			wcet = 1+branchlatency;
			latency = branchlatency;
			break;
			// IFLT = 155
		case Constants.IFLT:
			wcet = 1+branchlatency;
			latency = branchlatency;
			break;
			// IFGE = 156
		case Constants.IFGE:
			wcet = 1+branchlatency;
			latency = branchlatency;
			break;
			// IFGT = 157
		case Constants.IFGT:
			wcet = 1+branchlatency;
			latency = branchlatency;
			break;
			// IFLE = 158
		case Constants.IFLE:
			wcet = 1+branchlatency;
			latency = branchlatency;
			break;
			// IF_ICMPEQ = 159
		case Constants.IF_ICMPEQ:
			wcet = 1+branchlatency;
			latency = branchlatency;
			break;
			// IF_ICMPNE = 160
		case Constants.IF_ICMPNE:
			wcet = 1+branchlatency;
			latency = branchlatency;
			break;
			// IF_ICMPLT = 161
		case Constants.IF_ICMPLT:
			wcet = 1+branchlatency;
			latency = branchlatency;
			break;
			// IF_ICMPGE = 162
		case Constants.IF_ICMPGE:
			wcet = 1+branchlatency;
			latency = branchlatency;
			break;
			// IF_ICMPGT = 163
		case Constants.IF_ICMPGT:
			wcet = 1+branchlatency;
			latency = branchlatency;
			break;
			// IF_ICMPLE = 164
		case Constants.IF_ICMPLE:
			wcet = 1+branchlatency;
			latency = branchlatency;
			break;
			// IF_ACMPEQ = 165
		case Constants.IF_ACMPEQ:
			wcet = 1+branchlatency;
			latency = branchlatency;
			break;
			// IF_ACMPNE = 166
		case Constants.IF_ACMPNE:
			wcet = 1+branchlatency;
			latency = branchlatency;
			break;
			// GOTO = 167
		case Constants.GOTO:
			wcet = 1+branchlatency;
			latency = branchlatency;
			break;
			// JSR = 168
		case Constants.JSR:
			wcet = 8+branchlatency-3;
			latency = branchlatency-3;
			break;
			// RET = 169
		case Constants.RET:
			wcet = 1+branchlatency;
			latency = branchlatency;
			break;
			// TABLESWITCH = 170
		case Constants.TABLESWITCH:
			wcet = -1; // TODO
			break;
			// LOOKUPSWITCH = 171
		case Constants.LOOKUPSWITCH:
			wcet = -1; // TODO
			break;
			// IRETURN = 172
		case Constants.IRETURN:
			wcet = 7+branchlatency-6;
			latency = 2+branchlatency-6;
			break;
			// LRETURN = 173
		case Constants.LRETURN:
			wcet = 9;
			latency = 2;
			break;
			// FRETURN = 174
		case Constants.FRETURN:
			wcet = 7+branchlatency-6;
			latency = 2+branchlatency-6;
			break;
			// DRETURN = 175
		case Constants.DRETURN:
			wcet = 9;
			latency = 2;
			break;
			// ARETURN = 176
		case Constants.ARETURN:
			wcet = 10+w;
			latency = 2;
			break;
			// RETURN = 177
		case Constants.RETURN:
			wcet = 6+branchlatency-6;
			latency = 2;
			break;
			// GETSTATIC = 178
		case Constants.GETSTATIC:
			wcet = 1 + r;
			latency = r;
			break;
			// PUTSTATIC = 179
		case Constants.PUTSTATIC:
			wcet = 4+(2*w);
			latency = w;
			break;
			// GETFIELD = 180
		case Constants.GETFIELD:
			wcet = 1 + r;
			latency = r;
			break;
			// PUTFIELD = 181
		case Constants.PUTFIELD:
			wcet = 5+(2*w);
			latency = 2*w;
			break;
			// INVOKEVIRTUAL = 182
		case Constants.INVOKEVIRTUAL:
			wcet = 58 + 18 + (3*branchlatency) + (5*r) + t;
			latency = 18 + (3*branchlatency) + (5*r);
			break;
			// INVOKESPECIAL = 183
		case Constants.INVOKESPECIAL:
			wcet = 46 + 14 + (2*branchlatency) + (5*r) + t;
			latency = 14 + (2*branchlatency) + (5*r);
			break;
			// INVOKENONVIRTUAL = 183
			// case Constants.INVOKENONVIRTUAL : wcet = -1; break;
			// INVOKESTATIC = 184
		case Constants.INVOKESTATIC:
			wcet = 46 + 14 + (2*branchlatency) + (5*r) + t;
			latency = 14 + (2*branchlatency) + (5*r);
			break;
			// INVOKEINTERFACE = 185
		case Constants.INVOKEINTERFACE:
			wcet = -1; //112 + 4 * r; //TODO
			break;
			// NEW = 187
		case Constants.NEW:
			wcet = -1;
			break;
			// NEWARRAY = 188
		case Constants.NEWARRAY:
			wcet = -1;
			break;
			// ANEWARRAY = 189
		case Constants.ANEWARRAY:
			wcet = -1;
			break;
			// ARRAYLENGTH = 190
		case Constants.ARRAYLENGTH:
			wcet = 1+r;
			break;
			// ATHROW = 191
		case Constants.ATHROW:
			wcet = -1;
			break;
			// CHECKCAST = 192
		case Constants.CHECKCAST:
			wcet = -1; //TODO
			break;
			// INSTANCEOF = 193
		case Constants.INSTANCEOF:
			wcet = -1; //TODO
			break;
			// MONITORENTER = 194
		case Constants.MONITORENTER:
			wcet = -1; //TODO
			break;
			// MONITOREXIT = 195
		case Constants.MONITOREXIT:
			wcet = -1; //TODO
			break;
			// WIDE = 196
		case Constants.WIDE:
			wcet = -1;
			break;
			// MULTIANEWARRAY = 197
		case Constants.MULTIANEWARRAY:
			wcet = -1;
			break;
			// IFNULL = 198
		case Constants.IFNULL:
			wcet = 1+branchlatency;
			latency = branchlatency;
			break;
			// IFNONNULL = 199
		case Constants.IFNONNULL:
			wcet = 1+j;
			latency = j;
			break;
			// GOTO_W = 200
		case Constants.GOTO_W:
			wcet = -1; //TODO
			break;
			// JSR_W = 201
		case Constants.JSR_W:
			wcet = -1; //TODO
			break;
			// JOPSYS_RD = 209   

		default:
			wcet = -1;
		}
		if(latency<0) latency=0;
		return wcet;
	}

	// print timing table
	public static void main(String[] argv) {
		String head = "Jamuth Timing Table on " + new Date();
		System.out.println(head);
		System.out.println(ConsoleTable.getSepLine('=',head.length()));
		System.out.println();
		
		ConsoleTable table = new ConsoleTable();
		JamuthTimingTable timing = new JamuthTimingTable();

		table.addLegendTop(String.format("r = %d, w = %d, c = %d, fcs = %d, fca = %d",
				timing.r,timing.w,timing.c,timing.fcs,timing.fca));
		table.addLegendTop(String.format("rs = %d, t = %d, j = %d, fls = %d, fla = %d",
				timing.rs,timing.t,timing.j,timing.fls,timing.fla));
				
		// build table
		table.addColumn("opcode", Alignment.ALIGN_RIGHT)
		     .addColumn("name", Alignment.ALIGN_LEFT)
		     .addColumn("Cyc (&jump = 4)", Alignment.ALIGN_RIGHT)
		     .addColumn("Cyc (&jump = 1)", Alignment.ALIGN_RIGHT);
		for(int i = 0; i < 256; i++) {
			int opcode = i;
			if(JopInstr.isReserved(opcode)) continue;
			TableRow row = table.addRow();
			row.addCell(opcode)
			   .addCell(JopInstr.OPCODE_NAMES[i]);
			if(timing.hasTimingInfo(opcode)) {
				long t1 = timing.getCycles(new JamuthInstructionInfo(opcode,4));
				long t2 = timing.getCycles(new JamuthInstructionInfo(opcode,1));
				row.addCell(t1);
				row.addCell(t2);
			} else {
				row.addCell("... no info ...",2,Alignment.ALIGN_LEFT);
			}
		}
		System.out.println(table.render());
	}


}
