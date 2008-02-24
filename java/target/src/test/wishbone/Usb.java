/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

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
			flags = Native.rdMem(Const.IO_USB_STATUS);
			if ((flags & Const.MSK_UA_RDRF)!=0) {
				val = Native.rdMem(Const.IO_USB_DATA);
				if ((flags & Const.MSK_UA_TDRE)!=0) {
					Native.wrMem(val, Const.IO_USB_DATA);
				}
			}
		}
	}
	
	public static void read() {

		int tim = Native.rd(Const.IO_US_CNT)+1000000;
		int cnt = 0;
		for(;;) {
			while ((Native.rdMem(Const.IO_USB_STATUS) & Const.MSK_UA_RDRF)!=0) {
				Native.rdMem(Const.IO_USB_DATA);
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

		int tim = Native.rd(Const.IO_US_CNT)+1000000;
		int cnt = 0;
		for(;;) {
			while ((Native.rdMem(Const.IO_USB_STATUS) & Const.MSK_UA_TDRE)!=0) {
				Native.wrMem('x', Const.IO_USB_DATA);
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
