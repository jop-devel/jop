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

package com.jopdesign.io;

public final class SerialPort extends HardwareObject {
	
	// Some constants for the status port
	public static final int MASK_TDRE = 1;
	public static final int MASK_RDRF = 2;
	public static final int MASK_PARITY_ERR = 4;

	public static final int PARITY_ODD = 1;
	public static final int PARITY_EVEN = 3;
	
	public volatile int status;
	public volatile int data;
	

	/**
	 * Non-blocking read of a character. If the input buffer
	 * is empty, the last value is returned.
	 * @return
	 */
	final public int read() {
		return data;
	}
	
	/**
	 * Non-blocking write of a character. If the transmit buffer
	 * is full, the character is lost.
	 * @param ch
	 */
	final public void write(int ch) {
		data = ch;
	}
	
	final public void setParityMode(int parity_mode) {
		status = parity_mode;
	}
	
	/**
	 * A character is available in the input buffer.
	 * @return
	 */
	final public boolean rxFull() {
		return (status & MASK_RDRF) != 0;
	}
	
	/**
	 * The transmit buffer is empty.
	 * @return
	 */
	final public boolean txEmpty() {
		return (status & MASK_TDRE) != 0;
	}

	public boolean parityError() {
		return (status & MASK_PARITY_ERR) != 0;
	}
	
}
