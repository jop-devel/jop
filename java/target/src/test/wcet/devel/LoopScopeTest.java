package wcet.devel;

public class LoopScopeTest {

	public static void main(String [] args) {
		Big big = new Big();		
		measure(big);
	}

	public static void measure(Big big) {
		loop(big);
		loopedaccess(big);
	}

	// analysis detects the hits in access()
	public static void loop(Big big) {
		for (int k = 0; k < 8; k++) {
			if (big != null) {
				access(big);
			}
			big = big.next;
		}
	}

	public static void access(Big big) {
		int x = big.a + big.b + big.c + big.d;
		int y = big.e + big.f + big.g + big.h;
		int z = big.h + big.d + big.g + big.c;
		int w = big.f + big.b + big.e + big.a;	
	}

	// analysis does not detect any hits, although the code is
	// functionally equivalent to loop+access
	public static void loopedaccess(Big big) {
		for (int k = 0; k < 8; k++) {
			if (big != null) {
				int x = big.a + big.b + big.c + big.d;
				int y = big.e + big.f + big.g + big.h;
				int z = big.h + big.d + big.g + big.c;
				int w = big.f + big.b + big.e + big.a;
			}
			big = big.next;
		}
		
	}

}

class Big {
	int a;
	int b;
	int c;
	int d;
	int e;
	int f;
	int g;
	int h;
	Big next;
}