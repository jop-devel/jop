package jdk;

import java.util.*;
import jvm.TestCase;

public class TestVector extends TestCase {

	public String getName() {
		return "Vector";
	}

	public boolean test() {

		boolean ret = true;
		Vector v = new Vector();
		
		for (int i=0; i<10; ++i) {
			v.addElement(new Integer(i));
		}
		for (int i=0; i<10; ++i) {
			Integer ival = (Integer) v.elementAt(i);
			if (ival.intValue() != i) {
				ret = false;
			}
		}
		return ret;
	}

}
