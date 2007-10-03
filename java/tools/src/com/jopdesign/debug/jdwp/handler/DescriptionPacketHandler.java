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

import com.jopdesign.debug.jdwp.util.Debug;
import com.jopdesign.debug.jdwp.util.PacketQueue;
import com.sun.tools.jdi.PacketWrapper;

/**
 * A simple class to test basic packet handling on the JDWP
 * protocol.
 * 
 * This class behaves as follows:
 * - If a handshake packet arrives and the reply flag is set, handshake
 * - For all received packets, print a simple description
 *   to the standard output.
 * 
 * @author Paulo Abadie Guedes
 */
public class DescriptionPacketHandler extends BasicPacketHandler
{
  private PacketQueue outputQueue;
  boolean shouldReply = false;
  int count = 0;
  
  public DescriptionPacketHandler(String threadName, PacketQueue inputQueue, PacketQueue outputQueue)
  {
    super(threadName, inputQueue);
    this.outputQueue = outputQueue;
  }
  
  public DescriptionPacketHandler(PacketQueue inputQueue, PacketQueue outputQueue)
  {
    super(inputQueue);
    this.outputQueue = outputQueue;
  }

  /**
   * This method is responsible for packet handling.
   * The basic implementation just ignore the packet and return.
   */
  public void handlePacket(PacketWrapper packet) throws IOException
  {
    if(packet.isHandshakePacket() && shouldReply)
    {
      outputQueue.add(packet);
    }
    count ++;
    Debug.print(count + ": ");
    packet.printInformation();
  }
  
  public void setReplyFlag()
  {
    shouldReply = true;
  }
}
