// $Id: MicroDouble.java,v 1.3 2009/09/19 20:18:32 dave Exp $
/*
 * Double.java
 * Copyright (C) 2003, 2004 David Clausen
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * Portions of this software are derived from FDLIBM, which contained the
 * following notice:
 *
 * ====================================================
 * Copyright (C) 1993 by Sun Microsystems, Inc. All rights reserved.
 *
 * Developed at SunSoft, a Sun Microsystems, Inc. business.
 * Permission to use, copy, modify, and distribute this
 * software is freely granted, provided that this notice 
 * is preserved.
 * ====================================================
 *
 * For mor information on FDLIBM see:
 * http://netlib.bell-labs.com/netlib/fdlibm/index.html
 *
 */
package net.dclausen.microfloat;

import java.util.Random;

/**
 * A software implementation of IEEE-754 double precision math which does not
 * rely on the <code>double</code> data type. 
 * This class overloads the <code>long</code> data type by storing 
 * <code>double</code> data in it.
 * See the 
 * <a href="package-summary.html#package_description">package description</a> 
 * for more information.
 * <p>
 * @author David Clausen
 * @see <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Double.html">Double</a>
 * @see <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Math.html">Math</a>
 * @see Float
 * @version $Revision: 1.3 $
 */
public class MicroDouble {
  
  /////////////////////////////////////////////////////////////////////////////
  // General-purpose constants
  /////////////////////////////////////////////////////////////////////////////

  /**
   * A constant holding the same value as <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Double.html#POSITIVE_INFINITY">Double.POSITIVE_INFINITY</a>
   */
  public  static final long POSITIVE_INFINITY = 0x7ff0000000000000L;
  
  /**
   * A constant holding the same value as <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Double.html#NEGATIVE_INFINITY">Double.NEGATIVE_INFINITY</a>
   */
  public  static final long NEGATIVE_INFINITY = 0xfff0000000000000L;

  /**
   * A constant holding the same value as <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Double.html#NaN">Double.NaN</a>
   */
  public  static final long NaN               = 0x7ff8000000000000L;
  
  /**
   * A constant holding the same value as <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Double.html#MAX_VALUE">Double.MAX_VALUE</a>
   */
  public  static final long MAX_VALUE         = 0x7fefffffffffffffL;
  
  /**
   * A constant holding the same value as <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Double.html#MIN_VALUE">Double.MIN_VALUE</a>
   */
  public  static final long MIN_VALUE         = 0x0000000000000001L; 
  
  /**
   * A constant holding the same value as <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Math.html#E">Math.E</a>
   */
  public  static final long E                 = 0x4005bf0a8b145769L;
  
  /**
   * A constant holding the same value as <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Math.html#PI">Math.PI</a>
   */
  public  static final long PI                = 0x400921fb54442d18L;

  // Other constants needed internally, and exposed as a convenience.

  /** A constant holding the value of 0.0d */
  public static final long ZERO              = 0x0000000000000000L;

  /** A constant holding the value of -0.0d */
  public static final long NEGATIVE_ZERO     = 0x8000000000000000L;
  
  /** A constant holding the value of 1.0d */
  public static final long ONE               = 0x3ff0000000000000L;
  
  /** A constant holding the value of -1.0d */
  public static final long NEGATIVE_ONE      = 0xbff0000000000000L;
  
  /** A constant holding the value of 2.0d */
  public static final long TWO               = 0x4000000000000000L;
  
  /** A constant holding the value of 3.0d */
  public static final long THREE             = 0x4008000000000000L;
  
  /** A constant holding the value of 4.0d */
  public static final long FOUR              = 0x4010000000000000L;
  
  /** A constant holding the value of 5.0d */
  public static final long FIVE              = 0x4014000000000000L;
  
  /** A constant holding the value of 6.0d */
  public static final long SIX               = 0x4018000000000000L;
  
  /** A constant holding the value of 8.0d */
  public static final long EIGHT             = 0x4020000000000000L;
  
  /** A constant holding the value of 10.0d */
  public static final long TEN               = 0x4024000000000000L;
  
  /** A constant holding the value of 100.0d */
  public static final long ONE_HUNDRED       = 0x4059000000000000L;
  
  /** A constant holding the value of 1.5d */
  public static final long THREE_HALVES      = 0x3ff8000000000000L;
  
  /** A constant holding the value of 0.5d */
  public static final long ONE_HALF          = 0x3fe0000000000000L;
  
  /** A constant holding the value of (1.0d / 3.0d) */
  public static final long ONE_THIRD         = 0x3fd5555555555555L;
  
  /** A constant holding the value of 0.25d */
  public static final long ONE_FOURTH        = 0x3fd0000000000000L;
  
  /** A constant holding the value of 0.125d */
  public static final long ONE_EIGHTH        = 0x3fc0000000000000L;
  
  /** A constant holding the natural logarithm of 2 */
  public static final long LN2               = 0x3fe62e42fefa39efL;

  
  /////////////////////////////////////////////////////////////////////////////
  // Packing and unpacking the IEEE-754 double precision format
  /////////////////////////////////////////////////////////////////////////////

  private static final long ABS_MASK          = 0x7fffffffffffffffL;
  private static final long SIGN_MASK         = 0x8000000000000000L; // 1 bit
  private static final long EXPONENT_MASK     = 0x7ff0000000000000L; // 11 bits
  private static final long FRACTION_MASK     = 0x000fffffffffffffL; // 52 bits
  private static final long IMPLIED_ONE       = 0x0010000000000000L; // 53rd bit

  /** @return true iff d is negative */
  static boolean unpackSign(long d) {
    return (d < 0L);
  }

  /** @return an integer in the range [-1075, 972] */
  static int unpackExponent(long d) {
    return (((int) (d >> 52)) & 0x7ff) - 1075;
  }

  /** @return a long in the range [0, 0x001fffffffffffffL] */
  static long unpackMantissa(long d) {
    if ((d & EXPONENT_MASK) == 0) {
      return ((d & FRACTION_MASK) << 1);
    } else {
      return ((d & FRACTION_MASK) | IMPLIED_ONE);
    }
  }

  /** 
   * @return the double which most closely represents the given base-2 mantissa
   *         and exponent
   */
  static long pack(boolean negative, int exponent, long mantissa) {
    // reduce precision of mantissa, rounding if necessary
    if (mantissa != 0) {
      // left align mantissa
      int shift = BitUtils.countLeadingZeros(mantissa);
      mantissa <<= shift;
      exponent -= shift;

      if (exponent < -1085) {
        // subnormal
        mantissa = BitUtils.roundingRightShift(mantissa, -1074 - exponent);
      } else {
        // normal
        mantissa = BitUtils.roundingRightShift(mantissa, 11);
        if (mantissa == 0x20000000000000L) {
          // oops, rounding carried into the 54th bit
          mantissa = 0x10000000000000L;
          exponent++;
        }
        // pack the exponent
        if (exponent > 960) {
          mantissa = POSITIVE_INFINITY;
        } else {
          mantissa ^= IMPLIED_ONE;
          mantissa |= ((long) (exponent + 1086)) << 52;
        }
      }
    }
    
    // pack the sign bit
    if (negative) {
      mantissa |= SIGN_MASK;
    }
    
    return mantissa;
  }

  
  /////////////////////////////////////////////////////////////////////////////
  // Simple tests 
  /////////////////////////////////////////////////////////////////////////////

  /**
   * Mimics <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Double.html#isNaN(double)">Double.isNaN(double)</a>
   */
  public static boolean isNaN(long d) {
    return ((d & ABS_MASK) > POSITIVE_INFINITY);
  }

  /**
   * Mimics <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Double.html#isInfinite(double)">Double.isInfinite(double)</a>
   */
  public static boolean isInfinite(long d) {
    return ((d & ABS_MASK) == POSITIVE_INFINITY);
  }
  
  /**
   * Returns <code>true</code> if the specified number has zero
   * magnitude, <code>false</code> otherwise.
   *
   * @param   d   the <code>double</code> value to be tested.
   * @return  <code>true</code> if the value of the argument is positive
   *          zero or negative zero; <code>false</code> otherwise.
   */
  public static boolean isZero(long d) {
    return ((d & ABS_MASK) == ZERO);
  }

  
  /////////////////////////////////////////////////////////////////////////////
  // Sign changes
  /////////////////////////////////////////////////////////////////////////////

  /**
   * Mimics <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Math.html#abs(double)">Math.abs(double)</a>
   */
  public static long abs(long d) {
    //if (isNaN(d)) {
    //  return NaN;
    //}
    return (d & ABS_MASK);
  }

  /**
   * Returns the negation of a <code>double</code> value.
   * Special cases:
   * <ul>
   * <li>If the argument is negative zero, the result is positive zero.
   * <li>If the argument is positive zero, the result is negative zero.
   * <li>If the argument is negative infinity, the result is positive infinity.
   * <li>If the argument is positive infinity, the result is negative infinity.
   * <li>If the argument is NaN, the result is NaN.</ul>
   * <p>
   * This method takes the place of the unary <code>-</code> operator.
   *
   * @param   d   the <code>double</code> value whose negated value is to be 
   *              determined
   * @return  the negation of the argument.
   */
  public static long negate(long d) {
    if (isNaN(d)) {
      return NaN;
    }
    return (d ^ SIGN_MASK);
  }
  

  /////////////////////////////////////////////////////////////////////////////
  // Comparison
  /////////////////////////////////////////////////////////////////////////////

  /**
   * Returns <code>true</code> if the specified numbers are considered equal
   * according to 
   * <a href="http://java.sun.com/docs/books/jls/second_edition/html/expressions.doc.html#5198">section 15.21.1
   * of the JLS</a>.  Special cases:
   * <ul>
   * <li>If either operand is NaN, then the result is false
   * <li>Positive zero and negative zero are considered equal
   * </ul>
   * <p>
   * This method takes the place of the <code>==</code> operator.
   *
   * @param   d1   the first <code>double</code> value to be compared.
   * @param   d2   the second <code>double</code> value to be compared.
   * @return  <code>true</code> if the two values are considered equal;
   *          <code>false</code> otherwise.
   */
  public static boolean eq(long d1, long d2) {
    return (((d1 == d2) && (! isNaN(d1))) || (isZero(d1) && isZero(d2)));
  }

  /**
   * Returns <code>true</code> if the specified numbers are considered unequal
   * according to 
   * <a href="http://java.sun.com/docs/books/jls/second_edition/html/expressions.doc.html#5198">section
   * 15.21.1 of the JLS</a>.  Special cases:
   * <ul>
   * <li>If either operand is NaN, then the result is true
   * <li>Positive zero and negative zero are considered equal
   * </ul>
   * The value returned by <code>ne</code> is always the opposite of the value
   * returned by <code>eq</code> for the same arguments.
   * <p>
   * This method takes the place of the <code>!=</code> operator.
   *
   * @param   d1   the first <code>double</code> value to be compared.
   * @param   d2   the second <code>double</code> value to be compared.
   * @return  <code>true</code> if the two values are considered equal;
   *          <code>false</code> otherwise.
   */
  public static boolean ne(long d1, long d2) {
    return (! eq(d1, d2));
  }

  /**
   * Returns <code>true</code> if the first argument is considered less than
   * the second argument according to 
   * <a href="http://java.sun.com/docs/books/jls/second_edition/html/expressions.doc.html#153654">section
   * 15.20.1 of the JLS</a>.  Special cases:
   * <ul>
   * <li>If either operand is NaN, then the result is false
   * <li>Positive zero and negative zero are considered equal
   * <li>Negative infinity is conisdered less than all other values except NaN
   * <li>Positive infinity is conisdered greater than all other values except NaN
   * </ul>
   * <p>
   * This method takes the place of the <code>&lt;</code> operator.
   *
   * @param   d1   the first <code>double</code> value to be compared.
   * @param   d2   the second <code>double</code> value to be compared.
   * @return  <code>true</code> if the first value is less than the second value;
   *          <code>false</code> otherwise.
   */
  public static boolean lt(long d1, long d2) {
    if (isNaN(d1) || isNaN(d2)) {
      return false;
    } else if (d2 == ZERO) {
      d2 = NEGATIVE_ZERO;
    }
    return (cmp(d1, d2) < 0);
  }
  
  /**
   * Returns <code>true</code> if the first argument is considered less than
   * or equal to the second argument according to 
   * <a href="http://java.sun.com/docs/books/jls/second_edition/html/expressions.doc.html#153654">section
   * 15.20.1 of the JLS</a>.  Special cases:
   * <ul>
   * <li>If either operand is NaN, then the result is false
   * <li>Positive zero and negative zero are considered equal
   * <li>Negative infinity is conisdered less than all other values except NaN
   * <li>Positive infinity is conisdered greater than all other values except NaN
   * </ul>
   * <p>
   * This method takes the place of the <code>&lt;=</code> operator.
   *
   * @param   d1   the first <code>double</code> value to be compared.
   * @param   d2   the second <code>double</code> value to be compared.
   * @return  <code>true</code> if the first value is less than or equal to 
   *          the second value; <code>false</code> otherwise.
   */
  public static boolean le(long d1, long d2) {
    if (isNaN(d1) || isNaN(d2)) {
      return false;
    } else if (d2 == NEGATIVE_ZERO) {
      d2 = ZERO;
    }
    return (cmp(d1, d2) <= 0);
  }

  /**
   * Returns <code>true</code> if the first argument is considered greater than
   * the second argument according to 
   * <a href="http://java.sun.com/docs/books/jls/second_edition/html/expressions.doc.html#153654">section
   * 15.20.1 of the JLS</a>.  Special cases:
   * <ul>
   * <li>If either operand is NaN, then the result is false
   * <li>Positive zero and negative zero are considered equal
   * <li>Negative infinity is conisdered less than all other values except NaN
   * <li>Positive infinity is conisdered greater than all other values except NaN
   * </ul>
   * <p>
   * This method takes the place of the <code>&gt;</code> operator.
   *
   * @param   d1   the first <code>double</code> value to be compared.
   * @param   d2   the second <code>double</code> value to be compared.
   * @return  <code>true</code> if the first value is greater than the second value;
   *          <code>false</code> otherwise.
   */
  public static boolean gt(long d1, long d2) {
    if (isNaN(d1) || isNaN(d2)) {
      return false;
    } else if (d1 == ZERO) {
      d1 = NEGATIVE_ZERO;
    }
    return (cmp(d1, d2) > 0);
  }
  
  /**
   * Returns <code>true</code> if the first argument is considered greater than
   * or equal to the second argument according to 
   * <a href="http://java.sun.com/docs/books/jls/second_edition/html/expressions.doc.html#153654">section
   * 15.20.1 of the JLS</a>.  Special cases:
   * <ul>
   * <li>If either operand is NaN, then the result is false
   * <li>Positive zero and negative zero are considered equal
   * <li>Negative infinity is conisdered less than all other values except NaN
   * <li>Positive infinity is conisdered greater than all other values except NaN
   * </ul>
   * <p>
   * This method takes the place of the <code>&gt;=</code> operator.
   *
   * @param   d1   the first <code>double</code> value to be compared.
   * @param   d2   the second <code>double</code> value to be compared.
   * @return  <code>true</code> if the first value is greater than or equal to 
   *          the second value; <code>false</code> otherwise.
   */
  public static boolean ge(long d1, long d2) {
    if (isNaN(d1) || isNaN(d2)) {
      return false;
    } else if (d1 == NEGATIVE_ZERO) {
      d1 = ZERO;
    }
    return (cmp(d1, d2) >= 0);
  }

  /**
   * Mimics <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Double.html#compare(double, double)">Double.compare(double, double)</a>.
   * <p>
   * Note that when using this method (as well as <code>Double.compare</code>),
   * the following rules apply:
   * <ul><li>
   *		<code>NaN</code> is considered 
   *		to be equal to itself and greater than all other
   *		<code>double</code> values (including
   *		<code>POSITIVE_INFINITY</code>).
   * <li>
   *		<code>0.0</code> is considered to be greater
   *		than <code>-0.0</code>.
   * </ul>
   */
  public static int compare(long d1, long d2) {
    boolean n1 = isNaN(d1);
    boolean n2 = isNaN(d2);
    if (n1 || n2) {
      if (n1 && n2) {
        return 0;
      }
      return (n1 ? 1 : -1);
    }
    return cmp(d1, d2);
  }

  /**
   * Mimics <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Math.html#max(double, double)">Math.max(double, double)</a>
   */
  public static long max(long d1, long d2) {
    if (isNaN(d1) || isNaN(d2)) {
      return NaN;
    }
    return ((cmp(d1, d2) >= 0) ? d1 : d2);
  }

  /**
   * Mimics <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Math.html#min(double, double)">Math.min(double, double)</a>
   */
  public static long min(long d1, long d2) {
    if (isNaN(d1) || isNaN(d2)) {
      return NaN;
    }
    return ((cmp(d1, d2) < 0) ? d1 : d2);
  }

  private static int cmp(long d1, long d2) {
    if (d1 == d2) {
      return 0;
    } else if (d1 < 0L) {
      if (d2 < 0L) {
        return ((d1 < d2) ? 1 : -1);
      }
      return -1;
    } else if (d2 < 0) {
      return 1;
    }
    return ((d1 < d2) ? -1 : 1);
  }
  
  /////////////////////////////////////////////////////////////////////////////
  // Type conversion
  /////////////////////////////////////////////////////////////////////////////
  
  /** 
   * Convert the given <code>int</code> to a <code>double</code> as would happen
   * in a casting operation specified by 
   * <a href="http://java.sun.com/docs/books/jls/second_edition/html/conversions.doc.html#25214">section
   * 5.1.2 of the JLS</a>.  This is a widening primitive conversion which 
   * will result in neither a loss of magnitude nor precision.
   *
   * @param x the <code>int</code> to be converted
   * @return the <code>double</code> representation of the argument
   */
  public static long intToDouble(int x) {
    return longToDouble(x);
  }
  
  /** 
   * Convert the given <code>long</code> to a <code>double</code> as would happen
   * in a casting operation specified by 
   * <a href="http://java.sun.com/docs/books/jls/second_edition/html/conversions.doc.html#25214">section
   * 5.1.2 of the JLS</a>.  This is a widening primitive conversion which 
   * will not result in a loss of magnitude, but might result in a loss of
   * precision.
   *
   * @param x the <code>long</code> to be converted
   * @return the <code>double</code> representation of the argument
   */
  public static long longToDouble(long x) {
    if (x < 0) {
      return pack(true, 0, -x);
    }
    return pack(false, 0, x);
  }

  /** 
   * Convert the given <code>float</code> to a <code>double</code> as would happen
   * in a casting operation specified by 
   * <a href="http://java.sun.com/docs/books/jls/second_edition/html/conversions.doc.html#25214">section
   * 5.1.2 of the JLS</a>.  This is a widening primitive conversion which 
   * will result in neither a loss of magnitude nor precision.
   *
   * @param f the <code>float</code> to be converted
   * @return the <code>double</code> representation of the argument
   */
  public static long floatToDouble(int f) {
    if (MicroFloat.isNaN(f)) {
      return NaN;
    }
    boolean n = MicroFloat.unpackSign(f);
    if (MicroFloat.isZero(f)) {
      return (n ? NEGATIVE_ZERO : ZERO);
    } else if (MicroFloat.isInfinite(f)) {
      return (n ? NEGATIVE_INFINITY : POSITIVE_INFINITY);
    }
    int x = MicroFloat.unpackExponent(f);
    long m = MicroFloat.unpackMantissa(f);
    return pack(n, x, m);
  }
  
  /** 
   * Convert the given <code>double</code> to a <code>byte</code> as would happen
   * in a casting operation specified by 
   * <a href="http://java.sun.com/docs/books/jls/second_edition/html/conversions.doc.html#25363">section
   * 5.1.3 of the JLS</a>.  This is a narrowing primitive conversion which 
   * may result in a loss of magnitude and/or precision.
   * <p>
   * Note that this is a non-intuitive conversion.  If the argument is outside
   * of the range of the byte type, the result is basically meaningless.
   *
   * @param d the <code>double</code> to be converted
   * @return the <code>byte</code> representation of the argument
   */
  public static byte byteValue(long d) {
    return (byte) intValue(d);
  }

  /** 
   * Convert the given <code>double</code> to a <code>short</code> as would happen
   * in a casting operation specified by 
   * <a href="http://java.sun.com/docs/books/jls/second_edition/html/conversions.doc.html#25363">section
   * 5.1.3 of the JLS</a>.  This is a narrowing primitive conversion which 
   * may result in a loss of magnitude and/or precision.
   * <p>
   * Note that this is a non-intuitive conversion.  If the argument is outside
   * of the range of the short type, the result is basically meaningless.
   *
   * @param d the <code>double</code> to be converted
   * @return the <code>short</code> representation of the argument
   */
  public static short shortValue(long d) {
    return (short) intValue(d);
  }

  /** 
   * Convert the given <code>double</code> to an <code>int</code> as would happen
   * in a casting operation specified by 
   * <a href="http://java.sun.com/docs/books/jls/second_edition/html/conversions.doc.html#25363">section
   * 5.1.3 of the JLS</a>.  This is a narrowing primitive conversion which 
   * may result in a loss of magnitude and/or precision.
   *
   * @param d the <code>double</code> to be converted
   * @return the <code>int</code> representation of the argument
   */
  public static int intValue(long d) {
    long x = longValue(d);
    if (x >= Integer.MAX_VALUE) {
      return Integer.MAX_VALUE;
    } else if (x <= Integer.MIN_VALUE) {
      return Integer.MIN_VALUE;
    }
    return (int) x;
  }

  /** 
   * Convert the given <code>double</code> to a <code>long</code> as would happen
   * in a casting operation specified by 
   * <a href="http://java.sun.com/docs/books/jls/second_edition/html/conversions.doc.html#25363">section
   * 5.1.3 of the JLS</a>.  This is a narrowing primitive conversion which 
   * may result in a loss of magnitude and/or precision.
   *
   * @param d the <code>double</code> to be converted
   * @return the <code>long</code> representation of the argument
   */
  public static long longValue(long d) {
    if (isNaN(d)) {
      return 0;
    }
    boolean n = unpackSign(d);
    int x = unpackExponent(d);
    long m = unpackMantissa(d);
    if (x > 0) {
      if ((x >= 63) || ((m >> (63 - x)) != 0))  {
        return (n ? Long.MIN_VALUE : Long.MAX_VALUE);
      }
      m <<= x;
    } else if (x <= -53) {
      return 0;
    } else {
      m >>>= -x;
    }
    return (n ? -m : m);
  }

  /** 
   * Convert the given <code>double</code> to a <code>float</code> as would happen
   * in a casting operation specified by 
   * <a href="http://java.sun.com/docs/books/jls/second_edition/html/conversions.doc.html#25363">section
   * 5.1.3 of the JLS</a>.  This is a narrowing primitive conversion which 
   * may result in a loss of magnitude and/or precision.
   *
   * @param d the <code>double</code> to be converted
   * @return the <code>float</code> representation of the argument
   */
  public static int floatValue(long d) {
    return MicroFloat.doubleToFloat(d);
  }
  
  /////////////////////////////////////////////////////////////////////////////
  // Random number generation
  /////////////////////////////////////////////////////////////////////////////

  private static Random random;

  private static synchronized Random getRandom() {
    if (random == null) {
      random = new java.util.Random();
    }
    return random;
  }
  
  /**
   * Mimics <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Math.html#random()">Math.random()</a>
   */
  public static long random() {
    return pack(false, -64, getRandom().nextLong() << 11);
  }

  
  /////////////////////////////////////////////////////////////////////////////
  // Basic arithmetic
  /////////////////////////////////////////////////////////////////////////////

  /**
   * Returns the sum of the two <code>double</code> arguments according to 
   * <a href="http://java.sun.com/docs/books/jls/second_edition/html/expressions.doc.html#13510">section
   * 15.18.2 of the JLS</a>.
   * <p>
   * This method takes the place of the <code>+</code> operator.
   *
   * @param   d1   the first <code>double</code> value to be summed.
   * @param   d2   the second <code>double</code> value to be summed.
   * @return  the sum of the two arguments
   */
  public static long add(long d1, long d2) {
    if (isNaN(d1) || isNaN(d2)) {
      return NaN;
    }

    boolean n1 = unpackSign(d1);
    boolean n2 = unpackSign(d2);
    
    // special handling of infinity
    boolean i1 = isInfinite(d1);
    boolean i2 = isInfinite(d2);
    if (i1 || i2) {
      if (i1 && i2) {
        if (n1 != n2) {
          // infinites of opposite sign -> NaN
          return NaN;
        } else {
          // infinites of same sign -> infinity the same sign
          return d1;
        }
      } else if (i1) {
        return d1; // infinite + finite = infinite
      } else {
        return d2; // finite + infinite = infinite
      }
    }
    
    // special handling of zero
    boolean z1 = isZero(d1);
    boolean z2 = isZero(d2);
    if (z1 || z2) {
      if (z1 && z2) {
        if (n1 != n2) {
          // zeros of opposite sign -> positive zero
          return ZERO;
        } else {
          return d1; // zeros of same sign -> zero of the same sign
        }
      } else if (z1) {
        return d2; // zero + nonzero = nonzero
      } else {
        return d1; // nonzero + zero = nonzero
      }
    }
    
    // unpack, and add 3 guard digits
    long m1 = unpackMantissa(d1) << 3;
    int x1 = unpackExponent(d1) - 3;
    long m2 = unpackMantissa(d2) << 3;
    int x2 = unpackExponent(d2) - 3;
    
    // make exponents equal
    int dx = x1 - x2;
    if (dx > 0) {
      m2 = BitUtils.stickyRightShift(m2, dx);
      x2 = x1;
    } else if (dx < 0) {
      m1 = BitUtils.stickyRightShift(m1, -dx);
      x1 = x2;
    }

    // if the signs are different, negate the smaller mantissa and choose
    // the sign of the larger
    if (n1 ^ n2) { 
      if (m1 > m2) {
        m2 = -m2;
      } else {
        m1 = -m1;
        n1 = n2;
      }
    }
    
    // add (or subtract) mantissas
    m1 += m2;

    // pack result, and handle special case of zero (which always returns +0.0) 
    long d = pack(n1, x1, m1);
    if (d == NEGATIVE_ZERO) {
      return ZERO;
    }
    return d;
  }
  
  /**
   * Returns the difference of the two <code>double</code> arguments according to 
   * <a href="http://java.sun.com/docs/books/jls/second_edition/html/expressions.doc.html#13510">section
   * 15.18.2 of the JLS</a>.
   * <p>
   * This method takes the place of the binary <code>-</code> operator.
   *
   * @param   d1   the first <code>double</code> value 
   * @param   d2   the second <code>double</code> value
   * @return  the difference of the two arguments
   */
  public static long sub(long d1, long d2) {
    return add(d1, negate(d2));
  }

  /**
   * Returns the product of the two <code>double</code> arguments according to 
   * <a href="http://java.sun.com/docs/books/jls/second_edition/html/expressions.doc.html#5036">section
   * 15.17.1 of the JLS</a>.
   * <p>
   * This method takes the place of the <code>*</code> operator.
   *
   * @param   d1   the first <code>double</code> value
   * @param   d2   the second <code>double</code> value
   * @return  the product of the two arguments
   */
  public static long mul(long d1, long d2) {
    if (isNaN(d1) || isNaN(d2)) {
      return NaN;
    }

    boolean negative = unpackSign(d1) ^ unpackSign(d2);
    
    // special handling of infinity
    if (isInfinite(d1) || isInfinite(d2)) {
      if (isZero(d1) || isZero(d2)) {
        return NaN;
      } else {
        return (negative ? NEGATIVE_INFINITY : POSITIVE_INFINITY);
      }
    }
    
    // unpack
    long m1 = unpackMantissa(d1);
    int x1 = unpackExponent(d1);
    long m2 = unpackMantissa(d2);
    int x2 = unpackExponent(d2);
    
    // compute the resultant exponent
    x1 += x2;
    
    // compute the resultant mantissa using double-precision integer 
    // multiplication with 28 bit words
    long m11 = m1 & 0x0fffffff;
    long m12 = m1 >> 28;
    long m21 = m2 & 0x0fffffff;
    long m22 = m2 >> 28;
    
    long t1 = m11 * m21;
    long t2 = (m11 * m22) + (m12 * m21);
    long t3 = m12 * m22;
    
    t1 += (t2 & 0x0fffffff) << 28;
    t3 += t2 >>> 28;
    t3 += t1 >>> 56;
    t1 <<= 8;
    // the 128 bit result is now in t3t1

    // shift the result left into t3 and discard excess precision
    int s = BitUtils.countLeadingZeros(t3);
    x1 += 56 - s;
    if (s == 64) {
      t3 = t1;
    } else {
      t3 <<= s;
      t3 |= t1 >>> (64 - s);
      if ((t1 << s) != 0) {
        // discarded low bits go into the sticky bit
        t3 |= 1;
      }
    }
    
    // round and pack the result
    return pack(negative, x1, t3);
  }
  
  /**
   * Returns the quotient of the two <code>double</code> arguments according to 
   * <a href="http://java.sun.com/docs/books/jls/second_edition/html/expressions.doc.html#5047">section
   * 15.17.2 of the JLS</a>.
   * <p>
   * This method takes the place of the <code>/</code> operator.
   *
   * @param   d1   the <code>double</code> dividend 
   * @param   d2   the <code>double</code> divisor
   * @return  the quotient of the two arguments
   */
  public static long div(long d1, long d2) {
    if (isNaN(d1) || isNaN(d2)) {
      return NaN;
    }

    boolean negative = unpackSign(d1) ^ unpackSign(d2);
    
    // special handling of infinity
    boolean n1 = isInfinite(d1);
    boolean n2 = isInfinite(d2);
    if (n1 || n2) {
      if (n1 && n2) {
        return NaN;
      } else if (n1) {
        return (negative ? NEGATIVE_INFINITY : POSITIVE_INFINITY);
      } else {
        return (negative ? NEGATIVE_ZERO : ZERO);
      }
    }
    // neither value is infinite
    
    // special handling of zero
    n1 = isZero(d1);
    n2 = isZero(d2);
    if (n1 || n2) {
      if (n1 && n2) {
        return NaN;
      } else if (n1) {
        return (negative ? NEGATIVE_ZERO : ZERO);
      } else {
        return (negative ? NEGATIVE_INFINITY : POSITIVE_INFINITY);
      }
    }
    // neither value is zero
    
    // unpack
    long m1 = unpackMantissa(d1);
    int x1 = unpackExponent(d1);
    long m2 = unpackMantissa(d2);
    int x2 = unpackExponent(d2);

    // shift, divide, mod, repeat
    long m = 0;
    x1 -= x2;
    while (true) {
      int s = Math.min(BitUtils.countLeadingZeros(m1) - 1, 
              BitUtils.countLeadingZeros(m));
      if (s <= 8) {
        if (m1 != 0) {
          m |= 1;
        }
        break;
      }
      m1 <<= s;
      m <<= s;
      x1 -= s;
      m |= m1 / m2;
      m1 %= m2;
    }
    return pack(negative, x1, m);
  }
  
  /**
   * Returns the remainder of the two <code>double</code> arguments according to 
   * <a href="http://java.sun.com/docs/books/jls/second_edition/html/expressions.doc.html#24956">section
   * 15.17.3 of the JLS</a>.
   * <p>
   * This method takes the place of the <code>%</code> operator.
   *
   * @param   d1   the <code>double</code> dividend 
   * @param   d2   the <code>double</code> divisor
   * @return  the remainder of the two arguments
   * @see #IEEEremainder(long, long)
   */
  public static long mod(long d1, long d2) {
    if (isNaN(d1) || isNaN(d2) || isInfinite(d1) || isZero(d2)) {
      return NaN;
    } else if (isZero(d1) || isInfinite(d2)) {
      return d1;
    }
    
    // unpack
    int x1 = unpackExponent(d1);
    int x2 = unpackExponent(d2);
    if (x1 < x2) {
      return d1;
    }
    boolean n = unpackSign(d1);
    long m1 = unpackMantissa(d1);
    long m2 = unpackMantissa(d2);
    if (x1 == x2) {
      m1 %= m2;
    } else {
      // reduce m1 by left shifting and modding until the exponents x1 and x2 are 
      // equal
      while (x1 != x2) {
        int s = Math.min(BitUtils.countLeadingZeros(m1) - 1, x1 - x2);
        x1 -= s;
        m1 = (m1 << s) % m2;
      }
    }
    return pack(n, x1, m1);
  }

  
  /////////////////////////////////////////////////////////////////////////////
  // Rounding
  /////////////////////////////////////////////////////////////////////////////

  
  /**
   * Returns the <code>double</code> of greatest magnitude (furthest from zero)
   * that is equal to a mathematical integer and which has a mignitude not
   * greater than the argument's magnitude.  Special cases:
   * <ul><li>If the argument value is already equal to a mathematical 
   * integer, then the result is the same as the argument. 
   * <li>If the argument is NaN or an infinity or positive zero or 
   * negative zero, then the result is the same as the argument.</ul>
   *
   * @param   d   a <code>double</code> value.
   * @return the <code>double</code> of greatest magnitude (furthest from zero)
   *         whose magnitude is not greater than the argument's and which 
   *         is equal to a mathematical integer.
   */
  public static long truncate(long d) {
    return round(d, false, unpackSign(d));
  }

  /**
   * Mimics <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Math.html#rint(double)">Math.rint(double)</a>.
   */
  public static long rint(long d) {
    return round(d, true, false);
  }
  
  /**
   * Mimics <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Math.html#floor(double)">Math.floor(double)</a>.
   */
  public static long floor(long d) {
    return round(d, false, false);
  }
  
  /**
   * Mimcs <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Math.html#ceil(double)">Math.ceil(double)</a>.
   */
  public static long ceil(long d) {
    return round(d, false, true);
  }

  /**
   * Mimcs <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Math.html#round(double)">Math.round(double)</a>.
   */
  public static long round(long d) {
    return longValue(floor(add(d, ONE_HALF)));
  }

  private static long round(long d, boolean round, boolean ceil) {
    if (isNaN(d)) {
      return NaN;
    } else if (isZero(d) || isInfinite(d)) {
      return d;
    }
    int x = unpackExponent(d);
    if (x >= 0) {
      return d;
    }
    boolean n = unpackSign(d);
    long m = unpackMantissa(d);
    if (round) {
      m = BitUtils.roundingRightShift(m, -x);
    } else {
      long r;
      if (x <= -64) {
        r = m;
        m = 0;
      } else {
        r = m << (64 + x);
        m >>>= -x;
      }
      if ((n ^ ceil) && (r != 0)) {
        m++;
      }
    }
    return pack(n, 0, m);
  }

  
  /////////////////////////////////////////////////////////////////////////////
  // String Conversion
  /////////////////////////////////////////////////////////////////////////////
  
  // decimal -> binary 
  
  // base 2 mantissas for 10**-345 through 10**309, at intervals of 1000
  private static final long[] pow10m = {
          0xf4b0769e47eb5a79L, 0xeef453d6923bd65aL, 0xe95a99df8ace6f54L, 
          0xe3e27a444d8d98b8L, 0xde8b2b66b3bc4724L, 0xd953e8624b85dd79L, 
          0xd43bf0effdc0ba48L, 0xcf42894a5dce35eaL, 0xca66fa129f9b60a7L, 
          0xc5a890362fddbc63L, 0xc1069cd4eabe89f9L, 0xbc807527ed3e12bdL, 
          0xb8157268fdae9e4cL, 0xb3c4f1ba87bc8697L, 0xaf8e5410288e1b6fL, 
          0xab70fe17c79ac6caL, 0xa76c582338ed2622L, 0xa37fce126597973dL, 
          0x9faacf3df73609b1L, 0x9becce62836ac577L, 0x9845418c345644d7L, 
          0x94b3a202eb1c3f39L, 0x91376c36d99995beL, 0x8dd01fad907ffc3cL, 
          0x8a7d3eef7f1cfc52L, 0x873e4f75e2224e68L, 0x8412d9991ed58092L, 
          0x80fa687f881c7f8eL, 0xfbe9141915d7a922L, 0xf6019da07f549b2bL, 
          0xf03d93eebc589f88L, 0xea9c227723ee8bcbL, 0xe51c79a85916f485L, 
          0xdfbdcece67006ac9L, 0xda7f5bf590966849L, 0xd5605fcdcf32e1d7L, 
          0xd0601d8efc57b08cL, 0xcb7ddcdda26da269L, 0xc6b8e9b0709f109aL, 
          0xc21094364dfb5637L, 0xbd8430bd08277231L, 0xb913179899f68584L, 
          0xb4bca50b065abe63L, 0xb080392cc4349dedL, 0xac5d37d5b79b6239L, 
          0xa8530886b54dbdecL, 0xa46116538d0deb78L, 0xa086cfcd97bf97f4L, 
          0x9cc3a6eec6311a64L, 0x991711052d8bf3c5L, 0x9580869f0e7aac0fL, 
          0x91ff83775423cc06L, 0x8e938662882af53eL, 0x8b3c113c38f9f37fL, 
          0x87f8a8d4cfa417caL, 0x84c8d4dfd2c63f3bL, 0x81ac1fe293d599c0L, 
          0xfd442e4688bd304bL, 0xf7549530e188c129L, 0xf18899b1bc3f8ca2L, 
          0xebdf661791d60f56L, 0xe65829b3046b0afaL, 0xe0f218b8d25088b8L, 
          0xdbac6c247d62a584L, 0xd686619ba27255a3L, 0xd17f3b51fca3a7a1L, 
          0xcc963fee10b7d1b3L, 0xc7caba6e7c5382c9L, 0xc31bfa0fe5698db8L, 
          0xbe89523386091466L, 0xba121a4650e4ddecL, 0xb5b5ada8aaff80b8L, 
          0xb1736b96b6fd83b4L, 0xad4ab7112eb3929eL, 0xa93af6c6c79b5d2eL, 
          0xa54394fe1eedb8ffL, 0xa163ff802a3426a9L, 0x9d9ba7832936edc1L, 
          0x99ea0196163fa42eL, 0x964e858c91ba2655L, 0x92c8ae6b464fc96fL, 
          0x8f57fa54c2a9eab7L, 0x8bfbea76c619ef36L, 0x88b402f7fd75539bL, 
          0x857fcae62d8493a5L, 0x825ecc24c8737830L, 0xfea126b7d78186bdL, 
          0xf8a95fcf88747d94L, 0xf2d56790ab41c2a3L, 0xed246723473e3813L, 
          0xe7958cb87392c2c3L, 0xe2280b6c20dd5232L, 0xdcdb1b2798182245L, 
          0xd7adf884aa879177L, 0xd29fe4b18e88640fL, 0xcdb02555653131b6L, 
          0xc8de047564d20a8cL, 0xc428d05aa4751e4dL, 0xbf8fdb78849a5f97L, 
          0xbb127c53b17ec159L, 0xb6b00d69bb55c8d1L, 0xb267ed1940f1c61cL, 
          0xae397d8aa96c1b78L, 0xaa242499697392d3L, 0xa6274bbdd0fadd62L, 
          0xa2425ff75e14fc32L, 0x9e74d1b791e07e48L, 0x9abe14cd44753b53L, 
          0x971da05074da7befL, 0x9392ee8e921d5d07L, 0x901d7cf73ab0acd9L, 
          0x8cbccc096f5088ccL, 0x89705f4136b4a597L, 0x8637bd05af6c69b6L, 
          0x83126e978d4fdf3bL, 0x8000000000000000L, 0xfa00000000000000L, 
          0xf424000000000000L, 0xee6b280000000000L, 0xe8d4a51000000000L, 
          0xe35fa931a0000000L, 0xde0b6b3a76400000L, 0xd8d726b7177a8000L, 
          0xd3c21bcecceda100L, 0xcecb8f27f4200f3aL, 0xc9f2c9cd04674edfL, 
          0xc5371912364ce305L, 0xc097ce7bc90715b3L, 0xbc143fa4e250eb31L, 
          0xb7abc627050305aeL, 0xb35dbf821ae4f38cL, 0xaf298d050e4395d7L, 
          0xab0e93b6efee0054L, 0xa70c3c40a64e6c52L, 0xa321f2d7226895c8L, 
          0x9f4f2726179a2245L, 0x9b934c3b330c8577L, 0x97edd871cfda3a57L, 
          0x945e455f24fb1cf9L, 0x90e40fbeea1d3a4bL, 0x8d7eb76070a08aedL, 
          0x8a2dbf142dfcc7abL, 0x86f0ac99b4e8dafdL, 0x83c7088e1aab65dbL, 
          0x80b05e5ac60b6178L, 0xfb5878494ace3a5fL, 0xf5746577930d6501L, 
          0xefb3ab16c59b14a3L, 0xea1575143cf97227L, 0xe498f455c38b997aL, 
          0xdf3d5e9bc0f653e1L, 0xda01ee641a708deaL, 0xd4e5e2cdc1d1ea96L, 
          0xcfe87f7cef46ff17L, 0xcb090c8001ab551cL, 0xc646d63501a1511eL, 
          0xc1a12d2fc3978937L, 0xbd176620a501fc00L, 0xb8a8d9bbe123f018L, 
          0xb454e4a179dd1877L, 0xb01ae745b101e9e4L, 0xabfa45da0edbde69L, 
          0xa7f26836f282b733L, 0xa402b9c5a8d3a6e7L, 0xa02aa96b06deb0feL, 
          0x9c69a97284b578d8L, 0x98bf2f79d5993803L, 0x952ab45cfa97a0b3L, 
          0x91abb422ccb812efL, 0x8e41ade9fbebc27dL, 0x8aec23d680043beeL, 
          0x87aa9aff79042287L, 0x847c9b5d7c2e09b7L, 0x8161afb94b44f57dL, 
          0xfcb2cb35e702af78L, 0xf6c69a72a3989f5cL, 0xf0fdf2d3f3c30b9fL, 
          0xeb57ff22fc0c795aL, 0xe5d3ef282a242e82L, 0xe070f78d3927556bL, 
          0xdb2e51bfe9d0696aL, 0xd60b3bd56a5586f2L, 0xd106f86e69d785c8L, 
          0xcc20ce9bd35c78a5L, 0xc75809c42c684dd1L, 0xc2abf989935ddbfeL, 
          0xbe1bf1b059e9a8d6L, 0xb9a74a0637ce2ee1L, 0xb54d5e4a127f59c8L, 
          0xb10d8e1456105dadL, 0xace73cbfdc0bfb7bL, 0xa8d9d1535ce3b396L, 
          0xa4e4b66b68b65d61L, 0xa1075a24e4421731L, 0x9d412e0806e88aa6L, 
          0x9991a6f3d6bf1766L, 0x95f83d0a1fb69cd9L, 0x92746b9be2f8552cL, 
          0x8f05b1163ba6832dL, 0x8bab8eefb6409c1aL, 0x8865899617fb1871L, 
          0x8533285c936b35dfL, 0x8213f56a67f6b29cL, 0xfe0efb53d30dd4d8L, 
          0xf81aa16fdc1b81dbL, 0xf24a01a73cf2dcd0L, 0xec9c459d51852ba3L, 
          0xe7109bfba19c0c9dL, 0xe1a63853bbd26451L, 0xdc5c5301c56b75f7L, 
          0xd732290fbacaf134L, 0xd226fc195c6a2f8cL, 0xcd3a1230c43fb26fL, 
          0xc86ab5c39fa63441L, 0xc3b8358109e84f07L, 0xbf21e44003acdd2dL, 
          0xbaa718e68396cffeL, 0xb6472e511c81471eL, 0xb201833b35d63f73L, 
  };
  
  // base 2 exponents for 10**-345 through 10**309, at intervals of 1000
  private static final short[] pow10x = {
          -1146, -1136, -1126, -1116, -1106, -1096, -1086, -1076, 
          -1066, -1056, -1046, -1036, -1026, -1016, -1006, -996, 
          -986, -976, -966, -956, -946, -936, -926, -916, 
          -906, -896, -886, -876, -867, -857, -847, -837, 
          -827, -817, -807, -797, -787, -777, -767, -757, 
          -747, -737, -727, -717, -707, -697, -687, -677, 
          -667, -657, -647, -637, -627, -617, -607, -597, 
          -587, -578, -568, -558, -548, -538, -528, -518, 
          -508, -498, -488, -478, -468, -458, -448, -438, 
          -428, -418, -408, -398, -388, -378, -368, -358, 
          -348, -338, -328, -318, -308, -298, -289, -279, 
          -269, -259, -249, -239, -229, -219, -209, -199, 
          -189, -179, -169, -159, -149, -139, -129, -119, 
          -109, -99, -89, -79, -69, -59, -49, -39, 
          -29, -19, -9, 1, 10, 20, 30, 40, 
          50, 60, 70, 80, 90, 100, 110, 120, 
          130, 140, 150, 160, 170, 180, 190, 200, 
          210, 220, 230, 240, 250, 260, 270, 280, 
          290, 299, 309, 319, 329, 339, 349, 359, 
          369, 379, 389, 399, 409, 419, 429, 439, 
          449, 459, 469, 479, 489, 499, 509, 519, 
          529, 539, 549, 559, 569, 579, 588, 598, 
          608, 618, 628, 638, 648, 658, 668, 678, 
          688, 698, 708, 718, 728, 738, 748, 758, 
          768, 778, 788, 798, 808, 818, 828, 838, 
          848, 858, 868, 877, 887, 897, 907, 917, 
          927, 937, 947, 957, 967, 977, 987, 997, 
          1007, 1017, 1027, 
  };

  private static long decToDouble(boolean negative, int base10x, long base10m) {
    if (base10m == 0) {
      return (negative ? NEGATIVE_ZERO : ZERO);
    }
    // maximize base10m to ensure consistency between toString and parseDouble
    while ((base10m > 0) && (base10m <= 0x1999999999999999L)) { // (Long.MAX_VALUE / 5))) {
      base10m = (base10m << 3) + (base10m << 1);
      base10x--;
    }
    // base10x needs to be a multiple of 3, because the tables are
    // spaced at intervals of 1000 (not 10).
    base10x += 345;
    int mod = base10x % 3;
    base10x /= 3;
    if (base10x < 0) { // -345
      return (negative ? NEGATIVE_ZERO : ZERO);
    } else if (base10x > 218) { // 309
      return (negative ? NEGATIVE_INFINITY : POSITIVE_INFINITY);
    }
    int base2x = pow10x[base10x];
    int s = BitUtils.countLeadingZeros(base10m);
    base10m <<= s;
    base2x -= s;
    long base2m = dpMul(base10m, pow10m[base10x]);
    while (mod > 0) {
      if (base2m < 0) {
        base2m >>>= 1;
        base2x++;
      }
      base2m += base2m >>> 2;
      base2x += 3;
      mod--;
    }
    return pack(negative, base2x, base2m);
  }

  /**
   * Double-precision integer multiplication of x1 and x2.
   */
  private static final long dpMul(long x1, long x2) {
    long v1 = (x1 >>> 32)        * (x2 >>> 32);
    long v2 = (x1 & 0xffffffffL) * (x2 >>> 32);
    long v3 = (x1 >>> 32)        * (x2 & 0xffffffffL);
    v1 += v2 >>> 32;
    v1 += v3 >>> 32;
    if (((v2 + v3) << 32) < 0) {
      v1++;
    }
    return v1;
  }
  
  /**
   * Mimics <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Double.html#parseDouble(String)">Double.parseDouble(String)</a>.
   * <p>
   * See the notes on <code>toString</code> for some caveats on String 
   * conversion.
   *
   * @see #toString
   * @exception  NumberFormatException  if the string does not contain a
   *               parsable number.
   */
  public static long parseDouble(String s) {
    // remove leading & trailing whitespace
    s = s.trim().toUpperCase();
    
    // check length
    int len = s.length();
    if (len == 0) {
      throw new NumberFormatException(s);
    }
    
    // check for NaN
    if ("NAN".equals(s)) {
      return NaN;
    }
    
    // begin parsing, one character at a time
    int idx = 0;
    
    // read sign
    boolean negative = false;
    char c = s.charAt(0);
    negative = (c == '-');
    if (negative || (c == '+')) {
      idx = 1;
    }

    // check for "Infinity"
    if (idx < len) {
      c = s.charAt(idx);
      if ((c == 'I') || (c == 'i')) {
        if ("INFINITY".equals(s.substring(idx))) {
          return (negative ? NEGATIVE_INFINITY : POSITIVE_INFINITY);
        }
      }
    }

    // read Digits.Digits
    long mantissa = 0;
    int exponent = 0;
    int fractionChars = 0;
    boolean sticky = false;
    boolean readingFraction = false;
    while (idx < len) {
      c = s.charAt(idx);
      if (c == '.') {
        if (readingFraction) {
          throw new NumberFormatException(s);
        }
        readingFraction = true;
      } else if ((c < '0') || (c > '9')) {
        break;
      } else {
        fractionChars++;
        if (mantissa <= 0x1999999999999998L) { // ((Long.MAX_VALUE / 5) - 1)
          mantissa = (mantissa << 3) + (mantissa << 1) + (c - '0');
          if (readingFraction) {
            exponent--;
          }
        } else {
          if (! readingFraction) {
            exponent++;
          }
          sticky |= (c != '0');
        }
      }
      idx++;
    }
    if (fractionChars == 0) {
      throw new NumberFormatException(s);
    }
    
    // read exponent
    if (((idx + 1) < len) && ((s.charAt(idx) == 'E') || (s.charAt(idx) == 'e'))) {
      try {
        exponent += Integer.parseInt(s.substring(idx + 1));
      } catch (NumberFormatException e) {
        throw new NumberFormatException(s);
      }
      idx = len;
    } else if (idx != len) {
      // check that we parsed the entire string
      throw new NumberFormatException(s);
    }

    // convert the decimal to a float
    return decToDouble(negative, exponent, mantissa);
  }

  // binary -> decimal
  
  // base 10 mantissas for 2**-1075 through 2**972, at intervals of 2**11
  private static final long[] pow2m = {
          0x3f3d8b077b8e0b11L, 0x81842f29f2cce376L, 0x1a8662f3b3919708L, 
          0x3652b62c71ce021dL, 0x6f40f20501a5e7a8L, 0xe3d8f9e563a198e5L, 
          0x2ea9c639a0e5b3ffL, 0x5f90f22001d66e96L, 0xc3b8358109e84f07L, 
          0x2815578d865470daL, 0x52173a79e8197a93L, 0xa81f301449ee8c70L, 
          0x226e6cf846d8ca6fL, 0x4683f19a2ab1bf59L, 0x906a617d450187e2L, 
          0x1d9388b3aa30a574L, 0x3c928069cf3cb734L, 0x7c0d50b7ee0dc0edL, 
          0xfe0efb53d30dd4d8L, 0x3407fbc42995e10bL, 0x6a8f537d42bc2b19L, 
          0xda3c0f568cc4f3e9L, 0x2cb1c756f2a408feL, 0x5b88c3416ddb353cL, 
          0xbb764c4ca7a44410L, 0x266469bcf5afc5d9L, 0x4ea0970403744553L, 
          0xa1075a24e4421731L, 0x20fa8ae248247913L, 0x438a53baf1f4ae3cL, 
          0x8a5296ffe33cc930L, 0x1c5416bb92e3e607L, 0x3a044721f1706ea6L, 
          0x76d1770e38320986L, 0xf356f7ebf83552feL, 0x31d602710b1a1374L, 
          0x6610674de9ae3c53L, 0xd106f86e69d785c8L, 0x2acf0bf77baab497L, 
          0x57ac20b32a535d5eL, 0xb38d92d760ec4455L, 0x24c5bfdd7761f2f6L, 
          0x4b4f5be23c2cf3a2L, 0x9a3c2087a63f6399L, 0x1f965966bce055efL, 
          0x40b0d7dca5a27abfL, 0x847c9b5d7c2e09b7L, 0x1b221effe500d3b5L, 
          0x3791a7ef666817f9L, 0x71ce24bb2fefcecaL, 0xe912b9d1478ceb17L, 
          0x2fbbbed612bfe181L, 0x61c209e792f16b87L, 0xc83553c5c8965d3dL, 
          0x2900ae716a34e9baL, 0x53f9341b79415b99L, 0xabfa45da0edbde69L, 
          0x233894a789cd2ec7L, 0x4821f50d63f209c9L, 0x93ba47c980e98ce0L, 
          0x1e412f0f768fad71L, 0x3df622f090826959L, 0x7ee5a7d0010b1532L, 
          0x19fd0fef9de8dfe3L, 0x353978b370747aa6L, 0x6d00f7320d3846f5L, 
          0xdf3d5e9bc0f653e1L, 0x2db830ddf3e8b84cL, 0x5da22ed4e5309410L, 
          0xbfc2ef456ae276e9L, 0x2745d2cb73b0391fL, 0x506e3af8bbc71cebL, 
          0xa4b8cab1a1563f52L, 0x21bc2b266d3a36bfL, 0x4516df8a16fe63d6L, 
          0x8d7eb76070a08aedL, 0x1cfa698c95390ba9L, 0x3b58e88c75313ecaL, 
          0x798b138e3fe1c845L, 0xf8ebad2b84e0d58cL, 0x32fa9be33ac0aeceL, 
          0x6867a5a867f103b3L, 0xd5d238a4abe98068L, 0x2bca63414390e576L, 
          0x59aedfc10d7279c6L, 0xb7abc627050305aeL, 0x259da6542d43623dL, 
          0x4d0985cb1d3608aeL, 0x9dc5ada82b70b59eL, 0x204fce5e3e250261L, 
          0x422ca8b0a00a4250L, 0x878678326eac9000L, 0x1bc16d674ec80000L, 
          0x38d7ea4c68000000L, 0x746a528800000000L, 0xee6b280000000000L, 
          0x30d4000000000000L, 0x6400000000000000L, 0xcccccccccccccccdL, 
          0x29f16b11c6d1e109L, 0x55e63b88c230e77eL, 0xafebff0bcb24aaffL, 
          0x24075f3dceac2b36L, 0x49c97747490eae84L, 0x971da05074da7befL, 
          0x1ef2d0f5da7dd8aaL, 0x3f61ed7ca0c03283L, 0x81ceb32c4b43fcf5L, 
          0x1a95a5b7f87a0ef1L, 0x3671f73b54f1c895L, 0x6f80f42fc8971bd2L, 
          0xe45c10c42a2b3b06L, 0x2ec49f14ec5fb056L, 0x5fc7edbc424d2fcbL, 
          0xc428d05aa4751e4dL, 0x282c674aadc39bb6L, 0x524675555bad4716L, 
          0xa87fea27a539e9a5L, 0x22823c3e2fc3c55bL, 0x46ac8391ca4529b0L, 
          0x90bd77f3483bb9baL, 0x1da48ce468e7c702L, 0x3cb559e42ad070a9L, 
          0x7c54afe7c43a3ecaL, 0xfea126b7d78186bdL, 0x3425eb41e9c7c9adL, 
          0x6acca251be03a951L, 0xdab99e59958885c5L, 0x2ccb7e3a7cd51959L, 
          0x5bbd6d030bf1dde6L, 0xbbe226efb628afebL, 0x267a8065858fe90cL, 
          0x4ecdd3c1949b76e0L, 0xa163ff802a3426a9L, 0x210d8432d2fc5833L, 
          0x43b12f82b63e2546L, 0x8aa22c0dbef60ee4L, 0x1c6463225ab7ec1dL, 
          0x3a25a835f947855aL, 0x7715d36033c5acc0L, 0xf3e2f893dec3f126L, 
          0x31f2ae9b9f14e0b2L, 0x664b1ff7085be8daL, 0xd17f3b51fca3a7a1L, 
          0x2ae7ad1f207d4454L, 0x57de91a832277568L, 0xb3f4e093db73a093L, 
          0x24dae7f3aec97265L, 0x4b7ab0078ad3dbf3L, 0x9a94dd3e8cf578baL, 
          0x1fa885c8d117a609L, 0x40d60ff149eacce0L, 0x84c8d4dfd2c63f3bL, 
          0x1b31bb5dc320d18fL, 0x37b1a07e7d30c7ccL, 0x720f9eb539bbf765L, 
          0xe998d258869facd7L, 0x2fd735519e3bbc2eL, 0x61fa48553bdeb07eL, 
          0xc8a883c0fdaf7df0L, 0x29184594e3437adeL, 0x542984435aa6def6L, 
          0xac5d37d5b79b6239L, 0x234cd83c273db92fL, 0x484b75379c244c28L, 
          0x940f4613ae5ed137L, 0x1e5297287c2f4579L, 0x3e19c9072331b530L, 
          0x7f2eaa0a85848581L, 0x1a0c03b1df8af611L, 0x355817f373ccb876L, 
          0x6d3fadfac84b3424L, 0xdfbdcece67006ac9L, 0x2dd27ebb4504974eL, 
          0x5dd80dc941929e51L, 0xc0314325637a193aL, 0x275c6b23eb69b26dL, 
          0x509c814fb511cfb9L, 0xa5178fff668ae0b6L, 0x21cf93dd7888939aL, 
          0x453e9f77bf8e7e29L, 0x8dd01fad907ffc3cL, 0x1d0b15a491eb8459L, 
          0x3b7b0d9ac471b2e4L, 0x79d1013cf6ab6a45L, 0xf97ae3d0d2446f25L, 
          0x3317f065bfbf5f43L
 };
          
  // base 10 exponents for 2**-1075 through 2**972, at intervals of 2**11
  private static final short[] pow2x = {
          -323, -320, -316, -313, -310, -307, -303, -300, 
          -297, -293, -290, -287, -283, -280, -277, -273, 
          -270, -267, -264, -260, -257, -254, -250, -247, 
          -244, -240, -237, -234, -230, -227, -224, -220, 
          -217, -214, -211, -207, -204, -201, -197, -194, 
          -191, -187, -184, -181, -177, -174, -171, -167, 
          -164, -161, -158, -154, -151, -148, -144, -141, 
          -138, -134, -131, -128, -124, -121, -118, -114, 
          -111, -108, -105, -101, -98, -95, -91, -88, 
          -85, -81, -78, -75, -71, -68, -65, -62, 
          -58, -55, -52, -48, -45, -42, -38, -35, 
          -32, -28, -25, -22, -18, -15, -12, -9, 
          -5, -2, 1, 5, 8, 11, 15, 18, 
          21, 25, 28, 31, 35, 38, 41, 44, 
          48, 51, 54, 58, 61, 64, 68, 71, 
          74, 78, 81, 84, 87, 91, 94, 97, 
          101, 104, 107, 111, 114, 117, 121, 124, 
          127, 131, 134, 137, 140, 144, 147, 150, 
          154, 157, 160, 164, 167, 170, 174, 177, 
          180, 184, 187, 190, 193, 197, 200, 203, 
          207, 210, 213, 217, 220, 223, 227, 230, 
          233, 237, 240, 243, 246, 250, 253, 256, 
          260, 263, 266, 270, 273, 276, 280, 283, 
          286, 289, 293 
  } ;

  /**
   * Mimics <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Double.html#toString(double)">Double.toString(double)</a>.
   * <p>
   * String conversion is a bit of a gray area.  The J2SE implementation of
   * this function (<code>Double.toString(double)</code> has some problems.  
   * Often times it does not return the shortest valid String, even though it 
   * claims to do so, and it has a few
   * corner cases where it behaves oddly (e.g. 0.001 gets converted to 
   * the String "0.0010").
   * <p>
   * The implementation in MicroDouble uses a much simpler table-based 
   * algorithm.  It frequently returns slightly different results than 
   * <code>Double.toString(double)</code>.  Sometimes the results are better,
   * and sometimes worse.  Ususally the difference is confined to the last
   * character, which may be different or missing in one of the results.
   */
  public static String toString(long d) {
    return toString(d, 100);
  }
  
  /**
   * Returns a string representation of the double argument, rounded so that
   * the returned <code>String</code> is no longer than
   * <code>maxStringLength</code> characters (or 9 characters, if 
   * <code>maxStringLength</code> is less than 9).  
   *
   * @param      d   the <code>double</code> to be converted.
   * @param      maxStringLength the maximum length of the returned string 
   * @return     a string representation of the argument.
   *
   * @see #toString(long)
   */
  public static String toString(long d, int maxStringLength) {
    if (isNaN(d)) {
      return "NaN";
    }
    boolean n = unpackSign(d);
    if (isZero(d)) {
      return (n ? "-0.0" : "0.0");
    } else if (isInfinite(d)) {
      return (n ? "-Infinity" : "Infinity");
    }
    if (maxStringLength < 9) {
      maxStringLength = 9;
    }
    // convert from base 2 to base 10
    int base2x = unpackExponent(d);
    long base2m = unpackMantissa(d);
    int idx = base2x + 1075;
    int dx = idx % 11;
    base2m <<= dx;
    idx /= 11;
    int base10x = pow2x[idx];
    while (base2m <= 0xcccccccccccccccL) {
      base2m = (base2m << 3) + (base2m << 1); // base2m *= 10;
      base10x--;
    }
    long base10m = dpMul(base2m, pow2m[idx]);
    boolean roundedUp = false;
    while (true) {
      int r = (int) (base10m % 10);
      long mt = base10m / 10;
      int xt = base10x + 1;
      if (r != 0) {
        boolean rut;
        if ((r > 5) || ((r == 5) && (! roundedUp))) {
          rut = true;
          mt++;
        } else {
          rut = false;
        }
        long dt = decToDouble(n, xt, mt);
        if (dt != d) {
          if (rut) {
            mt--;
          } else {
            mt++;
          }
          rut ^= true;
          dt = decToDouble(n, xt, mt);
          if (dt != d) {
            break;
          }
        }
        roundedUp = rut;
      }
      base10m = mt;
      base10x = xt;
    }
    
    while (true) {
      String s = toString(n, base10x, base10m);
      if (s.length() <= maxStringLength) {
        return s;
      }
      int r = (int) (base10m % 10);
      base10m /= 10;
      base10x++;
      if ((r > 5) || ((r == 5) && (! roundedUp))) {
        roundedUp = true;
        base10m++;
      } else {
        roundedUp = false;
      }      
      while ((base10m % 10) == 0) {
        base10m /= 10;
        base10x++;
      }
    }
  }
  
  private static String toString(boolean negative, int base10x, long base10m) {
    StringBuffer sb = new StringBuffer(26);
    if (negative) {
      sb.append('-');
    }
    String s = Long.toString(base10m);
    base10x += s.length() - 1;
    boolean scientific = ((base10x < -3) || (base10x >= 7));
    int dp; // index of decimal point in final string
    if (scientific) {
      dp = 1;
    } else {
      dp = base10x + 1;
      if (dp < 1) {
        sb.append('0');
      }
    }
    for (int i=0; i<dp; i++) {
      if (i < s.length()) {
        sb.append(s.charAt(i));
      } else {
        sb.append('0');
      }
    }
    sb.append('.');
    if (dp >= s.length()) {
      sb.append('0');
    } else {
      for (int i=dp; i<s.length(); i++) {
        if (i < 0) {
          sb.append('0');
        } else {
          sb.append(s.charAt(i));
        }
      }
    }
    if (scientific) {
      sb.append('E');
      sb.append(Integer.toString(base10x));
    }
    return sb.toString();
  }

  private static final long ONE_EIGHTY =           0x4066800000000000L;
  private static final long TWO_HUNDRED =          0x4069000000000000L;
  
  /**
   * Mimics <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Math.html#toDegrees(double)">Math.toDegrees(double)</a>.
   */
  public static long toDegrees(long angrad) {
    return div(mul(angrad, ONE_EIGHTY), PI);
  }
  
  /**
   * Mimics <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Math.html#toRadians(double)">Math.toRadians(double)</a>.
   */
  public static long toRadians(long angdeg) {
    return mul(div(angdeg, ONE_EIGHTY), PI);
  }

  public static long toGradians(long angrad) {
    return div(mul(angrad, TWO_HUNDRED), PI);
  }

  public static long gradiansToRadians(long anggrad) {
    return mul(div(anggrad, TWO_HUNDRED), PI);
  }
  
  /////////////////////////////////////////////////////////////////////////////
  // Elementary functions.  Most are ported directly from fdlibm.
  /////////////////////////////////////////////////////////////////////////////

  private static long set(int newHiPart, int newLowPart) {
    return ((((long) newHiPart) << 32) | newLowPart);
  }

  private static long setLO(long d, int newLowPart) {
    return ((d & 0xFFFFFFFF00000000L) | newLowPart);
  }

  private static long setHI(long d, int newHiPart) {
    return ((d & 0x00000000FFFFFFFFL) | (((long) newHiPart) << 32));
  }

  private static int getHI(long d) {
    return ((int) (d >> 32));
  }
  
  private static int getLO(long d) {
    return ((int) d);
  }

  private static int ilogb(long d) {
    if (isZero(d)) {
      return 0x80000001;
    } else if (isNaN(d) || (isInfinite(d))) {
      return 0x7fffffff;
    }
    int x = (((int) (d >> 52)) & 0x7ff);
    if (x == 0) {
      long m = (d & FRACTION_MASK);
      while (m < IMPLIED_ONE) {
        m <<= 1;
        x--;
      }
    }
    return x - 1023;
  }
  
  /**
   * @return the magnitude of x with the sign of y
   */
  private static long copySign(long x, long y) {
    return (x & 0x7fffffffffffffffL) | (y & 0x8000000000000000L);
  }

  /** 
   * Returns the value of the first argument, multiplied by 2 raised to the
   * power of the second argument.  Note that the second argument is really
   * an <code>int</code>, not a <code>float</code> or <code>double</code>.
   *
   * @param d   a <code>double</code> value.
   * @param n   an <code>int</code> value.
   * @return  the value <code>d * 2<sup>n</sup></code>.
   */
  public static long scalbn(long d, int n) {
    if (isNaN(d)) {
      return NaN;
    } else if ((n == 0) || isInfinite(d) || isZero(d)) {
      return d;
    } else if (n >= 2098) {
      return copySign(POSITIVE_INFINITY, d);
    } else if (n <= -2099) {
      return copySign(ZERO, d);
    }
    int x = ((int) (d >> 52) & 0x7ff);
    int x2 = x + n;
    if ((x == 0) || (x2 <= 0)) { // argument and/or return value are subnormal 
      return pack(unpackSign(d), x2 - 1075, unpackMantissa(d));
    } else if (x2 >= 0x7ff) { // overflow
      return copySign(POSITIVE_INFINITY, d);
    }
    return ((d & 0x800fffffffffffffL) | (((long) x2) << 52));
  }

  /**
   * Mimics <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Math.html#IEEEremainder(double, double)">Math.IEEEremainder(double, double)</a>.
   */
  public static long IEEEremainder(long d1, long d2) {
    if (isNaN(d1) || isNaN(d2) || isInfinite(d1) || isZero(d2)) {
      return NaN;
    } else if (isZero(d1) || isInfinite(d2)) {
      return d1;
    }
    int hx = getHI(d1); // high word of x 
    int lx = getLO(d1); // low  word of x 
    int hp = getHI(d2); // high word of p 
    int lp = getLO(d2); // low  word of p 
    boolean negative = unpackSign(d1);
    hp &= 0x7fffffff;
    hx &= 0x7fffffff;

    if (hp<=0x7fdfffff) d1 = mod(d1,scalbn(d2, 1)); // now x < 2p 
    if (((hx-hp)|(lx-lp))==0) return ZERO; //zero*x;
    d1  = abs(d1);
    d2  = abs(d2);
    if (hp<0x00200000) {
      if(gt(scalbn(d1, 1), d2)) {
        d1 = sub(d1, d2);
        if (ge(scalbn(d1, 1), d2)) d1 = sub(d1, d2);
      }
    } else {
      long p_half = scalbn(d2, -1);
      if (gt(d1, p_half)) {
        d1 = sub(d1, d2);
        if (ge(d1, p_half)) d1 = sub(d1, d2);
      }
    }
    if (negative) {
      return negate(d1);
    }
    return d1;
  }

  /**
   * Mimics <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Math.html#sqrt(double)">Math.sqrt(double)</a>.
   */
  public static long sqrt(long d) {
    if (isZero(d)) {
      return d;
    } else if (unpackSign(d) || isNaN(d)) {
      return NaN;
    } else if (d == POSITIVE_INFINITY) {
      return d;
    }
    // f is positive, nonzero, and finite

    // unpack
    int x = unpackExponent(d);
    long m = unpackMantissa(d);
    // normalize 
    while (m < IMPLIED_ONE) {
      m <<= 1;
      x--;
    }
    // make exponent even
    if ((x & 1) != 0) {
      m <<= 1;
    }
    // compute final exponent
    x = (x >> 1) - 26;
    
    // generate sqrt(x) bit by bit
    m <<= 1;
    long q = 0L; // q = sqrt(x)
    long s = 0L;
    long r = 0x0020000000000000L;
    while (r != 0) {
      long t = s + r;
      if (t < m) {
        s = t + r;
        m -= t;
        q |= r;
      }
      m <<= 1;
      r >>= 1;
    }
    // round half even
    if (m != 0) {
      q += q & 1L;
    }
    q >>= 1;
    return (((x + 1075L) << 52) | (q & FRACTION_MASK));
  }
  
  private static final long EXP_UNDERFLOW = 0xc0874910d52d3051L; // -745.13321910194110842
  private static final long EXP_OVERFLOW  = 0x40862e42fefa39efL; // 709.782712893383973096
  private static final long LN2_HI        = 0x3fe62e42fee00000L;
  private static final long LN2_LO        = 0x3dea39ef35793c76L; //  1.90821492927058770002e-10
  private static final long INV_LN2       = 0x3ff71547652b82feL; //  1.44269504088896338700e+00
  private static final long P1            = 0x3fc555555555553eL; //  1.66666666666666019037e-01
  private static final long P2            = 0xbf66c16c16bebd93L; // -2.77777777770155933842e-03
  private static final long P3            = 0x3f11566aaf25de2cL; //  6.61375632143793436117e-05
  private static final long P4            = 0xbebbbd41c5d26bf1L; // -1.65339022054652515390e-06
  private static final long P5            = 0x3e66376972bea4d0L; //  4.13813679705723846039e-08

  /**
   * Mimics <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Math.html#exp(double)">Math.exp(double)</a>.
   */
  public static long exp(long d) {
    long x = d;
    if (isNaN(x)) {
      return NaN;
    } else if (isZero(x)) {
      return ONE;
    } else if (le(x, EXP_UNDERFLOW)) {
      return ZERO;
    } else if (ge(x, EXP_OVERFLOW)) {
      return POSITIVE_INFINITY;
    }

    // argument reduction
    long hi=0, lo=0;
    int k=0;
    int hx = getHI(x) & 0x7fffffff;
    if (hx > 0x3fd62e42) { // if |x| > 0.5 ln2
      if (hx < 0x3ff0a2b2) { // and |x| < 1.5 ln2
        if (unpackSign(x)) {
          hi = add(x, LN2_HI);
          lo = LN2_LO | SIGN_MASK;
          k = -1;
        } else {
          hi = sub(x, LN2_HI);
          lo = LN2_LO;
          k = 1;
        }
      } else {
        long t = rint(mul(INV_LN2, x));
        k = intValue(t);
        hi = sub(x, mul(t, LN2_HI));
        lo = mul(t, LN2_LO);
      }
      x = sub(hi, lo);
    } else if (hx < 0x3e300000) { // when |x| < 2**-28
      return add(x, ONE);
    } else {
      k = 0;
    }
    long t  = mul(x, x);
    long c = sub(x, mul(t, add(P1, mul(t, add(P2, mul(t, 
            add(P3, mul(t, add(P4, mul(t, P5))))))))));
    if (k == 0) {
      return sub(ONE, sub(div(mul(x, c), 
              sub(c, TWO)), x));
    } 
    x = sub(ONE, sub(sub(lo, div(mul(x, c), 
            sub(TWO, c))), hi));
    return scalbn(x, k);
  }

  // scaled coefficients related to expm1 
  private static final long Q1 = 0xBFA11111111110F4L; // -3.33333333333331316428e-02
  private static final long Q2 = 0x3F5A01A019FE5585L; // 1.58730158725481460165e-03
  private static final long Q3 = 0xBF14CE199EAADBB7L; // -7.93650757867487942473e-05
  private static final long Q4 = 0x3ED0CFCA86E65239L; // 4.00821782732936239552e-06
  private static final long Q5 = 0xBE8AFDB76E09C32DL; // -2.01099218183624371326e-07

  /**
   * Returns Euler's number <i>e</i> raised to the power of a
   * <code>double</code> value, less 1, computed in a way that is accurate
   * even when the value of d is close to zero.
   *
   * @param   d   the exponent to raise <i>e</i> to.
   * @return  the value <i>e</i><sup><code>d</code></sup><code> - 1</code>, 
   *          where <i>e</i> is the base of the natural logarithms.
   * @see #exp(long)
   * @see #log1p(long)
   */
  public static long expm1(long d) {
    int hx = getHI(d); // high word of x
    int xsb = hx & 0x80000000; // sign bit of x
    long y;
    if (xsb==0) y=d; else y= -d; // y = |x| 
    hx &= 0x7fffffff; // high word of |x| 

    // filter out huge and non-finite argument
    if(hx >= 0x4043687A) { // if |x|>=56*ln2 
      if(hx >= 0x40862E42) { // if |x|>=709.78... 
        if(hx>=0x7ff00000) {
          if(((hx&0xfffff)|getLO(d))!=0) 
               return NaN;
          else return (xsb==0)? d : NEGATIVE_ONE; // exp(+-inf)={inf,-1}
        }
        if(d > 0x40862E42FEFA39EFL) return POSITIVE_INFINITY; // 7.09782712893383973096e+02 
      }
      if(xsb!=0) { // x < -56*ln2, return -1.0 with inexact 
        return NEGATIVE_ONE;
      }
    }

    // argument reduction
    long hi, lo, c;
    int k;
    if(hx > 0x3fd62e42) { //  if  |x| > 0.5 ln2 
      if(hx < 0x3FF0A2B2) { // and |x| < 1.5 ln2 
        if(xsb==0) {
          hi = sub(d, LN2_HI);
          lo = LN2_LO;
          k = 1;
        } else {
          hi = add(d, LN2_HI);
          lo = negate(LN2_LO);
          k = -1;
        }
      } else {
        long tmp = mul(INV_LN2, d);
        if (xsb == 0) {
          tmp = add(tmp, ONE_HALF);
        } else {
          tmp = sub(tmp, ONE_HALF);
        }
        k = intValue(add(mul(INV_LN2, d), 
                (xsb == 0) ? ONE_HALF : negate(ONE_HALF)));
        long t = intToDouble(k);
        hi = sub(d, mul(t, LN2_HI)); //  t*ln2_hi is exact here 
        lo = mul(t, LN2_LO);
      }
      d = sub(hi, lo);
      c = sub(sub(hi, d), lo);
    } 
    else if(hx < 0x3c900000) { // when |x|<2**-54, return x 
      return d;
    }
    else {
      k = 0;
      c = 0;
    }

    // x is now in primary range 
    long hfx = scalbn(d, -1);
    long hxs = mul(d, hfx);
    long r1 = add(ONE, mul(hxs, add(Q1, mul(hxs, add(Q2, mul(hxs, 
            add(Q3, mul(hxs, add(Q4, mul(hxs, Q5))))))))));
    long t = sub(THREE, mul(r1, hfx));
    long e = mul(hxs, div(sub(r1, t), sub(SIX, mul(d, t))));
    if(k==0) return sub(d, sub(mul(d, e), hxs)); // c is 0
    else {
      e = sub(sub(mul(d, sub(e, c)), c), hxs);
      if (k == -1) return sub(scalbn(sub(d, e), -1), ONE_HALF);
      if(k==1) 
        if (lt(d, negate(ONE_FOURTH))) return negate(scalbn(sub(e, add(d, 
                ONE_HALF)), 1));
        else return add(ONE, scalbn(sub(d, e), 1));
        if (k <= -2 || k>56) { // suffice to return exp(x)-1 
          y = sub(ONE, sub(e, d));
          y = setHI(y, getHI(y) + (k << 20)); // add k to y's exponent 
          return sub(y, ONE);
        }
        t = ONE;
        if(k<20) {
          t = setHI(t, 0x3ff00000 - (0x200000>>k)); // t=1-2^-k 
          y = sub(t, sub(e, d));
          y = setHI(y, getHI(y) + (k << 20)); // add k to y's exponent 
       } else {
         t = setHI(t, ((0x3ff-k)<<20)); // 2^-k 
         y = add(sub(d, add(e, t)), ONE);
         y = setHI(y, getHI(y) + (k << 20)); //add k to y's exponent 
       }
    }
    return y;
  }

  private static final long BP[]       = { ONE, THREE_HALVES };
  private static final long DP_HI[]    = { ZERO, 0x3fe2b80340000000L}; // 5.84962487220764160156e-01 
  private static final long DP_LO[]    = { ZERO, 0x3e4cfdeb43cfd006L}; // 1.35003920212974897128e-08 
  // poly coefs for (3/2)*(log(x)-2s-2/3*s**3 
  private static final long L1         = 0x3fe3333333333303L; // 5.99999999999994648725e-01 
  private static final long L2         = 0x3fdb6db6db6fabffL; //  4.28571428578550184252e-01 
  private static final long L3         = 0x3fd55555518f264dL; //  3.33333329818377432918e-01 
  private static final long L4         = 0x3fd17460a91d4101L; //  2.72728123808534006489e-01 
  private static final long L5         = 0x3fcd864a93c9db65L; //  2.30660745775561754067e-01 
  private static final long L6         = 0x3fca7e284a454eefL; //  2.06975017800338417784e-01 
  private static final long LN2_HI_B   = 0x3fe62e4300000000L; //  6.93147182464599609375e-01 
  private static final long LN2_LO_B   = 0xbe205c610ca86c39L; // -1.90465429995776804525e-09) 0xbe205c610ca86c39 
  private static final long OVT        = 0x3c971547652b82feL; // 8.0085662595372944372e-17) // -(1024-log2(ovfl+.5ulp)) 
  private static final long CP         = 0x3feec709dc3a03fdL; //  9.61796693925975554329e-01 = 2/(3ln2)
  private static final long CP_HI      = 0x3feec709e0000000L; //  9.61796700954437255859e-01 = (float)cp
  private static final long CP_LO      = 0xbe3e2fe0145b01f5L; // -7.02846165095275826516e-09 = tail of cp_h
  private static final long INV_LN2_HI = 0x3ff7154760000000L; //  1.44269502162933349609e+00 = 24b 1/ln2
  private static final long INV_LN2_LO = 0x3e54ae0bf85ddf44L; //  1.92596299112661746887e-08 = 1/ln2 tail

  /**
   * Mimics <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Math.html#pow(double, double)">Math.pow(double, double)</a>.
   */
  public static long pow(long d1, long d2) {
    if (isZero(d2)) {
      return ONE;
    } else if (isNaN(d1) || isNaN(d2)) {
      return NaN;
    }
      
    int i0 = (int) ((ONE  >> 29) ^ 1);
    int i1 = 1 - i0;    
    int hx = getHI(d1);
    int lx = getLO(d1);
    int hy = getHI(d2);
    int ly = getLO(d2);
    int ix = hx & 0x7fffffff;
    int iy = hy & 0x7fffffff;

    // determine if y is an odd int when x < 0
    // yisint = 0 ... y is not an integer
    // yisint = 1 ... y is an odd int
    // yisint = 2 ... y is an even int
    int yisint  = 0;
    if (hx < 0) {
      if (iy >= 0x43400000)
        yisint = 2; // even integer y
      else if(iy >= 0x3ff00000) {
        int k = (iy>>20)-0x3ff; // exponent
        if (k > 20) {
          int j = ly>>>(52-k);
          if ((j<<(52-k)) == ly)
            yisint = 2-(j&1);
        } else if (ly == 0) {
          int j = iy >> (20-k);
          if ((j<<(20-k)) == iy)
            yisint = 2-(j&1);
        }
      }
    }

    // special value of y
    if (ly==0) {
      if (iy == 0x7ff00000) { // y is +-inf
        if (((ix-0x3ff00000)|lx) == 0)
                return  NaN; // inf**+-1 is NaN 
        else if (ix >= 0x3ff00000) // (|x|>1)**+-inf = inf,0 
                return (hy>=0)? POSITIVE_INFINITY : ZERO;
        else // (|x|<1)**-,+inf = inf,0 
                return (hy<0) ? POSITIVE_INFINITY : ZERO;
      } 
      if (iy == 0x3ff00000) { // y is  +-1 
        if (hy < 0)
          return div(ONE, d1);
        else
          return d1;
      }
      if (hy == 0x40000000)
        return mul(d1, d1); // y is  2
      if (hy == 0x3fe00000) { // y is  0.5
        if (hx>=0) // x >= +0 
          return sqrt(d1);
      }
    }

    long ax = abs(d1);
    // special value of x
    if (lx == 0) {
      if (ix==0x7ff00000 || ix==0 || ix==0x3ff00000) {
        long z = ax; // x is +-0,+-inf,+-1
        if (hy < 0 )
          z = div(ONE, z); // z = (1/|x|)
        if (hx < 0) {
          if (((ix-0x3ff00000)|yisint) == 0) {
            z = NaN; // (-1)**non-int is NaN
          } else if (yisint==1) 
            z = negate(z); // (x<0)**odd = -(|x|**odd)
        }
        return z;
      }
    }

	  int n = (hx >> 31) + 1;

    /* (x<0)**(non-int) is NaN */
	  if ((n | yisint) == 0) {
      return NaN;
    }

    boolean negative = ((n | (yisint-1)) == 0);

    // |y| is huge
    long t1, t2;
    if (iy > 0x41e00000) { // if |y| > 2**31
      if (iy > 0x43f00000){ // if |y| > 2**64, must o/uflow 
        if (ix <= 0x3fefffff) {
          return ((hy < 0) ? POSITIVE_INFINITY : ZERO);
        }
        return ((hy > 0) ? POSITIVE_INFINITY : ZERO);
      }
      // over/underflow if x is not close to one
      if (ix < 0x3fefffff) {
        if (negative) {
          return ((hy < 0) ? NEGATIVE_INFINITY : NEGATIVE_ZERO);
        } else {
          return ((hy < 0) ? POSITIVE_INFINITY : ZERO);
        }
      }
      if (ix > 0x3ff00000) {
        if (negative) {
          return ((hy > 0) ? NEGATIVE_INFINITY : NEGATIVE_ZERO);
        } else {
          return ((hy > 0) ? POSITIVE_INFINITY : ZERO);
        }
      }
      // now |1-x| is tiny <= 2**-20, suffice to compute
      // log(x) by x-x^2/2+x^3/3-x^4/4
      long t = sub(ax, ONE); // t has 20 trailing zeros
      long w = mul(mul(t, t), sub(ONE_HALF, mul(t, 
              sub(ONE_THIRD, mul(t, ONE_FOURTH)))));
      long u = mul(INV_LN2_HI, t); // INV_LN2_HI has 21 sig. bits
      long v = sub(mul(t, INV_LN2_LO), mul(w, INV_LN2));
      t1 = setLO(add(u, v), 0);
      t2 = sub(v, sub(t1, u));
    } else {
      n = 0;
      // take care subnormal number
      if (ix<0x00100000) {
        ax = scalbn(ax, 53);
        n -= 53;
        ix = getHI(ax);
      }
      n  += ((ix)>>20) - 0x3ff;
      int j  = ix & 0x000fffff;
      // determine interval 
      ix = j|0x3ff00000; // normalize ix
      int k;
      if (j <= 0x3988E)
        k=0; // |x|<sqrt(3/2)
      else if (j < 0xBB67A)
        k=1; // |x|<sqrt(3)
      else {
        k = 0;
        n+=1;
        ix -= 0x00100000;
      }
      ax = setHI(ax, ix);

      // compute ss = s_h+s_l = (x-1)/(x+1) or (x-1.5)/(x+1.5)
      long u = sub(ax, BP[k]); // bp[0]=1.0, bp[1]=1.5
      long v = div(ONE, add(ax, BP[k]));
      long ss = mul(u, v);
      long s_h = setLO(ss, 0);
      // t_h=ax+bp[k] High 
      long t_h = setHI(ZERO, ((ix>>1)|0x20000000)+0x00080000+(k<<18)); 
      long t_l = sub(ax, sub(t_h, BP[k]));
      long s_l = mul(v, sub(sub(u, mul(s_h, t_h)), mul(
              s_h, t_l)));
      // compute log(ax)
      long s2 = mul(ss, ss);
      long r = mul(mul(s2, s2), add(L1, mul(s2, add(L2, mul(
              s2, add(L3, mul(s2, add(L4, mul(s2, add(L5, mul(
              s2, L6)))))))))));
      r = add(r, mul(s_l, add(s_h, ss)));
      s2 = mul(s_h, s_h);
      t_h = setLO(add(add(THREE, s2), r), 0);
      t_l = sub(r, sub(sub(t_h, THREE), s2));
      // u+v = ss*(1+...) 
      u = mul(s_h, t_h);
      v = add(mul(s_l, t_h), mul(t_l, ss));
      // 2/(3log2)*(ss+...) 
      long p_h = setLO(add(u, v), 0);
      long p_l = sub(v, sub(p_h, u));
      long z_h = mul(CP_HI, p_h); // cp_h+cp_l = 2/(3*log2) 
      long z_l = add(add(mul(CP_LO, p_h), mul(p_l, CP)), DP_LO[k]);
      // log2(ax) = (ss+..)*2/(3*log2) = n + dp_h + z_h + z_l 
      long t = intToDouble(n);
      t1 = setLO(add(add(add(z_h, z_l), DP_HI[k]), t), 0);
      t2 = sub(z_l, sub(sub(sub(t1, t), DP_HI[k]), z_h));
    }

    // split up y into y1+y2 and compute (y1+y2)*(t1+t2) 
    long y1 = setLO(d2, 0);
    long p_l = add(mul(sub(d2, y1), t1), mul(d2, t2));
    long p_h = mul(y1, t1);
    long z = add(p_l, p_h);
    int j = getHI(z);
    int i = getLO(z);
    if (j >= 0x40900000) { // z >= 1024
      if ((((j-0x40900000)|i) != 0) // if z > 1024
          || gt(add(p_l, OVT), sub(z, p_h))) {
        return negative ? NEGATIVE_INFINITY : POSITIVE_INFINITY;
      }
    } else if((j&0x7fffffff) >= 0x4090cc00) { // z <= -1075
      if ((((j-0xc090cc00)|i) != 0) // z < -1075
          || le(p_l, sub(z, p_h))) {
        return negative ? NEGATIVE_ZERO : ZERO;
      }
    }
    // compute 2**(p_h+p_l)
    i = j&0x7fffffff;
    int k = (i>>20)-0x3ff;
    n = 0;
    if (i > 0x3fe00000) { // if |z| > 0.5, set n = [z+0.5]
      n = j + (0x00100000>>(k+1));
      k = ((n&0x7fffffff)>>20) - 0x3ff; // new k for n
      long t = ZERO;
      t = setHI(t, (n&~(0x000fffff>>k)));
      n = ((n&0x000fffff)|0x00100000)>>(20-k);
      if (j < 0) n = -n;
      p_h = sub(p_h, t);
    }
    long t = setLO(add(p_l, p_h), 0);
    long u = mul(t, LN2_HI_B);
    long v = add(mul(sub(p_l, sub(t, p_h)), LN2), mul(t, 
            LN2_LO_B));
    z = add(u, v);
    long w = sub(v, sub(z, u));
    t = mul(z, z);
    t1 = sub(z, mul(t, add(P1, mul(t, add(P2, mul(t, add(P3,
            mul(t, add(P4, mul(t, P5))))))))));
    long r = sub(div(mul(z, t1), sub(t1, TWO)), add(
            w, mul(z, w)));
    z = sub(ONE, sub(r, z));
    j = getHI(z);
    j += (n<<20);
    if ((j>>20) <= 0) {
      z = scalbn(z,n); //subnormal output
    } else {
      i = getHI(z);
      i += (n << 20);
      z = setHI(z, i);
    }
    if (negative) {
      z = negate(z);
    }
    return z;
  }
  
  /**
   * Returns the logarithm of a <code>double</code> value using a specified
   * base.  For most arguments, the return value is computed as:
   * <code>log(d) / log(base)</code>.  If <code>base</code> is <code>E</code> or 
   * <code>10</code> the dedicated log function is used.  If <code>base</code>
   * is zero, infinite, <code>NaN</code>, or negative, <code>NaN</code> is
   * returned.
   *
   * @param   d   a <code>double</code> value greater than <code>0.0</code>.
   * @param   base   a <code>double</code> value greater than <code>0.0</code>.
   * @return  the value log<sub><code>base</code></sub>&nbsp;<code>d</code>
   */
  public static long log(long d, long base) {
    if (base == E) {
      return log(d);
    } else if (base == TEN) {
      return log10(d);
    } else if (isZero(base) || isInfinite(base) || isNaN(base) 
            || unpackSign(base)) {
      return NaN;
    }
    return div(log(d), log(base));
  }

  private static final long IVLN10    = 0x3FDBCB7B1526E50EL; // 4.34294481903251816668e-01
  private static final long LOG10_2HI = 0x3FD34413509F6000L; // 3.01029995663611771306e-01
  private static final long LOG10_2LO = 0x3D59FEF311F12B36L; // 3.69423907715893078616e-13

  /**
   * Returns the base 10 logarithm of a <code>double</code>
   * value.  
   *
   * @param   d   a <code>double</code> value greater than <code>0.0</code>.
   * @return  the value log<sub>10</sub>&nbsp;<code>d</code>
   */
  public static long log10(long d) {
    if (isZero(d)) {
      return NEGATIVE_INFINITY;
    } else if (isNaN(d) || unpackSign(d)) {
      return NaN;
    } else if (d == POSITIVE_INFINITY) {
      return d;
    }
    int n = ilogb(d);
    if (n < 0) {
      n++;
    }
    d = scalbn(d, -n);
    long dn = intToDouble(n);
    return add(mul(dn, LOG10_2HI), add(mul(dn, LOG10_2LO), 
            mul(IVLN10, log(d))));
  }
  
  private static final long LG1 = 0x3fe5555555555593L;  // 6.666666666666735130e-01
  private static final long LG2 = 0x3fd999999997fa04L;  // 3.999999999940941908e-01
  private static final long LG3 = 0x3fd2492494229359L;  // 2.857142874366239149e-01
  private static final long LG4 = 0x3fcc71c51d8e78afL;  // 2.222219843214978396e-01
  private static final long LG5 = 0x3fc7466496cb03deL;  // 1.818357216161805012e-01
  private static final long LG6 = 0x3fc39a09d078c69fL;  // 1.531383769920937332e-01
  private static final long LG7 = 0x3fc2f112df3e5244L;  // 1.479819860511658591e-01

  /**
   * Mimics <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Math.html#log(double)">Math.log(double)</a>.
   */
  public static long log(long d) {
    if (isZero(d)) {
      return NEGATIVE_INFINITY;
    } else if (isNaN(d) || unpackSign(d)) {
      return NaN;
    } else if (d == POSITIVE_INFINITY) {
      return d;
    }

    int hx = getHI(d); // high word of x 
    int k=0;
    if (hx < 0x00100000) { // x < 2**-1022
      k -= 54;
      d = scalbn(d, 54);
      hx = getHI(d);
    } 
    k += (hx>>20)-1023;
    hx &= 0x000fffff;
    int i = (hx+0x95f64)&0x100000;
    d = setHI(d, hx|(i^0x3ff00000)); // normalize x or x/2
    k += (i>>20);
    long f = sub(d, ONE);
    if ((0x000fffff&(2+hx))<3) { // |f| < 2**-20
      if (isZero(f)) {
        if (k == 0) {
          return ZERO;
        }
        long dk = intToDouble(k);
        return add(mul(dk, LN2_HI), mul(dk, LN2_LO));
      }
      long R = mul(mul(f, f), sub(ONE_HALF, 
              mul(ONE_THIRD, f)));
      if (k == 0) {
        return sub(f, R);
      } else {
        long dk = intToDouble(k);
        return sub(mul(dk, LN2_HI), sub(sub(R, 
                mul(dk, LN2_LO)), f));
      }
    }
    long dk = intToDouble(k);
    long s = div(f, add(TWO, f));
    long z = mul(s, s);
    long w = mul(z, z);
    long R = add(mul(w, add(LG2, mul(w, add(LG4, mul(w, LG6))))),
            mul(z, add(LG1, mul(w, add(LG3, mul(w, add(LG5, 
            mul(w, LG7))))))));
    i = (hx - 0x6147a) | (0x6b851 - hx);
    if (i > 0) {
      long hfsq = mul(scalbn(f, -1), f);
      if (k == 0) {
        return sub(f, sub(hfsq, mul(s, add(hfsq, R))));
      } else {
        return sub(mul(dk, LN2_HI), sub(sub(hfsq, 
               add(mul(s, add(hfsq, R)), mul(dk, LN2_LO))), f));
      }
    } else if (k==0) {
      return sub(f, mul(s, sub(f, R)));
    } 
    return sub(mul(dk, LN2_HI), sub(sub(mul(s, 
            sub(f, R)), mul(dk, LN2_LO)), f));
  }

  private static final long LP1 = 0x3FE5555555555593L; // 6.666666666666735130e-01
  private static final long LP2 = 0x3FD999999997FA04L; // 3.999999999940941908e-01
  private static final long LP3 = 0x3FD2492494229359L; // 2.857142874366239149e-01
  private static final long LP4 = 0x3FCC71C51D8E78AFL; // 2.222219843214978396e-01
  private static final long LP5 = 0x3FC7466496CB03DEL; // 1.818357216161805012e-01
  private static final long LP6 = 0x3FC39A09D078C69FL; // 1.531383769920937332e-01
  private static final long LP7 = 0x3FC2F112DF3E5244L; // 1.479819860511658591e-01

  /**
   * Returns the natural logarithm of <code>1 + d</code>, computed in a way 
   * that is accurate even when the value of d is close to zero.
   *
   * @param   d   a <code>double</code> value greater than <code>-1.0</code>.
   * @return  the value ln&nbsp;<code>d</code>, the natural logarithm of
   *          <code>d + 1</code>.
   * @see #log(long)
   * @see #expm1(long)
   */
  public static long log1p(long d) {
    int hx = getHI(d);
    int ax = hx & 0x7fffffff;
    int k = 1;
    int hu = 0;
    long f = 0;
    if (hx < 0x3FDA827A) { // x < 0.41422 
      if(ax>=0x3ff00000) { // x <= -1.0 
        if (d == NEGATIVE_ONE) return NEGATIVE_INFINITY; // log1p(-1)=+inf 
        else return NaN; // log1p(x<-1)=NaN
      }
      if(ax<0x3e200000) { // |x| < 2**-29 
        if(ax<0x3c900000) // |x| < 2**-54 
          return d;
        else
          return sub(d, scalbn(mul(d, d), -1));
      }
      if(hx>0||hx<=0xbfd2bec3) {
        k=0;f=d;hu=1;} // -0.2929<x<0.41422 
    } 
    if (hx >= 0x7ff00000) return d;
    long u;
    long c = 0;
    if(k!=0) {
      if(hx<0x43400000) {
        u = add(ONE, d);
        hu = getHI(u); // high word of u 
        k  = (hu>>20)-1023;
        c  = (k > 0) ? sub(ONE, sub(u, d)) : sub(d, sub(u, 
                ONE)); // correction term 
        c  = div(c, u);
      } else {
        u  = d;
        hu = getHI(u); // high word of u 
        k  = (hu>>20)-1023;
        c  = ZERO;
      }
      hu &= 0x000fffff;
      if(hu<0x6a09e) {
        u = setHI(u, hu|0x3ff00000); // normalize u 
      } else {
        k += 1;
        u = setHI(u, hu|0x3fe00000); // normalize u/2 
        hu = (0x00100000-hu)>>2;
      }
      f = add(u, NEGATIVE_ONE);
    }
    long hfsq= scalbn(mul(f, f), -1);
    long R;
    long dk = intToDouble(k);
    if(hu==0) { // |f| < 2**-20 
      if(isZero(f)) {
        if(k==0) {
          return ZERO;  
        } else {
          c = add(c, mul(dk, LN2_LO)); 
          return add(mul(dk, LN2_HI), c);
        }
      }
      R = mul(hfsq, sub(ONE, div(scalbn(f, 2), THREE)));
      if(k==0) {
        return sub(f, R); 
      } else {
        return sub(mul(dk, LN2_HI), sub(sub(
                R, add(mul(dk, LN2_LO), c)), f));
      }
    }
    long s = div(f, add(TWO, f));
    long z = mul(s, s);
    R = mul(z, add(LP1, mul(z, add(LP2, mul(z, add(LP3, mul(
            z, add(LP4, mul(z, add(LP5, mul(z, add(LP6, mul(z, 
            LP7)))))))))))));
    if(k==0) {
      return sub(f, sub(hfsq, mul(s, add(hfsq, R))));
    } else {
      return sub(mul(dk, LN2_HI), sub(sub(hfsq, add(
              mul(s, add(hfsq, R)), add(mul(dk, LN2_LO), c))), f));
    }
  }
  
  /**
   * Mimics <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Math.html#sin(double)">Math.sin(double)</a>.
   */
  public static long sin(long d) {
    int ix = getHI(d) & 0x7fffffff;
    if (ix <= 0x3fe921fb) {
      // |x| ~< pi/4 
      return kernelSin(d, ZERO, 0);
    } else if (ix >= 0x7ff00000) {
      // sin(Inf or NaN) is NaN 
      return NaN;
    } else {
      // argument reduction needed 
      long[] y = new long[2];
      int n = remPio2(d, y);
      switch(n&3) {
        case 0:
          return  kernelSin(y[0], y[1], 1);
        case 1:
          return  kernelCos(y[0], y[1]);
        case 2:
          return negate(kernelSin(y[0], y[1], 1));
        default:
          return negate(kernelCos(y[0], y[1]));
      }
    }
  }

  /**
   * Mimics <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Math.html#cos(double)">Math.cos(double)</a>.
   */
  public static long cos(long d) {
    // High word of x.
    int ix = getHI(d) & 0x7fffffff;

    // |x| ~< pi/4
    if (ix <= 0x3fe921fb) {
      return kernelCos(d, ZERO);
    } else if (ix >= 0x7ff00000) {
      // cos(Inf or NaN) is NaN
      return NaN;
    } else {
      // argument reduction needed
      long y[] = new long[2];
      int n = remPio2(d,y);
      switch(n&3) {
        case 0: 
          return kernelCos(y[0],y[1]);
        case 1: 
          return negate(kernelSin(y[0],y[1],1));
        case 2: 
          return negate(kernelCos(y[0],y[1]));
        default:
          return kernelSin(y[0],y[1],1);
      }
    }
  }

  /**
   * Mimics <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Math.html#tan(double)">Math.tan(double)</a>.
   */
  public static long tan(long d) {
    // |x| ~< pi/4 
    int ix = getHI(d) & 0x7fffffff;
    if (ix <= 0x3fe921fb) {
      return kernelTan(d, ZERO, 1);
    } else if (ix >= 0x7ff00000) {
      // tan(Inf or NaN) is NaN
      return NaN;
    } else {
      // argument reduction needed 
      long[] y = new long [2];
      int n = remPio2(d, y);
      // 1 -- n even -1 -- n odd
      return kernelTan(y[0], y[1], 1-((n&1)<<1));
    }
  }
        
  private static final long PIO4    = 0x3fe921fb54442d18L; // 7.85398163397448278999e-01 
  private static final long PIO4_LO = 0x3c81a62633145c07L; // 3.06161699786838301793e-17 
  private static final long T0      = 0x3fd5555555555563L; // 3.33333333333334091986e-01 
  private static final long T1      = 0x3fc111111110fe7aL; // 1.33333333333201242699e-01
  private static final long T2      = 0x3faba1ba1bb341feL; // 5.39682539762260521377e-02 
  private static final long T3      = 0x3f9664f48406d637L; // 2.18694882948595424599e-02 
  private static final long T4      = 0x3f8226e3e96e8493L; // 8.86323982359930005737e-03 
  private static final long T5      = 0x3f6d6d22c9560328L; // 3.59207910759131235356e-03 
  private static final long T6      = 0x3f57dbc8fee08315L; // 1.45620945432529025516e-03 
  private static final long T7      = 0x3f4344d8f2f26501L; // 5.88041240820264096874e-04 
  private static final long T8      = 0x3f3026f71a8d1068L; // 2.46463134818469906812e-04 
  private static final long T9      = 0x3f147e88a03792a6L; // 7.81794442939557092300e-05 
  private static final long T10     = 0x3f12b80f32f0a7e9L; // 7.14072491382608190305e-05 
  private static final long T11     = 0xbef375cbdb605373L; // -1.85586374855275456654e-05 
  private static final long T12     = 0x3efb2a7074bf7ad4L; // 2.59073051863633712884e-05 
        
  private static long kernelTan(long x, long y, int iy) {
    int hx = getHI(x); // high word of x
    int ix = hx & 0x7fffffff;
    if (ix < 0x3e300000) { // x < 2**-28 
      if (intValue(x) == 0) {
        if (((ix | getLO(x)) | (iy + 1)) == 0) {
          return POSITIVE_INFINITY;
        } else if (iy == 1) {
          return x;
        } else {
          /* compute -1 / (x+y) carefully */
          long w = add(x, y);
          long z = setLO(w, 0);
          long v = sub(y, sub(z, x));
          long a = div(NEGATIVE_ONE, w);
          long t = setLO(a, 0);
          long s = add(ONE, mul(t, z));
          return add(t, mul(a, add(s, mul(t, v))));
        }
      }
    }
    if (ix>=0x3FE59428) {
      // |x|>=0.6744 
      if (hx<0) {
        x = negate(x);
        y = negate(y);
      }
      x = add(sub(PIO4, x), sub(PIO4_LO, y));
      y = ZERO;
    }
    long z = mul(x, x);
    long w = mul(z, z);

    // Break x^5*(T[1]+x^2*T[2]+...) into
    // x^5(T[1]+x^4*T[3]+...+x^20*T[11]) + 
    // x^5(x^2*(T[2]+x^4*T[4]+...+x^22*[T12]))
    long r = add(T1, mul(w, add(T3, mul(w, add(T5, mul(w, add(
            T7, mul(w, add(T9, mul(w, T11))))))))));
    long v = mul(z, add(T2, mul(w, add(T4, mul(w, add(T6, 
            mul(w, add(T8, mul(w, add(T10, mul(w, T12)))))))))));
    long s = mul(z, x);
    r = add(add(y, mul(z, add(mul(s, add(r, v)), y))), mul(T0, s));
    w = add(x, r);
    if (ix >= 0x3FE59428) {
      v = intToDouble(iy);
      return mul(intToDouble(1-((hx>>30)&2)), sub(v, mul(
              TWO, sub(x, sub(div(mul(w, w), add(
              w, v)), r)))));
    }
    if (iy == 1) {
      return w;
    } else { // if allow error up to 2 ulp, simply return -1.0/(x+r) here 
      // compute -1.0/(x+r) accurately 
      z = setLO(w, 0);
      v = sub(r, sub(z, x)); // z+v = r+x
      long a = div(NEGATIVE_ONE, w); // a = -1.0/w 
      long t = setLO(a, 0);
      s = add(ONE, mul(t, z));
      return add(t, mul(a, add(s, mul(t, v))));
    }
  }
        
  private static long S1 = 0xBFC5555555555549L; // -1.66666666666666324348e-01
  private static long S2 = 0x3F8111111110F8A6L; // 8.33333333332248946124e-03 
  private static long S3 = 0xBF2A01A019C161D5L; // -1.98412698298579493134e-04 
  private static long S4 = 0x3EC71DE357B1FE7DL; // 2.75573137070700676789e-06  
  private static long S5 = 0xBE5AE5E68A2B9CEBL; // -2.50507602534068634195e-08
  private static long S6 = 0x3DE5D93A5ACFD57CL; // 1.58969099521155010221e-10

  private static long kernelSin(long x, long y, int iy) {
    int ix = getHI(x) & 0x7fffffff; // high word of x
    if (ix < 0x3e400000) { // |x| < 2**-27 
      return x;
    }
    long z = mul(x, x);
    long v = mul(z, x);
    long r = add(S2, mul(z, add(S3, mul(z, add(S4, mul(z, add(S5,
            mul(z, S6))))))));
    if (iy == 0) {
      return add(x, mul(v, add(S1, mul(z, r))));
    }
    return sub(x, sub(sub(mul(z, 
            sub(mul(ONE_HALF, y), mul(v, r))), y), mul(v, S1)));
  }

  private static final long C1  = 0x3fa555555555554cL; //  4.16666666666666019037e-02 
  private static final long C2  = 0xbf56c16c16c15177L; // -1.38888888888741095749e-03 
  private static final long C3  = 0x3efa01a019cb1590L; //  2.48015872894767294178e-05 
  private static final long C4  = 0xbe927e4f809c52adL; // -2.75573143513906633035e-07 
  private static final long C5  = 0x3e21ee9ebdb4b1c4L; //  2.08757232129817482790e-09 
  private static final long C6  = 0xbda8fae9be8838d4L; // -1.13596475577881948265e-11 

  private static long kernelCos(long x, long y) {
    int ix = getHI(x) & 0x7fffffff; // ix = |x|'s high word
    if (ix < 0x3e400000) {
      // if x < 2**27
      return ONE;
    }
    long z = mul(x, x);
    long r = mul(z, add(C1, mul(z, add(C2, mul(z, add(C3, 
            mul(z, add(C4, mul(z, add(C5, mul(z, C6)))))))))));
    if (ix < 0x3FD33333) {
      // if |x| < 0.3
      return sub(ONE, sub(mul(ONE_HALF, z), sub(
              mul(z, r), mul(x, y))));
    }
    long qx;
    if (ix > 0x3fe90000) { // x > 0.78125
      qx = 0x3fd2000000000000L; // 0.28125
    } else {
      qx = set(ix-0x00200000, 0); // x/4 
    }
    long hz = sub(mul(ONE_HALF, z), qx);
    long a = sub(ONE, qx);
    return sub(a, (sub(hz, sub(mul(z, r), mul(x, y)))));
  }
        
  private static final long PIO2[] = {
          0x3ff921fb40000000L,  // 1.57079625129699707031e+00 
          0x3e74442d00000000L,  // 7.54978941586159635335e-08 
          0x3cf8469880000000L,  // 5.39030252995776476554e-15 
          0x3b78cc5160000000L,  // 3.28200341580791294123e-22 
          0x39f01b8380000000L,  // 1.27065575308067607349e-29 
          0x387a252040000000L,  // 1.22933308981111328932e-36 
          0x36e3822280000000L,  // 2.73370053816464559624e-44 
          0x3569f31d00000000L   // 2.16741683877804819444e-51 
  };
  
   // Table of constants for 2/pi, 396 Hex digits (476 decimal) of 2/pi 
  private static final int[] TWO_OVER_PI = {
          0xa2f983, 0x6e4e44, 0x1529fc, 0x2757d1, 0xf534dd, 0xc0db62, 
          0x95993c, 0x439041, 0xfe5163, 0xabdebb, 0xc561b7, 0x246e3a, 
          0x424dd2, 0xe00649, 0x2eea09, 0xd1921c, 0xfe1deb, 0x1cb129, 
          0xa73ee8, 0x8235f5, 0x2ebb44, 0x84e99c, 0x7026b4, 0x5f7e41, 
          0x3991d6, 0x398353, 0x39f49c, 0x845f8b, 0xbdf928, 0x3b1ff8, 
          0x97ffde, 0x05980f, 0xef2f11, 0x8b5a0a, 0x6d1f6d, 0x367ecf, 
          0x27cb09, 0xb74f46, 0x3f669e, 0x5fea2d, 0x7527ba, 0xc7ebe5, 
          0xf17b3d, 0x0739f7, 0x8a5292, 0xea6bfb, 0x5fb11f, 0x8d5d08, 
          0x560330, 0x46fc7b, 0x6babf0, 0xcfbc20, 0x9af436, 0x1da9e3, 
          0x91615e, 0xe61b08, 0x659985, 0x5f14a0, 0x68408d, 0xffd880, 
          0x4d7327, 0x310606, 0x1556ca, 0x73a8c9, 0x60e27b, 0xc08c6b};
          
  private static final int[] NPIO2_HW = {
          0x3ff921fb, 0x400921fb, 0x4012d97c, 0x401921fb, 0x401f6a7a, 0x4022d97c,
          0x4025fdbb, 0x402921fb, 0x402c463a, 0x402f6a7a, 0x4031475c, 0x4032d97c,
          0x40346b9c, 0x4035fdbb, 0x40378fdb, 0x403921fb, 0x403ab41b, 0x403c463a,
          0x403dd85a, 0x403f6a7a, 0x40407e4c, 0x4041475c, 0x4042106c, 0x4042d97c,
          0x4043a28c, 0x40446b9c, 0x404534ac, 0x4045fdbb, 0x4046c6cb, 0x40478fdb,
          0x404858eb, 0x404921fb};
          
  private static final long TWO24    = 0x4170000000000000L; // 1.67772160000000000000e+07 

  private static final long INV_PIO2 = 0x3fe45f306dc9c883L; // 6.36619772367581382433e-01 53 bits of 2/pi 
  
  private static final long PIO2_1   = 0x3ff921fb54400000L; // 1.57079632673412561417e+00 first  33 bit of pi/2 
  private static final long PIO2_1T  = 0x3dd0b4611a626331L; // 6.07710050650619224932e-11 pi/2 - pio2_1 
  private static final long PIO2_2   = 0x3dd0b4611a600000L; // 6.07710050630396597660e-11 second 33 bit of pi/2 
  private static final long PIO2_2T  = 0x3ba3198a2e037073L; // 2.02226624879595063154e-21 pi/2 - (pio2_1+pio2_2) 
  private static final long PIO2_3   = 0x3ba3198a2e000000L; // 2.02226624871116645580e-21 third  33 bit of pi/2 
  private static final long PIO2_3T  = 0x397b839a252049c1L; // 8.47842766036889956997e-32 pi/2 - (pio2_1+pio2_2+pio2_3) 

  // Return the remainder of x % pi/2 in y[0]+y[1].  This is rather complex.
  // Perhaps it would make sense to do something simpler.
  private static int remPio2(long x, long[] y) {
    int hx = getHI(x); // high word of x
    int ix = hx&0x7fffffff;
    if (ix <= 0x3fe921fb) {
      // |x| ~<= pi/4 , no need for reduction 
      y[0] = x;
      y[1] = ZERO;
      return 0;
    }

    if (ix < 0x4002d97c) {
      // |x| < 3pi/4, special case with n=+-1
      long a = PIO2_1;
      long b = (ix == 0x3ff921fb) ? PIO2_2T : PIO2_1T;
      if (hx > 0) {
        a = negate(a);
        b = negate(b);
      }
      long z = add(x, a);
      y[0] = add(z, b);
      y[1] = add(sub(z, y[0]), b);
      return (hx > 0) ? 1 : -1;
    }

    if (ix <= 0x413921fb) { // |x| ~<= 2^19*(pi/2), medium size 
      long t = abs(x);
      long fn = rint(mul(t, INV_PIO2));
      int n = intValue(fn);
      long r = sub(t, mul(fn, PIO2_1));
      long w = mul(fn, PIO2_1T); // 1st round good to 85 bit
      if ((n < 32) && (ix != NPIO2_HW[n-1])) {
        y[0] = sub(r, w); // quick check no cancellation 
      } else {
        int j = ix >> 20;
        y[0] = sub(r, w);
        int i = j - (((getHI(y[0])) >> 20) & 0x7ff);
        if (i > 16) { // 2nd iteration needed, good to 118 
          t  = r;
          w  = mul(fn, PIO2_2);
          r  = sub(t, w);
          w = sub(mul(fn, PIO2_2T), sub(sub(t, r), w));
          y[0] = sub(r, w);
          i = j - (((getHI(y[0])) >> 20) & 0x7ff);
          if (i > 49) { // 3rd iteration need, 151 bits acc
            t  = r;     // will cover all possible cases
            w  = mul(fn, PIO2_3);
            r  = sub(t, w);
            w = sub(mul(fn, PIO2_3T), sub(sub(t, r), w));
            y[0] = sub(r, w);
          }
        }
      }
      y[1] = sub(sub(r, y[0]), w);
      if (hx < 0) {
        y[0] = negate(y[0]);
        y[1] = negate(y[1]);
        return -n;
      } else {
        return n;
      }
    }

    // all other (large) arguments
    if (ix >= 0x7ff00000) {
      // x is inf or NaN 
      y[0] = y[1] = NaN;
      return 0;
    }
    // set z = scalbn(|x|,ilogb(x)-23)
    long z = getLO(x);
    int e0 = (int) ((ix >> 20) - 1046);
    z = setHI(z, ix - (e0 << 20));
    long[] tx = new long[3];
    for (int i=0; i<2; i++) {
      tx[i] = intToDouble(intValue(z));
      z     = scalbn(sub(z, tx[i]), 24);
    }
    tx[2] = z;
    int nx = 3;
    while (isZero(tx[nx-1])) nx--; // skip zero term 
    int n = kernelRemPio2(tx, y, e0, nx);
    if (hx < 0) {
      y[0] = negate(y[0]);
      y[1] = negate(y[1]);
      return -n;
    }
    return n;
  }

  // Work even harder to compute mod pi/2.  This is extremely complex.
  // Perhaps it would make sense to do something simpler.
  private static int kernelRemPio2(long[] x, long[] y, int e0, int nx) {
    // initialize jk
    int jk = 4;
    int jp = jk;

    // determine jx,jv,q0, note that 3>q0 
    int jx =  nx - 1;
    int jv = (e0-3)/24;
    if (jv < 0)  jv = 0;
    int q0 =  e0-24*(jv+1);

    // set up f[0] to f[jx+jk] where f[jx+jk] = two_over_pi[jv+jk]
    int j = jv - jx;
    int m = jx + jk;
    long[] f = new long[20];
    for (int i=0; i<=m; i++, j++) {
      f[i] = (j<0) ? ZERO : intToDouble(TWO_OVER_PI[j]);
    }

    // compute q[0],q[1],...q[jk]
    long[] q = new long[20];
    for (int i=0; i<=jk; i++) {
      long fw = ZERO;
      for (j=0; j<=jx; j++) {
        fw = add(fw, mul(x[j], f[jx+i-j]));
      }
      q[i] = fw;
    }
    
    int jz = jk;

    int n, ih;
    long z;
    int[] iq = new int[20];
    boolean recompute;
    do {
      recompute = false;
      // distill q[] into iq[] reversingly 
      int i;
      for (i=0, j=jz, z=q[jz]; j>0; i++, j--) {
        long fw = intToDouble(intValue(scalbn(z, -24)));
        iq[i] = intValue(sub(z, scalbn(fw, 24)));
        z = add(q[j-1], fw);
      }

      // compute n
      z = scalbn(z, q0); // actual value of z
      z = sub(z, scalbn(floor(scalbn(z, -3)), 3)); // trim off integer >= 8
      z = sub(z, mul(EIGHT, floor(mul(z, ONE_EIGHTH)))); // trim off integer >= 8
      n = intValue(z);
      z = sub(z, intToDouble(n));
      ih = 0;
      if (q0 > 0) { // need iq[jz-1] to determine n
        i  = (iq[jz-1]>>(24-q0));
        n += i;
        iq[jz-1] -= i<<(24-q0);
        ih = iq[jz-1]>>(23-q0);
      } else if (q0==0) {
        ih = iq[jz-1]>>23;
      } else if(ge(z, ONE_HALF)) {
        ih=2;
      }
      if (ih>0) { // q > 0.5
        n += 1;
        int carry = 0;
        for(i=0; i<jz; i++) { // compute 1-q 
          j = iq[i];
          if (carry == 0) {
            if (j != 0) {
              carry = 1;
              iq[i] = 0x1000000- j;
            }
          } else  iq[i] = 0xffffff - j;
        }
        if (q0 > 0) { // rare case: chance is 1 in 12
          switch (q0) {
          case 1:
            iq[jz-1] &= 0x7fffff; break;
          case 2:
            iq[jz-1] &= 0x3fffff; break;
          }
        }
        if (ih == 2) {
          z = sub(ONE, z);
          if (carry != 0) {
            z = sub(z, scalbn(ONE,q0));
          }
        }
      }
      
      // check if recomputation is needed 
      if (isZero(z)) {
        j = 0;
        for (i = jz-1;  i >= jk;  i--)
          j |= iq[i];
        if (j == 0) { // need recomputation
          int k;
          for (k=1; iq[jk-k]==0; k++); // k = no. of terms needed
          for (i=jz+1; i<=jz+k; i++) { // add q[jz+1] to q[jz+k]
            f[jx+i] = intToDouble(TWO_OVER_PI[jv+i]);
            long fw = ZERO;
            for (j=0; j<=jx; j++)
              fw = add(fw, mul(x[j], f[jx+i-j]));
            q[i] = fw;
          }
          jz += k;
          recompute = true;
        }
      }
    } while (recompute);

    // chop off zero terms 
    if (isZero(z)) {
      jz--;
      q0 -= 24;
      while (iq[jz] == 0) {
        jz--;
        q0 -= 24;
      }
    } else { // break z into 24-bit if necessary
      z = scalbn(z,-q0);
      if (ge(z, TWO24)) {
        long fw = intToDouble(intValue(scalbn(z, -24)));
        iq[jz] = intValue(sub(z, scalbn(fw, 24)));
        jz++;
        q0 += 24;
        iq[jz] = intValue(fw);
      } else iq[jz] = intValue(z);
    }

    // convert integer "bit" chunk to floating-point value
    long fw = scalbn(ONE, q0);
    for (int i=jz; i >= 0; i--) {
      q[i] = mul(fw, intToDouble(iq[i]));
      fw = scalbn(fw, -24);
    }

    // compute PIo2[0,...,jp]*q[jz,...,0] 
    long[] fq = new long[20];
    for (int i=jz; i>=0; i--) {
      fw = ZERO;
      for (int k=0; (k<=jp) && (k<=(jz-i)); k++)
        fw = add(fw, mul(PIO2[k], q[i+k]));
      fq[jz-i] = fw;
    }

    // compress fq[] into y[]
    fw = ZERO;
    for (int i=jz; i>=0; i--)
      fw = add(fw, fq[i]);
    y[0] = (ih==0)? fw : negate(fw);
    fw = sub(fq[0], fw);
    for (int i=1; i<=jz; i++)
      fw = add(fw, fq[i]);
    y[1] = (ih==0) ? fw: negate(fw); 
    return n&7;
  }
  
  private static final long PIO2_HI = 0x3FF921FB54442D18L;  // 1.57079632679489655800e+00
  private static final long PIO2_LO = 0x3C91A62633145C07L;  // 6.12323399573676603587e-17 
  private static final long PIO4_HI = 0x3FE921FB54442D18L;  // 7.85398163397448278999e-01
  // coefficient for R(x^2) 
  private static final long PS0     = 0x3fc5555555555555L;  //  1.66666666666666657415e-01 
  private static final long PS1     = 0xbfd4d61203eb6f7dL;  // -3.25565818622400915405e-01 
  private static final long PS2     = 0x3fc9c1550e884455L;  //  2.01212532134862925881e-01 
  private static final long PS3     = 0xbfa48228b5688f3bL;  // -4.00555345006794114027e-02 
  private static final long PS4     = 0x3f49efe07501b288L;  //  7.91534994289814532176e-04 
  private static final long PS5     = 0x3f023de10dfdf709L;  //  3.47933107596021167570e-05 
  private static final long QS1     = 0xc0033a271c8a2d4bL;  // -2.40339491173441421878e+00 
  private static final long QS2     = 0x40002ae59c598ac8L;  //  2.02094576023350569471e+00 
  private static final long QS3     = 0xbfe6066c1b8d0159L;  // -6.88283971605453293030e-01 
  private static final long QS4     = 0x3fb3b8c5b12e9282L;  //  7.70381505559019352791e-02 

  private static long pOverQ(long t) {
    return div(mul(t, add(PS0, mul(t, add(PS1, mul(t, add(PS2,
            mul(t, add(PS3, mul(t, add(PS4, mul(t, PS5))))))))))),
            add(ONE, mul(t, add(QS1, mul(t, add(QS2, mul(t,  
            add(QS3, mul(t, QS4)))))))));
  }

  /**
   * Mimics <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Math.html#asin(double)">Math.asin(double)</a>.
   */
  public static final long asin(long d) {
    int hx = getHI(d);
    int ix = hx & 0x7fffffff;
    if (ix>= 0x3ff00000) { // |x|>= 1 
      if(((ix-0x3ff00000)|getLO(d))==0)
        // asin(1)=+-pi/2 with inexact
        return copySign(PIO2_HI, d);
      return NaN;  // asin(|x|>1) is NaN
    } else if (ix<0x3fe00000) { // |x|<0.5 
      if (ix<0x3e400000) { // if |x| < 2**-27
        return d;
      }
      long t = mul(d, d);
      long w = pOverQ(t);
      return add(d, mul(d, w));
    }
    // 1> |x|>= 0.5
    long w = sub(ONE, abs(d));
    long t = scalbn(w, -1);
    long s = sqrt(t);
    if(ix>=0x3FEF3333) { // if |x| > 0.975
      w = pOverQ(t);
      t = sub(PIO2_HI, sub(scalbn(add(s, mul(s, w)), 1), 
              PIO2_LO));
    } else {
      w  = setLO(s, 0);
      long c = div(sub(t, mul(w, w)), add(s, w));
      long r = pOverQ(t);
      long p = sub(mul(scalbn(s, 1), r), sub(PIO2_LO,
              scalbn(c, 1)));
      long q = sub(PIO4_HI, scalbn(w, 1));
      t = sub(PIO4_HI, sub(p, q));
    }    
    return ((hx>0) ? t : negate(t));
  }

  /**
   * Mimics <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Math.html#acos(double)">Math.acos(double)</a>.
   */
  public static long acos(long d) {
    int hx = getHI(d);
    int ix = hx&0x7fffffff;
    if (ix >= 0x3ff00000) { // |x| >= 1 
      if (((ix-0x3ff00000)|getLO(d)) == 0) { // |x|==1
        if (hx>0) 
          return ZERO;
        else
          return PI; // acos(-1)= pi
      }
      return NaN; // acos(|x|>1) is NaN
    }
    if (ix < 0x3fe00000) { // |x| < 0.5
      if (ix <= 0x3c600000)
        return PIO2_HI; // if|x|<2**-57
      long z = mul(d, d);
      long r = pOverQ(z);
      return sub(PIO2_HI, sub(d, sub(PIO2_LO, mul(d, r))));
    } else if (hx<0) { // x < -0.5 
      long z = scalbn(add(ONE, d), -1);
      long s = sqrt(z);
      long r = pOverQ(z);
      long w = sub(mul(r, s), PIO2_LO);
      return sub(PI, scalbn(add(s, w), 1));
    } else { // x > 0.5 
      long z = scalbn(sub(ONE, d), -1);
      long s = sqrt(z);
      long df = setLO(s, 0);
      long c = div(sub(z, mul(df, df)), add(s, df));
      long r = pOverQ(z);
      long w = add(mul(r, s), c);
      return scalbn(add(df, w), 1);
    }
  }
  
  private static final long atanhi[] = {
          0x3fddac670561bb4fL,  // 4.63647609000806093515e-01 atan(0.5)hi 
          0x3fe921fb54442d18L,  // 7.85398163397448278999e-01 atan(1.0)hi
          0x3fef730bd281f69bL,  // 9.82793723247329054082e-01 atan(1.5)hi
          0x3ff921fb54442d18L   // 1.57079632679489655800e+00 atan(inf)hi
  };

  private static final long atanlo[] = {
          0x3c7a2b7f222f65e2L,  // 2.26987774529616870924e-17 atan(0.5)lo
          0x3c81a62633145c07L,  // 3.06161699786838301793e-17 atan(1.0)lo
          0x3c7007887af0cbbdL,  // 1.39033110312309984516e-17 atan(1.5)lo  
          0x3c91a62633145c07L   // 6.12323399573676603587e-17 atan(inf)lo  
  };

  private static final long AT0  = 0x3fd555555555550dL; //  3.33333333333329318027e-01 
  private static final long AT1  = 0xbfc999999998ebc4L; // -1.99999999998764832476e-01 
  private static final long AT2  = 0x3fc24924920083ffL; //  1.42857142725034663711e-01 
  private static final long AT3  = 0xbfbc71c6fe231671L; // -1.11111104054623557880e-01 
  private static final long AT4  = 0x3fb745cdc54c206eL; //  9.09088713343650656196e-02 
  private static final long AT5  = 0xbfb3b0f2af749a6dL; // -7.69187620504482999495e-02 
  private static final long AT6  = 0x3fb10d66a0d03d51L; //  6.66107313738753120669e-02 
  private static final long AT7  = 0xbfadde2d52defd9aL; // -5.83357013379057348645e-02 
  private static final long AT8  = 0x3fa97b4b24760debL; //  4.97687799461593236017e-02 
  private static final long AT9  = 0xbfa2b4442c6a6c2fL; // -3.65315727442169155270e-02 
  private static final long AT10 = 0x3f90ad3ae322da11L; //  1.62858201153657823623e-02 
        
  /**
   * Mimics <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Math.html#atan(double)">Math.atan(double)</a>.
   */
  public static long atan(long d) {
    int hx = getHI(d);
    int ix = hx&0x7fffffff;
    int id;
    if(ix>=0x44100000) { // if |x| >= 2^66 
      if(ix>0x7ff00000||
        (ix==0x7ff00000&&(getLO(d)!=0)))
        return NaN;
      return (hx > 0) ? atanhi[3] : negate(atanhi[3]);
    } if (ix < 0x3fdc0000) { // |x| < 0.4375
      if (ix < 0x3e200000) { // |x| < 2^-29 
        return d;
      }
      id = -1;
    } else {
      d = abs(d);
      if (ix < 0x3ff30000) { // |x| < 1.1875 
        if (ix < 0x3fe60000) { // 7/16 <=|x|<11/16 
          id = 0;
          d = div(sub(scalbn(d, 1), ONE), add(TWO, d));
        } else { // 11/16<=|x|< 19/16 
          id = 1;
          d = div(sub(d, ONE), add(d, ONE));
        }
      } else {
        if (ix < 0x40038000) { // |x| < 2.4375 
          id = 2;
          d = div(sub(d, THREE_HALVES), add(ONE,
                  mul(THREE_HALVES, d)));
        } else { // 2.4375 <= |x| < 2^66 
          id = 3;
          d = div(NEGATIVE_ONE, d);
        }
      }
    }
    // end of argument reduction 
    long z = mul(d, d);
    long w = mul(z, z);
    // break sum from i=0 to 10 aT[i]z**(i+1) into odd and even poly 
    long s1 = mul(z, add(AT0, mul(w, add(AT2, mul(w, add(AT4, 
            mul(w, add(AT6, mul(w, add(AT8, mul(w, AT10)))))))))));
    long s2 = mul(w, add(AT1, mul(w, add(AT3, mul(w, add(AT5,
            mul(w, add(AT7, mul(w, AT9)))))))));
    if (id<0) {
      return sub(d, mul(d, add(s1, s2)));
    } else {
      z = sub(atanhi[id], sub(sub(mul(d, add(s1, s2)), 
              atanlo[id]), d));
      return (hx < 0) ? negate(z): z;
    }
  }
  
  /**
   * Returns the hyperbolic cosine of an angle.
   *
   * @param   d   an angle, in radians.
   * @return  the hyperbolic cosine of the argument.
   */
  public static long cosh(long d) {
    if (isNaN(d)) {
      return NaN;
    } else if (isInfinite(d)) {
      return POSITIVE_INFINITY;
    }
    
    int ix = getHI(d) & 0x7fffffff;
 
    // |x| in [0,0.5*ln2], return 1+expm1(|x|)^2/(2*exp(|x|)) 
    if(ix<0x3fd62e43) {
      long t = expm1(abs(d));
      long w = add(ONE, t);
      if (ix<0x3c800000) return w; // cosh(tiny) = 1 
      return add(ONE, div(mul(t, t), add(w, w)));
    }

    // |x| in [0.5*ln2,22], return (exp(|x|)+1/exp(|x|)/2;
    if (ix < 0x40360000) {
      long t = exp(abs(d));
      return add(scalbn(t, -1), div(ONE_HALF, t));
    }

    // |x| in [22, log(maxdouble)] return half*exp(|x|) 
    if (ix < 0x40862E42)  return scalbn(exp(abs(d)), -1);

    // |x| in [log(maxdouble), overflowthresold]
    if (abs(d) <= 0x408633CE8fb9f87dL) {
      long w = exp(scalbn(abs(d), -1));
      long t = scalbn(w, -1);
      return mul(t, w);
    }

    // |x| > overflowthresold, cosh(x) overflow 
    return POSITIVE_INFINITY;
  }

  /**
   * Returns the hyperbolic sine of an angle.
   *
   * @param   d   an angle, in radians.
   * @return  the hyperbolic sine of the argument.
   */
  public static long sinh(long d) {
    if (isNaN(d)) {
      return NaN;
    } else if (isInfinite(d)) {
      return POSITIVE_INFINITY;
    }
    
    int jx = getHI(d);
    int ix = jx & 0x7fffffff;
    long h = ONE_HALF;
    if (jx < 0) h = negate(h);

    // |x| in [0,22], return sign(x)*0.5*(E+E/(E+1))) 
    if (ix < 0x40360000) { // |x|<22 
      if (ix<0x3e300000) // |x|<2**-28
        return d; // sinh(tiny) = tiny with inexact
      long t = expm1(abs(d));
      if(ix<0x3ff00000) return mul(h, sub(scalbn(t, 1), mul(t, 
              div(t, add(t, ONE)))));
      return mul(h, add(t, div(t, add(t, ONE))));
    }

    // |x| in [22, log(maxdouble)] return 0.5*exp(|x|) 
    if (ix < 0x40862E42)  return mul(h, exp(abs(d)));

    // |x| in [log(maxdouble), overflowthresold] 
    if (abs(d) <= 0x408633CE8fb9f87dL) {
      long w = exp(scalbn(abs(d), -1));
      long t = mul(h, w);
      return mul(t, w);
    }

    // |x| > overflowthresold, sinh(x) overflow 
    return copySign(POSITIVE_INFINITY, d);
  }

  /**
   * Returns the hyperbolic tangent of an angle.
   *
   * @param   d   an angle, in radians.
   * @return  the hyperbolic tangent of the argument.
   */
  public static long tanh(long d) {
    if (isNaN(d)) {
      return NaN;
    } else if (isInfinite(d)) {
      return copySign(POSITIVE_INFINITY, d);
    }
    
    int jx = getHI(d);
    int ix = jx & 0x7fffffff;

    // |x| < 22 
    long z;
    if (ix < 0x40360000) { // |x|<22 
        if (ix<0x3c800000) // |x|<2**-55 
          return d; // tanh(small) = small 
        if (ix>=0x3ff00000) { // |x|>=1  
          long t = expm1(scalbn(abs(d), 1));
          z = sub(ONE, div(TWO, add(t, TWO)));
        } else {
          long t = expm1(negate(mul(TWO, abs(d))));
          z = negate(div(t, add(t, TWO)));
        }
    // |x| > 22, return +-1 
    } else {
      z = ONE;
    }
    return (jx>=0) ? z : negate(z);
  }
  
  /**
   * Returns the arc hyperbolic cosine of an angle.
   *
   * @param   d   the value whose arc hyperbolic cosine is to be returned.
   * @return  the arc hyperbolic cosine of the argument.
   */
  public static long acosh(long d) {
    if (isNaN(d) || lt(d, ONE)) {
      return NaN;
    } else if (d == POSITIVE_INFINITY) {
      return d;
    } else if (d == ONE) {
      return ZERO;
    }
    int hx = getHI(d);
    if (hx > 0x41b00000) {
      return add(log(d), LN2);
    } else if (hx > 0x40000000) {
      long t = mul(d, d);
      return log(sub(scalbn(d, 1), div(ONE, add(d, 
              sqrt(sub(t, ONE))))));
    }
    long t = sub(d, ONE);
    return log1p(add(t, sqrt(add(scalbn(t, 1), 
            mul(t, t)))));
  }

  /**
   * Returns the arc hyperbolic sine of an angle.
   *
   * @param   d   the value whose arc hyperbolic sine is to be returned.
   * @return  the arc hyperbolic sine of the argument.
   */
  public static long asinh(long d) {
    int hx = getHI(d);
    int ix = hx&0x7fffffff;
    if(ix>=0x7ff00000) return d; // x is inf or NaN 
    if(ix< 0x3e300000) { // |x|<2**-28
      return d;
    } 
    long w;
    if(ix>0x41b00000) {	/* |x| > 2**28 */
      w = add(log(abs(d)), LN2);
    } else if (ix>0x40000000) {	/* 2**28 > |x| > 2.0 */
      long t = abs(d);
      w = log(add(scalbn(t, 1), div(ONE, add(sqrt(add(
              mul(d, d), ONE)), t))));
    } else {		/* 2.0 > |x| > 2**-28 */
      long t = mul(d, d);
      w = log1p(add(abs(d), div(t, add(ONE, sqrt(add(
              ONE, t))))));
    }
    if(hx>0) return w; else return negate(w);
  }
  
  /**
   * Returns the arc hyperbolic tangent of an angle.
   *
   * @param   d   the value whose arc hyperbolic tangent is to be returned.
   * @return  the arc hyperbolic tangent of the argument.
   */
  public static long atanh(long d) {
    if (isNaN(d) || gt(d, ONE) || lt(d, NEGATIVE_ONE)) {
      return NaN;
    } 
    boolean negate = unpackSign(d);
    d = abs(d);
    if (d == ONE) {
      d = POSITIVE_INFINITY;
    } else if (lt(d, ONE_HALF)) {
      long t = add(d, d);
      d = scalbn(log1p(add(t, div(mul(t, d), sub(ONE, d)))), -1);
    } else {
      d = scalbn(log1p(div(add(d, d), sub(ONE, d))), -1);
    }
    if (negate) {
      d = negate(d);
    }
    return d;
  }
  
  /**
   * Returns the difference of d2 and d1, expressed as a percentage of d1.
   *
   * @param   d1   the "starting" value 
   * @param   d2   the "final" value
   * @return  100 * ((d2 - d1) / d1)
   */
  public static long percentChange(long d1, long d2) {
    return mul(div(sub(d2, d1), d1), ONE_HUNDRED);
  }
  
  /**
   * Returns d2 expressed as a percentage of d1
   *
   * @param   d1   the "base" value 
   * @param   d2   the other value
   * @return  100 * (d2 / d1)
   */
  public static long percentTotal(long d1, long d2) {
    return mul(div(d2, d1), ONE_HUNDRED);
  }

  /**
   * Returns the factorial of d.  If d is not a mathematical integer greater 
   * than or equal to zero, the return value is NaN.  Use the gamma function
   * for non-integer values.
   * <p>
   * This is a naive implentation.  TODO: make this better.
   *
   * @param d a <code>double</code> value.
   * @return d! 
   */
  public static long factorial(long d) {
    if (isZero(d)) {
      return ONE;
    } else if (! isPositiveInteger(d)) {
      return NaN;
    }
    return factorial(ONE, d, ONE);
  }
  
  private static boolean isPositiveInteger(long d) {
    return (! (unpackSign(d) || isNaN(d) || isInfinite(d) || isZero(d) 
            || (rint(d) != d)));
  }
  
  private static long factorial(long base, long d1, long d2) {
    while ((d1 != d2) && (base != POSITIVE_INFINITY)) {
      base = mul(base, d1);
      d1 = add(d1, NEGATIVE_ONE);
    }
    return base;
  }
  
  /**
   * Return the number of ways of obtaining an ordered subset of d2 elements 
   * from a set of d1 elements.  If d1 and d2 are not mathematical integers
   * than or equal to zero, or if d1 is less than d2, the return value is NaN.
   *
   * @param d1 a <code>double</code> value
   * @param d2 a <code>double</code> value
   * @return d1! / (d1 - d2)!
   * @see #factorial(long)
   * @see #combinations(long, long)
   */
  public static long permutations(long d1, long d2) {
    if (! (isPositiveInteger(d1) && isPositiveInteger(d2) && ge(d1, d2))) {
      return NaN;
    }
    return factorial(ONE, d1, sub(d1, d2));
  }

  /**
   * Return the number of ways of obtaining an unordered subset of d2 elements 
   * from a set of d1 elements.  Also known as the binomial coefficient.
   * If d1 and d2 are not mathematical integers greater
   * than or equal to zero, or if d1 is less than d2, the return value is NaN.
   *
   * @param d1 a <code>double</code> value
   * @param d2 a <code>double</code> value
   * @return d1! / (d2! * (d1 - d2)!)
   * @see #factorial(long)
   * @see #permutations(long, long)
   */
  public static long combinations(long d1, long d2) {
    if (! (isPositiveInteger(d1) && isPositiveInteger(d2) && ge(d1, d2))) {
      return NaN;
    }
    long d3 = sub(d1, d2);
    if (gt(d3, d2)) {
      long tmp = d3;
      d3 = d2;
      d2 = tmp;
    }
    if (isZero(d3)) {
      d3 = ONE;
    } else {
      d3 = div(ONE, factorial(ONE, d3, ONE));
    }
    return factorial(d3, d1, d2);
  }

  
  /**
   * Returns the complete gamma function of d.  
   *
   * @param d a <code>double</code> value
   * @return Gamma(d)
   * @see #lgamma(long)
   */
  public static long gamma(long d) {
    if (isNaN(d) || isZero(d) 
        || (lt(d, ZERO) && ((d == NEGATIVE_INFINITY) || (rint(d) == d)))) {
      return NaN;
    }
    return lgamma(d, true);
  }
  
  /**
   * Returns the natural logarithm of the absolute value of the gamma function
   * of d.
   *
   * @param d a <code>double</code> value
   * @return Log(|Gamma(d)|)
   * @see #gamma(long)
   */
  public static long lgamma(long d) {
    return lgamma(d, false);
  }
  
  private static final long A0  = 0x3FB3C467E37DB0C8L; // 7.72156649015328655494e-02
  private static final long A1  = 0x3FD4A34CC4A60FADL; // 3.22467033424113591611e-01
  private static final long A2  = 0x3FB13E001A5562A7L; // 6.73523010531292681824e-02
  private static final long A3  = 0x3F951322AC92547BL; // 2.05808084325167332806e-02
  private static final long A4  = 0x3F7E404FB68FEFE8L; // 7.38555086081402883957e-03
  private static final long A5  = 0x3F67ADD8CCB7926BL; // 2.89051383673415629091e-03
  private static final long A6  = 0x3F538A94116F3F5DL; // 1.19270763183362067845e-03
  private static final long A7  = 0x3F40B6C689B99C00L; // 5.10069792153511336608e-04
  private static final long A8  = 0x3F2CF2ECED10E54DL; // 2.20862790713908385557e-04
  private static final long A9  = 0x3F1C5088987DFB07L; // 1.08011567247583939954e-04
  private static final long A10 = 0x3EFA7074428CFA52L; // 2.52144565451257326939e-05
  private static final long A11 = 0x3F07858E90A45837L; // 4.48640949618915160150e-05
  private static final long TC = 0x3FF762D86356BE3FL; // 1.46163214496836224576e+00
  private static final long TF = 0xBFBF19B9BCC38A42L; // -1.21486290535849611461e-01
  // tt = -(tail of tf)
  private static final long TT = 0xBC50C7CAA48A971FL; // -3.63867699703950536541e-18
  private static final long TB0 = 0x3FDEF72BC8EE38A2L; // 4.83836122723810047042e-01
  private static final long TB1 = 0xBFC2E4278DC6C509L; // -1.47587722994593911752e-01
  private static final long TB2 = 0x3FB08B4294D5419BL; // 6.46249402391333854778e-02
  private static final long TB3 = 0xBFA0C9A8DF35B713L; // -3.27885410759859649565e-02
  private static final long TB4 = 0x3F9266E7970AF9ECL; // 1.79706750811820387126e-02
  private static final long TB5 = 0xBF851F9FBA91EC6AL; // -1.03142241298341437450e-02
  private static final long TB6 = 0x3F78FCE0E370E344L; // 6.10053870246291332635e-03
  private static final long TB7 = 0xBF6E2EFFB3E914D7L; // -3.68452016781138256760e-03
  private static final long TB8 = 0x3F6282D32E15C915L; // 2.25964780900612472250e-03
  private static final long TB9 = 0xBF56FE8EBF2D1AF1L; // -1.40346469989232843813e-03
  private static final long TB10 = 0x3F4CDF0CEF61A8E9L; // 8.81081882437654011382e-04
  private static final long TB11 = 0xBF41A6109C73E0ECL; // -5.38595305356740546715e-04
  private static final long TB12 = 0x3F34AF6D6C0EBBF7L; // 3.15632070903625950361e-04
  private static final long TB13 = 0xBF347F24ECC38C38L; // -3.12754168375120860518e-04
  private static final long TB14 = 0x3F35FD3EE8C2D3F4L; // 3.35529192635519073543e-04
  private static final long U0  = 0xBFB3C467E37DB0C8L; // -7.72156649015328655494e-02
  private static final long U1  = 0x3FE4401E8B005DFFL; // 6.32827064025093366517e-01
  private static final long U2  = 0x3FF7475CD119BD6FL; // 1.45492250137234768737e+00
  private static final long U3  = 0x3FEF497644EA8450L; // 9.77717527963372745603e-01
  private static final long U4  = 0x3FCD4EAEF6010924L; // 2.28963728064692451092e-01
  private static final long U5  = 0x3F8B678BBF2BAB09L; // 1.33810918536787660377e-02
  private static final long V1  = 0x4003A5D7C2BD619CL; // 2.45597793713041134822e+00, /* 
  private static final long V2  = 0x40010725A42B18F5L; // 2.12848976379893395361e+00, /* 
  private static final long V3  = 0x3FE89DFBE45050AFL; // 7.69285150456672783825e-01, /* 
  private static final long V4  = 0x3FBAAE55D6537C88L; // 1.04222645593369134254e-01, /* 
  private static final long V5  = 0x3F6A5ABB57D0CF61L; // 3.21709242282423911810e-03, /* 
  private static final long SB0  = 0xBFB3C467E37DB0C8L; // 7.72156649015328655494e-02, /* 
  private static final long SB1  = 0x3FCB848B36E20878L; // 2.14982415960608852501e-01, /* 
  private static final long SB2  = 0x3FD4D98F4F139F59L; // 3.25778796408930981787e-01, /* 
  private static final long SB3  = 0x3FC2BB9CBEE5F2F7L; // 1.46350472652464452805e-01, /* 
  private static final long SB4  = 0x3F9B481C7E939961L; // 2.66422703033638609560e-02, /* 
  private static final long SB5  = 0x3F5E26B67368F239L; // 1.84028451407337715652e-03, /* 
  private static final long SB6  = 0x3F00BFECDD17E945L; // 3.19475326584100867617e-05, /* 
  private static final long R1  = 0x3FF645A762C4AB74L; // 1.39200533467621045958e+00, /* 
  private static final long R2  = 0x3FE71A1893D3DCDCL; // 7.21935547567138069525e-01, /* 
  private static final long R3  = 0x3FC601EDCCFBDF27L; // 1.71933865632803078993e-01, /*
  private static final long R4  = 0x3F9317EA742ED475L; // 1.86459191715652901344e-02, /* 
  private static final long R5  = 0x3F497DDACA41A95BL; // 7.77942496381893596434e-04, /* 
  private static final long R6  = 0x3EDEBAF7A5B38140L; // 7.32668430744625636189e-06, /* 
  private static final long W0  = 0x3FDACFE390C97D69L; // 4.18938533204672725052e-01, /* 
  private static final long W1  = 0x3FB555555555553BL; // 8.33333333333329678849e-02, /* 
  private static final long W2  = 0xBF66C16C16B02E5CL; // -2.77777777728775536470e-03, /* 
  private static final long W3  = 0x3F4A019F98CF38B6L; // 7.93650558643019558500e-04, /* 
  private static final long W4  = 0xBF4380CB8C0FE741L; // -5.95187557450339963135e-04, /* 
  private static final long W5  = 0x3F4B67BA4CDAD5D1L; // 8.36339918996282139126e-04, /* 
  private static final long W6  = 0xBF5AB89D0B9E43E4L; // -1.63092934096575273989e-03; /* 

  /**
   * Return gamma(x) or ln(|gamma(x)|).  First ln(|gamma(x)|) is computed.  Then,
   * if exp is true, the exponential of that value is computed, and the sign
   * is restored.
   */
  private static long lgamma(long x, boolean exp) {
    int hx = getHI(x);
    int lx = getLO(x);

    // purge off +-inf, NaN, +-0, and negative arguments 
    boolean negative = false;
    int ix = hx&0x7fffffff;
    if(ix>=0x7ff00000) return mul(x, x); 
    if((ix|lx)==0) return POSITIVE_INFINITY; // one/zero
    if(ix<0x3b900000) {	// |x|<2**-70, return -log(|x|)
      if(hx<0) {
        negative = true;
        x = negate(x);
      }
      x = negate(log(x));
      if (exp) {
        x = exp(x);
        if (negative) {
          x = negate(x);
        }
      }
      return x;
    }
    long t, nadj;
    if(hx<0) {
        if(ix>=0x43300000) // |x|>=2**52, must be -integer 
            return POSITIVE_INFINITY; // one/zero;
        t = sinPi(x);
        if(isZero(t)) return POSITIVE_INFINITY; // one/zero; // -integer 
        nadj = log(div(PI, abs(mul(t, x))));
        if (lt(t, ZERO)) {
          negative = true;
        }
        x = negate(x);
    } else {
      nadj = ZERO;
    }

    // purge off 1 and 2 
    long r, y;
    int i;
    if((((ix-0x3ff00000)|lx)==0)||(((ix-0x40000000)|lx)==0)) r = ZERO;
    // for x < 2.0 
    else if(ix<0x40000000) {
      if(ix<=0x3feccccc) { // lgamma(x) = lgamma(x+1)-log(x) 
        r = negate(log(x));
        if(ix>=0x3FE76944) {
          y = sub(ONE, x); 
          i=0;
        } else if(ix>=0x3FCDA661) {
          y= sub(x, sub(TC, ONE)); 
          i=1;
        } else {
          y = x; 
          i=2;
        }
      } else {
        r = ZERO;
        if(ix>=0x3FFBB4C3) {
          y=sub(TWO, x);
          i=0;
        } // [1.7316,2]
        else if(ix>=0x3FF3B4C4) {
          y=sub(x, TC);
          i=1;
        } // [1.23,1.73] 
        else {
          y=sub(x, ONE);
          i=2;
        }
      }
      long w, z, p1, p2, p3, p;
      switch(i) {
        case 0:
          z = mul(y, y);
          p1 = add(A0, mul(z, add(A2, mul(z, add(A4, mul(
                  z, add(A6, mul(z, add(A8, mul(z, A10))))))))));
          p2 = mul(z, add(A1, mul(z, add(A3, mul(z, add(
                  A5, mul(z, add(A7, mul(z, add(A9, mul(z, A11)))))))))));
          p = add(mul(y, p1), p2);
          r = add(r, sub(p, scalbn(y, -1))); 
          break;
        case 1:
          z = mul(y, y);
          w = mul(z, y);
          p1 = add(TB0, mul(w, add(TB3, mul(w, add(TB6, mul(w,
                  add(TB9, mul(w, TB12)))))))); // parallel comp 
          p2 = add(TB1, mul(w, add(TB4, mul(w, add(TB7, mul(w,
                  add(TB10, mul(w, TB13))))))));
          p3 = add(TB2, mul(w, add(TB5, mul(w, add(TB8, mul(w,
                  add(TB11, mul(w, TB14))))))));
          p = sub(mul(z, p1), sub(TT, mul(w, add(p2, 
                  mul(y, p3)))));
          r = add(r, add(TF, p));
          break;
        case 2:	
          p1 = mul(y, add(U0, mul(y, add(U1, mul(y, add(U2,
                  mul(y, add(U3, mul(y, add(U4, mul(y, U5)))))))))));
          p2 = add(ONE, mul(y, add(V1, mul(y, add(V2, mul(y,
                  add(V3, mul(y, add(V4, mul(y, V5))))))))));
          r = add(r, add(negate(scalbn(y, -1)), div(p1, p2)));
      }
    }
    else if(ix<0x40200000) { // x < 8.0 
      i = intValue(x);
      t = ZERO;
      y = sub(x, intToDouble(i));
      long p = mul(y, add(SB0, mul(y, add(SB1, mul(y, add(SB2, 
              mul(y, add(SB3, mul(y, add(SB4, mul(y, add(SB5,
              mul(y, SB6)))))))))))));
      long q = add(ONE, mul(y, add(R1, mul(y, add(R2, mul(y, add(R3,
              mul(y, add(R4, mul(y, add(R5, mul(y, R6))))))))))));
      r = add(scalbn(y, -1), div(p, q));
      long z = ONE; // lgamma(1+s) = log(s) + lgamma(s) 
      switch(i) {
        case 7: z = mul(z, add(y, SIX)); // FALLTHRU 
        case 6: z = mul(z, add(y, FIVE)); // FALLTHRU 
        case 5: z = mul(z, add(y, FOUR)); // FALLTHRU 
        case 4: z = mul(z, add(y, THREE)); // FALLTHRU 
        case 3: z = mul(z, add(y, TWO)); // FALLTHRU 
                r = add(r, log(z)); 
                break;
        }
    // 8.0 <= x < 2**58 
    } else if (ix < 0x43900000) {
        t = log(x);
        long z = div(ONE, x);
        y = mul(z, z);
        long w = add(W0, mul(z, add(W1, mul(y, add(W2, mul(y, 
                add(W3, mul(y, add(W4, mul(y, add(W5, mul(y, 
                W6))))))))))));
        r = add(mul(sub(x, ONE_HALF), sub(t, ONE)), w);
    } else {
    // 2**58 <= x <= inf 
      r = mul(x, sub(log(x), ONE));
    }
    if(hx<0) r = sub(nadj, r);
    if (exp) {
      r = exp(r);
      if (negative) {
        r = negate(r);
      }
    }
    return r;
  }
 
  private static final long TWO52 = 0x4330000000000000L; // 4.50359962737049600000e+15

  /** used by lgamma */
  private static long sinPi(long x) {
    int ix = 0x7fffffff & getHI(x);
    if(ix<0x3fd00000) return kernelSin(mul(PI, x), ZERO, 0);
    long y = negate(x); // x is assume negative

    // argument reduction, make sure inexact flag not raised if input is 
    // an integer
    long z = floor(y);
    int n;
    if (ne(z, y)) { // inexact anyway
      y = scalbn(y, -1);
      y = scalbn(sub(y, floor(y)), 1); // y = |x| mod 2.0
      n = intValue(scalbn(y, 2));
    } else {
      if(ix>=0x43400000) {
        y = ZERO; n = 0; // y must be even
      } else {
        if(ix<0x43300000) z = add(y, TWO52); // exact
        n = getLO(z) & 1; // lower word of z 
        y = intToDouble(n);
        n <<= 2;
      }
    }
    switch (n) {
      case 0:   y =  kernelSin(mul(PI, y),ZERO,0); break;
      case 1:   
      case 2:   y =  kernelCos(mul(PI, sub(ONE_HALF, y)),ZERO); break;
      case 3:  
      case 4:   y =  kernelSin(mul(PI, sub(ONE, y)),ZERO,0); break;
      case 5:
      case 6:   y = negate(kernelCos(mul(PI, sub(y, THREE_HALVES)),ZERO)); break;
      default:  y =  kernelSin(mul(PI, sub(y, TWO)),ZERO,0); break;
    }
    return negate(y);
  }
     
  
  /////////////////////////////////////////////////////////////////////////////
  // Instance members
  /////////////////////////////////////////////////////////////////////////////

  private final long value;

  
  /////////////////////////////////////////////////////////////////////////////
  // Constructors
  /////////////////////////////////////////////////////////////////////////////

  /**
   * Constructs a newly-allocated <code>MicroDouble</code> object that represents 
   * the argument. 
   *
   * @param d the <code>double</code> value to be represented by the <code>MicroDouble</code>.
   */
  public MicroDouble(long d) {
    // canonicalize NaN values so that hashCode() and equals() can be simpler
    if (isNaN(d)) {
      d = NaN;
    }
    value = d;
  }
  
  /**
   * Constructs a newly-allocated <code>MicroDouble</code> object that represents 
   * the argument.
   *
   * @param s a <code>String</code> to be converted to a <code>MicroDouble</code>.
   * @throws NumberFormatException if the <code>String</code> does not contain a
   *         parsable number.
   * @see #parseDouble(String)
   */
  public MicroDouble(String s) {
    this(parseDouble(s));
  }

  
  /////////////////////////////////////////////////////////////////////////////
  // Instance methods
  /////////////////////////////////////////////////////////////////////////////

  /**
   * Returns the <code>double</code> value of this <code>MicroDouble</code>
   * object.
   */
  public long doubleValue() {
    return value;
  }

  /**
   * Returns a String object representing this MicroDouble's value.
   * Equivalent to <code>toString(doubleValue())</code>.
   *
   * @see #toString(long)
   */
  public String toString() {
    return toString(value);
  }
    
  /**
   * Returns a hash code for this <code>MicroDouble</code> object.
   * Equivalent to <code>(int) (doubleValue() ^ (doubleValue >>> 32))</code>
   */
  public int hashCode() {
    return ((int) value) ^ ((int) (value >>> 32));
  }
  
  /**
   * Compares this object against the specified object.
   * Equivalent to 
   * <code>((obj instanceof MicroDouble) && (compare(((MicroDouble) obj).doubleValue(), doubleValue()) == 0))</code>
   * 
   * @see #compare(long, long)
   */
  public boolean equals(Object obj) {
    return ((obj instanceof MicroDouble) 
            && (((MicroDouble) obj).value == value));
  }
  
}
