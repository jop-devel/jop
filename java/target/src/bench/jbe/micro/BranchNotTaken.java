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

public class BranchNotTaken extends BenchMark {

/*
   18:	iload_1
   19:	iload_2
   20:	if_icmplt	28
   23:	iload_3
   24:	iload	4
   26:	iadd
   27:	istore_3
   28:	iload_3
   29:	iload	5
   31:	iadd
   32:	istore_3
*/
	public int test(int cnt) {

		int i;
		int a = 0;
		int b = 123;
		int c = 456;

		for (i=0; i<cnt; ++i) {
			if (cnt>=i) {
				a = a+b;
			}
			a = a+c;
		}
		return a;
	}

/*
   18:	iload_3
   19:	iload	4
   21:	iadd
   22:	istore_3
   23:	iload_3
   24:	iload	5
   26:	iadd
   27:	istore_3
*/
	public int overhead(int cnt) {

		int i;
		int a = 0;
		int b = 123;
		int c = 456;

		for (i=0; i<cnt; ++i) {
			a = a+b;
			a = a+c;
		}
		return a;
	}

	public String toString() {

		return "if_icmplt not taken";
	}

	public static void main(String[] args) {

		BenchMark bm = new BranchNotTaken();

		Execute.perform(bm);
	}
			
}
