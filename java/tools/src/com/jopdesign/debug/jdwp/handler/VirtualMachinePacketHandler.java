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
import com.jopdesign.debug.jdwp.constants.Capabilities;
import com.jopdesign.debug.jdwp.constants.CommandConstants;
import com.jopdesign.debug.jdwp.constants.ErrorConstants;
import com.jopdesign.debug.jdwp.constants.JDWPConstants;
import com.jopdesign.debug.jdwp.model.ObjectReferenceList;
import com.jopdesign.debug.jdwp.model.ReferenceType;
import com.jopdesign.debug.jdwp.model.ReferenceTypeList;
import com.jopdesign.debug.jdwp.util.Debug;
import com.jopdesign.debug.jdwp.util.PacketQueue;
import com.sun.tools.jdi.PacketWrapper;

/**
 * 
 * VirtualMachinePacketHandler.java
 * 
 * A class to handle JDWP commands which belong to the
 * VirtualMachine command set.
 * 
 * @author Paulo Abadie Guedes
 * 15/05/2007 - 18:27:37
 *
 */
public class VirtualMachinePacketHandler extends JDWPPacketHandler implements PacketHandler
{
  public VirtualMachinePacketHandler (JOPDebugInterface debugInterface, PacketQueue queue)
  {
    super(debugInterface, queue);
  }
  
  // VirtualMachine Command Set (1)
  public void replyPacket(PacketWrapper packet) throws IOException
  {
    int command = packet.getCmd();
    
    switch(command)
    {
      case CommandConstants.VirtualMachine_Version:  //1;
      {
        handleVersion(packet);
        break;
      }

      case CommandConstants.VirtualMachine_ClassesBySignature:  //2;
      {
        handleClassesBySignature(packet);
        break;
      }
      
      case CommandConstants.VirtualMachine_AllClasses:  //3;
      {
        handleAllClasses(packet);
        break;
      }
      
      case CommandConstants.VirtualMachine_AllThreads:  //4;
      {
        handleAllThreads(packet);
        break;
      }
      
      case CommandConstants.VirtualMachine_TopLevelThreadGroups:  //5;
      {
        handleTopLevelThreadGroups(packet);
        break;
      }
      
      case CommandConstants.VirtualMachine_Dispose:  //6;
      {
        handleDispose(packet);
        break;
      }

      case CommandConstants.VirtualMachine_IDSizes:  //7;
      {
        handleIDSizes(packet);
        break;
      }
      
      case CommandConstants.VirtualMachine_Suspend:  //8;
      {
        handleSuspend(packet);
        break;
      }
      
      case CommandConstants.VirtualMachine_Resume:  //9;
      {
        handleResume(packet);
        break;
      }
      
      case CommandConstants.VirtualMachine_Exit:  //10;
      {
        handleExit(packet);
        break;
      }
      
      case CommandConstants.VirtualMachine_CreateString:  //11;
      {
        handleCreateString(packet);
        break;
      }
      
      case CommandConstants.VirtualMachine_Capabilities:  //12;
      {
        handleCapabilities(packet);
        break;
      }
      
      case CommandConstants.VirtualMachine_ClassPaths:  //13;
      {
        handleClassPaths(packet);
        break;
      }
      
      case CommandConstants.VirtualMachine_DisposeObjects:  //14;
      {
        handleDisposeObjects(packet);
        break;
      }
      
      case CommandConstants.VirtualMachine_HoldEvents:  //15;
      {
        handleHoldEvents(packet);
        break;
      }
      
      case CommandConstants.VirtualMachine_ReleaseEvents:  //16;
      {
        handleReleaseEvents(packet);
        break;
      }
      
      case CommandConstants.VirtualMachine_CapabilitiesNew:  //17;
      {
        handleCapabilitiesNew(packet);
        break;
      }
      
      case CommandConstants.VirtualMachine_RedefineClasses:  //18;
      {
        handleRedefineClasses(packet);
        break;
      }
      
      case CommandConstants.VirtualMachine_SetDefaultStratum:  //19;
      {
        handleSetDefaultStratum(packet);
        break;
      }
      
      default:
      {
        // reply with an error code packet stating "not implemented"
        throwErrorNotImplemented(packet);
      }
    }
  }
  
  private void handleVersion(PacketWrapper packet)
  {
    writer.clear();
    
    writer.writeJDWPString(JDWPConstants.JOP_VERSION);
    
    writer.writeInt(JDWPConstants.JDWP_MAJOR);
    writer.writeInt(JDWPConstants.JDWP_MINOR);
    
    writer.writeJDWPString(JDWPConstants.JOP_JRE_VERSION);
    writer.writeJDWPString(JDWPConstants.JOP_JVM_NAME);
    
    sendReplyPacket(packet);
  }

  private void handleClassesBySignature(PacketWrapper packet)
  {
    writer.clear();
    
    reader.setPacket(packet);
    String signature = reader.readJDWPString();
    
    ReferenceTypeList list = debugInterface.getReferenceTypeList(signature);
    
    // DEBUG: remove or comment this;)
    Debug.println("ReferenceTypeList:");
    Debug.println(list);
    
    ReferenceType type;
    
    int index, size;
    size = list.size();
    writer.writeInt(size);
    
    for(index = 0; index < size; index++)
    {
      type = list.get(index);
      
      writer.writeByte(type.getTypeTag());
      writer.writeID(type.getTypeID(), JDWPConstants.referenceTypeIDSize);
      writer.writeInt(type.getStatus());
    }
    
    sendReplyPacket(packet);
  }
  
  private void handleAllClasses(PacketWrapper packet)
  {
    writer.clear();
    
    ReferenceTypeList list = debugInterface.getAllReferenceTypes();
    ReferenceType type;
    
    int index, size;
    size = list.size();
    writer.writeInt(size);
    
    for(index = 0; index < size; index++)
    {
      type = list.get(index);
      
      writer.writeByte(type.getTypeTag());
      writer.writeID(type.getTypeID(), JDWPConstants.referenceTypeIDSize);
      writer.writeJDWPString(type.getTypeSignature());
      writer.writeInt(type.getStatus());
    }
    
    sendReplyPacket(packet);
  }

  private void handleAllThreads(PacketWrapper packet)
  {
    writer.clear();
    
    ObjectReferenceList list = debugInterface.getCurrentlyRunningThreads();
    
    // DEBUG: remove or comment this
    Debug.println("handleAllThreads:");
    Debug.println(list);
    
    writeObjectIdList(list);
    
    sendReplyPacket(packet);
  }

  private void handleTopLevelThreadGroups(PacketWrapper packet)
  {
    writer.clear();
    
    ObjectReferenceList list = debugInterface.getTopLevelThreadGroups();
    writeObjectIdList(list);
    
    sendReplyPacket(packet);
  }

  private void handleDispose(PacketWrapper packet) throws IOException
  {
    // JDWP log analysis shows that it is necessary to send a reply packet here.
    writer.clear();
    
    // dispose after sending the reply packet. If there is an exception,
    // the debugger will shutdown.
    debugInterface.dispose();
    sendReplyPacket(packet);
  }

  private void handleIDSizes(PacketWrapper packet)
  {
    writer.clear();
    
    writer.writeInt(JDWPConstants.fieldIDSize);
    writer.writeInt(JDWPConstants.methodIDSize);
    writer.writeInt(JDWPConstants.objectIDSize);
    writer.writeInt(JDWPConstants.referenceTypeIDSize);
    writer.writeInt(JDWPConstants.frameIDSize);
    
    sendReplyPacket(packet);
  }

  private void handleSuspend(PacketWrapper packet) throws IOException
  {
    writer.clear();
    
    debugInterface.suspendJavaMachine();
    sendReplyPacket(packet);
  }
  
  private void handleResume(PacketWrapper packet) throws IOException
  {
    writer.clear();
    
    debugInterface.resumeJavaMachine();
    sendReplyPacket(packet);
  }
  
  private void handleExit(PacketWrapper packet) throws IOException
  {
    reader.setPacket(packet);
    int exitCode = reader.readInt();
    
    writer.clear();
    
    debugInterface.exitJavaMachine(exitCode);
    
    sendReplyPacket(packet);
  }

  private void handleCreateString(PacketWrapper packet)
  {
    writer.clear();
    
    reader.setPacket(packet);
    String data = reader.readJDWPString();
    
//    ReferenceTypeList list = symbolManager.getReferenceTypeList(signature);
//    ReferenceType type; 
    
    int id = debugInterface.createString(data);
    writer.writeID(id, JDWPConstants.objectIDSize);
    
    sendReplyPacket(packet);
  }

  private void handleCapabilities(PacketWrapper packet)
  {
    writer.clear();
    
    writer.writeBoolean(Capabilities.canWatchFieldModification);
    writer.writeBoolean(Capabilities.canWatchFieldAccess);
    writer.writeBoolean(Capabilities.canGetBytecodes);
    writer.writeBoolean(Capabilities.canGetSyntheticAttribute);
    writer.writeBoolean(Capabilities.canGetOwnedMonitorInfo);
    writer.writeBoolean(Capabilities.canGetCurrentContendedMonitor);
    writer.writeBoolean(Capabilities.canGetMonitorInfo);
    
    sendReplyPacket(packet);
  }

  private void handleClassPaths(PacketWrapper packet)
  {
    Debug.println("  Don't know how to handle packet type. Ignoring. ");
    packet.printInformation();
  }

  private void handleDisposeObjects(PacketWrapper packet)
  {
//    Debug.println("  Don't know how to handle packet type. Ignoring. ");
//    Util.printInformation(packet);
    //TODO: check if it is necessary to implement this.
    // ignore this request for now
    writer.clear();
    sendReplyPacket(packet);
  }

  private void handleHoldEvents(PacketWrapper packet)
  {
    writer.clear();
    sendReplyPacket(packet);
//    Debug.println("  Don't know how to handle packet type. Ignoring. ");
//    Util.printInformation(packet);
    debugInterface.holdEvents();
  }

  private void handleReleaseEvents(PacketWrapper packet)
  {
    writer.clear();
    sendReplyPacket(packet);
//    Debug.println("  Don't know how to handle packet type. Ignoring. ");
//    Util.printInformation(packet);
    debugInterface.releaseEvents();
  }
  
  private void handleCapabilitiesNew(PacketWrapper packet)
  {
    writer.clear();
    
    // the original capabilities
    writer.writeBoolean(Capabilities.canWatchFieldModification);
    writer.writeBoolean(Capabilities.canWatchFieldAccess);
    writer.writeBoolean(Capabilities.canGetBytecodes);
    writer.writeBoolean(Capabilities.canGetSyntheticAttribute);
    writer.writeBoolean(Capabilities.canGetOwnedMonitorInfo);
    writer.writeBoolean(Capabilities.canGetCurrentContendedMonitor);
    writer.writeBoolean(Capabilities.canGetMonitorInfo);

    // the new capabilities
    writer.writeBoolean(Capabilities.canRedefineClasses);
    writer.writeBoolean(Capabilities.canAddMethod);
    writer.writeBoolean(Capabilities.canUnrestrictedlyRedefineClasses);
    writer.writeBoolean(Capabilities.canPopFrames);
    writer.writeBoolean(Capabilities.canUseInstanceFilters);
    writer.writeBoolean(Capabilities.canGetSourceDebugExtension);
    writer.writeBoolean(Capabilities.canRequestVMDeathEvent);
    writer.writeBoolean(Capabilities.canSetDefaultStratum);
    
    // the reserved flags for future use. Currently all are false, 
    // just like the Java Virtual Machine 1.4.2 set them.
    writer.writeBoolean(Capabilities.reserved16);
    writer.writeBoolean(Capabilities.reserved17);
    writer.writeBoolean(Capabilities.reserved18);
    writer.writeBoolean(Capabilities.reserved19);
    writer.writeBoolean(Capabilities.reserved20);
    
    writer.writeBoolean(Capabilities.reserved21);
    writer.writeBoolean(Capabilities.reserved22);
    writer.writeBoolean(Capabilities.reserved23);
    writer.writeBoolean(Capabilities.reserved24);
    writer.writeBoolean(Capabilities.reserved25);
    
    writer.writeBoolean(Capabilities.reserved26);
    writer.writeBoolean(Capabilities.reserved27);
    writer.writeBoolean(Capabilities.reserved28);
    writer.writeBoolean(Capabilities.reserved29);
    writer.writeBoolean(Capabilities.reserved30);
    
    writer.writeBoolean(Capabilities.reserved31);
    writer.writeBoolean(Capabilities.reserved32);
    
    sendReplyPacket(packet);
  }

  private void handleRedefineClasses(PacketWrapper packet)
  {
    writer.clear();
    sendReplyPacket(packet, ErrorConstants.ERROR_NOT_IMPLEMENTED);
  }

  private void handleSetDefaultStratum(PacketWrapper packet)
  {
    writer.clear();
    sendReplyPacket(packet, ErrorConstants.ERROR_NOT_IMPLEMENTED);
  }
}
