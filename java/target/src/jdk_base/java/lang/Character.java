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

	public static char toUpperCase(char ch) {
		int intch = (int) ch;
		if (97 <= intch && intch <= 122)
			return (char) (intch - 32);
		if (65 <= intch && intch <= 90)
			return ch;
		return ch;
	}

} // class Character
