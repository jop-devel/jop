package jdk;

import jvm.TestCase;

public class HashCode extends TestCase {

	public String getName() {
		return "HashCode";
	}

	public boolean test() {
		
		int i = new Object().hashCode();
		int j = new Object().hashCode();
		int k = new Object().hashCode();
//		System.out.println("hash codes: "+i+" "+j+" "+k);
		return (i!=j && i!=k && j!=k);
	}

}
