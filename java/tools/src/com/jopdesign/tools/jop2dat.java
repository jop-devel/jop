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

	static final int MAX_MEM = 1024*1024/4;
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
