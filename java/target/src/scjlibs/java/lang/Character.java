/* java.lang.Character -- Wrapper class for char, and Unicode subsets
 Copyright (C) 1998, 1999, 2001, 2002, 2005 Free Software Foundation, Inc.

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

package java.lang;

import java.lang.CharSequence;

import annotation.ScopeSafe;


public final class Character {

	//	TODO: NOTE:  works only for ASCII encoding
	
	/**
	 * Largest value allowed for radix arguments in Java. This value is 36.
	 * 
	 * @see #digit(char,int)
	 * @see #forDigit(int,int)
	 * @see Integer#toString(int,int)
	 * @see Integer#valueOf(String)
	 */
	public static final int MAX_RADIX = 36;

	/**
	 * The maximum value the char data type can hold. This value is
	 * <code>'\\uFFFF'</code>.
	 */
	public static final char MAX_VALUE = '\u007F';

	/**
	 * Smallest value allowed for radix arguments in Java. This value is 2.
	 * 
	 * @see #digit(char,int)
	 * @see #forDigit(int,int)
	 * @see Integer#toString(int,int)
	 * @see Integer#valueOf(String)
	 */
	public static final int MIN_RADIX = 2;

	/**
	 * The minimum value the char data type can hold. This value is
	 * <code>'\\u0000'</code>.
	 */
	public static final char MIN_VALUE = '\u0000';

	/***********************************************************/ 
	/**
     * The minimum value of a supplementary code point.
     *
     * @since 1.5
     */
    public static final int MIN_SUPPLEMENTARY_CODE_POINT = 0x010000;

    /**
     * The maximum value of a Unicode code point.
     *
     * @since 1.5
     */
    public static final int MAX_CODE_POINT = 0x10ffff;  
    
    /**
     * The minimum value of a Unicode low-surrogate code unit in the
     * UTF-16 encoding. A low-surrogate is also known as a
     * <i>trailing-surrogate</i>.
     *
     * @since 1.5
     */
    public static final char MIN_LOW_SURROGATE  = '\uDC00';

    /**
     * The maximum value of a Unicode low-surrogate code unit in the
     * UTF-16 encoding. A low-surrogate is also known as a
     * <i>trailing-surrogate</i>.
     *
     * @since 1.5
     */
    public static final char MAX_LOW_SURROGATE  = '\uDFFF';
    
    /**
     * The minimum value of a Unicode high-surrogate code unit in the
     * UTF-16 encoding. A high-surrogate is also known as a
     * <i>leading-surrogate</i>.
     *
     * @since 1.5
     */
    public static final char MIN_HIGH_SURROGATE = '\uD800';
    
    /**
     * The maximum value of a Unicode high-surrogate code unit in the
     * UTF-16 encoding. A high-surrogate is also known as a
     * <i>leading-surrogate</i>.
     *
     * @since 1.5
     */
    public static final char MAX_HIGH_SURROGATE = '\uDBFF';
    
    /**
     * The minimum value of a Unicode surrogate code unit in the UTF-16 encoding.
     *
     * @since 1.5
     */
    public static final char MIN_SURROGATE = MIN_HIGH_SURROGATE;

    /**
     * The maximum value of a Unicode surrogate code unit in the UTF-16 encoding.
     *
     * @since 1.5
     */
    public static final char MAX_SURROGATE = MAX_LOW_SURROGATE;
    
    /**
     * The minimum value of a Unicode code point.
     * 
     * @since 1.5
     */
    public static final int MIN_CODE_POINT = 0x000000;
    /*************************************************************/

    
    /**
	 * The immutable value of this Character.
	 * 
	 * @serial the value of this Character
	 */
	private final char value;

	/**
	 * Wraps up a character.
	 * 
	 * @param value
	 *            the character to wrap
	 */
	public Character(char value) {
		this.value = value;
	}

	public char charValue() {
		return value;
	}

	public static int digit(char ch, int radix) {
		// TODO: only for radix 10 at the moment
		if (radix != 10)
			throw new IllegalArgumentException(
					"lang.Character: works only for radix 10");
		int intch = (int) ch;
		if (48 <= intch && intch <= 57)
			return intch - 48;
		return -1;
	}

	public boolean equals(Object o) {
		// TODO: instance of not implemented
		// return o instanceof Character && value == ((Character) o).value;
		return value == ((Character) o).value;
	}

	public int hashCode() {
		return value;
	}

	public static boolean isDigit(char ch) {
		int intch = (int) ch;
		if (48 <= intch && intch <= 57)
			return true;
		return false;
	}

	public static boolean isLowerCase(char ch) {
		int intch = (int) ch;
		if (97 <= intch && intch <= 122)
			return true;
		return false;
	}

	public static boolean isUpperCase(char ch) {
		int intch = (int) ch;
		if (65 <= intch && intch <= 90)
			return true;
		return false;
	}

	public static char toLowerCase(char ch) {
		int intch = (int) ch;
		if (97 <= intch && intch <= 122)
			return ch;
		if (65 <= intch && intch <= 90)
			return (char) (intch + 32);
		return ch;
	}

	public String toString() {
		// Package constructor avoids an array copy.
		return new String(new char[] { value }, 0, 1);
	}

	@ScopeSafe
	public static char toUpperCase(char ch) {
		int intch = (int) ch;
		if (97 <= intch && intch <= 122)
			return (char) (intch - 32);
		if (65 <= intch && intch <= 90)
			return ch;
		return ch;
	}

    /**
     * Converts the specified character (Unicode code point) to its
     * UTF-16 representation stored in a <code>char</code> array. If
     * the specified code point is a BMP (Basic Multilingual Plane or
     * Plane 0) value, the resulting <code>char</code> array has
     * the same value as <code>codePoint</code>. If the specified code
     * point is a supplementary code point, the resulting
     * <code>char</code> array has the corresponding surrogate pair.
     *
     * @param  codePoint a Unicode code point
     * @return a <code>char</code> array having
     *         <code>codePoint</code>'s UTF-16 representation.
     * @exception IllegalArgumentException if the specified
     * <code>codePoint</code> is not a valid Unicode code point.
     * @since  1.5
     */
    public static char[] toChars(int codePoint) {
        if (codePoint < 0 || codePoint > MAX_CODE_POINT) {
            throw new IllegalArgumentException();
        }
        if (codePoint < MIN_SUPPLEMENTARY_CODE_POINT) {
                return new char[] { (char) codePoint };
        }
        char[] result = new char[2];
        toSurrogates(codePoint, result, 0);
        return result;
    }

    static void toSurrogates(int codePoint, char[] dst, int index) {
        int offset = codePoint - MIN_SUPPLEMENTARY_CODE_POINT;
        dst[index+1] = (char)((offset & 0x3ff) + MIN_LOW_SURROGATE);
        dst[index] = (char)((offset >>> 10) + MIN_HIGH_SURROGATE);
    }
    
    /**
     * Returns the code point preceding the given index of the
     * <code>char</code> array. If the <code>char</code> value at
     * <code>(index - 1)</code> in the <code>char</code> array is in
     * the low-surrogate range, <code>(index - 2)</code> is not
     * negative, and the <code>char</code> value at <code>(index -
     * 2)</code> in the <code>char</code> array is in the
     * high-surrogate range, then the supplementary code point
     * corresponding to this surrogate pair is returned. Otherwise,
     * the <code>char</code> value at <code>(index - 1)</code> is
     * returned.
     *
     * @param a the <code>char</code> array
     * @param index the index following the code point that should be returned
     * @return the Unicode code point value before the given index.
     * @exception NullPointerException if <code>a</code> is null.
     * @exception IndexOutOfBoundsException if the <code>index</code>
     * argument is less than 1 or greater than the length of the
     * <code>char</code> array
     * @since  1.5
     */
    public static int codePointBefore(char[] a, int index) {
        return codePointBeforeImpl(a, index, 0);
    }
    
    /**
     * Returns the code point preceding the given index of the
     * <code>char</code> array, where only array elements with
     * <code>index</code> greater than or equal to <code>start</code>
     * can be used. If the <code>char</code> value at <code>(index -
     * 1)</code> in the <code>char</code> array is in the
     * low-surrogate range, <code>(index - 2)</code> is not less than
     * <code>start</code>, and the <code>char</code> value at
     * <code>(index - 2)</code> in the <code>char</code> array is in
     * the high-surrogate range, then the supplementary code point
     * corresponding to this surrogate pair is returned. Otherwise,
     * the <code>char</code> value at <code>(index - 1)</code> is
     * returned.
     *
     * @param a the <code>char</code> array
     * @param index the index following the code point that should be returned
     * @param start the index of the first array element in the
     * <code>char</code> array
     * @return the Unicode code point value before the given index.
     * @exception NullPointerException if <code>a</code> is null.
     * @exception IndexOutOfBoundsException if the <code>index</code>
     * argument is not greater than the <code>start</code> argument or
     * is greater than the length of the <code>char</code> array, or
     * if the <code>start</code> argument is negative or not less than
     * the length of the <code>char</code> array.
     * @since  1.5
     */
    public static int codePointBefore(char[] a, int index, int start) {
	if (index <= start || start < 0 || start >= a.length) {
	    throw new IndexOutOfBoundsException();
	}
	return codePointBeforeImpl(a, index, start);
    }

    static int codePointBeforeImpl(char[] a, int index, int start) {
        char c2 = a[--index];
        if (isLowSurrogate(c2)) {
            if (index > start) {
                char c1 = a[--index];
                if (isHighSurrogate(c1)) {
                    return toCodePoint(c1, c2);
                }
            }
        }
        return c2;
    }

    /**
     * Returns the code point preceding the given index of the
     * <code>CharSequence</code>. If the <code>char</code> value at
     * <code>(index - 1)</code> in the <code>CharSequence</code> is in
     * the low-surrogate range, <code>(index - 2)</code> is not
     * negative, and the <code>char</code> value at <code>(index -
     * 2)</code> in the <code>CharSequence</code> is in the
     * high-surrogate range, then the supplementary code point
     * corresponding to this surrogate pair is returned. Otherwise,
     * the <code>char</code> value at <code>(index - 1)</code> is
     * returned.
     *
     * @param seq the <code>CharSequence</code> instance
     * @param index the index following the code point that should be returned
     * @return the Unicode code point value before the given index.
     * @exception NullPointerException if <code>seq</code> is null.
     * @exception IndexOutOfBoundsException if the <code>index</code>
     * argument is less than 1 or greater than {@link
     * CharSequence#length() seq.length()}.
     * @since  1.5
     */
    public static int codePointBefore(CharSequence seq, int index) {
        char c2 = seq.charAt(--index);
        if (isLowSurrogate(c2)) {
            if (index > 0) {
                char c1 = seq.charAt(--index);
                if (isHighSurrogate(c1)) {
                    return toCodePoint(c1, c2);
                }
            }
        }
        return c2;
    }

    /**
     * Converts the specified surrogate pair to its supplementary code
     * point value. This method does not validate the specified
     * surrogate pair. The caller must validate it using {@link
     * #isSurrogatePair(char, char) isSurrogatePair} if necessary.
     *
     * @param  high the high-surrogate code unit
     * @param  low the low-surrogate code unit
     * @return the supplementary code point composed from the
     *         specified surrogate pair.
     * @since  1.5
     */
    public static int toCodePoint(char high, char low) {
        return ((high - MIN_HIGH_SURROGATE) << 10)
            + (low - MIN_LOW_SURROGATE) + MIN_SUPPLEMENTARY_CODE_POINT;
    }

	/**
     * Determines if the given <code>char</code> value is a
     * high-surrogate code unit (also known as <i>leading-surrogate
     * code unit</i>). Such values do not represent characters by
     * themselves, but are used in the representation of <a
     * href="#supplementary">supplementary characters</a> in the
     * UTF-16 encoding.
     *
     * <p>This method returns <code>true</code> if and only if
     * <blockquote><pre>ch >= '&#92;uD800' && ch <= '&#92;uDBFF'
     * </pre></blockquote>
     * is <code>true</code>.
     *
     * @param   ch   the <code>char</code> value to be tested.
     * @return  <code>true</code> if the <code>char</code> value
     *          is between '&#92;uD800' and '&#92;uDBFF' inclusive;
     *          <code>false</code> otherwise.
     * @see     java.lang.Character#isLowSurrogate(char)
     * @see     Character.UnicodeBlock#of(int)
     * @since   1.5
     */
    public static boolean isHighSurrogate(char ch) {
        return ch >= MIN_HIGH_SURROGATE && ch <= MAX_HIGH_SURROGATE;
    }

	/**
     * Determines if the given <code>char</code> value is a
     * low-surrogate code unit (also known as <i>trailing-surrogate code
     * unit</i>). Such values do not represent characters by themselves,
     * but are used in the representation of <a
     * href="#supplementary">supplementary characters</a> in the UTF-16 encoding.
     *
     * <p> This method returns <code>true</code> if and only if
     * <blockquote><pre>ch >= '&#92;uDC00' && ch <= '&#92;uDFFF'
     * </pre></blockquote> is <code>true</code>.
     *
     * @param   ch   the <code>char</code> value to be tested.
     * @return  <code>true</code> if the <code>char</code> value
     *          is between '&#92;uDC00' and '&#92;uDFFF' inclusive;
     *          <code>false</code> otherwise.
     * @see java.lang.Character#isHighSurrogate(char)
     * @since   1.5
     */
    public static boolean isLowSurrogate(char ch) {
        return ch >= MIN_LOW_SURROGATE && ch <= MAX_LOW_SURROGATE;
    }
    
    /**
     * Determines whether the specified code point is a valid Unicode
     * code point value in the range of <code>0x0000</code> to
     * <code>0x10FFFF</code> inclusive. This method is equivalent to
     * the expression:
     *
     * <blockquote><pre>
     * codePoint >= 0x0000 && codePoint <= 0x10FFFF
     * </pre></blockquote>
     *
     * @param  codePoint the Unicode code point to be tested
     * @return <code>true</code> if the specified code point value
     * is a valid code point value;
     * <code>false</code> otherwise.
     * @since  1.5
     */
    public static boolean isValidCodePoint(int codePoint) {
        return codePoint >= MIN_CODE_POINT && codePoint <= MAX_CODE_POINT;
    }
    
    public static int codePointAt(CharSequence seq, int index) {
        char c1 = seq.charAt(index++);
        if (isHighSurrogate(c1)) {
            if (index < seq.length()) {
                char c2 = seq.charAt(index);
                if (isLowSurrogate(c2)) {
                    return toCodePoint(c1, c2);
                }
            }
        }
        return c1;
    }

    /**
     * Returns the code point at the given index of the
     * <code>char</code> array. If the <code>char</code> value at
     * the given index in the <code>char</code> array is in the
     * high-surrogate range, the following index is less than the
     * length of the <code>char</code> array, and the
     * <code>char</code> value at the following index is in the
     * low-surrogate range, then the supplementary code point
     * corresponding to this surrogate pair is returned. Otherwise,
     * the <code>char</code> value at the given index is returned.
     *
     * @param a the <code>char</code> array
     * @param index the index to the <code>char</code> values (Unicode
     * code units) in the <code>char</code> array to be converted
     * @return the Unicode code point at the given index
     * @exception NullPointerException if <code>a</code> is null.
     * @exception IndexOutOfBoundsException if the value
     * <code>index</code> is negative or not less than
     * the length of the <code>char</code> array.
     * @since  1.5
     */
    public static int codePointAt(char[] a, int index) {
	return codePointAtImpl(a, index, a.length);
    }

    /**
     * Returns the code point at the given index of the
     * <code>char</code> array, where only array elements with
     * <code>index</code> less than <code>limit</code> can be used. If
     * the <code>char</code> value at the given index in the
     * <code>char</code> array is in the high-surrogate range, the
     * following index is less than the <code>limit</code>, and the
     * <code>char</code> value at the following index is in the
     * low-surrogate range, then the supplementary code point
     * corresponding to this surrogate pair is returned. Otherwise,
     * the <code>char</code> value at the given index is returned.
     *
     * @param a the <code>char</code> array
     * @param index the index to the <code>char</code> values (Unicode
     * code units) in the <code>char</code> array to be converted
     * @param limit the index after the last array element that can be used in the
     * <code>char</code> array
     * @return the Unicode code point at the given index
     * @exception NullPointerException if <code>a</code> is null.
     * @exception IndexOutOfBoundsException if the <code>index</code>
     * argument is negative or not less than the <code>limit</code>
     * argument, or if the <code>limit</code> argument is negative or
     * greater than the length of the <code>char</code> array.
     * @since  1.5
     */
    public static int codePointAt(char[] a, int index, int limit) {
	if (index >= limit || limit < 0 || limit > a.length) {
	    throw new IndexOutOfBoundsException();
	}
	return codePointAtImpl(a, index, limit);
    }

    static int codePointAtImpl(char[] a, int index, int limit) {
        char c1 = a[index++];
        if (isHighSurrogate(c1)) {
            if (index < limit) {
                char c2 = a[index];
                if (isLowSurrogate(c2)) {
                    return toCodePoint(c1, c2);
                }
            }
        }
        return c1;
    }
    
	static int offsetByCodePointsImpl(char[] a, int start, int count,
			int index, int codePointOffset) {
		int x = index;
		if (codePointOffset >= 0) {
			int limit = start + count;
			int i;
			for (i = 0; x < limit && i < codePointOffset; i++) {
				if (isHighSurrogate(a[x++])) {
					if (x < limit && isLowSurrogate(a[x])) {
						x++;
					}
				}
			}
			if (i < codePointOffset) {
				throw new IndexOutOfBoundsException();
			}
		} else {
			int i;
			for (i = codePointOffset; x > start && i < 0; i++) {
				if (isLowSurrogate(a[--x])) {
					if (x > start && isHighSurrogate(a[x - 1])) {
						x--;
					}
				}
			}
			if (i < 0) {
				throw new IndexOutOfBoundsException();
			}
		}
		return x;
	}
	
	static int codePointCountImpl(char[] a, int offset, int count) {
		int endIndex = offset + count;
		int n = 0;
		for (int i = offset; i < endIndex;) {
			n++;
			if (isHighSurrogate(a[i++])) {
				if (i < endIndex && isLowSurrogate(a[i])) {
					i++;
				}
			}
		}
		return n;
	}

} // class Character
