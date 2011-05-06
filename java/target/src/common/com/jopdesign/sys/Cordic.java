/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2010 Thomas B. Preusser <thomas.preusser@tu-dresden.de>
  
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

package com.jopdesign.sys;

public class Cordic {

  /**
   * Returns the trigonometric sine of an angle.  Special cases:
   * <ul><li>If the argument is NaN or an infinity, then the
   * result is NaN.
   * <li>If the argument is positive zero, then the result is
   * positive zero; if the argument is negative zero, then the
   * result is negative zero.</ul>
   * Be warned that this implementation only provides an accuracy
   * over 16 binary digits, i.e. 4 decimal digits. This is sufficient
   * for most casual computations but does not comply to the 
   * J2SE API specification.
   *
   * @param   a   an angle, in radians.
   * @return  the sine of the argument.
   * @since   CLDC 1.1
   */
  public static double sin(double a) {
    return  cordic(a, true);
  }

  /**
   * Returns the trigonometric cosine of an angle. Special case:
   * <ul><li>If the argument is NaN or an infinity, then the
   * result is NaN.</ul>
   * Be warned that this implementation only provides an accuracy
   * over 16 binary digits, i.e. 4 decimal digits. This is sufficient
   * for most casual computations but does not comply to the 
   * J2SE API specification.
   *
   * @param   a   an angle, in radians.
   * @return  the cosine of the argument.
   * @since   CLDC 1.1
   */
  public static double cos(double a) {
    return  cordic(a, false);
  }

  private final static int    CORDIC_N = 16;
  private final static int    CORDIC_K = 0x26DD3B6A;
  private final static int[]  CORDIC_BETA = {
    0x3243F6A9, 0x1DAC6705, 0x0FADBAFD, 0x07F56EA7,
    0x03FEAB77, 0x01FFD55C, 0x00FFFAAB, 0x007FFF55,
    0x003FFFEB, 0x001FFFFD, 0x00100000, 0x00080000,
    0x00040000, 0x00020000, 0x00010000, 0x00008000
  };

  /**
   * This is the internal implementation of both sin() and cos()
   * based on a CORDIC kernel using int arithmetic. This implementation
   * emphasizes efficiency over accuracy, which is limited to about 16
   * binary digits.
   *
   * @param phi  argument angle
   * @param sin  calculate the sine of phi if true, the cosine of phi otherwise
   * @return  the sine or cosine of the argument angle phi
   * @author  Thomas B. Preusser <thomas.preusser@tu-dresden.de>
   */
  private static double cordic(double  phi, final boolean  sin) {

    phi %= 2*Math.PI;
    if     (phi < -Math.PI)  phi += 2*Math.PI;
    else if(phi >  Math.PI)  phi -= 2*Math.PI;
    int  x; {
      int  y = 0;
      if     (phi < -Math.PI/2.0) { phi += Math.PI; x = -CORDIC_K; }
      else if(phi >  Math.PI/2.0) { phi -= Math.PI; x = -CORDIC_K; }
      else                        {                 x =  CORDIC_K; }
    
      int  pp; {
	final int  hi = (int)(Double.doubleToLongBits(phi) >> 52);
	final int  exp;
	if((exp = (hi&0x7FF)-1023) < -30)  return  sin? phi : 1.0;
	if(exp                     >   0)  return  Double.NaN;
	pp = (((int)(Double.doubleToLongBits(phi)>>22)&0x3FFFFFFF)|0x40000000)>>-exp;
	if(hi < 0)  pp = -pp;
      }

      for(int  i = 0; i < CORDIC_N; i++) {
		  // @WCA loop = CORDIC_N
	if(pp >= 0) {
	  final int  xx;
	  xx  = x - (y>>i);
	  y  += x>>i;
	  x   = xx;
	  pp -= CORDIC_BETA[i];
	}
	else {
	  final int  xx;
	  xx  = x + (y>>i);
	  y  -= x>>i;
	  x   = xx;
	  pp += CORDIC_BETA[i];
	}
      }
      if(sin)  x = y;
    }
    if(x == 0)  return  0.0;

    final boolean  neg;
    if(neg = (x < 0))  x = -x;

//  final int    shift = Integer.numberOfLeadingZeros(x);
    // The CLDC 1.1 compliant Integer does not contain the numberOfLeadingZeros method
    // It is therefore added here
    //*****************************
	int xCopy = x;
    x |= x >>> 1;
	x |= x >>> 2;
	x |= x >>> 4;
	x |= x >>> 8;
	x |= x >>> 16;
	x = ~x;
    x = ((x >> 1) & 0x55555555) + (x & 0x55555555);
    x = ((x >> 2) & 0x33333333) + (x & 0x33333333);
    x = ((x >> 4) & 0x0f0f0f0f) + (x & 0x0f0f0f0f);
    x = ((x >> 8) & 0x00ff00ff) + (x & 0x00ff00ff);
    final int shift = ((x >> 16) & 0x0000ffff) + (x & 0x0000ffff);
    x = xCopy;
    //*****************************
    final double res   = Double.longBitsToDouble(((((long)x)<<(shift+33))>>>12)|
						 (((long)(1024-shift))<<52));
    return  neg? -res : res;
  }

//  public static void main(String[] args) {
//    for(double  d = 0.0; d < 6.3; d += 0.05) {
//      final double  ts = Math.sin(d);
//      final double  tc = Math.cos(d);
//      final double  es = sin(d);
//      final double  ec = cos(d);
//      System.out.printf("Sin: %12.10f / %12.10f: %9.7f\n", es, ts, (es-ts)/ts);
//      System.out.printf("Cos: %12.10f / %12.10f: %9.7f\n", tc, tc, (ec-tc)/tc);
//    }
//  }

}