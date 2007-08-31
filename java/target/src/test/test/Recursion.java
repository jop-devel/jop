package test;

public class Recursion {

	static int cnt;
	
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
