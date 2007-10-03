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
import com.jopdesign.debug.jdwp.model.Line;
import com.jopdesign.debug.jdwp.model.LineTable;
import com.jopdesign.debug.jdwp.model.Variable;
import com.jopdesign.debug.jdwp.model.VariableTable;
import com.jopdesign.debug.jdwp.util.PacketQueue;
import com.sun.tools.jdi.PacketWrapper;

/**
 * 
 * MethodPacketHandler.java
 * 
 * A class to handle JDWP commands which belong to the
 * Method command set.
 *  
 * @author Paulo Abadie Guedes 24/05/2007 - 13:31:11
 * 
 */
public class MethodPacketHandler extends JDWPPacketHandler
{
  public MethodPacketHandler(JOPDebugInterface debugInterface, PacketQueue outputQueue)
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
      case CommandConstants.Method_LineTable: // 1
      {
        handleLineTable(packet);
        break;
      } // 1
      case CommandConstants.Method_VariableTable: // 2
      {
        handleVariableTable(packet);
        break;
      } // 2
      case CommandConstants.Method_Bytecodes: // 3
      {
        handleBytecodes(packet);
        break;
      } // 3
      case CommandConstants.Method_IsObsolete: // 4
      {
        handleIsObsolete(packet);
        break;
      } // 4

      default:
      {
        // reply with an error code packet stating "not implemented"
        throwErrorNotImplemented(packet);
      }
    }
  }
  
  private void handleLineTable(PacketWrapper packet) throws JDWPException
  {
    long typeId = readReferenceTypeId(packet);
    long methodId = reader.readMethodId();
    
    Line line;
    LineTable table;
    
    table = debugInterface.getLineTable(typeId, methodId);
    int i, numLines;
    numLines = table.numLines();
    
    long data;
    
    data = table.getStart();
    writer.writeLong(data);
    
    data = table.getEnd();
    writer.writeLong(data);
    writer.writeInt(numLines);
    
    for(i = 0; i < numLines; i++)
    {
      line = table.getLine(i);
      long codeIndex = line.getLineCodeIndex();
      writer.writeLong(codeIndex);
      int number = line.getLineNumber();
      writer.writeInt(number);
    }
    
    sendReplyPacket(packet);
  }
  
  private void handleVariableTable(PacketWrapper packet) throws JDWPException
  {
    int index,size, data;
    String stringData;
    long typeId;
    long methodId;
    VariableTable table;
    Variable variable;
    
    typeId = readReferenceTypeId(packet);
    methodId = reader.readMethodId();
    
    table = debugInterface.getVariableTable(typeId, methodId);
    
    data = table.getArgCnt();
    writer.writeInt(data);
    data = table.getSlots();
    writer.writeInt(data);
    
    size = table.size();
    for (index = 0; index < size; index++)
    {
      variable = table.getVariable(index);
      
      long codeIndex = variable.getCodeIndex();
      writer.writeLong(codeIndex);
      
      stringData = variable.getName();
      writer.writeJDWPString(stringData);
      
      stringData = variable.getSignature();
      writer.writeJDWPString(stringData);
      
      data = variable.getLength();
      writer.writeInt(data);
      
      data = variable.getSlot();
      writer.writeInt(data);
    }
  }

  private void handleBytecodes(PacketWrapper packet) throws JDWPException
  {
    int index, size;
    long typeId = readReferenceTypeId(packet);
    long methodId = reader.readMethodId();
    
    byte[] bytecodes = debugInterface.getBytecodes(typeId, methodId);
    size = bytecodes.length;
    
    writer.writeInt(size);
    for (index = 0; index < bytecodes.length; index++)
    {
      byte data = bytecodes[index];
      writer.writeByte(data);
    }
  }

  private void handleIsObsolete(PacketWrapper packet) throws JDWPException
  {
    long typeId = readReferenceTypeId(packet);
    long methodId = reader.readMethodId();
    
    boolean isObsolete = debugInterface.isObsolete(typeId, methodId);
    writer.writeBoolean(isObsolete);
  }
}
