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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.*;

public class TestJdk {

	public static void main(String args[]) {

		System.out.println("current time: " + System.currentTimeMillis());
		System.out.println("current time: " + System.currentTimeMillis());
		System.out.println("current time: " + System.currentTimeMillis());

		
		// test Hashtable
		System.out.println("Hashtable test");
		Hashtable vc = new Hashtable();
		Integer i = new Integer(1);
		vc.put("one", i);
		vc.put("two", new Integer(2));
		vc.put("three", new Integer(3));
		System.out.println("Hashtable.toString():");
		System.out.println(vc.toString());
		System.out.println(vc.toString());

		if (vc.containsKey("two")) {
			System.out.println("OK");
		} else {
			System.out.println("ERROR Hashtable");
		}
		if (vc.contains(i)) {
			System.out.println("OK");
		} else {
			System.out.println("ERROR Hashtable");
		}

		if (!vc.containsKey(new Object())) {
			System.out.println("OK");
		} else {
			System.out.println("ERROR Hashtable");
		}

		if (!vc.isEmpty()) {
			System.out.println("OK");
		} else {
			System.out.println("ERROR Hashtable");
		}

		Enumeration hashenum;

		// TODO: JOP says readMem: wrong address

		// hashenum = vc.elements();
		// System.out.println("Hashtable value Enumeration:");
		// while (hashenum.hasMoreElements()) {
		// // System.out.println(hashenum.nextElement().toString());
		// }
		//
		// hashenum = vc.keys();
		// System.out.println("Hashtable key Enumeration:");
		// while (hashenum.hasMoreElements()) {
		// System.out.println(hashenum.nextElement().toString());
		// }

		if (vc.get("one").equals(i)) {
			System.out.println("OK");
		} else {
			System.out.println("ERROR Hashtable");
		}
		if (vc.size() == 3) {
			System.out.println("OK");
		} else {
			System.out.println("ERROR Hashtable");
		}
		if (i.equals(vc.remove("one"))) {
			System.out.println("OK");
		} else {
			System.out.println("ERROR Hashtable");
		}
		if (!(vc.size() == 3)) {
			System.out.println("OK");
		} else {
			System.out.println("ERROR Hashtable");
		}
		if (vc.size() == 2) {
			System.out.println("OK");
		} else {
			System.out.println("ERROR Hashtable");
		}

		System.out.println("Hashtable.toString():");
		System.out.println(vc.toString());

		vc.clear();
		if (vc.isEmpty()) {
			System.out.println("OK");
		} else {
			System.out.println("ERROR Hashtable");
		}

		System.out.println("Hashtable test END");

		
		// MARTIN: uncomment this test for the Vector class
		// 

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
// uncomment until here
		
//		if (!vec.contains("nelsonafasf")) {
//			System.out.println("OK");
//		} else {
//			System.out.println("ERROR Vector");
//		}
//
//		System.out.println("testing Vector.copyInto(Array)");
//		Object[] objarray = new Object[6];
//		vec.copyInto(objarray);
//		for (int ix = 0; ix < 6; ix++) {
//
//			System.out.println(objarray[ix].toString());
//		}

//		if ("nelson1".equals(vec.elementAt(4))) {
//			System.out.println("OK");
//		} else {
//			System.out.println("ERROR Vector");
//		}
//
//		// vec.elementAt(10);
//
//		if (vec.indexOf("nelson2") == 5) {
//			System.out.println("OK");
//		} else {
//			System.out.println("ERROR Vector");
//		}
//
//		vec.addElement(new Integer(1));
//		
//		if (vec.indexOf("nelson2",1) == 6) {
//			System.out.println("OK");
//		} else {
//			System.out.println("ERROR Vector");
//		}
//
//		String five = new String("five");

//		System.out.println("Vector test END");

		// test Stack.java
		/*
		 * Stack stack = new Stack(); stack.push("first1");
		 * stack.push("first2"); stack.push("first3"); stack.push("first4");
		 * System.out.println(stack.toString());
		 * System.out.println(stack.peek().toString());
		 * 
		 * System.out.println(stack.pop().toString());
		 * System.out.println(stack.pop().toString());
		 * System.out.println(stack.pop().toString());
		 * System.out.println(stack.pop().toString());
		 * System.out.println(stack.pop().toString());
		 * 
		 */

		

		// test lang.Character
		/*
		 * Character ch1 = new Character('2');
		 * System.out.println(ch1.toString()); Integer i = new
		 * Integer((int)'2'); System.out.println(i.toString()); int inttmp =
		 * Character.digit('a', 10); Integer i2 = new Integer(inttmp);
		 * System.out.println(i2.toString()); if(Character.isLowerCase('A')){
		 * System.out.println("true"); }else{ System.out.println("false"); }
		 * 
		 * String teststr = new String("1123abcdABCD=)("); teststr =
		 * teststr.toLowerCase(); System.out.println(teststr); teststr =
		 * teststr.toUpperCase(); System.out.println(teststr);
		 */

		Byte b = new Byte((byte) 34);
		System.out.println(b.toString());
		byte b1 = Byte.parseByte("34");
		Byte b2 = new Byte(b1);
		System.out.println(b2.toString());
		if (b.equals(new Integer(2))) {
			System.out.println("true");
		} else {
			System.out.println("false");

		}

		OutputStreamWriter osw = new OutputStreamWriter(
				(OutputStream) System.out);
		try {
			osw.write(new char[] { '2', '\r', '\n' });
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		DataOutputStream dos = new DataOutputStream((OutputStream) System.out);
		try {
			dos.writeChars("test das");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.print("test");

	}

}
