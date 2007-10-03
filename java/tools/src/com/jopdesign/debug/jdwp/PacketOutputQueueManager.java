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
import java.io.OutputStream;

import com.jopdesign.debug.jdwp.handler.BasicPacketHandler;
import com.jopdesign.debug.jdwp.sniffer.PacketOutputStreamWriter;
import com.jopdesign.debug.jdwp.util.Debug;
import com.jopdesign.debug.jdwp.util.PacketQueue;
import com.sun.tools.jdi.PacketWrapper;

/**
 * This communication interface is responsible for conversions to
 * an output stream. It queries a queue and convert its packets into a 
 * sequence of bytes. The bytes are then written to the output stream. 
 *  
 * @author Paulo Abadie Guedes
 */
public class PacketOutputQueueManager extends BasicPacketHandler
{
  private PacketOutputStreamWriter output;
  
  // TODO: REMOVE
//  private PacketQueue queue;
//  private PacketQueueManager queueManager;
  
  /**
   * This is the object responsible to create, hold and manage 
   * an output queue. This is used to feed the output stream.
   * 
   * When this Thread is running, as long as new packets are available, 
   * the data is read and coverted into JDWP packets. The packets are then
   * written to the output stream.
   */
  public PacketOutputQueueManager(String threadId, OutputStream stream, 
    PacketQueue queue) throws IOException
  {
    super(threadId, queue);
    output = new PacketOutputStreamWriter(stream);
  }
  
  public PacketOutputQueueManager(OutputStream stream, 
      PacketQueue queue) throws IOException
    {
      super(queue);
      output = new PacketOutputStreamWriter(stream);
    }
  
  /**
   * Handle the packet properly. This method write the packet to the 
   * corresponding output stream.
   */
  public void handlePacket(PacketWrapper packet) throws IOException
  {
    output.writePacket(packet);
    output.flush();
    
    Debug.println(" Sent packet: " + packet);
  }
  
  //TODO: remove this. Just to test the synchronization
//  public void run_debug()
//  {
//    PacketWrapper packet = PacketWrapper.createHandshakePacket();
//    try
//    {
//      Thread.sleep(1000);
//      handlePacket(packet);
//    }
//    catch (InterruptedException e)
//    {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    } catch (IOException e)
//    {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    }
//    
//    super.run();
//  }
}
