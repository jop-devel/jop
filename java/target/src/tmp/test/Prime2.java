//
//	Prime.java
//
//	prime performace test with uart
//
package test;

import com.jopdesign.sys.Native;
import util.Dbg;


public class Prime2 {
	
	public static void main( String s[] ) {

		int i, j, k, t1, t2, t3, us1, us2, us3, cnt;
		
		Dbg.initSerWait();
		
		int[] tab = new int[310];
		
		cnt = 0;

		t1 = Native.rd(Native.IO_CNT);
		t2 = Native.rd(Native.IO_CNT)-t1;  // #tics for 1 IO_CNT/IO_US_CNT read.

		us1 = Native.rd(Native.IO_US_CNT);
		us2 = Native.rd(Native.IO_US_CNT)-us1; // #us for 1 IO_CNT/IO_US_CNT read.
		
		t1 = Native.rd(Native.IO_CNT);
		us1 = Native.rd(Native.IO_US_CNT);

		for (i=3; i<2000; ++i) {
			for (j=2; j<i; ++j) {
				for (k=i; k>0; ) {
					k -= j;
				}
				if (k==0) break;
			}
			if (j==i) tab[cnt++] = i; // Saving prime number
		}

		for (k=0; k<cnt; k++) {
			Dbg.intVal(tab[k]); 
			while ((Native.rd(Native.IO_STATUS)&Native.MSK_UA_TDRE)==0); Native.wr(' ', Native.IO_UART);
		}
		
		t3 = Native.rd(Native.IO_CNT);
		us3= Native.rd(Native.IO_US_CNT);

		t3 = t3-t1-2*t2;
		us3 = us3-us1-2*us2;

		Dbg.wr('\r');
		Dbg.wr('\n');
		Dbg.intVal(cnt);
		Dbg.wr(" prime numbers found");
		
		Dbg.wr('\r');
		Dbg.wr('\n');
		Dbg.intVal(t3/100);
		Dbg.intVal(t3%100);
		
		Dbg.wr(" tics");
		Dbg.wr('\r');
		Dbg.wr('\n');
		Dbg.intVal(us3/100);
		Dbg.intVal(us3%100);
		Dbg.wr(" us");
		
		for (;;) ;	// stop program
	}
}
