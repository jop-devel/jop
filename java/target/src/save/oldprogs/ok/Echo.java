//
//	Echo.java will be copied to Jop.java
//

public class Echo {

	public static void main( String s[] ) {

		int ch;

		print_char('H');
		print_char('a');
		print_char('l');
		print_char('l');
		print_char('o');
		print_char('\r');
		print_char('\n');

		for (;;) {
			while ((JopSys.rd(1)&2)==0); ch = JopSys.rd(2);
			print_char(ch);
		}
	}

	static void wait_serial() {
		while ((JopSys.rd(1)&1)==0) ;
	}

	static void print_char(int i) {
		wait_serial();
		JopSys.wr(i, 2);
	}

}
