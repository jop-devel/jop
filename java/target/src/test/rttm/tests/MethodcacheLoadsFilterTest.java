package rttm.tests;

import rttm.atomic;
import rttm.Diagnostics;

public class MethodcacheLoadsFilterTest {

	public static void main(String[] args) {
		System.out.println(atomicSection());
		
		Diagnostics.saveStatistics();
		Diagnostics.stat(0);
	}
	
	@atomic private static int atomicSection() {
		return factorial(5);
	}
	
	/**
	 * Method load should bypass transaction cache.
	 */
	private static int factorial(int n) {
		return n == 1 ? 1 : n*factorial(n-1);
	}
}
