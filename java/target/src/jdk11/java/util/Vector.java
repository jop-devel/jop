/* Vector.java -- Class that provides growable arrays.
 Copyright (C) 1998, 1999, 2000, 2001, 2004, 2005  Free Software Foundation, Inc.

 This file is part of GNU Classpath.

 GNU Classpath is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2, or (at your option)
 any later version.

 GNU Classpath is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with GNU Classpath; see the file COPYING.  If not, write to the
 Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 02110-1301 USA.

 Linking this library statically or dynamically with other modules is
 making a combined work based on this library.  Thus, the terms and
 conditions of the GNU General Public License cover the whole
 combination.

 As a special exception, the copyright holders of this library give you
 permission to link this library with independent modules to produce an
 executable, regardless of the license terms of these independent
 modules, and to copy and distribute the resulting executable under
 terms of your choice, provided that you also meet, for each linked
 independent module, the terms and conditions of the license of that
 module.  An independent module is a module which is not derived from
 or based on this library.  If you modify this library, you may extend
 this exception to your version of the library, but you are not
 obligated to do so.  If you do not wish to do so, delete this
 exception statement from your version. */

package java.util;

/**
 * The <code>Vector</code> classes implements growable arrays of Objects. You
 * can access elements in a Vector with an index, just as you can in a built in
 * array, but Vectors can grow and shrink to accommodate more or fewer objects.
 * <p>
 * 
 * Vectors try to mantain efficiency in growing by having a
 * <code>capacityIncrement</code> that can be specified at instantiation. When
 * a Vector can no longer hold a new Object, it grows by the amount in
 * <code>capacityIncrement</code>. If this value is 0, the vector doubles in
 * size.
 * <p>
 * 
 * Vector implements the JDK 1.2 List interface, and is therefore a fully
 * compliant Collection object. The iterators are fail-fast - if external code
 * structurally modifies the vector, any operation on the iterator will then
 * throw a {@link ConcurrentModificationException}. The Vector class is fully
 * synchronized, but the iterators are not. So, when iterating over a vector, be
 * sure to synchronize on the vector itself. If you don't want the expense of
 * synchronization, use ArrayList instead. On the other hand, the Enumeration of
 * elements() is not thread-safe, nor is it fail-fast; so it can lead to
 * undefined behavior even in a single thread if you modify the vector during
 * iteration.
 * <p>
 * 
 * Note: Some methods, especially those specified by List, specify throwing
 * {@link IndexOutOfBoundsException}, but it is easier to implement by throwing
 * the subclass {@link ArrayIndexOutOfBoundsException}. Others directly specify
 * this subclass.
 * 
 * @author Scott G. Miller
 * @author Bryce McKinlay
 * @author Eric Blake (ebb9@email.byu.edu)
 * @see Collection
 * @see List
 * @see ArrayList
 * @see LinkedList
 * @since 1.0
 * @status updated to 1.4
 */
public class Vector {

	/**
	 * The internal array used to hold members of a Vector. The elements are in
	 * positions 0 through elementCount - 1, and all remaining slots are null.
	 * 
	 * @serial the elements
	 */
	protected Object[] elementData;

	/**
	 * The number of elements currently in the vector, also returned by
	 * {@link #size}.
	 * 
	 * @serial the size
	 */
	protected int elementCount;

	/**
	 * The amount the Vector's internal array should be increased in size when a
	 * new element is added that exceeds the current size of the array, or when
	 * {@link #ensureCapacity} is called. If &lt;= 0, the vector just doubles in
	 * size.
	 * 
	 * @serial the amount to grow the vector by
	 */
	protected int capacityIncrement;

	/**
	 * A count of the number of structural modifications that have been made to
	 * the list (that is, insertions and removals).
	 */
	protected int modCount;

	/**
	 * Constructs an empty vector with an initial size of 10, and a capacity
	 * increment of 0
	 */

	public Vector() {
		this(10, 0);
	}

	/**
	 * Constructs a Vector with the initial capacity and capacity increment
	 * specified.
	 * 
	 * @param initialCapacity
	 *            the initial size of the Vector's internal array
	 * @param capacityIncrement
	 *            the amount the internal array should be increased by when
	 *            necessary, 0 to double the size
	 * @throws IllegalArgumentException
	 *             if initialCapacity &lt; 0
	 */
	public Vector(int initialCapacity, int capacityIncrement) {
		if (initialCapacity < 0)
			throw new IllegalArgumentException();
		elementData = new Object[initialCapacity];
		this.capacityIncrement = capacityIncrement;
		modCount = 0;
	}

	/**
	 * Constructs a Vector with the initial capacity specified, and a capacity
	 * increment of 0 (double in size).
	 * 
	 * @param initialCapacity
	 *            the initial size of the Vector's internal array
	 * @throws IllegalArgumentException
	 *             if initialCapacity &lt; 0
	 */
	public Vector(int initialCapacity) {
		this(initialCapacity, 0);
	}

	/**
	 * Adds an element to the Vector at the end of the Vector. The vector is
	 * increased by ensureCapacity(size() + 1) if needed.
	 * 
	 * @param obj
	 *            the object to add to the Vector
	 */
	public synchronized void addElement(Object obj) {
		if (elementCount == elementData.length)
			ensureCapacity(elementCount + 1);
		modCount++;
		elementData[elementCount++] = obj;
	}

	/**
	 * Returns the size of the internal data array (not the amount of elements
	 * contained in the Vector).
	 * 
	 * @return capacity of the internal data array
	 */
	public synchronized int capacity() {
		return elementData.length;
	}

	/**
	 * Returns true when <code>elem</code> is contained in this Vector.
	 * 
	 * @param elem
	 *            the element to check
	 * @return true if the object is contained in this Vector, false otherwise
	 */
	public boolean contains(Object elem) {
		return indexOf(elem, 0) >= 0;
	}

	/**
	 * Copies the contents of the Vector into the provided array. If the array
	 * is too small to fit all the elements in the Vector, an
	 * {@link IndexOutOfBoundsException} is thrown without modifying the array.
	 * Old elements in the array are overwritten by the new elements.
	 * 
	 * @param a
	 *            target array for the copy
	 * @throws IndexOutOfBoundsException
	 *             the array is not large enough
	 * @throws NullPointerException
	 *             the array is null
	 * @see #toArray(Object[])
	 */

	public synchronized void copyInto(Object[] a) {
		System.arraycopy(elementData, 0, a, 0, elementCount);
	}

	/**
	 * Returns the Object stored at <code>index</code>.
	 * 
	 * @param index
	 *            the index of the Object to retrieve
	 * @return the object at <code>index</code>
	 * @throws ArrayIndexOutOfBoundsException
	 *             index &lt; 0 || index &gt;= size()
	 * @see #get(int)
	 */
	public synchronized Object elementAt(int index) {
		checkBoundExclusive(index);
		return elementData[index];
	}

	/**
	 * Returns an Enumeration of the elements of this Vector. The enumeration
	 * visits the elements in increasing index order, but is NOT thread-safe.
	 * 
	 * @return an Enumeration
	 * @see #iterator()
	 */
	// No need to synchronize as the Enumeration is not thread-safe!
	public Enumeration elements() {
		return new Enumeration() {
			private int i = 0;

			public boolean hasMoreElements() {
				return i < elementCount;
			}

			public Object nextElement() {
				if (i >= elementCount)
					throw new NoSuchElementException();
				return elementData[i++];
			}
		};
	}

	/**
	 * Ensures that <code>minCapacity</code> elements can fit within this
	 * Vector. If <code>elementData</code> is too small, it is expanded as
	 * follows: If the <code>elementCount + capacityIncrement</code> is
	 * adequate, that is the new size. If <code>capacityIncrement</code> is
	 * non-zero, the candidate size is double the current. If that is not
	 * enough, the new size is <code>minCapacity</code>.
	 * 
	 * @param minCapacity
	 *            the desired minimum capacity, negative values ignored
	 */
	public synchronized void ensureCapacity(int minCapacity) {
		if (elementData.length >= minCapacity)
			return;

		int newCapacity;
		if (capacityIncrement <= 0)
			newCapacity = elementData.length * 2;
		else
			newCapacity = elementData.length + capacityIncrement;

		// TODO: any way to omit this new?

		Object[] newArray = new Object[Math.max(newCapacity, minCapacity)];

		System.arraycopy(elementData, 0, newArray, 0, elementCount);
		elementData = newArray;
	}

	/**
	 * Returns the first element (index 0) in the Vector.
	 * 
	 * @return the first Object in the Vector
	 * @throws NoSuchElementException
	 *             the Vector is empty
	 */
	public synchronized Object firstElement() {
		if (elementCount == 0)
			throw new NoSuchElementException();

		return elementData[0];
	}

	/**
	 * Returns the first occurrence of <code>elem</code> in the Vector, or -1
	 * if <code>elem</code> is not found.
	 * 
	 * @param elem
	 *            the object to search for
	 * @return the index of the first occurrence, or -1 if not found
	 */
	public int indexOf(Object elem) {
		return indexOf(elem, 0);
	}

	/**
	 * Searches the vector starting at <code>index</code> for object
	 * <code>elem</code> and returns the index of the first occurrence of this
	 * Object. If the object is not found, or index is larger than the size of
	 * the vector, -1 is returned.
	 * 
	 * @param e
	 *            the Object to search for
	 * @param index
	 *            start searching at this index
	 * @return the index of the next occurrence, or -1 if it is not found
	 * @throws IndexOutOfBoundsException
	 *             if index &lt; 0
	 */
	public synchronized int indexOf(Object e, int index) {
		for (int i = index; i < elementCount; i++)
			if (elementData[i].equals(e))
				return i;
		return -1;
	}

	/**
	 * Inserts a new element into the Vector at <code>index</code>. Any
	 * elements at or greater than index are shifted up one position.
	 * 
	 * @param obj
	 *            the object to insert
	 * @param index
	 *            the index at which the object is inserted
	 * @throws ArrayIndexOutOfBoundsException
	 *             index &lt; 0 || index &gt; size()
	 * @see #add(int, Object)
	 */
	public synchronized void insertElementAt(Object obj, int index) {
		checkBoundInclusive(index);
		if (elementCount == elementData.length)
			ensureCapacity(elementCount + 1);
		modCount++;
		System.arraycopy(elementData, index, elementData, index + 1,
				elementCount - index);
		elementCount++;
		elementData[index] = obj;
	}

	/**
	 * Returns true if this Vector is empty, false otherwise
	 * 
	 * @return true if the Vector is empty, false otherwise
	 */
	public synchronized boolean isEmpty() {
		return elementCount == 0;
	}

	/**
	 * Returns the last element in the Vector.
	 * 
	 * @return the last Object in the Vector
	 * @throws NoSuchElementException
	 *             the Vector is empty
	 */
	public synchronized Object lastElement() {
		if (elementCount == 0)
			throw new NoSuchElementException();

		return elementData[elementCount - 1];
	}

	/**
	 * Returns the last index of <code>elem</code> within this Vector, or -1
	 * if the object is not within the Vector.
	 * 
	 * @param elem
	 *            the object to search for
	 * @return the last index of the object, or -1 if not found
	 */
	public int lastIndexOf(Object elem) {
		return lastIndexOf(elem, elementCount - 1);
	}

	/**
	 * Returns the index of the first occurrence of <code>elem</code>, when
	 * searching backwards from <code>index</code>. If the object does not
	 * occur in this Vector, or index is less than 0, -1 is returned.
	 * 
	 * @param e
	 *            the object to search for
	 * @param index
	 *            the index to start searching in reverse from
	 * @return the index of the Object if found, -1 otherwise
	 * @throws IndexOutOfBoundsException
	 *             if index &gt;= size()
	 */
	public synchronized int lastIndexOf(Object e, int index) {
		checkBoundExclusive(index);
		for (int i = index; i >= 0; i--)
			if (elementData[i].equals(e))
				return i;
		return -1;
	}

	/**
	 * Removes all elements from the Vector. Note that this does not resize the
	 * internal data array.
	 * 
	 * @see #clear()
	 */
	public synchronized void removeAllElements() {
		if (elementCount == 0)
			return;

		modCount++;
		for (int i = 0; i < elementCount; i++) {
			elementData[i] = null;
		}
		elementCount = 0;
	}

	/**
	 * Removes the first (the lowestindex) occurance of the given object from
	 * the Vector. If such a remove was performed (the object was found), true
	 * is returned. If there was no such object, false is returned.
	 * 
	 * @param obj
	 *            the object to remove from the Vector
	 * @return true if the Object was in the Vector, false otherwise
	 * @see #remove(Object)
	 */
	public synchronized boolean removeElement(Object obj) {
		int idx = indexOf(obj, 0);
		if (idx >= 0) {
			remove(idx);
			return true;
		}
		return false;
	}

	/**
	 * Removes the element at <code>index</code>, and shifts all elements at
	 * positions greater than index to their index - 1.
	 * 
	 * @param index
	 *            the index of the element to remove
	 * @throws ArrayIndexOutOfBoundsException
	 *             index &lt; 0 || index &gt;= size();
	 * @see #remove(int)
	 */
	public void removeElementAt(int index) {
		remove(index);
	}

	/**
	 * Changes the element at <code>index</code> to be <code>obj</code>
	 * 
	 * @param obj
	 *            the object to store
	 * @param index
	 *            the position in the Vector to store the object
	 * @throws ArrayIndexOutOfBoundsException
	 *             the index is out of range
	 * @see #set(int, Object)
	 */
	public void setElementAt(Object obj, int index) {
		set(index, obj);
	}

	/**
	 * Returns the number of elements stored in this Vector.
	 * 
	 * @return the number of elements in this Vector
	 */
	public synchronized int size() {
		return elementCount;
	}

	/**
	 * Explicitly sets the size of the vector (but not necessarily the size of
	 * the internal data array). If the new size is smaller than the old one,
	 * old values that don't fit are lost. If the new size is larger than the
	 * old one, the vector is padded with null entries.
	 * 
	 * @param newSize
	 *            The new size of the internal array
	 * @throws ArrayIndexOutOfBoundsException
	 *             if the new size is negative
	 */
	public synchronized void setSize(int newSize) {
		// Don't bother checking for the case where size() == the capacity of
		// the
		// vector since that is a much less likely case; it's more efficient to
		// not do the check and lose a bit of performance in that infrequent
		// case
		modCount++;
		ensureCapacity(newSize);
		if (newSize < elementCount)
			for (int i = elementCount; i < newSize; i++) {
				elementData[i] = null;
			}
		// Arrays.fill(elementData, newSize, elementCount, null);
		elementCount = newSize;
	}

	/**
	 * Trims the Vector down to size. If the internal data array is larger than
	 * the number of Objects its holding, a new array is constructed that
	 * precisely holds the elements. Otherwise this does nothing.
	 */
	public synchronized void trimToSize() {
		// Don't bother checking for the case where size() == the capacity of
		// the
		// vector since that is a much less likely case; it's more efficient to
		// not do the check and lose a bit of performance in that infrequent
		// case

		Object[] newArray = new Object[elementCount];
		System.arraycopy(elementData, 0, newArray, 0, elementCount);
		elementData = newArray;
	}

	/**
	 * Removes the element at the specified index, and returns it.
	 * 
	 * @param index
	 *            the position from which to remove the element
	 * @return the object removed
	 * @throws ArrayIndexOutOfBoundsException
	 *             index &lt; 0 || index &gt;= size()
	 * @since 1.2
	 */
	public synchronized Object remove(int index) {
		checkBoundExclusive(index);
		Object temp = elementData[index];
		modCount++;
		elementCount--;
		if (index < elementCount)
			System.arraycopy(elementData, index + 1, elementData, index,
					elementCount - index);
		elementData[elementCount] = null;
		return temp;
	}

	/**
	 * Puts <code>element</code> into the Vector at position
	 * <code>index</code> and returns the Object that previously occupied that
	 * position.
	 * 
	 * @param index
	 *            the index within the Vector to place the Object
	 * @param element
	 *            the Object to store in the Vector
	 * @return the previous object at the specified index
	 * @throws ArrayIndexOutOfBoundsException
	 *             index &lt; 0 || index &gt;= size()
	 * @since 1.2
	 */
	private synchronized Object set(int index, Object element) {
		checkBoundExclusive(index);
		Object temp = elementData[index];
		elementData[index] = element;
		return temp;
	}

	/**
	 * Compares this to the given object.
	 * 
	 * @param o
	 *            the object to compare to
	 * @return true if the two are equal
	 * @since 1.2
	 */
	/*
	 * public synchronized boolean equals(Object o) { // Here just for the
	 * sychronization. return super.equals(o); }
	 */
	/**
	 * Returns a string representation of this Vector in the form "[element0,
	 * element1, ... elementN]".
	 * 
	 * @return the String representation of this Vector
	 */
	public synchronized String toString() {
		if (this.size() <= 0)
			return "";
		StringBuffer sb = new StringBuffer();
		sb.append("{\n");
		for (int i = 0; i < size(); i++) {
			sb.append(elementData[i]);
			sb.append(";\n");
		}
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Checks that the index is in the range of possible elements (inclusive).
	 * 
	 * @param index
	 *            the index to check
	 * @throws ArrayIndexOutOfBoundsException
	 *             if index &gt; size
	 */
	private void checkBoundInclusive(int index) {
		// Implementation note: we do not check for negative ranges here, since
		// use of a negative index will cause an ArrayIndexOutOfBoundsException
		// with no effort on our part.
		if (index > elementCount)
			throw new ArrayIndexOutOfBoundsException(index + " > "
					+ elementCount);
	}

	/**
	 * Checks that the index is in the range of existing elements (exclusive).
	 * 
	 * @param index
	 *            the index to check
	 * @throws ArrayIndexOutOfBoundsException
	 *             if index &gt;= size
	 */
	private void checkBoundExclusive(int index) {
		// Implementation note: we do not check for negative ranges here, since
		// use of a negative index will cause an ArrayIndexOutOfBoundsException
		// with no effort on our part.
		if (index >= elementCount)
			throw new ArrayIndexOutOfBoundsException(index + " >= "
					+ elementCount);
	}

}
