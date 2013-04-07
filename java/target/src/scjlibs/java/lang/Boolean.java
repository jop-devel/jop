/* Boolean.java -- object wrapper for boolean
 Copyright (C) 1998, 2001, 2002, 2005, 2006  Free Software Foundation, Inc.

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

import java.io.Serializable;

/**
 * Instances of class <code>Boolean</code> represent primitive
 * <code>boolean</code> values.
 * 
 * @author Paul Fisher
 * @author Eric Blake (ebb9@email.byu.edu)
 * @since 1.0
 * @status updated to 1.5
 */
public final class Boolean {
	/**
	 * Compatible with JDK 1.0.2+.
	 */
	private static final long serialVersionUID = -3665804199014368530L;

	/**
	 * This field is a <code>Boolean</code> object representing the primitive
	 * value <code>true</code>. This instance is returned by the static
	 * <code>valueOf()</code> methods if they return a <code>Boolean</code>
	 * representing <code>true</code>.
	 */
	public static final Boolean TRUE = new Boolean(true);

	/**
	 * This field is a <code>Boolean</code> object representing the primitive
	 * value <code>false</code>. This instance is returned by the static
	 * <code>valueOf()</code> methods if they return a <code>Boolean</code>
	 * representing <code>false</code>.
	 */
	public static final Boolean FALSE = new Boolean(false);

	/**
	 * The primitive type <code>boolean</code> is represented by this
	 * <code>Class</code> object.
	 * 
	 * @since 1.1
	 */
	// public static final Class TYPE = VMClassLoader.getPrimitiveClass('Z');
	/**
	 * The immutable value of this Boolean.
	 * 
	 * @serial the wrapped value
	 */
	private final boolean value;

	/**
	 * Create a <code>Boolean</code> object representing the value of the
	 * argument <code>value</code>. In general the use of the static method
	 * <code>valueof(boolean)</code> is more efficient since it will not
	 * create a new object.
	 * 
	 * @param value
	 *            the primitive value of this <code>Boolean</code>
	 * @see #valueOf(boolean)
	 */
	public Boolean(boolean value) {
		this.value = value;
	}

	/**
	 * Creates a <code>Boolean</code> object representing the primitive
	 * <code>true</code> if and only if <code>s</code> matches the string
	 * "true" ignoring case, otherwise the object will represent the primitive
	 * <code>false</code>. In general the use of the static method
	 * <code>valueof(String)</code> is more efficient since it will not create
	 * a new object.
	 * 
	 * @param s
	 *            the <code>String</code> representation of <code>true</code>
	 *            or false
	 */
	public Boolean(String s) {
		// TODO this is a workaround to equalsIgnoreCase()
		value = ("true".equals(s) || "TRUE".equals(s) || "True".equals(s));
	}

	/**
	 * Return the primitive <code>boolean</code> value of this
	 * <code>Boolean</code> object.
	 * 
	 * @return true or false, depending on the value of this Boolean
	 */
	public boolean booleanValue() {
		return value;
	}

	/**
	 * Returns the Boolean <code>TRUE</code> if the given boolean is
	 * <code>true</code>, otherwise it will return the Boolean
	 * <code>FALSE</code>.
	 * 
	 * @param b
	 *            the boolean to wrap
	 * @return the wrapper object
	 * @see #TRUE
	 * @see #FALSE
	 * @since 1.4
	 */
	public static Boolean valueOf(boolean b) {
		return b ? TRUE : FALSE;
	}

	/**
	 * Returns "true" if the value of this object is <code>true</code> and
	 * returns "false" if the value of this object is <code>false</code>.
	 * 
	 * @return the string representation of this
	 */
	public String toString() {
		return value ? "true" : "false";
	}
	
	  public static String toString(boolean b)
	  {
	    return b ? "true" : "false";
	  }

	/**
	 * Returns the integer <code>1231</code> if this object represents the
	 * primitive <code>true</code> and the integer <code>1237</code>
	 * otherwise.
	 * 
	 * @return the hash code
	 */
	public int hashCode() {
		return value ? 1231 : 1237;
	}

	/**
	 * If the <code>obj</code> is an instance of <code>Boolean</code> and
	 * has the same primitive value as this object then <code>true</code> is
	 * returned. In all other cases, including if the <code>obj</code> is
	 * <code>null</code>, <code>false</code> is returned.
	 * 
	 * @param obj
	 *            possibly an instance of any <code>Class</code>
	 * @return true if <code>obj</code> equals this
	 */
	public boolean equals(Object obj) {
		// TODO: instanceof...
		return value == ((Boolean) obj).value;
	}

}
