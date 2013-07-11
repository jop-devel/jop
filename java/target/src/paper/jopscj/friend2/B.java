package jopscj.friend2;

import jopscj.friend1.Helper; 
import jopscj.friend1.A;

public class B {
	
	static Helper help;
	static A a = new A();
	
	public static void setHelper(Helper h){
		help = h;
	}
	
	public static void someMethod(){
		// Can use M3() because is public
		A.baz();
		
		// Can use private methods
		help.foo(a);
		help.bar(a);
	}

}
