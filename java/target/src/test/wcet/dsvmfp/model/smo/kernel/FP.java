package wcet.dsvmfp.model.smo.kernel;

/*

 FP version 3.1

 Copyright (c) 2004 Andre de Leiradella <leiradella@bigfoot.com>

 This program is licensed under the Artistic License.

 See http://www.opensource.org/licenses/artistic-license.html for details.

 Uses parts or ideas from FPMath. FPMath is copyright (c) 2001 Beartronics and
 is authored by Henry Minsky.
 http://bearlib.sourceforge.net/

 Uses parts or ideas from oMathFP. oMathFP is copyright (c) 2004 Dan Carter.
 http://orbisstudios.com/

 */

public class FP {
	
	private static Error overflow = new Error("FP.intToFp:Overflow");
//	public static int HALF;
//
//	public static int ONE;
//
//	public static int TWO;
//
//	public static int PI;
//
//	public static int E;
//
//	public static int MAX;
//
//	public static int MIN;
//
//	private static byte[] gP;
//
//	private static byte[] gA;

	private static StringBuffer gB;

	 public static final int HALF = 32768;

	 public static final int ONE = 65536;

	 public static final int TWO = 131072;

	 public static final int PI = 205887;

	 public static final int E = 178145;

	 public static final int MAX = 2147483647;

	 public static final int MIN = -2147483648;

	 private static byte[] gP = new byte[21];

	 private static byte[] gA = new byte[21];

   private static final int fpfact[] = { 1<<16,
      1<<16,
      2<<16,
      6<<16,
      24<<16,
      120<<16,
      720<<16,
      5040<<16,
      40320<<16
   };


	// private static StringBuffer gB = new StringBuffer(23);

	// Don't use
	private FP() {
	}

//	public static void init() {
//		HALF = 32768;
//		ONE = 65536;
//		TWO = 131072;
//		PI = 205887;
//		E = 178145;
//		MAX = 2147483647;
//		MIN = -2147483648;
//		gP = new byte[21];
//		gA = new byte[21];
//
//	}

	public static int fpToInt(int f) {
		return f >> 16;
	}

	public static int intToFp(int i) {
		if (i > FP.ONE) {
			// System.out.println("FP.intToFp:Overflow");
			throw overflow;
		}
		return i << 16;
	}

	public static int add(int f1, int f2) {
		return f1 + f2;
	}

	public static int sub(int f1, int f2) {
		return f1 - f2;
	}

	public static int mul(int f1, int f2) {
      return (f1>>8) * (f2>>8);
//        int res;
//        res = ((f1 >> 16) * (f2 >> 16)) << 16; //AH*BH
//        res += ((f1 >> 16) * (f2 & 0x0000FFFF)) ; //AH*BL
//        res += ((f1 & 0x0000FFFF) * (f2 >> 16)) ; //AL*BH
//        res += ((f1 & 0x0000FFFF) * (f2 & 0x0000FFFF)) >> 16; //AL*BL
//
//		return res;
	}

	public static int div(int f1, int f2) {
		f2= (1<<30)/((f2+1)>>2); //approx. 1/f2
		return FP.mul(f1,f2);
		//return ((int) (((((long) f1) << 32) / f2) >> 16));  //The bytecode for long shift was not implemented
	}

	public static int mod(int f1, int f2) {
		return f1 % f2;
	}

	public static int min(int f1, int f2) {
		return f1 < f2 ? f1 : f2;
	}

	public static int max(int f1, int f2) {
		return f1 > f2 ? f1 : f2;
	}

	public static int sqrt(int f) {
		int i, g;
		g = ((f + 65536) >> 1);
		i = 8;
		do
			g = ((int) ((g + (((((long) f) << 32) / g) >> 16)) >> 1));
		while (--i != 0);
		return g;
	}

	public static int round(int f) {
		return f < 0 ? -((-f + 32768) & ~0xFFFF) : (f + 32768) & ~0xFFFF;
	}

	public static int ceil(int f) {
		if ((f & 0xFFFF) == 0)
			return f;
		return (f & ~0xFFFF) + (f < 0 ? 0 : 65536);
	}

	public static int floor(int f) {
		if ((f & 0xFFFF) == 0)
			return f;
		return (f & ~0xFFFF) + (f < 0 ? 65536 : 0);
	}

	public static int trunc(int f) {
		return f < 0 ? -(-f & ~0xFFFF) : f & ~0xFFFF;
	}

	public static int frac(int f) {
		return (f < 0 ? -f : f) & 0xFFFF;
	}

	public static int sin(int f) {
		boolean neg;
		f = (f % 411774);
		if (neg = f < 0)
			f = -f;
		if (f < 102943) {
			;
		} else if (f < 205887) {
			f = (205887 - f);
		} else if (f < 308830) {
			f = (f - 205887);
			neg = !neg;
		} else {
			f = (411774 - f);
			neg = !neg;
		}
		int g = ((int) ((((long) f) * ((long) f)) >> 16));
		g = ((int) ((((long) f) * (65536 + ((((long) g) * (((((long) g) * ((long) 498)) >> 16) - 10881)) >> 16))) >> 16));
		return neg ? -g : g;
	}

	public static int cos(int f) {
		return sin(f + 102943);
	}

	public static int tan(int f) {
		int s, c;
		s = sin(f);
		c = cos(f);
		if (c != 0)
			return ((int) (((((long) s) << 32) / c) >> 16));
		return s < 0 ? -2147483648 : 2147483647;
	}

	public static int asin(int f) {
		boolean neg;
		if (neg = f < 0)
			f = -f;
		int g = ((int) (102943 - ((((long) sqrt((65536 - f))) * (((((long) f) * (((((long) f) * (((((long) f) * ((long) -1228)) >> 16) + 4866)) >> 16) - 13900)) >> 16) + 102939)) >> 16)));
		return neg ? -g : g;
	}

	public static int acos(int f) {
		return (102943 - asin(f));
	}

	public static int atan(int f) {
		return asin(((int) (((((long) f) << 32) / sqrt(((int) (65536 + ((((long) f) * ((long) f)) >> 16))))) >> 16)));
	}

	public static int atan2(int y, int x) {
		if (y == 0)
			return x < 0 ? 205887 : 0;
		if (x == 0)
			return y > 0 ? 102943 : -102943;
		int z = ((int) (((((long) y) << 32) / x) >> 16));
		z = atan(z < 0 ? -z : z);
		if (x > 0)
			return y > 0 ? z : -z;
		return y > 0 ? (205887 - z) : (z - 205887);
	}

  public static int exp (int x) {
    int result = 1<<16;
    int x2 = mul(x,x);
    int x3 = mul(x2,x);
    int x4 = mul(x2,x2);
    int x5 = mul(x4,x);
    int x6 = mul(x4,x2);
    int x7 = mul(x6,x);
    int x8 = mul(x4,x4);
    return result + x
        + div(x2,fpfact[2])
        + div(x3,fpfact[3])
        + div(x4,fpfact[4])
        + div(x5,fpfact[5])
        + div(x6,fpfact[6])
        + div(x7,fpfact[7])
        + div(x8,fpfact[8]);
      }
  /*
	public static int exp(int f) {
		if (f == 0)
			return 65536;
		int k = ((int) (((((long) (f < 0 ? -f : f)) * ((long) 94547)) >> 16) + 32768))
				& ~0xFFFF;
		if (f < 0)
			k = -k;
		f = ((int) (f - ((((long) k) * ((long) 45425)) >> 16)));
		int z = ((int) ((((long) f) * ((long) f)) >> 16));
		int r = ((int) (131072 + ((((long) z) * (((((long) z) * ((long) ((z >> 14) - 182))) >> 16) + 10921)) >> 16)));
		k = k < 0 ? 65536 >> (-k >> 16) : 65536 << (k >> 16);
		return ((int) ((((long) k) * (65536 + (((((long) (f << 1)) << 32) / (r - f)) >> 16))) >> 16));
	}
  */

	public static int ln(int f) {
		if (f < 0)
			return 0;
		if (f == 0)
			return -2147483648;
		int log2 = 0, g = f;
		while (g >= 131072) {
			g >>= 1;
			log2++;
		}
		g -= 65536;
		int s = ((int) (((((long) g) << 32) / (131072 + g)) >> 16));
		int z = ((int) ((((long) s) * ((long) s)) >> 16));
		int w = ((int) ((((long) z) * ((long) z)) >> 16));
		int r = ((int) (((((long) w) * (((((long) w) * (((((long) w) * ((long) 10036)) >> 16) + 14563)) >> 16) + 26214)) >> 16) + (((((((long) w) * (((((long) w) * (((((long) w) * ((long) 9697)) >> 16) + 11916)) >> 16) + 18724)) >> 16) + 43689) * ((long) z)) >> 16)));
		return ((int) (((45425 * log2) + g) - ((((long) s) * ((long) (g - r))) >> 16)));
	}

	public static int pow(int f, int n) {
		if (n == 0)
			return 65536;
		return exp(((int) ((((long) ln(f)) * ((long) n)) >> 16)));
	}

	public static int abs(int f) {
		if (f >= 0)
			return f;
		else
			return -f;
	}

	public static boolean epsEqual(int f1, int f2, int eps_fp) {
		int diff_fp = FP.sub(f1, f2);
		if (diff_fp > 0)
			return diff_fp < eps_fp;
		else
			return diff_fp > -eps_fp;
	}

	public static String fpToStr(int f) {
		gB = new StringBuffer(23);
		byte[] pow = gP;
		byte[] acc = gA;
		int digit, carry;
		boolean neg;
		StringBuffer sb = gB;
		digit = 0;
		do
			pow[digit] = acc[digit] = 0;
		while (++digit < 21);
		pow[9] = 1;
		pow[10] = 5;
		pow[11] = 2;
		pow[12] = 5;
		pow[13] = 8;
		pow[14] = 7;
		pow[15] = 8;
		pow[16] = 9;
		pow[17] = 0;
		pow[18] = 6;
		pow[19] = 2;
		pow[20] = 5;
		if (neg = f < 0)
			f = -f;
		while (f != 0) {
			if ((f & 1) != 0) {
				digit = 20;
				carry = 0;
				do {
					acc[digit] = (byte) (acc[digit] + pow[digit] + carry);
					if (acc[digit] > 9) {
						acc[digit] -= 10;
						carry = 1;
					} else
						carry = 0;
				} while (--digit >= 0);
			}
			digit = 20;
			carry = 0;
			do {
				pow[digit] = (byte) (pow[digit] + pow[digit] + carry);
				if (pow[digit] > 9) {
					pow[digit] -= 10;
					carry = 1;
				} else
					carry = 0;
			} while (--digit >= 0);
			f >>>= 1;
		}
		sb.setLength(0);
		if (neg)
			sb.append('-');
		for (f = 0; f < 21 && acc[f] == 0; f++)
			;
		if (f > 4)
			sb.append('0');
		for (digit = f; digit < 5; digit++)
			sb.append(acc[digit]);
		for (f = 20; f >= 0 && acc[f] == 0; f--)
			;
		if (f > 4)
			sb.append('.');
		for (digit = 5; digit <= f; digit++)
			sb.append(acc[digit]);
		return sb.toString();
	}

}