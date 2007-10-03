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
import com.jopdesign.debug.jdwp.constants.ErrorConstants;
import com.jopdesign.debug.jdwp.constants.JDWPConstants;
import com.jopdesign.debug.jdwp.model.GenericReferenceData;
import com.jopdesign.debug.jdwp.model.GenericReferenceDataList;
import com.jopdesign.debug.jdwp.util.PacketQueue;
import com.sun.tools.jdi.PacketWrapper;

// TODO: remove unnecessary try/catch blocks from the first eight handlers.
// now exceptions are all being handled on the previous method. 

/**
 * 
 * ReferenceTypePacketHandler.java
 * 
 * A class to handle JDWP commands which belong to the
 * ReferenceType command set.
 * 
 * @author Paulo Abadie Guedes
 * 21/05/2007 - 23:25:14
 *
 */
public class ReferenceTypePacketHandler 
  extends JDWPPacketHandler
{
  public ReferenceTypePacketHandler(JOPDebugInterface debugInterface, PacketQueue outputQueue)
  {
    super(debugInterface, outputQueue);
  }
  
  public void replyPacket(PacketWrapper packet) throws IOException, JDWPException
  {
    int command = packet.getCmd();
    
    switch(command)
    {
      case CommandConstants.ReferenceType_Signature: // 1
      {
        handleSignature(packet);
        break;
      } // 1
      case CommandConstants.ReferenceType_ClassLoader: // 2
      {
        handleClassLoader(packet);
        break;
      } // 2
      case CommandConstants.ReferenceType_Modifiers: // 3
      {
        handleModifiers(packet);
        break;
      } // 3
      case CommandConstants.ReferenceType_Fields: // 4
      {
        handleFields(packet);
        break;
      } // 4
      case CommandConstants.ReferenceType_Methods: // 5
      {
        handleMethods(packet);
        break;
      } // 5
      case CommandConstants.ReferenceType_GetValues: // 6
      {
        handleGetValues(packet);
        break;
      } // 6
      case CommandConstants.ReferenceType_SourceFile: // 7
      {
        handleSourceFile(packet);
        break;
      } // 7
      case CommandConstants.ReferenceType_NestedTypes: // 8
      {
        handleNestedTypes(packet);
        break;
      } // 8
      case CommandConstants.ReferenceType_Status: // 9
      {
        handleStatus(packet);
        break;
      } // 9
      case CommandConstants.ReferenceType_Interfaces: // 10
      {
        handleInterfaces(packet);
        break;
      } //10
      case CommandConstants.ReferenceType_ClassObject: //11
      {
        handleClassObject(packet);
        break;
      } //11
      case CommandConstants.ReferenceType_SourceDebugExtension: //12
      {
        handleSourceDebugExtension(packet);
        break;
      } //12
      default:
      {
        // reply with an error code packet stating "not implemented"
        throwErrorNotImplemented(packet);
      }
    }
  }
  
  private void handleSignature(PacketWrapper packet) // 1
  {
    writer.clear();
    
    long referenceType = readReferenceTypeId(packet);
    String signature = debugInterface.getSignature(referenceType);
    
    if(signature != null)
    {
      writer.writeJDWPString(signature);
      sendReplyPacket(packet);
    }
    else
    {
      sendReplyPacket(packet, ErrorConstants.ERROR_INVALID_CLASS);
    }
  }
  
  private void handleClassLoader(PacketWrapper packet) throws JDWPException // 2
  {
    writer.clear();
    
    long referenceType = readReferenceTypeId(packet);
    long loaderID = debugInterface.getClassLoaderID(referenceType);
    writer.writeID(loaderID, JDWPConstants.objectIDSize);
    sendReplyPacket(packet);
  }

  private void handleModifiers(PacketWrapper packet) throws JDWPException // 3
  {
    writer.clear();
    
    long referenceType = readReferenceTypeId(packet);
    int data = debugInterface.getModifiers(referenceType);
    writer.writeInt(data);
    sendReplyPacket(packet);
  }
  
  private void handleFields(PacketWrapper packet) throws JDWPException // 4
  {
    writer.clear();
    
    long referenceType = readReferenceTypeId(packet);
    GenericReferenceDataList list;
    list = debugInterface.getFieldReferenceList(referenceType);
    
    handleFieldOrMethod(list, JDWPConstants.fieldIDSize);
    
    sendReplyPacket(packet);
  }

  /**
   * @param list
   */
  private void handleFieldOrMethod(GenericReferenceDataList list, int idSize)
  {
    int index, size;
    GenericReferenceData fieldOrMethod;
    
    long fieldOrMethodId;
    String name;
    String signature;
    int modifiers = 0;
    
    size = list.size();
    writer.writeInt(size);
    for(index = 0; index < size; index++)
    {
      fieldOrMethod = list.get(index);
      
      fieldOrMethodId = fieldOrMethod.getFieldOrMethodId();
      name = fieldOrMethod.getName();
      signature = fieldOrMethod.getSignature();
      modifiers = fieldOrMethod.getModifiers();
      
      writer.writeID(fieldOrMethodId, idSize);
      writer.writeJDWPString(name);
      writer.writeJDWPString(signature);
      writer.writeInt(modifiers);
    }
  }
  
  private void handleMethods(PacketWrapper packet) throws JDWPException // 5
  {
    writer.clear();
    
    long referenceType = readReferenceTypeId(packet);
    GenericReferenceDataList list;
    list = debugInterface.getMethodReferenceList(referenceType);
    
    handleFieldOrMethod(list, JDWPConstants.methodIDSize);
    
    sendReplyPacket(packet);
  }

  private void handleGetValues(PacketWrapper packet) throws JDWPException // 6
  {
    writer.clear();
    
    int index;
    int id;
    GenericReferenceData reference;
    GenericReferenceDataList referenceList = new GenericReferenceDataList();
    
    long referenceType = readReferenceTypeId(packet);
    int numValues = reader.readInt();
    
    for(index = 0; index < numValues; index++)
    {
      id = reader.readInt();
      reference = new GenericReferenceData(id);
      referenceList.add(reference);
    }
    
    debugInterface.getStaticValues(referenceType, referenceList);
    writer.writeInt(numValues);
    
    for(index = 0; index < numValues; index++)
    {
      reference = referenceList.get(index);
      writer.writeTaggedValue(reference);
    }
    
    sendReplyPacket(packet);
  }

  private void handleSourceFile(PacketWrapper packet) throws JDWPException // 7
  {
    writer.clear();
    
    // get the data from the input packet
    String sourceFile = "";
    long referenceTypeId = readReferenceTypeId(packet);
    
    // build the reply packet and post it
    sourceFile = debugInterface.getSourceFile(referenceTypeId);
    writer.writeJDWPString(sourceFile);
    
    sendReplyPacket(packet);
  }

  private void handleNestedTypes(PacketWrapper packet) throws JDWPException // 8
  {
    writer.clear();
    
    int index;
    long typeId = readReferenceTypeId(packet);
    
    GenericReferenceDataList list;
    GenericReferenceData data;
    list = debugInterface.getNestedTypesList(typeId);
    
    int numTypes = list.size();
    writer.writeInt(numTypes);
    for(index = 0; index < numTypes; index++)
    {
      data = list.get(index);
      int tag = data.getTag();
      long referenceTypeId = data.getReferenceTypeId();
      
      writer.writeByte(tag);
      writer.writeReferenceTypeId(referenceTypeId);
    }
    
    sendReplyPacket(packet);
  }

  private void handleStatus(PacketWrapper packet) throws JDWPException // 9
  {
    writer.clear();
    
    long typeId = readReferenceTypeId(packet);
    
    int status = debugInterface.getStatus(typeId);
    writer.writeInt(status);
    
    sendReplyPacket(packet);
  }

  private void handleInterfaces(PacketWrapper packet) throws JDWPException // 10
  {
    writer.clear();
    
    int index;
    long typeId = readReferenceTypeId(packet);
    
    GenericReferenceDataList list;
    GenericReferenceData data;
    list = debugInterface.getDeclaredInterfacesList(typeId);
    
    int numTypes = list.size();
    writer.writeInt(numTypes);
    for(index = 0; index < numTypes; index++)
    {
      data = list.get(index);
      
      long referenceTypeId = data.getReferenceTypeId();
      writer.writeReferenceTypeId(referenceTypeId);
    }
    
    sendReplyPacket(packet);
  }
  
  private void handleClassObject(PacketWrapper packet) throws JDWPException // 11
  {
    writer.clear();
    
    long classId;
    long typeId = readReferenceTypeId(packet);
    
    classId = debugInterface.getClassObject(typeId);
    writer.writeReferenceTypeId(classId);
    
    sendReplyPacket(packet);
  }

  private void handleSourceDebugExtension(PacketWrapper packet) throws JDWPException // 12
  {
    writer.clear();
    
    String extension;
    long typeId = readReferenceTypeId(packet);
    
    extension = debugInterface.getSourceDebugExtension (typeId);
    writer.writeJDWPString(extension);
    
    sendReplyPacket(packet);
  }
}
