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
package jembench.ejip;

/**
 * A non-locking buffer for single reader and
 * single writer. Classical usage is in an interrupt handler.
 *  
 * @author Martin Schoeberl
 *
 */
public class PacketQueue {
	
	/**
	 * The buffer, manipulated by the reader and writer.
	 */
	private volatile Packet[] data;
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
	public PacketQueue(int capacity) {
		// the following does not work on JOP with
		// the Sun javac as it produces a checkcast
		// and we have not yet implemented checkcast
		// for array types
//		T[] ts = (T[]) new Object[capacity+1];
//		data = ts;
		data = new Packet[capacity+1];
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
	 * Unchecked enqueue of an element. Leaves the buffer corrupted
	 * when invoked on a full buffer. 
	 * @return
	 */
	public void enq(Packet val) {
		int i = wrPtr;
		data[i++] = val;
		if (i>=data.length) i=0;
		wrPtr = i;
	}

	/**
	 * Dequeue of an element. Returns null for an empty queue.
	 * @return the value or null
	 */
	public Packet deq() {
		if (rdPtr==wrPtr) return null;
		int i =rdPtr;
		Packet val = data[i++];
		if (i>=data.length) i=0;
		rdPtr = i;
		return val;
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
	 * Write one entry
	 * @param val the entry
	 * @return true if successful
	 */
	public boolean checkedEnq(Packet val) {
		if (full()) return false;
		enq(val);
		return true;
	}
}
