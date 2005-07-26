/*
 * Created on 30.05.2005
 *
 */
package wishbone;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

/**
 * @author admin
 *
 */
public class Guitar {

	// This values depend on the ADC!!!
	final static int OFFSET = 1550;
	final static int FSAMP = 30000;

	public static void main(String[] args) {
		
		int i, j;
		int left, right;
		int val;
		int min, max;
		int t1, t2;


		min = 999999;
		max = 0;

		int[] samples = new int[100];
		
		i = 0;

		for(;;) {
			val = Native.rdMem(Const.WB_TS0);
			if (val>0) {
				continue;
			}
			val &= 0xffff;

			i = (i+1) & 0xffff;
			j = i;
			if (j>0x7fff) {
				j = 0xffff-i;
			}
			left = (val*j)>>15;
			right = (val*(0x7fff-j))>>15;

			val = left + (right<<16);
			Native.wrMem(val, Const.WB_TS0);
			
			// Test if we missed the sample output
			val = Native.rdMem(Const.WB_TS0);
			if (val<0) {
				System.out.print('*');
			}
/*
			if (val>max) max = val;
			if (val<min) min = val;
			System.out.print(min);
			System.out.print(max);
			System.out.println(val);
*/
		}

/*
		t1 = Native.rd(Const.IO_CNT);
		t2 = Native.rd(Const.IO_CNT);
		System.out.print("JOP counter: ");
		System.out.println(t2-t1);
*/


	}
}
