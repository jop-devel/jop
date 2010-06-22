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

	enum StackType {
		PUSH, POP, NOP
	};
	
	enum JmpType {
		BR, JMP, NOP
	};

	public String name;
	public int opcode;
	public int opdSize;
	public JmpType jType;
	StackType sType;

	/** Length of instruction without opd and nxt */
	final static int INSTLEN = 10;

	private Instruction(String s, int oc, int ops, JmpType jp, StackType st) {
		name = s;
		opcode = oc;
		opdSize = ops;
		jType = jp;
		sType = st;
	}

	public String toString() {
		return name;
	}

	public boolean isStackConsumer() {
		return sType == StackType.POP;
	}

	public boolean isStackProducer() {
		return sType == StackType.PUSH;
	}

	public boolean noStackUse() {
		return sType == StackType.NOP;
	}

	private static Instruction[] ia = new Instruction[] {

			//
			// 'pop' instructions
			//

			new Instruction("pop", 0x000, 0, JmpType.NOP, StackType.POP),
			new Instruction("and", 0x001, 0, JmpType.NOP, StackType.POP),
			new Instruction("or",  0x002, 0, JmpType.NOP, StackType.POP),
			new Instruction("xor", 0x003, 0, JmpType.NOP, StackType.POP),
			new Instruction("add", 0x004, 0, JmpType.NOP, StackType.POP),
			new Instruction("sub", 0x005, 0, JmpType.NOP, StackType.POP),

			// st (vp) 3 bits
			new Instruction("st0", 0x010 + 0, 0, JmpType.NOP, StackType.POP),
			new Instruction("st1", 0x010 + 1, 0, JmpType.NOP, StackType.POP),
			new Instruction("st2", 0x010 + 2, 0, JmpType.NOP, StackType.POP),
			new Instruction("st3", 0x010 + 3, 0, JmpType.NOP, StackType.POP),
			new Instruction("st",  0x010 + 4, 0, JmpType.NOP, StackType.POP),
			new Instruction("stmi", 0x010 + 5, 0, JmpType.NOP, StackType.POP),

			new Instruction("stvp", 0x018, 0, JmpType.NOP, StackType.POP),
			new Instruction("stjpc", 0x019, 0, JmpType.NOP, StackType.POP),
			new Instruction("star", 0x01a, 0, JmpType.NOP, StackType.POP),
			new Instruction("stsp", 0x01b, 0, JmpType.NOP, StackType.POP),

			// shift
			new Instruction("ushr", 0x01c, 0, JmpType.NOP, StackType.POP),
			new Instruction("shl", 0x01d, 0, JmpType.NOP, StackType.POP),
			new Instruction("shr", 0x01e, 0, JmpType.NOP, StackType.POP),
			// new Instruction("shift reserved", 0x1f, 0, JmpType.NOP, StackType.POP),

			// 5 bits: 0x20-0x3f
			new Instruction("stm", 0x020, 5, JmpType.NOP, StackType.POP),

			// MMU 4 bit subfield selects function (pop type)
			new Instruction("stmul", 0x040 + 0, 0, JmpType.NOP, StackType.POP),
			new Instruction("stmwa", 0x040 + 1, 0, JmpType.NOP, StackType.POP),
			new Instruction("stmra", 0x040 + 2, 0, JmpType.NOP, StackType.POP),
			new Instruction("stmwd", 0x040 + 3, 0, JmpType.NOP, StackType.POP),
			// array instructions
			new Instruction("stald", 0x040 + 4, 0, JmpType.NOP, StackType.POP),
			new Instruction("stast", 0x040 + 5, 0, JmpType.NOP, StackType.POP),
			// getfield/putfield
			new Instruction("stgf",  0x040 + 6, 0, JmpType.NOP, StackType.POP),
			new Instruction("stpf",  0x040 + 7, 0, JmpType.NOP, StackType.POP),
			// magic copying
			new Instruction("stcp",  0x040 + 8, 0, JmpType.NOP, StackType.POP),
			// bytecode read
			new Instruction("stbcrd", 0x040 + 9, 0, JmpType.NOP, StackType.POP),
			// store TOS to MMU index register for jopsys_*field
			new Instruction("stidx", 0x040 + 0xa, 0, JmpType.NOP, StackType.POP),
			// putstatic
			new Instruction("stps", 0x040 + 0xb, 0, JmpType.NOP, StackType.POP),
			// read constants through cache
			new Instruction("stmrac", 0x040 + 0xc, 0, JmpType.NOP, StackType.POP),
			// read through fully associative cache
			new Instruction("stmraf", 0x040 + 0xd, 0, JmpType.NOP, StackType.POP),

			//
			// 'push' instructions
			//

			// 5 bits: 0xa0-0xbf
			new Instruction("ldm", 0x0a0, 5, JmpType.NOP, StackType.PUSH),
			// 5 bits: 0xc0-0xdf
			new Instruction("ldi", 0x0c0, 5, JmpType.NOP, StackType.PUSH),

			// MMU 4 bit subfield selects function (push type)
			new Instruction("ldmrd", 0x0e0 + 0, 0, JmpType.NOP, StackType.PUSH),
			new Instruction("ldmul", 0x0e0 + 1, 0, JmpType.NOP, StackType.PUSH),
			new Instruction("ldbcstart", 0x0e0 + 2, 0, JmpType.NOP, StackType.PUSH),

			// ld (vp) 3 bits
			new Instruction("ld0", 0x0e8 + 0, 0, JmpType.NOP, StackType.PUSH),
			new Instruction("ld1", 0x0e8 + 1, 0, JmpType.NOP, StackType.PUSH),
			new Instruction("ld2", 0x0e8 + 2, 0, JmpType.NOP, StackType.PUSH),
			new Instruction("ld3", 0x0e8 + 3, 0, JmpType.NOP, StackType.PUSH),
			new Instruction("ld", 0x0e8 + 4, 0, JmpType.NOP, StackType.PUSH),
			new Instruction("ldmi", 0x0e8 + 5, 0, JmpType.NOP, StackType.PUSH),

			// 2 bits
			new Instruction("ldsp", 0x0f0 + 0, 0, JmpType.NOP, StackType.PUSH),
			new Instruction("ldvp", 0x0f0 + 1, 0, JmpType.NOP, StackType.PUSH),
			new Instruction("ldjpc", 0x0f0 + 2, 0, JmpType.NOP, StackType.PUSH),

			// ld opd 2 bits
			new Instruction("ld_opd_8u", 0x0f4 + 0, 0, JmpType.NOP, StackType.PUSH),
			new Instruction("ld_opd_8s", 0x0f4 + 1, 0, JmpType.NOP, StackType.PUSH),
			new Instruction("ld_opd_16u", 0x0f4 + 2, 0, JmpType.NOP, StackType.PUSH),
			new Instruction("ld_opd_16s", 0x0f4 + 3, 0, JmpType.NOP, StackType.PUSH),

			new Instruction("dup", 0x0f8, 0, JmpType.NOP, StackType.PUSH),

			//
			// 'no SP change' instructions
			//
			new Instruction("nop", 0x100, 0, JmpType.NOP, StackType.NOP),
			new Instruction("wait", 0x101, 0, JmpType.NOP, StackType.NOP),
			new Instruction("jbr", 0x102, 0, JmpType.NOP, StackType.NOP),
			
			// no SP change instructions for MMU
			// 4 bit subfield selects function (pop type)

			// getstatic
			new Instruction("stgs", 0x110 + 0x0, 0, JmpType.NOP, StackType.NOP),


			// branches
			new Instruction("bz", 0x180, 6, JmpType.BR, StackType.POP),
			new Instruction("bnz", 0x1c0, 6, JmpType.BR, StackType.POP),
			new Instruction("jmp", 0x200, 9, JmpType.JMP, StackType.NOP),
		};

	public static Map<String, Instruction> map = new HashMap<String, Instruction>();
	public static Map<Integer, Instruction> imap = new TreeMap<Integer, Instruction>();

	static {
		for (int i = 0; i < ia.length; ++i) {
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

		for (int i = 0; i < ia.length; ++i) {
			Instruction ins = ia[i];

			System.out.print("\t\t\twhen \"");
			System.out.print(Jopa.bin(ins.opcode >>> ins.opdSize, INSTLEN
					- ins.opdSize));
			for (int j = 0; j < ins.opdSize; ++j) {
				System.out.print("-");
			}
			System.out.print("\" =>\t\t\t-- ");
			System.out.print(ins.name);
			System.out.println();
		}

		System.out.println();
		// we assume that the stack type is encoded in the upper
		// 4 bits
		StackType st[] = new StackType[16];
		for (int i = 0; i < ia.length; ++i) {
			Instruction ins = ia[i];

			int idx = ins.opcode >>> (INSTLEN - 4);
			if (st[idx] == null) {
				st[idx] = ins.sType;
			} else if (st[idx] != ins.sType) {
				throw new Error("Conflicting stack types: " + ins.name);
			}
		}
		for (int i = 0; i < 16; ++i) {
			System.out.print("\t\twhen \"");
			System.out.print(Jopa.bin(i, 4));
			System.out.print("\" =>\t\t\t-- " + st[i]);
			System.out.println();
			if (st[i]==StackType.PUSH) {
				System.out.println("\t\t\t\tis_push <= '1';");
			}
			if (st[i]==StackType.POP) {
				System.out.println("\t\t\t\tis_pop <= '1';");
			}
		}
	}

	public static void printCsv() {

		for (int i = 0; i < ia.length; ++i) {
			Instruction ins = ia[i];

			System.out.print(ins.name);
			System.out.print(";;{");
			System.out.print(Jopa.bin(ins.opcode >>> ins.opdSize, INSTLEN
					- ins.opdSize));
			for (int j = 0; j < ins.opdSize; ++j) {
				System.out.print("-");
			}
			System.out.println("}");
		}
	}

	public static void printTable() {

		Instruction table[] = new Instruction[1024];
		for (int i = 0; i < 1024; i++)
			table[i] = null;
		for (int i = 0; i < ia.length; ++i) {
			Instruction ins = ia[i];
			int up = 1 << ins.opdSize;
			for (int j = 0; j < up; j++) {
				int code = ins.opcode | j;
				if (table[code] != null) {
					System.err.println("Two entries for: " + code + " : " + ins
							+ " and " + table[code]);
					System.exit(1);
				} else
					table[code] = ins;
			}
		}
		for (int i = 0; i < 256; i++) {
			System.out.print(String.format("0x%02x ", i));
			if (table[i] == null)
				System.out.print("---");
			else {
				Instruction ins = table[i];
				System.out.print(ins);
				if (ins.opdSize != 0)
					System.out.print(" " + (i & ((1 << ins.opdSize) - 1)));
				if (ins.isStackConsumer())
					System.out.print(" [-]");
				else if (ins.isStackProducer())
					System.out.print(" [+]");
			}
			System.out.println("");
		}
	}

	public static String genJavaConstants() {

		StringBuffer sb = new StringBuffer();
		sb.append("package com.jopdesign.timing.jop;\n");
		sb.append("public class MicrocodeConstants {\n");
		for (Instruction i : ia) {
			sb.append(String.format(
					"  public static final int %-15s = 0x%x; /* %s %s%s*/ \n",
					i.name.toUpperCase(), i.opcode,
					i.isStackConsumer() ? "consumer"
							: (i.isStackProducer() ? "producer" : "nostack"),
					i.opdSize != 0 ? "opd MS: not to confuse it with opd in mc"
							: "", i.jType==JmpType.BR || i.jType==JmpType.JMP ? "jmp " : ""));
		}
		sb.append("};");
		return sb.toString();
	}
	
	/**
	 * Reserved VHDL keywords
	 */
	static String[] reserved = {
		"and", "or", "xor", "wait"
	};
	static String substReserved(String s) {
		
		for (int i=0; i<reserved.length; ++i) {
			if (reserved[i].equals(s)) {
				return s+"_x";
			}
		}
		return s;
	}
	/**
	 * Print the VHDL code for microcode mnemonics in the simulation.
	 */
	public static void printVhdlMicrocode() {
		
		System.out.println("\ttype mcval is (");
		for (int i = 0; i < ia.length; ++i) {
			Instruction ins = ia[i];
			System.out.println("\t\t"+substReserved(ins.name)+",");
		}
		System.out.println("\t\tunknown");

		System.out.println("\t);");
		System.out.println("\tsignal val : mcval;");

		System.out.println("");
		System.out.println("");
		System.out.println("begin");
		System.out.println("");
		System.out.println("process(instr)");
		System.out.println("begin");
		System.out.println("");
		System.out.println("\tval <= unknown;");
		System.out.println("\tif instr(9)='1' then");
		System.out.println("\t\tval <= jmp;");
		System.out.println("\telsif instr(9 downto 6)=\"0110\" then");
		System.out.println("\t\tval <= bz;");
		System.out.println("\telsif instr(9 downto 6)=\"0111\" then");
		System.out.println("\t\tval <= bnz;");
		System.out.println("\telse");
		System.out.println("\t\tcase instr is");
		for (int i = 0; i < ia.length; ++i) {
			Instruction ins = ia[i];
			if (ins.opdSize>=6) {
				continue;			// jmp, bz, and bnz
			}
			for (int j=0; j<1<<ins.opdSize; ++j) {
				System.out.print("\t\t\twhen \"");
				System.out.print(Jopa.bin(ins.opcode+j, INSTLEN));
				System.out.print("\" => val <= ");
				System.out.println(substReserved(ins.name)+";");
			}
		}

		System.out.println("");
		System.out.println("\t\t\twhen others => null;");
		System.out.println("\t\tend case;");
		System.out.println("\tend if;");
		System.out.println("");
		System.out.println("end process;");
		System.out.println("");
	}

	public static void main(String[] args) {

		// printVhdl();
		// printCsv();
		// printTable();
		// System.out.println(genJavaConstants());
		printVhdlMicrocode();
	}

}
