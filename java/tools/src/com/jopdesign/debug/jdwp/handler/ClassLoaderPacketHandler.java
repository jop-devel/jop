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
import com.jopdesign.debug.jdwp.model.ReferenceType;
import com.jopdesign.debug.jdwp.model.ReferenceTypeList;
import com.jopdesign.debug.jdwp.util.PacketQueue;
import com.sun.tools.jdi.PacketWrapper;

/**
 * 
 * ClassLoaderPacketHandler.java
 * 
 * A class to handle JDWP commands which belong to the ClassLoader command set.
 * 
 * @author Paulo Abadie Guedes
 * 
 * 30/05/2007 - 12:14:01
 * 
 */
public class ClassLoaderPacketHandler extends JDWPPacketHandler
{
  public ClassLoaderPacketHandler(JOPDebugInterface debugInterface,
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
      case CommandConstants.ClassLoaderReference_VisibleClasses: // 1
      {
        handleVisibleClasses(packet);
        break;
      } // 1

      default:
      {
        // reply with an error code packet stating "not implemented"
        throwErrorNotImplemented(packet);
      }
    }
  }

  private void handleVisibleClasses(PacketWrapper packet) throws JDWPException
  {
    long classId = readReferenceTypeId(packet);

    classId = debugInterface.getSuperclass(classId);
    writer.writeReferenceTypeId(classId);
    ReferenceTypeList list = debugInterface.getVisibleClasses(classId);
    ReferenceType type;

    int index;
    int size = list.size();
    writer.writeInt(size);
    for (index = 0; index < size; index++)
    {
      type = list.get(index);

      writer.writeByte(type.getTypeTag());
      writer.writeReferenceTypeId(type.getTypeID());
    }
    sendReplyPacket(packet);
  }
}
