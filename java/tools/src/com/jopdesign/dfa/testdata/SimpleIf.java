package com.jopdesign.dfa.testdata;

public class SimpleIf {

	public static void main(String [] args) {
		
		if (args.length == 0) {
			x();
		}
		y();
		x();
	}
	
	static boolean a;
	
	private static void x() { }
	private static void y() { }
	
}
