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


public class ArrayMulti extends TestCase {

	public String toString() {
		return "ArrayMulti";
	}

	public boolean test() {

		boolean ok = true;
		
		double da[][] = new double[4][5];
		int ia[][] = new int[2][3];
		int val = 1;
		if (ia.length!=2) {
			System.out.println("Error - array.length");
			ok = false;
		}
		if (ia[1].length!=3) {
			System.out.println("Error - array.length");
			ok = false;
		}
		for (int i=0; i<ia.length; ++i) {
			for (int j=0; j<ia[i].length; ++j) {
				ia[i][j] = ~i+j;			
			}
		}
		for (int i=0; i<ia.length; ++i) {
			for (int j=0; j<ia[i].length; ++j) {
				if (ia[i][j] != ~i+j) {
					System.out.println("Error in array");
					ok = false;
				}
			}
		}
		
		for (int i=0; i<da.length; ++i) {
			for (int j=0; j<da[i].length; ++j) {
				da[i][j] = (double) (i*1000 + j);			
			}
		}
		for (int i=0; i<da.length; ++i) {
			for (int j=0; j<da[i].length; ++j) {
				if (da[i][j] != (double) (i*1000 + j)) {
					System.out.println("Error in double array");
					ok = false;
				}
			}
		}
		
		// let's check the integer array again
		for (int i=0; i<ia.length; ++i) {
			for (int j=0; j<ia[i].length; ++j) {
				if (ia[i][j] != ~i+j) {
					System.out.println("Error in array");
					ok = false;
				}
			}
		}


		return ok;
	}


}
