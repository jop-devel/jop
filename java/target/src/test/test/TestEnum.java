/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2009, Martin Schoeberl (martin@jopdesign.com)

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


/**
 * 
 */
package test;

/**
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */
public class TestEnum {

	enum Test { A, B, C };
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Test t = Test.A;
		System.out.println(t);
		t = Test.B;
		System.out.println(t);
		
		test(Test.A);
		test(Test.B);
		test(Test.C);
		
		// not yet supported:
		// System.out.println(t.valueOf("A"));
		// System.out.println(t.values());
	}
	
	static void test(Test t) {
		if (t==Test.A) {
			System.out.println("enum A");
		}
		if (t==Test.B) {
			System.out.println("enum B");
		}
		if (t==Test.C) {
			System.out.println("enum C");
		}
			
	}

}
