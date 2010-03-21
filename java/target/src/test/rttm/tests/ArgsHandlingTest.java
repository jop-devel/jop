package rttm.tests;

import rttm.atomic;

public class ArgsHandlingTest {

	public static void main(String[] args) {
		System.out.println(transaction(1, 10, 100));
	}
	
	@atomic private static int transaction(int arg0, int arg1, int arg2) {
		return arg0+arg1+arg2;
	}
}
