package jdk;

import java.util.*;
import jvm.TestCase;

public class TestVector extends TestCase {

	public String getName() {
		return "Vect";
	}

	public boolean test() {

		Vector v = new Vector();
		
		for (int i=0; i<10; ++i) {
			v.add(new Integer(i));
		}
		for (int i=0; i<10; ++i) {
			System.out.println((String) v.get(i));
		}
		return true;
	}

}
