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

import com.jopdesign.debug.jdwp.JOPDebugInterface;
import com.jopdesign.debug.jdwp.constants.CommandConstants;
import com.jopdesign.debug.jdwp.util.PacketQueue;
import com.sun.tools.jdi.PacketWrapper;

/**
 * ClassObjectReferencePacketHandler.java
 * 
 * A class to handle JDWP commands which belong to the 
 * ClassObjectReference command set.
 * 
 * @author Paulo Abadie Guedes
 *
 * 13/06/2007 - 18:15:49
 * 
 */
public class ClassObjectReferencePacketHandler extends JDWPPacketHandler
    implements PacketHandler
{
  public ClassObjectReferencePacketHandler(JOPDebugInterface debugInterface,
      PacketQueue outputQueue)
  {
    super(debugInterface, outputQueue);
  }

  public void replyPacket(PacketWrapper packet) throws IOException,
      JDWPException
  {
    int command = packet.getCmd();
    writer.clear();

    switch (command)
    {
      case CommandConstants.ClassObjectReference_ReflectedType: // 1
      {
        handleReflectedType(packet);
        break;
      }
      
      default:
      {
        // reply with an error code packet stating "not implemented"
        throwErrorNotImplemented(packet);
      }
    }
  }

  /**
   * @param packet
   */
  private void handleReflectedType(PacketWrapper packet)
  {
    // not necessary now
    throwErrorNotImplemented(packet);
   }
}
