package test;

public class Recursion {

	static int cnt;
	
	static void npexcp() {
		
		Object o = null;
		o.hashCode();
	}
	
	static void abexcp() {
		
		int[] ia = new int[3];
		ia[4] = 2;
	}
	
	static void foo() {
		++cnt;
		System.out.println("depth= "+cnt);
		foo();
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		foo();
	}

}
