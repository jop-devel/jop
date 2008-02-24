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
 * Created on 16.06.2005
 *
 */
package gctest;

import com.jopdesign.sys.Native;


/**
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */
public class MyList {
	
	final static int CNT = 1000;
	
	private MyList next;
	private int a, b, c;
	
	
	static MyList list1, list2;
	
	
	static void test() {
		
		MyList list1, list2;
		
		MyList ptr = null;
		int i, j;
		list1 = list2 = null;
		
		for (int cnt=0; cnt<100;++cnt) {
			for (i=0; i<CNT; ++i) {
				ptr = list1;
//				System.out.println("new 1");
//				System.out.println(Native.getSP());
				list1 = new MyList();
				list1.a = i+cnt;
				list1.next = ptr;
				ptr = list2;
//				System.out.println("new 2");
//				System.out.println(Native.getSP());
				list2 = new MyList();
				list2.a = 1000+i+cnt;
				list2.next = ptr;
			}
			i = CNT;
			ptr = list1;
			j = 0;
			while (ptr!=null) {
				++j;
				--i;
				int val = ptr.a;
				if (val!=i+cnt) {
					System.out.println("Problem");
					System.exit(1);
				}
				ptr = ptr.next;
			}
			if (j!=CNT) {
				System.out.println("different size");
				System.exit(1);
			}
			i = CNT+1000+cnt;
			ptr = list2;
			j = 0;
			while (ptr!=null) {
				++j;
				--i;
				int val = ptr.a;
				if (val!=i) {
					System.out.println("Problem");
					System.exit(1);
				}
				ptr = ptr.next;
			}
			if (j!=CNT) {
				System.out.println("different size");
				System.exit(1);
			}
			System.out.print('*');
			// free both lists
			list1 = null;
			list2 = null;
		}
	}
	
	public static void main(String[] args) {
		test();
	}
}
