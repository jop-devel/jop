//
//	Stat.java will be copied to Jop.java
//

public class Stat {

	static int a;

	static boolean b;

	static void setA(int i) {
		a=i;
	}
	static int getA() {
		return a;
	}

	public static void main( String s[] ) {

		int ch;
		int str[] = { 'H', 'a', 'l', 'l', 'o', '\n' };


		for (int i=0; i<str.length; ++i)
			print_char(str[i]);

		print_char('0'+getA());
		setA(5);
		print_char('0'+getA());

		b = false;
		if (b) print_char('t'); else print_char('f');
		b = true;
		if (b) print_char('t'); else print_char('f');
		
		for (;;) ;
	}

	static void wait_serial() {
		while ((JopSys.rd(1)&1)==0) ;
	}

	static void print_char(int i) {
		wait_serial();
		JopSys.wr(i, 2);
	}

}
