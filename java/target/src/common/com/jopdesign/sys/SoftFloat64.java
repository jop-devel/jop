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
 * For more information on FDLIBM see:
 * http://netlib.bell-labs.com/netlib/fdlibm/index.html
 *
 */

/*
 * Adapted by Wolfgang Puffitsch for JOP.
 * 
 * See oroginal source at: http://www.dclausen.net/projects/microfloat/
 */

package com.jopdesign.sys;

public class SoftFloat64 {

	/////////////////////////////////////////////////////////////////////////////
	// General-purpose constants
	/////////////////////////////////////////////////////////////////////////////

	/**
	 * A constant holding the same value as <a
	 * href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Double.html#NaN">Double.NaN</a>
	 */
	public static final long NaN = 0x7ff8000000000000L;

	/**
	 * A constant holding the same value as <a
	 * href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Double.html#POSITIVE_INFINITY">Double.POSITIVE_INFINITY</a>
	 */
	public static final long POSITIVE_INFINITY = 0x7ff0000000000000L;

	/**
	 * A constant holding the same value as <a
	 * href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Double.html#NEGATIVE_INFINITY">Double.NEGATIVE_INFINITY</a>
	 */
	// 	public static final long NEGATIVE_INFINITY = 0xfff0000000000000L;

	/**
	 * A constant holding the same value as <a
	 * href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Double.html#MAX_VALUE">Double.MAX_VALUE</a>
	 */
	// 	public static final long MAX_VALUE = 0x7fefffffffffffffL;

	/**
	 * A constant holding the same value as <a
	 * href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Double.html#MIN_VALUE">Double.MIN_VALUE</a>
	 */
	// 	public static final long MIN_VALUE = 0x0000000000000001L;

	/** A constant holding the value of 0.0d */
	public static final long ZERO = 0x0000000000000000L;

	/** A constant holding the value of -0.0d */
	// 	public static final long NEGATIVE_ZERO = 0x8000000000000000L;

	/** A constant holding the value of 1.0d */
	public static final long ONE = 0x3ff0000000000000L;

	/** A constant holding the value of 2.0d */
	public static final long TWO = 0x4000000000000000L;

	/** A constant holding the value of 0.5d */
	// 	public static final long ONE_HALF          = 0x3fe0000000000000L;

	/////////////////////////////////////////////////////////////////////////////
	// Packing and unpacking the IEEE-754 double precision format
	/////////////////////////////////////////////////////////////////////////////
	
	  static final long ABS_MASK          = 0x7fffffffffffffffL;
	  static final long SIGN_MASK         = 0x8000000000000000L; // 1 bit
//	  private static final long EXPONENT_MASK     = 0x7ff0000000000000L; // 11 bits
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
		if ((d & 0x7ff0000000000000L) == 0) {
			return ((d & 0x000fffffffffffffL) << 1);
		} else {
			return ((d & 0x000fffffffffffffL) | 0x0010000000000000L);
		}
	}

	/**
	 * @return the double which most closely represents the given base-2
	 *         mantissa and exponent
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
				mantissa = BitUtils.roundingRightShift(mantissa, -1074
													   - exponent);
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
					mantissa = 0x7ff0000000000000L;
				} else {
					mantissa ^= 0x0010000000000000L;
					mantissa |= ((long) (exponent + 1086)) << 52;
				}
			}
		}

		// pack the sign bit
		if (negative) {
			mantissa |= 0x8000000000000000L;
		}

		return mantissa;
	}


	/////////////////////////////////////////////////////////////////////////////
	// Simple tests
	/////////////////////////////////////////////////////////////////////////////

	/**
	 * Mimics <a
	 * href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Double.html#isNaN(double)">Double.isNaN(double)</a>
	 */
	public static boolean isNaN(long d) {
		return ((d & 0x7fffffffffffffffL) > 0x7ff0000000000000L);
	}

	/**
	 * Mimics <a
	 * href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Double.html#isInfinite(double)">Double.isInfinite(double)</a>
	 */
	public static boolean isInfinite(long d) {
		return ((d & 0x7fffffffffffffffL) == 0x7ff0000000000000L);
	}

	/**
	 * Returns <code>true</code> if the specified number has zero magnitude,
	 * <code>false</code> otherwise.
	 *
	 * @param d
	 *            the <code>double</code> value to be tested.
	 * @return <code>true</code> if the value of the argument is positive zero
	 *         or negative zero; <code>false</code> otherwise.
	 */
	public static boolean isZero(long d) {
		return ((d & 0x7fffffffffffffffL) == 0x0000000000000000L);
	}


	/////////////////////////////////////////////////////////////////////////////
	// Comparison
	/////////////////////////////////////////////////////////////////////////////

    public static int double_cmpg(long a, long b) {
		if (isNaN(a) || isNaN(b)) {
			return 1;		// one is NaN
		}
		return cmp(a, b);
    }

    public static int double_cmpl(long a, long b) {
		if (isNaN(a) || isNaN(b)) {
			return -1;		// one is NaN
		}
		return cmp(a, b);
    }

	private static int cmp(long d1, long d2) {
		// test for equal
		if (d1 == d2)
			return 0;

		// positive zero and negative zero are considered equal
		if (((d1 | d2) << 1) == 0)
			return 0;

		// actual comparison
		if (d1 < 0L) {
			if (d2 < 0L) {
				return ((d1 < d2) ? 1 : -1);
			} else {
				return -1;
			}
		} else if (d2 < 0) {
			return 1;
		} else {
			return ((d1 < d2) ? -1 : 1);
		}
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
		if (Const.SUPPORT_FLOAT) {
			if (SoftFloat32.isNaN(f)) {
				return 0x7ff8000000000000L;
			}
			boolean n = f < 0;
			if (SoftFloat32.isZero(f)) {
				return (n ? 0x8000000000000000L : 0x0000000000000000L);
			} else if (SoftFloat32.isInfinite(f)) {
				return (n ? 0xfff0000000000000L : 0x7ff0000000000000L);
			}
			int x = SoftFloat32.unpackExponent(f);
			long m = SoftFloat32.unpackMantissa(f);
			return pack(n, x, m);
		} else {
			throw new RuntimeException("Not implemented");
		}
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
		boolean n = d < 0;
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
	public static long double_add(long d1, long d2) {
		if (isNaN(d1) || isNaN(d2)) {
			return 0x7ff8000000000000L;
		}

		boolean n1 = d1 < 0L;
		boolean n2 = d2 < 0L;

		// special handling of infinity
		boolean i1 = isInfinite(d1);
		boolean i2 = isInfinite(d2);
		if (i1 || i2) {
			if (i1 && i2) {
				if (n1 != n2) {
					// infinites of opposite sign -> NaN
					return 0x7ff8000000000000L;
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
					return 0x0000000000000000L;
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
		} else if (dx < 0L) {
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
		if (d == 0x8000000000000000L) {
			return 0x0000000000000000L;
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
	public static long double_sub(long d1, long d2) {
		return double_add(d1, d2 ^ 0x8000000000000000L);
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
	public static long double_mul(long d1, long d2) {
		if (isNaN(d1) || isNaN(d2)) {
			return 0x7ff8000000000000L;
		}

		boolean negative = (d1 < 0L) ^ (d2 < 0L);

		// special handling of infinity
		if (isInfinite(d1) || isInfinite(d2)) {
			if (isZero(d1) || isZero(d2)) {
				return 0x7ff8000000000000L;
			} else {
				return (negative ? 0xfff0000000000000L : 0x7ff0000000000000L);
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

		if (t3 == 0) {
			// the high 64 bits are zero and can be ignored.
			return pack(negative, x1, t1);
		}

		t1 <<= 8;
		// the 128 bit result is now in t3t1

		// shift the result left into t3 and discard excess precision
		int s = BitUtils.countLeadingZeros(t3);
		x1 += 56 - s;
		t3 <<= s;
		t3 |= t1 >>> (64 - s);
		if ((t1 << s) != 0) {
			// discarded low bits go into the sticky bit
			t3 |= 1;
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
	public static long double_div(long d1, long d2) {
		if (isNaN(d1) || isNaN(d2)) {
			return 0x7ff8000000000000L;
		}

		boolean negative = (d1 < 0L) ^ (d2 < 0L);

		// special handling of infinity
		boolean n1 = isInfinite(d1);
		boolean n2 = isInfinite(d2);
		if (n1 || n2) {
			if (n1 && n2) {
				return 0x7ff8000000000000L;
			} else if (n1) {
				return (negative ? 0xfff0000000000000L : 0x7ff0000000000000L);
			} else {
				return (negative ? 0x8000000000000000L : 0x0000000000000000L);
			}
		}
		// neither value is infinite

		// special handling of zero
		n1 = isZero(d1);
		n2 = isZero(d2);
		if (n1 || n2) {
			if (n1 && n2) {
				return 0x7ff8000000000000L;
			} else if (n1) {
				return (negative ? 0x8000000000000000L : 0x0000000000000000L);
			} else {
				return (negative ? 0xfff0000000000000L : 0x7ff0000000000000L);
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
			int s = BitUtils.countLeadingZeros(m1) - 1; // @WCA loop <= 228
			int t = BitUtils.countLeadingZeros(m);
			s = t < s ? t : s;
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
	public static long double_rem(long d1, long d2) {
		if (isNaN(d1) || isNaN(d2) || isInfinite(d1) || isZero(d2)) {
			return 0x7ff8000000000000L;
		} else if (isZero(d1) || isInfinite(d2)) {
			return d1;
		}

		// unpack
		int x1 = unpackExponent(d1);
		int x2 = unpackExponent(d2);
		if (x1 < x2) {
			return d1;
		}
		boolean n = d1 < 0L;
		long m1 = unpackMantissa(d1);
		long m2 = unpackMantissa(d2);
		if (x1 == x2) {
			m1 %= m2;
		} else {
			// reduce m1 by left shifting and modding until the exponents x1 and x2 are
			// equal
			int i = 0;
			while (x1 != x2) {
				// @WCA loop <= 206
				int s = BitUtils.countLeadingZeros(m1) - 1;
				s = x1-x2 < s ? x1-x2 : s;
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
	 * Mimics
	 * <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Math.html#round(double)">Math.round(double)</a>,
	 * using single precision.
	 */
	public static long double_round(long f) {
		return longValue(round(double_add(f,  0x3fe0000000000000L), false, false));
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
	    while (m < IMPLIED_ONE) { // @WCA loop <= 52
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
	    while (r != 0) { // @WCA loop = 53
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
	private static long round(long d, boolean round, boolean ceil) {
		if (isNaN(d)) {
			return 0x7ff8000000000000L;
		} else if (isZero(d) || isInfinite(d)) {
			return d;
		}
		int x = unpackExponent(d);
		if (x >= 0) {
			return d;
		}
		boolean n = d < 0L;
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
}
