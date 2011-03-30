// $Id: MicroFloat.java,v 1.2 2004/08/03 04:57:42 Dave Exp $
/*
 * Float.java
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
 */
package net.dclausen.microfloat;

/**
 * A software implementation of IEEE-754 single precision math which does not
 * rely on the <code>float</code> data type. 
 * This class overloads the <code>int</code> data type by storing 
 * <code>float</code> data in it.
 * See the 
 * <a href="package-summary.html#package_description">package description</a> 
 * for more information.
 * <p>
 * @author David Clausen
 * @see <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Float.html">Float</a>
 * @see MicroDouble
 * @version $Revision: 1.2 $
 */
public final class MicroFloat {

  /////////////////////////////////////////////////////////////////////////////
  // General-purpose constants
  /////////////////////////////////////////////////////////////////////////////

  /**
   * A constant representing the same value as 
   * <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Float.html#POSITIVE_INFINITY">Float.POSITIVE_INFINITY</a>
   */
  public  static final int POSITIVE_INFINITY = 0x7f800000;

  /**
   * A constant holding the same value as 
   * <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Float.html#NEGATIVE_INFINITY">Float.NEGATIVE_INFINITY</a>
   */
  public  static final int NEGATIVE_INFINITY = 0xff800000;
  
  /**
   * A constant holding the same value as
   * <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Float.html#NaN">Float.NaN</a>
   */
  public  static final int NaN               = 0x7fc00000;

  /**
   * A constant holding the same value as 
   * <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Float.html#MAX_VALUE">Float.MAX_VALUE</a>
   */
  public  static final int MAX_VALUE         = 0x7f7fffff;
  
  /**
   * A constant holding the same value as
   * <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Float.html#MIN_VALUE">Float.MIN_VALUE</a>
   */
  public  static final int MIN_VALUE         = 0x00000001;

  /**
   * A single-precision version of {@link MicroDouble#E}
   */
  public  static final int E                 = 0x402df854;

  /**
   * A single-precision version of {@link MicroDouble#PI}
   */
  public  static final int PI                = 0x40490fdb;
  
  // Other constants needed internally, and exposed as a convenience.
  
  /** A constant holding the value of 0.0f */
  public static final int ZERO              = 0x00000000;

  /** A constant holding the value of -0.0f */
  public static final int NEGATIVE_ZERO     = 0x80000000;

  /** A constant holding the value of 1.0f */
  public static final int ONE               = 0x3f800000;

  /** A constant holding the value of 2.0f */
  public static final int TWO               = 0x40000000;

  /** A constant holding the value of 0.5f */
  public static final int ONE_HALF          = 0x3f000000;
  

  /////////////////////////////////////////////////////////////////////////////
  // Packing and unpacking the IEEE-754 single precision format
  /////////////////////////////////////////////////////////////////////////////

  private static final int ABS_MASK          = 0x7fffffff;
  private static final int SIGN_MASK         = 0x80000000; // 1 bit
  private static final int EXPONENT_MASK     = 0x7f800000; // 8 bits
  private static final int FRACTION_MASK     = 0x007fffff; // 23 bits
  private static final int IMPLIED_ONE       = 0x00800000; // 24th bit

  /** @return true iff d is negative */
  static boolean unpackSign(int f) {
    return (f < 0);
  }

  /** @return an integer in the range [-150, 105] */
  static int unpackExponent(int f) {
    return ((f >> 23) & 0xff) - 150;
  }
  
  /** @return an integer in the range [0, 0x00ffffff] */
  static int unpackMantissa(int f) {
    if ((f & EXPONENT_MASK) == 0) {
      return ((f & FRACTION_MASK) << 1);
    } else {
      return ((f & FRACTION_MASK) | IMPLIED_ONE);
    }
  }

  /** 
   * @return the float which most closely represents the given base-2 mantissa
   *         and exponent
   */
  static int pack(boolean negative, int exponent, int mantissa) {
    // left align mantissa
    int shift = BitUtils.countLeadingZeros(mantissa);
    mantissa <<= shift;
    exponent -= shift;
    return pack2(negative, exponent, mantissa);
  }

  /** 
   * @return the float which most closely represents the given base-2 mantissa
   *         and exponent
   */
  static int pack(boolean negative, int exponent, long mantissa) {
    // shift mantissa so that it is left-aligned when cast to an int
    int shift = 32 - BitUtils.countLeadingZeros(mantissa);
    exponent += shift;
    if (shift > 0) {
      mantissa = BitUtils.stickyRightShift(mantissa, shift);
    } else if (shift < 0) {
      mantissa <<= -shift;
    }
    return pack2(negative, exponent, (int) mantissa);
  }

  /**
   * @param mantissa must be left aligned (or zero)
   */
  private static int pack2(boolean negative, int exponent, int mantissa) {
    // reduce precision of mantissa, rounding if necessary
    if (mantissa != 0) {
      if (exponent < -157) {
        // subnormal
        mantissa = BitUtils.roundingRightShift(mantissa, -149 - exponent);
      } else {
        // normal
        mantissa = BitUtils.roundingRightShift(mantissa, 8);
        if (mantissa == 0x1000000) {
          // oops, the rounding carried into the 25th bit
          mantissa = 0x800000;
          exponent++;
        }
        // pack the exponent
        if (exponent > 96) {
          mantissa = POSITIVE_INFINITY;
        } else {
          mantissa ^= IMPLIED_ONE;
          mantissa |= (exponent + 158) << 23;
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
   * Mimics 
   * <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Float.html#isNaN(float)">Float.isNaN(float)</a>
   */
  public static boolean isNaN(int f) {
    return ((f & ABS_MASK) > POSITIVE_INFINITY);
  }

  /**
   * Mimics
   * <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Float.html#isInfinite(float)">Float.isInfinite(float)</a>
   */
  public static boolean isInfinite(int f) {
    return ((f & ABS_MASK) == POSITIVE_INFINITY);
  }
  
  /**
   * Returns <code>true</code> if the specified number has zero
   * magnitude, <code>false</code> otherwise.
   *
   * @param   f   the <code>float</code> value to be tested.
   * @return  <code>true</code> if the value of the argument is positive
   *          zero or negative zero; <code>false</code> otherwise.
   */
  public static boolean isZero(int f) {
    return ((f & ABS_MASK) == 0);
  }

  
  /////////////////////////////////////////////////////////////////////////////
  // Sign changes
  /////////////////////////////////////////////////////////////////////////////
  
  /**
   * Mimics 
   * <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Math.html#abs(float)">Math.abs(float)</a>
   */
  public static int abs(int f) {
    //if (isNaN(f)) {
    //  return NaN;
    //}
    return (f & ABS_MASK);
  }

  /**
   * Returns the negation of a <code>float</code> value.
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
   * @param   f   the <code>float</code> value whose negated value is to be 
   *              determined
   * @return  the negation of the argument.
   */
  public static int negate(int f) {
    if (isNaN(f)) {
      return NaN;
    }
    return (f ^ SIGN_MASK);
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
   * @param   f1   the first <code>float</code> value to be compared.
   * @param   f2   the second <code>float</code> value to be compared.
   * @return  <code>true</code> if the two values are considered equal;
   *          <code>false</code> otherwise.
   */
  public static boolean eq(int f1, int f2) {
    return (((f1 == f2) && (! isNaN(f1))) || (isZero(f1) && isZero(f2)));
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
   * @param   f1   the first <code>float</code> value to be compared.
   * @param   f2   the second <code>float</code> value to be compared.
   * @return  <code>true</code> if the two values are considered equal;
   *          <code>false</code> otherwise.
   */
  public static boolean ne(int f1, int f2) {
    return (! eq(f1, f2));
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
   * @param   f1   the first <code>float</code> value to be compared.
   * @param   f2   the second <code>float</code> value to be compared.
   * @return  <code>true</code> if the first value is less than the second value;
   *          <code>false</code> otherwise.
   */
  public static boolean lt(int f1, int f2) {
    if (isNaN(f1) || isNaN(f2)) {
      return false;
    } else if (f2 == ZERO) {
      f2 = NEGATIVE_ZERO;
    }
    return (cmp(f1, f2) < 0);
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
   * @param   f1   the first <code>float</code> value to be compared.
   * @param   f2   the second <code>float</code> value to be compared.
   * @return  <code>true</code> if the first value is less than or equal to 
   *          the second value; <code>false</code> otherwise.
   */
  public static boolean le(int f1, int f2) {
    if (isNaN(f1) || isNaN(f2)) {
      return false;
    } else if (f2 == NEGATIVE_ZERO) {
      f2 = ZERO;
    }
    return (cmp(f1, f2) <= 0);
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
   * @param   f1   the first <code>float</code> value to be compared.
   * @param   f2   the second <code>float</code> value to be compared.
   * @return  <code>true</code> if the first value is greater than the second value;
   *          <code>false</code> otherwise.
   */
  public static boolean gt(int f1, int f2) {
    if (isNaN(f1) || isNaN(f2)) {
      return false;
    } else if (f1 == ZERO) {
      f1 = NEGATIVE_ZERO;
    }
    return (cmp(f1, f2) > 0);
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
   * @param   f1   the first <code>float</code> value to be compared.
   * @param   f2   the second <code>float</code> value to be compared.
   * @return  <code>true</code> if the first value is greater than or equal to 
   *          the second value; <code>false</code> otherwise.
   */
  public static boolean ge(int f1, int f2) {
    if (isNaN(f1) || isNaN(f2)) {
      return false;
    } else if (f1 == NEGATIVE_ZERO) {
      f1 = ZERO;
    }
    return (cmp(f1, f2) >= 0);
  }


  /**
   * Mimics 
   * <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Float.html#compare(float, float)">Float.compare(float, float)</a>.
   * <p>
   * Note that when using this method (as well as <code>Float.compare</code>),
   * the following rules apply:
   * <ul><li>
   *		<code>NaN</code> is considered 
   *		to be equal to itself and greater than all other
   *		<code>float</code> values (including
   *		<code>POSITIVE_INFINITY</code>).
   * <li>
   *		<code>0.0</code> is considered to be greater
   *		than <code>-0.0</code>.
   * </ul>
   */
  public static int compare(int f1, int f2) {
    boolean n1 = isNaN(f1);
    boolean n2 = isNaN(f2);
    if (n1 || n2) {
      if (n1 && n2) {
        return 0;
      }
      return (n1 ? 1 : -1);
    }
    return cmp(f1, f2);
  }

  /**
   * Mimics 
   * <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Math.html#max(float, float)">Math.max(float, float)</a>.
   */
  public static int max(int f1, int f2) {
    if (isNaN(f1) || isNaN(f2)) {
      return NaN;
    }
    return ((cmp(f1, f2) < 0) ? f2 : f1);
  }

  /**
   * Mimics 
   * <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Math.html#min(float, float)">Math.min(float, float)</a>.
   */
  public static int min(int f1, int f2) {
    if (isNaN(f1) || isNaN(f2)) {
      return NaN;
    }
    return ((cmp(f1, f2) > 0) ? f2 : f1);
  }

  private static int cmp(int f1, int f2) {
    if (f1 < 0) {
      if (f2 < 0) {
        return f2 - f1;
      } else {
        return -1;
      }
    } else if (f2 < 0) {
      return 1;
    } else {
      return f1 - f2;
    }
  }


  /////////////////////////////////////////////////////////////////////////////
  // Type conversion
  /////////////////////////////////////////////////////////////////////////////

  /** 
   * Convert the given <code>int</code> to a <code>float</code> as would happen
   * in a casting operation specified by 
   * <a href="http://java.sun.com/docs/books/jls/second_edition/html/conversions.doc.html#25214">section
   * 5.1.2 of the JLS</a>.  This is a widening primitive conversion which 
   * will not result in a loss of magnitude, but might result in a loss of
   * precision.
   *
   * @param x the <code>int</code> to be converted
   * @return the <code>float</code> representation of the argument
   */
  public static int intToFloat(int x) {
    if (x < 0) {
      return pack(true, 0, -x);
    }
    return pack(false, 0, x);
  }
  
  /** 
   * Convert the given <code>long</code> to a <code>float</code> as would happen
   * in a casting operation specified by 
   * <a href="http://java.sun.com/docs/books/jls/second_edition/html/conversions.doc.html#25214">section
   * 5.1.2 of the JLS</a>.  This is a widening primitive conversion which 
   * will not result in a loss of magnitude, but might result in a loss of
   * precision.
   *
   * @param x the <code>long</code> to be converted
   * @return the <code>float</code> representation of the argument
   */
  public static int longToFloat(long x) {
    if (x < 0) {
      return pack(true, 0, -x);
    }
    return pack(false, 0, x);
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
  public static int doubleToFloat(long d) {
    if (MicroDouble.isNaN(d)) {
      return NaN;
    }
    boolean n = MicroDouble.unpackSign(d);
    if (MicroDouble.isZero(d)) {
      return (n ? NEGATIVE_ZERO : ZERO);
    } else if (MicroDouble.isInfinite(d)) {
      return (n ? NEGATIVE_INFINITY : POSITIVE_INFINITY);
    }
    int x = MicroDouble.unpackExponent(d);
    long m = MicroDouble.unpackMantissa(d);
    return pack(n, x, m);
  }

  /** 
   * Convert the given <code>float</code> to a <code>byte</code> as would happen
   * in a casting operation specified by 
   * <a href="http://java.sun.com/docs/books/jls/second_edition/html/conversions.doc.html#25363">section
   * 5.1.3 of the JLS</a>.  This is a narrowing primitive conversion which 
   * may result in a loss of magnitude and/or precision.
   * <p>
   * Note that this is a non-intuitive conversion.  If the argument is outside
   * of the range of the byte type, the result is basically meaningless.
   *
   * @param f the <code>float</code> to be converted
   * @return the <code>byte</code> representation of the argument
   */
  public static byte byteValue(int f) {
    long x = intValue(f);
    return (byte) x;
  }

  /** 
   * Convert the given <code>float</code> to a <code>short</code> as would happen
   * in a casting operation specified by 
   * <a href="http://java.sun.com/docs/books/jls/second_edition/html/conversions.doc.html#25363">section
   * 5.1.3 of the JLS</a>.  This is a narrowing primitive conversion which 
   * may result in a loss of magnitude and/or precision.
   * <p>
   * Note that this is a non-intuitive conversion.  If the argument is outside
   * of the range of the short type, the result is basically meaningless.
   *
   * @param f the <code>float</code> to be converted
   * @return the <code>short</code> representation of the argument
   */
  public static short shortValue(int f) {
    long x = intValue(f);
    return (short) x;
  }

  /** 
   * Convert the given <code>float</code> to an <code>int</code> as would happen
   * in a casting operation specified by 
   * <a href="http://java.sun.com/docs/books/jls/second_edition/html/conversions.doc.html#25363">section
   * 5.1.3 of the JLS</a>.  This is a narrowing primitive conversion which 
   * may result in a loss of magnitude and/or precision.
   *
   * @param f the <code>float</code> to be converted
   * @return the <code>int</code> representation of the argument
   */
  public static int intValue(int f) {
    long x = longValue(f);
    if (x >= Integer.MAX_VALUE) {
      return Integer.MAX_VALUE;
    } else if (x <= Integer.MIN_VALUE) {
      return Integer.MIN_VALUE;
    }
    return (int) x;
  }
  
  /** 
   * Convert the given <code>float</code> to a <code>long</code> as would happen
   * in a casting operation specified by 
   * <a href="http://java.sun.com/docs/books/jls/second_edition/html/conversions.doc.html#25363">section
   * 5.1.3 of the JLS</a>.  This is a narrowing primitive conversion which 
   * may result in a loss of magnitude and/or precision.
   *
   * @param f the <code>float</code> to be converted
   * @return the <code>long</code> representation of the argument
   */
  public static long longValue(int f) {
    if (isNaN(f)) {
      return 0;
    }
    boolean n = unpackSign(f);
    int x = unpackExponent(f);
    long m = unpackMantissa(f);
    if (x > 0) {
      if ((x >= 63) || ((m >> (63 - x)) != 0))  {
        return (n ? Long.MIN_VALUE : Long.MAX_VALUE);
      }
      m <<= x;
    } else if (x <= -24) {
      return 0;
    } else {
      m >>>= -x;
    }
    return (n ? -m : m);
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
  public static long doubleValue(int f) {
    return MicroDouble.floatToDouble(f);
  }
  

  /////////////////////////////////////////////////////////////////////////////
  // Basic arithmetic
  /////////////////////////////////////////////////////////////////////////////

  /**
   * Returns the sum of the two <code>float</code> arguments according to 
   * <a href="http://java.sun.com/docs/books/jls/second_edition/html/expressions.doc.html#13510">section
   * 15.18.2 of the JLS</a>.
   * <p>
   * This method takes the place of the binary <code>+</code> operator.
   *
   * @param   f1   the first <code>float</code> value to be summed.
   * @param   f2   the second <code>float</code> value to be summed.
   * @return  the sum of the two arguments
   */
  public static int add(int f1, int f2) {
    if (isNaN(f1) || isNaN(f2)) {
      return NaN;
    }
    
    boolean n1 = unpackSign(f1);
    boolean n2 = unpackSign(f2);
    
    // special handling of infinity
    boolean i1 = isInfinite(f1);
    boolean i2 = isInfinite(f2);
    if (i1 || i2) {
      if (i1 && i2) {
        if (n1 != n2) {
          // infinites of opposite sign -> NaN
          return NaN;
        } else {
          // infinites of same sign -> infinity the same sign
          return f1;
        }
      } else if (i1) {
        return f1; // infinite + finite = infinite
      } else {
        return f2; // finite + infinite = infinite
      }
    }
    
    // special handling of zero
    boolean z1 = isZero(f1);
    boolean z2 = isZero(f2);
    if (z1 || z2) {
      if (z1 && z2) {
        if (n1 != n2) {
          // zeros of opposite sign -> positive zero
          return ZERO;
        } else {
          return f1; // zeros of same sign -> zero of the same sign
        }
      } else if (z1) {
        return f2; // zero + nonzero = nonzero
      } else {
        return f1; // nonzero + zero = nonzero
      }
    }
    
    // unpack, and add 3 guard digits
    int m1 = unpackMantissa(f1) << 3;
    int x1 = unpackExponent(f1) - 3;
    int m2 = unpackMantissa(f2) << 3;
    int x2 = unpackExponent(f2) - 3;

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
    int f = pack(n1, x1, m1);
    if (f == NEGATIVE_ZERO) {
      return ZERO;
    }
    return f;
  }

  /**
   * Returns the difference of the two <code>float</code> arguments according to 
   * <a href="http://java.sun.com/docs/books/jls/second_edition/html/expressions.doc.html#13510">section
   * 15.18.2 of the JLS</a>.
   * <p>
   * This method takes the place of the binary <code>-</code> operator.
   *
   * @param   f1   the first <code>float</code> value 
   * @param   f2   the second <code>float</code> value
   * @return  the difference of the two arguments
   */
  public static int sub(int f1, int f2) {
    return add(f1, negate(f2));
  }

  /**
   * Returns the product of the two <code>float</code> arguments according to 
   * <a href="http://java.sun.com/docs/books/jls/second_edition/html/expressions.doc.html#5036">section
   * 15.17.1 of the JLS</a>.
   * <p>
   * This method takes the place of the <code>*</code> operator.
   *
   * @param   f1   the first <code>float</code> value
   * @param   f2   the second <code>float</code> value
   * @return  the product of the two arguments
   */
  public static int mul(int f1, int f2) {
    if (isNaN(f1) || isNaN(f2)) {
      return NaN;
    }

    boolean negative = unpackSign(f1) ^ unpackSign(f2);
    
    // special handling of infinity
    if (isInfinite(f1) || isInfinite(f2)) {
      if (isZero(f1) || isZero(f2)) {
        return NaN;
      } else {
        return (negative ? NEGATIVE_INFINITY : POSITIVE_INFINITY);
      }
    }
    
    // unpack
    int m1 = unpackMantissa(f1);
    int x1 = unpackExponent(f1);
    int m2 = unpackMantissa(f2);
    int x2 = unpackExponent(f2);
    
    // compute the resultant exponent
    x1 += x2;
    
    // compute the resultant mantissa using integer multiplication
    long m = ((long) m1) * ((long) m2);
    
    // round and pack the result
    return pack(negative, x1, m);
  }

  /**
   * Returns the quotient of the two <code>float</code> arguments according to 
   * <a href="http://java.sun.com/docs/books/jls/second_edition/html/expressions.doc.html#5047">section
   * 15.17.2 of the JLS</a>.
   * <p>
   * This method takes the place of the <code>/</code> operator.
   *
   * @param   f1   the <code>float</code> dividend 
   * @param   f2   the <code>float</code> divisor
   * @return  the quotient of the two arguments
   */
  public static int div(int f1, int f2) {
    if (isNaN(f1) || isNaN(f2)) {
      return NaN;
    }

    boolean negative = unpackSign(f1) ^ unpackSign(f2);
    
    // special handling of infinity
    boolean n1 = isInfinite(f1);
    boolean n2 = isInfinite(f2);
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
    n1 = isZero(f1);
    n2 = isZero(f2);
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
    int m1 = unpackMantissa(f1);
    int x1 = unpackExponent(f1);
    int m2 = unpackMantissa(f2);
    int x2 = unpackExponent(f2);

    // shift the dividend to the left to increase precision, then do an integer
    // divide
    int s = BitUtils.countLeadingZeros(m1) + 22;
    long m3 = ((long) m1) << s;
    int x = x1 - x2 - s;
    long m = m3 / m2;
    boolean r = ((m * m2) != m3);

    // put a non-zero fraction into the sticky bit
    if (r) {
      m |= 1; 
    }
    return pack(negative, x, m);
  }

  /**
   * Returns the remainder of the two <code>float</code> arguments according to 
   * <a href="http://java.sun.com/docs/books/jls/second_edition/html/expressions.doc.html#24956">section
   * 15.17.3 of the JLS</a>.
   * <p>
   * This method takes the place of the <code>%</code> operator.
   *
   * @param   f1   the <code>float</code> dividend 
   * @param   f2   the <code>float</code> divisor
   * @return  the remainder of the two arguments
   */
  public static int mod(int f1, int f2) {
    if (isNaN(f1) || isNaN(f2) || isInfinite(f1) || isZero(f2)) {
      return NaN;
    } else if (isZero(f1) || isInfinite(f2)) {
      return f1;
    }
    
    // unpack
    int x1 = unpackExponent(f1);
    int x2 = unpackExponent(f2);
    if (x1 < x2) {
      return f1;
    }
    boolean n = unpackSign(f1);
    int m1 = unpackMantissa(f1);
    int m2 = unpackMantissa(f2);
    if (x1 == x2) {
      m1 %= m2;
    } else {
      // reduce m1 by left shifting and modding until the exponents x1 and x2 are 
      // equal
      while (x1 != x2) {
        int s = Math.min(39, x1 - x2);
        x1 -= s;
        m1 = (int) ((((long) m1) << s) % m2);
      }
    }
    return pack(n, x1, m1);
  }


  /////////////////////////////////////////////////////////////////////////////
  // Rounding
  /////////////////////////////////////////////////////////////////////////////

  /**
   * Returns the <code>float</code> of greatest magnitude (furthest from zero)
   * that is equal to a mathematical integer and which has a mignitude not
   * greater than the argument's magnitude.  Special cases:
   * <ul><li>If the argument value is already equal to a mathematical 
   * integer, then the result is the same as the argument. 
   * <li>If the argument is NaN or an infinity or positive zero or 
   * negative zero, then the result is the same as the argument.</ul>
   *
   * @param   f   a <code>float</code> value.
   * @return the <code>float</code> of greatest magnitude (furthest from zero)
   *         whose magnitude is not greater than the argument's and which 
   *         is equal to a mathematical integer.
   */
  public static int truncate(int f) {
    return round(f, false, unpackSign(f));
  }

  /**
   * Mimics 
   * <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Math.html#rint(double)">Math.rint(double)</a>, 
   * using single precision.
   */
  public static int rint(int f) {
    return round(f, true, false);
  }
  
  /**
   * Mimics
   * <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Math.html#floor(double)">Math.floor(double)</a>, 
   * using single precision.
   */
  public static int floor(int f) {
    return round(f, false, false);
  }
  
  /**
   * Mimics
   * <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Math.html#ceil(double)">Math.ceil(double)</a>, 
   * using single precision.
   */
  public static int ceil(int f) {
    return round(f, false, true);
  }

  /**
   * Mimics
   * <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Math.html#round(double)">Math.round(double)</a>, 
   * using single precision.
   */
  public static int round(int f) {
    return intValue(floor(add(f, ONE_HALF)));
  }

  private static int round(int f, boolean round, boolean ceil) {
    if (isNaN(f)) {
      return NaN;
    } else if (isZero(f) || isInfinite(f)) {
      return f;
    }
    int x = unpackExponent(f);
    if (x >= 0) {
      return f;
    }
    boolean n = unpackSign(f);
    int m = unpackMantissa(f);
    if (round) {
      m = BitUtils.roundingRightShift(m, -x);
    } else {
      int r;
      if (x <= -32) {
        r = m;
        m = 0;
      } else {
        r = m << (32 + x);
        m >>>= -x;
      }
      if ((n ^ ceil) && (r != 0)) {
        m++;
      }
    }
    return pack(n, 0, m);
  }


  /////////////////////////////////////////////////////////////////////////////
  // String conversion
  /////////////////////////////////////////////////////////////////////////////

  // decimal -> binary
  
  // base 2 mantissas for 10**-54 through 10**38, at intervals of 100
  private static final int[] pow10m = {
          0xc428d05b, 0x993fe2c7, 0xef73d257, 0xbb127c54, 0x92267121, 
          0xe45c10c4, 0xb267ed19, 0x8b61313c, 0xd9c7dced, 0xaa242499, 
          0x84ec3c98, 0xcfb11ead, 0xa2425ff7, 0xfd87b5f3, 0xc6120625, 
          0x9abe14cd, 0xf1c90081, 0xbce50865, 0x9392ee8f, 0xe69594bf, 
          0xb424dc35, 0x8cbccc09, 0xdbe6fecf, 0xabcc7712, 0x8637bd06, 
          0xd1b71759, 0xa3d70a3d, 0x80000000, 0xc8000000, 0x9c400000, 
          0xf4240000, 0xbebc2000, 0x9502f900, 0xe8d4a510, 0xb5e620f5, 
          0x8e1bc9bf, 0xde0b6b3a, 0xad78ebc6, 0x87867832, 0xd3c21bcf, 
          0xa56fa5ba, 0x813f3979, 0xc9f2c9cd, 0x9dc5ada8, 0xf684df57, 
          0xc097ce7c, 0x96769951,
  };
  
  // base 2 exponents for 10**-54 through 10**38, at intervals of 100
  private static final short[] pow10x = {
          -211, -204, -198, -191, -184, -178, -171, -164, 
          -158, -151, -144, -138, -131, -125, -118, -111, 
          -105, -98, -91, -85, -78, -71, -65, -58, 
          -51, -45, -38, -31, -25, -18, -12, -5, 
          2, 8, 15, 22, 28, 35, 42, 48, 
          55, 62, 68, 75, 81, 88, 95,
  };
          
  private static int decToFloat(boolean negative, int base10x, int base10m) {
    if (base10m == 0) {
      return (negative ? NEGATIVE_ZERO : ZERO);
    }
    // maximize base10m to ensure consistency between toString and parseFloat
    while ((base10m > 0) && (base10m <= 0x19999999)) { // (Integer.MAX_VALUE / 5))) {
      base10m = (base10m << 3) + (base10m << 1);
      base10x--;
    }
    // base10x needs to be a multiple of 2, because the tables are
    // spaced at intervals of 100 (not 10).
    base10x += 54;
    boolean mod = ((base10x & 1) != 0);
    base10x >>= 1;
    if (base10x < 0) { // -54
      return (negative ? NEGATIVE_ZERO : ZERO);
    } else if (base10x > 46) { // 38
      return (negative ? NEGATIVE_INFINITY : POSITIVE_INFINITY);
    }
    int base2x = pow10x[base10x];
    long base2m = (base10m & 0xffffffffL) * (pow10m[base10x] & 0xffffffffL);
    if (mod) {
      if (base2m < 0) {
        base2m >>>= 1;
        base2x++;
      }
      base2m += base2m >>> 2;
      base2x += 3;
    }
    return pack(negative, base2x, base2m);
  }

  /**
   * Mimics
   * <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Float.html#parseFloat(String)">Float.parseFloat(String)</a>.
   * <p>
   * <b>This implementation is known to be inaccurate, and 
   * does not always return the same value as 
   * <code>Float.parseFloat</code>.</b>  However the difference should be no 
   * greater than 1 ulp.
   *
   * @exception  NumberFormatException  if the string does not contain a
   *               parsable number.
   */
  public static int parseFloat(String s) {
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
    int mantissa = 0;
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
        if (mantissa <= 0x19999998) { // ((Integer.MAX_VALUE / 5) - 1)) { 
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
    return decToFloat(negative, exponent, mantissa);
  }

  // binary -> decimal
  
  // base 10 mantissas for 2**-150 through 2**98, at intervals of 2**8
  private static final int[] pow2m = {
          0xb35dbf82, 0x2deaf18a, 0x758ca7c7, 
          0x1e17b843, 0x4d0985cb, 0xc5371912, 
          0x327cb273, 0x813f3979, 0x21165458, 
          0x54b40b20, 0xd8d726b7, 0x3782dacf, 
          0x8e1bc9bf, 0x246139cb, 0x5d21dba0, 
          0xee6b2800, 0x3d090000, 0x9c400000, 
          0x28000000, 0x66666666, 0x1a36e2eb, 
          0x431bde83, 0xabcc7712, 0x2bfaffc3, 
          0x709709a1, 0x1cd2b298, 0x49c97747, 
          0xbce50865, 0x305b6680, 0x7bcb43d7, 
          0x1fb0f6be, 0x51212ffc, 
  };

  // base 10 exponents for 2 ^ -150 through 2 ^ 98, at intervals of 2 ^ 8
  private static final byte[] pow2x = {
          -45, -42, -40, -37, -35, -33, -30, -28, 
          -25, -23, -21, -18, -16, -13, -11, -9, 
          -6, -4, -1, 1, 4, 6, 8, 11, 
          13, 16, 18, 20, 23, 25, 28, 30, 
  };

  /**
   * Mimics
   * <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Float.html#toString(float)">Float.toString(float)</a>.
   * <p>
   * <b>This implementation is known to be inaccurate, and 
   * does not always return the same value as 
   * <code>Float.toString</code>.</b>  However the difference should be no 
   * greater than 1 ulp.
   */
  public static String toString(int f) {
    if (isNaN(f)) {
      return "NaN";
    }
    boolean n = unpackSign(f);
    StringBuffer sb = new StringBuffer(15);
    if (n) {
      sb.append('-');
    }
    if (isZero(f)) {
      sb.append("0.0");
      return sb.toString();
    } else if (isInfinite(f)) {
      sb.append("Infinity");
      return sb.toString();
    }
    // convert from base 2 to base 10
    int base2x = unpackExponent(f);
    int base2m = unpackMantissa(f);
    int idx = base2x + 150;
    int dx = idx & 7;
    base2m <<= dx;
    idx >>= 3;
    int base10x = pow2x[idx];
    while (base2m <= 0xccccccc) {
      base2m = (base2m << 3) + (base2m << 1); // base2m *= 10;
      base10x--;
    }
    long base10ml = base2m * (pow2m[idx] & 0xffffffffL);
    int base10m = (int) (base10ml >>> 32);
    if ((base10ml << 32) < 0) {
      base10m++;
    }

    // reduce the number of digits in m10 
    boolean roundedUp = false;
    while (true) {
      int r = base10m % 10;
      int mt = base10m / 10;
      int xt = base10x + 1;
      if (r != 0) {
        if ((r > 5) || ((r == 5) && (! roundedUp))) {
          roundedUp = true;
          mt++;
        } else {
          roundedUp = false;
        }
        int ft = decToFloat(n, xt, mt);
        if (ft != f) {
          if (roundedUp) {
            mt--;
          } else {
            mt++;
          }
          roundedUp ^= true;
          ft = decToFloat(n, xt, mt);
          if (ft != f) {
            break;
          }
        }
      }
      base10m = mt;
      base10x = xt;
    }
    
    // convert to string
    String s = Integer.toString(base10m);
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

  
  /////////////////////////////////////////////////////////////////////////////
  // Instance members
  /////////////////////////////////////////////////////////////////////////////

  private final int value;

  
  /////////////////////////////////////////////////////////////////////////////
  // Constructors
  /////////////////////////////////////////////////////////////////////////////

  
  /**
   * Constructs a newly-allocated <code>MicroFloat</code> object that represents 
   * the argument. 
   *
   * @param f the <code>float</code> value to be represented by the <code>MicroFloat</code>.
   */
  public MicroFloat(int f) {
    // canonicalize NaN values so that hashCode() and equals() can be simpler
    if (isNaN(f)) {
      f = NaN;
    }
    value = f;
  }
  
  /**
   * Constructs a newly-allocated <code>MicroFloat</code> object that represents 
   * the argument.
   *
   * @param s a <code>String</code> to be converted to a <code>MicroFloat</code>.
   * @throws NumberFormatException if the <code>String</code> does not contain a
   *         parsable number.
   * @see #parseFloat(String)
   */
  public MicroFloat(String s) {
    this(parseFloat(s));
  }

  
  /////////////////////////////////////////////////////////////////////////////
  // Instance methods
  /////////////////////////////////////////////////////////////////////////////
  
  /**
   * Returns the <code>float</code> value of this <code>MicroFloat</code>
   * object.
   */
  public int floatValue() {
    return value;
  }

  /**
   * Returns a String object representing this MicroFloat's value.
   * Equivalent to <code>toString(floatValue())</code>.
   *
   * @see #toString(int)
   */
  public String toString() {
    return toString(value);
  }

  /**
   * Returns a hash code for this <code>MicroFloat</code> object.
   * Equivalent to floatValue().
   */
  public int hashCode() {
    return value;
  }

  /**
   * Compares this object against the specified object.
   * Equivalent to <code>((obj instanceof MicroFloat) && (compare(((MicroFloat) obj).floatValue(), floatValue()) == 0))</code>
   * 
   * @see #compare(int, int)
   */
  public boolean equals(Object obj) {
    return ((obj instanceof MicroFloat)
            && (((MicroFloat) obj).value == value));
  }
  
}
