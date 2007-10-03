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
 * StackFramePacketHandler.java
 * 
 * A class to handle JDWP commands which belong to the 
 * StackFrame command set.
 * 
 * @author Paulo Abadie Guedes
 *
 * 12/06/2007 - 19:03:06
 * 
 */
public class StackFramePacketHandler extends JDWPPacketHandler implements
    PacketHandler
{
  public StackFramePacketHandler(JOPDebugInterface debugInterface,
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
      case CommandConstants.StackFrame_GetValues: // 1
      {
        handleGetValues(packet);
        break;
      }
      case CommandConstants.StackFrame_SetValues: // 2
      {
        handleSetValues(packet);
        break;
      }
      case CommandConstants.StackFrame_ThisObject: // 3
      {
        handleThisObject(packet);
        break;
      }
      case CommandConstants.StackFrame_PopFrames: // 4
      {
        handlePopFrames(packet);
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
   * @throws IOException 
   * @throws JDWPException 
   */
  private void handleGetValues(PacketWrapper packet) throws IOException, JDWPException
  {
    int index = 0;
    GenericReferenceDataList list;
    GenericReferenceData data;
    
    writer.clear();
    
    long threadId = readObjectId(packet);
    long frameId = reader.readFrameId();
    
    // read the data to know which local variables to get 
    int slots = reader.readInt();
    list = new GenericReferenceDataList();
    for(index = 0; index < slots; index++)
    {
      int localVariableIndex = reader.readInt();
      byte tagConstant = reader.readByte();
      
      data = new GenericReferenceData(localVariableIndex);
      data.setTag(tagConstant);
      
      list.add(data);
    }
    
    // query the Java machine to get the stack values
    list = debugInterface.getStackFrameValues(threadId, frameId, list);
    
    // write all values into the packet
    writer.writeInt(list.size());
    writer.writeTaggedValueList(list);
    
    // and deliver it, at last!
    sendReplyPacket(packet);
  }
  
  /**
   * @param packet
   * @throws IOException 
   * @throws JDWPException 
   */
  private void handleSetValues(PacketWrapper packet) throws IOException, JDWPException
  {
    GenericReferenceDataList list;
    
    writer.clear();
    
    long threadId = readObjectId(packet);
    long frameId = reader.readFrameId();
    
    // read the data to know which local variables to set 
    int length = reader.readInt();
    list = reader.readTaggedValueList(length);
    
    // request the Java machine to set the stack values
    debugInterface.setStackFrameValues(threadId, frameId, list);
    
    // write no values into the packet: reply data should be empty.
    // send the answer.
    sendReplyPacket(packet);
  }
  
  /**
   * @param packet
   * @throws IOException 
   * @throws JDWPException 
   */
  private void handleThisObject(PacketWrapper packet) throws JDWPException, IOException
  {
    GenericReferenceData data;
    
    writer.clear();
    
    long threadId = readObjectId(packet);
    long frameId = reader.readFrameId();
    
    // request the "this" pointer
    data = debugInterface.getThisObject(threadId, frameId);
    
    // write the objectId to the packet
    writer.writeTaggedValue(data);
    
    // send the answer
    sendReplyPacket(packet);
  }

  /**
   * @param packet
   */
  private void handlePopFrames(PacketWrapper packet)
  {
    // not necessary now
    throwErrorNotImplemented(packet);
  }
}
