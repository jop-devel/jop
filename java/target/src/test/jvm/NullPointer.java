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

/**
 * Test different null pointer checks.
 * 
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */
public class NullPointer extends TestCase {

	public String toString() {
		return "NullPointer";
	}
	
	int ival;
	long lval;
	NullPointer rval;
	
	/**
	 * an invokevirtual
	 */
	public void foo() {
		
	}
	
	/**
	 * an invokespecial
	 */
	private void bar() {
		
	}

	public boolean test() {

		boolean ok = true;
		int i;
		long l;
		NullPointer r;
		
		NullPointer nullObj = null;
		boolean caught;
		
		caught = false;
		try {
			nullObj.ival = 123;
		} catch (NullPointerException e) {
			caught = true;
		}
		ok &= caught;
		
		caught = false;
		try {
			i = nullObj.ival;
		} catch (NullPointerException e) {
			caught = true;
		}
		ok &= caught;
		
		caught = false;
		try {
			nullObj.lval = 1L;
		} catch (NullPointerException e) {
			caught = true;
		}
		ok &= caught;

		caught = false;
		try {
			l = nullObj.lval;
		} catch (NullPointerException e) {
			caught = true;
		}
		ok &= caught;

		caught = false;
		try {
			nullObj.rval = this;
		} catch (NullPointerException e) {
			caught = true;
		}
		ok &= caught;

		caught = false;
		try {
			r = nullObj.rval;
		} catch (NullPointerException e) {
			caught = true;
		}
		ok &= caught;

		caught = false;
		try {
			nullObj.foo();
		} catch (NullPointerException e) {
			caught = true;
		}
		ok &= caught;

		// TODO: null pointer check in invokespecial is missing
		
//		caught = false;
//		try {
//			nullObj.bar();
//		} catch (NullPointerException e) {
//			caught = true;
//		}
//		ok &= caught;

		return ok;
	}
}
