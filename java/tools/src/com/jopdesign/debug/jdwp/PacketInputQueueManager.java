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

import com.jopdesign.debug.jdwp.sniffer.PacketInputStreamReader;
import com.jopdesign.debug.jdwp.util.BasicWorker;
import com.jopdesign.debug.jdwp.util.Debug;
import com.jopdesign.debug.jdwp.util.PacketQueue;
import com.sun.tools.jdi.PacketWrapper;

/**
 * This communication interface is responsible for conversions from
 * an input stream. It parses input data and convert it into a 
 * sequence of packets. The packets are queued and can be 
 * consumed later by another object. 
 *  
 * @author Paulo Abadie Guedes
 */
public class PacketInputQueueManager extends BasicWorker
{
  private PacketInputStreamReader input;
  private PacketQueue queue;
  
  /**
   * This is the object responsible to create, hold and manage 
   * an input queue. This is created from the input stream.
   * 
   * When this Thread is running, as long as new data is available, 
   * the data is read and splitted into packets. The packets are then
   * made available into a queue, for later consumption by another
   * object. 
   * 
   */
  public PacketInputQueueManager(String threadID, InputStream stream, 
    PacketQueue queue) throws IOException
  {
    super(threadID);
    init(stream, queue);
  }
  
  public PacketInputQueueManager(InputStream stream, PacketQueue queue) 
    throws IOException
  {
    super();
    init(stream, queue);
  }
  
  /**
   * Standard way to initialize data for this class.
   * 
   * @param stream
   * @param queue
   * @throws IOException
   */
  private void init(InputStream stream, PacketQueue queue) throws IOException
  {
    this.input = new PacketInputStreamReader(stream);
    this.queue = queue;    
  }
  
  public void run()
  {
    while(isWorking())
    {
      try
      {
        PacketWrapper packet = input.readPacket();
        queue.add(packet);
        
        Debug.println("  Received packet! ");
      }
      catch (IOException e)
      {
        stopWorking();
        e.printStackTrace();
      }
    }
  }
}
