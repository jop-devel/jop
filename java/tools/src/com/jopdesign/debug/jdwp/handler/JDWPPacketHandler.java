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
import com.jopdesign.debug.jdwp.constants.ErrorConstants;
import com.jopdesign.debug.jdwp.constants.JDWPConstants;
import com.jopdesign.debug.jdwp.io.PacketReader;
import com.jopdesign.debug.jdwp.io.PacketWriter;
import com.jopdesign.debug.jdwp.model.ObjectReference;
import com.jopdesign.debug.jdwp.model.ObjectReferenceList;
import com.jopdesign.debug.jdwp.model.ReferenceType;
import com.jopdesign.debug.jdwp.util.PacketQueue;
import com.sun.tools.jdi.PacketWrapper;

/**
 * JDWPPacketHandler.java
 * 
 * This class will be the base for most of the packet 
 * handler hierarchy. It contains methods and fields 
 * which are common to most of the packet handlers 
 * used inside the MainPacketHandler.
 * 
 * Usage:
 * 
 * The general contract for subclasses is to overwrite
 * the handlePacket method.
 * To answer, do the following: 
 * 
 * 1) Read content if necessary using the reader field.
 * 2) Clear the writer field
 * 3) Use writer to build all fields needed
 * 4) Call one of the forms of "sendReplyPacket" to make the object
 *    render a brand new packet and add it to the output queue.
 *    If an error code should be sent, inform it. 
 * 
 * The default implementation will handle packets just by replying
 * them with an empty packet, whose error code is
 * set to ErrorConstants.ERROR_NOT_IMPLEMENTED. 
 * 
 * @author Paulo Abadie Guedes
 * 21/05/2007 - 14:50:31
 *
 */
public class JDWPPacketHandler implements PacketHandler
{
  protected JOPDebugInterface debugInterface;
  
  protected PacketQueue outputQueue;
  protected PacketWriter writer;
  protected PacketReader reader;

  public JDWPPacketHandler(JOPDebugInterface debugInterface, PacketQueue queue)
  {
    this.debugInterface = debugInterface;
    this.outputQueue = queue;
    this.writer = new PacketWriter();
    this.reader = new PacketReader();    
  }
  
//  public JDWPPacketHandler(PacketQueue queue)
//  {
//    this.outputQueue = queue;
//    this.writer = new PacketWriter();
//    this.reader = new PacketReader();
//  }
//  
  /**
   * The default implementation call reply packet to handle the request.
   * 
   * If a JDWPException is thrown, this method catch it and send a
   * reply packet with the correspoding error code.
   * 
   * Subclasses should not override this method.
   * Instead, override the replyPacket method and throw a JDWPException
   * if there is any error.
   */
  public final void handlePacket(PacketWrapper packet) throws IOException
  {
    try
    {
      // try to reply. If there's an exception, reply with an error code. 
      replyPacket(packet);
    }
    catch(JDWPException exception)
    {
      sendReplyPacket(packet, exception.getErrorCode());
    }
    catch(IOException exception)
    {
      // send a reply packet
      System.out.println("  IOexception! " + exception.getLocalizedMessage());
      exception.printStackTrace();
      sendReplyPacket(packet, ErrorConstants.ERROR_INTERNAL);
      
      // rethrow the exception so the debug session will finish
      throw exception;
    }
    catch(Throwable exception)
    {
      System.out.println("  Unexpected exception! " + exception.getLocalizedMessage());
      exception.printStackTrace();
      sendReplyPacket(packet, ErrorConstants.ERROR_INTERNAL);
    }
  }
  
  /**
   * The default implementatoin just throw an exception stating 
   * "not implemented". Subclasses should override this method to 
   * provide packet handling services.
   * 
   * Errors will be represented by exceptions and treated properly 
   * on the handlePacket method, since error handling is 
   * very similar for all possible types of error.
   * 
   * @param packet
   * @throws JDWPException
   * @throws IOException
   */
  public void replyPacket(PacketWrapper packet) throws JDWPException, IOException
  {
    throwErrorNotImplemented(packet);
  }
  
  protected void throwErrorNotImplemented(PacketWrapper packet)
  {
    writer.clear();
    sendReplyPacket(packet, ErrorConstants.ERROR_NOT_IMPLEMENTED);
  }
  
  /**
   * Send a packet to the debugger.
   * Use the given packet as a base to assign the same ID 
   * of the original packet.
   * 
   * @param packet
   * @param errorCode
   */
  protected void sendPacket(PacketWrapper packet, int errorCode, boolean isReply)
  {
    // create a packet with the correct data
    PacketWrapper replyPacket = writer.createPacket();
    
    replyPacket.setID(packet);
    if(isReply)
    {
      replyPacket.setReplyPacket();
    }
    replyPacket.setError(errorCode);
    
    outputQueue.add(replyPacket);
  }
  
  /**
   * Send an event packet to the debugger.
   * Use the given packet as a base to assign the same ID 
   * of the original packet.
   * 
   * @param packet
   * @param errorCode
   */
  protected void sendEventPacket(PacketWrapper packet)
  {
    sendPacket(packet, ErrorConstants.ERROR_NONE, false);
  }
  
  /**
   * Send a reply packet to the debugger.
   * Use the given packet as a base to assign the same ID 
   * of the original packet.
   * 
   * @param packet
   * @param errorCode
   */
  protected void sendReplyPacket(PacketWrapper packet, int errorCode)
  {
    sendPacket(packet, errorCode, true);
  }

  protected void sendReplyPacket(PacketWrapper packet)
  {
    sendReplyPacket(packet, ErrorConstants.ERROR_NONE);
  }

  protected long readReferenceTypeId(PacketWrapper packet)
  {
    reader.setPacket(packet);
    return reader.readReferenceTypeID();
  }
  
  protected long readObjectId(PacketWrapper packet)
  {
    reader.setPacket(packet);
    return reader.readObjectID();
  }
  

  protected void writeObjectIdList(ObjectReferenceList list)
  {
    writeIdList(list, JDWPConstants.objectIDSize);
  }
  
  /**
   * @param list
   */
  protected void writeIdList(ObjectReferenceList list, int idSize)
  {
    ObjectReference reference;
    
    int index, size;
    long id;
    size = list.size();
    writer.writeInt(size);
    
    for(index = 0; index < size; index++)
    {
      reference = list.get(index);
      id = reference.getObjectId();
      writer.writeID(id, idSize);
    }
  }
  /**
   * @param count
   * @param list
   * @return
   */
  protected void writeTaggedObjectIdList(ObjectReferenceList list)
  {
    int count, index;
    ObjectReference reference;
    count = list.size();
    for (index = 0; index < count; index++)
    {
      reference = list.get(index);
      writeTaggedObjectId(reference);
    }
  }

  /**
   * @param reference
   */
  protected void writeTaggedObjectId(ObjectReference reference)
  {
    long objectId;
    ReferenceType type;
    byte tag;
    type = reference.getType();
    tag = type.getTypeTag();
    objectId = reference.getObjectId();
    
    writer.writeByte(tag);
    writer.writeObjectId(objectId);
  }
}
