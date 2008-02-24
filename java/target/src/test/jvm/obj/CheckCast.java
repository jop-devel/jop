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
 * failes for interfaces
 */
package jvm.obj;

import jvm.TestCase;

/**
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */
public class CheckCast extends TestCase implements Runnable {
	
	public String getName() {
		return "CheckCast";
	}
	
	static interface A extends sup{}

	static interface sup {}

	static class B implements A {}

	static class C implements A {}
	
	public boolean test() {
		
		boolean ok = true;
		
		Object o = new CheckCast();
		CheckCast cc;
		cc = (CheckCast) o;
		
		// Issue: JOP does not check interfaces on checkcast!
		//Runnable r = (Runnable) o;
		
		A a = new B();
		B b = new B();
		C c = new C();

		ok = ok && b instanceof A;
		ok = ok && c instanceof A;
		ok = ok && a instanceof Object;
	
		ok = ok && a instanceof A; 
		ok = ok && a instanceof sup; 
		return ok;
	}



	public void run() {
		// just dummy to use an interface
	}

}
