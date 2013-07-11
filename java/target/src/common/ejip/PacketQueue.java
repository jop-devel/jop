/*
 * Copyright (c) Martin Schoeberl, martin@jopdesign.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *	This product includes software developed by Martin Schoeberl
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 */


/**
 * 
 */
package ejip;

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
