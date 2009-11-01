package rttm.swtest;

import rttm.Atomic;

public class AnnotationCompileTest {
	static boolean foo = false;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		atomicMethod();
		System.out.println("Finished transactional method.");
		System.out.println(foo);
	}
	
	@Atomic static void atomicMethod() {
		foo = true;
	}

}
