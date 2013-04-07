package java.util;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.NoPoolElementException;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

/*
 * %W% %E%
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/**
 * The {@code Vector} class implements a growable array of
 * objects. Like an array, it contains components that can be
 * accessed using an integer index. However, the size of a
 * {@code Vector} can grow or shrink as needed to accommodate
 * adding and removing items after the {@code Vector} has been created.
 *
 * <p>Each vector tries to optimize storage management by maintaining a
 * {@code capacity} and a {@code capacityIncrement}. The
 * {@code capacity} is always at least as large as the vector
 * size; it is usually larger because as components are added to the
 * vector, the vector's storage increases in chunks the size of
 * {@code capacityIncrement}. An application can increase the
 * capacity of a vector before inserting a large number of
 * components; this reduces the amount of incremental reallocation.
 *
 * <p>The Iterators returned by Vector's iterator and listIterator
 * methods are <em>fail-fast</em>: if the Vector is structurally modified
 * at any time after the Iterator is created, in any way except through the
 * Iterator's own remove or add methods, the Iterator will throw a
 * ConcurrentModificationException.  Thus, in the face of concurrent
 * modification, the Iterator fails quickly and cleanly, rather than risking
 * arbitrary, non-deterministic behavior at an undetermined time in the future.
 * The Enumerations returned by Vector's elements method are <em>not</em>
 * fail-fast.
 *
 * <p>Note that the fail-fast behavior of an iterator cannot be guaranteed
 * as it is, generally speaking, impossible to make any hard guarantees in the
 * presence of unsynchronized concurrent modification.  Fail-fast iterators
 * throw {@code ConcurrentModificationException} on a best-effort basis.
 * Therefore, it would be wrong to write a program that depended on this
 * exception for its correctness:  <i>the fail-fast behavior of iterators
 * should be used only to detect bugs.</i>
 *
 * <p>As of the Java 2 platform v1.2, this class was retrofitted to
 * implement the {@link List} interface, making it a member of the
 * <a href="{@docRoot}/../technotes/guides/collections/index.html"> Java
 * Collections Framework</a>.  Unlike the new collection
 * implementations, {@code Vector} is synchronized.
 *
 * @author  Lee Boynton
 * @author  Jonathan Payne
 * @version %I%, %G%
 * @see Collection
 * @see List
 * @see ArrayList
 * @see LinkedList
 * @since   JDK1.0
 */
public class Vector<E extends AbstractPoolObject>
    extends AbstractList<E>
    implements List<E>, RandomAccess //, Cloneable, java.io.Serializable
{
    /**
     * The array buffer into which the components of the vector are
     * stored. The capacity of the vector is the length of this array buffer,
     * and is at least large enough to contain all the vector's elements.
     *
     * <p>Any array elements following the last element in the Vector are null.
     *
     * @serial
     */
    protected AbstractPoolObject[] elementData;
    

    /**
     * The number of valid components in this {@code Vector} object.
     * Components {@code elementData[0]} through
     * {@code elementData[elementCount-1]} are the actual items.
     *
     * @serial
     */
    protected int elementCount;

    /**
     * Default capacity when the constructor is called with an empty argument
     */
    protected static final int DEFAULT_CAPACITY = 10;

	/**
	 * Constructs an empty vector with the specified initial capacity.
	 * 
	 * @param initialCapacity
	 *            the initial capacity of the vector
	 * @throws IllegalArgumentException
	 *             if the specified initial capacity is negative
	 */
	public Vector(int initialCapacity){
		super();
		if (initialCapacity < 0)
			throw new IllegalArgumentException("Illegal Capacity: "
					+ initialCapacity);
		this.elementData = new AbstractPoolObject[initialCapacity];
	}

    /**
     * Constructs an empty vector so that its internal data array
     * has size {@code 10}.
     */
	public Vector() {
		this(DEFAULT_CAPACITY);
	}


    /**
     * Copies the components of this vector into the specified array.
     * The item at index {@code k} in this vector is copied into
     * component {@code k} of {@code anArray}.
     *
     * @param  anArray the array into which the components get copied
     * @throws NullPointerException if the given array is null
     * @throws IndexOutOfBoundsException if the specified array is not
     *         large enough to hold all the components of this vector
     * @throws ArrayStoreException if a component of this vector is not of
     *         a runtime type that can be stored in the specified array
     * @see #toArray(Object[])
     */
	//TODO
	public synchronized void copyInto(Object[] anArray) {
		System.arraycopy(elementData, 0, anArray, 0, elementCount);
	}

    /**
     * Returns the current capacity of this vector.
     *
     * @return  the current capacity (the length of its internal
     *          data array, kept in the field {@code elementData}
     *          of this vector)
     */
	public synchronized int capacity() {
		return elementData.length;
	}

    /**
     * Returns the number of components in this vector.
     *
     * @return  the number of components in this vector
     */
	public synchronized int size() {
		return elementCount;
	}

    /**
     * Tests if this vector has no components.
     *
     * @return  {@code true} if and only if this vector has
     *          no components, that is, its size is zero;
     *          {@code false} otherwise.
     */
	public synchronized boolean isEmpty() {
		return elementCount == 0;
	}

    /**
     * Returns an enumeration of the components of this vector. The
     * returned {@code Enumeration} object will generate all items in
     * this vector. The first item generated is the item at index {@code 0},
     * then the item at index {@code 1}, and so on.
     *
     * @return  an enumeration of the components of this vector
     * @see     Iterator
     */
	//TODO
	public Enumeration<E> elements() {

		return new Enumeration<E>() {
			int count = 0;

			public boolean hasMoreElements() {
				return count < elementCount;
			}
			
			/*
			 * (non-Javadoc)
			 * @see scjlibs.util.Enumeration#nextElement()
			 */
			public E nextElement() {
				synchronized (Vector.this) {
					if (count < elementCount) {
						return (E) elementData[count++];
					}
				}
				throw new NoSuchElementException("Vector Enumeration");
			}
		};
	}

    /**
     * Returns {@code true} if this vector contains the specified element.
     * More formally, returns {@code true} if and only if this vector
     * contains at least one element {@code e} such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
     *
     * @param o element whose presence in this vector is to be tested
     * @return {@code true} if this vector contains the specified element
     */
	public boolean contains(Object o) {
		return indexOf(o, 0) >= 0;
	}

    /**
     * Returns the index of the first occurrence of the specified element
     * in this vector, or -1 if this vector does not contain the element.
     * More formally, returns the lowest index {@code i} such that
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>,
     * or -1 if there is no such index.
     *
     * @param o element to search for
     * @return the index of the first occurrence of the specified element in
     *         this vector, or -1 if this vector does not contain the element
     */
	public int indexOf(Object o) {
		return indexOf(o, 0);
	}

    /**
     * Returns the index of the first occurrence of the specified element in
     * this vector, searching forwards from {@code index}, or returns -1 if
     * the element is not found.
     * More formally, returns the lowest index {@code i} such that
     * <tt>(i&nbsp;&gt;=&nbsp;index&nbsp;&amp;&amp;&nbsp;(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i))))</tt>,
     * or -1 if there is no such index.
     *
     * @param o element to search for
     * @param index index to start searching from
     * @return the index of the first occurrence of the element in
     *         this vector at position {@code index} or later in the vector;
     *         {@code -1} if the element is not found.
     * @throws IndexOutOfBoundsException if the specified index is negative
     * @see     Object#equals(Object)
     */
	public synchronized int indexOf(Object o, int index) {
		if (o == null) {
			for (int i = index; i < elementCount; i++)
				if (elementData[i] == null)
					return i;
		} else {
			for (int i = index; i < elementCount; i++)
				if (o.equals(elementData[i]))
					return i;
		}
		return -1;
	}

    /**
     * Returns the index of the last occurrence of the specified element
     * in this vector, or -1 if this vector does not contain the element.
     * More formally, returns the highest index {@code i} such that
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>,
     * or -1 if there is no such index.
     *
     * @param o element to search for
     * @return the index of the last occurrence of the specified element in
     *         this vector, or -1 if this vector does not contain the element
     */
	public synchronized int lastIndexOf(Object o) {
		return lastIndexOf(o, elementCount - 1);
	}

    /**
     * Returns the index of the last occurrence of the specified element in
     * this vector, searching backwards from {@code index}, or returns -1 if
     * the element is not found.
     * More formally, returns the highest index {@code i} such that
     * <tt>(i&nbsp;&lt;=&nbsp;index&nbsp;&amp;&amp;&nbsp;(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i))))</tt>,
     * or -1 if there is no such index.
     *
     * @param o element to search for
     * @param index index to start searching backwards from
     * @return the index of the last occurrence of the element at position
     *         less than or equal to {@code index} in this vector;
     *         -1 if the element is not found.
     * @throws IndexOutOfBoundsException if the specified index is greater
     *         than or equal to the current size of this vector
     */
	public synchronized int lastIndexOf(Object o, int index) {
		if (index >= elementCount)
			throw new IndexOutOfBoundsException(index + " >= " + elementCount);

		if (o == null) {
			for (int i = index; i >= 0; i--)
				if (elementData[i] == null)
					return i;
		} else {
			for (int i = index; i >= 0; i--)
				if (o.equals(elementData[i]))
					return i;
		}
		return -1;
	}

    /**
     * Returns the component at the specified index.
     *
     * <p>This method is identical in functionality to the {@link #get(int)}
     * method (which is part of the {@link List} interface).
     *
     * @param      index   an index into this vector
     * @return     the component at the specified index
     * @throws ArrayIndexOutOfBoundsException if the index is out of range
     *	       ({@code index < 0 || index >= size()})
     */
	public synchronized E elementAt(int index) {
		if (index >= elementCount) {
			throw new ArrayIndexOutOfBoundsException(index + " >= "
					+ elementCount);
		}

		return (E) elementData[index];
	}

    /**
     * Returns the first component (the item at index {@code 0}) of
     * this vector.
     *
     * @return     the first component of this vector
     * @throws NoSuchElementException if this vector has no components
     */
	public synchronized E firstElement() {
		if (elementCount == 0) {
			throw new NoSuchElementException();
		}
		return (E) elementData[0];
	}

    /**
     * Returns the last component of the vector.
     *
     * @return  the last component of the vector, i.e., the component at index
     *          <code>size()&nbsp;-&nbsp;1</code>.
     * @throws NoSuchElementException if this vector is empty
     */
	public synchronized E lastElement() {
		if (elementCount == 0) {
			throw new NoSuchElementException();
		}
		return (E) elementData[elementCount - 1];
	}

    /**
     * Sets the component at the specified {@code index} of this
     * vector to be the specified object. The previous component at that
     * position is discarded.
     *
     * <p>The index must be a value greater than or equal to {@code 0}
     * and less than the current size of the vector.
     *
     * <p>This method is identical in functionality to the
     * {@link #set(int, Object) set(int, E)}
     * method (which is part of the {@link List} interface). Note that the
     * {@code set} method reverses the order of the parameters, to more closely
     * match array usage.  Note also that the {@code set} method returns the
     * old value that was stored at the specified position.
     *
     * @param      obj     what the component is to be set to
     * @param      index   the specified index
     * @throws ArrayIndexOutOfBoundsException if the index is out of range
     *	       ({@code index < 0 || index >= size()})
     */
	public synchronized void setElementAt(E obj, int index) {

		if (index >= elementCount)
			throw new ArrayIndexOutOfBoundsException(index + " >= "
					+ elementCount);

		elementData[index].terminate();
		elementData[index] = obj;
		obj.initialize();

	}

    /**
     * Deletes the component at the specified index. Each component in
     * this vector with an index greater or equal to the specified
     * {@code index} is shifted downward to have an index one
     * smaller than the value it had previously. The size of this vector
     * is decreased by {@code 1}.
     *
     * <p>The index must be a value greater than or equal to {@code 0}
     * and less than the current size of the vector. 
     *
     * <p>This method is identical in functionality to the {@link #remove(int)}
     * method (which is part of the {@link List} interface).  Note that the
     * {@code remove} method returns the old value that was stored at the
     * specified position.
     *
     * @param      index   the index of the object to remove
     * @throws ArrayIndexOutOfBoundsException if the index is out of range
     *	       ({@code index < 0 || index >= size()})
     */
	public synchronized void removeElementAt(int index) {
		modCount++;
		if (index >= elementCount) {
			throw new ArrayIndexOutOfBoundsException(index + " >= "
					+ elementCount);
		} else if (index < 0) {
			throw new ArrayIndexOutOfBoundsException(index);
		}
		
		returnToPool(elementData[index]);
		
		int j = elementCount - index - 1;
		if (j > 0) {
			System.arraycopy(elementData, index + 1, elementData, index, j);
		}
		
		elementCount--;
		elementData[elementCount] = null;
	}

    /**
     * Inserts the specified object as a component in this vector at the
     * specified {@code index}. Each component in this vector with
     * an index greater or equal to the specified {@code index} is
     * shifted upward to have an index one greater than the value it had
     * previously.
     *
     * <p>The index must be a value greater than or equal to {@code 0}
     * and less than or equal to the current size of the vector. (If the
     * index is equal to the current size of the vector, the new element
     * is appended to the Vector.)
     *
     * <p>This method is identical in functionality to the
     * {@link #add(int, Object) add(int, E)}
     * method (which is part of the {@link List} interface).  Note that the
     * {@code add} method reverses the order of the parameters, to more closely
     * match array usage.
     *
     * @param      obj     the component to insert
     * @param      index   where to insert the new component
     * @throws ArrayIndexOutOfBoundsException if the index is out of range
     *	       ({@code index < 0 || index > size()})
     */
	public synchronized void insertElementAt(E obj, int index) {

		if (index > elementCount) {
			throw new ArrayIndexOutOfBoundsException(index + " > "
					+ elementCount);
		}
		
		if(elementCount < elementData.length){
			modCount++;
			System.arraycopy(elementData, index, elementData, index + 1,
					elementCount - index);
			elementData[index] = obj;
			elementCount++;
		}
	}

    /**
     * Adds the specified component to the end of this vector,
     * increasing its size by one. The capacity of this vector is
     * increased if its size becomes greater than its capacity.
     *
     * <p>This method is identical in functionality to the
     * {@link #add(Object) add(E)}
     * method (which is part of the {@link List} interface).
     *
     * @param   obj   the component to be added
     */
	public synchronized void addElement(E obj) {
		
		add(obj);
		
	}

    /**
     * Removes the first (lowest-indexed) occurrence of the argument
     * from this vector. If the object is found in this vector, each
     * component in the vector with an index greater or equal to the
     * object's index is shifted downward to have an index one smaller
     * than the value it had previously.
     *
     * <p>This method is identical in functionality to the
     * {@link #remove(Object)} method (which is part of the
     * {@link List} interface).
     *
     * @param   obj   the component to be removed
     * @return  {@code true} if the argument was a component of this
     *          vector; {@code false} otherwise.
     */
	public synchronized boolean removeElement(Object obj) {
		modCount++;
		int i = indexOf(obj);
		if (i >= 0) {
			removeElementAt(i);
			return true;
		}
		return false;
	}

    /**
     * Removes all components from this vector and sets its size to zero.
     *
     * <p>This method is identical in functionality to the {@link #clear}
     * method (which is part of the {@link List} interface).
     */
	public synchronized void removeAllElements() {
		modCount++;
		
		for (int i = 0; i < elementCount; i++){//@WCA loop=DEFAULT_CAPACITY
			/* Restore entries into the pool */
			returnToPool(elementData[i]);
		}

		elementCount = 0;
    }

    /**
     * Returns an array containing all of the elements in this Vector
     * in the correct order.
     *
     * @since 1.2
     */
    public synchronized Object[] toArray() {
        return Arrays.copyOf(elementData, elementCount);
    }

    /**
     * Returns an array containing all of the elements in this Vector in the
     * correct order; the runtime type of the returned array is that of the
     * specified array.  If the Vector fits in the specified array, it is
     * returned therein.  Otherwise, a new array is allocated with the runtime
     * type of the specified array and the size of this Vector.
     *
     * <p>If the Vector fits in the specified array with room to spare
     * (i.e., the array has more elements than the Vector),
     * the element in the array immediately following the end of the
     * Vector is set to null.  (This is useful in determining the length
     * of the Vector <em>only</em> if the caller knows that the Vector
     * does not contain any null elements.)
     *
     * @param a the array into which the elements of the Vector are to
     *		be stored, if it is big enough; otherwise, a new array of the
     * 		same runtime type is allocated for this purpose.
     * @return an array containing the elements of the Vector
     * @throws ArrayStoreException if the runtime type of a is not a supertype
     * of the runtime type of every element in this Vector
     * @throws NullPointerException if the given array is null
     * @since 1.2
     */
	public synchronized <T> T[] toArray(T[] a) {
		if (a.length < elementCount)
			return (T[]) Arrays.copyOf(elementData, elementCount, a.getClass());

		System.arraycopy(elementData, 0, a, 0, elementCount);

		if (a.length > elementCount)
			a[elementCount] = null;

		return a;
	}

    // Positional Access Operations

    /**
     * Returns the element at the specified position in this Vector.
     *
     * @param index index of the element to return
     * @return object at the specified index
     * @throws ArrayIndexOutOfBoundsException if the index is out of range
     *            ({@code index < 0 || index >= size()})
     * @since 1.2
     */
	public synchronized E get(int index) {
		if (index >= elementCount)
			throw new ArrayIndexOutOfBoundsException(index);

		return (E) elementData[index];
	}

    /**
     * Replaces the element at the specified position in this Vector with the
     * specified element.
     *
     * @param index index of the element to replace
     * @param element element to be stored at the specified position
     * @return the element previously at the specified position
     * @throws ArrayIndexOutOfBoundsException if the index is out of range
     *	       ({@code index < 0 || index >= size()})
     * @since 1.2
     */
	public synchronized E set(int index, E element) {
		
		if (index >= elementCount)
			throw new ArrayIndexOutOfBoundsException(index);

		E oldValue = (E) elementData[index];
		elementData[index] = element;
		oldValue.getPool().releasePoolObject(oldValue);
		
		return oldValue;
	}

    /**
     * Appends the specified element to the end of this Vector.
     *
     * @param e element to be appended to this Vector
     * @return {@code true} (as specified by {@link Collection#add})
     * @since 1.2
     */
	public synchronized boolean add(E e) {

		if(elementCount < elementData.length){
			modCount++;
			elementData[elementCount] = e;
			elementCount++;
			return true;
		}else{
			throw new NoPoolElementException(elementData.length, elementCount + 1);
		}

	}

    /**
     * Removes the first occurrence of the specified element in this Vector
     * If the Vector does not contain the element, it is unchanged.  More
     * formally, removes the element with the lowest index i such that
     * {@code (o==null ? get(i)==null : o.equals(get(i)))} (if such
     * an element exists).
     *
     * @param o element to be removed from this Vector, if present
     * @return true if the Vector contained the specified element
     * @since 1.2
     */
    public boolean remove(Object o) {
        return removeElement(o);
    }

    /**
     * Inserts the specified element at the specified position in this Vector.
     * Shifts the element currently at that position (if any) and any
     * subsequent elements to the right (adds one to their indices).
     *
     * @param index index at which the specified element is to be inserted
     * @param element element to be inserted
     * @throws ArrayIndexOutOfBoundsException if the index is out of range
     *         ({@code index < 0 || index > size()})
     * @since 1.2
     */
    public void add(int index, E element) {
        insertElementAt(element, index);
    }

    /**
     * Removes the element at the specified position in this Vector.
     * Shifts any subsequent elements to the left (subtracts one from their
     * indices).  Returns the element that was removed from the Vector.
     *
     * @throws ArrayIndexOutOfBoundsException if the index is out of range
     *         ({@code index < 0 || index >= size()})
     * @param index the index of the element to be removed
     * @return element that was removed
     * @since 1.2
     */
	public synchronized E remove(int index) {
		
		modCount++;
		if (index >= elementCount)
			throw new ArrayIndexOutOfBoundsException(index);
		
		Object oldValue = elementData[index];
		
		/* Return entry to pool */
		returnToPool(oldValue);

		int numMoved = elementCount - index - 1;
		if (numMoved > 0)
			System.arraycopy(elementData, index + 1, elementData, index,
					numMoved);
		elementCount--;
		elementData[elementCount] = null;

		return (E) oldValue;
	}

    /**
     * Removes all of the elements from this Vector.  The Vector will
     * be empty after this call returns (unless it throws an exception).
     *
     * @since 1.2
     */
    public void clear() {
        removeAllElements();
    }

    /**
     * Compares the specified Object with this Vector for equality.  Returns
     * true if and only if the specified Object is also a List, both Lists
     * have the same size, and all corresponding pairs of elements in the two
     * Lists are <em>equal</em>.  (Two elements {@code e1} and
     * {@code e2} are <em>equal</em> if {@code (e1==null ? e2==null :
     * e1.equals(e2))}.)  In other words, two Lists are defined to be
     * equal if they contain the same elements in the same order.
     *
     * @param o the Object to be compared for equality with this Vector
     * @return true if the specified Object is equal to this Vector
     */
    public synchronized boolean equals(Object o) {
        return super.equals(o);
    }

    /**
     * Returns the hash code value for this Vector.
     */
    public synchronized int hashCode() {
        return super.hashCode();
    }

    /**
     * Returns a string representation of this Vector, containing
     * the String representation of each element.
     */
    // TODO: allocates an iterator
    public synchronized String toString() {
        return super.toString();
    }

//    /**
//     * Returns a view of the portion of this List between fromIndex,
//     * inclusive, and toIndex, exclusive.  (If fromIndex and toIndex are
//     * equal, the returned List is empty.)  The returned List is backed by this
//     * List, so changes in the returned List are reflected in this List, and
//     * vice-versa.  The returned List supports all of the optional List
//     * operations supported by this List.
//     *
//     * <p>This method eliminates the need for explicit range operations (of
//     * the sort that commonly exist for arrays).   Any operation that expects
//     * a List can be used as a range operation by operating on a subList view
//     * instead of a whole List.  For example, the following idiom
//     * removes a range of elements from a List:
//     * <pre>
//     *	    list.subList(from, to).clear();
//     * </pre>
//     * Similar idioms may be constructed for indexOf and lastIndexOf,
//     * and all of the algorithms in the Collections class can be applied to
//     * a subList.
//     *
//     * <p>The semantics of the List returned by this method become undefined if
//     * the backing list (i.e., this List) is <i>structurally modified</i> in
//     * any way other than via the returned List.  (Structural modifications are
//     * those that change the size of the List, or otherwise perturb it in such
//     * a fashion that iterations in progress may yield incorrect results.)
//     *
//     * @param fromIndex low endpoint (inclusive) of the subList
//     * @param toIndex high endpoint (exclusive) of the subList
//     * @return a view of the specified range within this List
//     * @throws IndexOutOfBoundsException if an endpoint index value is out of range
//     *         {@code (fromIndex < 0 || toIndex > size)}
//     * @throws IllegalArgumentException if the endpoint indices are out of order
//     *	       {@code (fromIndex > toIndex)}
//     */
//    public synchronized List<E> subList(int fromIndex, int toIndex) {
//        return Collections.synchronizedList(super.subList(fromIndex, toIndex),
//                                            this);
//    }

    /**
     * Removes from this List all of the elements whose index is between
     * fromIndex, inclusive and toIndex, exclusive.  Shifts any succeeding
     * elements to the left (reduces their index).
     * This call shortens the ArrayList by (toIndex - fromIndex) elements.  (If
     * toIndex==fromIndex, this operation has no effect.)
     *
     * @param fromIndex index of first element to be removed
     * @param toIndex index after last element to be removed
     */
	protected synchronized void removeRange(int fromIndex, int toIndex) {
		modCount++;
		int numMoved = elementCount - toIndex;
		System.arraycopy(elementData, toIndex, elementData, fromIndex, numMoved);

		int newElementCount = elementCount - (toIndex - fromIndex);
		while (elementCount != newElementCount) {
			returnToPool(elementData[elementCount]);
			--elementCount;
		}
	}

	private void returnToPool(Object e){
		E temp = (E) e;
		temp.getPool().releasePoolObject(temp);
	}
	
}
