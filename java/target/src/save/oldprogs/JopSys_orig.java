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

	static int rd(int adr) {
		switch (adr) {
			case 0 :			// in port
				return 0;
			case 1 :
				return status();
			case 2 :
				return uart_rd();
			case 3 :
				return ecp_rd();
			case 10 :
				return cnt();
			case 11 :
				return cntms();
		}
		return 0;
	}
	static void wr(int val, int adr) {

		switch (adr) {
			case 0 :			// out port
				outp(val);
				break;
			case 2 :
				uart_wr(val);
				break;
			case 3 :
				ecp_wr(val);
				break;
			default:
				System.out.print("addr: "+adr+" data: "+val);
		}
	}

//
//	simulation of JOP io
//
	private static int status() {
		return -1;
	}
	private static int uart_rd() {
		return 'U';
	}
	private static void uart_wr(int ch) {
		System.out.print((char) ch);
	}
	private static int ecp_rd() {
		return 'E';
	}
	private static void ecp_wr(int ch) {
		System.out.print((char) ch);
	}
	private static int cnt() {

		Date tm = new Date();

		long l = tm.getTime();
		if (firstTime==0L) {
			firstTime = l;
		}
		l -= firstTime;
		return (int) (l*24*1000);
	}
	private static int cntms() {

		Date tm = new Date();

		long l = tm.getTime();
		if (firstTime==0L) {
			firstTime = l;
		}
		l -= firstTime;
		return (int) (l);
	}
	private static void outp(int val) {
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
