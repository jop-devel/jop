/* Integer.java -- object wrapper for int
   Copyright (C) 1998, 1999, 2001, 2002, 2004, 2005
   Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */


package java.lang;

/**
 * Instances of class <code>Integer</code> represent primitive
 * <code>int</code> values.
 *
 * Additionally, this class provides various helper functions and variables
 * related to ints.
 *
 * @author Paul Fisher
 * @author John Keiser
 * @author Warren Levy
 * @author Eric Blake (ebb9@email.byu.edu)
 * @author Tom Tromey (tromey@redhat.com)
 * @since 1.0
 * @status largely updated to 1.5
 */
public final class Integer 
{
 
  /**
   * Table for calculating digits, used in Character, Long, and Integer.
   */
  static final char[] digits = {
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
    'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
    'u', 'v', 'w', 'x', 'y', 'z',
  };
  /**
   * The minimum value an <code>int</code> can represent is -2147483648 (or
   * -2<sup>31</sup>).
   */
  public static final int MIN_VALUE = 0x80000000;

  /**
   * The maximum value an <code>int</code> can represent is 2147483647 (or
   * 2<sup>31</sup> - 1).
   */
  public static final int MAX_VALUE = 0x7fffffff;


  /**
   * The number of bits needed to represent an <code>int</code>.
   * @since 1.5
   */
  public static final int SIZE = 32;


  /**
   * The immutable value of this Integer.
   *
   * @serial the wrapped int
   */
  private final int value;

  /**
   * Create an <code>Integer</code> object representing the value of the
   * <code>int</code> argument.
   *
   * @param value the value to use
   */
  public Integer(int value)
  {
    this.value = value;
  }
  
  
  public byte byteValue()
  {
    return (byte) value;
  }
  
  
  
  public boolean equals(Object obj)
  {
	  //TODO: instance of not implemented
    //return obj instanceof Integer && value == ((Integer) obj).value;
  return value == ((Integer) obj).value;
  }
  
  
  public int hashCode()
  {
    return value;
  }
  
  public int intValue()
  {
    return value;
  }
  public long longValue()
  {
    return value;
  }
  
  
  public static int parseInt(String str, int radix)
  {
    return parseInt(str, radix, false);
  }


  public static int parseInt(String s)
  {
    return parseInt(s, 10, false);
  }
  
  public short shortValue()
  {
    return (short) value;
  }
  
  public static String toBinaryString(int i)
  {
    return toUnsignedString(i, 1);
  }
  /**
   * Helper for converting unsigned numbers to String.
   *
   * @param num the number
   * @param exp log2(digit) (ie. 1, 3, or 4 for binary, oct, hex)
   */
  
  private static String toUnsignedString(int num, int exp)
  {
    // Use an array large enough for a binary number.
    int mask = (1 << exp) - 1;
    char[] buffer = new char[32];
    int i = 32;
    do
      {
        buffer[--i] = digits[num & mask];
        num >>>= exp;
      }
    while (num != 0);

    // Package constructor avoids an array copy.
    return new String(buffer, i, 32 - i);
  }
  
  
  public static String toHexString(int i)
  {
    return toUnsignedString(i, 4);
  }
  public static String toOctalString(int i)
  {
    return toUnsignedString(i, 3);
  }

	public String toString()
  {
	    return String.valueOf(value);
	  }

  public static String toString(int i)
  {
    // This is tricky: in libgcj, String.valueOf(int) is a fast native
    // implementation.  In Classpath it just calls back to
    // Integer.toString(int, int).
    return String.valueOf(i);
  }

  

  public static String toString(int num, int radix)
  {
	 
    if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX)
      radix = 10;

    // For negative numbers, print out the absolute value w/ a leading '-'.
    // Use an array large enough for a binary number.
    char[] buffer = new char[33];
    int i = 33;
    boolean isNeg = false;
    if (num < 0)
      {
        isNeg = true;
        num = -num;

        // When the value is MIN_VALUE, it overflows when made positive
        if (num < 0)
	  {
	    buffer[--i] = digits[(int) (-(num + radix) % radix)];
	    num = -(num / radix);
	  }
      }

    do
      {
        buffer[--i] = digits[num % radix];
        num /= radix;
      }
    while (num > 0 && i > 0);

    if (isNeg)
      buffer[--i] = '-';
    

    // Package constructor avoids an array copy.
    return new String(buffer, i, 33 - i);
  }



  public static Integer valueOf(String s)
  {
    return new Integer(parseInt(s, 10, false));
  }
  
  public static Integer valueOf(int val)
  {
      return new Integer(val);
  }
  /**
   * Return the value of this <code>Integer</code> as a <code>float</code>.
   *
   * @return the float value
   */
  public float floatValue()
  {
    return value;
  }

  /**
   * Return the value of this <code>Integer</code> as a <code>double</code>.
   *
   * @return the double value
   */
  public double doubleValue()
  {
    return value;
  }


  private static int parseInt(String str, int radix, boolean decode)
  {
	  if(str.value.length != str.length())
		  throw new Error("lang.Integer.parseInt(): String count and length do not match");
	  for(int i = 0; i < str.value.length;i++){
		  if(!Character.isDigit(str.value[i]) && (str.value[i] != '-' || i > 0))
			  throw new Error("lang.Integer.parseInt(): String contains not only Digits");
	  }
    if (! decode && str == null)
      throw new NumberFormatException();
    int index = 0;
    int len = str.length();
    boolean isNeg = false;
    if (len == 0)
      throw new NumberFormatException("string length is null");
    int ch = str.charAt(index);
    if (ch == '-')
      {
        if (len == 1)
          throw new NumberFormatException("pure '-'");
        isNeg = true;
        ch = str.charAt(++index);
      }
    if (decode)
      {
        if (ch == '0')
          {
            if (++index == len)
              return 0;
            if ((str.charAt(index) & ~('x' ^ 'X')) == 'X')
              {
                radix = 16;
                index++;
              }
            else
              radix = 8;
          }
        else if (ch == '#')
          {
            radix = 16;
            index++;
          }
      }
    if (index == len)
      throw new NumberFormatException("non terminated number: " + str);

    int max = MAX_VALUE / radix;
    // We can't directly write `max = (MAX_VALUE + 1) / radix'.
    // So instead we fake it.
    if (isNeg && MAX_VALUE % radix == radix - 1)
      ++max;

    int val = 0;
    while (index < len)
      {
	if (val < 0 || val > max)
	  throw new NumberFormatException("number overflow (pos=" + index + ") : " + str);

        ch = Character.digit(str.charAt(index++), radix);
        val = val * radix + ch;
        if (ch < 0 || (val < 0 && (! isNeg || val != MIN_VALUE)))
          throw new NumberFormatException("invalid character at position " + index + " in " + str);
      }
    return isNeg ? -val : val;
  }
  
  /**
   * Return the number of leading zeros in value.
   * @param value the value to examine
   * @since 1.5
   */
  public static int numberOfLeadingZeros(int value)
  {
    value |= value >>> 1;
    value |= value >>> 2;
    value |= value >>> 4;
    value |= value >>> 8;
    value |= value >>> 16;
    return bitCount(~value);
  }
  
  /**
   * Return the number of bits set in x.
   * @param x value to examine
   * @since 1.5
   */
  public static int bitCount(int x)
  {
    // Successively collapse alternating bit groups into a sum.
    x = ((x >> 1) & 0x55555555) + (x & 0x55555555);
    x = ((x >> 2) & 0x33333333) + (x & 0x33333333);
    x = ((x >> 4) & 0x0f0f0f0f) + (x & 0x0f0f0f0f);
    x = ((x >> 8) & 0x00ff00ff) + (x & 0x00ff00ff);
    return ((x >> 16) & 0x0000ffff) + (x & 0x0000ffff);
  }


}


