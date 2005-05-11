package jbe;

import jbe.kfl.Mast;

public class BenchKfl extends BenchMark {

	public BenchKfl() {

		Mast.main(null);
	}

	public int test(int cnt) {

		int i;

		for (i=0; i<cnt; ++i) {
			Mast.loop();
		}

		return i;
	}

	public String getName() {

		return "Kfl";
	}

	public static void main(String[] args) {

		BenchMark bm = new BenchKfl();

		Execute.perform(bm);
	}
			
}
