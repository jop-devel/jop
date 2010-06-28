/*
  File: WaitFreeQueue.java

  Originally written by Doug Lea and released into the public domain.
  This may be used for any purposes whatsoever without acknowledgment.
  Thanks for the assistance and support of Sun Microsystems Labs,
  and everyone contributing, testing, and using this code.

  History:
  Date       Who                What
  16Jun1998  dl               Create public version
   5Aug1998  dl               replaced int counters with longs
  17nov2001  dl               Simplify given Bill Pugh's observation
                              that counted pointers are unnecessary.
 */

/**
 * A wait-free linked list based queue implementation.
 * <p>
 *
 * While this class conforms to the full Channel interface, only the
 * <code>put</code> and <code>poll</code> methods are useful in most
 * applications. Because the queue does not support blocking
 * operations, <code>take</code> relies on spin-loops, which can be
 * extremely wasteful.  <p>
 *
 * This class is adapted from the algorithm described in <a
 * href="http://www.cs.rochester.edu/u/michael/PODC96.html"> Simple,
 * Fast, and Practical Non-Blocking and Blocking Concurrent Queue
 * Algorithms</a> by Maged M. Michael and Michael L. Scott.  This
 * implementation is not strictly wait-free since it relies on locking
 * for basic atomicity and visibility requirements.  Locks can impose
 * unbounded waits, although this should not be a major practical
 * concern here since each lock is held for the duration of only a few
 * statements. (However, the overhead of using so many locks can make
 * it less attractive than other Channel implementations on JVMs where
 * locking operations are very slow.)  <p>
 *
 * @see BoundedLinkedQueue
 * @see LinkedQueue
 * 
 * <p>[<a href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html"> Introduction to this package. </a>]

 **/

package rttm.WaitFreeQueue;

import com.jopdesign.sys.Native;

public class WaitFreeQueueCAS_TM extends WaitFreeQueue {

	/*
	 * This is a straightforward adaptation of Michael & Scott algorithm, with
	 * CAS's simulated via per-field locks, and without version numbers for
	 * pointers since, under Java Garbage Collection, you can never see the
	 * "wrong" node with the same address as the one you think you have.
	 */

	/** List nodes for Queue **/
	protected final static class Node {
		protected final Object value;
		protected volatile Node next;

		/** Make a new node with indicated item, and null link **/
		protected Node(Object x) {
			value = x;
		}

		/** Simulate a CAS operation for 'next' field **/
		protected boolean CASNext(Node oldNext, Node newNext) {
			boolean result = false;
			Native.wrMem(1, Const.MAGIC); // start transaction
			if (next == oldNext) {
				next = newNext;
				result = true;
			}
			Native.wrMem(0, Const.MAGIC); // end transaction
			return result;
		}
	}

	/** Head of list is always a dummy node **/
	protected volatile Node head = new Node(null);
	/** Pointer to last node on list **/
	protected volatile Node tail = head;

	/** Lock for simulating CAS for tail field **/
	protected final Object tailLock = new Object();

	/** Simulate CAS for head field **/
	protected boolean CASHead(Node oldHead, Node newHead) {
		boolean result = false;
		Native.wrMem(1, Const.MAGIC); // start transaction
		if (head == oldHead) {
			head = newHead;
			result = true;
		}
		Native.wrMem(0, Const.MAGIC); // end transaction
		return result;
	}

	/** Simulate CAS for tail field **/
	protected boolean CASTail(Node oldTail, Node newTail) {
		boolean result = false;
		Native.wrMem(1, Const.MAGIC); // start transaction
		if (tail == oldTail) {
			tail = newTail;
			result = true;
		}
		Native.wrMem(0, Const.MAGIC); // end transaction
		return result;
	}

	public void put(Object x) {
		Node n = new Node(x);

		for (;;) {
			Node t = tail;
			// Try to link new node to end of list.
			if (t.CASNext(null, n)) {
				// Must now change tail field.
				// This CAS might fail, but if so, it will be fixed by others.
				CASTail(t, n);
				return;
			}

			// If cannot link, help out a previous failed attempt to move tail
			CASTail(t, t.next);
		}
	}

	public boolean offer(Object x, long msecs) {
		put(x);
		return true;
	}

	/** Main dequeue algorithm, called by poll, take. **/
	protected Object extract() {
		for (;;) {
			Node h = head;
			Node first = h.next;

			if (first == null) {
				return null;
			}

			Object result = first.value;
			if (CASHead(h, first))
				return result;
		}
	}

	public Object peek() {
		Node first = head.next;

		if (first == null)
			return null;

		// Note: This synch unnecessary after JSR-133.
		// It exists only to guarantee visibility of returned object,
		// No other synch is needed, but "old" memory model requires one.
		// synchronized(this) {
		return first.value;
		// }
	}

	/**
	 * Spin until poll returns a non-null value. You probably don't want to call
	 * this method. A Thread.sleep(0) is performed on each iteration as a
	 * heuristic to reduce contention. If you would rather use, for example, an
	 * exponential backoff, you could manually set this up using poll.
	 **/
	public Object take() {
		for (;;) {
			Object x = extract();
			if (x != null)
				return x;
			else
				try {
					Thread.sleep(0);
				} catch (Exception ex) {
				}
		}
	}

	/**
	 * Spin until poll returns a non-null value or time elapses. if msecs is
	 * positive, a Thread.sleep(0) is performed on each iteration as a heuristic
	 * to reduce contention.
	 **/
	public Object poll(long msecs) {
		if (msecs <= 0)
			return extract();

		long startTime = System.currentTimeMillis();
		for (;;) {
			Object x = extract();
			if (x != null)
				return x;
			else if (System.currentTimeMillis() - startTime >= msecs)
				return null;
			else
				try {
					Thread.sleep(0);
				} catch (Exception ex) {
				}
		}

	}
}
