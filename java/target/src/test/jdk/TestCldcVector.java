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

package jdk;

import java.util.Vector;

public class TestCldcVector {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		// test Vector.java
		System.out.println("Vector test");
		Vector vec = new Vector();
		if (vec.size() == 0) {
			System.out.println("OK");
		} else {
			System.out.println("ERROR Vector");
		}
		vec.addElement(new Integer(1));
		vec.addElement(new Integer(2));
		vec.addElement(new Integer(3));
		vec.addElement("nelson");
		vec.addElement("nelson1");
		vec.addElement("nelson2");

		if ((vec.size() == 6)) {
			System.out.println("OK");
		} else {
			System.out.println("ERROR Vector");
		}
		System.out.println("Vector.toString():");
		System.out.println(vec.toString());
		System.out.println(vec.toString());

		if (vec.contains("nelson")) {
			System.out.println("OK");
		} else {
			System.out.println("ERROR Vector");
		}

		if(!vec.isEmpty()){
			System.out.println("OK");
		} else {
			System.out.println("ERROR Vector");
		}
		
		
		if (!vec.contains("nelsonafasf")) {
			System.out.println("OK");
		} else {
			System.out.println("ERROR Vector");
		}

		System.out.println("testing Vector.copyInto(Array)");
		Object[] objarray = new Object[6];
		vec.copyInto(objarray);
		for (int ix = 0; ix < 6; ix++) {

			System.out.println(objarray[ix].toString());
		}

		if ("nelson1".equals(vec.elementAt(4))) {
			System.out.println("OK");
		} else {
			System.out.println("ERROR Vector");
		}

		// vec.elementAt(10);

		if (vec.indexOf("nelson2") == 5) {
			System.out.println("OK");
		} else {
			System.out.println("ERROR Vector");
		}

		vec.addElement(new Integer(1));
		
		if (vec.indexOf(new Integer(1),1) == 6) {
			System.out.println("OK");
		} else {
			System.out.println("ERROR Vector");
		}
		
		System.out.println("Vector.toString():");
		System.out.println(vec.toString());
		System.out.println("remove:");
		vec.remove(1);
		
		System.out.println("Vector.toString():");
		System.out.println(vec.toString());
		System.out.println("remove:");
		vec.remove(1);
		
		System.out.println("Vector.toString():");
		System.out.println(vec.toString());
		
		vec.removeAllElements();
		if(vec.isEmpty()){
			System.out.println("OK");
		} else {
			System.out.println("ERROR Vector");
		}
		
		
		String five = new String("five");

		System.out.println("Vector test END");

	}

}
