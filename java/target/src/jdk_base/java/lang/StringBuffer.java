/* StringBuffer.java -- Growable strings
 Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003 Free Software Foundation, Inc.

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
 Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 02111-1307 USA.

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

package java.lang;

// import java.io.Serializable;

// public final class StringBuffer implements Serializable, CharSequence
public final class StringBuffer implements CharSequence {

	int count;

	char[] value;

	private final static int DEFAULT_CAPACITY = 16;

	public StringBuffer() {

		this(DEFAULT_CAPACITY);
	}

	public StringBuffer(int capacity) {

		value = new char[capacity];
	}

	public StringBuffer(String str) {

		// Unfortunately, because the size is 16 larger, we cannot share.
		// count = str.count;
		count = str.length();
		value = new char[count];
		//TODO: System.arraycopy()
		for (int i = 0; i < count; ++i)
			value[i] = str.value[i];
		// str.getChars(0, count, value, 0);
	}

	// since 1.5
	public StringBuffer(CharSequence seq) {
		count = seq.length();
		value = new char[count];
		for (int i = 0; i < count; ++i)
			value[i] = seq.charAt(i);		
	}

	public String toString() {
		return new String(this);
	}

	public StringBuffer append(boolean b) {
		if (b) {
			return append("true");
		} else {
			return append("false");
		}
	}

	public synchronized StringBuffer append(char ch) {
		ensureCapacity_unsynchronized(count + 1);
		value[count++] = ch;
		return this;
	}

	public StringBuffer append(char[] data) {
		return append(data, 0, data.length);
	}

	public synchronized StringBuffer append(char[] data, int offset, int count) {
		ensureCapacity_unsynchronized(this.count + count);
		arraycopy(data, offset, value, this.count, count);
		this.count += count;
		return this;
	}

	//	don't create a temporary object
	//	public StringBuffer append(int inum) {
	//		return append(String.valueOf(inum));
	//	}

	private static final int MAX_TMP = 32;
	private static char[] tmp = new char[MAX_TMP]; // a generic buffer

	public synchronized StringBuffer append(int val) {

		int i;
		int sign = 1;
		// When the value is MIN_VALUE, it overflows when made positive
		if (val < 0) {
			this.append('-');
			sign = -1;
		}
		for (i = 0; i < MAX_TMP - 1; ++i) {
			tmp[i] = (char) (((val % 10) * sign) + '0');
			val /= 10;
			if (val == 0)
				break;
		}
		for (val = i; val >= 0; --val) {
			this.append(tmp[val]);
		}

		return this;
	}

	public StringBuffer append(long lnum) {
		return append(Long.toString(lnum, 10));
	}

	public StringBuffer append(double d) {
		return append(Double.toString(d));
	}

	public StringBuffer append(Object obj) {
		if (obj == null)
			return append("null");
		return append(obj.toString());
	}

	public synchronized StringBuffer append(String str) {
		if (str == null)
			str = "null";
		int len = str.length();
		ensureCapacity_unsynchronized(count + len);
		str.getChars(0, len, value, count);
		count += len;
		return this;
	}

	/*
	 only in 1.4 (not 1.3)!
	 */
	public synchronized StringBuffer append(StringBuffer stringBuffer) {
		if (stringBuffer == null)
			return append("null");
		//    synchronized (stringBuffer)
		//      {
		int len = stringBuffer.count;
		ensureCapacity_unsynchronized(count + len);
		arraycopy(stringBuffer.value, 0, value, count, len);
		count += len;
		//      }
		return this;
	}

	public synchronized int capacity() {
		return value.length;
	}

	public synchronized char charAt(int index) {
		if (index < 0 || index >= count)
			throw new StringIndexOutOfBoundsException(index);

		return value[index];
	}

	public synchronized StringBuffer delete(int start, int end) {
		if (start < 0 || start > count || start > end)
			throw new StringIndexOutOfBoundsException(start);

		if (end > count)
			end = count;
		// This will unshare if required.
		ensureCapacity_unsynchronized(count);
		if (count - end != 0)
			arraycopy(value, end, value, start, count - end);
		count -= end - start;
		return this;
	}

	public StringBuffer deleteCharAt(int index) {
		return delete(index, index + 1);
	}

	public synchronized StringBuffer replace(int start, int end, String str) {
		if (start < 0 || start > count || start > end)
			//      throw new StringIndexOutOfBoundsException(start);
			return this;

		// int len = str.count;
		int len = str.length();
		// Calculate the difference in 'count' after the replace.
		int delta = len - (end > count ? count : end) + start;
		ensureCapacity_unsynchronized(count + delta);

		if (delta != 0 && end < count)
			arraycopy(value, end, value, end + delta, count - end);

		str.getChars(0, len, value, start);
		count += delta;
		return this;
	}

	private void ensureCapacity_unsynchronized(int minimumCapacity) {
		// if (shared || minimumCapacity > value.length)
		if (minimumCapacity > value.length) {
			// We don't want to make a larger vector when `shared' is
			// set. If we do, then setLength becomes very inefficient
			// when repeatedly reusing a StringBuffer in a loop.
			int max = (minimumCapacity > value.length ? value.length * 2 + 2
					: value.length);
			minimumCapacity = (minimumCapacity < max ? max : minimumCapacity);
			char[] nb = new char[minimumCapacity];
			// System.arraycopy(value, 0, nb, 0, count);
			for (int i = 0; i < count; ++i)
				nb[i] = value[i];
			value = nb;
			// shared = false;
		}
	}

	public synchronized void getChars(int srcOffset, int srcEnd, char[] dst,
			int dstOffset) {
		int todo = srcEnd - srcOffset;
		if (srcOffset < 0 || srcEnd > count || todo < 0)
			// throw new StringIndexOutOfBoundsException();
			return;
		arraycopy(value, srcOffset, dst, dstOffset, todo);
	}

	public StringBuffer insert(int offset, boolean bool) {
		String tmp = "false";
		if (bool)
			tmp = "true";
		return insert(offset, tmp);
	}

	public synchronized StringBuffer insert(int offset, char ch) {
		if (offset < 0 || offset > count)
			// throw new StringIndexOutOfBoundsException(offset);
			return this;
		ensureCapacity_unsynchronized(count + 1);
		arraycopy(value, offset, value, offset + 1, count - offset);
		value[offset] = ch;
		count++;
		return this;
	}

	public StringBuffer insert(int offset, char[] data) {
		return insert(offset, data, 0, data.length);
	}

	private synchronized StringBuffer insert(int offset, char[] str,
			int str_offset, int len) {
		if (offset < 0 || offset > count || len < 0 || str_offset < 0
				|| str_offset + len > str.length)
			// throw new StringIndexOutOfBoundsException();
			return this;
		ensureCapacity_unsynchronized(count + len);
		arraycopy(value, offset, value, offset + len, count - offset);
		arraycopy(str, str_offset, value, offset, len);
		count += len;
		return this;
	}

	public StringBuffer insert(int offset, int inum) {
		return insert(offset, String.valueOf(inum));
	}

	public StringBuffer insert(int offset, long lnum) {
		return insert(offset, Long.toString(lnum, 10));
	}

	public synchronized StringBuffer insert(int offset, Object obj) {
		return insert(offset, obj.toString());
	}

	public synchronized StringBuffer insert(int offset, String str) {
		if (offset < 0 || offset > count)
			// throw new StringIndexOutOfBoundsException(offset);
			return this;
		if (str == null)
			str = "null";
		// int len = str.count;
		int len = str.length();
		ensureCapacity_unsynchronized(count + len);
		arraycopy(value, offset, value, offset + len, count - offset);
		str.getChars(0, len, value, offset);
		count += len;
		return this;
	}

	public int indexOf(String str) {
		return indexOf(str, 0);
	}

	public synchronized int indexOf(String str, int fromIndex) {
		if (fromIndex < 0)
			fromIndex = 0;
		int limit = count - str.length();
		for (; fromIndex <= limit; fromIndex++)
			if (regionMatches(fromIndex, str))
				return fromIndex;
		return -1;
	}

	private boolean regionMatches(int toffset, String other) {
		int len = other.length();
		int index = 0;
		while (--len >= 0)
			if (value[toffset++] != other.charAt(index++))
				return false;
		return true;
	}

	public synchronized int length() {
		return count;
	}

	public synchronized StringBuffer reverse() {
		// Call ensureCapacity to enforce copy-on-write.
		ensureCapacity_unsynchronized(count);
		for (int i = count >> 1, j = count - i; --i >= 0; ++j) {
			char c = value[i];
			value[i] = value[j];
			value[j] = c;
		}
		return this;
	}

	public synchronized void setCharAt(int index, char ch) {
		if (index < 0 || index >= count)
			throw new StringIndexOutOfBoundsException(index);
		// Call ensureCapacity to enforce copy-on-write.
		ensureCapacity_unsynchronized(count);
		value[index] = ch;
	}

	public synchronized void setLength(int newLength) {
		if (newLength < 0)
			throw new StringIndexOutOfBoundsException(newLength);

		ensureCapacity_unsynchronized(newLength);
		while (count < newLength)
			value[count++] = '\0';
		count = newLength;
	}

	private void arraycopy(char[] src, int src_pos, char[] dst, int dst_pos,
			int count) {

		if (src_pos < 0 || dst_pos < 0 || count < 0
				|| count + src_pos > src.length || count + dst_pos > dst.length) {
			throw new ArrayIndexOutOfBoundsException("String.arraycopy");
		}

		for (int i = 0; i < count; ++i)
			dst[dst_pos + i] = src[src_pos + i];
	}

	/**
	 * Creates a substring of this StringBuffer, starting at a specified index
	 * and ending at one character before a specified index. This is implemented
	 * the same as <code>substring(beginIndex, endIndex)</code>, to satisfy
	 * the CharSequence interface.
	 *
	 * @param beginIndex index to start at (inclusive, base 0)
	 * @param endIndex index to end at (exclusive)
	 * @return new String which is a substring of this StringBuffer
	 * @throws IndexOutOfBoundsException if beginIndex or endIndex is out of
	 *         bounds
	 * @see #substring(int, int)
	 * @since 1.4
	 */
	public CharSequence subSequence(int beginIndex, int endIndex) {
		return substring(beginIndex, endIndex);
	}

	/**
	 * Creates a substring of this StringBuffer, starting at a specified index
	 * and ending at one character before a specified index.
	 *
	 * @param beginIndex index to start at (inclusive, base 0)
	 * @param endIndex index to end at (exclusive)
	 * @return new String which is a substring of this StringBuffer
	 * @throws StringIndexOutOfBoundsException if beginIndex or endIndex is out
	 *         of bounds
	 * @since 1.2
	 */
	public synchronized String substring(int beginIndex, int endIndex) {
		int len = endIndex - beginIndex;
		if (beginIndex < 0 || endIndex > count || endIndex < beginIndex)
			throw new StringIndexOutOfBoundsException();
		if (len == 0)
			return "";
		// Package constructor avoids an array copy.
		return new String(value, beginIndex, len);
	}

}
