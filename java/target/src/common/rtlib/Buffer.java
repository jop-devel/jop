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
 * A non-blocking integer buffer for a single reader and
 * a single writer. Classical usage is in an interrupt handler.
 *  
 * @author Martin Schoeberl
 *
 */
public class Buffer {
	
	/**
	 * The buffer, manipulated by the reader and writer.
	 * One issue: the array content should be volatile.
	 */
	private volatile int[] data;
	/**
	 * Read pointer, manipulated only by the reader.
	 */
	private volatile int rdPtr;
	/**
	 * Write pointer, manipulated only by the writer.
	 */
	private volatile int wrPtr;
	
	/**
	 * Create a buffer (queue of fixed size)
	 * @param size of the buffer
	 */
	public Buffer(int size) {
		data = new int[size+1];
		rdPtr = wrPtr = 0;
	}
	
	/**
	 * Return capacity of the buffer
	 * @return the value
	 */
	public int capacity() {
		return data.length;
	}

	/**
	 * Is the buffer empty?
	 * @return true if empty
	 */
	public boolean empty() {
		return rdPtr==wrPtr;
	}
	
	/**
	 * Is the buffer full?
	 * @return true if full
	 */
	public boolean full() {
		int i = wrPtr+1;
		// >= makes Wolfgang's DFA happy
		if (i>=data.length) i=0;
		return i==rdPtr;
	}

	/**
	 * Unchecked read from the buffer. Leaves the buffer corrupted
	 * when invoked on an empty buffer. 
	 * @return the value
	 */
	public int read() {
		int i =rdPtr;
		int val = data[i++];
		if (i>=data.length) i=0;
		rdPtr = i;
		return val;
	}
		
	/**
	 * Unchecked write to the buffer. Leaves the buffer corrupted
	 * when invoked on a full buffer. 
	 * @return
	 */
	public void write(int val) {
		int i = wrPtr;
		data[i++] = val;
		if (i>=data.length) i=0;
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
	
	/**
	 * Read one entry
	 * @return entry or -1 on empty buffer
	 */
	public int checkedRead() {
		if (empty()) return -1;
		return read();
	}

	/**
	 * Write one entry
	 * @param val the entry
	 * @return true if successful
	 */
	public boolean checkedWrite(int val) {
		if (full()) return false;
		write(val);
		return true;
	}
}
