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

public class Add extends BenchMark {

/*
   14:	iload_2
   15:	iload_3
   16:	iadd
   17:	iload_3
   18:	iadd
   19:	istore_2
*/
	public int test(int cnt) {

		int a = 0;
		int b = 123;
		int i;

		for (i=0; i<cnt; ++i) {
			a = a+b+b;
		}
		return a;
	}

/*
   14:	iload_2
   15:	iload_3
   16:	iadd
   17:	istore_2
*/
	public int overhead(int cnt) {

		int a = 0;
		int b = 123;
		int i;

		for (i=0; i<cnt; ++i) {
			a = a+b;
		}
		return a;
	}

	int overheadMinus(int cnt) {

		return 0;
	}

	public String toString() {

		return "iload_3 iadd";
	}

	public static void main(String[] args) {

		Add bm = new Add();

		Execute.perform(bm);
	}
			
}
