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
import com.jopdesign.debug.jdwp.model.Frame;
import com.jopdesign.debug.jdwp.model.FrameList;
import com.jopdesign.debug.jdwp.model.Location;
import com.jopdesign.debug.jdwp.model.ObjectReference;
import com.jopdesign.debug.jdwp.model.ObjectReferenceList;
import com.jopdesign.debug.jdwp.util.PacketQueue;
import com.sun.tools.jdi.PacketWrapper;

/**
 * 
 * ThreadReferencePacketHandler.java
 * 
 * A class to handle JDWP commands which belong to the
 * ThreadReference command set.
 *  
 * @author Paulo Abadie Guedes 24/05/2007 - 17:37:21
 * 
 */
public class ThreadReferencePacketHandler extends JDWPPacketHandler
{
  public ThreadReferencePacketHandler(JOPDebugInterface debugInterface, PacketQueue outputQueue)
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
      case CommandConstants.ThreadReference_Name: // 1
      {
        handleName(packet);
        break;
      }
      case CommandConstants.ThreadReference_Suspend: // 2
      {
        handleSuspend(packet);
        break;
      }
      case CommandConstants.ThreadReference_Resume: // 3
      {
        handleResume(packet);
        break;
      }
      case CommandConstants.ThreadReference_Status: // 4
      {
        handleStatus(packet);
        break;
      }
      case CommandConstants.ThreadReference_ThreadGroup: // 5
      {
        handleThreadGroup(packet);
        break;
      }
      case CommandConstants.ThreadReference_Frames: // 6
      {
        handleFrames(packet);
        break;
      }
      case CommandConstants.ThreadReference_FrameCount: // 7
      {
        handleFrameCount(packet);
        break;
      }
      case CommandConstants.ThreadReference_OwnedMonitors: // 8
      {
        handleOwnedMonitors(packet);
        break;
      }
      case CommandConstants.ThreadReference_CurrentContendedMonitor: // 9
      {
        handleCurrentContendedMonitor(packet);
        break;
      }
      case CommandConstants.ThreadReference_Stop: // 10
      {
        handleStop(packet);
        break;
      }
      case CommandConstants.ThreadReference_Interrupt: // 11
      {
        handleInterrupt(packet);
        break;
      }
      case CommandConstants.ThreadReference_SuspendCount: // 12
      {
        handleSuspendCount(packet);
        break;
      }
      default:
      {
        // reply with an error code packet stating "not implemented"
        throwErrorNotImplemented(packet);
      }
    }
  }

  // private void handleValue(PacketWrapper packet)
  // {
  // long objectId = readObjectId(packet);
  //    
  // String value = debugInterface.getStringValue(objectId);
  // writer.writeJDWPString(value);
  //    
  // sendReplyPacket(packet);
  // }

  private void handleName(PacketWrapper packet) throws JDWPException // 1
  {
    long objectId = readObjectId(packet);
    
    String value = debugInterface.getThreadName(objectId);
    writer.writeJDWPString(value);
    
    sendReplyPacket(packet);
  }

  private void handleSuspend(PacketWrapper packet) throws JDWPException,
    IOException // 2
  {
    long objectId = readObjectId(packet);
    debugInterface.suspendThread(objectId);    
  }

  private void handleResume(PacketWrapper packet) throws JDWPException // 3
  {
    long objectId = readObjectId(packet);
    debugInterface.resumeThread(objectId);    
  }

  private void handleStatus(PacketWrapper packet) throws JDWPException // 4
  {
    long objectId = readObjectId(packet);
    int threadStatus = debugInterface.getThreadStatus(objectId);
    int suspendStatus = debugInterface.getThreadSuspendStatus(objectId);
    
    writer.writeInt(threadStatus);
    writer.writeInt(suspendStatus);
    
    sendReplyPacket(packet);
  }

  private void handleThreadGroup(PacketWrapper packet) throws JDWPException // 5
  {
    long objectId = readObjectId(packet);
    int threadGroup = debugInterface.getThreadGroup(objectId);
    writer.writeInt(threadGroup);
    
    sendReplyPacket(packet);    
  }

  private void handleFrames(PacketWrapper packet) throws JDWPException // 6
  {
    int i, size;
    FrameList list;
    Frame frame;
    long threadId = readObjectId(packet);
    int startFrame = reader.readInt();
    int length = reader.readInt();
    
    // throw an exception if thread is not suspended
    list = debugInterface.getFrames(threadId, startFrame, length);
    size = list.size();
    for (i = 0; i < size; i++)
    {
      frame = list.get(i);
      long frameId = frame.getFrameId();
      writer.writeFrameId(frameId);
      Location location = frame.getLocation();
      writer.writeLocation(location);
    }
    
    sendReplyPacket(packet);    
  }

  private void handleFrameCount(PacketWrapper packet) throws JDWPException // 7
  {
    long threadId;
    int frameCount;
    
    threadId = readObjectId(packet);
    
    frameCount = debugInterface.getFrameCount(threadId);
    writer.writeInt(frameCount);
    sendReplyPacket(packet);
  }

  private void handleOwnedMonitors(PacketWrapper packet) throws JDWPException // 8
  {
    int count;
    long threadId;
    ObjectReferenceList list;
    threadId = readObjectId(packet);
    
    list = debugInterface.getOwnedMonitors(threadId);
    count = list.size();
    writer.writeInt(count);
    writeTaggedObjectIdList(list);
    
    sendReplyPacket(packet);
  }

  private void handleCurrentContendedMonitor(PacketWrapper packet) throws JDWPException // 9
  {
    long threadId;
    ObjectReference monitor;
    threadId = readObjectId(packet);
    
    monitor = debugInterface.getCurrentContendedMonitor(threadId);
    writeTaggedObjectId(monitor);
    
    sendReplyPacket(packet);    
  }

  private void handleStop(PacketWrapper packet) throws JDWPException // 10
  {
    long threadId, exceptionId;
    
    threadId = readObjectId(packet);
    exceptionId = readObjectId(packet);
    
    debugInterface.stopThread(threadId, exceptionId);
  }

  private void handleInterrupt(PacketWrapper packet) throws JDWPException // 11
  {
    long threadId;
    
    threadId = readObjectId(packet);
    
    debugInterface.interruptThread(threadId);
  }

  private void handleSuspendCount(PacketWrapper packet) throws JDWPException // 12
  {
    long threadId;
    int suspendCount;
    
    threadId = readObjectId(packet);
    suspendCount = debugInterface.getSuspendCount(threadId);
    writer.writeInt(suspendCount);
    sendReplyPacket(packet);
  }
}
