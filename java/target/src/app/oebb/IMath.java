package oebb;

/**
 * A faster replacement for (int)(java.lang.Math.sqrt(x)).  Completely accurate for x < 2147483648 (i.e. 2^31)...
 */

public class IMath {
	
	/**
	* Mark Borgerding's algorithm...
	* Not terribly speedy...
	*
	* no round => error 0.5
	*/
	
	/** breaks for val >= 32768*32768
		compare should be (guess*guess - val > 0) ???

	static int mborg_isqrt(int val) {

		int guess=0;
		int bit = 1 << 15;
		do {
			guess ^= bit;  
			// check to see if we can set this bit without going over sqrt(val)...
			if (guess * guess > val )
				guess ^= bit;  // it was too much, unset the bit...
		} while ((bit >>= 1) != 0);
 
		return guess;
	}
	*/ 
	
	/** breaks for val >= 32768*32768
	static int mborg_isqrt2(int val) {
	
		int g, g2, b, b2, gxb;
	
		g	= 0;	  			// guess			
		g2  = 0;	  			// guess^2		
		b	= 1<<15;			// bit		
		b2  = 1<<31;			// 2*bit^2
		gxb = 1<<30;			// bit*(2*guess+bit)
	
		do {
			if( g2+gxb <= val ) {	// (guess+bit)^2 <= val?
				 g	^= b;			// guess += bit		
				 g2  += gxb;		// (g+b)^2 = g^2+gxb
				 gxb += b2;			// b(2(g+b)+b) = b(2g+b)+b^2
			}
	
			b	>>>= 1;				// bit >>= 1
			b2  >>>= 2;				// 2(b/2)^2 = 2b^2/4
			gxb = (gxb-b2) >>> 1;	// b(2g+b/2)/2 = (b(2g+b)-2b^2/4)/2
		} while(b != 0);
	
		return g;
	}
	*/ 
		
	static int sqrt(int val) {

		int temp, g=0, b = 0x8000, bshft = 15;
		do {
			if (val >= (temp = (((g<<1)+b)<<bshft--))) {
				g += b;
				val -= temp;
			}
		} while ((b >>= 1) != 0);
		return g;
	}

/*
	public static void main(String[] args) {

		for (int i=0; i>=0; ++i) {

			if ((i&0xfffff)==0) {
				System.out.println(i);
			}

			// int j = mborg_isqrt(i);
			// int k = mborg_isqrt2(i);
			int l = sqrt(i);
			int m = (int) (java.lang.Math.sqrt(i));
			// if (j!=m || k!=m || l!=m) { 
			if (l!=m) { 
				// System.out.println(i+" "+j+" "+k+" "+l+" "+m);
				System.out.println(i+" "+l+" "+m);
				break;
			}
		}
	}
*/
}	 
