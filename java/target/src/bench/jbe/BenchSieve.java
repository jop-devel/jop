package jbe;

public class BenchSieve extends BenchMark {

	final static int SIZE = 100;
	static boolean flags[];

	public BenchSieve() {

		flags = new boolean[SIZE+1];
	}

	public int test(int cnt) {

		int i, prime, k, iter, count;
		count=0;

		for (int j=0; j<cnt; ++j) {
			count=0;
			for(i=0; i<=SIZE; i++) flags[i]=true;
			for (i=0; i<=SIZE; i++) {
				if(flags[i]) {
					prime=i+i+3;
					for(k=i+prime; k<=SIZE; k+=prime)
						flags[k]=false;
					count++;
				}
			}
		}

		return count;
	}


	public String getName() {

		return "Sieve";
	}

	public static void main(String[] args) {

		BenchMark bm = new BenchSieve();

		Execute.perform(bm);
	}
			
}
