/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2001-2009, Martin Schoeberl (martin@jopdesign.com)

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

package timing;

public class PutField extends BenchMark {
	
	volatile public int field;
	
/*
    ALOAD 0
    ILOAD 4
    PUTFIELD timing/PutField.field : I
   L6
    LINENUMBER 46 L6
    ILOAD 2
    ILOAD 3
    IADD
    ISTORE 2
*/
	public int test(int cnt) {

		int a = 0;
		int b = 123;
		int i;

		for (i=0; i<cnt; ++i) {
			field = i;
			a += b;
		}
		return a+b+field;

	}

/*
    ILOAD 2
    ILOAD 4
    ILOAD 3
    IADD
    IADD
    ISTORE 2
*/
	public int overhead(int cnt) {

		int a = 0;
		int b = 123;
		int i;

		for (i=0; i<cnt; ++i) {
			a += i+b;
		}
		return a+b+field;
	}

	public String toString() {

		return "putfield";
	}

	public static void main(String[] args) {

		PutField bm = new PutField();
		bm.execute();
	}
			
}
