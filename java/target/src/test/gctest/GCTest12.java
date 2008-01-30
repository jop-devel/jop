package gctest;

public class GCTest12 {

	static GcTClassA sa;
	static GcTClassB sb;
	static GcTClassC sc;
	
	GcTClassA a;
	GcTClassB b;
	GcTClassC c;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		GcTClassA la = new GcTClassA();
		GcTClassB lb = new GcTClassB();
		GcTClassC lc = new GcTClassC();
		
		sa = new GcTClassA();
		sb = new GcTClassB();
		sc = new GcTClassC();
		
		GCTest12 gct = new GCTest12();
		gct.a = new GcTClassA();
		gct.b = new GcTClassB();
		gct.c = new GcTClassC();
		
		if (la.foo()!=1) fail("1");
		if (lb.fee(123)!=2) fail("2");
		if (lc.fum("abc", 456)!=3) fail("3");
		gct.testOther();
		for (int i=0; i<100000; ++i) {
			new GCTest12();
		}
		if (la.foo()!=1) fail("a");
		if (lb.fee(123)!=2) fail("b");
		if (lc.fum("abc", 456)!=3) fail("c");
		gct.testOther();
		System.out.println("Test passed");
	}

	private void testOther() {
		if (sa.foo()!=1) fail("4");
		if (sb.fee(123)!=2) fail("5");
		if (sc.fum("abc", 456)!=3) fail("6");
		if (a.foo()!=1) fail("7");
		if (b.fee(123)!=2) fail("8");
		if (c.fum("abc", 456)!=3) fail("9");
	}

	static void fail(String s) {
		System.out.println("Test failed "+s);
		System.exit(0);
	}
}

class GcTClassA {
	
	int foo() {
		return 1;
	}
}

class GcTClassB {
	
	int fee(int abc) {
		return 2;
	}
}

class GcTClassC {
	
	int fum(String s, int x) {
		return 3;
	}
}