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

public class InvokeInterface extends BenchMark {

	static If iv;
	static int val;

/*
   30:	iload_2
   31:	getstatic	#2; //Field iv:Ljbe/micro/If;
   34:	iload_3
   35:	iload_1
   36:	invokeinterface	#6,  3; //InterfaceMethod jbe/micro/If.add:(II)I
   41:	iadd
   42:	istore_2
*/
	public int test(int cnt) {

		int a = 0;
		int i;
		if (iv==null) {
			iv = new IfImp();
			val = 123;
		}

		for (i=0; i<cnt; ++i) {
			a += iv.add(i, cnt);
		}
		return a;
	}

/*
   30:	iload_2
   31:	iload_3
   32:	getstatic	#5; //Field val:I
   35:	iadd
   36:	iadd
   37:	istore_2
*/
	public int overhead(int cnt) {

		int a = 0;
		int i;
		if (iv==null) {
			iv = new IfImpTwo();
			val = 123;
		}

		for (i=0; i<cnt; ++i) {
			a += i+val;
		}
		return a;
	}


	public String toString() {

		return "invokeinterface";
	}

	public static void main(String[] args) {

		BenchMark bm = new InvokeInterface();

		Execute.perform(bm);
	}
			
}
