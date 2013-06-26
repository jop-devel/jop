/* Short.java -- object wrapper for short
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
 * Instances of class <code>Short</code> represent primitive
 * <code>short</code> values.
 * 
 * Additionally, this class provides various helper functions and variables
 * related to shorts.
 * 
 * @author Paul Fisher
 * @author John Keiser
 * @author Eric Blake (ebb9@email.byu.edu)
 * @since 1.1
 * @status updated to 1.4
 */
public final class Short {
	/**
	 * Compatible with JDK 1.1+.
	 */
	private static final long serialVersionUID = 7515723908773894738L;

	/**
	 * The minimum value a <code>short</code> can represent is -32768 (or -2<sup>15</sup>).
	 */
	public static final short MIN_VALUE = -32768;

	/**
	 * The minimum value a <code>short</code> can represent is 32767 (or 2<sup>15</sup>).
	 */
	public static final short MAX_VALUE = 32767;

	/**
	 * The primitive type <code>short</code> is represented by this
	 * <code>Class</code> object.
	 */
	// public static final Class TYPE = VMClassLoader.getPrimitiveClass('S');
	/**
	 * The number of bits needed to represent a <code>short</code>.
	 * 
	 * @since 1.5
	 */
	public static final int SIZE = 16;

	// This caches some Short values, and is used by boxing conversions
	// via valueOf(). We must cache at least -128..127; these constants
	// control how much we actually cache.

	/**
	 * The immutable value of this Short.
	 * 
	 * @serial the wrapped short
	 */
	private final short value;

	public Short(short value) {
		this.value = value;
	}

	public Short(String s) {
		value = parseShort(s, 10);
	}

	public boolean equals(Object obj) {

		// TODO: instance of not implemented
		// return obj instanceof Short && value == ((Short) obj).value;
		return value == ((Short) obj).value;

	}

	public int hashCode() {
		return value;
	}

	public static short parseShort(String s) {
		return parseShort(s, 10);
	}

	public static short parseShort(String s, int radix) {
		int i = Integer.parseInt(s, radix);
		if ((short) i != i)
			throw new NumberFormatException();
		return (short) i;
	}

	/**
	 * Return the value of this <code>Short</code>.
	 * 
	 * @return the short value
	 */
	public short shortValue() {
		return value;
	}

	/**
	 * Converts the <code>Short</code> value to a <code>String</code> and
	 * assumes a radix of 10.
	 * 
	 * @return the <code>String</code> representation of this
	 *         <code>Short</code>
	 */
	public String toString() {
		return Integer.toString((int)value,10);
		// return String.valueOf(value);
	}

	  /**
	   * Converts the <code>short</code> to a <code>String</code> and assumes
	   * a radix of 10.
	   *
	   * @param s the <code>short</code> to convert to <code>String</code>
	   * @return the <code>String</code> representation of the argument
	   */
	  public static String toString(short s)
	  {
	    return String.valueOf(s);
	  }
	  
	  /**
	   * Returns a <code>Short</code> object wrapping the value.
	   * In contrast to the <code>Short</code> constructor, this method
	   * will cache some values.  It is used by boxing conversion.
	   *
	   * @param val the value to wrap
	   * @return the <code>Short</code>
	   * @since 1.5
	   */
	  public static Short valueOf(short val)
	  {
	      return new Short(val);
	  }
}
