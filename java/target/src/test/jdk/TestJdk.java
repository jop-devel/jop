package jdk;

import java.util.*;

public class TestJdk {

	public static void main(String args[]) {
		Hashtable vc = new Hashtable();
		Integer i = new Integer(1);
		vc.put("one", i);
		vc.put("two", new Integer(2));
		vc.put("three", new Integer(3));
		System.out.println(vc.toString());
		if (vc.contains("two")) {
			System.out.println("true");
		} else {
			System.out.println("false");
			System.out.println(vc.toString());
		}
	}
	 

}
