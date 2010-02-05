/* String.java -- immutable character sequences; the object of string literals
 Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 Free Software Foundation, Inc.

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

import java.io.UnsupportedEncodingException;

public final class String implements CharSequence {
	/**
	 * Characters which make up the String. Package access is granted for use by
	 * StringBuffer.
	 */
	final char[] value;

	/**
	 * Creates an empty String (length 0). Unless you really need a new object,
	 * consider using <code>""</code> instead. CLCD 1.0
	 */
	public String() {
		value = "".value;
	}

	/**
	 * Creates a new String using the byte array. Uses the encoding of the
	 * platform's default charset, so the resulting string may be longer or
	 * shorter than the byte array. For more decoding control, use
	 * {@link java.nio.charset.CharsetDecoder}. The behavior is not specified
	 * if the decoder encounters invalid characters; this implementation throws
	 * an Error.
	 * 
	 * @param data
	 *            byte array to copy
	 * @throws NullPointerException
	 *             if data is null
	 * @throws UnsupportedEncodingException
	 * @throws Error
	 *             if the decoding fails
	 * @see #String(byte[], int, int)
	 * @see #String(byte[], int, int, String)
	 * @since 1.1
	 */
	public String(byte[] data) throws NullPointerException {
		this(data, 0, data.length);
	}

	/**
	 * Creates a new String using the portion of the byte array starting at the
	 * offset and ending at offset + count. Uses the encoding of the platform's
	 * default charset, so the resulting string may be longer or shorter than
	 * the byte array. For more decoding control, use
	 * {@link java.nio.charset.CharsetDecoder}. The behavior is not specified
	 * if the decoder encounters invalid characters; this implementation throws
	 * an Error.
	 * 
	 * @param data
	 *            byte array to copy
	 * @param offset
	 *            the offset to start at
	 * @param count
	 *            the number of bytes in the array to use
	 * @throws
	 * @throws NullPointerException
	 *             if data is null
	 * @throws IndexOutOfBoundsException
	 *             if offset or count is incorrect
	 * @throws Error
	 *             if the decoding fails
	 * @see #String(byte[], int, int, String)
	 * @since 1.1
	 */

	public String(byte[] data, int offset, int count) {
		
		if (data.length - offset < count)
			throw new StringIndexOutOfBoundsException();

		char[] cbuf = new char[count];
		for (int i = 0; i < count; i++) {
			cbuf[i] = (char) data[i + offset];
		}

		this.value = cbuf;

	}

	/**
	 * Creates a new String using the portion of the byte array starting at the
	 * offset and ending at offset + count. Uses the specified encoding type to
	 * decode the byte array, so the resulting string may be longer or shorter
	 * than the byte array. For more decoding control, use
	 * {@link java.nio.charset.CharsetDecoder}, and for valid character sets,
	 * see {@link java.nio.charset.Charset}. The behavior is not specified if
	 * the decoder encounters invalid characters; this implementation throws an
	 * Error.
	 * 
	 * @param data
	 *            byte array to copy
	 * @param offset
	 *            the offset to start at
	 * @param count
	 *            the number of bytes in the array to use
	 * @param encoding
	 *            the name of the encoding to use
	 * @throws NullPointerException
	 *             if data or encoding is null
	 * @throws IndexOutOfBoundsException
	 *             if offset or count is incorrect (while unspecified, this is a
	 *             StringIndexOutOfBoundsException)
	 * @throws UnsupportedEncodingException
	 *             if encoding is not found
	 * @throws Error
	 *             if the decoding fails
	 * @since 1.1
	 */
	public String(byte[] data, int offset, int count, String encoding)
			throws UnsupportedEncodingException {

		encoding = encoding.toUpperCase();
		if (!encoding.equals("ASCII"))
			throw new UnsupportedEncodingException();

		if (offset < 0)
			throw new StringIndexOutOfBoundsException();
		if (count < 0)
			throw new StringIndexOutOfBoundsException();
		// equivalent to: offset + count < 0 || offset + count > data.length
		if (data.length - offset < count)
			throw new StringIndexOutOfBoundsException();

		char[] cbuf = new char[count];
		for (int i = 0; i < count; i++) {
			cbuf[i] = (char) data[i + offset];
		}

		this.value = cbuf;

	}

	/**
	 * Creates a new String using the byte array. Uses the specified encoding
	 * type to decode the byte array, so the resulting string may be longer or
	 * shorter than the byte array. For more decoding control, use
	 * {@link java.nio.charset.CharsetDecoder}, and for valid character sets,
	 * see {@link java.nio.charset.Charset}. The behavior is not specified if
	 * the decoder encounters invalid characters; this implementation throws an
	 * Error.
	 * 
	 * @param data
	 *            byte array to copy
	 * @param encoding
	 *            the name of the encoding to use
	 * @throws NullPointerException
	 *             if data or encoding is null
	 * @throws UnsupportedEncodingException
	 *             if encoding is not found
	 * @throws Error
	 *             if the decoding fails
	 * @see #String(byte[], int, int, String)
	 * @since 1.1
	 */
	public String(byte[] data, String encoding)
			throws UnsupportedEncodingException {
		this(data, 0, data.length, encoding);
	}

	/**
	 * Creates a new String using the character sequence of the char array.
	 * Subsequent changes to data do not affect the String.
	 * 
	 * @param data
	 *            char array to copy
	 * @throws NullPointerException
	 *             if data is null
	 */
	// public String(char[] data) {
	// this(data, 0, data.length);
	// }
	public String(char[] ca) {
		int count = ca.length;
		value = new char[count];
		for (int i = 0; i < count; ++i)
			value[i] = ca[i];
	}

	/**
	 * Creates a new String using the character sequence of a subarray of
	 * characters. The string starts at offset, and copies count chars.
	 * Subsequent changes to data do not affect the String.
	 * 
	 * @param data
	 *            char array to copy
	 * @param offset
	 *            position (base 0) to start copying out of data
	 * @param count
	 *            the number of characters from data to copy
	 * @throws NullPointerException
	 *             if data is null
	 * @throws IndexOutOfBoundsException
	 *             if (offset &lt; 0 || count &lt; 0 || offset + count &lt; 0
	 *             (overflow) || offset + count &gt; data.length) (while
	 *             unspecified, this is a StringIndexOutOfBoundsException)
	 */
	public String(char[] data, int offset, int count)
			throws IndexOutOfBoundsException {

		if (offset < 0)
			throw new StringIndexOutOfBoundsException();
		if (count < 0)
			throw new StringIndexOutOfBoundsException();
		// equivalent to: offset + count < 0 || offset + count > data.length
		if (data.length - offset < count)
			throw new StringIndexOutOfBoundsException();
		value = new char[count];
		// VMSystem.arraycopy(data, offset, value, 0, count);
		//System.out.println("dbg: String constructor");
		// TODO: System.arraycopy produces stack overflow
		for (int i = 0; i < count; i++) {
			value[i] = data[i + offset];
		}
		// System.arraycopy(data, offset, value, 0, count);

	}

	/**
	 * Copies the contents of a String to a new String. Since Strings are
	 * immutable, only a shallow copy is performed.
	 * 
	 * @param str
	 *            String to copy
	 * @throws NullPointerException
	 *             if value is null CLDC 1.0
	 */
	public String(String str) {
		value = str.value;
	}

	/**
	 * Creates a new String using the character sequence represented by the
	 * StringBuffer. Subsequent changes to buf do not affect the String.
	 * 
	 * @param buffer
	 *            StringBuffer to copy
	 * @throws NullPointerException
	 *             if buffer is null
	 */
	public String(StringBuffer buffer) throws NullPointerException {
		synchronized (buffer) {

			int count = buffer.length();
			value = new char[count];
			// VMSystem.arraycopy(buffer.value, 0, value, 0, count);
			// System.arraycopy(buffer.value, 0, value, 0, count);
			for (int i = 0; i < count; i++)
				value[i] = buffer.value[i];
		}
	}

	/*
	 * final char[] value;
	 * 
	 * public String() { value = "".value; }
	 * 
	 * public String(String str) { value = str.value; }
	 * 
	 * public String(char[] ca) { int count = ca.length; value = new
	 * char[count]; for (int i=0; i<count; ++i) value[i] = ca[i]; } public
	 * String(StringBuffer str) { int count = str.length(); value = new
	 * char[count]; for (int i=0; i<count; ++i) value[i] = str.value[i]; }
	 */

	public char charAt(int index) {

		if (index < 0 || index >= value.length)
			throw new StringIndexOutOfBoundsException(index);

		return value[index];
	}

	public int compareTo(String anotherString) {

		if (anotherString == null)
			throw new NullPointerException();
		int i = (value.length < anotherString.value.length) ? value.length
				: anotherString.value.length;
		int j1 = 0;
		int j2 = 0;
		while (--i >= 0) {
			int result = value[j1++] - anotherString.value[j2++];
			if (result != 0)
				return result;
		}
		return value.length - anotherString.value.length;
	}

	public String concat(String str) {
		if (str == null)
			throw new NullPointerException();
		if (str.value.length == 0)
			return this;
		if (value.length == 0)
			return str;
		char[] newStr = new char[value.length + str.value.length];
		// VMSystem.arraycopy(value, offset, newStr, 0, count);
		// VMSystem.arraycopy(str.value, str.offset, newStr, count, str.count);
		// TODO: System.arraycopy throws exception
		// System.arraycopy(value, 0, newStr, 0, value.length);
		// System.arraycopy(str.value, 0, newStr, value.length,
		// str.value.length);

		// TODO: inefficient
		for (int i = 0; i < value.length; i++) {
			newStr[i] = value[i];
		}
		for (int i2 = 0; i2 < str.value.length; i2++) {
			newStr[i2 + value.length] = str.value[i2];
		}

		// Package constructor avoids an array copy.
		// System.out.println(newStr.length);
		return new String(newStr, 0, newStr.length);
	}

	public boolean endsWith(String suffix) {
		if (suffix == null)
			throw new NullPointerException();
		return regionMatches(false, value.length - suffix.value.length, suffix,
				0, suffix.value.length);
	}

	public boolean equals(Object anObject) {
		if (!(anObject instanceof String))
			return false;
		String str2 = (String) anObject;
		if (value.length != str2.value.length)
			return false;
		if (value == str2.value)
			return true;
		int i = value.length;
		int x = 0;
		int y = 0;
		while (--i >= 0)
			if (value[x++] != str2.value[y++])
				return false;
		return true;
	}

	public byte[] getBytes() {
		// XXX - Throw an error here?
		// For now, default to the 'safe' encoding.
		byte[] bytes = new byte[value.length];
		for (int i = 0; i < value.length; i++) {
			if (value[i] <= 0xFF) {
				bytes[i] = (byte) value[i];
			} else {
				bytes[i] = (byte) '?';
			}
		}
		return bytes;

	}

	public byte[] getBytes(String enc) throws UnsupportedEncodingException {
		enc = enc.toUpperCase();
		if (!enc.equals("ASCII"))
			throw new UnsupportedEncodingException(
					"at String.getBytes(String encoding)");
		return this.getBytes();
	}

	public void getChars(int srcBegin, int srcEnd, char dst[], int dstBegin) {
		if (srcBegin < 0 || srcBegin > srcEnd || srcEnd > value.length)
			throw new StringIndexOutOfBoundsException();
		/*
		 * if (srcBegin < 0 || srcBegin > srcEnd || srcEnd > value.length)
		 * return; // wait for array bound exception!
		 */
		/*
		 * System.arraycopy(value, srcBegin + offset, dst, dstBegin, srcEnd -
		 * srcBegin);
		 */
		for (int i = 0; i < srcEnd - srcBegin; ++i)
			dst[dstBegin + i] = value[srcBegin + i];
	}

	public int hashCode() {

		// Compute the hash code using a local variable to be reentrant.
		int hashCode = 0;
		int limit = value.length;
		for (int i = 0; i < limit; i++)
			hashCode = hashCode * 31 + value[i];
		return hashCode;
	}

	public int indexOf(int ch) {
		return indexOf(ch, 0);
	}

	public int indexOf(int ch, int fromIndex) {
		if ((char) ch != ch)
			return -1;
		if (fromIndex < 0)
			fromIndex = 0;
		int i = fromIndex;
		for (; fromIndex < value.length; fromIndex++)
			if (value[i++] == ch)
				return fromIndex;
		return -1;
	}

	public int indexOf(String str) {
		return indexOf(str, 0);
	}

	//
	// for CoffeinMarkEmbedded
	//
	public int indexOf(String str, int fromIndex) {
		if (fromIndex < 0)
			fromIndex = 0;
		int limit = value.length - str.value.length;
		for (; fromIndex <= limit; fromIndex++)
			if (regionMatches(fromIndex, str, 0, str.value.length))
				return fromIndex;
		return -1;
	}

	public int lastIndexOf(int ch) {
		return lastIndexOf(ch, value.length - 1);
	}

	public int lastIndexOf(int ch, int fromIndex) {
		if ((char) ch != ch)
			return -1;
		if (fromIndex >= value.length)
			fromIndex = value.length - 1;
		int i = fromIndex;
		for (; fromIndex >= 0; fromIndex--)
			if (value[i--] == ch)
				return fromIndex;
		return -1;
	}

	public int length() {
		return value.length;
	}

	public boolean regionMatches(boolean ignoreCase, int toffset, String other,
			int ooffset, int len) {
		if (toffset < 0 || ooffset < 0 || toffset + len > value.length
				|| ooffset + len > other.value.length)
			return false;

		while (--len >= 0) {
			char c1 = value[toffset++];
			char c2 = other.value[ooffset++];
			// Note that checking c1 != c2 is redundant when ignoreCase is true,
			// but it avoids method calls.
			if (c1 != c2
					&& (!ignoreCase || (Character.toLowerCase(c1) != Character
							.toLowerCase(c2) && (Character.toUpperCase(c1) != Character
							.toUpperCase(c2)))))
				return false;
		}
		return true;
	}

	public String replace(char oldChar, char newChar) {
		if (oldChar == newChar)
			return this;
		int i = value.length;
		int x = -1;
		while (--i >= 0)
			if (value[++x] == oldChar)
				break;
		if (i < 0)
			return this;
		// char[] newStr = (char[]) value.clone();
		char[] newStr = new char[value.length];

		//TODO: System.arraycopy crashes
		//System.arraycopy(value, 0, newStr, 0, value.length);
		for (int i2 = 0; i2 < value.length; i2++) {
			newStr[i2] = value[i2];
		}
		newStr[x] = newChar;
		while (--i >= 0)
			if (value[++x] == oldChar)
				newStr[x] = newChar;
		// Package constructor avoids an array copy.
		return new String(newStr, 0, value.length);
	}

	public boolean regionMatches(int toffset, String other, int ooffset, int len) {
		if (toffset < 0 || ooffset < 0 || toffset + len > value.length
				|| ooffset + len > other.value.length)
			return false;
		while (--len >= 0) {
			char c1 = value[toffset++];
			char c2 = other.value[ooffset++];
			if (c1 != c2)
				return false;
		}
		return true;
	}

	public boolean startsWith(String prefix, int toffset) {
		return regionMatches(false, toffset, prefix, 0, prefix.value.length);
	}

	public boolean startsWith(String prefix) {
		return regionMatches(false, 0, prefix, 0, prefix.value.length);
	}

	/**
	 * Creates a substring of this String, starting at a specified index
	 * and ending at one character before a specified index. This behaves like
	 * <code>substring(begin, end)</code>.
	 *
	 * @param begin index to start substring (inclusive, base 0)
	 * @param end index to end at (exclusive)
	 * @return new String which is a substring of this String
	 * @throws IndexOutOfBoundsException if begin &lt; 0 || end &gt; length()
	 *         || begin &gt; end
	 * @since 1.4
	 */
	public CharSequence subSequence(int begin, int end) {
		return substring(begin, end);
	}

	public String substring(int begin) {
		return substring(begin, value.length);
	}

	public String substring(int beginIndex, int endIndex) {
		if (beginIndex < 0 || endIndex > value.length || beginIndex > endIndex)
			throw new StringIndexOutOfBoundsException();
		if (beginIndex == 0 && endIndex == value.length)
			return this;
		int len = endIndex - beginIndex;
		// Package constructor avoids an array copy.
		return new String(value, beginIndex, len);
	}

	public char[] toCharArray() {
		char[] copy = new char[value.length];
		// VMSystem.arraycopy(value, offset, copy, 0, count);
		//System.arraycopy(value, 0, copy, 0, value.length);
		for (int i = 0; i < value.length; i++) {
			copy[i] = value[i];
		}
		return copy;
	}

	public String toLowerCase() {
		char[] tmpchar = new char[this.value.length];
		for (int i = 0; i < this.value.length; i++) {
			tmpchar[i] = Character.toLowerCase(this.value[i]);
		}
		return new String(tmpchar);
	}

	public String toString() {
		return this;
	}

	public String toUpperCase() {
		char[] tmpchar = new char[this.value.length];
		for (int i = 0; i < this.value.length; i++) {
			tmpchar[i] = Character.toUpperCase(this.value[i]);
		}
		return new String(tmpchar);
	}

	public String trim() {
		int limit = value.length;
		if (value.length == 0
				|| (value[0] > '\u0020' && value[limit - 1] > '\u0020'))
			return this;
		int begin = 0;
		do
			if (begin == limit)
				return "";
		while (value[begin++] <= '\u0020');
		int end = limit;
		while (value[--end] <= '\u0020')
			;
		return substring(begin - 1, end + 1);
	}

	public static String valueOf(boolean b) {
		return b ? "true" : "false";
	}

	public static String valueOf(char c) {
		// Package constructor avoids an array copy.
		return new String(new char[] { c }, 0, 1);
	}

	public static String valueOf(char[] data) {
		return valueOf(data, 0, data.length);
	}

	public static String valueOf(char[] data, int offset, int count) {
		return new String(data, offset, count);
	}

	public static String valueOf(int i) {
		// See Integer to understand why we call the two-arg variant.
		return Integer.toString(i, 10);
	}

	public static String valueOf(long l) {
		return Long.toString(l);
	}

	public static String valueOf(Object obj) {
		return obj == null ? "null" : obj.toString();
	}

	/**
	 * Returns the value array of the given string if it is zero based or a copy
	 * of it that is zero based (stripping offset and making length equal to
	 * count). Used for accessing the char[]s of gnu.java.lang.CharData. Package
	 * private for use in Character.
	 */

	// what is this used for???
	// it's not in the JDK
	// static char[] zeroBasedStringValue(String s) {
	// char[] value;
	//
	// if ( s.count == s.value.length)
	// value = s.value;
	// else {
	// int count = s.count;
	// value = new char[count];
	// // VMSystem.arraycopy(s.value, s.offset, value, 0, count);
	// System.arraycopy(s.value, 0, value, 0, count);
	//
	// }
	//
	// return value;
	// }

	// this is needed for the collection classes
	public boolean equalsIgnoreCase(String b) {

		// avoid NullPointerExceptions
		if (b == null)
			return false;

		return toUpperCase().equals(b.toUpperCase());
	}
	
	  /** @since 1.5 */
	  public static String format(String format, Object... args)
	  {
	    return format + " String.format() not implemented";
	  }
}
