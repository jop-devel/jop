package vmtest;

import util.*;

public class FloatTest {

	public static void main(String[] agrgs) {

		Dbg.initSerWait();				// use serial line for debug output

		Dbg.wr("FloatTest\n");

		float f1 = 1.3F;
		float f2 = 2.9F;

		float f3 = f1+f2;

		int i = (int) f3;
		Dbg.intVal(i);

		f3 = f1-f2;
		i = (int) f3;
		Dbg.intVal(i);

		// mark the end of the program
		// in emb. systems there is no exit()
		for (;;);
	}
}
