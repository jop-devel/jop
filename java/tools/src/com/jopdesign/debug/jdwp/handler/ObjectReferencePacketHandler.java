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
import com.jopdesign.debug.jdwp.model.GenericReferenceData;
import com.jopdesign.debug.jdwp.model.GenericReferenceDataList;
import com.jopdesign.debug.jdwp.model.ObjectReference;
import com.jopdesign.debug.jdwp.model.ReferenceType;
import com.jopdesign.debug.jdwp.util.PacketQueue;
import com.sun.tools.jdi.PacketWrapper;

/**
 * 
 * ObjectReferencePacketHandler.java
 * 
 * A class to handle JDWP commands which belong to the
 * ObjectReference command set.
 *  
 * @author Paulo Abadie Guedes
 * 24/05/2007 - 14:53:52
 *
 */
public class ObjectReferencePacketHandler extends JDWPPacketHandler
{
  public ObjectReferencePacketHandler(JOPDebugInterface debugInterface, PacketQueue outputQueue)
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
      case CommandConstants.ObjectReference_ReferenceType: // 1
      {
        handleReferenceType(packet);
        break;
      } // 1
      case CommandConstants.ObjectReference_GetValues: // 2
      {
        handleGetValues(packet);
        break;
      } // 2
      case CommandConstants.ObjectReference_SetValues: // 3
      {
        handleSetValues(packet);
        break;
      } // 3
      case CommandConstants.ObjectReference_MonitorInfo: // 5
      {
        handleMonitorInfo(packet);
        break;
      } // 5
      case CommandConstants.ObjectReference_InvokeMethod: // 6
      {
        handleInvokeMethod(packet);
        break;
      } // 6
      case CommandConstants.ObjectReference_DisableCollection: // 7
      {
        handleDisableCollection(packet);
        break;
      } // 7
      case CommandConstants.ObjectReference_EnableCollection: // 8
      {
        handleEnableCollection(packet);
        break;
      } // 8
      case CommandConstants.ObjectReference_IsCollected: // 9
      {
        handleIsCollected(packet);
        break;
      } // 9

      default:
      {
        // reply with an error code packet stating "not implemented"
        throwErrorNotImplemented(packet);
      }
    }
  }

  private void handleReferenceType(PacketWrapper packet)
  {
    long objectId = readObjectId(packet);
    
    ObjectReference reference = debugInterface.getObjectReference(objectId);
    ReferenceType type = reference.getType();
    
    byte tag = type.getTypeTag();
    writer.writeByte(tag);
    
    long typeId = type.getTypeID();
    writer.writeReferenceTypeId(typeId);
    
    sendReplyPacket(packet);
  }
  
  private void handleGetValues(PacketWrapper packet)
  {
    int index;
    long objectId = readObjectId(packet);
    
    int numFields = reader.readInt();
    writer.writeInt(numFields);
    
    for (index = 0; index < numFields; index++)
    {
      long fieldId = reader.readFieldId();
      
      GenericReferenceData field = debugInterface.getField(objectId, fieldId);
      writer.writeTaggedValue(field);
    }
    sendReplyPacket(packet);
  }
  
  private void handleSetValues(PacketWrapper packet)
  {
    int index;
    GenericReferenceDataList list = new GenericReferenceDataList();
    long objectId = readObjectId(packet);
    
    int numFields = reader.readInt();
    writer.writeInt(numFields);
    
    for (index = 0; index < numFields; index++)
    {
      long fieldId = reader.readFieldId();
      GenericReferenceData field = debugInterface.getField(objectId, fieldId);
      
      reader.readUntaggedValue(field);
      list.add(field);
    }
    // commit the changes
    debugInterface.setFieldValues(objectId, list);
    
    // TODO check if here should be sent a packet or not.
    sendReplyPacket(packet);
  }
  
  private void handleMonitorInfo(PacketWrapper packet)
  {
    // TODO not implemented now. Will be later.
    throwErrorNotImplemented(packet);
  }
  
  private void handleInvokeMethod(PacketWrapper packet)
  {
    // TODO not implemented now. Will be later.
    throwErrorNotImplemented(packet);
  }
  
  private void handleDisableCollection(PacketWrapper packet)
  {
    // TODO Auto-generated method stub
    throwErrorNotImplemented(packet);
  }
  
  private void handleEnableCollection(PacketWrapper packet)
  {
    // TODO Auto-generated method stub
    throwErrorNotImplemented(packet);
  }
  
  private void handleIsCollected(PacketWrapper packet) throws JDWPException
  {
    // TODO Auto-generated method stub
    throwErrorNotImplemented(packet); 
  }
}
