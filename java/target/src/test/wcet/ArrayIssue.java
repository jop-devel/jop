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

package wcet;

import com.jopdesign.sys.*;

/**
 * WCET timing issue - measurement differs from WCET analysis
 * @author martin
 *
 */
public class ArrayIssue {

	int[][] block = { {99, 104}, {109, 113} };

	public static void main(String[] args) {
		ArrayIssue ai = new ArrayIssue();
		ai.fdct(ai.block);
	}
	
	void fdct(int[][] block) {
	     int t1, diff;
	     t1 = Native.rd(Const.IO_CNT);
	     t1 = Native.rd(Const.IO_CNT)-t1;
	     diff = t1;

	     JVMHelp.wr('*');
	     
	     int[] ia = new int[1];
	     
	     t1 = Native.rd(Const.IO_CNT);
//	     int tmp0 = block[0][0];
//	     block[0][0] = 0;
	     ia[0] = 0;

//	     int x[] = block[0];
//	     int i = x[0];
	     
	     t1 = Native.rd(Const.IO_CNT)-t1;
	     JVMHelp.wr('-');
	     System.out.println(t1-diff);
	     
	}
}
