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
 * CharQueue.java
 * 
 * A queue of char elements.
 * 
 * @author Paulo Guedes
 *
 * 31/05/2007 - 18:35:28
 * 
 */
public class CharQueue
{
  private char[] buffer;
  int numberOfElements;
  int bufferSize;
  
  public CharQueue(int capacity)
  {
    if(capacity < 1)
    {
      capacity = 16;
    }
    bufferSize = capacity;
    setEmpty();
  }
  
  public void setEmpty()
  {
    numberOfElements = 0;
    buffer = new char[bufferSize];
  }
  
  /**
   * Add the given char to the end of the internal buffer.
   * 
   * If the queue is full, shift all other chars one position to the left,
   * toward the beginning. In this case the first char is overwritten and lost.
   * 
   * @param c
   */
  public void add(char data)
  {
    if(numberOfElements >= buffer.length)
    {
      numberOfElements = buffer.length;
      shiftBuffer();
    }
    else
    {
      numberOfElements++;
    }
    buffer[numberOfElements - 1] = data;
  }
  
  /**
   * Shift all elements inside the buffer one position to the left.
   *
   */
  private void shiftBuffer()
  {
    int size = buffer.length-1;
    int i;
    
    for (i = 0; i < size; ++i)
    {
      buffer[i] = buffer[i + 1];
    }
  }
  
  /**
   * Remove the next element from the queue and return it.
   * 
   * @return
   */
  public char getNext()
  {
    char next = 0;
    
    if(numberOfElements > 0)
    {
      next = buffer[0];
      shiftBuffer();
      numberOfElements--;
    }
    
    return next;
  }
  
  /**
   * Return the maximum capacity of this queue.
   * 
   * @return
   */
  public int capacity()
  {
    return buffer.length;
  }
  
  /**
   * Return the current number of elements inside this queue.
   * 
   * @return
   */
  public int numElements()
  {
    return numberOfElements;
  }
  
  /**
   * Get an array containing a copy of the elements which are
   * still held inside this queue.
   * 
   * Old elements (already removed by the next() call)
   * will not be available anymore. The returned array
   * is a copy and cannot be used to manipulate the queue.
   * 
   * The internal state of the queue remain the same: no element
   * is removed nor added by this call.
   * 
   * If the queue is empty, this method return an array with size 0.  
   * 
   * @return
   */
  public char[] getElements()
  {
    char[] data = new char[numberOfElements];
    System.arraycopy(buffer, 0, data, 0, numberOfElements);
    return data;
  }
  
  public boolean matchPrefix(char[] prefix)
  {
    boolean result = false;
    
    if(prefix.length <= numElements())
    {
      result = JopDebuggerUtil.arrayPrefixEquals(prefix, buffer);
    }
    
    return result;
  }

  /**
   * @return
   */
  public boolean isFull()
  {
    return (numElements() >= capacity()) ;
  }

  /**
   * @param length
   */
  public void discard(int length)
  {
    if(length >= numElements())
    {
      setEmpty();
    }
    else
    {
      for(int i = 0; i < length; i++)
      {
        shiftBuffer();
      }
      numberOfElements -= length;
    }
  }
  
  /**
   * check to see if there is any possibility that the prefix 
   * will be matched. This is used to allow bytes to be
   * flushed as soon as possible.
   * 
   * This method is different than match() since it looks
   * only at the already existing bytes. So, if the prefix is
   * "TEST" and the buffer has already "TE" it will return 
   * false (it's possible to match "TEST" with this content).
   * However, if the buffer contains "EST" it will return 
   * true (it's impossible to match the prefix with the 
   * current content).
   * 
   * @param token_chars 
   * @return
   */
  public boolean impossibleMatch(char[] token_chars)
  {
    int i;
    int size = numElements();
    boolean result = false;
    
    if(size > token_chars.length)
    {
      size = token_chars.length;
    }
    
    for(i = 0; i < size; i++)
    {
      if(buffer[i] != token_chars[i])
      {
        // one different byte found. Impossible match.
        result = true;
        break;
      }
    }
    
    return result;
  }
}
