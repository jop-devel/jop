/*
 * Created on 30.05.2005
 *
 */
package dsp;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

/**
 * @author admin
 *
 */
public class Example {

	// This values depends on the ADC!!!
	final static int OFFSET = 1550;
	final static int FSAMP = 30000;
	
	final static int ABC = 65535;

	public static void main(String[] args) {
		
		int cnt = 0;
		int cnt2 = 0;
		int add = 1;
		int add2 = 3;
		int left, right;
		int val;
		int sig = 0;
		

		for(;;) {
			// wait for next sample time
			val = Native.rdMem(Const.WB_TS0);
			if (val>0) {
				continue;
			}
			// we don't use val
			
			cnt += add;
			if (cnt>=ABC) {
				cnt = ABC;
				add = -11;
			} else if (cnt<=0) {
				add = 11;
				cnt = 0;
			}
			cnt2 += add2;
			if (cnt2>=ABC) {
				cnt2 = ABC;
				add2 = -13;
			} else if (cnt2<=0) {
				cnt2 = 0;
				add2 = 13;
			}

			sig = (sig+1) & 0x7f;
			int rec = (sig & 0x40)<<1;
			int saw = sig;
			
			left = (rec*cnt + saw*(0xffff-cnt2))>>18;
			right = (saw*cnt2 + rec*(0xffff-cnt))>>18;

			
			

			val = left + (right<<16);
			Native.wrMem(val, Const.WB_TS0);
			
			// Test if we missed the sample output
			val = Native.rdMem(Const.WB_TS0);
			if (val<0) {
				System.out.print('*');
			}
		}



	}
}
