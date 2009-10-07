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

/*
 * Created on 30.07.2005
 *
 */
package jvm.obj;

import jvm.TestCase;

/**
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */
public class TypeMix extends TestCase {
	
	public String toString() {
		return "Object Type Mix";
	}
	
	
	public boolean test() {
		
		boolean ok = true;
		
		A a = new A(123);
		B b = new B(456);
		A sup = b;

		ok = ok && a.val==123;
		ok = ok && b.val==456;
		ok = ok && ((A) b).val==0;
		ok = ok && sup.val==0;
		/*- checkcast not implemented yet
		ok = ok && ((B) sup).val==456;
		*/

		return ok;
	}

	class A {
		int val;
		A(int v) {
			val = v;
		}
	}
	class B extends A {
		long val;
		B(int v) {
			super(0);
			val = v;
		}
	}
	
	public static void main(String args[]) {
		new TypeMix().test();
	}
}
