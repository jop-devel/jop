/*
 * Created on 26.04.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package test;

import com.jopdesign.sys.*;
/**
 * @author admin
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Echo {

	public static void main(String[] args) {
		
		System.out.println("Hello");
		for (;;) {
/*			
			if ((Native.rd(Const.IO_STATUS)&Const.MSK_UA_RDRF)!=0) {
				int val = Native.rd(Const.IO_UART);
*/
			try {
				if (System.in.available()!=0) {
					int val = System.in.read();
					System.out.print(val);
					System.out.print(" ");
				}
			} catch (Exception e) { /* do nothing in JOP */ }
		}
	}
}
