package rttm.tests;

import rttm.AbortException;
import rttm.atomic;

public class AbortTest {

	public static void main(String[] args) throws Exception {
		try {
			transaction();
			throw new Exception("Should not have been reached!");
		} catch (AbortException e) {
			System.out.println("AbortException catched.");
		}
		
	}
	
	@atomic private static void transaction() {
		rttm.Commands.abort();
	}
}
