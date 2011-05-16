/*
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

/*
 * Adapted by Wolfgang Puffitsch for JOP.
 */

package com.jopdesign.sys;

public final class SoftFloat32 {

	/////////////////////////////////////////////////////////////////////////////
	// General-purpose constants
	/////////////////////////////////////////////////////////////////////////////

	/**
	 * A constant representing the same value as
	 * <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Float.html#POSITIVE_INFINITY">Float.POSITIVE_INFINITY</a>
	 */
	// 	public  static final int POSITIVE_INFINITY = 0x7f800000;

	/**
	 * A constant holding the same value as
	 * <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Float.html#NEGATIVE_INFINITY">Float.NEGATIVE_INFINITY</a>
	 */
	// 	public  static final int NEGATIVE_INFINITY = 0xff800000;

	/**
	 * A constant holding the same value as
	 * <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Float.html#NaN">Float.NaN</a>
	 */
	// 	public  static final int NaN               = 0x7fc00000;

	/**
	 * A constant holding the same value as
	 * <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Float.html#MAX_VALUE">Float.MAX_VALUE</a>
	 */
	// 	public  static final int MAX_VALUE         = 0x7f7fffff;

	/**
	 * A constant holding the same value as
	 * <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Float.html#MIN_VALUE">Float.MIN_VALUE</a>
	 */
	// 	public  static final int MIN_VALUE         = 0x00000001;

	/**
	 * A single-precision version of {@link MicroDouble#E}
	 */
	// 	public  static final int E                 = 0x402df854;

	/**
	 * A single-precision version of {@link MicroDouble#PI}
	 */
	// 	public  static final int PI                = 0x40490fdb;

	// Other constants needed internally, and exposed as a convenience.

	/** A constant holding the value of 0.0f */
	// 	public static final int ZERO              = 0x00000000;

	/** A constant holding the value of -0.0f */
	// 	public static final int NEGATIVE_ZERO     = 0x80000000;

	/** A constant holding the value of 1.0f */
	// 	public static final int ONE               = 0x3f800000;

	/** A constant holding the value of 2.0f */
	// 	public static final int TWO               = 0x40000000;

	/** A constant holding the value of 0.5f */
	// 	public static final int ONE_HALF          = 0x3f000000;


	/////////////////////////////////////////////////////////////////////////////
	// Packing and unpacking the IEEE-754 single precision format
	/////////////////////////////////////////////////////////////////////////////

	// 	private static final int ABS_MASK          = 0x7fffffff;
	// 	private static final int SIGN_MASK         = 0x80000000; // 1 bit
	// 	private static final int EXPONENT_MASK     = 0x7f800000; // 8 bits
	// 	private static final int FRACTION_MASK     = 0x007fffff; // 23 bits
	// 	private static final int IMPLIED_ONE       = 0x00800000; // 24th bit

	/** @return an integer in the range [-150, 105] */
	public static int unpackExponent(int f) {
		return ((f >> 23) & 0xff) - 150;
	}

	/** @return an integer in the range [0, 0x00ffffff] */
	public static int unpackMantissa(int f) {
		if ((f & 0x7f800000) == 0) {
			return ((f & 0x007fffff) << 1);
		} else {
			return ((f & 0x007fffff) | 0x00800000);
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
	public static int pack(boolean negative, int exponent, long mantissa) {
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
					mantissa = 0x7f800000;
				} else {
					mantissa ^= 0x00800000;
					mantissa |= (exponent + 158) << 23;
				}
			}
		}

		// pack the sign bit
		if (negative) {
			mantissa |= 0x80000000;
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
		return ((f & 0x7fffffff) > 0x7f800000);
	}

	/**
	 * Mimics
	 * <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Float.html#isInfinite(float)">Float.isInfinite(float)</a>
	 */
	public static boolean isInfinite(int f) {
		return ((f & 0x7fffffff) == 0x7f800000);
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
		return ((f & 0x7fffffff) == 0);
	}

	/////////////////////////////////////////////////////////////////////////////
	// Comparison
	/////////////////////////////////////////////////////////////////////////////

    public static int float_cmpg(int a, int b) {
		if (isNaN(a) || isNaN(b)) {
			return 1;		// one is NaN
		}
		return cmp(a, b);
    }

    public static int float_cmpl(int a, int b) {
		if (isNaN(a) || isNaN(b)) {
			return -1;		// one is NaN
		}
		return cmp(a, b);
    }

	private static int cmp(int f1, int f2) {
		// test for equal
		if (f1 == f2)
			return 0;

		// positive zero and negative zero are considered euqal
		if (((f1 | f2) << 1) == 0)
			return 0;

		// actual comparison
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
		if (Const.SUPPORT_DOUBLE) {
			if (SoftFloat64.isNaN(d)) {
				return 0x7fc00000;
			}
			boolean n = d < 0;
			if (SoftFloat64.isZero(d)) {
				return (n ? 0x80000000 : 0x00000000);
			} else if (SoftFloat64.isInfinite(d)) {
				return (n ? 0xff800000 : 0x7f800000);
			}
			int x = SoftFloat64.unpackExponent(d);
			long m = SoftFloat64.unpackMantissa(d);
			return pack(n, x, m);
		} else {
			throw new RuntimeException("Not implemented");
		}
	}

	//   /**
	//    * Convert the given <code>float</code> to a <code>byte</code> as would happen
	//    * in a casting operation specified by
	//    * <a href="http://java.sun.com/docs/books/jls/second_edition/html/conversions.doc.html#25363">section
	//    * 5.1.3 of the JLS</a>.  This is a narrowing primitive conversion which
	//    * may result in a loss of magnitude and/or precision.
	//    * <p>
	//    * Note that this is a non-intuitive conversion.  If the argument is outside
	//    * of the range of the byte type, the result is basically meaningless.
	//    *
	//    * @param f the <code>float</code> to be converted
	//    * @return the <code>byte</code> representation of the argument
	//    */
	//   public static byte byteValue(int f) {
	//     long x = intValue(f);
	//     return (byte) x;
	//   }

	//   /**
	//    * Convert the given <code>float</code> to a <code>short</code> as would happen
	//    * in a casting operation specified by
	//    * <a href="http://java.sun.com/docs/books/jls/second_edition/html/conversions.doc.html#25363">section
	//    * 5.1.3 of the JLS</a>.  This is a narrowing primitive conversion which
	//    * may result in a loss of magnitude and/or precision.
	//    * <p>
	//    * Note that this is a non-intuitive conversion.  If the argument is outside
	//    * of the range of the short type, the result is basically meaningless.
	//    *
	//    * @param f the <code>float</code> to be converted
	//    * @return the <code>short</code> representation of the argument
	//    */
	//   public static short shortValue(int f) {
	//     long x = intValue(f);
	//     return (short) x;
	//   }

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
		boolean n = f < 0;
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
	public static int float_add(int f1, int f2) {
		if (isNaN(f1) || isNaN(f2)) {
			return 0x7fc00000;
		}

		boolean n1 = f1 < 0;
		boolean n2 = f2 < 0;

		// special handling of infinity
		boolean i1 = isInfinite(f1);
		boolean i2 = isInfinite(f2);
		if (i1 || i2) {
			if (i1 && i2) {
				if (n1 != n2) {
					// infinites of opposite sign -> NaN
					return 0x7fc00000;
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
					return 0x00000000;
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
		if (f == 0x80000000) {
			return 0x00000000;
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
	public static int float_sub(int f1, int f2) {
		return float_add(f1, f2 ^ 0x80000000);
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
	public static int float_mul(int f1, int f2) {
		if (isNaN(f1) || isNaN(f2)) {
			return 0x7fc00000;
		}

		boolean negative = (f1 < 0) ^ (f2 < 0);

		// special handling of infinity
		if (isInfinite(f1) || isInfinite(f2)) {
			if (isZero(f1) || isZero(f2)) {
				return 0x7fc00000;
			} else {
				return (negative ? 0xff800000 : 0x7f800000);
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
	public static int float_div(int f1, int f2) {
		if (isNaN(f1) || isNaN(f2)) {
			return 0x7fc00000;
		}

		boolean negative = (f1 < 0) ^ (f2 < 0);

		// special handling of infinity
		boolean n1 = isInfinite(f1);
		boolean n2 = isInfinite(f2);
		if (n1 || n2) {
			if (n1 && n2) {
				return 0x7fc00000;
			} else if (n1) {
				return (negative ? 0xff800000 : 0x7f800000);
			} else {
				return (negative ? 0x80000000 : 0x00000000);
			}
		}
		// neither value is infinite

		// special handling of zero
		n1 = isZero(f1);
		n2 = isZero(f2);
		if (n1 || n2) {
			if (n1 && n2) {
				return 0x7fc00000;
			} else if (n1) {
				return (negative ? 0x80000000 : 0x00000000);
			} else {
				return (negative ? 0xff800000 : 0x7f800000);
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
	public static int float_rem(int f1, int f2) {
		if (isNaN(f1) || isNaN(f2) || isInfinite(f1) || isZero(f2)) {
			return 0x7fc00000;
		} else if (isZero(f1) || isInfinite(f2)) {
			return f1;
		}

		// unpack
		int x1 = unpackExponent(f1);
		int x2 = unpackExponent(f2);
		if (x1 < x2) {
			return f1;
		}
		boolean n = f1 < 0;
		int m1 = unpackMantissa(f1);
		int m2 = unpackMantissa(f2);
		if (x1 == x2) {
			m1 %= m2;
		} else {
			// reduce m1 by left shifting and modding until the exponents x1 and x2 are
			// equal
			while (x1 != x2) { // @WCA loop <= 7
				int s = x1-x2 < 39 ? x1-x2 : 39;
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
	 * Mimics
	 * <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Math.html#round(double)">Math.round(double)</a>,
	 * using single precision.
	 */
	public static int float_round(int f) {
		return intValue(round(float_add(f, 0x3f000000), false, false));
	}

	private static int round(int f, boolean round, boolean ceil) {
		if (isNaN(f)) {
			return 0x7fc00000;
		} else if (isZero(f) || isInfinite(f)) {
			return f;
		}
		int x = unpackExponent(f);
		if (x >= 0) {
			return f;
		}
		boolean n = f < 0;
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

}
