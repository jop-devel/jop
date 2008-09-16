/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2008, Martin Schoeberl (martin@jopdesign.com)

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
package rtlib;

/**
 * A non-locking integer buffer for single reader and
 * single write. A classical usages is in an interrupt handler.
 *  
 * @author Martin Schoeberl
 *
 */
public class Buffer {
	
	/**
	 * The buffer, manipulated by the reader and writer.
	 * One issue: the array content should be volatile.
	 */
	public volatile int[] data;
	/**
	 * Read pointer, manipulated only by the reader.
	 */
	public volatile int rdPtr;
	/**
	 * Write pointer, manipulated only by the writer.
	 */
	public volatile int wrPtr;
	
	public Buffer(int size) {
		data = new int[size];
		rdPtr = wrPtr = 0;
	}
	
	public boolean empty() {
		return rdPtr==wrPtr;
	}
	
	public boolean full() {
		int i = wrPtr+1;
		if (i==data.length) i=0;
		return i==rdPtr;
	}

	public int read() {
		int i =rdPtr;
		int val = data[i++];
		if (i==data.length) i=0;
		rdPtr = i;
		return val;
	}
		
	public void write(int val) {
		int i = wrPtr;
		data[i++] = val;
		if (i==data.length) i=0;
		wrPtr = i;
	}
	
	/**
	 * Buffer fill state
	 * @return available items
	 */
	public int cnt() {
		int i = wrPtr-rdPtr;
		if (i<0) i+=data.length;
		return i;
	}
	
	/**
	 * Free elements in the buffer
	 * @return free slots
	 */
	public int free() {
		return data.length-1-cnt();
	}
	
	public int checkedRead() {
		if (empty()) return -1;
		return read();
	}

	public boolean checkedWrite(int val) {
		if (full()) return false;
		write(val);
		return true;
	}
}
