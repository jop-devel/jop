package test;

import util.*;

public class Hello {

	public static void main(String[] agrgs) {

		// need some initialisation
		Dbg.initSerWait();

		Dbg.wr("Hello World from JAVA!");

		// mark the end of the program
		// in emb. systems there is no exit()
//		for (;;);
//		we don't need this anymore!
	}
}
