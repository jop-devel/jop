/* java.lang.Math -- common mathematical functions, native allowed (VMMath)
   Copyright (C) 1998, 2001, 2002, 2003, 2006 Free Software Foundation, Inc.

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

//import gnu.classpath.Configuration;

//import java.util.Random;

import com.jopdesign.sys.Const;
import com.jopdesign.sys.Cordic;
import com.jopdesign.sys.Native;
import com.jopdesign.sys.SoftFloat32;
import com.jopdesign.sys.SoftFloat64;
import com.jopdesign.sys.MathSupport;

/**
 * Helper class containing useful mathematical functions and constants.
 * <P>
 * 
 * Note that angles are specified in radians. Conversion functions are provided
 * for your convenience.
 * 
 * @author Paul Fisher
 * @author John Keiser
 * @author Eric Blake (ebb9@email.byu.edu)
 * @author Andrew John Hughes (gnu_andrew@member.fsf.org)
 * @since 1.0
 */
public final class Math {

	// FIXME - This is here because we need to load the "javalang" system
	// library somewhere late in the bootstrap cycle. We cannot do this
	// from VMSystem or VMRuntime since those are used to actually load
	// the library. This is mainly here because historically Math was
	// late enough in the bootstrap cycle to start using System after it
	// was initialized (called from the java.util classes).

	/**
	 * Math is non-instantiable
	 */
	private Math() {
	}

	/**
	 * A random number generator, initialized on first use.
	 */
	// private static Random rand;

	/**
	 * The most accurate approximation to the mathematical constant <em>e</em>:
	 * <code>2.718281828459045</code>. Used in natural log and exp.
	 * 
	 * @see #log(double)
	 * @see #exp(double)
	 */
	public static final double E = 2.718281828459045;

	/**
	 * The most accurate approximation to the mathematical constant <em>pi</em>:
	 * <code>3.141592653589793</code>. This is the ratio of a circle's diameter
	 * to its circumference.
	 */
	public static final double PI = 3.141592653589793;

	/**
	 * Take the absolute value of the argument. (Absolute value means make it
	 * positive.)
	 * <P>
	 * 
	 * Note that the the largest negative value (Integer.MIN_VALUE) cannot be
	 * made positive. In this case, because of the rules of negation in a
	 * computer, MIN_VALUE is what will be returned. This is a <em>negative</em>
	 * value. You have been warned.
	 * 
	 * @param i
	 *            the number to take the absolute value of
	 * @return the absolute value
	 * @see Integer#MIN_VALUE
	 */
	public static int abs(int i) {
		return (i < 0) ? -i : i;
	}

	/**
	 * Take the absolute value of the argument. (Absolute value means make it
	 * positive.)
	 * <P>
	 * 
	 * Note that the the largest negative value (Long.MIN_VALUE) cannot be made
	 * positive. In this case, because of the rules of negation in a computer,
	 * MIN_VALUE is what will be returned. This is a <em>negative</em> value.
	 * You have been warned.
	 * 
	 * @param l
	 *            the number to take the absolute value of
	 * @return the absolute value
	 * @see Long#MIN_VALUE
	 */
	public static long abs(long l) {
		return (l < 0) ? -l : l;
	}

	/**
	 * Take the absolute value of the argument. (Absolute value means make it
	 * positive.)
	 * <P>
	 * 
	 * This is equivalent, but faster than, calling
	 * <code>Float.intBitsToFloat(0x7fffffff & Float.floatToIntBits(a))</code>.
	 * 
	 * @param f
	 *            the number to take the absolute value of
	 * @return the absolute value
	 */
	public static float abs(float f) {
		return (f <= 0) ? 0 - f : f;
	}

	/**
	 * Take the absolute value of the argument. (Absolute value means make it
	 * positive.)
	 * 
	 * This is equivalent, but faster than, calling
	 * <code>Double.longBitsToDouble(Double.doubleToLongBits(a)
	 *       &lt;&lt; 1) &gt;&gt;&gt; 1);</code>.
	 * 
	 * @param d
	 *            the number to take the absolute value of
	 * @return the absolute value
	 */
	public static double abs(double d) {
		return (d <= 0) ? 0 - d : d;
	}

	/**
	 * Return whichever argument is smaller.
	 * 
	 * @param a
	 *            the first number
	 * @param b
	 *            a second number
	 * @return the smaller of the two numbers
	 */
	public static int min(int a, int b) {
		return (a < b) ? a : b;
	}

	/**
	 * Return whichever argument is smaller.
	 * 
	 * @param a
	 *            the first number
	 * @param b
	 *            a second number
	 * @return the smaller of the two numbers
	 */
	public static long min(long a, long b) {
		return (a < b) ? a : b;
	}

	/**
	 * Return whichever argument is smaller. If either argument is NaN, the
	 * result is NaN, and when comparing 0 and -0, -0 is always smaller.
	 * 
	 * @param a
	 *            the first number
	 * @param b
	 *            a second number
	 * @return the smaller of the two numbers
	 */
	public static float min(float a, float b) {
		// this check for NaN, from JLS 15.21.1, saves a method call
		if (a != a)
			return a;
		// no need to check if b is NaN; < will work correctly
		// recall that -0.0 == 0.0, but [+-]0.0 - [+-]0.0 behaves special
		if (a == 0 && b == 0)
			return -(-a - b);
		return (a < b) ? a : b;
	}

	/**
	 * Return whichever argument is smaller. If either argument is NaN, the
	 * result is NaN, and when comparing 0 and -0, -0 is always smaller.
	 * 
	 * @param a
	 *            the first number
	 * @param b
	 *            a second number
	 * @return the smaller of the two numbers
	 */
	public static double min(double a, double b) {
		// this check for NaN, from JLS 15.21.1, saves a method call
		if (a != a)
			return a;
		// no need to check if b is NaN; < will work correctly
		// recall that -0.0 == 0.0, but [+-]0.0 - [+-]0.0 behaves special
		if (a == 0 && b == 0)
			return -(-a - b);
		return (a < b) ? a : b;
	}

	/**
	 * Return whichever argument is larger.
	 * 
	 * @param a
	 *            the first number
	 * @param b
	 *            a second number
	 * @return the larger of the two numbers
	 */
	public static int max(int a, int b) {
		return (a > b) ? a : b;
	}

	/**
	 * Return whichever argument is larger.
	 * 
	 * @param a
	 *            the first number
	 * @param b
	 *            a second number
	 * @return the larger of the two numbers
	 */
	public static long max(long a, long b) {
		return (a > b) ? a : b;
	}

	/**
	 * Return whichever argument is larger. If either argument is NaN, the
	 * result is NaN, and when comparing 0 and -0, 0 is always larger.
	 * 
	 * @param a
	 *            the first number
	 * @param b
	 *            a second number
	 * @return the larger of the two numbers
	 */
	public static float max(float a, float b) {
		// this check for NaN, from JLS 15.21.1, saves a method call
		if (a != a)
			return a;
		// no need to check if b is NaN; > will work correctly
		// recall that -0.0 == 0.0, but [+-]0.0 - [+-]0.0 behaves special
		if (a == 0 && b == 0)
			return a - -b;
		return (a > b) ? a : b;
	}

	/**
	 * Return whichever argument is larger. If either argument is NaN, the
	 * result is NaN, and when comparing 0 and -0, 0 is always larger.
	 * 
	 * @param a
	 *            the first number
	 * @param b
	 *            a second number
	 * @return the larger of the two numbers
	 */
	public static double max(double a, double b) {
		// this check for NaN, from JLS 15.21.1, saves a method call
		if (a != a)
			return a;
		// no need to check if b is NaN; > will work correctly
		// recall that -0.0 == 0.0, but [+-]0.0 - [+-]0.0 behaves special
		if (a == 0 && b == 0)
			return a - -b;
		return (a > b) ? a : b;
	}
	
	  /**
	   * The trigonometric function <em>sin</em>. The sine of NaN or infinity is
	   * NaN, and the sine of 0 retains its sign. This is accurate within 1 ulp,
	   * and is semi-monotonic.
	   *
	   * @param a the angle (in radians)
	   * @return sin(a)
	   */
	  public static double sin(double a)
	  {
	    return Cordic.sin(a);
	  }

	  /**
	   * The trigonometric function <em>cos</em>. The cosine of NaN or infinity is
	   * NaN. This is accurate within 1 ulp, and is semi-monotonic.
	   *
	   * @param a the angle (in radians)
	   * @return cos(a)
	   */
	  public static double cos(double a)
	  {
	    return Cordic.cos(a);
	  }
	  
	  public static double tan(double a)
	  {
		  return sin(a) / cos(a);
	  }
	  
	  public static double atan(double a)
	  {
		  return Double.longBitsToDouble(
				  MathSupport.atan(Double.doubleToLongBits(a)));
	  }
	  
	  public static double log(double a) {
		  return Double.longBitsToDouble(
				  MathSupport.log(Double.doubleToLongBits(a)));
	  }

	  /**
	   * Quick hack of atan2
	   * 
	   * Missing handling NaN, Inf, and probably much more
	   * @param y
	   * @param x
	   * @return
	   */
	  public static double atan2(double y, double x) {

		  double val;
//		  double ax, ay, val;
//		  // TODO: what happens on x, y == 0?
//		  ax = x < 0 ? -x : x;
//		  ay = y < 0 ? -y : y;
//		  // TODO: check for small numbers
		  val = atan(y/x);
		  if (x > 0) {
			  return val;
		  }
		  if (y < 0) {
			  return val - PI;
		  }
		  return val + PI;
	  }


	/**
	 * Take a square root. If the argument is NaN or negative, the result is
	 * NaN; if the argument is positive infinity, the result is positive
	 * infinity; and if the result is either zero, the result is the same. This
	 * is accurate within the limits of doubles.
	 * 
	 * <p>
	 * For a cube root, use <code>cbrt</code>. For other roots, use
	 * <code>pow(a, 1 / rootNumber)</code>.
	 * </p>
	 * 
	 * @param a
	 *            the numeric argument
	 * @return the square root of the argument
	 * @see #cbrt(double)
	 * @see #pow(double, double)
	 */
	public static double sqrt(double a) {
		if (Const.SUPPORT_DOUBLE) {
			return Native.toDouble(SoftFloat64.sqrt(Native.toLong(a)));
		} else {
			throw new RuntimeException("Not implemented");
		}
	}

	  /**
	   * Convert from degrees to radians. The formula for this is
	   * radians = degrees * (pi/180); however it is not always exact given the
	   * limitations of floating point numbers.
	   *
	   * @param degrees an angle in degrees
	   * @return the angle in radians
	   * @since 1.2
	   */
	  public static double toRadians(double degrees)
	  {
	    return (degrees * PI) / 180;
	  }

	/**
	 * Return the closest integer value to the argument. If the argument is NaN,
	 * the result is 0; if the argument's value does not fit an int,
	 * Integer.MIN_VALUE or Integer.MAX_VALUE are returned, depending on the
	 * sign.
	 * 
	 * @param a
	 *            the float to convert
	 * @return the closest int to the argument
	 */
	public static int round(float a) {
		if (Const.SUPPORT_FLOAT) {
			return SoftFloat32.float_round(Float.floatToIntBits(a));
		} else {
			throw new RuntimeException("Not implemented");
		}
	}

	public static long round(double a) {
		if (Const.SUPPORT_DOUBLE) {
			return SoftFloat64.double_round(Double.doubleToLongBits(a));
		} else {
			throw new RuntimeException("Not implemented");
		}
	}

	public static int floor(double d) {
		if (Const.SUPPORT_DOUBLE) {
			return (int) d;
		} else {
			throw new RuntimeException("Not implemented");
		}
	}

	public static int floor(float f) {
		if (Const.SUPPORT_FLOAT) {
			return (int) f;
		} else {
			throw new RuntimeException("Not implemented");
		}
	}

	public static long ceil(float f) {
		if (Const.SUPPORT_FLOAT) {
			return (int) f;
		} else {
			throw new RuntimeException("Not implemented");
		}
	}

}
