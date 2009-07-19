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

package jbe.micro;

import jbe.BenchMark;
import jbe.Execute;

public class Array extends BenchMark {

	volatile static int[] arr;
	volatile static int abc;

	public Array() {

		arr = new int[1024];
		for (int i=0; i<100; ++i) arr[i] = 27+i;
		abc = 123;
	}

	public int test(int cnt) {

		int a = 0;
		int i;

/*
   9:	iload_2
   10:	getstatic	#2; //Field arr:[I
   13:	iload_3
   14:	sipush	1023
   17:	iand
   18:	iaload
   19:	iadd
   20:	istore_2
   21:	iinc	3, 1
*/
		for (i=0; i<cnt; ++i) {
			a += arr[i&0x3ff];
		}
		return a;
	}

	public int overhead(int cnt) {

		int a = 0;
		int i;

/*
   9:	iload_2
   10:	getstatic	#3; //Field abc:I
   13:	sipush	1023
   16:	iand
   17:	iadd
   18:	istore_2
   19:	iinc	3, 1
*/
		for (i=0; i<cnt; ++i) {
			a += abc&0x3ff;
		}
		return a;
	}


	public String toString() {

		return "iaload";
	}

	public static void main(String[] args) {

		BenchMark bm = new Array();

		Execute.perform(bm);
	}
			
}
