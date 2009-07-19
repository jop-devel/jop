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

public class Fadd extends BenchMark {

/*
    FLOAD 2
    FLOAD 3
    FADD
    FLOAD 3
    FADD
    FSTORE 2
*/
	public int test(int cnt) {

		float a = 0;
		float b = 123;
		int i;

		for (i=0; i<cnt; ++i) {
			a = a+b+b;
		}
		return (int) a;
	}

/*
    FLOAD 2
    FLOAD 3
    FADD
    FSTORE 2
*/
	public int overhead(int cnt) {

		float a = 0;
		float b = 123;
		int i;

		for (i=0; i<cnt; ++i) {
			a = a+b;
		}
		return (int) a;
	}

	int overheadMinus(int cnt) {

		return 0;
	}

	public String toString() {

		return "fload_3 fadd";
	}

	public static void main(String[] args) {

		Fadd bm = new Fadd();

		Execute.perform(bm);
	}
			
}
