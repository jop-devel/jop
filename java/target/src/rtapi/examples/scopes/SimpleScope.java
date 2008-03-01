package examples.scopes;

import javax.realtime.LTMemory;
import javax.realtime.ScopedMemory;

public class SimpleScope {

	static class Abc {
		Object ref;
	}
	static class MyRunner implements Runnable {
		
		ScopedMemory outer;
		
		public void run() {
			System.out.println(abcref);
			for (int i=0; i<10; ++i) {
				String s = "i="+i;
			}
			// this should throw an exception
			// sa.ref = abcref;
			// Besides being recursive it is not
			// allowed to reenter a scope again
			// outer.enter(this);
		}
		Abc abcref;
		void setAbc(Abc abc) {
			abcref = abc;
		}
		void setOuter(ScopedMemory sc) {
			outer = sc;
		}
	}
	
	static Abc sa = new Abc();
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		final ScopedMemory scope = new LTMemory(0, 20000L);
		Runnable run = new Runnable() {
			public void run() {
				ScopedMemory inner = new LTMemory(0, 2000L);
				MyRunner r = new MyRunner();
				for (int i=0; i<100; ++i) {
					Abc abc = new Abc();
					r.setAbc(abc);
					r.setOuter(scope);
					for (int j=0; j<10; ++j) {
						inner.enter(r);
					}
				}
			}			
		};
		
		for (int i=0; i<20; ++i) {
			System.out.println("*");
			scope.enter(run);
			// this is a dangling reference
			// sa.ref.toString();
		}
	}

}
