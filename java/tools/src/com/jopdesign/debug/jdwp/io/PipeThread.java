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
import java.io.OutputStream;

/**
 * PipeThread.java
 * 
 * A pipe to connect two streams and pump data. 
 * 
 * This class is a Thread which can connect an InputStream
 * to an OutputStream. While it is running, it takes care
 * of data transfer between the streams.
 * 
 * Its behaviour is close to what happens when 
 * a shell pipe connects the output of one programs
 * to the input of the next one.
 * 
 * @author Paulo Abadie Guedes
 * 29/05/2007 - 16:55:42
 * 
 */
public class PipeThread
{
  private static final int BUFFER_SIZE = 4 * 1024;
  
  private InputStream input;
  private OutputStream output;
  
  public PipeThread(InputStream inputStream, OutputStream outputStream)
  {
    this.input = inputStream;
    this.output = outputStream;
  }
  
  /**
   * This method will pump bytes from the input stream to the output
   * stream over and over again, until there is nothing more to read.
   * 
   * Then it will close all streams.
   */  
  public void run()
  {
    InputStream in = getInputStream();
    OutputStream out = getOutputStream();
    
    byte data[] = new byte[BUFFER_SIZE];
    int num;
    int count = 0;
    try
    {
      while ((num = in.read(data)) != -1)
      {
        out.write(data, 0, num);
        
        count += num;
        
        // flush the stream every BUFFER_SIZE bytes. No specific need to
        // be BUFFER_SIZE or any other value, but it's better doing this
        // by number of bytes than using a timeout.
        // avoid too small values, just to prevent too much flush calls.
        if(count > BUFFER_SIZE)
        {
          System.out.println(" Bytes: " + count);
          out.flush();
          count = 0;
        }
      }
    }
    catch (IOException e)
    {
      System.out.println("  Failure: " + e.getMessage());
      e.printStackTrace();
    }
    finally
    {
      try
      {
        close();
      }
      catch (IOException e)
      {
        System.out.println("  Failure: " + e.getMessage());
        e.printStackTrace();
      }
    }
  }

  /**
   * @throws IOException 
   * 
   */
  private void close() throws IOException
  {
    // try to close both streams, no matter if the first throws
    // an exception. If there is any error let it be handled elsewhere.
    try
    {
      input.close();
    }
    finally
    {
      output.close();
    }
  }

  /**
   * @return
   */
  private OutputStream getOutputStream()
  {
    return output;
  }

  /**
   * @return
   */
  private InputStream getInputStream()
  {
    return input;
  }
}
