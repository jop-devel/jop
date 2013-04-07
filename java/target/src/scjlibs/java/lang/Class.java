/* Class.java -- Representation of a Java class.
 Copyright (C) 1998, 1999, 2000, 2002, 2003, 2004, 2005, 2006
 Free Software Foundation

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

import com.jopdesign.sys.GC;
import com.jopdesign.sys.Native;

public final class Class<T> {

	// The memory address of the class structure
	int classRefAddress;

	boolean is_array = false;
	boolean is_interface = false;
	boolean is_primitive = false;

	protected Class() {

	}

	public static Class forName(String name) // throws ClassNotFoundException
	{
		throw new Error("NYI");
	}

	/**
	 * Returns the {@code Class} representing the component type of an array. If
	 * this class does not represent an array class this method returns null.
	 * 
	 * @return the {@code Class} representing the component type of this class
	 *         if this class is an array
	 */
	public Class getComponentType() {

		if(!is_array){
			return null;
		}
//		else{
//			clazz = new Class();
//			clazz.classRefAddress = classRefAddress;
//		}
		
		return null;
	}

	/**
	 * 
	 * Determines if this {@code Class} object represents an array class.
	 * 
	 * @return {@code true} if this object represents an array class;
	 *         {@code false} otherwise.
	 */
	public boolean isArray() {
		
		return is_array;
	}

	/**
	 * Determines if the specified {@code Object} is assignment-compatible with
	 * the object represented by this {@code Class}. This method is the dynamic
	 * equivalent of the Java language {@code instanceof} operator. The method
	 * returns {@code true} if the specified {@code Object} argument is non-null
	 * and can be cast to the reference type represented by this {@code Class}
	 * object without raising a {@code ClassCastException.} It returns
	 * {@code false} otherwise.
	 * 
	 * <p>
	 * Specifically, if this {@code Class} object represents a declared class,
	 * this method returns {@code true} if the specified {@code Object} argument
	 * is an instance of the represented class (or of any of its subclasses); it
	 * returns {@code false} otherwise. If this {@code Class} object represents
	 * an array class, this method returns {@code true} if the specified
	 * {@code Object} argument can be converted to an object of the array class
	 * by an identity conversion or by a widening reference conversion; it
	 * returns {@code false} otherwise. If this {@code Class} object represents
	 * an interface, this method returns {@code true} if the class or any
	 * superclass of the specified {@code Object} argument implements this
	 * interface; it returns {@code false} otherwise. If this {@code Class}
	 * object represents a primitive type, this method returns {@code false}.
	 * 
	 * @param obj
	 *            the object to check
	 * @return true if {@code obj} is an instance of this class
	 * 
	 * @since JDK1.1
	 */
	public boolean isInstance(Object value) {
		throw new Error("NYI");
	}

	/**
	 * Determines if the specified {@code Class} object represents an interface
	 * type.
	 * 
	 * @return {@code true} if this object represents an interface;
	 *         {@code false} otherwise.
	 */
	public boolean isInterface() {
		return false;
	}

	/**
	 * Determines if the specified {@code Class} object represents a primitive
	 * type.
	 * 
	 * <p>
	 * There are nine predefined {@code Class} objects to represent the eight
	 * primitive types and void. These are created by the Java Virtual Machine,
	 * and have the same names as the primitive types that they represent,
	 * namely {@code boolean}, {@code byte}, {@code char}, {@code short},
	 * {@code int}, {@code long}, {@code float}, and {@code double}.
	 * 
	 * <p>
	 * These objects may only be accessed via the following public static final
	 * variables, and are the only {@code Class} objects for which this method
	 * returns {@code true}.
	 * 
	 * @return true if and only if this class represents a primitive type
	 * 
	 * @see java.lang.Boolean#TYPE
	 * @see java.lang.Character#TYPE
	 * @see java.lang.Byte#TYPE
	 * @see java.lang.Short#TYPE
	 * @see java.lang.Integer#TYPE
	 * @see java.lang.Long#TYPE
	 * @see java.lang.Float#TYPE
	 * @see java.lang.Double#TYPE
	 * @see java.lang.Void#TYPE
	 * @since JDK1.1
	 */
	public boolean isPrimitive() {
		return false;
	}

	/**
	 * Creates a new instance of the class represented by this {@code Class}
	 * object. The class is instantiated as if by a {@code new} expression with
	 * an empty argument list. The class is initialized if it has not already
	 * been initialized.
	 * 
	 * <p>
	 * Note that this method propagates any exception thrown by the nullary
	 * constructor, including a checked exception. Use of this method
	 * effectively bypasses the compile-time exception checking that would
	 * otherwise be performed by the compiler. The
	 * {@link java.lang.reflect.Constructor#newInstance(java.lang.Object...)
	 * Constructor.newInstance} method avoids this problem by wrapping any
	 * exception thrown by the constructor in a (checked)
	 * {@link java.lang.reflect.InvocationTargetException}.
	 * 
	 * @return a newly allocated instance of the class represented by this
	 *         object.
	 * @exception IllegalAccessException
	 *                if the class or its nullary constructor is not accessible.
	 * @exception InstantiationException
	 *                if this {@code Class} represents an abstract class, an
	 *                interface, an array class, a primitive type, or void; or
	 *                if the class has no nullary constructor; or if the
	 *                instantiation fails for some other reason.
	 * @exception ExceptionInInitializerError
	 *                if the initialization provoked by this method fails.
	 * @exception SecurityException
	 *                If a security manager, <i>s</i>, is present and any of the
	 *                following conditions is met:
	 * 
	 *                <ul>
	 * 
	 *                <li> invocation of
	 *                {@link SecurityManager#checkMemberAccess
	 *                s.checkMemberAccess(this, Member.PUBLIC)} denies creation
	 *                of new instances of this class
	 * 
	 *                <li> the caller's class loader is not the same as or an
	 *                ancestor of the class loader for the current class and
	 *                invocation of {@link SecurityManager#checkPackageAccess
	 *                s.checkPackageAccess()} denies access to the package of
	 *                this class
	 * 
	 *                </ul>
	 * 
	 */
	public Object newInstance() {
		return Native.toObject(GC.newObject(classRefAddress));
	}

}
