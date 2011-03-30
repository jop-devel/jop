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
package csp.stream;

// TODOs:
//  Method names are currently quite ambiguous. Consider:
//    empty()  -> isEmpty()   // see also: java.util.Collection
//    full()   -> isFull()
//    cnt()    -> available() // or even size() as in Collection
//    free()   -> capacity()  // or even remainingCapacity()
//
//  For increased familiarity with the J2SE API, also consider:
//    checkedEnq() -> offer()
//    deq()        -> poll()
//
// MS: I don't care too much about those names. It's a very simple
// class with very simple names. At least I know what is meant ;-)
//
//  Do we really need the unchecked enq()?
// If one knows what he/she is doing this is more efficient...
/**
 * A non-blocking queue of byte buffer for single reader and single writer.
 * Classical usage is in an interrupt handler.
 * 
 * @author Martin Schoeberl
 * 
 */
public class BufferQueue {

	/**
	 * The buffer, manipulated by the reader and writer.
	 */
	private final byte[][] data;
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
	 * 
	 * @param array
	 *            of references. Has to contain one more element than the
	 *            intended buffer size.
	 */
	public BufferQueue(int capacity) {
		data = new byte[capacity + 1][];
		rdPtr = wrPtr = 0;

	}

	/**
	 * Is the buffer empty?
	 * 
	 * @return true if empty
	 */
	public synchronized boolean empty() {
		return rdPtr == wrPtr;
	}

	/**
	 * Is the buffer full?
	 * 
	 * @return true if full
	 */
	public synchronized boolean full() {
		int i = wrPtr + 1;
		// >= makes Wolfgang's DFA happy
		if (i >= data.length)
			i = 0;
		return i == rdPtr;
	}

	/**
	 * Unchecked enqueue of an element. Leaves the buffer corrupted when invoked
	 * on a full buffer.
	 * 
	 * @return
	 */
	public synchronized void enq(byte val[]) {
		int i = wrPtr;
		data[i++] = val;
		if (i >= data.length)
			i = 0;
		wrPtr = i;
	}

	/**
	 * Dequeue of an element. Returns null for an empty queue.
	 * 
	 * @return the value or null
	 */
	public synchronized byte[] deq() {
		if (rdPtr == wrPtr)
			return null;
		int i = rdPtr;
		byte[] val = data[i++];
		if (i >= data.length)
			i = 0;
		rdPtr = i;
		return val;
	}

	/**
	 * Buffer fill state
	 * 
	 * @return available items
	 */
	public synchronized int cnt() {
		int i = wrPtr - rdPtr;
		if (i < 0)
			i += data.length;
		return i;
	}

	/**
	 * Free elements in the buffer
	 * 
	 * @return free slots
	 */
	public synchronized int free() {
		return data.length - 1 - cnt();
	}

	/**
	 * Write one entry
	 * 
	 * @param val
	 *            the entry
	 * @return true if successful
	 */
	public synchronized boolean checkedEnq(byte val[]) {
		if (full())
			return false;
		enq(val);
		return true;
	}
}
