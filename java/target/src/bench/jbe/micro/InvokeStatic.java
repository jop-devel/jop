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

public class InvokeStatic extends BenchMark {

	static int val;
	public InvokeStatic() {
		val = 123;
	}
/*
   0:	iload_0
   1:	iload_1
   2:	iadd
   3:	getstatic	#2; //Field val:I
   6:	iadd
   7:	ireturn
*/
	public static int add(int a, int b) {

		return a+b+val;
	}
		

/*
   9:	iload_2
   10:	iload_3
   11:	iload_1
   12:	invokestatic	#3; //Method add:(II)I
   15:	iadd
   16:	istore_2
*/
	public int test(int cnt) {

		int a = 0;
		int i;

		for (i=0; i<cnt; ++i) {
			a += InvokeStatic.add(i, cnt);
		}
		return a;
	}

/*
   9:	iload_2
   10:	iload_3
   11:	iload_1
   12:	iadd
   13:	getstatic	#2; //Field val:I
   16:	iadd
   17:	iadd
   18:	istore_2
*/
	public int overhead(int cnt) {

		int a = 0;
		int i;

		for (i=0; i<cnt; ++i) {
			a += i+cnt+val;
		}
		return a;
	}


	public String toString() {

		return "invokestatic";
	}

	public static void main(String[] args) {

		BenchMark bm = new InvokeStatic();

		Execute.perform(bm);
	}
			
}
