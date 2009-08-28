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

public class MultiArray extends TestCase {

	public String toString() {
		return "MultiArray";
	}
	
	public boolean test() {

		boolean ok = true;

		int ia[] = new int[11];
		int val = 1;
		
		MultiArray ma[][] = new MultiArray[3][4];
		
//		Array a[] = new Array[2];
		Array a2[][] = new Array[4][5];
		


		for (int i=0; i<ia.length; ++i) {
			ia[i] = ~i;
		}
		for (int i=0; i<ia.length; ++i) {
			if (ia[i] != ~i) {
				System.out.println("Error");
				ok = false;
			}
		}
		
		int ia2[][] = new int[3][5];
		
		if (ia2.length != 3) {
			System.out.println("Size Error");
			ok = false;
		}
		for (int i=0; i<ia2.length; ++i) {
			for (int j=0; j<5; ++j) {
				ia2[i][j] = i+j;
			}
		}
		for (int i=0; i<3; ++i) {
			for (int j=0; j<5; ++j) {
				if (ia2[i][j] != i+j) {
					ok = false;
				}
			}
		}
		
		return ok;
	}


}
