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
 * A non-locking buffer for single reader and
 * single writer. Classical usage is in an interrupt handler.
 *  
 * @author Martin Schoeberl
 *
 */
public class SingleReaderWriterQueue<T> {
	
	/**
	 * The buffer, manipulated by the reader and writer.
	 * One issue: the array content should be volatile.
	 */
	private volatile T[] data;
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
	 * @param array of references. Has to contain one
	 * more element than the intended buffer size.
	 */
	public SingleReaderWriterQueue(T[] buffer) {
		data = buffer;
		rdPtr = wrPtr = 0;
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
	public T read() {
		int i =rdPtr;
		T val = data[i++];
		if (i>=data.length) i=0;
		rdPtr = i;
		return val;
	}
		
	/**
	 * Unchecked write to the buffer. Leaves the buffer corrupted
	 * when invoked on a full buffer. 
	 * @return
	 */
	public void write(T val) {
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
	public T checkedRead() {
		if (rdPtr==wrPtr) return null;
		return read();
	}

	/**
	 * Write one entry
	 * @param val the entry
	 * @return true if successful
	 */
	public boolean checkedWrite(T val) {
		if (full()) return false;
		write(val);
		return true;
	}
}
