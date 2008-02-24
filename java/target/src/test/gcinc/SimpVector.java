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

package gcinc;

import java.util.Vector;

public class SimpVector implements Runnable {

	Vector v;
	int nr;
	int size; 
	
	public SimpVector(int i) {
		size = i;
	}
	public void run() {
		if (nr==0) {
			if (size==0) throw new Error("Size is 0");
			// create the vector
			v = new Vector();
			for (int i=0; i<size; ++i) {
				v.addElement(new Integer(i));
			}
			nr = 1;
		} else if (nr==1) {
			// check the vector
			if (v.size()!=size) throw new Error("Size changed");
			for (int i=0; i<size; ++i) {
				Object o = v.elementAt(i);
				if (o==null) throw new Error("Null pointer to element");
				Integer it = (Integer) o;
				int iv = it.intValue();
				if (iv!=i) throw new Error("Value is wrong");
			}
			nr = 0;
		}
	}

	static SimpVector a,b,c;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		a = new SimpVector(100);
		b = new SimpVector(25);
		c = new SimpVector(999);

		for (;;) {
			a.run();
			b.run();
			c.run();
		}
	}

}
