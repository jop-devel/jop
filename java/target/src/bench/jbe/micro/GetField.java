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

public class GetField extends BenchMark {

	public int field;

	public GetField() {

		field = 4711;
	}

	static GetField iv;
	static int stat;



/*
   40:	iload_2
   41:	iload_3
   42:	iadd
   43:	aload	5
   45:	getfield	#2; //Field field:I
   48:	iadd
   49:	istore_2
*/
	public int test(int cnt) {

		int a = 0;
		int b = 123;
		int i;
		if (iv==null) {
			iv = new GetFieldExt();
			stat = 123;
		}
		GetField x = iv;

		for (i=0; i<cnt; ++i) {
			a = a+b+x.field;
		}
		return a;
	}

/*
   35:	iload_2
   36:	iload_3
   37:	iadd
   38:	istore_2
*/
	public int overhead(int cnt) {

		int a = 0;
		int b = 123;
		int i;
		if (iv==null) {
			iv = new GetField();
			stat = 123;
		}

		for (i=0; i<cnt; ++i) {
			a = a+b;
		}
		return a;
	}


	public String toString() {

		return "getfield";
	}

	public static void main(String[] args) {

		BenchMark bm = new GetField();

		Execute.perform(bm);
	}
			
}
