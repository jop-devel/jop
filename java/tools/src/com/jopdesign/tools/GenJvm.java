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
