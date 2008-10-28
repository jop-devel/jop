package com.jopdesign.dfa.testdata;

public class SimpleConst {

	public static void main(String [] args) {
		int bound = 0;
		for (int i = 0; i < 10 && bound < 20; i++) {
			for (int k = 0; k < 10 && bound < 20; k++) {
				bound++;
			}
		}
	}
	
}
