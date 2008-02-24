
/* This Java source file is derived from the SoftFloat IEC/IEEE
   Floating-point Arithmetic Package, Release 2b, which requires the
   following paragraphs to be included: */

/*============================================================================

This C source file is part of the SoftFloat IEC/IEEE Floating-point Arithmetic
Package, Release 2b.

Written by John R. Hauser.  This work was made possible in part by the
International Computer Science Institute, located at Suite 600, 1947 Center
Street, Berkeley, California 94704.  Funding was partially provided by the
National Science Foundation under grant MIP-9311980.  The original version
of this code was written as part of a project to build a fixed-point vector
processor in collaboration with the University of California at Berkeley,
overseen by Profs. Nelson Morgan and John Wawrzynek.  More information
is available through the Web page `http://www.cs.berkeley.edu/~jhauser/
arithmetic/SoftFloat.html'.

THIS SOFTWARE IS DISTRIBUTED AS IS, FOR FREE.  Although reasonable effort has
been made to avoid it, THIS SOFTWARE MAY CONTAIN FAULTS THAT WILL AT TIMES
RESULT IN INCORRECT BEHAVIOR.  USE OF THIS SOFTWARE IS RESTRICTED TO PERSONS
AND ORGANIZATIONS WHO CAN AND WILL TAKE FULL RESPONSIBILITY FOR ALL LOSSES,
COSTS, OR OTHER PROBLEMS THEY INCUR DUE TO THE SOFTWARE, AND WHO FURTHERMORE
EFFECTIVELY INDEMNIFY JOHN HAUSER AND THE INTERNATIONAL COMPUTER SCIENCE
INSTITUTE (possibly via similar legal warning) AGAINST ALL LOSSES, COSTS, OR
OTHER PROBLEMS INCURRED BY THEIR CUSTOMERS AND CLIENTS DUE TO THE SOFTWARE.

Derivative works are acceptable, even for commercial purposes, so long as
(1) the source code for the derivative work includes prominent notice that
the work is derivative, and (2) the source code includes prominent notice with
these four paragraphs for those parts of this code that are retained.

=============================================================================*/

/*
  Adapted to Java (2005, 2007) by Martin Schoeberl (martin@jopdesign.com)
*/

package com.jopdesign.sys;
/**
*	comments:
*		all 64 bit functions removed
*/

public class SoftFloat {

/*----------------------------------------------------------------------------
| Shifts `a' right by the number of bits given in `count'.  If any nonzero
| bits are shifted off, they are ``jammed'' into the least significant bit of
| the result by setting the least significant bit to 1.  The value of `count'
| can be arbitrarily large; in particular, if `count' is greater than 32, the
| result will be either 0 or 1, depending on whether `a' is zero or nonzero.
| The result is stored in the location pointed to by `zPtr'.
*----------------------------------------------------------------------------*/
    static int shift32RightJamming(int a, int count) {
	int z;

	if (count == 0) {
	    z = a;
	} else if (count < 32) {
	    z = (a >>> count) | (((a << ((-count) & 31)) != 0) ? 1 : 0);
	} else {
	    z = (a != 0) ? 1 : 0;
	}
	return z;
    }

/*----------------------------------------------------------------------------
| Returns the number of leading 0 bits before the most-significant 1 bit of
| `a'.  If `a' is zero, 32 is returned.
*----------------------------------------------------------------------------*/
    static int countLeadingZeros32(int a) {
	int cnt;
	for (cnt = 0; cnt < 32; ++cnt) {
	    if (a < 0) {	// MSB set
		break;
	    }
	    a <<= 1;
	}

	return cnt;
    }

/*----------------------------------------------------------------------------
| Takes an abstract floating-point value having sign `zSign', exponent `zExp',
| and significand `zSig', and returns the proper single-precision floating-
| point value corresponding to the abstract input.  Ordinarily, the abstract
| value is simply rounded and packed into the single-precision format, with
| the inexact exception raised if the abstract input cannot be represented
| exactly.  However, if the abstract value is too large, the overflow and
| inexact exceptions are raised and an infinity or maximal finite value is
| returned.  If the abstract value is too small, the input value is rounded to
| a subnormal number, and the underflow and inexact exceptions are raised if
| the abstract input cannot be represented exactly as a subnormal single-
| precision floating-point number.
|	 The input significand `zSig' has its binary point between bits 30
| and 29, which is 7 bits to the left of the usual location.  This shifted
| significand must be normalized or smaller.  If `zSig' is not normalized,
| `zExp' must be 0; in that case, the result returned is a subnormal number,
| and it must not require rounding.  In the usual case that `zSig' is
| normalized, `zExp' must be 1 less than the ``true'' floating-point exponent.
| The handling of underflow and overflow follows the IEC/IEEE Standard for
| Binary Floating-Point Arithmetic.
*----------------------------------------------------------------------------*/
    static int roundAndPackFloat32(int zSign, int zExp, int zSig) {
	int roundIncrement, roundBits;

	roundIncrement = 0x40;	// round to nearst in Java
	roundBits = zSig & 0x7f;
	if (0xfd <= (zExp & 0xffff)) {
	    if ((0xfd < zExp)
		|| ((zExp == 0xfd)
		    && ((zSig + roundIncrement) < 0))) {
		return (((zSign) << 31) + ((0xff) << 23)) -
		    ((roundIncrement == 0) ? 1 : 0);
	    }
	    if (zExp < 0) {
		zSig = shift32RightJamming(zSig, -zExp);
		zExp = 0;
		roundBits = zSig & 0x7f;
	    }
	}
	zSig = (zSig + roundIncrement) >>> 7;
	zSig &= ~(((roundBits ^ 0x40) == 0) ? 1 : 0);
	if (zSig == 0) {
	    zExp = 0;
	}
	return (((zSign) << 31) + ((zExp) << 23) + zSig);
    }

/*----------------------------------------------------------------------------
| Takes an abstract floating-point value having sign `zSign', exponent `zExp',
| and significand `zSig', and returns the proper single-precision floating-
| point value corresponding to the abstract input.  This routine is just like
| `roundAndPackFloat32' except that `zSig' does not have to be normalized.
| Bit 31 of `zSig' must be zero, and `zExp' must be 1 less than the ``true''
| floating-point exponent.
*----------------------------------------------------------------------------*/
    static int normalizeRoundAndPackFloat32(int zSign, int zExp, int zSig) {
	int shiftCount;

	int a = zSig;
	for (shiftCount = 0; shiftCount < 32; ++shiftCount) {
	    if (a < 0) {	// MSB set
		break;
	    }
	    a <<= 1;
	}
	--shiftCount;

	return roundAndPackFloat32(zSign, zExp - shiftCount,
				   zSig << shiftCount);
    }


/*----------------------------------------------------------------------------
| Returns the result of converting the 32-bit two's complement integer `a' to
| the single-precision floating-point format.  The conversion is performed
| according to the IEC/IEEE Standard for Binary Floating-Point Arithmetic.
*----------------------------------------------------------------------------*/
    public static int int32_to_float32(int a) {
	int zSign;

	if (a == 0) {
	    return 0;
	}
	if (a == 0x80000000) {
	    return ((1) << 31) + ((0x9E) << 23);
	}

	zSign = a >>> 31;
	return normalizeRoundAndPackFloat32(zSign, 0x9C,
					    (zSign != 0) ? -a : a);
    }

/*----------------------------------------------------------------------------
| Returns the result of converting the single-precision floating-point value
| `a' to the 32-bit two's complement integer format.  The conversion is
| performed according to the IEC/IEEE Standard for Binary Floating-Point
| Arithmetic---which means in particular that the conversion is rounded
| according to the current rounding mode.  If `a' is a NaN, the largest
| positive integer is returned.  Otherwise, if the conversion overflows, the
| largest integer with the same sign as `a' is returned.
*----------------------------------------------------------------------------*/
    public static int float32_to_int32( int a ) {
	int aSign;
	int aExp, shiftCount;
	int aSig, aSigExtra;
	int z;

	aSig = a & 0x007fffff;
	aExp = ( a>>>23 ) & 0xff;
	aSign = a>>>31;
	shiftCount = aExp - 0x9e;

	if ( shiftCount >= 0 ) {
	    if ( ( aExp == 0xff ) && (aSig != 0) ) {
		return 0;
	    } else if ( aSign!=0 ) {
		return 0x80000000;
	    } else {
		return 0x7fffffff;
	    }
	} else if ( shiftCount == -33 ) {
	    return ((aSign == 0) && (aSig == 0x7fffff)) ? 1 : 0;
	} else if ( shiftCount < -33 ) {
	    return 0;
	}

	aSig |= 0x00800000;
	aSig <<= 7;

	z = aSig >>> -shiftCount-1;
	
	aSig = shift32RightJamming(aSig, -shiftCount-9) & 0xff;

	// those fine rounding rules
	if (aSign == 0) {
	    if (aSig >= 0x80) {
		z += 1;
	    } else if ((aExp == 0x96) && ((z & 1) != 0)) {
		z += 1;
	    }
	} else {
	    if (aSig > 0x80) {
		z += 1;
	    } else if ((aExp == 0x96) && (aSig == 1) && ((z & 1) != 0)) {
		z -= 1;
	    }
	}
	
	if ( aSign != 0 ) {
	    z = -z;
	}

	return z;
    }

/*----------------------------------------------------------------------------
| Returns the result of converting the single-precision floating-point value
| `a' to the 32-bit two's complement integer format.  The conversion is
| performed according to the IEC/IEEE Standard for Binary Floating-Point
| Arithmetic, except that the conversion is always rounded toward zero.
| If `a' is a NaN, the largest positive integer is returned.  Otherwise, if
| the conversion overflows, the largest integer with the same sign as `a' is
| returned.
*----------------------------------------------------------------------------*/
    public static int float32_to_int32_round_to_zero(int a) {
	int aSign;
	int aExp, shiftCount;
	int aSig;
	int z;

	aSig = a & 0x007fffff;
	aExp = (a >>> 23) & 0xff;
	aSign = a >>> 31;
	shiftCount = aExp - 0x9e;

	if (0 <= shiftCount) {
	    if (((aExp == 0xff) && aSig != 0)) {	// NaN
		// NaN has to return 0 in Java!
		// That is different form IEEE 754
		return 0;
	    } else if (aSign == 0) {	// +INF
		return 0x7FFFFFFF;
	    }
	    return 0x80000000;
	} else if (aExp <= 0x7E) {
	    return 0;
	}

	aSig = (aSig | 0x00800000) << 8;
	z = aSig >>> -shiftCount;

	if (aSign != 0) {
	    z = -z;
	}

	return z;
    }


///*----------------------------------------------------------------------------
//| Rounds the single-precision floating-point value `a' to an integer,
//| and returns the result as a single-precision floating-point value.  The
//| operation is performed according to the IEC/IEEE Standard for Binary
//| Floating-Point Arithmetic.
//*----------------------------------------------------------------------------*/
//
//float32 float32_round_to_int( float32 a )
//{
//      flag aSign;
//      int16 aExp;
//      bits32 lastBitMask, roundBitsMask;
//      int8 roundingMode;
//      float32 z;
//
//      aExp = extractFloat32Exp( a );
//      if ( 0x96 <= aExp ) {
//              if ( ( aExp == 0xFF ) && extractFloat32Frac( a ) ) {
//                      return propagateFloat32NaN( a, a );
//              }
//              return a;
//      }
//      if ( aExp <= 0x7E ) {
//              if ( (bits32) ( a<<1 ) == 0 ) return a;
//              float_exception_flags |= float_flag_inexact;
//              aSign = extractFloat32Sign( a );
//              switch ( float_rounding_mode ) {
//               case float_round_nearest_even:
//                      if ( ( aExp == 0x7E ) && extractFloat32Frac( a ) ) {
//                              return packFloat32( aSign, 0x7F, 0 );
//                      }
//                      break;
//               case float_round_down:
//                      return aSign ? 0xBF800000 : 0;
//               case float_round_up:
//                      return aSign ? 0x80000000 : 0x3F800000;
//              }
//              return packFloat32( aSign, 0, 0 );
//      }
//      lastBitMask = 1;
//      lastBitMask <<= 0x96 - aExp;
//      roundBitsMask = lastBitMask - 1;
//      z = a;
//      roundingMode = float_rounding_mode;
//      if ( roundingMode == float_round_nearest_even ) {
//              z += lastBitMask>>1;
//              if ( ( z & roundBitsMask ) == 0 ) z &= ~ lastBitMask;
//      }
//      else if ( roundingMode != float_round_to_zero ) {
//              if ( extractFloat32Sign( z ) ^ ( roundingMode == float_round_up ) ) {
//                      z += roundBitsMask;
//              }
//      }
//      z &= ~ roundBitsMask;
//      if ( z != a ) float_exception_flags |= float_flag_inexact;
//      return z;
//
//}
//

/*----------------------------------------------------------------------------
| Returns the result of adding the absolute values of the single-precision
| floating-point values `a' and `b'.  If `zSign' is 1, the sum is negated
| before being returned.  `zSign' is ignored if the result is a NaN.
| The addition is performed according to the IEC/IEEE Standard for Binary
| Floating-Point Arithmetic.
*----------------------------------------------------------------------------*/
    static int addFloat32Sigs(int a, int b, int zSign) {
	int aExp, bExp, zExp;
	int aSig, bSig, zSig;
	int expDiff;

	aSig = a & 0x007FFFFF;
	aExp = (a >>> 23) & 0xff;
	bSig = b & 0x007FFFFF;
	bExp = (b >>> 23) & 0xff;
	expDiff = aExp - bExp;
	aSig <<= 6;
	bSig <<= 6;

	if (0 < expDiff) {
	    if (aExp == 0xff) {
		return a;
	    }
	    if (bExp == 0) {
		--expDiff;
	    } else {
		bSig |= 0x20000000;
	    }
	    bSig = shift32RightJamming(bSig, expDiff);
	    zExp = aExp;
	} else if (expDiff < 0) {
	    if (bExp == 0xff) {
		if (bSig != 0) {
		    return b;
		}
		return (((zSign) << 31) + ((0xff) << 23));
	    }
	    if (aExp == 0) {
		++expDiff;
	    } else {
		aSig |= 0x20000000;
	    }
	    aSig = shift32RightJamming(aSig, -expDiff);
	    zExp = bExp;
	} else {
	    if (aExp == 0xff) {
		if (aSig != 0 || bSig != 0) {
		    return 0x7fc00000;
		}
		return a;
	    }
	    if (aExp == 0) {
		return (((zSign) << 31) + ((aSig + bSig) >>> 6));
	    }
	    zSig = 0x40000000 + aSig + bSig;
	    zExp = aExp;
	    return roundAndPackFloat32(zSign, zExp, zSig);
	}
	aSig |= 0x20000000;
	zSig = (aSig + bSig) << 1;
	--zExp;
	if (zSig < 0) {
	    zSig = aSig + bSig;
	    ++zExp;
	}
	return roundAndPackFloat32(zSign, zExp, zSig);
    }

/*----------------------------------------------------------------------------
| Returns the result of subtracting the absolute values of the single-
| precision floating-point values `a' and `b'.  If `zSign' is 1, the
| difference is negated before being returned.  `zSign' is ignored if the
| result is a NaN.  The subtraction is performed according to the IEC/IEEE
| Standard for Binary Floating-Point Arithmetic.
*----------------------------------------------------------------------------*/
    static int subFloat32Sigs(int a, int b, int zSign) {
	int aExp, bExp, zExp;
	int aSig, bSig, zSig;
	int expDiff;

	aSig = a & 0x007FFFFF;
	aExp = (a >>> 23) & 0xff;
	bSig = b & 0x007FFFFF;
	bExp = (b >>> 23) & 0xff;
	expDiff = aExp - bExp;
	aSig <<= 7;
	bSig <<= 7;

	if (0 < expDiff) {
	    if (aExp == 0xff) {
		return a;
	    }
	    if (bExp == 0) {
		--expDiff;
	    } else {
		bSig |= 0x40000000;
	    }
	    bSig = shift32RightJamming(bSig, expDiff);
	    aSig |= 0x40000000;
	    zSig = aSig - bSig;
	    zExp = aExp;
	    --zExp;
	    return normalizeRoundAndPackFloat32(zSign, zExp, zSig);
	} else if (expDiff < 0) {
	    if (bExp == 0xff) {
		if (bSig != 0) {
		    return b;
		}
		return (((zSign ^ 1) << 31) + ((0xff) << 23));
	    }
	    if (aExp == 0) {
		++expDiff;
	    } else {
		aSig |= 0x40000000;
	    }
	    aSig = shift32RightJamming(aSig, -expDiff);
	    bSig |= 0x40000000;
	    zSig = bSig - aSig;
	    zExp = bExp;
	    zSign ^= 1;
	    --zExp;
	    return normalizeRoundAndPackFloat32(zSign, zExp, zSig);
	} else {
	    if (aExp == 0xff) {
		if (aSig != 0 || bSig != 0)
		    return 0x7fc00000;
		return 0xffc00000;
	    }
	    if (aExp == 0) {
		aExp = 1;
		bExp = 1;
	    }
	    if (bSig < aSig) {
		zSig = aSig - bSig;
		zExp = aExp;
		--zExp;
		return normalizeRoundAndPackFloat32(zSign, zExp, zSig);
	    }
	    if (aSig < bSig) {
		zSig = bSig - aSig;
		zExp = bExp;
		zSign ^= 1;
		--zExp;
		return normalizeRoundAndPackFloat32(zSign, zExp, zSig);
	    }
	}
	return 0;
    }

/*----------------------------------------------------------------------------
| Returns the result of adding the single-precision floating-point values `a'
| and `b'.  The operation is performed according to the IEC/IEEE Standard for
| Binary Floating-Point Arithmetic.
*----------------------------------------------------------------------------*/
    public static int float32_add(int a, int b) {
	int aSign, bSign;

	aSign = a >>> 31;
	bSign = b >>> 31;
	if (aSign == bSign) {
	    return addFloat32Sigs(a, b, aSign);
	} else {
	    return subFloat32Sigs(a, b, aSign);
	}
    }

/*----------------------------------------------------------------------------
| Returns the result of subtracting the single-precision floating-point values
| `a' and `b'.  The operation is performed according to the IEC/IEEE Standard
| for Binary Floating-Point Arithmetic.
*----------------------------------------------------------------------------*/
    public static int float32_sub(int a, int b) {
	int aSign, bSign;

	aSign = a >>> 31;
	bSign = b >>> 31;
	if (aSign == bSign) {
	    return subFloat32Sigs(a, b, aSign);
	} else {
	    return addFloat32Sigs(a, b, aSign);
	}
    }

/*----------------------------------------------------------------------------
| Returns the result of multiplying the single-precision floating-point values
| `a' and `b'.  The operation is performed according to the IEC/IEEE Standard
| for Binary Floating-Point Arithmetic.
*----------------------------------------------------------------------------*/
    public static int float32_mul(int a, int b) {
	int aSign, bSign, zSign;
	int aExp, bExp, zExp;
	int aSig, bSig;
	long zSig;

	aSig = a & 0x007fffff;
	aExp = (a >>> 23) & 0xff;
	aSign = a >>> 31;
	bSig = b & 0x007fffff;
	bExp = (b >>> 23) & 0xff;
	bSign = b >>> 31;
	zSign = aSign ^ bSign;

	if (aExp == 0xff) {
	    if (aSig != 0) {
		return a;
	    }
	    if ((bExp == 0xff) && (bSig != 0)) {
		return b;
	    }
	    if ((bExp | bSig) == 0) {
		return 0xffc00000;
	    }
	    return (zSign << 31) | 0x7f800000;
	}
	if (bExp == 0xff) {
	    if (bSig != 0) {
		return b;
	    }
	    if ((aExp | aSig) == 0) {
		return 0xffc00000;
	    }
	    return (zSign << 31) | 0x7f800000;
	}
	if (aExp == 0) {
	    if (aSig == 0) {
		return (zSign << 31);
	    }
	    int shiftCount;
	    shiftCount = countLeadingZeros32(aSig) - 8;
	    aSig = aSig << shiftCount;
	    aExp = 1 - shiftCount;
	}
	if (bExp == 0) {
	    if (bSig == 0) {
		return (zSign << 31);
	    }
	    int shiftCount;
	    shiftCount = countLeadingZeros32(bSig) - 8;
	    bSig = bSig << shiftCount;
	    bExp = 1 - shiftCount;
	}

	zExp = aExp + bExp - 0x7f;
	aSig = (aSig | 0x00800000);
	bSig = (bSig | 0x00800000);

	zSig = ((long) aSig << 7) * ((long) bSig << 8);

	// normalize
	if (0 <= (zSig << 1)) {
	    zSig <<= 1;
	    --zExp;
	}

	// shift right with sticky bit
	if ((int) zSig != 0) {
	    zSig >>>= 32;
	    zSig |= 1;
	} else {
	    zSig >>>= 32;
	}

//     System.out.println(Integer.toHexString(aSig) + " E" +
//                     Integer.toHexString(aExp) + ",\t" +
//                     Integer.toHexString(bSig) + " E" +
//                     Integer.toHexString(bExp) + ",\t" +
//                     Long.toHexString(zSig) + " E" +
//                     Integer.toHexString(zExp));

	return roundAndPackFloat32(zSign, zExp, (int) zSig);
    }
//
///*----------------------------------------------------------------------------
//| Returns the result of dividing the single-precision floating-point value `a'
//| by the corresponding value `b'.  The operation is performed according to the
//| IEC/IEEE Standard for Binary Floating-Point Arithmetic.
//*----------------------------------------------------------------------------*/
    public static int float32_div(int a, int b) {
	int aSign, bSign, zSign;
	int aExp, bExp, zExp;
	int aSig, bSig;
	long zSig;

	aSig = a & 0x007FFFFF;
	aExp = (a >>> 23) & 0xff;
	aSign = a >>> 31;
	bSig = b & 0x007FFFFF;
	bExp = (b >>> 23) & 0xff;
	bSign = b >>> 31;
	zSign = aSign ^ bSign;

	if (aExp == 0xff) {
	    if (aSig != 0)
		return a;
	    if (bExp == 0xff) {
		if (bSig != 0) {
		    return b;
		}
		return 0x7fc00000;
	    }
	    return (zSign << 31) | 0x7f800000;
	}
	if (bExp == 0xff) {
	    if (bSig != 0) {
		return b;
	    }
	    return (zSign << 31);
	}
	if (bExp == 0) {
	    if (bSig == 0) {
		if ((aExp | aSig) == 0) {
		    return 0x7fc00000;
		}
		return (zSign << 31) | 0x7f800000;
	    }
	    int shiftCount;
	    shiftCount = countLeadingZeros32(bSig) - 8;
	    bSig = bSig << shiftCount;
	    bExp = 1 - shiftCount;
	}
	if (aExp == 0) {
	    if (aSig == 0) {
		return (zSign << 31);
	    }
	    int shiftCount;
	    shiftCount = countLeadingZeros32(aSig) - 8;
	    aSig = aSig << shiftCount;
	    aExp = 1 - shiftCount;
	}

	zExp = aExp - bExp + 0x7e;
	aSig = (aSig | 0x00800000);
	bSig = (bSig | 0x00800000);
	zSig = 0;

	zSig = ((long) aSig << 38) / (bSig);

	// shift right with sticky bit
	if ((zSig & 0xff) != 0) {
	    zSig >>>= 8;
	    zSig |= 1;
	} else {
	    zSig >>>= 8;
	}

	// rounding, with all those nice exceptions
	if ((zExp > 0) && ((zSig & 0x3f) == 0x20)) {
	    zSig |= 1;
	} else if ((zSig & 0x7f) == 0x40) {
	    zSig |= 1;
	} else if ((zExp == -1) && ((zSig & 0xff) == 0x80)) {
	    zSig |= 1;
	} else if ((zExp < -1) && ((((long) aSig << 38) % (bSig)) != 0)) {
	    zSig |= 1;
	}

//     System.out.println(Integer.toHexString(aSig) + " E" +
//                     Integer.toHexString(aExp) + ",\t" +
//                     Integer.toHexString(bSig) + " E" +
//                     Integer.toHexString(bExp) + ",\t" +
//                     Long.toHexString(zSig) + " E" +
//                     Integer.toHexString(zExp));

	return normalizeRoundAndPackFloat32(zSign, aExp - bExp + 0x7e,
					    (int) zSig);
    }

///*----------------------------------------------------------------------------
//| Returns the remainder of the single-precision floating-point value `a'
//| with respect to the corresponding value `b'.  The operation is performed
//| according to the IEC/IEEE Standard for Binary Floating-Point Arithmetic.
//*----------------------------------------------------------------------------*/
    public static int float32_rem(int a, int b) {
	int aSign, bSign, rSign;
	int aExp, bExp, rExp;
	int aSig, bSig, rSig;
	int expDiff;

	aSig = a & 0x007FFFFF;
	aExp = (a >>> 23) & 0xff;
	aSign = a >>> 31;
	bSig = b & 0x007FFFFF;
	bExp = (b >>> 23) & 0xff;
	bSign = b >>> 31;

	if (aExp == 0xff) {
	    return 0x7fc00000;
	}
	if (bExp == 0xff) {
	    if (bSig != 0) {
		return b;
	    }
	    return a;
	}
	if (bExp == 0) {
	    if (bSig == 0) {
		return 0x7fc00000;
	    }
	    int shiftCount;
	    shiftCount = countLeadingZeros32(bSig) - 8;
	    bSig = bSig << shiftCount;
	    bExp = 1 - shiftCount;
	}
	if (aExp == 0) {
	    if (aSig == 0) {
		return a;
	    }
	    int shiftCount;
	    shiftCount = countLeadingZeros32(aSig) - 8;
	    aSig = aSig << shiftCount;
	    aExp = 1 - shiftCount;
	}

	expDiff = aExp - bExp;
	aSig = (aSig | 0x00800000);
	bSig = (bSig | 0x00800000);

	if (expDiff < 0) {
	    if (expDiff < -1) {
		return a;
	    }
	    --bExp;
	} else {
	    // this could be made faster, maybe
	    while (expDiff > 0) {
		if (aSig >= bSig) {
		    aSig = aSig - bSig;
		}
		aSig <<= 1;
		--expDiff;
	    }
	    if (aSig >= bSig) {
		aSig = aSig - bSig;
	    }
	}

//     System.out.println(Integer.toHexString(aSig) + " E" +
//                     Integer.toHexString(aExp) + ",\t" +
//                     Integer.toHexString(bSig) + " E" +
//                     Integer.toHexString(bExp) + ",\t" +
//                     Integer.toHexString(expDiff));

	return normalizeRoundAndPackFloat32(aSign, bExp, aSig << 6);
    }

///*----------------------------------------------------------------------------
//| Returns the square root of the single-precision floating-point value `a'.
//| The operation is performed according to the IEC/IEEE Standard for Binary
//| Floating-Point Arithmetic.
//*----------------------------------------------------------------------------*/
//
//float32 float32_sqrt( float32 a )
//{
//      flag aSign;
//      int16 aExp, zExp;
//      bits32 aSig, zSig, rem0, rem1, term0, term1;
//
//      aSig = extractFloat32Frac( a );
//      aExp = extractFloat32Exp( a );
//      aSign = extractFloat32Sign( a );
//      if ( aExp == 0xFF ) {
//              if ( aSig ) return propagateFloat32NaN( a, 0 );
//              if ( ! aSign ) return a;
//              float_raise( float_flag_invalid );
//              return float32_default_nan;
//      }
//      if ( aSign ) {
//              if ( ( aExp | aSig ) == 0 ) return a;
//              float_raise( float_flag_invalid );
//              return float32_default_nan;
//      }
//      if ( aExp == 0 ) {
//              if ( aSig == 0 ) return 0;
//              normalizeFloat32Subnormal( aSig, &aExp, &aSig );
//      }
//      zExp = ( ( aExp - 0x7F )>>1 ) + 0x7E;
//      aSig = ( aSig | 0x00800000 )<<8;
//      zSig = estimateSqrt32( aExp, aSig ) + 2;
//      if ( ( zSig & 0x7F ) <= 5 ) {
//              if ( zSig < 2 ) {
//                      zSig = 0x7FFFFFFF;
//                      goto roundAndPack;
//              }
//              else {
//                      aSig >>= aExp & 1;
//                      mul32To64( zSig, zSig, &term0, &term1 );
//                      sub64( aSig, 0, term0, term1, &rem0, &rem1 );
//                      while ( (sbits32) rem0 < 0 ) {
//                              --zSig;
//                              shortShift64Left( 0, zSig, 1, &term0, &term1 );
//                              term1 |= 1;
//                              add64( rem0, rem1, term0, term1, &rem0, &rem1 );
//                      }
//                      zSig |= ( ( rem0 | rem1 ) != 0 );
//              }
//      }
//      shift32RightJamming( zSig, 1, &zSig );
// roundAndPack:
//      return roundAndPackFloat32( 0, zExp, zSig );
//
//}
//
/*----------------------------------------------------------------------------
| Returns 1 if the single-precision floating-point value `a' is greater to
| the corresponding value `b', -1 if smaller and 0 if equal.  The comparison is performed
| according to the IEC/IEEE Standard for Binary Floating-Point Arithmetic.
| NaN resturns 1 or -1 depending on cmpg of cmpl.
*----------------------------------------------------------------------------*/

    public static int float32_cmpg(int a, int b) {

	if (((((a >>> 23) & 0xff) == 0xff) && ((a & 0x007FFFFF) != 0)) ||
	    ((((b >>> 23) & 0xff) == 0xff) && ((b & 0x007FFFFF) != 0))) {
	    return 1;		// one is NaN
	}
	return float32_cmp(a, b);
    }
    public static int float32_cmpl(int a, int b) {
	if (((((a >>> 23) & 0xff) == 0xff) && ((a & 0x007FFFFF) != 0)) ||
	    ((((b >>> 23) & 0xff) == 0xff) && ((b & 0x007FFFFF) != 0))) {
	    return -1;		// one is NaN
	}
	return float32_cmp(a, b);
    }

    static int float32_cmp(int a, int b) {
	// test for equal
	if (a == b)
	    return 0;
	if (((a | b) << 1) == 0)
	    return 0;		// positiv zero and negative zero are considered euqal

	// test for lt
	int aSign = a >>> 31;
	int bSign = b >>> 31;
	if (aSign != bSign) {
	    if (aSign != 0 && (((a | b) << 1) != 0)) {
		return -1;
	    } else {
		return 1;
	    }
	}
	if ((a != b) && ((aSign ^ ((a < b) ? 1 : 0)) != 0)) {
	    return -1;
	} else {
	    return 1;
	}
    }

}				// end class
