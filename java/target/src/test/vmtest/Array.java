package vmtest;

import util.*;

public class Array {

	public static void main(String[] agrgs) {

		Dbg.initSerWait();				// use serial line for debug output

		int ia[] = new int[3];
		int val = 1;

		Dbg.wr("iaload\n");
		val = ia[0];
		val = ia[2];

		Dbg.wr("iastore\n");
		ia[0] = val;
		ia[2] = val;

		for (int i=0; i<ia.length; ++i) {
			ia[i] = ~i;
		}
		for (int i=0; i<ia.length; ++i) {
			if (ia[i] != ~i) {
				Dbg.wr("ERROR");
			}
		}

		Dbg.wr("iaload bound\n");
//		val = ia[-1];
//		val = ia[3];

		Dbg.wr("iastore bound\n");
//		ia[-1] = val;
//		ia[3] = val;

		np(null);
	}

	public static void np(int[] ia) {

		int val = 1;
		Dbg.wr("iaload null pointer\n");
//		val = ia[1];
		Dbg.wr("iastore null pointer\n");
		ia[1] = val;
	}

}
