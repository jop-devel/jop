import java.util.*;

public class JopSys {
//
// Reihenfolge so lassen!!!
// ergibt static funktionsnummern:
//		1 rd
//		2 wr
//		...
//
	static long firstTime = 0;

	static int rd( int adr ) {
		return 1;
	}
	static void wr( int adr, int val ) {

		char ch;
		
		ch = (char) val;
		System.out.print( ch );
	}
	static int status() {
		return -1;
	}
	static int uart_rd() {
		return 'U';
	}
	static void uart_wr(int ch) {
		System.out.print((char) ch);
	}
	static int ecp_rd() {
		return 'E';
	}
	static void ecp_wr(int ch) {
		System.out.print((char) ch);
	}
	static int cnt() {

		Date tm = new Date();

		long l = tm.getTime();
		if (firstTime==0L) {
			firstTime = l;
		}
		l -= firstTime;
		return (int) (l*24*1000);
	}
	static int cntms() {

		Date tm = new Date();

		long l = tm.getTime();
		if (firstTime==0L) {
			firstTime = l;
		}
		l -= firstTime;
		return (int) (l);
	}
	static void outp(int val) {
		System.out.println("out: "+val);
	}

	public static void main(String[] arg) {
		int i = cnt();
		for(int j=0; j<10;++j) {
			while(i == cnt());
			int k = i;
			i = cnt();
			System.out.println(i+" "+(i-k));
		}
	}
}
