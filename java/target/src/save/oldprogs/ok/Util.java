/**
*	div. utilites, test von mul/div/rem
*
*	todo: div, mod mit 0x80000000 ist FALSCH!!!
*/

public class Util {

	public static int mul(int a, int b) {

		boolean neg = false;
		if (a<0) {
			neg = true;
			a = -a;
		}
		if (b<0) {
			neg = !neg;
			b = -b;
		}

		int c = 0;
		for (int i=0; i<32; ++i) {
			c <<= 1;
			if ((a & 0x80000000)!=0) {
				c += b;
			}
			a <<= 1;
		}
		if (neg) {
			c = -c;
		}
		return c;
	}
public static int add(int a, int b) { return a+b; }

	public static int div(int a, int b) {

		boolean neg = false;
		if (a<0) {
			neg = true;
			a = -a;
		}
		if (b<0) {
			neg = !neg;
			b = -b;
		}

		int c = 0;
		int r = 0;
		for (int i=0; i<32; ++i) {
			c <<= 1;
			r <<= 1;
			if ((a & 0x80000000)!=0) {
				r |= 1;
			}
			a <<= 1;
			if (r>=b) {
				r -= b;
				c |= 1;
			}
		}

		if (neg) {
			c = -c;
		}
		return c;
	}

	public static int rem(int a, int b) {

		boolean neg = false;
		if (a<0) {
			neg = true;
			a = -a;
		}
		if (b<0) {
			b = -b;
		}

		int c = 0;
		int r = 0;
		for (int i=0; i<32; ++i) {
			c <<= 1;
			r <<= 1;
			if ((a & 0x80000000)!=0) {
				r |= 1;
			}
			a <<= 1;
			if (r>=b) {
				r -= b;
				c |= 1;
			}
		}

		if (neg) {
			r = -r;
		}
		return r;
	}

/*
	public static void test(int a, int b) {

		System.out.println(a+"*"+b+"=\t"+mul(a, b)+" : "+(a*b));
		if (b!=0) {
			System.out.println(a+"/"+b+"=\t"+div(a, b)+" : "+(a/b));
			System.out.println(a+"%"+b+"=\t"+rem(a, b)+" : "+(a%b));
		}
	}

	public static void main(String[] args) {

		test(0, 0);
		test(0, 1);
		test(1, 0);
		test(3, 2);
		test(-3, 2);
		test(3, -2);
		test(-3, -2);
		test(10000, -3);
		test(10000, -287652987);
		test(123456789, -3);
		test(123456789, 123456789);
		test(-1, -1);
		test(0x7fffffff, 2);
		test(2, 0x7fffffff);

		test(0x80000000, 3);
		test(3, 0x80000000);

		test(0x80000000, 0x80000000);
		test(0x80000000, 0x7fffffff);
		test(0x7fffffff, 0x80000000);
		test(0x7fffffff, 0x7fffffff);

		test(0x80000001, 0x80000001);
		test(0x80000001, 0x7fffffff);
		test(0x7fffffff, 0x80000001);
		test(0x7fffffff, 0x7fffffff);

//		for (int i=1; i!=0; ++i) {
//			for (int j=1; j!=0; ++j) {
//				if (i*j != mul(i, j)) {
//					System.out.println("Fehler: "+i+" "+j);
//					System.exit(-1);
//				}
//			}
//			System.out.print(i+"\r");
		}
	}
*/
}
