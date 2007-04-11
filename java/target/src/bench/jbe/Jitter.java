/*
 * Created on 13.10.2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package jbe;


/**
 * @author martin
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Jitter {
	
	public static final int CNT = 1000;

	public static void main(String[] args) {

		test(new BenchKfl());
	}


	public static void test(BenchMark b) {

		int[] results = new int[CNT];
		int t;
		int max = 0;
		int min = 2000000000;

		LowLevel.msg("Jitter");
		LowLevel.msg(b.getName());
		LowLevel.lf();
		
		for (int i=0; i<CNT; ++i) {
			t = LowLevel.timeMicros();
/*	to check if high resolution counter is correct
			long l = System.currentTimeMillis()+100;
			while (System.currentTimeMillis()-l < 0);
*/
			b.test(1);
			t = LowLevel.timeMicros()-t;
			results[i] = t;
		}
		for (int i=0; i<CNT; ++i) {
			t = results[i];
			if (t>max) max = t;
			if (t<min) min = t;
			LowLevel.msg(t);
			LowLevel.lf();
		}

		LowLevel.lf();
		LowLevel.msg("min", min);
		LowLevel.msg("max", max);
		LowLevel.lf();
		if (min!=0) LowLevel.msg("ratio", max/min);
		LowLevel.lf();

	}
}
