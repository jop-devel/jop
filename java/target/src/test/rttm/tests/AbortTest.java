package rttm.tests;

import rttm.AbortException;
import rttm.Atomic;

public class AbortTest {

	public static void main(String[] args) throws Exception {
		try {
			transaction();
			throw new Exception("Should not have been reached!");
		} catch (AbortException e) {
			System.out.println("AbortException catched.");
		}
		
	}
	
	@Atomic private static void transaction() {
		rttm.Operations.abort();
	}
}
