/*******************************************************************************

    An implementation of the Java Debug Wire Protocol (JDWP) for JOP
    Copyright (C) 2007 Paulo Abadie Guedes

    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
    
*******************************************************************************/

package com.jopdesign.debug.jdwp.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * HexadecimalInputStream.java
 * 
 * Class to convert from a sequence of hexadecimal characters
 * to a decimal value.
 * 
 * Valid input characters are those from '0' to '9', 'A' to 'F' and also
 * from 'a' to 'f'. Any character outside those three ranges will
 * cause an IOException.
 * 
 * @author Paulo Abadie Guedes
 *
 * 31/05/2007 - 23:52:35
 * 
 */
public class HexadecimalInputStream extends InputStream
{
  private InputStream inputStream;
  
  public HexadecimalInputStream (InputStream stream)
  {
    inputStream = stream;
  }
  
  public int available() throws IOException
  {
    int count = inputStream.available();
    return count/2;
  }
  
  /**
   * Read one byte from the stream, converting two hexadecimal
   * characters from the underlying stream into one decimal (byte) value
   * in the range 0 - 255 (0x00 - 0xFF).
   */
  public int read() throws IOException
  {
    int first, second;
    
    first = inputStream.read();
    second = inputStream.read();
    
    if((first == -1) || (second == -1))
    {
      // end of stream reached
      throw new IOException(" Stream closed.");
    }
    
    first = hexToDecimal(first);
    second = hexToDecimal(second);
    
    return (((first << 4) | second) & 0xff);
  }

  /**
   * 
   * @param value
   * @return
   * @throws IOException
   */
  public static int hexToDecimal(int value) throws IOException
  {
    switch(value)
    {
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
      {
        value = value - '0';
        break;
      }
      
      case 'A':
      case 'B':
      case 'C':
      case 'D':
      case 'E':
      case 'F':
      {
        value = value - 'A' + 10;
        break;
      }
      
      case 'a':
      case 'b':
      case 'c':
      case 'd':
      case 'e':
      case 'f':
      {
        value = value - 'a' + 10;
        break;
      }
      default:
      {
        // invalid number: this should not happen.
        throw new IOException(" Only hexadecimal characters supported." +
            " Invalid value: " + value + " Char:" + ((char) value));
      }
    }
    
    return value;
  }
}
