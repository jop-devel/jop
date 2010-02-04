/*
 * Copyright 1996-2004 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package java.lang;

public class VMFloatingDecimal{
    boolean     isExceptional;
    boolean     isNegative;
    int         decExponent;
    char        digits[];
    int         nDigits;
    int         bigIntExp;
    int         bigIntNBits;
    boolean     mustSetRoundDir = false;
    boolean     fromHex = false;
    int         roundDir = 0; // set by doubleValue

    private     VMFloatingDecimal( boolean negSign, int decExponent, char []digits, int n,  boolean e )
    {
        isNegative = negSign;
        isExceptional = e;
        this.decExponent = decExponent;
        this.digits = digits;
        this.nDigits = n;
    }

    /*
     * Constants of the implementation
     * Most are IEEE-754 related.
     * (There are more really boring constants at the end.)
     */
    static final long   signMask = 0x8000000000000000L;
    static final long   expMask  = 0x7ff0000000000000L;
    static final long   fractMask= ~(signMask|expMask);
    static final int    expShift = 52;
    static final int    expBias  = 1023;
    static final long   fractHOB = ( 1L<<expShift ); // assumed High-Order bit
    static final long   expOne   = ((long)expBias)<<expShift; // exponent of 1.0
    static final int    maxSmallBinExp = 62;
    static final int    minSmallBinExp = -( 63 / 3 );
    static final int    maxDecimalDigits = 15;
    static final int    maxDecimalExponent = 308;
    static final int    minDecimalExponent = -324;
    static final int    bigDecimalExponent = 324; // i.e. abs(minDecimalExponent)

    static final long   highbyte = 0xff00000000000000L;
    static final long   highbit  = 0x8000000000000000L;
    static final long   lowbytes = ~highbyte;

    static final int    singleSignMask =    0x80000000;
    static final int    singleExpMask  =    0x7f800000;
    static final int    singleFractMask =   ~(singleSignMask|singleExpMask);
    static final int    singleExpShift  =   23;
    static final int    singleFractHOB  =   1<<singleExpShift;
    static final int    singleExpBias   =   127;
    static final int    singleMaxDecimalDigits = 7;
    static final int    singleMaxDecimalExponent = 38;
    static final int    singleMinDecimalExponent = -45;

    static final int    intDecimalDigits = 9;

    private static final char infinity[] = { 'I', 'n', 'f', 'i', 'n', 'i', 't', 'y' };
    private static final char notANumber[] = { 'N', 'a', 'N' };
    private static final char zero[] = { '0', '0', '0', '0', '0', '0', '0', '0' };

    /*
     * count number of bits from high-order 1 bit to low-order 1 bit,
     * inclusive.
     */
    private static int
    countBits( long v ){
        //
        // the strategy is to shift until we get a non-zero sign bit
        // then shift until we have no bits left, counting the difference.
        // we do byte shifting as a hack. Hope it helps.
        //
        if ( v == 0L ) return 0;

        while ( ( v & highbyte ) == 0L ){
            v <<= 8;
        }
        while ( v > 0L ) { // i.e. while ((v&highbit) == 0L )
            v <<= 1;
        }

        int n = 0;
        while (( v & lowbytes ) != 0L ){
            v <<= 8;
            n += 8;
        }
        while ( v != 0L ){
            v <<= 1;
            n += 1;
        }
        return n;
    }

    /*
     * Keep big powers of 5 handy for future reference.
     */
    private static FDBigInt b5p[];

    private static // synchronized
		FDBigInt
    big5pow( int p ){
//         assert p >= 0 : p; // negative power of 5
        if ( b5p == null ){
            b5p = new FDBigInt[ p+1 ];
        }else if (b5p.length <= p ){
            FDBigInt t[] = new FDBigInt[ p+1 ];
            System.arraycopy( b5p, 0, t, 0, b5p.length );
            b5p = t;
        }
        if ( b5p[p] != null )
            return b5p[p];
        else if ( p < small5pow.length )
            return b5p[p] = new FDBigInt( small5pow[p] );
        else if ( p < long5pow.length )
            return b5p[p] = new FDBigInt( long5pow[p] );
        else {
            // construct the value.
            // recursively.
            int q, r;
            // in order to compute 5^p,
            // compute its square root, 5^(p/2) and square.
            // or, let q = p / 2, r = p -q, then
            // 5^p = 5^(q+r) = 5^q * 5^r
            q = p >> 1;
            r = p - q;
            FDBigInt bigq =  b5p[q];
            if ( bigq == null )
                bigq = big5pow ( q );
            if ( r < small5pow.length ){
                return (b5p[p] = bigq.mult( small5pow[r] ) );
            }else{
                FDBigInt bigr = b5p[ r ];
                if ( bigr == null )
                    bigr = big5pow( r );
                return (b5p[p] = bigq.mult( bigr ) );
            }
        }
    }

    //
    // a common operation
    //
    private static FDBigInt
    multPow52( FDBigInt v, int p5, int p2 ){
        if ( p5 != 0 ){
            if ( p5 < small5pow.length ){
                v = v.mult( small5pow[p5] );
            } else {
                v = v.mult( big5pow( p5 ) );
            }
        }
        if ( p2 != 0 ){
            v.lshiftMe( p2 );
        }
        return v;
    }

    //
    // another common operation
    //
    private static FDBigInt
    constructPow52( int p5, int p2 ){
        FDBigInt v = new FDBigInt( big5pow( p5 ) );
        if ( p2 != 0 ){
            v.lshiftMe( p2 );
        }
        return v;
    }

    /*
     * Make a floating double into a FDBigInt.
     * This could also be structured as a FDBigInt
     * constructor, but we'd have to build a lot of knowledge
     * about floating-point representation into it, and we don't want to.
     *
     * AS A SIDE EFFECT, THIS METHOD WILL SET THE INSTANCE VARIABLES
     * bigIntExp and bigIntNBits
     *
     */
    private FDBigInt
    doubleToBigInt( double dval ){
        long lbits = Double.doubleToLongBits( dval ) & ~signMask;
        int binexp = (int)(lbits >>> expShift);
        lbits &= fractMask;
        if ( binexp > 0 ){
            lbits |= fractHOB;
        } else {
//             assert lbits != 0L : lbits; // doubleToBigInt(0.0)
            binexp +=1;
            while ( (lbits & fractHOB ) == 0L){
                lbits <<= 1;
                binexp -= 1;
            }
        }
        binexp -= expBias;
        int nbits = countBits( lbits );
        /*
         * We now know where the high-order 1 bit is,
         * and we know how many there are.
         */
        int lowOrderZeros = expShift+1-nbits;
        lbits >>>= lowOrderZeros;

        bigIntExp = binexp+1-nbits;
        bigIntNBits = nbits;
        return new FDBigInt( lbits );
    }

    /*
     * Compute a number that is the ULP of the given value,
     * for purposes of addition/subtraction. Generally easy.
     * More difficult if subtracting and the argument
     * is a normalized a power of 2, as the ULP changes at these points.
     */
    private static double
    ulp( double dval, boolean subtracting ){
        long lbits = Double.doubleToLongBits( dval ) & ~signMask;
        int binexp = (int)(lbits >>> expShift);
        double ulpval;
        if ( subtracting && ( binexp >= expShift ) && ((lbits&fractMask) == 0L) ){
            // for subtraction from normalized, powers of 2,
            // use next-smaller exponent
            binexp -= 1;
        }
        if ( binexp > expShift ){
            ulpval = Double.longBitsToDouble( ((long)(binexp-expShift))<<expShift );
        } else if ( binexp == 0 ){
            ulpval = Double.MIN_VALUE;
        } else {
            ulpval = Double.longBitsToDouble( 1L<<(binexp-1) );
        }
        if ( subtracting ) ulpval = - ulpval;

        return ulpval;
    }

    public static VMFloatingDecimal
		readJavaFormatString( String in ) throws NumberFormatException {
        boolean isNegative = false;
        boolean signSeen   = false;
        int     decExp;
        char    c;

		parseNumber:
        try{
            in = in.trim(); // don't fool around with white space.
                            // throws NullPointerException if null
            int l = in.length();
            if ( l == 0 ) throw new NumberFormatException("empty String");
            int i = 0;
            switch ( c = in.charAt( i ) ){
            case '-':
                isNegative = true;
                //FALLTHROUGH
            case '+':
                i++;
                signSeen = true;
            }

            // Check for NaN and Infinity strings
            c = in.charAt(i);
            if(c == 'N' || c == 'I') { // possible NaN or infinity
                boolean potentialNaN = false;
                char targetChars[] = null;  // char array of "NaN" or "Infinity"

                if(c == 'N') {
                    targetChars = notANumber;
                    potentialNaN = true;
                } else {
                    targetChars = infinity;
                }

                // compare Input string to "NaN" or "Infinity"
                int j = 0;
                while(i < l && j < targetChars.length) {
                    if(in.charAt(i) == targetChars[j]) {
                        i++; j++;
                    }
                    else // something is amiss, throw exception
                        break parseNumber;
                }

                // For the candidate string to be a NaN or infinity,
                // all characters in input string and target char[]
                // must be matched ==> j must equal targetChars.length
                // and i must equal l
                if( (j == targetChars.length) && (i == l) ) { // return NaN or infinity
                    return (potentialNaN ?
							new VMFloatingDecimal(false, 0, notANumber, notANumber.length, true) // NaN has no sign
                            : new VMFloatingDecimal(isNegative, 0, infinity, notANumber.length, true));
                }
                else { // something went wrong, throw exception
                    break parseNumber;
                }

            } else if (c == '0')  { // check for hexadecimal floating-point number
                if (l > i+1 ) {
                    char ch = in.charAt(i+1);
                    if (ch == 'x' || ch == 'X' ) // possible hex string
                        //return parseHexString(in);
						throw new NumberFormatException("Cannot convert hexadecimal to floating point");
                }
            }  // look for and process decimal floating-point string

            char[] digits = new char[ l ];
            int    nDigits= 0;
            boolean decSeen = false;
            int decPt = 0;
            int nLeadZero = 0;
            int nTrailZero= 0;
			digitLoop:
            while ( i < l ){
                switch ( c = in.charAt( i ) ){
                case '0':
                    if ( nDigits > 0 ){
                        nTrailZero += 1;
                    } else {
                        nLeadZero += 1;
                    }
                    break; // out of switch.
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    while ( nTrailZero > 0 ){
                        digits[nDigits++] = '0';
                        nTrailZero -= 1;
                    }
                    digits[nDigits++] = c;
                    break; // out of switch.
                case '.':
                    if ( decSeen ){
                        // already saw one ., this is the 2nd.
                        throw new NumberFormatException("multiple points");
                    }
                    decPt = i;
                    if ( signSeen ){
                        decPt -= 1;
                    }
                    decSeen = true;
                    break; // out of switch.
                default:
                    break digitLoop;
                }
                i++;
            }
            /*
             * At this point, we've scanned all the digits and decimal
             * point we're going to see. Trim off leading and trailing
             * zeros, which will just confuse us later, and adjust
             * our initial decimal exponent accordingly.
             * To review:
             * we have seen i total characters.
             * nLeadZero of them were zeros before any other digits.
             * nTrailZero of them were zeros after any other digits.
             * if ( decSeen ), then a . was seen after decPt characters
             * ( including leading zeros which have been discarded )
             * nDigits characters were neither lead nor trailing
             * zeros, nor point
             */
            /*
             * special hack: if we saw no non-zero digits, then the
             * answer is zero!
             * Unfortunately, we feel honor-bound to keep parsing!
             */
            if ( nDigits == 0 ){
                digits = zero;
                nDigits = 1;
                if ( nLeadZero == 0 ){
                    // we saw NO DIGITS AT ALL,
                    // not even a crummy 0!
                    // this is not allowed.
                    break parseNumber; // go throw exception
                }

            }

            /* Our initial exponent is decPt, adjusted by the number of
             * discarded zeros. Or, if there was no decPt,
             * then its just nDigits adjusted by discarded trailing zeros.
             */
            if ( decSeen ){
                decExp = decPt - nLeadZero;
            } else {
                decExp = nDigits+nTrailZero;
            }

            /*
             * Look for 'e' or 'E' and an optionally signed integer.
             */
            if ( (i < l) &&  (((c = in.charAt(i) )=='e') || (c == 'E') ) ){
                int expSign = 1;
                int expVal  = 0;
                int reallyBig = Integer.MAX_VALUE / 10;
                boolean expOverflow = false;
                switch( in.charAt(++i) ){
                case '-':
                    expSign = -1;
                    //FALLTHROUGH
                case '+':
                    i++;
                }
                int expAt = i;
				expLoop:
                while ( i < l  ){
                    if ( expVal >= reallyBig ){
                        // the next character will cause integer
                        // overflow.
                        expOverflow = true;
                    }
                    switch ( c = in.charAt(i++) ){
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                        expVal = expVal*10 + ( (int)c - (int)'0' );
                        continue;
                    default:
                        i--;           // back up.
                        break expLoop; // stop parsing exponent.
                    }
                }
                int expLimit = bigDecimalExponent+nDigits+nTrailZero;
                if ( expOverflow || ( expVal > expLimit ) ){
                    //
                    // The intent here is to end up with
                    // infinity or zero, as appropriate.
                    // The reason for yielding such a small decExponent,
                    // rather than something intuitive such as
                    // expSign*Integer.MAX_VALUE, is that this value
                    // is subject to further manipulation in
                    // doubleValue() and floatValue(), and I don't want
                    // it to be able to cause overflow there!
                    // (The only way we can get into trouble here is for
                    // really outrageous nDigits+nTrailZero, such as 2 billion. )
                    //
                    decExp = expSign*expLimit;
                } else {
                    // this should not overflow, since we tested
                    // for expVal > (MAX+N), where N >= abs(decExp)
                    decExp = decExp + expSign*expVal;
                }

                // if we saw something not a digit ( or end of string )
                // after the [Ee][+-], without seeing any digits at all
                // this is certainly an error. If we saw some digits,
                // but then some trailing garbage, that might be ok.
                // so we just fall through in that case.
                // HUMBUG
                if ( i == expAt )
                    break parseNumber; // certainly bad
            }
            /*
             * We parsed everything we could.
             * If there are leftovers, then this is not good input!
             */
            if ( i < l &&
				 ((i != l - 1) ||
				  (in.charAt(i) != 'f' &&
				   in.charAt(i) != 'F' &&
				   in.charAt(i) != 'd' &&
				   in.charAt(i) != 'D'))) {
                break parseNumber; // go throw exception
            }

            return new VMFloatingDecimal( isNegative, decExp, digits, nDigits,  false );

        }
		catch ( StringIndexOutOfBoundsException e ) {
		}
        
		throw new NumberFormatException("For input string: \"" + in + "\"");
    }

    /*
     * Take a VMFloatingDecimal, which we presumably just scanned in,
     * and find out what its value is, as a float.
     * This is distinct from doubleValue() to avoid the extremely
     * unlikely case of a double rounding error, wherein the conversion
     * to double has one rounding error, and the conversion of that double
     * to a float has another rounding error, IN THE WRONG DIRECTION,
     * ( because of the preference to a zero low-order bit ).
     */

    public double
		doubleValue(){
        int     kDigits = Math.min( nDigits, maxDecimalDigits+1 );
        long    lValue;
        double  dValue;
        double  rValue, tValue;

        // First, check for NaN and Infinity values
        if(digits == infinity || digits == notANumber) {
            if(digits == notANumber)
                return Double.NaN;
            else
                return (isNegative?Double.NEGATIVE_INFINITY:Double.POSITIVE_INFINITY);
        }
        else {
            if (mustSetRoundDir) {
                roundDir = 0;
            }
            /*
             * convert the lead kDigits to a long integer.
             */
            // (special performance hack: start to do it using int)
            int iValue = (int)digits[0]-(int)'0';
            int iDigits = Math.min( kDigits, intDecimalDigits );
            for ( int i=1; i < iDigits; i++ ){
                iValue = iValue*10 + (int)digits[i]-(int)'0';
            }
            lValue = (long)iValue;
            for ( int i=iDigits; i < kDigits; i++ ){
                lValue = lValue*10L + (long)((int)digits[i]-(int)'0');
            }
            dValue = (double)lValue;
            int exp = decExponent-kDigits;
            /*
             * lValue now contains a long integer with the value of
             * the first kDigits digits of the number.
             * dValue contains the (double) of the same.
             */

            if ( nDigits <= maxDecimalDigits ){
                /*
                 * possibly an easy case.
                 * We know that the digits can be represented
                 * exactly. And if the exponent isn't too outrageous,
                 * the whole thing can be done with one operation,
                 * thus one rounding error.
                 * Note that all our constructors trim all leading and
                 * trailing zeros, so simple values (including zero)
                 * will always end up here
                 */
                if (exp == 0 || dValue == 0.0)
                    return (isNegative)? -dValue : dValue; // small floating integer
                else if ( exp >= 0 ){
                    if ( exp <= maxSmallTen ){
                        /*
                         * Can get the answer with one operation,
                         * thus one roundoff.
                         */
                        rValue = dValue * small10pow[exp];
                        if ( mustSetRoundDir ){
                            tValue = rValue / small10pow[exp];
                            roundDir = ( tValue ==  dValue ) ? 0
                                :( tValue < dValue ) ? 1
                                : -1;
                        }
                        return (isNegative)? -rValue : rValue;
                    }
                    int slop = maxDecimalDigits - kDigits;
                    if ( exp <= maxSmallTen+slop ){
                        /*
                         * We can multiply dValue by 10^(slop)
                         * and it is still "small" and exact.
                         * Then we can multiply by 10^(exp-slop)
                         * with one rounding.
                         */
                        dValue *= small10pow[slop];
                        rValue = dValue * small10pow[exp-slop];

                        if ( mustSetRoundDir ){
                            tValue = rValue / small10pow[exp-slop];
                            roundDir = ( tValue ==  dValue ) ? 0
                                :( tValue < dValue ) ? 1
                                : -1;
                        }
                        return (isNegative)? -rValue : rValue;
                    }
                    /*
                     * Else we have a hard case with a positive exp.
                     */
                } else {
                    if ( exp >= -maxSmallTen ){
                        /*
                         * Can get the answer in one division.
                         */
                        rValue = dValue / small10pow[-exp];
                        tValue = rValue * small10pow[-exp];
                        if ( mustSetRoundDir ){
                            roundDir = ( tValue ==  dValue ) ? 0
                                :( tValue < dValue ) ? 1
                                : -1;
                        }
                        return (isNegative)? -rValue : rValue;
                    }
                    /*
                     * Else we have a hard case with a negative exp.
                     */
                }
            }

            /*
             * Harder cases:
             * The sum of digits plus exponent is greater than
             * what we think we can do with one error.
             *
             * Start by approximating the right answer by,
             * naively, scaling by powers of 10.
             */
            if ( exp > 0 ){
                if ( decExponent > maxDecimalExponent+1 ){
                    /*
                     * Lets face it. This is going to be
                     * Infinity. Cut to the chase.
                     */
                    return (isNegative)? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
                }
                if ( (exp&15) != 0 ){
                    dValue *= small10pow[exp&15];
                }
                if ( (exp>>=4) != 0 ){
                    int j;
                    for( j = 0; exp > 1; j++, exp>>=1 ){
                        if ( (exp&1)!=0)
                            dValue *= big10pow[j];
                    }
                    /*
                     * The reason for the weird exp > 1 condition
                     * in the above loop was so that the last multiply
                     * would get unrolled. We handle it here.
                     * It could overflow.
                     */
                    double t = dValue * big10pow[j];
                    if ( Double.isInfinite( t ) ){
                        /*
                         * It did overflow.
                         * Look more closely at the result.
                         * If the exponent is just one too large,
                         * then use the maximum finite as our estimate
                         * value. Else call the result infinity
                         * and punt it.
                         * ( I presume this could happen because
                         * rounding forces the result here to be
                         * an ULP or two larger than
                         * Double.MAX_VALUE ).
                         */
                        t = dValue / 2.0;
                        t *= big10pow[j];
                        if ( Double.isInfinite( t ) ){
                            return (isNegative)? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
                        }
                        t = Double.MAX_VALUE;
                    }
                    dValue = t;
                }
            } else if ( exp < 0 ){
                exp = -exp;
                if ( decExponent < minDecimalExponent-1 ){
                    /*
                     * Lets face it. This is going to be
                     * zero. Cut to the chase.
                     */
                    return (isNegative)? -0.0 : 0.0;
                }
                if ( (exp&15) != 0 ){
                    dValue /= small10pow[exp&15];
                }
                if ( (exp>>=4) != 0 ){
                    int j;
                    for( j = 0; exp > 1; j++, exp>>=1 ){
                        if ( (exp&1)!=0)
                            dValue *= tiny10pow[j];
                    }
                    /*
                     * The reason for the weird exp > 1 condition
                     * in the above loop was so that the last multiply
                     * would get unrolled. We handle it here.
                     * It could underflow.
                     */
                    double t = dValue * tiny10pow[j];
                    if ( t == 0.0 ){
                        /*
                         * It did underflow.
                         * Look more closely at the result.
                         * If the exponent is just one too small,
                         * then use the minimum finite as our estimate
                         * value. Else call the result 0.0
                         * and punt it.
                         * ( I presume this could happen because
                         * rounding forces the result here to be
                         * an ULP or two less than
                         * Double.MIN_VALUE ).
                         */
                        t = dValue * 2.0;
                        t *= tiny10pow[j];
                        if ( t == 0.0 ){
                            return (isNegative)? -0.0 : 0.0;
                        }
                        t = Double.MIN_VALUE;
                    }
                    dValue = t;
                }
            }

            dValue = correctionLoop(kDigits, lValue, dValue);
            return (isNegative)? -dValue : dValue;
        }
    }

	private double correctionLoop(int kDigits, long lValue, double dValue) {
		int exp;
		/*
		 * dValue is now approximately the result.
		 * The hard part is adjusting it, by comparison
		 * with FDBigInt arithmetic.
		 * Formulate the EXACT big-number result as
		 * bigD0 * 10^exp
		 */
		FDBigInt bigD0 = new FDBigInt( lValue, digits, kDigits, nDigits );
		exp   = decExponent - nDigits;

		correctionLoop:
		while(true){
			/* AS A SIDE EFFECT, THIS METHOD WILL SET THE INSTANCE VARIABLES
			 * bigIntExp and bigIntNBits
			 */
			FDBigInt bigB = doubleToBigInt( dValue );

			/*
			 * Scale bigD, bigB appropriately for
			 * big-integer operations.
			 * Naively, we multiply by powers of ten
			 * and powers of two. What we actually do
			 * is keep track of the powers of 5 and
			 * powers of 2 we would use, then factor out
			 * common divisors before doing the work.
			 */
			int B2, B5; // powers of 2, 5 in bigB
			int     D2, D5; // powers of 2, 5 in bigD
			int Ulp2;   // powers of 2 in halfUlp.
			if ( exp >= 0 ){
				B2 = B5 = 0;
				D2 = D5 = exp;
			} else {
				B2 = B5 = -exp;
				D2 = D5 = 0;
			}
			if ( bigIntExp >= 0 ){
				B2 += bigIntExp;
			} else {
				D2 -= bigIntExp;
			}
			Ulp2 = B2;
			// shift bigB and bigD left by a number s. t.
			// halfUlp is still an integer.
			int hulpbias;
			if ( bigIntExp+bigIntNBits <= -expBias+1 ){
				// This is going to be a denormalized number
				// (if not actually zero).
				// half an ULP is at 2^-(expBias+expShift+1)
				hulpbias = bigIntExp+ expBias + expShift;
			} else {
				hulpbias = expShift + 2 - bigIntNBits;
			}
			B2 += hulpbias;
			D2 += hulpbias;
			// if there are common factors of 2, we might just as well
			// factor them out, as they add nothing useful.
			int common2 = Math.min( B2, Math.min( D2, Ulp2 ) );
			B2 -= common2;
			D2 -= common2;
			Ulp2 -= common2;
			// do multiplications by powers of 5 and 2
			bigB = multPow52( bigB, B5, B2 );
			FDBigInt bigD = multPow52( new FDBigInt( bigD0 ), D5, D2 );
			//
			// to recap:
			// bigB is the scaled-big-int version of our floating-point
			// candidate.
			// bigD is the scaled-big-int version of the exact value
			// as we understand it.
			// halfUlp is 1/2 an ulp of bigB, except for special cases
			// of exact powers of 2
			//
			// the plan is to compare bigB with bigD, and if the difference
			// is less than halfUlp, then we're satisfied. Otherwise,
			// use the ratio of difference to halfUlp to calculate a fudge
			// factor to add to the floating value, then go 'round again.
			//
			FDBigInt diff;
			int cmpResult;
			boolean overvalue;
			if ( (cmpResult = bigB.cmp( bigD ) ) > 0 ){
				overvalue = true; // our candidate is too big.
				diff = bigB.sub( bigD );
				if ( (bigIntNBits == 1) && (bigIntExp > -expBias) ){
					// candidate is a normalized exact power of 2 and
					// is too big. We will be subtracting.
					// For our purposes, ulp is the ulp of the
					// next smaller range.
					Ulp2 -= 1;
					if ( Ulp2 < 0 ){
						// rats. Cannot de-scale ulp this far.
						// must scale diff in other direction.
						Ulp2 = 0;
						diff.lshiftMe( 1 );
					}
				}
			} else if ( cmpResult < 0 ){
				overvalue = false; // our candidate is too small.
				diff = bigD.sub( bigB );
			} else {
				// the candidate is exactly right!
				// this happens with surprising frequency
				break correctionLoop;
			}
			FDBigInt halfUlp = constructPow52( B5, Ulp2 );
			if ( (cmpResult = diff.cmp( halfUlp ) ) < 0 ){
				// difference is small.
				// this is close enough
				if (mustSetRoundDir) {
					roundDir = overvalue ? -1 : 1;
				}
				break correctionLoop;
			} else if ( cmpResult == 0 ){
				// difference is exactly half an ULP
				// round to some other value maybe, then finish
				dValue += 0.5*ulp( dValue, overvalue );
				// should check for bigIntNBits == 1 here??
				if (mustSetRoundDir) {
					roundDir = overvalue ? -1 : 1;
				}
				break correctionLoop;
			} else {
				// difference is non-trivial.
				// could scale addend by ratio of difference to
				// halfUlp here, if we bothered to compute that difference.
				// Most of the time ( I hope ) it is about 1 anyway.
				dValue += ulp( dValue, overvalue );
				if ( dValue == 0.0 || dValue == Double.POSITIVE_INFINITY )
					break correctionLoop; // oops. Fell off end of range.
				continue; // try again.
			}
		}
		return dValue;
	}

    /*
     * All the positive powers of 10 that can be
     * represented exactly in double/float.
     */
    private static final double small10pow[] = {
        1.0e0,
        1.0e1, 1.0e2, 1.0e3, 1.0e4, 1.0e5,
        1.0e6, 1.0e7, 1.0e8, 1.0e9, 1.0e10,
        1.0e11, 1.0e12, 1.0e13, 1.0e14, 1.0e15,
        1.0e16, 1.0e17, 1.0e18, 1.0e19, 1.0e20,
        1.0e21, 1.0e22
    };

    private static final float singleSmall10pow[] = {
        1.0e0f,
        1.0e1f, 1.0e2f, 1.0e3f, 1.0e4f, 1.0e5f,
        1.0e6f, 1.0e7f, 1.0e8f, 1.0e9f, 1.0e10f
    };

    private static final double big10pow[] = {
        1e16, 1e32, 1e64, 1e128, 1e256 };
    private static final double tiny10pow[] = {
        1e-16, 1e-32, 1e-64, 1e-128, 1e-256 };

    private static final int maxSmallTen = small10pow.length-1;
    private static final int singleMaxSmallTen = singleSmall10pow.length-1;

    private static final int small5pow[] = {
        1,
        5,
        5*5,
        5*5*5,
        5*5*5*5,
        5*5*5*5*5,
        5*5*5*5*5*5,
        5*5*5*5*5*5*5,
        5*5*5*5*5*5*5*5,
        5*5*5*5*5*5*5*5*5,
        5*5*5*5*5*5*5*5*5*5,
        5*5*5*5*5*5*5*5*5*5*5,
        5*5*5*5*5*5*5*5*5*5*5*5,
        5*5*5*5*5*5*5*5*5*5*5*5*5
    };


    private static final long long5pow[] = {
        1L,
        5L,
        5L*5,
        5L*5*5,
        5L*5*5*5,
        5L*5*5*5*5,
        5L*5*5*5*5*5,
        5L*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5,
        5L*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5*5,
    };

    // approximately ceil( log2( long5pow[i] ) )
    private static final int n5bits[] = {
        0,
        3,
        5,
        7,
        10,
        12,
        14,
        17,
        19,
        21,
        24,
        26,
        28,
        31,
        33,
        35,
        38,
        40,
        42,
        45,
        47,
        49,
        52,
        54,
        56,
        59,
        61,
    };
}

/*
 * A really, really simple bigint package
 * tailored to the needs of floating base conversion.
 */
class FDBigInt {
    int nWords; // number of words used
    int data[]; // value: data[0] is least significant


    public FDBigInt( int v ){
        nWords = 1;
        data = new int[1];
        data[0] = v;
    }

    public FDBigInt( long v ){
        data = new int[2];
        data[0] = (int)v;
        data[1] = (int)(v>>>32);
        nWords = (data[1]==0) ? 1 : 2;
    }

    public FDBigInt( FDBigInt other ){
        data = new int[nWords = other.nWords];
        System.arraycopy( other.data, 0, data, 0, nWords );
    }

    private FDBigInt( int [] d, int n ){
        data = d;
        nWords = n;
    }

    public FDBigInt( long seed, char digit[], int nd0, int nd ){
        int n= (nd+8)/9;        // estimate size needed.
        if ( n < 2 ) n = 2;
        data = new int[n];      // allocate enough space
        data[0] = (int)seed;    // starting value
        data[1] = (int)(seed>>>32);
        nWords = (data[1]==0) ? 1 : 2;
        int i = nd0;
        int limit = nd-5;       // slurp digits 5 at a time.
        int v;
        while ( i < limit ){
            int ilim = i+5;
            v = (int)digit[i++]-(int)'0';
            while( i <ilim ){
                v = 10*v + (int)digit[i++]-(int)'0';
            }
            multaddMe( 100000, v); // ... where 100000 is 10^5.
        }
        int factor = 1;
        v = 0;
        while ( i < nd ){
            v = 10*v + (int)digit[i++]-(int)'0';
            factor *= 10;
        }
        if ( factor != 1 ){
            multaddMe( factor, v );
        }
    }

    /*
     * Left shift by c bits.
     * Shifts this in place.
     */
    public void
		lshiftMe( int c )throws IllegalArgumentException {
        if ( c <= 0 ){
            if ( c == 0 )
                return; // silly.
            else
                throw new IllegalArgumentException("negative shift count");
        }
        int wordcount = c>>5;
        int bitcount  = c & 0x1f;
        int anticount = 32-bitcount;
        int t[] = data;
        int s[] = data;
        if ( nWords+wordcount+1 > t.length ){
            // reallocate.
            t = new int[ nWords+wordcount+1 ];
        }
        int target = nWords+wordcount;
        int src    = nWords-1;
        if ( bitcount == 0 ){
            // special hack, since an anticount of 32 won't go!
            System.arraycopy( s, 0, t, wordcount, nWords );
            target = wordcount-1;
        } else {
            t[target--] = s[src]>>>anticount;
            while ( src >= 1 ){
                t[target--] = (s[src]<<bitcount) | (s[--src]>>>anticount);
            }
            t[target--] = s[src]<<bitcount;
        }
        while( target >= 0 ){
            t[target--] = 0;
        }
        data = t;
        nWords += wordcount + 1;
        // may have constructed high-order word of 0.
        // if so, trim it
        while ( nWords > 1 && data[nWords-1] == 0 )
            nWords--;
    }

    /*
     * normalize this number by shifting until
     * the MSB of the number is at 0x08000000.
     * This is in preparation for quoRemIteration, below.
     * The idea is that, to make division easier, we want the
     * divisor to be "normalized" -- usually this means shifting
     * the MSB into the high words sign bit. But because we know that
     * the quotient will be 0 < q < 10, we would like to arrange that
     * the dividend not span up into another word of precision.
     * (This needs to be explained more clearly!)
     */
    public int
		normalizeMe() throws IllegalArgumentException {
        int src;
        int wordcount = 0;
        int bitcount  = 0;
        int v = 0;
        for ( src= nWords-1 ; src >= 0 && (v=data[src]) == 0 ; src--){
            wordcount += 1;
        }
        if ( src < 0 ){
            // oops. Value is zero. Cannot normalize it!
            throw new IllegalArgumentException("zero value");
        }
        /*
         * In most cases, we assume that wordcount is zero. This only
         * makes sense, as we try not to maintain any high-order
         * words full of zeros. In fact, if there are zeros, we will
         * simply SHORTEN our number at this point. Watch closely...
         */
        nWords -= wordcount;
        /*
         * Compute how far left we have to shift v s.t. its highest-
         * order bit is in the right place. Then call lshiftMe to
         * do the work.
         */
        if ( (v & 0xf0000000) != 0 ){
            // will have to shift up into the next word.
            // too bad.
            for( bitcount = 32 ; (v & 0xf0000000) != 0 ; bitcount-- )
                v >>>= 1;
        } else {
            while ( v <= 0x000fffff ){
                // hack: byte-at-a-time shifting
                v <<= 8;
                bitcount += 8;
            }
            while ( v <= 0x07ffffff ){
                v <<= 1;
                bitcount += 1;
            }
        }
        if ( bitcount != 0 )
            lshiftMe( bitcount );
        return bitcount;
    }

    /*
     * Multiply a FDBigInt by an int.
     * Result is a new FDBigInt.
     */
    public FDBigInt
		mult( int iv ) {
        long v = iv;
        int r[];
        long p;

        // guess adequate size of r.
        r = new int[ ( v * ((long)data[nWords-1]&0xffffffffL) > 0xfffffffL ) ? nWords+1 : nWords ];
        p = 0L;
        for( int i=0; i < nWords; i++ ) {
            p += v * ((long)data[i]&0xffffffffL);
            r[i] = (int)p;
            p >>>= 32;
        }
        if ( p == 0L){
            return new FDBigInt( r, nWords );
        } else {
            r[nWords] = (int)p;
            return new FDBigInt( r, nWords+1 );
        }
    }

    /*
     * Multiply a FDBigInt by an int and add another int.
     * Result is computed in place.
     * Hope it fits!
     */
    public void
		multaddMe( int iv, int addend ) {
        long v = iv;
        long p;

        // unroll 0th iteration, doing addition.
        p = v * ((long)data[0]&0xffffffffL) + ((long)addend&0xffffffffL);
        data[0] = (int)p;
        p >>>= 32;
        for( int i=1; i < nWords; i++ ) {
            p += v * ((long)data[i]&0xffffffffL);
            data[i] = (int)p;
            p >>>= 32;
        }
        if ( p != 0L){
            data[nWords] = (int)p; // will fail noisily if illegal!
            nWords++;
        }
    }

    /*
     * Multiply a FDBigInt by another FDBigInt.
     * Result is a new FDBigInt.
     */
    public FDBigInt
		mult( FDBigInt other ){
        // crudely guess adequate size for r
        int r[] = new int[ nWords + other.nWords ];
        int i;
        // I think I am promised zeros...

        for( i = 0; i < this.nWords; i++ ){
            long v = (long)this.data[i] & 0xffffffffL; // UNSIGNED CONVERSION
            long p = 0L;
            int j;
            for( j = 0; j < other.nWords; j++ ){
                p += ((long)r[i+j]&0xffffffffL) + v*((long)other.data[j]&0xffffffffL); // UNSIGNED CONVERSIONS ALL 'ROUND.
                r[i+j] = (int)p;
                p >>>= 32;
            }
            r[i+j] = (int)p;
        }
        // compute how much of r we actually needed for all that.
        for ( i = r.length-1; i> 0; i--)
            if ( r[i] != 0 )
                break;
        return new FDBigInt( r, i+1 );
    }

    /*
     * Add one FDBigInt to another. Return a FDBigInt
     */
    public FDBigInt
		add( FDBigInt other ){
        int i;
        int a[], b[];
        int n, m;
        long c = 0L;
        // arrange such that a.nWords >= b.nWords;
        // n = a.nWords, m = b.nWords
        if ( this.nWords >= other.nWords ){
            a = this.data;
            n = this.nWords;
            b = other.data;
            m = other.nWords;
        } else {
            a = other.data;
            n = other.nWords;
            b = this.data;
            m = this.nWords;
        }
        int r[] = new int[ n ];
        for ( i = 0; i < n; i++ ){
            c += (long)a[i] & 0xffffffffL;
            if ( i < m ){
                c += (long)b[i] & 0xffffffffL;
            }
            r[i] = (int) c;
            c >>= 32; // signed shift.
        }
        if ( c != 0L ){
            // oops -- carry out -- need longer result.
            int s[] = new int[ r.length+1 ];
            System.arraycopy( r, 0, s, 0, r.length );
            s[i++] = (int)c;
            return new FDBigInt( s, i );
        }
        return new FDBigInt( r, i );
    }

    /*
     * Subtract one FDBigInt from another. Return a FDBigInt
     * Assert that the result is positive.
     */
    public FDBigInt
		sub( FDBigInt other ){
        int r[] = new int[ this.nWords ];
        int i;
        int n = this.nWords;
        int m = other.nWords;
        int nzeros = 0;
        long c = 0L;
        for ( i = 0; i < n; i++ ){
            c += (long)this.data[i] & 0xffffffffL;
            if ( i < m ){
                c -= (long)other.data[i] & 0xffffffffL;
            }
            if ( ( r[i] = (int) c ) == 0 )
                nzeros++;
            else
                nzeros = 0;
            c >>= 32; // signed shift
        }
//         assert c == 0L : c; // borrow out of subtract
//         assert dataInRangeIsZero(i, m, other); // negative result of subtract
        return new FDBigInt( r, n-nzeros );
    }

    private static boolean dataInRangeIsZero(int i, int m, FDBigInt other) {
        while ( i < m )
            if (other.data[i++] != 0)
                return false;
        return true;
    }

    /*
     * Compare FDBigInt with another FDBigInt. Return an integer
     * >0: this > other
     *  0: this == other
     * <0: this < other
     */
    public int
		cmp( FDBigInt other ){
        int i;
        if ( this.nWords > other.nWords ){
            // if any of my high-order words is non-zero,
            // then the answer is evident
            int j = other.nWords-1;
            for ( i = this.nWords-1; i > j ; i-- )
                if ( this.data[i] != 0 ) return 1;
        }else if ( this.nWords < other.nWords ){
            // if any of other's high-order words is non-zero,
            // then the answer is evident
            int j = this.nWords-1;
            for ( i = other.nWords-1; i > j ; i-- )
                if ( other.data[i] != 0 ) return -1;
        } else{
            i = this.nWords-1;
        }
        for ( ; i > 0 ; i-- )
            if ( this.data[i] != other.data[i] )
                break;
        // careful! want unsigned compare!
        // use brute force here.
        int a = this.data[i];
        int b = other.data[i];
        if ( a < 0 ){
            // a is really big, unsigned
            if ( b < 0 ){
                return a-b; // both big, negative
            } else {
                return 1; // b not big, answer is obvious;
            }
        } else {
            // a is not really big
            if ( b < 0 ) {
                // but b is really big
                return -1;
            } else {
                return a - b;
            }
        }
    }

    /*
     * Compute
     * q = (int)( this / S )
     * this = 10 * ( this mod S )
     * Return q.
     * This is the iteration step of digit development for output.
     * We assume that S has been normalized, as above, and that
     * "this" has been lshift'ed accordingly.
     * Also assume, of course, that the result, q, can be expressed
     * as an integer, 0 <= q < 10.
     */
    public int
		quoRemIteration( FDBigInt S )throws IllegalArgumentException {
        // ensure that this and S have the same number of
        // digits. If S is properly normalized and q < 10 then
        // this must be so.
        if ( nWords != S.nWords ){
            throw new IllegalArgumentException("disparate values");
        }
        // estimate q the obvious way. We will usually be
        // right. If not, then we're only off by a little and
        // will re-add.
        int n = nWords-1;
        long q = ((long)data[n]&0xffffffffL) / (long)S.data[n];
        long diff = 0L;
        for ( int i = 0; i <= n ; i++ ){
            diff += ((long)data[i]&0xffffffffL) -  q*((long)S.data[i]&0xffffffffL);
            data[i] = (int)diff;
            diff >>= 32; // N.B. SIGNED shift.
        }
        if ( diff != 0L ) {
            // damn, damn, damn. q is too big.
            // add S back in until this turns +. This should
            // not be very many times!
            long sum = 0L;
            while ( sum ==  0L ){
                sum = 0L;
                for ( int i = 0; i <= n; i++ ){
                    sum += ((long)data[i]&0xffffffffL) +  ((long)S.data[i]&0xffffffffL);
                    data[i] = (int) sum;
                    sum >>= 32; // Signed or unsigned, answer is 0 or 1
                }
                /*
                 * Originally the following line read
                 * "if ( sum !=0 && sum != -1 )"
                 * but that would be wrong, because of the
                 * treatment of the two values as entirely unsigned,
                 * it would be impossible for a carry-out to be interpreted
                 * as -1 -- it would have to be a single-bit carry-out, or
                 * +1.
                 */
//                 assert sum == 0 || sum == 1 : sum; // carry out of division correction
                q -= 1;
            }
        }
        // finally, we can multiply this by 10.
        // it cannot overflow, right, as the high-order word has
        // at least 4 high-order zeros!
        long p = 0L;
        for ( int i = 0; i <= n; i++ ){
            p += 10*((long)data[i]&0xffffffffL);
            data[i] = (int)p;
            p >>= 32; // SIGNED shift.
        }
//         assert p == 0L : p; // Carry out of *10
        return (int)q;
    }

    public long
		longValue(){
        // if this can be represented as a long, return the value
//         assert this.nWords > 0 : this.nWords; // longValue confused

        if (this.nWords == 1)
            return ((long)data[0]&0xffffffffL);

//         assert dataInRangeIsZero(2, this.nWords, this); // value too big
//         assert data[1] >= 0;  // value too big
        return ((long)(data[1]) << 32) | ((long)data[0]&0xffffffffL);
    }

    public String
		toString() {
        StringBuffer r = new StringBuffer(30);
        r.append('[');
        int i = Math.min( nWords-1, data.length-1) ;
        if ( nWords > data.length ){
            r.append( "("+data.length+"<"+nWords+"!)" );
        }
        for( ; i> 0 ; i-- ){
            r.append( Integer.toHexString( data[i] ) );
            r.append(' ');
        }
        r.append( Integer.toHexString( data[0] ) );
        r.append(']');
        return new String( r );
    }
}