
/**
*	jop2dat.java
*
*	Convert file.jop to memory initialization for VHDL simulation.
*
*/

package com.jopdesign.tools;

import java.io.*;
import java.util.*;
import com.jopdesign.sys.*;

public class jop2dat {

	static final int MAX_MEM = 128*1024/4;
	int[] mem = new int[MAX_MEM];
	int heap;

	jop2dat(String fn) {
		convert(fn);
	}

	void convert(String fn) {

		heap = 0;

		try {
			StreamTokenizer in = new StreamTokenizer(new FileReader(fn));

			in.wordChars( '_', '_' );
			in.wordChars( ':', ':' );
			in.eolIsSignificant(true);
			in.slashStarComments(true);
			in.slashSlashComments(true);
			in.lowerCaseMode(true);

			
			while (in.nextToken()!=StreamTokenizer.TT_EOF) {
				if (in.ttype == StreamTokenizer.TT_NUMBER) {
					mem[heap++] = (int) in.nval;
				}
			}


			int instr = mem[0];
			System.out.println("Program: "+fn);
			System.out.println(instr + " instruction word ("+(instr*4/1024)+" KB)");
			System.out.println(heap + " words mem read ("+(heap*4/1024)+" KB)");

			PrintStream ram_mem = new PrintStream(new FileOutputStream("mem_main.dat"));
			for (int i=0; i<heap; ++i) {
				ram_mem.println(mem[i]+" ");
			}
			ram_mem.close();

		} catch (IOException e) {
			System.out.println(e.getMessage());
			System.exit(-1);
		}
	}


	public static void main(String args[]) {

		jop2dat js = null;
		if (args.length==1) {
			js = new jop2dat(args[0]);
		} else {
			System.out.println("usage: java jop2dat file.jop");
			System.exit(-1);
		}

	}
}
