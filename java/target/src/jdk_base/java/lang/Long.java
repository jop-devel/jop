/* Long.java -- object wrapper for long
 Copyright (C) 1998, 1999, 2001, 2002, 2005  Free Software Foundation, Inc.

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

/**
 * Instances of class <code>Long</code> represent primitive <code>long</code>
 * values.
 * 
 * Additionally, this class provides various helper functions and variables
 * related to longs.
 * 
 * @author Paul Fisher
 * @author John Keiser
 * @author Warren Levy
 * @author Eric Blake (ebb9@email.byu.edu)
 * @since 1.0
 * @status updated to 1.5
 */
public final class Long {
	/**
	 * Compatible with JDK 1.0.2+.
	 */
	private static final long serialVersionUID = 4290774380558885855L;

	/**
	 * The minimum value a <code>long</code> can represent is
	 * -9223372036854775808L (or -2<sup>63</sup>).
	 */
	public static final long MIN_VALUE = 0x8000000000000000L;

	/**
	 * The maximum value a <code>long</code> can represent is
	 * 9223372036854775807 (or 2<sup>63</sup> - 1).
	 */
	public static final long MAX_VALUE = 0x7fffffffffffffffL;

	/**
	 * The number of bits needed to represent a <code>long</code>.
	 * 
	 * @since 1.5
	 */
	public static final int SIZE = 64;

	/**
	 * The immutable value of this Long.
	 * 
	 * @serial the wrapped long
	 */
	private final long value;

	/**
	 * Create a <code>Long</code> object representing the value of the
	 * <code>long</code> argument.
	 * 
	 * @param value
	 *            the value to use
	 */
	/**
	 * Table for calculating digits, used in Character, Long, and Integer.
	 */
	static final char[] digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
			'9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
			'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y',
			'z' };

	public Long(long value) {
		this.value = value;
	}

	/**
	 * Returns <code>true</code> if <code>obj</code> is an instance of
	 * <code>Long</code> and represents the same long value.
	 * 
	 * @param obj
	 *            the object to compare
	 * @return whether these Objects are semantically equal
	 */
	public boolean equals(Object obj) {
		// TODO: instancefo not implemented yet
		// return obj instanceof Long && value == ((Long) obj).value;
		return value == ((Long) obj).value;
	}

	/**
	 * Return a hashcode representing this Object. <code>Long</code>'s hash
	 * code is calculated by <code>(int) (value ^ (value &gt;&gt; 32))</code>.
	 * 
	 * @return this Object's hash code
	 */
	public int hashCode() {
		return (int) (value ^ (value >>> 32));
	}

	  public static Long valueOf(String s)
	  {
	    return new Long(parseLong(s, 10, false));
	  }
	  /**
	   * Returns a <code>Long</code> object wrapping the value.
	   *
	   * @param val the value to wrap
	   * @return the <code>Long</code>
	   * @since 1.5
	   */
	  public static Long valueOf(long val)
	  {
	    // We aren't required to cache here.  We could, though perhaps we
	    // ought to consider that as an empirical question.
	    return new Long(val);
	  }

	/**
	 * Return the value of this <code>Long</code>.
	 * 
	 * @return the long value
	 */
	public long longValue() {
		return value;
	}

	public static long parseLong(String s) {
		return parseLong(s, 10, false);
	}

	public static long parseLong(String str, int radix) {
		return parseLong(str, radix, false);
	}

	public String toString() {
		return toString(value, 10);
	}

	public static String toString(long num) {
		return toString(num, 10);
	}

	/**
	 * Converts the <code>long</code> to a <code>String</code> using the
	 * specified radix (base). If the radix exceeds
	 * <code>Character.MIN_RADIX</code> or <code>Character.MAX_RADIX</code>,
	 * 10 is used instead. If the result is negative, the leading character is
	 * '-' ('\\u002D'). The remaining characters come from
	 * <code>Character.forDigit(digit, radix)</code> ('0'-'9','a'-'z').
	 * 
	 * @param num
	 *            the <code>long</code> to convert to <code>String</code>
	 * @param radix
	 *            the radix (base) to use in the conversion
	 * @return the <code>String</code> representation of the argument
	 */
	public static String toString(long num, int radix) {
		// Use the Integer toString for efficiency if possible.
		if ((int) num == num)
			return Integer.toString((int) num, radix);

		if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX)
			radix = 10;

		// For negative numbers, print out the absolute value w/ a leading '-'.
		// Use an array large enough for a binary number.
		char[] buffer = new char[65];
		int i = 65;
		boolean isNeg = false;
		if (num < 0) {
			isNeg = true;
			num = -num;

			// When the value is MIN_VALUE, it overflows when made positive
			if (num < 0) {
				buffer[--i] = digits[(int) (-(num + radix) % radix)];
				num = -(num / radix);
			}
		}

		do {
			buffer[--i] = digits[(int) (num % radix)];
			num /= radix;
		} while (num > 0 && i > 0);

		if (isNeg)
			buffer[--i] = '-';

		// Package constructor avoids an array copy.
		return new String(buffer, i, 65 - i);
	}

	/**
	 * Helper for parsing longs.
	 * 
	 * @param str
	 *            the string to parse
	 * @param radix
	 *            the radix to use, must be 10 if decode is true
	 * @param decode
	 *            if called from decode
	 * @return the parsed long value
	 * @throws NumberFormatException
	 *             if there is an error
	 * @throws NullPointerException
	 *             if decode is true and str is null
	 * @see #parseLong(String, int)
	 * @see #decode(String)
	 */
	private static long parseLong(String str, int radix, boolean decode) {
		if (!decode && str == null)
			throw new NumberFormatException();
		int index = 0;
		int len = str.length();
		boolean isNeg = false;
		if (len == 0)
			throw new NumberFormatException();
		int ch = str.charAt(index);
		if (ch == '-') {
			if (len == 1)
				throw new NumberFormatException();
			isNeg = true;
			ch = str.charAt(++index);
		}
		if (decode) {
			if (ch == '0') {
				if (++index == len)
					return 0;
				if ((str.charAt(index) & ~('x' ^ 'X')) == 'X') {
					radix = 16;
					index++;
				} else
					radix = 8;
			} else if (ch == '#') {
				radix = 16;
				index++;
			}
		}
		if (index == len)
			throw new NumberFormatException();

		long max = MAX_VALUE / radix;
		// We can't directly write `max = (MAX_VALUE + 1) / radix'.
		// So instead we fake it.
		if (isNeg && MAX_VALUE % radix == radix - 1)
			++max;

		long val = 0;
		while (index < len) {
			if (val < 0 || val > max)
				throw new NumberFormatException();

			ch = Character.digit(str.charAt(index++), radix);
			val = val * radix + ch;
			if (ch < 0 || (val < 0 && (!isNeg || val != MIN_VALUE)))
				throw new NumberFormatException();
		}
		return isNeg ? -val : val;
	}

  private static String toUnsignedString(long num, int exp)
  {
    // Use an array large enough for a binary number.
    int mask = (1 << exp) - 1;
    char[] buffer = new char[64];
    int i = 64;
    do
      {
		  buffer[--i] = digits[(int)num & mask];
        num >>>= exp;
      }
    while (num != 0);

    // Package constructor avoids an array copy.
    return new String(buffer, i, 64 - i);
  }


  public static String toHexString(long i)
  {
    return toUnsignedString(i, 4);
  }
}
