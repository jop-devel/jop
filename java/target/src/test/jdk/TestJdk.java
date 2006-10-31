package jdk;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class TestJdk {

	public static void main(String args[]) {

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

		System.out.println("current time: " + System.currentTimeMillis());
		System.out.println("current time: " + System.currentTimeMillis());
		System.out.println("current time: " + System.currentTimeMillis());

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

		System.out.println("Vector test END");

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

		// test String and Co
		/*
		 * String str1 = new String(); String str2 = new String(); str2 = "ames
		 * cole"; str1 = "james cole"; System.out.println(str1);
		 * System.out.println(str2); int i = 0; i = str1.compareTo(str2);
		 * System.out.println(i); String tmp = str1.concat(str2);
		 * System.out.println(tmp); if(tmp.endsWith("Cole")){
		 * System.out.println("true"); } else { System.out.println("false"); }
		 * if(str1.equals(str1)){ System.out.println("true equality"); } else {
		 * System.out.println("false equalitiy"); }
		 * 
		 * byte[] sb = str1.getBytes(); try { byte[] sb1 =
		 * str1.getBytes("ISO_8859_1"); } catch (UnsupportedEncodingException e) { //
		 * TODO Auto-generated catch block e.printStackTrace(); }
		 * 
		 * int hashcode = str1.hashCode(); System.out.print(hashcode);
		 * 
		 * int indexof = 0; indexof = str1.indexOf(99);
		 * System.out.print("indexof:"); System.out.print(indexof); indexof =
		 * str1.indexOf("e",7); System.out.print("indexof:");
		 * System.out.print(indexof); indexof = str1.lastIndexOf(97,7);
		 * System.out.print("indexof:"); System.out.print(indexof); tmp =
		 * str1.replace('a', 'b'); System.out.print(tmp);
		 * System.out.print("substring:\n");
		 * 
		 * tmp = str1.substring(3, 7); System.out.print(tmp);
		 * //str1.toLowerCase();
		 * 
		 * String str3 = new String(" bloody mary "); tmp = str3.trim();
		 * System.out.print(tmp);
		 * 
		 * 
		 * //System.out.println(sb.toString());
		 * 
		 */

		// Long lon = new Long(System.currentTimeMillis());
		// System.out.println(lon.toString());
		// test String Buffer
		/*
		 * StringBuffer sb = new StringBuffer("dfmasafasa");
		 * 
		 * System.out.println(sb.toString()); sb.append(true);
		 * System.out.println(sb.toString()); sb.append("hgfjz");// TODO: FIXXME :
		 * STACK OVERFLOW System.out.println(sb.toString()); sb.append(1);
		 * System.out.println(sb.toString()); char[] tmp = new char[10]; tmp[0] =
		 * '0'; tmp[1] = '1'; tmp[2] = '2'; tmp[3] = '3'; sb.append(tmp,1,5);
		 * System.out.println(sb.toString()); sb.deleteCharAt(5);
		 * System.out.println(sb.toString()); sb.insert(3, true);
		 * System.out.println(sb.toString());
		 * System.out.println(sb.reverse().toString()); sb.append(new
		 * Integer(2)); System.out.println(sb.toString());
		 * 
		 */
		/*
		 * Short sho= new Short((short)2); System.out.println(sho.toString());
		 * 
		 * Long lo = new Long(234); System.out.println(lo.toString()); String
		 * tmplong = new String("-123234234546"); long lo1 =
		 * Long.parseLong(tmplong); Long lo2 = new Long(lo1);
		 * System.out.println(lo2.toString());
		 */

		/*
		 * Integer i1 = new Integer(1); System.out.println(i1.toString()); int
		 * i2 = Integer.parseInt("123"); Integer i3 = new Integer(i2);
		 * System.out.println(i3.toString());
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
