package jvm;

public class Except extends TestCase {
	
	public String getName() {
		return "Except";
	}
	
	public boolean test() {

		boolean ok = true;
				
		ok = ok && recursion();

		return ok;
	}

	public static boolean recursion() {
		
		System.out.println("recursion");
		
		return recursion();
	}
	
}
