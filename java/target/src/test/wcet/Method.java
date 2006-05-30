package wcet;

public class Method {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		foo();
	}
	
	static void foo() {
		
		for (int i=0; i<10; ++i) { // @WCA loop=10
			a();
			b();
		}
	}

	static void a() {
		
		int val = 123;
		for (int i=0; i<10; ++i) { // @WCA loop=10
			val += val;
		}
	}

	static void b() {
		
		int val = 123;
		for (int i=0; i<5; ++i) { // @WCA loop=5
			val += c();
		}
	}
	
	static int c() {
		
		return 456;
	}
}
