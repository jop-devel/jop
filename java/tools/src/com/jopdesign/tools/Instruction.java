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


import java.util.*;

public class Instruction {

	String name;
	public int opcode;
	public boolean hasOpd;
	public boolean isJmp;

	final static int INSTLEN = 16;

	private Instruction(String s, int oc, boolean opd, boolean jp) {
		name = s;
		opcode = oc;
		hasOpd = opd;
		isJmp = jp;
	}

	public String toString() {
		return name;
	}

	private static Instruction[] ia = new Instruction[] 
	{

//
//	'pop' instructions
//
		new Instruction("pop", 0x00, false, false),
		new Instruction("and", 0x01, false, false),
		new Instruction("or",  0x02, false, false),
		new Instruction("xor", 0x03, false, false),
		new Instruction("add", 0x04, false, false),
		new Instruction("sub", 0x05, false, false),

//	extension 'address' selects function 4 bits

		// multiplication
		new Instruction("stmul", 0x06, false, false),

		new Instruction("stmwa", 0x07, false, false),

		new Instruction("stmra", 0x08+0, false, false),
		new Instruction("stmwd", 0x08+1, false, false),
		// array instructions
		new Instruction("stald", 0x08+2, false, false),
		new Instruction("stast", 0x08+3, false, false),
		// getfield/putfield
		new Instruction("stgf",  0x08+4, false, false),
		new Instruction("stpf",  0x08+5, false, false),
		// magic copying
		new Instruction("stcp",  0x08+6, false, false),
		// bytecode read
		new Instruction("stbcrd",0x08+7, false, false),

//	st (vp)	3 bits
		new Instruction("st0",   0x10+0, false, false),
		new Instruction("st1",   0x10+1, false, false),
		new Instruction("st2",   0x10+2, false, false),
		new Instruction("st3",   0x10+3, false, false),
		new Instruction("st",    0x10+4, false, false),
		new Instruction("stmi",  0x10+5, false, false),

		new Instruction("stvp",  0x18, false, false),
		new Instruction("stjpc", 0x19, false, false),
		new Instruction("star",  0x1a, false, false),
		new Instruction("stsp",  0x1b, false, false),

//	shift
		new Instruction("ushr", 0x1c, false, false),
		new Instruction("shl", 0x1d, false, false),
		new Instruction("shr", 0x1e, false, false),
		//new Instruction("shift reserved", 0x1f, false, false),

//	5 bits
		new Instruction("stm", 0x20, true, false),

		new Instruction("bz", 0x40, true, true),
		new Instruction("bnz", 0x60, true, true),
//
//	'no sp change' instructions
//
		new Instruction("nop", 0x80, false, false),
		new Instruction("wait", 0x81, false, false),

		new Instruction("jbr", 0x82, false, false),

//
//	'push' instructions
//

//	5 bits
		new Instruction("ldm", 0xa0, true, false),

		new Instruction("ldi", 0xc0, true, false),

//		extension 'address' selects function 4 bits
		new Instruction("ldmrd", 0xe0+0, false, false),
		new Instruction("ldmul", 0xe0+6, false, false),
		new Instruction("ldbcstart", 0xe0+7, false, false),

//	ld (vp)	3 bits
		new Instruction("ld0", 0xe8+0, false, false),
		new Instruction("ld1", 0xe8+1, false, false),
		new Instruction("ld2", 0xe8+2, false, false),
		new Instruction("ld3", 0xe8+3, false, false),
		new Instruction("ld",  0xe8+4, false, false),
		new Instruction("ldmi",  0xe8+5, false, false),

//	2 bits
		new Instruction("ldsp", 0xf0+0, false, false),
		new Instruction("ldvp", 0xf0+1, false, false),
		new Instruction("ldjpc", 0xf0+2, false, false),

//	ld opd 2 bits
		new Instruction("ld_opd_8u", 0xf4+0, false, false),
		new Instruction("ld_opd_8s", 0xf4+1, false, false),
		new Instruction("ld_opd_16u", 0xf4+2, false, false),
		new Instruction("ld_opd_16s", 0xf4+3, false, false),

		new Instruction("dup", 0xf8, false, false),
	};

	public static Map map = new HashMap();

	static {
		for (int i=0; i<ia.length; ++i) {
			map.put(ia[i].name, ia[i]);
		}
	}

	public static Instruction get(String s) {

		return (Instruction) map.get(s);
	}

	public static void printVhdl() {

		for (int i=0; i<ia.length; ++i) {
			Instruction ins = ia[i];

			System.out.print("\t\t\twhen \"");
			if (ins.hasOpd) {
				System.out.print(Jopa.bin(ins.opcode>>>5, 3));
				System.out.print("-----");
			} else {
				System.out.print(Jopa.bin(ins.opcode, 8));
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
			if (ins.hasOpd) {
				System.out.print(Jopa.bin(ins.opcode>>>5, 3));
				System.out.print("-----");
			} else {
				System.out.print(Jopa.bin(ins.opcode, 8));
			}
			System.out.println("}");
		}
	}


	public static void main(String[] args) {

		// printVhdl();
		printCsv();
	}

}
		
