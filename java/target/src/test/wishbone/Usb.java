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
public class Usb {

	public static void main(String[] args) {

		System.out.println("USB Test");
		read();
	}
	
	public static void echo() {

		int val, flags;
		for(;;) {
			flags = Native.rdMem(Const.WB_USB_STATUS);
			if ((flags & Const.MSK_UA_RDRF)!=0) {
				val = Native.rdMem(Const.WB_USB_DATA);
				if ((flags & Const.MSK_UA_TDRE)!=0) {
					Native.wrMem(val, Const.WB_USB_DATA);
				}
			}
		}
	}
	
	public static void read() {

		int val, flags;
		int tim = Native.rd(Const.IO_US_CNT)+1000000;
		int cnt = 0;
		for(;;) {
			while ((Native.rdMem(Const.WB_USB_STATUS) & Const.MSK_UA_RDRF)!=0) {
				Native.rdMem(Const.WB_USB_DATA);
				++cnt;
			}
			if (tim-Native.rd(Const.IO_US_CNT) < 0) {
				cnt /= 1024;
				System.out.print(cnt);
				System.out.print(" KB/s    \r");
				cnt = 0;
				tim = Native.rd(Const.IO_US_CNT)+1000000;;
			}
		}
	}

	public static void write() {

		int val, flags;
		int tim = Native.rd(Const.IO_US_CNT)+1000000;
		int cnt = 0;
		for(;;) {
			while ((Native.rdMem(Const.WB_USB_STATUS) & Const.MSK_UA_TDRE)!=0) {
				Native.wrMem('x', Const.WB_USB_DATA);
				++cnt;
			}
			if (tim-Native.rd(Const.IO_US_CNT) < 0) {
				cnt /= 1024;
				System.out.print(cnt);
				System.out.print(" KB/s    \r");
				cnt = 0;
				tim = Native.rd(Const.IO_US_CNT)+1000000;;
			}
		}
	}

}
