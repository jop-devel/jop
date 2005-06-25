/*
 * Created on 03.06.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package test;

/**
 * @author admin
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class B extends A {

	public void f2() {
		System.out.println("B.f2()");
	}
	public static void main(String[] args) {
		System.out.println("main B");
		B b = new B();
		b.f1();
		b.f2();
	}
}
