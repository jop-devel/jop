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
