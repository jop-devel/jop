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

package com.jopdesign.debug.jdwp.handler;

import java.io.IOException;

import com.jopdesign.debug.jdwp.util.BasicWorker;
import com.jopdesign.debug.jdwp.util.PacketQueue;
import com.sun.tools.jdi.PacketWrapper;

/**
 * A basic packet handler is a worker which knows how to
 * handle or consume JDWP packets in some way.
 * 
 * It works closely with a PacketQueue, which holds the
 * packets for consumption.
 * 
 * Subclasses are free to decide what to do with the packets.
 * This class is the base for other PacketHandlers on this package.
 * 
 * It can also be used to create a Thread which can handle packets, 
 * using one of the constructors that accept objects implementing
 * the PacketHandler interface. It works in a way very similar to the 
 * way the Thread class and Runnable interface work together.
 * 
 * If no handler is provided, the basic implementation just ignore
 * packets. If a handler is provided, packet handling is delegated
 * to it. This way, classes are free to inherit from other classes
 * and just implement PacketHandler when needed.
 * 
 * @author Paulo Abadie Guedes
 */
public class BasicPacketHandler extends BasicWorker implements PacketHandler
{
  private PacketQueue queue;
  private PacketHandler handler;
  
  public BasicPacketHandler(String threadId, PacketQueue queue)
  {
    super(threadId);
    setQueue(queue);
  }
  
  public BasicPacketHandler(PacketQueue queue)
  {
    setQueue(queue);
  }
  
  public BasicPacketHandler(String threadId, PacketQueue queue, PacketHandler handler)
  {
    super(threadId);
    setQueue(queue);
    setHandler(handler);
  }
  
  public BasicPacketHandler(PacketQueue queue, PacketHandler handler)
  {
    setQueue(queue);
    setHandler(handler);
  }
  
  private void setQueue(PacketQueue queue)
  {
    this.queue = queue;
  }
  
  private void setHandler(PacketHandler handler)
  {
    this.handler = handler;
  }
  
  public void run()
  {
    while(isWorking())
    {
      try
      {
        PacketWrapper packet = queue.removeNext();
        handlePacket(packet);
      }
      catch (IOException e)
      {
        stopWorking();
      }
    }
  }
  
  /**
   * This method is responsible for packet handling.
   * 
   * The basic implementation just ignore the packet and return.
   * If there is a handler registered, use it to handle the packet.
   */
  public void handlePacket(PacketWrapper packet) throws IOException
  {
    if(handler != null)
    {
      handler.handlePacket(packet);
    }
  }
}
