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

package com.jopdesign.tools;


import java.io.Serializable;
import java.util.*;

public class Instruction implements Serializable {
	private static final long serialVersionUID = 1L;
	
	enum StackType {PUSH, POP, NOP};

	public String name;
	public int opcode;
	public int opdSize;
	public boolean isJmp;
	StackType sType;

	/** Length of instruction without opd and nxt */
	final static int INSTLEN = 8;

	private Instruction(String s, int oc, int ops, boolean jp, StackType st) {
		name = s;
		opcode = oc;
		opdSize = ops;
		isJmp = jp;
		sType = st;
	}

	public String toString() {
		return name;
	}
	
	public boolean isStackConsumer() {
		return sType==StackType.POP;
	}
	
	public boolean isStackProducer() {
		return sType==StackType.PUSH;
	}

	public boolean noStackUse() {
		return sType==StackType.NOP;
	}


	private static Instruction[] ia = new Instruction[] {

//
//	'pop' instructions
//
		new Instruction("pop", 0x00, 0, false, StackType.POP),
		new Instruction("and", 0x01, 0, false, StackType.POP),
		new Instruction("or",  0x02, 0, false, StackType.POP),
		new Instruction("xor", 0x03, 0, false, StackType.POP),
		new Instruction("add", 0x04, 0, false, StackType.POP),
		new Instruction("sub", 0x05, 0, false, StackType.POP),

//	extension 'address' selects function 4 bits

		// multiplication
		new Instruction("stmul", 0x06, 0, false, StackType.POP),

		new Instruction("stmwa", 0x07, 0, false, StackType.POP),

		new Instruction("stmra", 0x08+0, 0, false, StackType.POP),
		new Instruction("stmwd", 0x08+1, 0, false, StackType.POP),
		// array instructions
		new Instruction("stald", 0x08+2, 0, false, StackType.POP),
		new Instruction("stast", 0x08+3, 0, false, StackType.POP),
		// getfield/putfield
		new Instruction("stgf",  0x08+4, 0, false, StackType.POP),
		new Instruction("stpf",  0x08+5, 0, false, StackType.POP),
		// magic copying
		new Instruction("stcp",  0x08+6, 0, false, StackType.POP),
		// bytecode read
		new Instruction("stbcrd",0x08+7, 0, false, StackType.POP),

//	st (vp)	3 bits
		new Instruction("st0",   0x10+0, 0, false, StackType.POP),
		new Instruction("st1",   0x10+1, 0, false, StackType.POP),
		new Instruction("st2",   0x10+2, 0, false, StackType.POP),
		new Instruction("st3",   0x10+3, 0, false, StackType.POP),
		new Instruction("st",    0x10+4, 0, false, StackType.POP),
		new Instruction("stmi",  0x10+5, 0, false, StackType.POP),

		new Instruction("stvp",  0x18, 0, false, StackType.POP),
		new Instruction("stjpc", 0x19, 0, false, StackType.POP),
		new Instruction("star",  0x1a, 0, false, StackType.POP),
		new Instruction("stsp",  0x1b, 0, false, StackType.POP),

//	shift
		new Instruction("ushr", 0x1c, 0, false, StackType.POP),
		new Instruction("shl", 0x1d, 0, false, StackType.POP),
		new Instruction("shr", 0x1e, 0, false, StackType.POP),
		//new Instruction("shift reserved", 0x1f, 0, false, StackType.POP),

//	5 bits
		new Instruction("stm", 0x20, 5, false, StackType.POP),

		new Instruction("bz", 0x40, 5, true, StackType.POP),
		new Instruction("bnz", 0x60, 5, true, StackType.POP),
//
//	'no sp change' instructions
//
		new Instruction("nop", 0x80, 0, false, StackType.NOP),
		new Instruction("wait", 0x81, 0, false, StackType.NOP),

		new Instruction("jbr", 0x82, 0, false, StackType.NOP),

//
//	'push' instructions
//

//	5 bits
		new Instruction("ldm", 0xa0, 5, false, StackType.PUSH),

		new Instruction("ldi", 0xc0, 5, false, StackType.PUSH),

//		extension 'address' selects function 4 bits
		new Instruction("ldmrd", 0xe0+0, 0, false, StackType.PUSH),
		new Instruction("ldmul", 0xe0+6, 0, false, StackType.PUSH),
		new Instruction("ldbcstart", 0xe0+7, 0, false, StackType.PUSH),

//	ld (vp)	3 bits
		new Instruction("ld0", 0xe8+0, 0, false, StackType.PUSH),
		new Instruction("ld1", 0xe8+1, 0, false, StackType.PUSH),
		new Instruction("ld2", 0xe8+2, 0, false, StackType.PUSH),
		new Instruction("ld3", 0xe8+3, 0, false, StackType.PUSH),
		new Instruction("ld",  0xe8+4, 0, false, StackType.PUSH),
		new Instruction("ldmi",  0xe8+5, 0, false, StackType.PUSH),

//	2 bits
		new Instruction("ldsp", 0xf0+0, 0, false, StackType.PUSH),
		new Instruction("ldvp", 0xf0+1, 0, false, StackType.PUSH),
		new Instruction("ldjpc", 0xf0+2, 0, false, StackType.PUSH),

//	ld opd 2 bits
		new Instruction("ld_opd_8u", 0xf4+0, 0, false, StackType.PUSH),
		new Instruction("ld_opd_8s", 0xf4+1, 0, false, StackType.PUSH),
		new Instruction("ld_opd_16u", 0xf4+2, 0, false, StackType.PUSH),
		new Instruction("ld_opd_16s", 0xf4+3, 0, false, StackType.PUSH),

		new Instruction("dup", 0xf8, 0, false, StackType.PUSH),
	};

	public static Map<String,Instruction> map = new HashMap<String,Instruction>();
	public static Map<Integer,Instruction> imap = new TreeMap<Integer,Instruction>();

	static {
		for (int i=0; i<ia.length; ++i) {
			map.put(ia[i].name, ia[i]);
			imap.put(ia[i].opcode, ia[i]);
		}
	}

	public static Instruction get(String name) {
		return map.get(name);
	}

	public static Instruction get(int opcode) {
		return imap.get(opcode);
	}

	public static void printVhdl() {

		for (int i=0; i<ia.length; ++i) {
			Instruction ins = ia[i];

			System.out.print("\t\t\twhen \"");
			System.out.print(Jopa.bin(ins.opcode>>>ins.opdSize, INSTLEN-ins.opdSize));
			for (int j=0; j<ins.opdSize; ++j) {
				System.out.print("-");
			}
			System.out.print("\" =>\t\t\t\t-- ");
			System.out.print(ins.name);
			System.out.println();
		}
	}

	public static void printCsv() {

		for (int i=0; i<ia.length; ++i) {
			Instruction ins = ia[i];

			System.out.print(ins.name);
			System.out.print(";;{");
			System.out.print(Jopa.bin(ins.opcode>>>ins.opdSize, INSTLEN-ins.opdSize));
			for (int j=0; j<ins.opdSize; ++j) {
				System.out.print("-");
			}
			System.out.println("}");
		}
	}

	public static void printTable() {
		
		Instruction table[] = new Instruction[256];
		for(int i = 0; i < 256; i++) table[i] = null;
		for (int i=0; i<ia.length; ++i) {
			Instruction ins = ia[i];
			int up = 1<<ins.opdSize;
			for(int j = 0; j < up; j++) {
					int code = ins.opcode | j;
					if(table[code] != null) {
						System.err.println("Two entries for: "+code+" : "+ins+" and "+table[code]);
						System.exit(1);
					}
					else table[code] = ins;
			}
		}
		for(int i = 0; i < 256; i++) {
			System.out.print(String.format("0x%02x ",i));
			if(table[i] == null) System.out.print("---");
			else {
				Instruction ins = table[i];
				System.out.print(ins);
				if(ins.opdSize!=0) System.out.print(" "+(i&((1<<ins.opdSize)-1)));
				if(ins.isStackConsumer()) System.out.print(" [-]");
				else if(ins.isStackProducer()) System.out.print(" [+]");				
			}
			System.out.println("");
		}
	}

	public static String genJavaConstants() {
		
		StringBuffer sb = new StringBuffer();
		sb.append("package com.jopdesign.timing;\n");
		sb.append("public class MicrocodeConstants {\n");
		for(Instruction i : ia) {
			sb.append(String.format("  public static final int %-15s = 0x%x; /* %s %s%s*/ \n",
									i.name.toUpperCase(),
									i.opcode,
									i.isStackConsumer() 
									  ? "consumer"
									  : (i.isStackProducer() ? "producer" : "nostack"),
									i.opdSize!=0 ? "opd MS: not to confuse it with opd in mc" : "",
									i.isJmp  ? "jmp " : ""));
		}
		sb.append("};");				
		return sb.toString();
	}
	
	public static void main(String[] args) {

		// printVhdl();
		printCsv();
		// printTable();
		// System.out.println(genJavaConstants());
	}

}
		
