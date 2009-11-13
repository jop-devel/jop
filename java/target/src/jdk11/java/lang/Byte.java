/* Byte.java -- object wrapper for byte
 Copyright (C) 1998, 2001, 2002, 2005  Free Software Foundation, Inc.

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
 * Instances of class <code>Byte</code> represent primitive <code>byte</code>
 * values.
 * 
 * Additionally, this class provides various helper functions and variables
 * useful to bytes.
 * 
 * @author Paul Fisher
 * @author John Keiser
 * @author Per Bothner
 * @author Eric Blake (ebb9@email.byu.edu)
 * @since 1.1
 * @status updated to 1.5
 */
public final class Byte  {

	/**
	 * The minimum value a <code>byte</code> can represent is -128 (or -2<sup>7</sup>).
	 */
	public static final byte MIN_VALUE = -128;

	/**
	 * The maximum value a <code>byte</code> can represent is 127 (or 2<sup>7</sup> -
	 * 1).
	 */
	public static final byte MAX_VALUE = 127;

	/**
	 * The number of bits needed to represent a <code>byte</code>.
	 * 
	 * @since 1.5
	 */
	public static final int SIZE = 8;

	
	/**
	 * The immutable value of this Byte.
	 * 
	 * @serial the wrapped byte
	 */
	private final byte value;

	/**
	 * Create a <code>Byte</code> object representing the value of the
	 * <code>byte</code> argument.
	 * 
	 * @param value
	 *            the value to use
	 */
	public Byte(byte value) {
		this.value = value;
	}

	/**
	 * Return the value of this <code>Byte</code>.
	 * 
	 * @return the byte value
	 */
	public byte byteValue() {
		return value;
	}

	/**
	 * Returns <code>true</code> if <code>obj</code> is an instance of
	 * <code>Byte</code> and represents the same byte value.
	 * 
	 * @param obj
	 *            the object to compare
	 * @return whether these Objects are semantically equal
	 */
	public boolean equals(Object obj) {
		//TODO: instanceof not implemented yet
		//return obj instanceof Byte && value == ((Byte) obj).value;
		return value == ((Byte) obj).value;
	}

	/**
	 * Return a hashcode representing this Object. <code>Byte</code>'s hash
	 * code is simply its value.
	 * 
	 * @return this Object's hash code
	 */
	public int hashCode() {
		return value;
	}

	/**
	 * Converts the specified <code>String</code> into a <code>byte</code>.
	 * This function assumes a radix of 10.
	 * 
	 * @param s
	 *            the <code>String</code> to convert
	 * @return the <code>byte</code> value of <code>s</code>
	 * @throws NumberFormatException
	 *             if <code>s</code> cannot be parsed as a <code>byte</code>
	 * @see #parseByte(String)
	 */
	public static byte parseByte(String s) {
		return parseByte(s, 10);
	}

	/**
	 * Converts the specified <code>String</code> into an <code>int</code>
	 * using the specified radix (base). The string must not be
	 * <code>null</code> or empty. It may begin with an optional '-', which
	 * will negate the answer, provided that there are also valid digits. Each
	 * digit is parsed as if by <code>Character.digit(d, radix)</code>, and
	 * must be in the range <code>0</code> to <code>radix - 1</code>.
	 * Finally, the result must be within <code>MIN_VALUE</code> to
	 * <code>MAX_VALUE</code>, inclusive. Unlike Double.parseDouble, you may
	 * not have a leading '+'.
	 * 
	 * @param s
	 *            the <code>String</code> to convert
	 * @param radix
	 *            the radix (base) to use in the conversion
	 * @return the <code>String</code> argument converted to <code>byte</code>
	 * @throws NumberFormatException
	 *             if <code>s</code> cannot be parsed as a <code>byte</code>
	 */
	public static byte parseByte(String s, int radix) {
		int i = Integer.parseInt(s, radix);
		if ((byte) i != i)
			throw new NumberFormatException();
		return (byte) i;
	}

	/**
	 * Converts the <code>Byte</code> value to a <code>String</code> and
	 * assumes a radix of 10.
	 * 
	 * @return the <code>String</code> representation of this
	 *         <code>Byte</code>
	 * @see Integer#toString()
	 */
	public String toString() {
		return String.valueOf(value);
	}

	   public static String toString(byte b)
	   {
	     return String.valueOf(b);
	   }
}
