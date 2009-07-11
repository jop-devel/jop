/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2009, Raimund Kirner (raimund@vmars.tuwien.ac.at)

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

// project: spcmp paper
// example: bubble sort
// author:  Raimund Kirner, 22.06.2009

package sp;

import com.jopdesign.sys.Native;

/**
 * A real-time task that performs sorting via bubble sort. This is a single-path
 * program.
 * 
 * @author Raimund Kirner (raimund@vmars.tuwien.ac.at)
 */
class SPBubbleSort extends SimpleTask {
	final int SIZE = 20;
	int[] aDat;
	int i;
	int j;
	int temp;
	boolean cond;

	// Standard Constructor
	public SPBubbleSort() {
		aDat = new int[SIZE];
	}

	// read data from mem
	public void read() {
		for (i = 0; i < aDat.length; i++) {
			aDat[i] = (i + 30) % SIZE;
		}
	}

	// write data back to mem
	public void write() {
		for (i = 0; i < aDat.length; i++) {
			System.out.println("array[" + i + "]: " + aDat[i] + ";");
		}
	}

	// sort routine
	public void execute() {
		// Sortieren
		i = 0;
		while (i < aDat.length) {
			j = aDat.length - 1;
			while (j > i) {
				cond = aDat[j] < aDat[j - 1];
				// conventional swap:
				// if ( cond==true ) {
				// temp = aDat[j];
				// aDat[j] = aDat[j-1];
				// aDat[j-1] = temp;
				// }
				//
				// single path swap:
				temp = aDat[j];
				aDat[j] = Native.condMove(aDat[j - 1], aDat[j], cond);
				aDat[j - 1] = Native.condMove(temp, aDat[j - 1], cond);
				j = j - 1;
			}
			i = i + 1;
		}

	}

	public static void main(String[] args) {
		SPBubbleSort spbs = new SPBubbleSort();

		spbs.read();

		spbs.execute();

		spbs.write();
	}
}
