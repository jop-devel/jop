/*
 * MathSupport.java
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

/*
 * Add (some) support functions from microfloat
 * see: http://www.dclausen.net/projects/microfloat/
 */
package com.jopdesign.sys;

import static com.jopdesign.sys.SoftFloat64.*;

public class MathSupport {

	  /** A constant holding the value of -1.0d */
	  public static final long NEGATIVE_ONE      = 0xbff0000000000000L;

	  /**
	   * A constant holding the same value as <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Double.html#NEGATIVE_INFINITY">Double.NEGATIVE_INFINITY</a>
	   */
	  public  static final long NEGATIVE_INFINITY = 0xfff0000000000000L;

	  /** A constant holding the value of 0.5d */
	  public static final long ONE_HALF          = 0x3fe0000000000000L;
	  
	  /** A constant holding the value of (1.0d / 3.0d) */
	  public static final long ONE_THIRD         = 0x3fd5555555555555L;

	  /** A constant holding the value of 1.5d */
	  public static final long THREE_HALVES      = 0x3ff8000000000000L;

	  /** A constant holding the natural logarithm of 2 */
	  public static final long LN2               = 0x3fe62e42fefa39efL;

	  /**
	   * Mimics <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Math.html#abs(double)">Math.abs(double)</a>
	   */
	  public static long abs(long d) {
	    //if (isNaN(d)) {
	    //  return NaN;
	    //}
	    return (d & ABS_MASK);
	  }
	  
	  private static int getHI(long d) {
		    return ((int) (d >> 32));
		  }
		  
		  private static int getLO(long d) {
		    return ((int) d);
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
          d = double_div(double_sub(scalbn(d, 1), ONE), double_add(TWO, d));
        } else { // 11/16<=|x|< 19/16 
          id = 1;
          d = double_div(double_sub(d, ONE), double_add(d, ONE));
        }
      } else {
        if (ix < 0x40038000) { // |x| < 2.4375 
          id = 2;
          d = double_div(double_sub(d, THREE_HALVES), double_add(ONE,
                  double_mul(THREE_HALVES, d)));
        } else { // 2.4375 <= |x| < 2^66 
          id = 3;
          d = double_div(NEGATIVE_ONE, d);
        }
      }
    }
    // end of argument reduction 
    long z = double_mul(d, d);
    long w = double_mul(z, z);
    // break sum from i=0 to 10 aT[i]z**(i+1) into odd and even poly 
    long s1 = double_mul(z, double_add(AT0, double_mul(w, double_add(AT2, double_mul(w, double_add(AT4, 
            double_mul(w, double_add(AT6, double_mul(w, double_add(AT8, double_mul(w, AT10)))))))))));
    long s2 = double_mul(w, double_add(AT1, double_mul(w, double_add(AT3, double_mul(w, double_add(AT5,
            double_mul(w, double_add(AT7, double_mul(w, AT9)))))))));
    if (id<0) {
      return double_sub(d, double_mul(d, double_add(s1, s2)));
    } else {
      z = double_sub(atanhi[id], double_sub(double_sub(double_mul(d, double_add(s1, s2)), 
              atanlo[id]), d));
      return (hx < 0) ? negate(z): z;
    }
  }
  
  private static long setHI(long d, int newHiPart) {
	    return ((d & 0x00000000FFFFFFFFL) | (((long) newHiPart) << 32));
	  }

  private static final long LN2_HI        = 0x3fe62e42fee00000L;
  private static final long LN2_LO        = 0x3dea39ef35793c76L; //  1.90821492927058770002e-10

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
    long f = double_sub(d, ONE);
    if ((0x000fffff&(2+hx))<3) { // |f| < 2**-20
      if (isZero(f)) {
        if (k == 0) {
          return ZERO;
        }
        long dk = intToDouble(k);
        return double_add(double_mul(dk, LN2_HI), double_mul(dk, LN2_LO));
      }
      long R = double_mul(double_mul(f, f), double_sub(ONE_HALF, 
              double_mul(ONE_THIRD, f)));
      if (k == 0) {
        return double_sub(f, R);
      } else {
        long dk = intToDouble(k);
        return double_sub(double_mul(dk, LN2_HI), double_sub(double_sub(R, 
                double_mul(dk, LN2_LO)), f));
      }
    }
    long dk = intToDouble(k);
    long s = double_div(f, double_add(TWO, f));
    long z = double_mul(s, s);
    long w = double_mul(z, z);
    long R = double_add(double_mul(w, double_add(LG2, double_mul(w, double_add(LG4, double_mul(w, LG6))))),
            double_mul(z, double_add(LG1, double_mul(w, double_add(LG3, double_mul(w, double_add(LG5, 
            double_mul(w, LG7))))))));
    i = (hx - 0x6147a) | (0x6b851 - hx);
    if (i > 0) {
      long hfsq = double_mul(scalbn(f, -1), f);
      if (k == 0) {
        return double_sub(f, double_sub(hfsq, double_mul(s, double_add(hfsq, R))));
      } else {
        return double_sub(double_mul(dk, LN2_HI), double_sub(double_sub(hfsq, 
               double_add(double_mul(s, double_add(hfsq, R)), double_mul(dk, LN2_LO))), f));
      }
    } else if (k==0) {
      return double_sub(f, double_mul(s, double_sub(f, R)));
    } 
    return double_sub(double_mul(dk, LN2_HI), double_sub(double_sub(double_mul(s, 
            double_sub(f, R)), double_mul(dk, LN2_LO)), f));
  }
}
