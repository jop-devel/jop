/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2009, Martin Schoeberl (martin@jopdesign.com)

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


/**
 * 
 */
package jeopard;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Native;

/**
 * Communication between JOP and jamaicaVM for the JEOPARD project.
 * Extend this class for the communication of control values.
 * 
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */
public class Control {
	
	int[] data;
	boolean avail;
	
	public Control() {
		data = new int[getSize()-getSuperSize()];
	}
	
	final int getSize() {
		int ptr = Native.toInt(this);
		ptr = Native.rdMem(ptr+1);	// pointer to MTAB
		int size = Native.rdMem(ptr+Const.MTAB2CLINFO+Const.CLASS_SIZE);
		return size;
	}
	
	final int getSuperSize() {
		int ptr = Native.toInt(this);
		ptr = Native.rdMem(ptr+1);	// pointer to MTAB
		ptr = Native.rdMem(ptr+Const.MTAB2CLINFO+Const.CLASS_SUPER);
		int size = Native.rdMem(ptr+Const.CLASS_SIZE);
		return size;
	}

	void pack() {
		int ptr = Native.toInt(this);
		ptr = Native.rdMem(ptr);	// pointer to instance
		int off = getSuperSize();
		for (int i=0; i<data.length; ++i) {
			data[i] = Native.rdMem(ptr+i+off);
		}
	}
	
	void unpack() {
		int ptr = Native.toInt(this);
		ptr = Native.rdMem(ptr);	// pointer to instance
		int off = getSuperSize();
		for (int i=0; i<data.length; ++i) {
			Native.wrMem(data[i], ptr+i+off);
		}		
	}
	
	public void send() {
		pack();
		avail = true;
	}
	
	public boolean dataAvail() {
		return avail;
	}
	
	public void receive() {
		if (!avail) {
			System.out.println("Data not available - use old value");
		}
		unpack();
		avail = false;
	}
}
