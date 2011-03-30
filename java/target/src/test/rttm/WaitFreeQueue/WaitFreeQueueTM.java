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

public class WaitFreeQueueTM extends WaitFreeQueue {
	/** List nodes for Queue **/
	protected final static class Node {
		protected final Object value;
		protected volatile Node next = null;
		protected volatile Node previous = null;

		/** Make a new node with indicated item, and null link **/
		protected Node(Object x) {
			value = x;
		}
	}

	/** Head of list is always a dummy node **/
	protected volatile Node head = new Node(null);
	/** Pointer to last node on list **/
	protected volatile Node tail = new Node(null);
	protected volatile int size = 0;

	public WaitFreeQueueTM() {
		head.next = tail;
		head.previous = null;
		tail.previous = head;
		tail.next = null;
	}

	public void put(Object x) {
		Node n = new Node(x);
		Native.wrMem(1, Const.MAGIC); // start transaction
		n.previous = tail.previous;
		n.next = tail;
		tail.previous.next = n;
		tail.previous = n;
		Native.wrMem(0, Const.MAGIC); // end transaction
	}

	public Object get() {
		Node n = null;
		for (;;) {
			if (head.next == tail) {
				continue;
			}
			Native.wrMem(1, Const.MAGIC); // start transaction
			n = null;
			if (head.next != tail) {
				n = head.next;
				head.next = n.next;
				n.next.previous = head;
			}
			Native.wrMem(0, Const.MAGIC); // end transaction
			if (n != null)
				return n.value;
		}
	}

	public boolean offer(Object x, long msecs) {
		// TODO Auto-generated method stub
		return false;
	}

	public Object peek() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object poll(long msecs) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object take() {
		// TODO Auto-generated method stub
		return null;
	}
}
