package vmtest;

import util.*;

public class Switch {

	public static void main(String[] agrgs) {

		Dbg.initSerWait();				// use serial line for debug output

		Dbg.wr("tableswitch\n");

		sw(2);
		sw(3);
		sw(4);
		sw(5);
		sw(6);

		Dbg.wr("lookupswitch\n");


		lsw(0);
		lsw(1);
		lsw(4);
		lsw(5);
		lsw(6);
		lsw(7);

		// mark the end of the program
		// in emb. systems there is no exit()
		for (;;);
	}

	public static void sw(int i) {

		int x = 0;
		switch (i) {
			case 3:
				Dbg.wr("3\n");
				break;
			case 4:
				Dbg.wr("4\n");
				break;
			case 5:
				Dbg.wr("5\n");
				break;
			default:
				Dbg.wr("default\n");
		}
	}

	public static void lsw(int i) {

		int x = 35;
		switch (i) {
			case 1:
				Dbg.wr("1\n");
				break;
			case 7:
				Dbg.wr("7\n");
				break;
			case 5:
				Dbg.wr("5\n");
				break;
			default:
				Dbg.wr("default\n");
		}
	}
}
