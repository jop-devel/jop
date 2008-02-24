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


public class Array extends TestCase {

	public String getName() {
		return "Array";
	}

	public boolean test() {

		boolean ok = true;
		
		int ia[] = new int[3];
		int val = 1;
/*
		System.out.println("iaload");
		val = ia[0];
		val = ia[2];

		System.out.println("iastore");
		ia[0] = val;
		ia[2] = val;
*/
		if (ia.length!=3) {
			System.out.println("Error - array.length");
			ok = false;
		}
		for (int i=0; i<ia.length; ++i) {
			ia[i] = ~i;
		}
		for (int i=0; i<ia.length; ++i) {
			if (ia[i] != ~i) {
				System.out.println("Error in array");
				ok = false;
			}
		}

//		System.out.println("iaload bound");
//		val = ia[-1];
//		val = ia[3];

//		System.out.println("iastore bound");
//		ia[-1] = val;
//		ia[3] = val;

//		np(null);

		return ok;
	}

	public static void np(int[] ia) {

		int val = 1;
		System.out.println("iaload null pointer");
//		val = ia[1];
		System.out.println("iastore null pointer");
		ia[1] = val;
	}

}
