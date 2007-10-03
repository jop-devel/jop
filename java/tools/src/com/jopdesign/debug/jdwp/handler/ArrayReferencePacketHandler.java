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
import com.jopdesign.debug.jdwp.model.GenericReferenceDataList;
import com.jopdesign.debug.jdwp.model.ReferenceType;
import com.jopdesign.debug.jdwp.util.PacketQueue;
import com.sun.tools.jdi.PacketWrapper;

/**
 * ArrayReferencePacketHandler.java
 * 
 * A class to handle JDWP commands which belong to the 
 * ArrayReference command set.
 * 
 * @author Paulo Abadie Guedes 26/05/2007 - 19:16:41
 * 
 */
public class ArrayReferencePacketHandler extends JDWPPacketHandler
{
  public ArrayReferencePacketHandler(JOPDebugInterface debugInterface, PacketQueue outputQueue)
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
      case CommandConstants.ArrayReference_Length: // 1
      {
        handleLength(packet);
        break;
      }
      case CommandConstants.ArrayReference_GetValues: // 2
      {
        handleGetValues(packet);
        break;
      }
      case CommandConstants.ArrayReference_SetValues: // 3
      {
        handleSetValues(packet);
        break;
      }

      default:
      {
        // reply with an error code packet stating "not implemented"
        throwErrorNotImplemented(packet);
      }
    }
  }

  private void handleLength(PacketWrapper packet) throws JDWPException // 1
  {
     long objectId;
     int length;
     
     objectId = readObjectId(packet);
     length = debugInterface.getArrayLength(objectId);
     writer.writeInt(length);
    
     sendReplyPacket(packet);
  }

  private void handleGetValues(PacketWrapper packet) throws JDWPException // 2
  {
    long objectId;
    int firstIndex, length;
    boolean isObjectArray;
    
    objectId = readObjectId(packet);
    firstIndex = reader.readInt();
    length = reader.readInt();
    
    GenericReferenceDataList list;
    
    isObjectArray = debugInterface.isObjectArray(objectId);
    list = debugInterface.getArrayValues(objectId, firstIndex, length);
    writer.writeByte(list.getTag());
    writer.writeInt(list.size());
    if(isObjectArray)
    {
      writer.writeTaggedValueList(list);
    }
    else
    {
      writer.writeUntaggedObjectIdList(list);
    }
    
    sendReplyPacket(packet);
  }
  
  private void handleSetValues(PacketWrapper packet) throws JDWPException // 3
  {
    long objectId;
    int firstIndex, length;
    
    objectId = readObjectId(packet);
    firstIndex = reader.readInt();
    length = reader.readInt();
    
    GenericReferenceDataList list;
    ReferenceType type = debugInterface.getArrayType(objectId);
    
    // read all values based on the type of the given reference
    list = reader.readUntaggedValueList(length, type);
    
    // set all values to the destination array
    debugInterface.setArrayValues(firstIndex, list);
  }
}
