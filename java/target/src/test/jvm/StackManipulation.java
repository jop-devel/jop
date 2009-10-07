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

package jvm;

public class StackManipulation extends TestCase {
	
	public String toString() {
		return "StackManipulation";
	}

	long l1, l2;

	public boolean test() {

		boolean ok = true;
		
		// dup_x2
		char s[] = new char[2];
		s[0] = s[1] = 'x';
		ok = ok && (s[0]=='x' && s[1]=='x');
		
		long l[] = new long[2];
		long lx;
		
		
		l[0] = 123;
		
		// dup2
		l[0] = lx = 56;
		
		ok = ok && (lx==56 && l[0]==56);
		
		// dup2_x1
		l1 = l2 = 8765;
		
		ok = ok && (l1==8765 && l2==8765);
		
		// dup2_x2
		lx = l[0] = 2;
		ok = ok && (l[0]==2 && l[1]==0 && lx==2);
		
		// dup2_x2
		l[0] = l[1] = 1;
		ok = ok && (l[0]==1 && l[1]==1);

		return ok;
	}

}
