
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

package com.jopdesign.sys;
/**
*	comments:
*		all 64 bit functions removed
*/

public class SoftFloat {

// processor include 
//typedef int flag;
//typedef int uint8;
//typedef int int8;
//typedef int uint16;
//typedef int int16;
//typedef unsigned int uint32;
//typedef signed int int32;
//#ifdef BITS64
//typedef unsigned long long int uint64;
//typedef signed long long int int64;
//#endif
//
///*----------------------------------------------------------------------------
//| Each of the following `typedef's defines a type that holds integers
//| of _exactly_ the number of bits specified.  For instance, for most
//| implementation of C, `bits16' and `sbits16' should be `typedef'ed to
//| `unsigned short int' and `signed short int' (or `short int'), respectively.
//*----------------------------------------------------------------------------*/
//typedef unsigned char bits8;
//typedef signed char sbits8;
//typedef unsigned short int bits16;
//typedef signed short int sbits16;
//typedef unsigned int bits32;
//typedef signed int sbits32;
//#ifdef BITS64
//typedef unsigned long long int bits64;
//typedef signed long long int sbits64;
//#endif

/*----------------------------------------------------------------------------
| The `LIT64' macro takes as its argument a textual integer literal and
// #include "milieu.h"
// #include "softfloat.h"

/*----------------------------------------------------------------------------
| Floating-point rounding mode and exception flags.
*----------------------------------------------------------------------------*/
// int8 float_rounding_mode = float_round_nearest_even;
// int8 float_exception_flags = 0;

/*----------------------------------------------------------------------------
| Primitive arithmetic functions, including multi-word arithmetic, and
| division and square root approximations.  (Can be specialized to target if
| desired.)
*----------------------------------------------------------------------------*/

// #include "softfloat-macros"
//
/*----------------------------------------------------------------------------
| Shifts `a' right by the number of bits given in `count'.  If any nonzero
| bits are shifted off, they are ``jammed'' into the least significant bit of
| the result by setting the least significant bit to 1.  The value of `count'
| can be arbitrarily large; in particular, if `count' is greater than 32, the
| result will be either 0 or 1, depending on whether `a' is zero or nonzero.
| The result is stored in the location pointed to by `zPtr'.
*----------------------------------------------------------------------------*/

//INLINE void shift32RightJamming( bits32 a, int16 count, bits32 *zPtr )
static int shift32RightJamming( int a, int count )
{
	// bits32 z;
	int z;

	if ( count == 0 ) {
		z = a;
	}
	else if ( count < 32 ) {
		z = ( a>>>count ) | (( ( a<<( ( -count ) & 31 ) ) != 0 ) ? 1 : 0);
	}
	else {
		z = ( a != 0 ) ? 1 : 0;
	}
//	*zPtr = z;
	return z;

}

///*----------------------------------------------------------------------------
//| Shifts the 64-bit value formed by concatenating `a0' and `a1' left by the
//| number of bits given in `count'.  Any bits shifted off are lost.  The value
//| of `count' must be less than 32.  The result is broken into two 32-bit
//| pieces which are stored at the locations pointed to by `z0Ptr' and `z1Ptr'.
//*----------------------------------------------------------------------------*/
//
//INLINE void
// shortShift64Left(
//	 bits32 a0, bits32 a1, int16 count, bits32 *z0Ptr, bits32 *z1Ptr )
//{
//
//	*z1Ptr = a1<<count;
//	*z0Ptr =
//		( count == 0 ) ? a0 : ( a0<<count ) | ( a1>>( ( - count ) & 31 ) );
//
//}
///*----------------------------------------------------------------------------
//| Adds the 64-bit value formed by concatenating `a0' and `a1' to the 64-bit
//| value formed by concatenating `b0' and `b1'.  Addition is modulo 2^64, so
//| any carry out is lost.  The result is broken into two 32-bit pieces which
//| are stored at the locations pointed to by `z0Ptr' and `z1Ptr'.
//*----------------------------------------------------------------------------*/
//
//INLINE void
// add64(
//	 bits32 a0, bits32 a1, bits32 b0, bits32 b1, bits32 *z0Ptr, bits32 *z1Ptr )
//{
//	bits32 z1;
//
//	z1 = a1 + b1;
//	*z1Ptr = z1;
//	*z0Ptr = a0 + b0 + ( z1 < a1 );
//
//}
//
///*----------------------------------------------------------------------------
//| Adds the 96-bit value formed by concatenating `a0', `a1', and `a2' to the
//| 96-bit value formed by concatenating `b0', `b1', and `b2'.  Addition is
//| modulo 2^96, so any carry out is lost.  The result is broken into three
//| 32-bit pieces which are stored at the locations pointed to by `z0Ptr',
//| `z1Ptr', and `z2Ptr'.
//*----------------------------------------------------------------------------*/
//
//INLINE void
// add96(
//	 bits32 a0,
//	 bits32 a1,
//	 bits32 a2,
//	 bits32 b0,
//	 bits32 b1,
//	 bits32 b2,
//	 bits32 *z0Ptr,
//	 bits32 *z1Ptr,
//	 bits32 *z2Ptr
// )
//{
//	bits32 z0, z1, z2;
//	int8 carry0, carry1;
//
//	z2 = a2 + b2;
//	carry1 = ( z2 < a2 );
//	z1 = a1 + b1;
//	carry0 = ( z1 < a1 );
//	z0 = a0 + b0;
//	z1 += carry1;
//	z0 += ( z1 < carry1 );
//	z0 += carry0;
//	*z2Ptr = z2;
//	*z1Ptr = z1;
//	*z0Ptr = z0;
//
//}
//
///*----------------------------------------------------------------------------
//| Subtracts the 64-bit value formed by concatenating `b0' and `b1' from the
//| 64-bit value formed by concatenating `a0' and `a1'.  Subtraction is modulo
//| 2^64, so any borrow out (carry out) is lost.  The result is broken into two
//| 32-bit pieces which are stored at the locations pointed to by `z0Ptr' and
//| `z1Ptr'.
//*----------------------------------------------------------------------------*/
//
//INLINE void
// sub64(
//	 bits32 a0, bits32 a1, bits32 b0, bits32 b1, bits32 *z0Ptr, bits32 *z1Ptr )
//{
//
//	*z1Ptr = a1 - b1;
//	*z0Ptr = a0 - b0 - ( a1 < b1 );
//
//}
//
///*----------------------------------------------------------------------------
//| Subtracts the 96-bit value formed by concatenating `b0', `b1', and `b2' from
//| the 96-bit value formed by concatenating `a0', `a1', and `a2'.  Subtraction
//| is modulo 2^96, so any borrow out (carry out) is lost.  The result is broken
//| into three 32-bit pieces which are stored at the locations pointed to by
//| `z0Ptr', `z1Ptr', and `z2Ptr'.
//*----------------------------------------------------------------------------*/
//
//INLINE void
// sub96(
//	 bits32 a0,
//	 bits32 a1,
//	 bits32 a2,
//	 bits32 b0,
//	 bits32 b1,
//	 bits32 b2,
//	 bits32 *z0Ptr,
//	 bits32 *z1Ptr,
//	 bits32 *z2Ptr
// )
//{
//	bits32 z0, z1, z2;
//	int8 borrow0, borrow1;
//
//	z2 = a2 - b2;
//	borrow1 = ( a2 < b2 );
//	z1 = a1 - b1;
//	borrow0 = ( a1 < b1 );
//	z0 = a0 - b0;
//	z0 -= ( z1 < borrow1 );
//	z1 -= borrow1;
//	z0 -= borrow0;
//	*z2Ptr = z2;
//	*z1Ptr = z1;
//	*z0Ptr = z0;
//
//}
//
///*----------------------------------------------------------------------------
//| Multiplies `a' by `b' to obtain a 64-bit product.  The product is broken
//| into two 32-bit pieces which are stored at the locations pointed to by
//| `z0Ptr' and `z1Ptr'.
//*----------------------------------------------------------------------------*/
//
//INLINE void mul32To64( bits32 a, bits32 b, bits32 *z0Ptr, bits32 *z1Ptr )
//{
//	bits16 aHigh, aLow, bHigh, bLow;
//	bits32 z0, zMiddleA, zMiddleB, z1;
//
//	aLow = a;
//	aHigh = a>>16;
//	bLow = b;
//	bHigh = b>>16;
//	z1 = ( (bits32) aLow ) * bLow;
//	zMiddleA = ( (bits32) aLow ) * bHigh;
//	zMiddleB = ( (bits32) aHigh ) * bLow;
//	z0 = ( (bits32) aHigh ) * bHigh;
//	zMiddleA += zMiddleB;
//	z0 += ( ( (bits32) ( zMiddleA < zMiddleB ) )<<16 ) + ( zMiddleA>>16 );
//	zMiddleA <<= 16;
//	z1 += zMiddleA;
//	z0 += ( z1 < zMiddleA );
//	*z1Ptr = z1;
//	*z0Ptr = z0;
//
//}
//
///*----------------------------------------------------------------------------
//| Multiplies the 64-bit value formed by concatenating `a0' and `a1' by `b'
//| to obtain a 96-bit product.  The product is broken into three 32-bit pieces
//| which are stored at the locations pointed to by `z0Ptr', `z1Ptr', and
//| `z2Ptr'.
//*----------------------------------------------------------------------------*/
//
//INLINE void
// mul64By32To96(
//	 bits32 a0,
//	 bits32 a1,
//	 bits32 b,
//	 bits32 *z0Ptr,
//	 bits32 *z1Ptr,
//	 bits32 *z2Ptr
// )
//{
//	bits32 z0, z1, z2, more1;
//
//	mul32To64( a1, b, &z1, &z2 );
//	mul32To64( a0, b, &z0, &more1 );
//	add64( z0, more1, 0, z1, &z0, &z1 );
//	*z2Ptr = z2;
//	*z1Ptr = z1;
//	*z0Ptr = z0;
//
//}
//
///*----------------------------------------------------------------------------
//| Multiplies the 64-bit value formed by concatenating `a0' and `a1' to the
//| 64-bit value formed by concatenating `b0' and `b1' to obtain a 128-bit
//| product.  The product is broken into four 32-bit pieces which are stored at
//| the locations pointed to by `z0Ptr', `z1Ptr', `z2Ptr', and `z3Ptr'.
//*----------------------------------------------------------------------------*/
//
//INLINE void
// mul64To128(
//	 bits32 a0,
//	 bits32 a1,
//	 bits32 b0,
//	 bits32 b1,
//	 bits32 *z0Ptr,
//	 bits32 *z1Ptr,
//	 bits32 *z2Ptr,
//	 bits32 *z3Ptr
// )
//{
//	bits32 z0, z1, z2, z3;
//	bits32 more1, more2;
//
//	mul32To64( a1, b1, &z2, &z3 );
//	mul32To64( a1, b0, &z1, &more2 );
//	add64( z1, more2, 0, z2, &z1, &z2 );
//	mul32To64( a0, b0, &z0, &more1 );
//	add64( z0, more1, 0, z1, &z0, &z1 );
//	mul32To64( a0, b1, &more1, &more2 );
//	add64( more1, more2, 0, z2, &more1, &z2 );
//	add64( z0, z1, 0, more1, &z0, &z1 );
//	*z3Ptr = z3;
//	*z2Ptr = z2;
//	*z1Ptr = z1;
//	*z0Ptr = z0;
//
//}
//
///*----------------------------------------------------------------------------
//| Returns an approximation to the 32-bit integer quotient obtained by dividing
//| `b' into the 64-bit value formed by concatenating `a0' and `a1'.  The
//| divisor `b' must be at least 2^31.  If q is the exact quotient truncated
//| toward zero, the approximation returned lies between q and q + 2 inclusive.
//| If the exact quotient q is larger than 32 bits, the maximum positive 32-bit
//| unsigned integer is returned.
//*----------------------------------------------------------------------------*/
//
//static bits32 estimateDiv64To32( bits32 a0, bits32 a1, bits32 b )
//{
//	bits32 b0, b1;
//	bits32 rem0, rem1, term0, term1;
//	bits32 z;
//
//	if ( b <= a0 ) return 0xFFFFFFFF;
//	b0 = b>>16;
//	z = ( b0<<16 <= a0 ) ? 0xFFFF0000 : ( a0 / b0 )<<16;
//	mul32To64( b, z, &term0, &term1 );
//	sub64( a0, a1, term0, term1, &rem0, &rem1 );
//	while ( ( (sbits32) rem0 ) < 0 ) {
//		z -= 0x10000;
//		b1 = b<<16;
//		add64( rem0, rem1, b0, b1, &rem0, &rem1 );
//	}
//	rem0 = ( rem0<<16 ) | ( rem1>>16 );
//	z |= ( b0<<16 <= rem0 ) ? 0xFFFF : rem0 / b0;
//	return z;
//
//}
//
///*----------------------------------------------------------------------------
//| Returns an approximation to the square root of the 32-bit significand given
//| by `a'.  Considered as an integer, `a' must be at least 2^31.  If bit 0 of
//| `aExp' (the least significant bit) is 1, the integer returned approximates
//| 2^31*sqrt(`a'/2^31), where `a' is considered an integer.  If bit 0 of `aExp'
//| is 0, the integer returned approximates 2^31*sqrt(`a'/2^30).  In either
//| case, the approximation returned lies strictly within +/-2 of the exact
//| value.
//*----------------------------------------------------------------------------*/
//
//static bits32 estimateSqrt32( int16 aExp, bits32 a )
//{
//	static const bits16 sqrtOddAdjustments[] = {
//		0x0004, 0x0022, 0x005D, 0x00B1, 0x011D, 0x019F, 0x0236, 0x02E0,
//		0x039C, 0x0468, 0x0545, 0x0631, 0x072B, 0x0832, 0x0946, 0x0A67
//	};
//	static const bits16 sqrtEvenAdjustments[] = {
//		0x0A2D, 0x08AF, 0x075A, 0x0629, 0x051A, 0x0429, 0x0356, 0x029E,
//		0x0200, 0x0179, 0x0109, 0x00AF, 0x0068, 0x0034, 0x0012, 0x0002
//	};
//	int8 index;
//	bits32 z;
//
//	index = ( a>>27 ) & 15;
//	if ( aExp & 1 ) {
//		z = 0x4000 + ( a>>17 ) - sqrtOddAdjustments[ index ];
//		z = ( ( a / z )<<14 ) + ( z<<15 );
//		a >>= 1;
//	}
//	else {
//		z = 0x8000 + ( a>>17 ) - sqrtEvenAdjustments[ index ];
//		z = a / z + z;
//		z = ( 0x20000 <= z ) ? 0xFFFF8000 : ( z<<15 );
//		if ( z <= a ) return (bits32) ( ( (sbits32) a )>>1 );
//	}
//	return ( ( estimateDiv64To32( a, 0, z ) )>>1 ) + ( z>>1 );
//
//}
//
/*----------------------------------------------------------------------------
| Returns the number of leading 0 bits before the most-significant 1 bit of
| `a'.  If `a' is zero, 32 is returned.
*----------------------------------------------------------------------------*/

// static int8 countLeadingZeros32( bits32 a )
static int countLeadingZeros32( int a )
{
/*
	static const int8 countLeadingZerosHigh[] = {
		8, 7, 6, 6, 5, 5, 5, 5, 4, 4, 4, 4, 4, 4, 4, 4,
		3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
		2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
	};
	int8 shiftCount;

	shiftCount = 0;
	if ( a < 0x10000 ) {
		shiftCount += 16;
		a <<= 16;
	}
	if ( a < 0x1000000 ) {
		shiftCount += 8;
		a <<= 8;
	}
	shiftCount += countLeadingZerosHigh[ a>>24 ];
	return shiftCount;
*/
	int cnt;
	for (cnt=0; cnt<32; ++cnt) {
		if (a < 0) {		// MSB set
			break;
		}
		a <<= 1;
	}

	return cnt;
}

///*----------------------------------------------------------------------------
//| Returns 1 if the 64-bit value formed by concatenating `a0' and `a1' is
//| equal to the 64-bit value formed by concatenating `b0' and `b1'.  Otherwise,
//| returns 0.
//*----------------------------------------------------------------------------*/
//
//INLINE flag eq64( bits32 a0, bits32 a1, bits32 b0, bits32 b1 )
//{
//
//	return ( a0 == b0 ) && ( a1 == b1 );
//
//}
//
///*----------------------------------------------------------------------------
//| Returns 1 if the 64-bit value formed by concatenating `a0' and `a1' is less
//| than or equal to the 64-bit value formed by concatenating `b0' and `b1'.
//| Otherwise, returns 0.
//*----------------------------------------------------------------------------*/
//
//INLINE flag le64( bits32 a0, bits32 a1, bits32 b0, bits32 b1 )
//{
//
//	return ( a0 < b0 ) || ( ( a0 == b0 ) && ( a1 <= b1 ) );
//
//}
//
///*----------------------------------------------------------------------------
//| Returns 1 if the 64-bit value formed by concatenating `a0' and `a1' is less
//| than the 64-bit value formed by concatenating `b0' and `b1'.  Otherwise,
//| returns 0.
//*----------------------------------------------------------------------------*/
//
//INLINE flag lt64( bits32 a0, bits32 a1, bits32 b0, bits32 b1 )
//{
//
//	return ( a0 < b0 ) || ( ( a0 == b0 ) && ( a1 < b1 ) );
//
//}
//
///*----------------------------------------------------------------------------
//| Returns 1 if the 64-bit value formed by concatenating `a0' and `a1' is not
//| equal to the 64-bit value formed by concatenating `b0' and `b1'.  Otherwise,
//| returns 0.
//*----------------------------------------------------------------------------*/
//
//INLINE flag ne64( bits32 a0, bits32 a1, bits32 b0, bits32 b1 )
//{
//
//	return ( a0 != b0 ) || ( a1 != b1 );
//
//}
// end #include "softfloat-macros"


/*----------------------------------------------------------------------------
| Functions and definitions to determine:  (1) whether tininess for underflow
| is detected before or after rounding by default, (2) what (if anything)
| happens when exceptions are raised, (3) how signaling NaNs are distinguished
| from quiet NaNs, (4) the default generated quiet NaNs, and (4) how NaNs
| are propagated from function inputs to output.  These details are target-
| specific.
*----------------------------------------------------------------------------*/
// #include "softfloat-specialize"

//**************** thats the include
//
///*----------------------------------------------------------------------------
//| Underflow tininess-detection mode, statically initialized to default value.
//| (The declaration in `softfloat.h' must match the `int8' type here.)
//*----------------------------------------------------------------------------*/
//int8 float_detect_tininess = float_tininess_before_rounding;
//
///*----------------------------------------------------------------------------
//| Raises the exceptions specified by `flags'.  Floating-point traps can be
//| defined here if desired.  It is currently not possible for such a trap
//| to substitute a result value.  If traps are not implemented, this routine
//| should be simply `float_exception_flags |= flags;'.
//*----------------------------------------------------------------------------*/
//
//void float_raise( int8 flags )
//{
//
//	float_exception_flags |= flags;
//
//}
//
///*----------------------------------------------------------------------------
//| Internal canonical NaN format.
//*----------------------------------------------------------------------------*/
//typedef struct {
//	flag sign;
//	bits32 high, low;
//} commonNaNT;
//
///*----------------------------------------------------------------------------
//| The pattern for a default generated single-precision NaN.
//*----------------------------------------------------------------------------*/
//enum {
//	float32_default_nan = 0x7FFFFFFF
//};
//
/*----------------------------------------------------------------------------
| Returns 1 if the single-precision floating-point value `a' is a NaN;
| otherwise returns 0.
*----------------------------------------------------------------------------*/

/* I don't need it
static boolean float32_is_nan(int a)
{

	return ( 0xFF000000 < (a<<1) );

}
*/
//
///*----------------------------------------------------------------------------
//| Returns 1 if the single-precision floating-point value `a' is a signaling
//| NaN; otherwise returns 0.
//*----------------------------------------------------------------------------*/
//
//flag float32_is_signaling_nan( float32 a )
//{
//
//	return ( ( ( a>>22 ) & 0x1FF ) == 0x1FE ) && ( a & 0x003FFFFF );
//
//}
//
///*----------------------------------------------------------------------------
//| Returns the result of converting the single-precision floating-point NaN
//| `a' to the canonical NaN format.  If `a' is a signaling NaN, the invalid
//| exception is raised.
//*----------------------------------------------------------------------------*/
//
//static commonNaNT float32ToCommonNaN( float32 a )
//{
//	commonNaNT z;
//
//	if ( float32_is_signaling_nan( a ) ) float_raise( float_flag_invalid );
//	z.sign = a>>31;
//	z.low = 0;
//	z.high = a<<9;
//	return z;
//
//}
//
///*----------------------------------------------------------------------------
//| Returns the result of converting the canonical NaN `a' to the single-
//| precision floating-point format.
//*----------------------------------------------------------------------------*/
//
//static float32 commonNaNToFloat32( commonNaNT a )
//{
//
//	return ( ( (bits32) a.sign )<<31 ) | 0x7FC00000 | ( a.high>>9 );
//
//}
//
/*----------------------------------------------------------------------------
| Takes two single-precision floating-point values `a' and `b', one of which
| is a NaN, and returns the appropriate NaN result.  If either `a' or `b' is a
| signaling NaN, the invalid exception is raised.
*----------------------------------------------------------------------------*/
// signaling removed

// could be simpler...

/*
static int propagateFloat32NaN( int a, int b )
{
	boolean bIsNaN;

return 0x7fc00000;
	// aIsNaN = float32_is_nan( a );
	bIsNaN = float32_is_nan( b );
	a |= 0x00400000;
	b |= 0x00400000;
	return bIsNaN ? b : a;

}
*/


//**************** end include

/*----------------------------------------------------------------------------
| Returns the fraction bits of the single-precision floating-point value `a'.
*----------------------------------------------------------------------------*/

/*
static int extractFloat32Frac(int a)
{

	return a & 0x007FFFFF;

}
*/

/*----------------------------------------------------------------------------
| Returns the exponent bits of the single-precision floating-point value `a'.
*----------------------------------------------------------------------------*/

/*
static int extractFloat32Exp(int a)
{

	return ( a>>>23 ) & 0xFF;

}
*/

/*----------------------------------------------------------------------------
| Returns the sign bit of the single-precision floating-point value `a'.
*----------------------------------------------------------------------------*/

/*
static int extractFloat32Sign(int a)
{

	return a>>>31;

}
*/
//
///*----------------------------------------------------------------------------
//| Normalizes the subnormal single-precision floating-point value represented
//| by the denormalized significand `aSig'.  The normalized exponent and
//| significand are stored at the locations pointed to by `zExpPtr' and
//| `zSigPtr', respectively.
//*----------------------------------------------------------------------------*/
//
//static void
// normalizeFloat32Subnormal( bits32 aSig, int16 *zExpPtr, bits32 *zSigPtr )
//{
//	int8 shiftCount;
//
//	shiftCount = countLeadingZeros32( aSig ) - 8;
//	*zSigPtr = aSig<<shiftCount;
//	*zExpPtr = 1 - shiftCount;
//
//}
//
/*----------------------------------------------------------------------------
| Packs the sign `zSign', exponent `zExp', and significand `zSig' into a
| single-precision floating-point value, returning the result.  After being
| shifted into the proper positions, the three fields are simply added
| together to form the result.  This means that any integer portion of `zSig'
| will be added into the exponent.  Since a properly normalized significand
| will have an integer portion equal to 1, the `zExp' input should be 1 less
| than the desired result exponent whenever `zSig' is a complete, normalized
| significand.
*----------------------------------------------------------------------------*/

//INLINE float32 packFloat32( flag zSign, int16 zExp, bits32 zSig )
/*
static int packFloat32( int zSign, int zExp, int zSig )
{

	return ( ( zSign )<<31 ) + ( ( zExp )<<23 ) + zSig;

}
*/

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

//static float32 roundAndPackFloat32( flag zSign, int16 zExp, bits32 zSig )
static int roundAndPackFloat32( int zSign, int zExp, int zSig )
{
	// int8 roundingMode;
	// flag roundNearestEven;
	// int8 roundIncrement, roundBits;
	// flag isTiny;
	int roundIncrement, roundBits;

	roundIncrement = 0x40;		// round to nearst in Java
	roundBits = zSig & 0x7F;
	// if ( 0xFD <= (bits16) zExp ) {
	if ( 0xFD <= (zExp & 0xffff) ) {
		if (	( 0xFD < zExp )
			 || (	( zExp == 0xFD )
				  // && ( (sbits32) ( zSig + roundIncrement ) < 0 ) )
				  && ( ( zSig + roundIncrement ) < 0 ) )
		   ) {
//			float_raise( float_flag_overflow | float_flag_inexact );
			// return packFloat32( zSign, 0xFF, 0 ) - ((roundIncrement == 0) ? 1 : 0);
			return (((zSign)<<31) + ((0xff)<<23)) - ((roundIncrement == 0) ? 1 : 0);
		}
		if ( zExp < 0 ) {
/*
			isTiny =
				   ( float_detect_tininess == float_tininess_before_rounding )
				|| ( zExp < -1 )
				|| ( zSig + roundIncrement < 0x80000000 );
*/
			zSig = shift32RightJamming( zSig, -zExp);
			zExp = 0;
			roundBits = zSig & 0x7F;
//			if ( isTiny && roundBits ) float_raise( float_flag_underflow );
		}
	}
//	if ( roundBits ) float_exception_flags |= float_flag_inexact;
	zSig = ( zSig + roundIncrement )>>>7;
	zSig &= ~ ( ( ( roundBits ^ 0x40 ) == 0 ) ? 1 : 0 );
	if ( zSig == 0 ) zExp = 0;
	// return packFloat32( zSign, zExp, zSig );
	return (((zSign)<<31) + ((zExp)<<23) + zSig);

}

/*----------------------------------------------------------------------------
| Takes an abstract floating-point value having sign `zSign', exponent `zExp',
| and significand `zSig', and returns the proper single-precision floating-
| point value corresponding to the abstract input.  This routine is just like
| `roundAndPackFloat32' except that `zSig' does not have to be normalized.
| Bit 31 of `zSig' must be zero, and `zExp' must be 1 less than the ``true''
| floating-point exponent.
*----------------------------------------------------------------------------*/

// static float32 normalizeRoundAndPackFloat32( flag zSign, int16 zExp, bits32 zSig )
static int normalizeRoundAndPackFloat32( int zSign, int zExp, int zSig )
{
	int shiftCount;

	// shiftCount = countLeadingZeros32( zSig ) - 1;
	int a = zSig;
	for (shiftCount=0; shiftCount<32; ++shiftCount) {
		if (a < 0) {		// MSB set
			break;
		}
		a <<= 1;
	}
	shiftCount -= 1;

	return roundAndPackFloat32( zSign, zExp - shiftCount, zSig<<shiftCount );

}


/*----------------------------------------------------------------------------
| Returns the result of converting the 32-bit two's complement integer `a' to
| the single-precision floating-point format.  The conversion is performed
| according to the IEC/IEEE Standard for Binary Floating-Point Arithmetic.
*----------------------------------------------------------------------------*/

// float32 int32_to_float32( int32 a )
public static int int32_to_float32( int a )
{
	// flag zSign;
	int zSign;

	if ( a == 0 ) return 0;
	// if ( a == (sbits32) 0x80000000 ) return packFloat32( 1, 0x9E, 0 );
	if (a == 0x80000000) return (( 1 )<<31) + ( ( 0x9E )<<23 );


	// zSign = (a < 0);
	zSign = a>>>31;
	return normalizeRoundAndPackFloat32( zSign, 0x9C, (zSign!=0) ? -a : a );

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

/*
	no rounding used in Java.

// int32 float32_to_int32( float32 a )
public static int float32_to_int32( int a )
{
	// flag aSign;
	// int16 aExp, shiftCount;
	// bits32 aSig, aSigExtra;
	// int32 z;
	// int8 roundingMode;
	int aSign;
	int aExp, shiftCount;
	int aSig, aSigExtra;
	int z;
	int roundingMode;

	// aSig = extractFloat32Frac( a );
	// aExp = extractFloat32Exp( a );
	// aSign = extractFloat32Sign( a );
	aSig = a & 0x007FFFFF;
	aExp = ( a>>>23 ) & 0xFF;
	aSign = a>>>31;
	shiftCount = aExp - 0x96;
	if ( 0 <= shiftCount ) {
		if ( 0x9E <= aExp ) {
			if ( a != 0xCF000000 ) {
//				float_raise( float_flag_invalid );
				// if ( ! aSign || ( ( aExp == 0xFF ) && aSig ) ) {
				if ( aSign==0 || ( ( aExp == 0xFF ) && aSig!=0 ) ) {
					return 0x7FFFFFFF;
				}
			}
			return 0x80000000;
		}
		z = ( aSig | 0x00800000 )<<shiftCount;
		if ( aSign!=0 ) z = - z;
	}
	else {
		if ( aExp < 0x7E ) {
			aSigExtra = aExp | aSig;
			z = 0;
		}
		else {
			aSig |= 0x00800000;
			aSigExtra = aSig<<( shiftCount & 31 );
			z = aSig>>>( -shiftCount );
		}
		// if ( aSigExtra ) float_exception_flags |= float_flag_inexact;
		// roundingMode = float_rounding_mode;
		// if ( roundingMode == float_round_nearest_even ) {
			if ( aSigExtra < 0 ) {
				++z;
				// if ( (bits32) ( aSigExtra<<1 ) == 0 ) z &= ~1;
				if ( ( aSigExtra<<1 ) == 0 ) z &= ~1;
			}
			if ( aSign!=0 ) z = - z;
//		}
//		else {
//			aSigExtra = ( aSigExtra != 0 );
//			if ( aSign ) {
//				z += ( roundingMode == float_round_down ) & aSigExtra;
//				z = - z;
//			}
//			else {
//				z += ( roundingMode == float_round_up ) & aSigExtra;
//			}
//		}
	}
	return z;

}
*/

/*----------------------------------------------------------------------------
| Returns the result of converting the single-precision floating-point value
| `a' to the 32-bit two's complement integer format.  The conversion is
| performed according to the IEC/IEEE Standard for Binary Floating-Point
| Arithmetic, except that the conversion is always rounded toward zero.
| If `a' is a NaN, the largest positive integer is returned.  Otherwise, if
| the conversion overflows, the largest integer with the same sign as `a' is
| returned.
*----------------------------------------------------------------------------*/

// int32 float32_to_int32_round_to_zero( float32 a )
public static int float32_to_int32_round_to_zero(int a)
{
	// flag aSign;
	// int16 aExp, shiftCount;
	// bits32 aSig;
	// int32 z;
	int aSign;
	int aExp, shiftCount;
	int aSig;
	int z;

	// aSig = extractFloat32Frac( a );
	// aExp = extractFloat32Exp( a );
	// aSign = extractFloat32Sign( a );
	aSig = a & 0x007FFFFF;
	aExp = ( a>>>23 ) & 0xFF;
	aSign = a>>>31;
	shiftCount = aExp - 0x9E;
	if ( 0 <= shiftCount ) {
		if ( a != 0xCF000000 ) {
			// float_raise( float_flag_invalid );
			// if ( ! aSign || ( ( aExp == 0xFF ) && aSig ) ) return 0x7FFFFFFF;
			if (((aExp == 0xFF) && aSig!=0)) { // NaN
				// NaN hase to return 0 in Java!
				// That is different form IEEE 754
				return 0;
			} else if (aSign==0) {				// +INF
				return 0x7FFFFFFF;
			}
		}
		return 0x80000000;
	}
	else if ( aExp <= 0x7E ) {
		// if ( aExp | aSig ) float_exception_flags |= float_flag_inexact;
		return 0;
	}
	aSig = ( aSig | 0x00800000 )<<8;
	z = aSig>>>( -shiftCount );
/*
	if ( (bits32) ( aSig<<( shiftCount & 31 ) ) ) {
		float_exception_flags |= float_flag_inexact;
	}
*/
	if (aSign!=0) z = - z;
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
//	flag aSign;
//	int16 aExp;
//	bits32 lastBitMask, roundBitsMask;
//	int8 roundingMode;
//	float32 z;
//
//	aExp = extractFloat32Exp( a );
//	if ( 0x96 <= aExp ) {
//		if ( ( aExp == 0xFF ) && extractFloat32Frac( a ) ) {
//			return propagateFloat32NaN( a, a );
//		}
//		return a;
//	}
//	if ( aExp <= 0x7E ) {
//		if ( (bits32) ( a<<1 ) == 0 ) return a;
//		float_exception_flags |= float_flag_inexact;
//		aSign = extractFloat32Sign( a );
//		switch ( float_rounding_mode ) {
//		 case float_round_nearest_even:
//			if ( ( aExp == 0x7E ) && extractFloat32Frac( a ) ) {
//				return packFloat32( aSign, 0x7F, 0 );
//			}
//			break;
//		 case float_round_down:
//			return aSign ? 0xBF800000 : 0;
//		 case float_round_up:
//			return aSign ? 0x80000000 : 0x3F800000;
//		}
//		return packFloat32( aSign, 0, 0 );
//	}
//	lastBitMask = 1;
//	lastBitMask <<= 0x96 - aExp;
//	roundBitsMask = lastBitMask - 1;
//	z = a;
//	roundingMode = float_rounding_mode;
//	if ( roundingMode == float_round_nearest_even ) {
//		z += lastBitMask>>1;
//		if ( ( z & roundBitsMask ) == 0 ) z &= ~ lastBitMask;
//	}
//	else if ( roundingMode != float_round_to_zero ) {
//		if ( extractFloat32Sign( z ) ^ ( roundingMode == float_round_up ) ) {
//			z += roundBitsMask;
//		}
//	}
//	z &= ~ roundBitsMask;
//	if ( z != a ) float_exception_flags |= float_flag_inexact;
//	return z;
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

// static float32 addFloat32Sigs( float32 a, float32 b, flag zSign )
static int addFloat32Sigs( int a, int b, int zSign )
{
	// int16 aExp, bExp, zExp;
	// bits32 aSig, bSig, zSig;
	// int16 expDiff;
	int aExp, bExp, zExp;
	int aSig, bSig, zSig;
	int expDiff;

	// aSig = extractFloat32Frac( a );
	// aExp = extractFloat32Exp( a );
	// bSig = extractFloat32Frac( b );
	// bExp = extractFloat32Exp( b );
	aSig = a & 0x007FFFFF;
	aExp = ( a>>>23 ) & 0xFF;
	bSig = b & 0x007FFFFF;
	bExp = ( b>>>23 ) & 0xFF;
	expDiff = aExp - bExp;
	aSig <<= 6;
	bSig <<= 6;
	if ( 0 < expDiff ) {
		if ( aExp == 0xFF ) {
			// if ( aSig!=0 ) return propagateFloat32NaN( a, b );
			if ( aSig!=0 ) return 0x7fc00000;
			return a;
		}
		if ( bExp == 0 ) {
			--expDiff;
		}
		else {
			bSig |= 0x20000000;
		}
		bSig = shift32RightJamming(bSig, expDiff);
		zExp = aExp;
	}
	else if ( expDiff < 0 ) {
		if ( bExp == 0xFF ) {
			// if ( bSig!=0 ) return propagateFloat32NaN( a, b );
			if ( bSig!=0 ) return 0x7fc00000;
			// return packFloat32( zSign, 0xFF, 0 );
			return (((zSign)<<31) + ((0xff)<<23));
		}
		if ( aExp == 0 ) {
			++expDiff;
		}
		else {
			aSig |= 0x20000000;
		}
		aSig = shift32RightJamming( aSig, -expDiff);
		zExp = bExp;
	}
	else {
		if ( aExp == 0xFF ) {
			// if ( (aSig | bSig)!=0 ) return propagateFloat32NaN( a, b );
			if ( (aSig | bSig)!=0 ) return 0x7fc00000;
			return a;
		}
		// if ( aExp == 0 ) return packFloat32( zSign, 0, ( aSig + bSig )>>>6 );
		if ( aExp == 0 ) return (((zSign)<<31) + ((aSig+bSig)>>>6));
		zSig = 0x40000000 + aSig + bSig;
		zExp = aExp;
		return roundAndPackFloat32( zSign, zExp, zSig );
	}
	aSig |= 0x20000000;
	zSig = ( aSig + bSig )<<1;
	--zExp;
	if ( zSig < 0 ) {
		zSig = aSig + bSig;
		++zExp;
	}
	return roundAndPackFloat32( zSign, zExp, zSig );

}

/*----------------------------------------------------------------------------
| Returns the result of subtracting the absolute values of the single-
| precision floating-point values `a' and `b'.  If `zSign' is 1, the
| difference is negated before being returned.  `zSign' is ignored if the
| result is a NaN.  The subtraction is performed according to the IEC/IEEE
| Standard for Binary Floating-Point Arithmetic.
*----------------------------------------------------------------------------*/

// static float32 subFloat32Sigs( float32 a, float32 b, flag zSign )
static int subFloat32Sigs( int a, int b, int zSign )
{
	// int16 aExp, bExp, zExp;
	// bits32 aSig, bSig, zSig;
	// int16 expDiff;
	int aExp, bExp, zExp;
	int aSig, bSig, zSig;
	int expDiff;

	// aSig = extractFloat32Frac( a );
	// aExp = extractFloat32Exp( a );
	// bSig = extractFloat32Frac( b );
	// bExp = extractFloat32Exp( b );
	aSig = a & 0x007FFFFF;
	aExp = ( a>>>23 ) & 0xFF;
	bSig = b & 0x007FFFFF;
	bExp = ( b>>>23 ) & 0xFF;
	expDiff = aExp - bExp;
	aSig <<= 7;
	bSig <<= 7;
	if ( 0 < expDiff ) {
		if ( aExp == 0xFF ) {
			// if ( aSig!=0 ) return propagateFloat32NaN( a, b );
			if ( aSig!=0 ) return 0x7fc00000;
			return a;
		}
		if ( bExp == 0 ) {
			--expDiff;
		}
		else {
			bSig |= 0x40000000;
		}
		bSig = shift32RightJamming(bSig, expDiff);
		aSig |= 0x40000000;
		zSig = aSig - bSig;
		zExp = aExp;
		--zExp;
		return normalizeRoundAndPackFloat32( zSign, zExp, zSig );
	} else if ( expDiff < 0 ) {
		if ( bExp == 0xFF ) {
			// if ( bSig!=0 ) return propagateFloat32NaN( a, b );
			if ( bSig!=0 ) return 0x7fc00000;
			// return packFloat32( zSign ^ 1, 0xFF, 0 );
			return (((zSign^1)<<31) + ((0xff)<<23));
		}
		if ( aExp == 0 ) {
			++expDiff;
		}
		else {
			aSig |= 0x40000000;
		}
		aSig = shift32RightJamming( aSig, -expDiff);
		bSig |= 0x40000000;
		zSig = bSig - aSig;
		zExp = bExp;
		zSign ^= 1;
		--zExp;
		return normalizeRoundAndPackFloat32( zSign, zExp, zSig );

	}
	if ( aExp == 0xFF ) {
		// if ( aSig!=0 || bSig!=0 ) return propagateFloat32NaN( a, b );
		if ( aSig!=0 || bSig!=0 ) return 0x7fc00000;
		// float_raise( float_flag_invalid );
		// return float32_default_nan;
		// return 0x7FFFFFFF;
return 0x7fc00000;
	}
	if ( aExp == 0 ) {
		aExp = 1;
		bExp = 1;
	}
	if ( bSig < aSig ) {
		zSig = aSig - bSig;
		zExp = aExp;
		--zExp;
		return normalizeRoundAndPackFloat32( zSign, zExp, zSig );
	}
	if ( aSig < bSig ) {
		zSig = bSig - aSig;
		zExp = bExp;
		zSign ^= 1;
		--zExp;
		return normalizeRoundAndPackFloat32( zSign, zExp, zSig );
	}
	// return packFloat32( 0, 0, 0 );
	return 0;

}

/*----------------------------------------------------------------------------
| Returns the result of adding the single-precision floating-point values `a'
| and `b'.  The operation is performed according to the IEC/IEEE Standard for
| Binary Floating-Point Arithmetic.
*----------------------------------------------------------------------------*/

public static int float32_add( int a, int b )
{
	int aSign, bSign;

	// aSign = extractFloat32Sign( a );
	// bSign = extractFloat32Sign( b );
	aSign = a>>>31;
	bSign = b>>>31;
	if ( aSign == bSign ) {
		return addFloat32Sigs( a, b, aSign );
	} else {
		return subFloat32Sigs( a, b, aSign );
	}

}

/*----------------------------------------------------------------------------
| Returns the result of subtracting the single-precision floating-point values
| `a' and `b'.  The operation is performed according to the IEC/IEEE Standard
| for Binary Floating-Point Arithmetic.
*----------------------------------------------------------------------------*/

public static int float32_sub( int a, int b )
{
	int aSign, bSign;

	// aSign = extractFloat32Sign( a );
	// bSign = extractFloat32Sign( b );
	aSign = a>>>31;
	bSign = b>>>31;
	if ( aSign == bSign ) {
		return subFloat32Sigs( a, b, aSign );
	}
	else {
		return addFloat32Sigs( a, b, aSign );
	}

}

///*----------------------------------------------------------------------------
//| Returns the result of multiplying the single-precision floating-point values
//| `a' and `b'.  The operation is performed according to the IEC/IEEE Standard
//| for Binary Floating-Point Arithmetic.
//*----------------------------------------------------------------------------*/
//
//float32 float32_mul( float32 a, float32 b )
//{
//	flag aSign, bSign, zSign;
//	int16 aExp, bExp, zExp;
//	bits32 aSig, bSig, zSig0, zSig1;
//
//	aSig = extractFloat32Frac( a );
//	aExp = extractFloat32Exp( a );
//	aSign = extractFloat32Sign( a );
//	bSig = extractFloat32Frac( b );
//	bExp = extractFloat32Exp( b );
//	bSign = extractFloat32Sign( b );
//	zSign = aSign ^ bSign;
//	if ( aExp == 0xFF ) {
//		if ( aSig || ( ( bExp == 0xFF ) && bSig ) ) {
//			return propagateFloat32NaN( a, b );
//		}
//		if ( ( bExp | bSig ) == 0 ) {
//			float_raise( float_flag_invalid );
//			return float32_default_nan;
//		}
//		return packFloat32( zSign, 0xFF, 0 );
//	}
//	if ( bExp == 0xFF ) {
//		if ( bSig ) return propagateFloat32NaN( a, b );
//		if ( ( aExp | aSig ) == 0 ) {
//			float_raise( float_flag_invalid );
//			return float32_default_nan;
//		}
//		return packFloat32( zSign, 0xFF, 0 );
//	}
//	if ( aExp == 0 ) {
//		if ( aSig == 0 ) return packFloat32( zSign, 0, 0 );
//		normalizeFloat32Subnormal( aSig, &aExp, &aSig );
//	}
//	if ( bExp == 0 ) {
//		if ( bSig == 0 ) return packFloat32( zSign, 0, 0 );
//		normalizeFloat32Subnormal( bSig, &bExp, &bSig );
//	}
//	zExp = aExp + bExp - 0x7F;
//	aSig = ( aSig | 0x00800000 )<<7;
//	bSig = ( bSig | 0x00800000 )<<8;
//	mul32To64( aSig, bSig, &zSig0, &zSig1 );
//	zSig0 |= ( zSig1 != 0 );
//	if ( 0 <= (sbits32) ( zSig0<<1 ) ) {
//		zSig0 <<= 1;
//		--zExp;
//	}
//	return roundAndPackFloat32( zSign, zExp, zSig0 );
//
//}
//
///*----------------------------------------------------------------------------
//| Returns the result of dividing the single-precision floating-point value `a'
//| by the corresponding value `b'.  The operation is performed according to the
//| IEC/IEEE Standard for Binary Floating-Point Arithmetic.
//*----------------------------------------------------------------------------*/
//
//float32 float32_div( float32 a, float32 b )
//{
//	flag aSign, bSign, zSign;
//	int16 aExp, bExp, zExp;
//	bits32 aSig, bSig, zSig, rem0, rem1, term0, term1;
//
//	aSig = extractFloat32Frac( a );
//	aExp = extractFloat32Exp( a );
//	aSign = extractFloat32Sign( a );
//	bSig = extractFloat32Frac( b );
//	bExp = extractFloat32Exp( b );
//	bSign = extractFloat32Sign( b );
//	zSign = aSign ^ bSign;
//	if ( aExp == 0xFF ) {
//		if ( aSig ) return propagateFloat32NaN( a, b );
//		if ( bExp == 0xFF ) {
//			if ( bSig ) return propagateFloat32NaN( a, b );
//			float_raise( float_flag_invalid );
//			return float32_default_nan;
//		}
//		return packFloat32( zSign, 0xFF, 0 );
//	}
//	if ( bExp == 0xFF ) {
//		if ( bSig ) return propagateFloat32NaN( a, b );
//		return packFloat32( zSign, 0, 0 );
//	}
//	if ( bExp == 0 ) {
//		if ( bSig == 0 ) {
//			if ( ( aExp | aSig ) == 0 ) {
//				float_raise( float_flag_invalid );
//				return float32_default_nan;
//			}
//			float_raise( float_flag_divbyzero );
//			return packFloat32( zSign, 0xFF, 0 );
//		}
//		normalizeFloat32Subnormal( bSig, &bExp, &bSig );
//	}
//	if ( aExp == 0 ) {
//		if ( aSig == 0 ) return packFloat32( zSign, 0, 0 );
//		normalizeFloat32Subnormal( aSig, &aExp, &aSig );
//	}
//	zExp = aExp - bExp + 0x7D;
//	aSig = ( aSig | 0x00800000 )<<7;
//	bSig = ( bSig | 0x00800000 )<<8;
//	if ( bSig <= ( aSig + aSig ) ) {
//		aSig >>= 1;
//		++zExp;
//	}
//	zSig = estimateDiv64To32( aSig, 0, bSig );
//	if ( ( zSig & 0x3F ) <= 2 ) {
//		mul32To64( bSig, zSig, &term0, &term1 );
//		sub64( aSig, 0, term0, term1, &rem0, &rem1 );
//		while ( (sbits32) rem0 < 0 ) {
//			--zSig;
//			add64( rem0, rem1, 0, bSig, &rem0, &rem1 );
//		}
//		zSig |= ( rem1 != 0 );
//	}
//	return roundAndPackFloat32( zSign, zExp, zSig );
//
//}
//
///*----------------------------------------------------------------------------
//| Returns the remainder of the single-precision floating-point value `a'
//| with respect to the corresponding value `b'.  The operation is performed
//| according to the IEC/IEEE Standard for Binary Floating-Point Arithmetic.
//*----------------------------------------------------------------------------*/
//
//float32 float32_rem( float32 a, float32 b )
//{
//	flag aSign, bSign, zSign;
//	int16 aExp, bExp, expDiff;
//	bits32 aSig, bSig, q, allZero, alternateASig;
//	sbits32 sigMean;
//
//	aSig = extractFloat32Frac( a );
//	aExp = extractFloat32Exp( a );
//	aSign = extractFloat32Sign( a );
//	bSig = extractFloat32Frac( b );
//	bExp = extractFloat32Exp( b );
//	bSign = extractFloat32Sign( b );
//	if ( aExp == 0xFF ) {
//		if ( aSig || ( ( bExp == 0xFF ) && bSig ) ) {
//			return propagateFloat32NaN( a, b );
//		}
//		float_raise( float_flag_invalid );
//		return float32_default_nan;
//	}
//	if ( bExp == 0xFF ) {
//		if ( bSig ) return propagateFloat32NaN( a, b );
//		return a;
//	}
//	if ( bExp == 0 ) {
//		if ( bSig == 0 ) {
//			float_raise( float_flag_invalid );
//			return float32_default_nan;
//		}
//		normalizeFloat32Subnormal( bSig, &bExp, &bSig );
//	}
//	if ( aExp == 0 ) {
//		if ( aSig == 0 ) return a;
//		normalizeFloat32Subnormal( aSig, &aExp, &aSig );
//	}
//	expDiff = aExp - bExp;
//	aSig = ( aSig | 0x00800000 )<<8;
//	bSig = ( bSig | 0x00800000 )<<8;
//	if ( expDiff < 0 ) {
//		if ( expDiff < -1 ) return a;
//		aSig >>= 1;
//	}
//	q = ( bSig <= aSig );
//	if ( q ) aSig -= bSig;
//	expDiff -= 32;
//	while ( 0 < expDiff ) {
//		q = estimateDiv64To32( aSig, 0, bSig );
//		q = ( 2 < q ) ? q - 2 : 0;
//		aSig = - ( ( bSig>>2 ) * q );
//		expDiff -= 30;
//	}
//	expDiff += 32;
//	if ( 0 < expDiff ) {
//		q = estimateDiv64To32( aSig, 0, bSig );
//		q = ( 2 < q ) ? q - 2 : 0;
//		q >>= 32 - expDiff;
//		bSig >>= 2;
//		aSig = ( ( aSig>>1 )<<( expDiff - 1 ) ) - bSig * q;
//	}
//	else {
//		aSig >>= 2;
//		bSig >>= 2;
//	}
//	do {
//		alternateASig = aSig;
//		++q;
//		aSig -= bSig;
//	} while ( 0 <= (sbits32) aSig );
//	sigMean = aSig + alternateASig;
//	if ( ( sigMean < 0 ) || ( ( sigMean == 0 ) && ( q & 1 ) ) ) {
//		aSig = alternateASig;
//	}
//	zSign = ( (sbits32) aSig < 0 );
//	if ( zSign ) aSig = - aSig;
//	return normalizeRoundAndPackFloat32( aSign ^ zSign, bExp, aSig );
//
//}
//
///*----------------------------------------------------------------------------
//| Returns the square root of the single-precision floating-point value `a'.
//| The operation is performed according to the IEC/IEEE Standard for Binary
//| Floating-Point Arithmetic.
//*----------------------------------------------------------------------------*/
//
//float32 float32_sqrt( float32 a )
//{
//	flag aSign;
//	int16 aExp, zExp;
//	bits32 aSig, zSig, rem0, rem1, term0, term1;
//
//	aSig = extractFloat32Frac( a );
//	aExp = extractFloat32Exp( a );
//	aSign = extractFloat32Sign( a );
//	if ( aExp == 0xFF ) {
//		if ( aSig ) return propagateFloat32NaN( a, 0 );
//		if ( ! aSign ) return a;
//		float_raise( float_flag_invalid );
//		return float32_default_nan;
//	}
//	if ( aSign ) {
//		if ( ( aExp | aSig ) == 0 ) return a;
//		float_raise( float_flag_invalid );
//		return float32_default_nan;
//	}
//	if ( aExp == 0 ) {
//		if ( aSig == 0 ) return 0;
//		normalizeFloat32Subnormal( aSig, &aExp, &aSig );
//	}
//	zExp = ( ( aExp - 0x7F )>>1 ) + 0x7E;
//	aSig = ( aSig | 0x00800000 )<<8;
//	zSig = estimateSqrt32( aExp, aSig ) + 2;
//	if ( ( zSig & 0x7F ) <= 5 ) {
//		if ( zSig < 2 ) {
//			zSig = 0x7FFFFFFF;
//			goto roundAndPack;
//		}
//		else {
//			aSig >>= aExp & 1;
//			mul32To64( zSig, zSig, &term0, &term1 );
//			sub64( aSig, 0, term0, term1, &rem0, &rem1 );
//			while ( (sbits32) rem0 < 0 ) {
//				--zSig;
//				shortShift64Left( 0, zSig, 1, &term0, &term1 );
//				term1 |= 1;
//				add64( rem0, rem1, term0, term1, &rem0, &rem1 );
//			}
//			zSig |= ( ( rem0 | rem1 ) != 0 );
//		}
//	}
//	shift32RightJamming( zSig, 1, &zSig );
// roundAndPack:
//	return roundAndPackFloat32( 0, zExp, zSig );
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

	// if (	( ( extractFloat32Exp( a ) == 0xFF ) && extractFloat32Frac( a ) )
	// 	 || ( ( extractFloat32Exp( b ) == 0xFF ) && extractFloat32Frac( b ) ) ) {
	if ((((a>>>23) == 0xFF ) && ((a & 0x007FFFFF)!=0)) ||
		(((b>>>23) == 0xFF ) && ((b & 0x007FFFFF)!=0))) {
		return 1;		// one is NaN
	}
	return float32_cmp(a, b);
}
public static int float32_cmpl(int a, int b) {

	// if (	( ( extractFloat32Exp( a ) == 0xFF ) && extractFloat32Frac( a ) )
	// 	 || ( ( extractFloat32Exp( b ) == 0xFF ) && extractFloat32Frac( b ) ) ) {
	if ((((a>>>23) == 0xFF ) && ((a & 0x007FFFFF)!=0)) ||
		(((b>>>23) == 0xFF ) && ((b & 0x007FFFFF)!=0))) {
		return -1;		// one is NaN
	}
	return float32_cmp(a, b);
}

// flag float32_eq( float32 a, float32 b )
static int float32_cmp(int a, int b)
{


	// test for equal
	if (a == b) return 0;
	if ((( a | b )<<1) == 0) return 0;		// positiv zero and negative zero are considered euqal

	// test for lt
	int aSign = a>>>31;
	int bSign = b>>>31;
	if ( aSign != bSign ) {
		// return aSign && ( (bits32) ( ( a | b )<<1 ) != 0 ); return 1 if a < b
		if (aSign!=0 && ((( a | b )<<1) != 0)) {
			return -1;
		} else {
			return 1;
		}
	}
	// return ( a != b ) && ( aSign ^ ( a < b ) ); return 1 if a < b
	if ((a != b) && ((aSign ^ ((a<b) ? 1 : 0))!=0)) {
		return -1;
	} else {
		return 1;
	}

//flag float32_lt( float32 a, float32 b )
//{
//	flag aSign, bSign;
//
//	aSign = extractFloat32Sign( a );
//	bSign = extractFloat32Sign( b );
//	if ( aSign != bSign ) return aSign && ( (bits32) ( ( a | b )<<1 ) != 0 );
//	return ( a != b ) && ( aSign ^ ( a < b ) );
//
}

///*----------------------------------------------------------------------------
//| Returns 1 if the single-precision floating-point value `a' is equal to
//| the corresponding value `b', and 0 otherwise.  The comparison is performed
//| according to the IEC/IEEE Standard for Binary Floating-Point Arithmetic.
//*----------------------------------------------------------------------------*/
//
//flag float32_eq( float32 a, float32 b )
//{
//
//	if (	( ( extractFloat32Exp( a ) == 0xFF ) && extractFloat32Frac( a ) )
//		 || ( ( extractFloat32Exp( b ) == 0xFF ) && extractFloat32Frac( b ) )
//	   ) {
//		if ( float32_is_signaling_nan( a ) || float32_is_signaling_nan( b ) ) {
//			float_raise( float_flag_invalid );
//		}
//		return 0;
//	}
//	return ( a == b ) || ( (bits32) ( ( a | b )<<1 ) == 0 );
//
//}
//
///*----------------------------------------------------------------------------
//| Returns 1 if the single-precision floating-point value `a' is less than
//| or equal to the corresponding value `b', and 0 otherwise.  The comparison
//| is performed according to the IEC/IEEE Standard for Binary Floating-Point
//| Arithmetic.
//*----------------------------------------------------------------------------*/
//
//flag float32_le( float32 a, float32 b )
//{
//	flag aSign, bSign;
//
//	if (	( ( extractFloat32Exp( a ) == 0xFF ) && extractFloat32Frac( a ) )
//		 || ( ( extractFloat32Exp( b ) == 0xFF ) && extractFloat32Frac( b ) )
//	   ) {
//		float_raise( float_flag_invalid );
//		return 0;
//	}
//	aSign = extractFloat32Sign( a );
//	bSign = extractFloat32Sign( b );
//	if ( aSign != bSign ) return aSign || ( (bits32) ( ( a | b )<<1 ) == 0 );
//	return ( a == b ) || ( aSign ^ ( a < b ) );
//
//}
//
///*----------------------------------------------------------------------------
//| Returns 1 if the single-precision floating-point value `a' is less than
//| the corresponding value `b', and 0 otherwise.  The comparison is performed
//| according to the IEC/IEEE Standard for Binary Floating-Point Arithmetic.
//*----------------------------------------------------------------------------*/
//
//flag float32_lt( float32 a, float32 b )
//{
//	flag aSign, bSign;
//
//	if (	( ( extractFloat32Exp( a ) == 0xFF ) && extractFloat32Frac( a ) )
//		 || ( ( extractFloat32Exp( b ) == 0xFF ) && extractFloat32Frac( b ) )
//	   ) {
//		float_raise( float_flag_invalid );
//		return 0;
//	}
//	aSign = extractFloat32Sign( a );
//	bSign = extractFloat32Sign( b );
//	if ( aSign != bSign ) return aSign && ( (bits32) ( ( a | b )<<1 ) != 0 );
//	return ( a != b ) && ( aSign ^ ( a < b ) );
//
//}
//
///*----------------------------------------------------------------------------
//| Returns 1 if the single-precision floating-point value `a' is equal to
//| the corresponding value `b', and 0 otherwise.  The invalid exception is
//| raised if either operand is a NaN.  Otherwise, the comparison is performed
//| according to the IEC/IEEE Standard for Binary Floating-Point Arithmetic.
//*----------------------------------------------------------------------------*/
//
//flag float32_eq_signaling( float32 a, float32 b )
//{
//
//	if (	( ( extractFloat32Exp( a ) == 0xFF ) && extractFloat32Frac( a ) )
//		 || ( ( extractFloat32Exp( b ) == 0xFF ) && extractFloat32Frac( b ) )
//	   ) {
//		float_raise( float_flag_invalid );
//		return 0;
//	}
//	return ( a == b ) || ( (bits32) ( ( a | b )<<1 ) == 0 );
//
//}
//
///*----------------------------------------------------------------------------
//| Returns 1 if the single-precision floating-point value `a' is less than or
//| equal to the corresponding value `b', and 0 otherwise.  Quiet NaNs do not
//| cause an exception.  Otherwise, the comparison is performed according to the
//| IEC/IEEE Standard for Binary Floating-Point Arithmetic.
//*----------------------------------------------------------------------------*/
//
//flag float32_le_quiet( float32 a, float32 b )
//{
//	flag aSign, bSign;
//	int16 aExp, bExp;
//
//	if (	( ( extractFloat32Exp( a ) == 0xFF ) && extractFloat32Frac( a ) )
//		 || ( ( extractFloat32Exp( b ) == 0xFF ) && extractFloat32Frac( b ) )
//	   ) {
//		if ( float32_is_signaling_nan( a ) || float32_is_signaling_nan( b ) ) {
//			float_raise( float_flag_invalid );
//		}
//		return 0;
//	}
//	aSign = extractFloat32Sign( a );
//	bSign = extractFloat32Sign( b );
//	if ( aSign != bSign ) return aSign || ( (bits32) ( ( a | b )<<1 ) == 0 );
//	return ( a == b ) || ( aSign ^ ( a < b ) );
//
//}
//
///*----------------------------------------------------------------------------
//| Returns 1 if the single-precision floating-point value `a' is less than
//| the corresponding value `b', and 0 otherwise.  Quiet NaNs do not cause an
//| exception.  Otherwise, the comparison is performed according to the IEC/IEEE
//| Standard for Binary Floating-Point Arithmetic.
//*----------------------------------------------------------------------------*/
//
//flag float32_lt_quiet( float32 a, float32 b )
//{
//	flag aSign, bSign;
//
//	if (	( ( extractFloat32Exp( a ) == 0xFF ) && extractFloat32Frac( a ) )
//		 || ( ( extractFloat32Exp( b ) == 0xFF ) && extractFloat32Frac( b ) )
//	   ) {
//		if ( float32_is_signaling_nan( a ) || float32_is_signaling_nan( b ) ) {
//			float_raise( float_flag_invalid );
//		}
//		return 0;
//	}
//	aSign = extractFloat32Sign( a );
//	bSign = extractFloat32Sign( b );
//	if ( aSign != bSign ) return aSign && ( (bits32) ( ( a | b )<<1 ) != 0 );
//	return ( a != b ) && ( aSign ^ ( a < b ) );
//
//}
//
}	// end class
