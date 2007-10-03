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

package com.jopdesign.debug.jdwp.jop;

/**
 * JopDebuggerUtil.java
 *
 * Loading the JopDebuggerUtil class f
 * 
 * @author Paulo Guedes
 *
 * 31/05/2007 - 15:02:13
 * 
 */
public class JopDebuggerUtil
{
  /**
   * Check if the first array is a prefix of the second. The first should
   * be shorter than the second or have the same length.
   * 
   * @param first
   * @param second
   * @return
   */
  public static final boolean arrayPrefixEquals(char[] first, char[] second)
  {
    int index, sizeFirst, sizeSecond;
    boolean result = false;
    
    if(first == second)
    {
      result = true;
    }
    else
    {
      sizeFirst = first.length;
      sizeSecond = second.length;
      
      if(sizeFirst <= sizeSecond) 
      {
        // assume it's true and try to show otherwise
        result = true;
        for(index = 0; index < sizeFirst; index++)
        {
          if(first[index] != second[index])
          {
            result = false;
            break;
          }
        }
      }
    }
    
    return result;
  }

  public static final boolean arrayEquals(char[] first, char[] second)
  {
    boolean result;
    int sizeFirst, sizeSecond;
    
    sizeFirst = first.length;
    sizeSecond = second.length;
    
    if(first == second)
    {
      // same object: true
      result = true;
    }
    else
    {
      if(sizeFirst != sizeSecond)
      {
        // different lengths: no way they can be the same.
        result = false;
      }
      else
      {
        // may have the same content: check.
        result = JopDebuggerUtil.arrayPrefixEquals(first, second);
      }
    }
    
    return result;
  }

  public static final boolean arrayEquals(byte[] first, byte[] second)
  {
    boolean result = false;
    int sizeFirst, sizeSecond, index;
    
    sizeFirst = first.length;
    sizeSecond = second.length;
    
    if(sizeFirst == sizeSecond)
    {
      result = true;
      for(index = 0; index < sizeFirst; index++)
      {
        if(first[index] != second[index])
        {
          result = false;
          break;
        }
      }
    }
    
    return result;
  }
}
