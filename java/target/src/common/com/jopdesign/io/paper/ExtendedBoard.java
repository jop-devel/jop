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

public class ExtendedBoard extends BaseBoard {

	private final static int GPS_ADDRESS = 0xffff0030;
	private final static int PARALLEL_ADDRESS = 0xffff0040;
	
	private SerialPort gps;
	private ParallelPort parallel;

	ExtendedBoard() {
		gps = (SerialPort) jvmHWOCreate(GPS_ADDRESS);
		parallel = (ParallelPort) jvmHWOCreate(PARALLEL_ADDRESS);
	};
	
	static ExtendedBoard single = new ExtendedBoard();
	
	public static ExtendedBoard getExtendedFactory() {		
		return single;
	}

	public SerialPort getGpsPort() { return gps; }
	public ParallelPort getParallelPort() { return parallel; }
}
