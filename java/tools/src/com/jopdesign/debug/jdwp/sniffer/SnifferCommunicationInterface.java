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

package com.jopdesign.debug.jdwp.sniffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

import com.jopdesign.debug.jdwp.io.DuplicateOutputStream;
import com.jopdesign.debug.jdwp.util.Worker;

// IMPROVE: make this class inherit from my PipeThread instead of Thread
public class SnifferCommunicationInterface extends Thread
{
  private Worker worker;
  private Socket socket;
  private InputStream inputStream;
  
  private OutputStream outputStream;
  private OutputStream snifferOutputStream;
  private DuplicateOutputStream duplicateOutputStream;
  
  // size of the read buffer used to transport data.
  private static final int BUFFER_SIZE = 4 * 1024;

  public SnifferCommunicationInterface(Socket snifferSocket) throws IOException
  {
    this(null, snifferSocket);
  }
  
  public SnifferCommunicationInterface(Worker listener, Socket snifferSocket) throws IOException
  {
    worker = listener;
    socket = snifferSocket;
  }
  
  /**
   * Initialize internal variables.
   * 
   * @throws IOException
   */
  public void initialize(OutputStream output, OutputStream snifferStream) throws IOException
  {
    inputStream = socket.getInputStream();
    
    outputStream = output;
    
    snifferOutputStream = snifferStream;
    
    duplicateOutputStream = new DuplicateOutputStream(outputStream, snifferOutputStream);
  }

  /**
   * @return the inputStream
   */
  private InputStream getInputStream()
  {
    return inputStream;
  }

  /**
   * @return the outputStream
   */
  private OutputStream getOutputStream()
  {
    return duplicateOutputStream;
  }
  
  /**
   * Close all streams and finish execution.
   * 
   * @throws IOException 
   */
  public void close() throws IOException
  {
    duplicateOutputStream.close();
    snifferOutputStream.close();
    outputStream.close();
    inputStream.close();
    socket.close();
  }
  
  /**
   * This method will pump bytes from the input stream to both output
   * streams over and over again until there is nothing more to read.
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
        // interesting to see the chunks of data being written.
        // helps to better understand the logic behind 
        // packet creation in the debugger;) 
//        System.out.println("Num: " + num);
        
        // flush the stream every BUFFER_SIZE bytes. No specific need to
        // be BUFFER_SIZE or any other value, but it's better doing this
        // by number of bytes than using a timeout.
        // avoid too small values, just to prevent too much flush calls.
        if(count > BUFFER_SIZE)
        {
//          System.out.println(" Bytes: " + count);
          out.flush();
          count = 0;
        }
      }
    }
    catch (SocketException e)
    {
      System.out.println("  Socket closed.");
    }
    catch (IOException e)
    {
      System.out.println("  Failure: " + e.getMessage());
      e.printStackTrace();
    }
    finally
    {
      // notify if necessary
      if(worker != null)
      {
        worker.stopWorking();
      }
      
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
}
