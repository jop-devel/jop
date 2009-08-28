/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2007, Alberto Andreotti

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

package jvm.math;

import jvm.TestCase;


public class FloatTest extends TestCase {

	public String toString() {
		return "FloatTest";
	}
	
	public boolean test() {

		boolean ok = true;
		
		float f1 = 1.3F;
		float f2 = 2.9F;

		float f3 = f1+f2;

		ok = ok && test_f2i();
		
		int i = (int) f3;
		ok = ok && (i==4);

		f3 = f1-f2;
		i = (int) f3;
		ok = ok && (i==-1);

		f1 = 0F;
		f2 = 1F;
		f3 = 2F;
		
		i = (int) (f1+f2+f3);
		ok = ok && (i==3);
		


		return ok;
	}
	
	boolean test_f2i() {
		
		boolean ok = true;
		
		ok = ok && (((int) 0F) == 0);
		ok = ok && (((int) 1F) == 1);
		ok = ok && (((int) 2F) == 2);
		ok = ok && (((int) 0.1F) == 0);
		ok = ok && (((int) 0.4F) == 0);
		ok = ok && (((int) 0.7F) == 0);
		ok = ok && (((int) 0.9999F) == 0);
		ok = ok && (((int) 99.9999F) == 99);

		ok = ok && (((int) -0F) == 0);
		ok = ok && (((int) -1F) == -1);
		ok = ok && (((int) -2F) == -2);
		ok = ok && (((int) -0.1F) == 0);
		ok = ok && (((int) -0.3F) == 0);
		ok = ok && (((int) -0.99F) == 0);
		ok = ok && (((int) -1.1F) == -1);
		ok = ok && (((int) -999.999F) == -999);

		return ok;
	}
}
