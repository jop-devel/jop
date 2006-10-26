package noc;

import com.jopdesign.sys.Native;

public class NoC {

	final static int NOC_BASE = 1024*512;
	final static int NOC_SIG = NOC_BASE+512;
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		int old_voter, voter, sens1, sens2, sens3;
		old_voter = -1;
		
		for (;;) {
			while (Native.rd(NOC_SIG)==0) {
				;	// busy wait for the signal
			}
			sens1 = Native.rd(NOC_BASE);
			sens2 = Native.rd(NOC_BASE+4);
			sens3 = Native.rd(NOC_BASE+8);
			voter = Native.rd(NOC_BASE+16);
			if (voter!=old_voter) {
				print(sens1, sens2, sens3, voter);
			}
			old_voter = voter;
			
//			if (Native.rd(NOC_SIG)!=0) {
//				System.out.println("missed the signal");
//			} else {
//				System.out.println("ok");
//			}
		}
	}
	private static void print(int sens1, int sens2, int sens3, int voter) {

		System.out.print("Sensor1=");
		System.out.print(sens1);
		System.out.print(" Sensor2=");
		System.out.print(sens2);
		System.out.print(" Sensor3=");
		System.out.print(sens3);
		System.out.print(" Voting=");
		System.out.print(voter&0xf);
		voter >>= 4;
		switch (voter) {
		case 0:
			System.out.println(" all sensors ok");
			break;
		case 1:
			System.out.println(" Sensor 1 wrong");
			break;
		case 2:
			System.out.println(" Sensor 2 wrong");
			break;
		case 3:
			System.out.println(" Sensor 3 wrong");
			break;
		case 4:
			System.out.println(" all sensors different");
			break;
		}
	}

}
