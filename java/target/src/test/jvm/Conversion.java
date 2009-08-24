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

package jvm;

public class Conversion  extends TestCase {
	
	public String toString() {
		return "Conversion";
	}


	public boolean test() {

		boolean ok = true;
		
		int i;
		
		char c;
		short s;
		long l;
		byte b;
		
		i = 123;
		
		b = (byte) i;
		c = (char) i;
		s = (short) i;
		l = (long) i;
		i = (int) l;
		
		ok = ok && (b==123);
		ok = ok && (c=='{');
		ok = ok && (s==123);
		ok = ok && (l==123);
		ok = ok && (i==123);
		
		return ok;
	}

}
