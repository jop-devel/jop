package test;

import util.*;

public class Hello {

	public static void main(String[] agrgs) {

		// need some initialisation
		Dbg.initSer();

		Dbg.wr("Hello World!");

		// mark the end of the program
		// in emb. systems there is no exit()
		for (;;);
	}
}
