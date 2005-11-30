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
public class Simple {

	public static void main(String[] args) {
		
		int t1, t2;
		int i, j;
		
		System.out.println("Wishbone counter:");
		for (i=0; i<10; ++i) {
			System.out.println(Native.rdMem(Const.WB_TS0));			
		}
		System.out.println();
		
		System.out.print("Wishbone slave 0: ");
		System.out.println(Native.rdMem(Const.WB_TS0+1));
		System.out.print("Wishbone slave 1: ");
		System.out.println(Native.rdMem(Const.WB_TS1+1));
		
		System.out.println("Writing values to the slaves...");
		Native.wrMem(123, Const.WB_TS0+1);
		Native.wrMem(456, Const.WB_TS1+1);
		Native.wrMem(222, Const.WB_TS2+1);
		Native.wrMem(333, Const.WB_TS3+1);

		System.out.print("Wishbone slave 0: ");
		System.out.println(Native.rdMem(Const.WB_TS0+1));
		System.out.print("Wishbone slave 1: ");
		System.out.println(Native.rdMem(Const.WB_TS1+1));
		System.out.print("Wishbone slave 2: ");
		System.out.println(Native.rdMem(Const.WB_TS2+1));
		System.out.print("Wishbone slave 3: ");
		System.out.println(Native.rdMem(Const.WB_TS3+1));
		

		t1 = Native.rd(Const.IO_CNT);
		t2 = Native.rd(Const.IO_CNT);
		System.out.print("JOP counter: ");
		System.out.println(t2-t1);

		t1 = Native.rdMem(Const.WB_TS0);
		t2 = Native.rdMem(Const.WB_TS0);
		System.out.print("Wishbone counter: ");
		System.out.println(t2-t1);

	}
}
