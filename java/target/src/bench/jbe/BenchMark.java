package jbe;

public abstract class BenchMark {

	/**
	 * Provide the test function inside a loop running
	 * cnt times.
	 * @param cnt
	 * @return
	 */
	public abstract int test(int cnt);

	/**
	 * Compensate for any overhead in the test function.
	 * @param cnt
	 * @return
	 */
	public int overhead(int cnt) {
		return 0;
	}

/*
	int overheadMinus(int cnt) {

		return 0;
	}
*/

	/**
	 * Provide the name of the benchmark.
	 */
	public abstract String getName();

	public static void main(String[] args) {

		// BenchMark bm = new BenchMark();
		// Execute.perform(bm);
	}
			
}
