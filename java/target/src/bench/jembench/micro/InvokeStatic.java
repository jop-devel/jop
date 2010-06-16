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

package jembench.micro;

import jembench.SerialBenchmark;


public class InvokeStatic extends SerialBenchmark {

	public String toString() {

		return "invokestatic";
	}

/*
   0:	iload_0
   1:	iload_1
   2:	iadd
   7:	ireturn
*/
	public static int add(int a, int b) {

		return a+b;
	}
		

/*
   9:	iload_2
   10:	iload_3
   11:	iload_1
   12:	invokestatic	#3; //Method add:(II)I
   15:	iadd
   16:	istore_2
*/
	public int perform(int cnt) {

		int a = 0;
		int i;

		for (i=0; i<cnt; ++i) {
			a += add(i, cnt);
		}
		return a;
	}

/*
   9:	iload_2
   10:	iload_3
   11:	iload_1
   12:	iadd
   17:	iadd
   18:	istore_2
*/
	public int overhead(int cnt) {

		int a = 0;
		int i;

		for (i=0; i<cnt; ++i) {
			a += i+cnt;
		}
		return a;
	}

}
