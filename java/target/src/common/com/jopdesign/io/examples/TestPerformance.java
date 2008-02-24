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

package com.jopdesign.io.examples;

import util.Timer;

import com.jopdesign.io.*;
import com.jopdesign.sys.*;

public class TestPerformance {

	public static void main(String[] args) {

		int val = '*';
		System.out.println("Performance Test of HW Objects");

		DspioFactory fact = DspioFactory.getDspioFactory();		
		SerialPort sp = fact.getSerialPort();
		SysDevice sys = fact.getSysDevice();
		
		int ts, te, to;
		ts = Native.rdMem(Const.IO_CNT);
		te = Native.rdMem(Const.IO_CNT);
		to = te-ts;
		ts = Native.rdMem(Const.IO_CNT);
//		val = Native.rdMem(Const.IO_UART);
//		Native.wrMem(val, Const.IO_UART);
//		val = sp.data;
		sp.data = val;
		te = Native.rdMem(Const.IO_CNT);
		System.out.println(te-ts-to);

		
	}
}
