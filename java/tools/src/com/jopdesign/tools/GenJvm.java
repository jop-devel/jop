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
*	Generate JVM.java for JVM functions in Java.
*/


package com.jopdesign.tools;

import java.util.*;

public class GenJvm{

	public static void main(String[] args) {

		System.out.println("package com.jopdesign.sys;");
		System.out.println();
		System.out.println("class JVM {");
		System.out.println();

		for (int i=1; i<256; ++i) {

			System.out.print("\tprivate static void ");
			System.out.print("f_"+JopInstr.name(i));
			System.out.print("() { ");
			int imp = JopInstr.imp(i);
			if (imp==JopInstr.IMP_ASM) {
				System.out.print("JVMHelp.noim(); /* jvm.asm */ ");
			} else if (imp==JopInstr.IMP_JAVA) {
				System.out.println();
				System.out.println("\t\t//\tTODO: implement ");
				System.out.print("\t");
			} else if (imp==JopInstr.IMP_NO) {
				System.out.print("JVMHelp.noim();");
			}
			System.out.println("}");
		}

		System.out.println("}");
	}
}
