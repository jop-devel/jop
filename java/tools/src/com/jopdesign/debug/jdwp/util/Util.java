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

package com.jopdesign.debug.jdwp.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;


/**
 * A small set of util methods for development.
 * 
 * @author Paulo Abadie Guedes
 */
public class Util
{
  public static final String NEW_LINE;
  
  // a simple way to get the newline character in a platform independent way
  static
  {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream stream = new PrintStream(out);
    stream.println();
    stream.flush();
    
    NEW_LINE = out.toString();
  }
  
  public static String printByteArray(byte[] data)
  {
    int i, count, size, lastSize;
    int estimatedSize;
    
    count = data.length;
    estimatedSize = 4 * count;
    StringBuffer buffer = new StringBuffer(estimatedSize);
    
    lastSize = 0;
    size = 0;
    for(i = 0; i < count; i++)
    {
      buffer.append(data[i]);
      if(count > (1 + i))
      {
        buffer.append(" ");
      }
      // break the text in lines 
      size = buffer.length() - lastSize;
      if(size > 70)
      {
        buffer.append(NEW_LINE);
        size = 0;
        lastSize = buffer.length();
      }
    }
    
    return buffer.toString();
  }
  
  public static byte[] copyByteArray(byte[] data)
  {
    byte[] copy; 
    copy = new byte[data.length];
    System.arraycopy(data, 0, copy, 0, data.length);
    return copy;
  }
  
  /**
   * Check if the length and content is the same for both arrays.
   * 
   * Return false otherwise: if any is null, if lenghts does not match
   * or if contents differ in any way.
   * 
   * @param firstArray
   * @param secondArray
   * @return
   */
  public static boolean contentEquals(byte[] firstArray, byte[] secondArray)
  {
    // assume they are different.
    boolean result = false;
    int index, size;
    byte byte1, byte2;
    
    if(firstArray != null && secondArray != null)
    {
      if(firstArray.length == secondArray.length)
      {
        size = firstArray.length;
        
        // ok, they seem to be the same until now. 
        // let's assume it's true and then try to prove otherwise.
        result = true;
        for(index = 0; index < size; index++)
        {
          byte1 = firstArray[index];
          byte2 = secondArray[index];
          if(byte1 != byte2)
          {
            // find it! they're different, stop.
            result = false;
            break;
          }
        }
      }
    }
    
    return result;
  }

  /**
   * @param input
   * @return
   * @throws IOException
   */
  public static byte[] readByteArray(InputStream input, int size) throws IOException
  {
    byte[] data = new byte[size];
    int num = 0;
    while (num < data.length)
    {
      num += input.read(data, num, data.length - num);
    }
    return data;
  }
  
  public static boolean contains(byte[] data, byte value)
  {
    boolean result = false;
    int index, size;
    
    if(data != null)
    {
      size = data.length;
      for(index = 0; index < size; index++)
      {
        if(data[index] == value)
        {
          result = true;
          break;
        }
      }
    }
    
    return result;
  }
}
