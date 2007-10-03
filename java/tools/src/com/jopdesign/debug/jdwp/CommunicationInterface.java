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

package com.jopdesign.debug.jdwp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class CommunicationInterface extends Thread
{
  private InputStream in;
  private OutputStream out;
  
  // size of the read buffer used to transport data.
  private static final int BUFFER_SIZE = 4 * 1024;
  
  private byte data[] = new byte[BUFFER_SIZE];
  
  public CommunicationInterface(InputStream in, OutputStream out) throws IOException
  {
    this.in = in;
    this.out = out;
  }
  
  /**
   * @return the inputStream
   * @throws IOException 
   */
  protected InputStream getInputStream() throws IOException
  {
    return in;
  }

  /**
   * @return the outputStream
   */
  protected OutputStream getOutputStream() throws IOException
  {
    return out;
  }
  
  /**
   * Close all streams and finish execution.
   * 
   * @throws IOException 
   */
  public void close() throws IOException
  {
    try
    {
      in.close();
    }
    finally
    {
      out.close();
    }
  }
  
  /**
   * This method will pump bytes from the input stream to the output
   * stream over and over again until there is nothing more to read.
   * 
   * Then it will close all streams.
   */  
  public void run()
  {
    try
    {
      work();
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
        System.out.println("  Failure closing: " + e.getMessage());
        e.printStackTrace();
      }
      finally
      {
        // if needed, notify another objects that work here is done. 
        notifyStopWorking();
      }
    }
  }
  
  /**
   * This is the main method that should be overridden by subclasses.
   * It is responsible to handle data from the streams in some way.
   * 
   * @param in
   * @param out
   * @throws IOException
   */
  public void work() throws IOException
  {
    int num = 1;
    
    while (num > -1)
    {
      num = pumpData();
    }
  }
  
  /**
   * The default implementation for the work method is to forward data 
   * from one stream to the other.
   * 
   * @return
   * @throws IOException
   */
  public final int pumpData() throws IOException
  {
    int num = in.read(data);
    if(num > -1)
    {
      out.write(data, 0, num);
    }
    
    return num;    
  }

  /**
   * This method will be called after all work is done in 
   * and just after this object stops running.
   * 
   * The default implementation does nothing and just return. 
   */
  public void notifyStopWorking()
  {
    
  }
}
