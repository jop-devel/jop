/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2007, Alberto Andreotti

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
public class SimpGC3 implements Runnable {

	public myList list;
	int length,nr;
	public SimpGC3(int i) {
		length = i;
		
	}
	public void run() {
		if (nr==0) {
			if (length==0) throw new Error("Length is 0");
			// create the List
			list= new myList();
			for (int i=1; i<length/2 + 1; ++i) {
				list.add(new typeA(i));
				list.add(new typeB(i));
			}
			nr = 1;
		} else if (nr==1) {
			//System.out.println(length);
			// check the list
			if (list.size()!=length) throw new Error("Size changed");
			testObject to;
			to=(testObject)list.next();
			for (int i=1; i<length/2+1; ++i) {
				if (to==null) throw new Error("Null pointer to element");
				if (!to.testYourself(i)) throw new Error("Value is wrong");
				to=(testObject)list.next();
			
				if (to==null) throw new Error("Null pointer to element");
				if (!to.testYourself(i)) throw new Error("Value is wrong");
				to=(testObject)list.next();
				}	
			nr = 0;
		
	}
	}

	static SimpGC3 a,b,c;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		a = new SimpGC3(10);
		b = new SimpGC3(10);
		c = new SimpGC3(10);
		//GC.setConcurrent();	
		for (;;) {
			a.run();
			b.run();
			c.run();
		//	System.out.println("I'm running");
		}
	}

}		
