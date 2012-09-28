package jopscj.friend1;

import jopscj.friend2.B;

public class A {
	
	static {
		
		Helper h = new Helper();
		B.setHelper(h);
	}
	
	// This method should be exposed to "friend" packages
	void foo(){
		
	}
	
	// This method should be exposed to "friend" packages
	void bar(){
		
	}
		
	// This is public so anyone can use it
	public static void baz(){
		
	}

}
