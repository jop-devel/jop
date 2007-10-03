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
import com.jopdesign.debug.jdwp.util.PacketQueue;
import com.sun.tools.jdi.PacketWrapper;

/**
 * 
 * ClassTypePacketHandler.java
 * 
 * A class to handle JDWP commands which belong to the
 * ClassType command set.
 *  
 * @author Paulo Abadie Guedes
 * 24/05/2007 - 10:44:01
 *
 */
public class ClassTypePacketHandler extends JDWPPacketHandler
{
  public ClassTypePacketHandler(JOPDebugInterface debugInterface, PacketQueue outputQueue)
  {
    super(debugInterface, outputQueue);
  }
  
  public void replyPacket(PacketWrapper packet) throws IOException, JDWPException
  {
    int command = packet.getCmd();
    writer.clear();
    
    switch(command)
    {
      case CommandConstants.ClassType_Superclass: // 1
      {
        handleSuperclass(packet);
        break;
      } // 1
      case CommandConstants.ClassType_SetValues: // 2
      {
        handleSetValues(packet);
        break;
      } // 2
      case CommandConstants.ClassType_InvokeMethod: // 3
      {
        handleInvokeMethod(packet);
        break;
      } // 3
      case CommandConstants.ClassType_NewInstance: // 4
      {
        handleNewInstance(packet);
        break;
      } // 4
      default:
      {
        // reply with an error code packet stating "not implemented"
        throwErrorNotImplemented(packet);
      }
    }
  }
  
  private void handleSuperclass(PacketWrapper packet) throws JDWPException
  {
    long classId = readReferenceTypeId(packet);
    classId = debugInterface.getSuperclass(classId);
    writer.writeReferenceTypeId(classId);
    
    sendReplyPacket(packet);
  }
  
  private void handleSetValues(PacketWrapper packet) throws JDWPException
  {
    int i;
    long fieldId;
    GenericReferenceData data;
    GenericReferenceDataList list = new GenericReferenceDataList();
    long classId = readReferenceTypeId(packet);
    int numFields = reader.readInt();
    
    for(i = 0; i < numFields; i++)
    {
      fieldId = reader.readFieldId();
      data = debugInterface.getStaticFieldReferenceData(classId, fieldId);
      reader.readUntaggedValue(data);
      list.add(data);
    }
    
    debugInterface.setStaticValues(classId, list);
    
//     TODO: check if there is actually a need to reply this
    sendReplyPacket(packet);
  }
  
  private void handleInvokeMethod(PacketWrapper packet) throws JDWPException, IOException
  {
//  TODO Auto-generated method stub
    super.replyPacket(packet);
  }
  
  private void handleNewInstance(PacketWrapper packet) throws JDWPException, IOException
  {
    // TODO Auto-generated method stub
    super.replyPacket(packet);
  }
}
