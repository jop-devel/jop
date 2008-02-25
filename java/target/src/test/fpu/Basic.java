/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2007, Stephan Ramberger

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

package fpu;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;
import com.jopdesign.sys.SoftFloat;

public class Basic {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		int a, b, c;
		a = SoftFloat.int32_to_float32(3);
		b = SoftFloat.int32_to_float32(2);
		Native.wrMem(a, Const.FPU_A);
		Native.wrMem(b, Const.FPU_B);
		Native.wrMem(Const.FPU_OP_ADD, Const.FPU_OP);
		c = Native.rdMem(Const.FPU_RES);
		c = SoftFloat.float32_to_int32_round_to_zero(c);
		System.out.println(c);
	}

}
