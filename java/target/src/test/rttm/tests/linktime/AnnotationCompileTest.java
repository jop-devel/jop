package rttm.tests.linktime;

import rttm.atomic;

public class AnnotationCompileTest {
	static volatile boolean foo = false;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		atomicMethod();
		System.out.println("Finished transactional method.");
		System.out.println(foo);
	}
	
	@atomic static void atomicMethod() {
		foo = true;
	}

}
