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
		
		System.out.println(Const.WB_USB_STATUS);
		System.out.println(Const.WB_USB_DATA);
		int val, flags;
		for(;;) {
			flags = Native.rdMem(Const.WB_USB_STATUS);
			if ((flags & Const.MSK_UA_RDRF)!=0) {
				val = Native.rdMem(Const.WB_USB_DATA);
				System.out.print((char) val);
				if ((flags & Const.MSK_UA_TDRE)!=0) {
					Native.wrMem(val, Const.WB_USB_DATA);
				}
			}
		}

	}
}
