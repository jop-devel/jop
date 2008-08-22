/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008, Jack Whitham 

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

public final class ControlChannel extends HardwareObject {
	
	// Some constants for the status port
	public static final int MASK_TDRE = 1;
	public static final int MASK_RDRF = 2;
	public static final int MASK_LOCK = 4;
	public static final int MASK_RELEASE = 8;
	public static final int MASK_ADVANCE = 16;

	public volatile int status;
	public volatile int data;
	

	public void init() {}


	public boolean tryToAcquireLock() {
        // read status register - if you get '1' in the LOCK bit,
        // then you've got the lock; otherwise you must try again.
		return (status & MASK_LOCK) != 0;
	}

	public void releaseLock() {
        // write to the status register with '1' in the RELEASE bit
        // to release the lock
		status = MASK_RELEASE;
	}

	public void advance() {
        // write to the status register with '1' in the ADVANCE bit
        // to clear the read register for the next word
		status = MASK_ADVANCE;
    }

	public int read() {
		return data;
	}
	
	public void write(int word) {
		data = word;
	}

	public boolean rxFull() {
		return (status & MASK_RDRF) != 0;
	}
	
	public boolean txEmpty() {
		return (status & MASK_TDRE) != 0;
	}
	
	
}
