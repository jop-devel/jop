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

package com.jopdesign.io.paper;

import com.jopdesign.io.SerialPort;

public class IOSystem {
	
	private IOSystem() {}
	
	static native ParallelPort jvmPPCreate();
	static native SerialPort jvmSPCreate(int nr);
	
	// some JVM mechanism to create the hardware object
	private static ParallelPort pp = jvmPPCreate();
	
	private static SerialPort sp = jvmSPCreate(0);
	private static SerialPort gps = jvmSPCreate(1);
	
	public static ParallelPort getParallelPort() {
		return pp;
	}
	
	public static SerialPort getSerialPort() {
		return sp;
	}
	
	public static SerialPort getGpsPort() {
		return gps;
	}
}
