/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2009, Martin Schoeberl (martin@jopdesign.com)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/


/**
 * 
 */
package java.lang;

import com.jopdesign.sys.Native;
import com.jopdesign.sys.SoftFloat32;

/**
 * @author ???
 *
 */
public class VMFloat {

	public static String toString(float f) {
		return toString(Native.toInt(f));
	}
	
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
	
	
		/** A constant holding the value of 0.0d */
	public static final int ZERO = 0x00000000;

	/** A constant holding the value of -0.0f */
	public static final int NEGATIVE_ZERO = 0x80000000;

	
	/** A constant holding the value of 0.0d */
	public static final int ONE = 0x3f800000;

	
	/** A constant holding the value of 2.0d */
	public static final int TWO = 0x40000000;
	 // base 10 exponents for 2 ^ -150 through 2 ^ 98, at intervals of 2 ^ 8
	  private static final byte[] pow2x = { -45, -42, -40, -37, -35, -33, -30,
			-28, -25, -23, -21, -18, -16, -13, -11, -9, -6, -4, -1, 1, 4, 6, 8,
			11, 13, 16, 18, 20, 23, 25, 28, 30, };
	  
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
	  
	  /** @return true if d is negative */
	  static boolean unpackSign(int f) {
	    return (f < 0);
	  }

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
	        return SoftFloat32.pack(negative, base2x, base2m);
	      }
	  
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
      if (SoftFloat32.isNaN(f)) {
        return "NaN";
      }
      boolean n = unpackSign(f);
      StringBuffer sb = new StringBuffer(15);
      if (n) {
        sb.append('-');
      }
      if (SoftFloat32.isZero(f)) {
        sb.append("0.0");
        return sb.toString();
      } else if (SoftFloat32.isInfinite(f)) {
        sb.append("Infinity");
        return sb.toString();
      }
      // convert from base 2 to base 10
      int base2x = SoftFloat32.unpackExponent(f);
      int base2m = SoftFloat32.unpackMantissa(f);
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


    

    public static String ftoa(int f,int frac_len) {
    	int mantissa, int_part, frac_part;
    	int exp2;
    	String p=new String();

    	if (SoftFloat32.isZero(f)) {
    		return "0.00";
    	}
    	

    	exp2 = ((f >>> 23) - 127);
    	mantissa= (f & 0xFFFFFF) | 0x800000;
    	frac_part = 0;
    	int_part = 0;

    	if (exp2 >= 31) {
    		return "Too Large";
    	} else if (exp2 < -23) {
    		return "Too Small";
    	} else if (exp2 >= 23)
    		int_part = mantissa << (exp2 - 23);
    	else if (exp2 >= 0) {
    		int_part = mantissa >> (23 - exp2);
    		frac_part = (mantissa << (exp2 + 1)) & 0xFFFFFF;
    	} else
    		/* if (exp2 < 0) */
    		frac_part = (mantissa & 0xFFFFFF) >> -(exp2 + 1);

   
    	if (f < 0)
    		p = "-";

    	if (int_part == 0)
    		p = p+'0';
    	else {
    		p=p+Integer.toString(int_part);
    	}
    	p = p+'.';

    	if (frac_part == 0)
    		p = p+'0';
    	else {
    		

    		/* print BCD */
    		for (int m = 0; m < frac_len; m++) {
    			/* frac_part *= 10; */
    			frac_part = (frac_part << 3) + (frac_part << 1);

    			p = p+ (char)((frac_part >> 24) + 0x30);
    			frac_part &= 0x00FFFFFF;
    		}
    		//ToDo: delete ending zeros
    	}
  
    	return p;
    }
 
}
