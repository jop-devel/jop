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
import java.io.OutputStream;

/**
 * This object has the ability to write the same data to two streams.
 * 
 * It is useful to allow the creation of a secondary output stream 
 * which can be used to feed another object with a copy of the same data.
 *  
 * This can be used for instance to log a stream such
 * as a network, file, keyboard, serial line or another 
 * kind of stream.
 * 
 * There is absolutely no assumption made about the objects besides
 * being valid, not null OutputStream objects.
 * 
 * Any output stream may be used. Even the same stream object 
 * may be used for both parameters to create an strange "echo" effect,
 * although there is absolutely no guarantee about the usefulness of
 * such usage. Who knows, maybe someone wants to duplicate audio 
 * samples in a raw audion file or just scramble a plain text.    
 * 
 * @author Paulo Abadie Guedes
 */
public class DuplicateOutputStream extends OutputStream
{
  /**
   * The first output stream to receive data.
   */
  private OutputStream outputOne;

  /**
   * The second output stream to receive data.
   */
  private OutputStream outputTwo;
  
  /**
   * Create a DuplicateOutputStream based on the provided objects.
   * 
   * @param outOne
   * @param outTwo
   */
  public DuplicateOutputStream(OutputStream outOne, OutputStream outTwo)
  {
    outputOne = outOne;
    outputTwo = outTwo;
  }
  
  /**
   * Write the int to both internal streams. 
   */
  public void write(int b) throws IOException
  {
    try
    {
      outputOne.write(b);
    }
    finally
    {
      outputTwo.write(b);
    }
  }
  
  /**
   * Write the array to both internal streams. 
   */
  public void write(byte b[]) throws IOException
  {
    try
    {
      outputOne.write(b);
    }
    finally
    {
      outputTwo.write(b);
    }
  }
  
  /**
   * Write the array section to both internal streams. 
   */
  public void write(byte b[], int off, int len) throws IOException
  {
    try
    {
      outputOne.write(b, off, len);
    }
    finally
    {
      outputTwo.write(b, off, len);
    }
  }
  
  /**
   * Flushes this output stream and forces any buffered output bytes 
   * to be written out to both internal output streams.
   */
  public void flush() throws IOException
  {
    try
    {
      outputOne.flush();
    }
    finally
    {
      outputTwo.flush();
    }
  }
  
  /**
   * Closes this output stream and releases any system resources 
   * associated with it.
   */
  public void close() throws IOException
  {
    try
    {
      outputOne.close();
    }
    finally
    {
      outputTwo.close();
    }
  }
}
