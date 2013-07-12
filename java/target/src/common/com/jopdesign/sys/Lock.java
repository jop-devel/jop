package com.jopdesign.sys;

class Lock {

	// dumbed-down version of rtlib.Buffer
	static class Pool {
		/**
		 * The buffer, manipulated by the reader and writer.
		 * One issue: the array content should be volatile.
		 */
		static volatile int[] data;
		/**
		 * Read pointer, manipulated only by the reader.
		 */
		static volatile int rdPtr = 0;
		/**
		 * Write pointer, manipulated only by the writer.
		 */
		static volatile int wrPtr = 0;
	
		/**
		 * Create a buffer (queue of fixed size)
		 * @param size of the buffer
		 */
		Pool(int size) {
			data = new int[size+1];
		}

		/**
		 * Is the buffer empty?
		 * @return true if empty
		 */
		static public boolean empty() {
			return rdPtr==wrPtr;
		}

		/**
		 * Is the buffer full?
		 * @return true if full
		 */
		static public boolean full() {
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
		static public int read() {
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
		static public void write(int val) {
			int i = wrPtr;
			data[i++] = val;
			if (i>=data.length) i=0;
			wrPtr = i;
		}
	}

	volatile int level;
	volatile int holder; // use int to avoid putfield_ref
	volatile int queue;
	volatile int tail;

	// keep locks alive during GC
	private static Lock[] lifeline = new Lock[Native.rdMem(Const.IO_CPUCNT)*24];

	// fill object pool
	static {
		int i = 0;
		Pool.data = new int[Native.rdMem(Const.IO_CPUCNT)*24];
		while (!Pool.full()) {
			Lock l = new Lock();
			Pool.write(Native.toInt(l));
			lifeline[i++] = l;
		}
	}
}