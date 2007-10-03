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
import com.jopdesign.debug.jdwp.util.Debug;
import com.jopdesign.debug.jdwp.util.PacketQueue;
import com.sun.tools.jdi.PacketWrapper;

/**
 * This is the main packet handler for this framework.
 * Its goal is to handle JDWP packets and take decisions
 * about what to do for each new event.
 * 
 * It acts as a dispatcher, who knows what to do 
 * for each situation and call the proper handler
 * based on the command set of the packet.
 * 
 * @author Paulo Abadie Guedes
 */
public class MainPacketHandler extends BasicPacketHandler implements PacketHandler
{
  private PacketQueue outputQueue;
  
  // a list of handlers. Each one will take care of its own type
  // of command set. The commented handlers are not necessary.
  private PacketHandler handlerForVirtualMachineCommandSet;
  
  private PacketHandler handlerForReferenceTypeCommandSet;
  private PacketHandler handlerForClassTypeCommandSet;
  private PacketHandler handlerForArrayTypeCommandSet;
  //private PacketHandler handlerForInterfaceTypeCommandSet;
  private PacketHandler handlerForMethodCommandSet;

  //private PacketHandler handlerForFieldCommandSet;
  private PacketHandler handlerForObjectReferenceCommandSet;
  private PacketHandler handlerForStringReferenceCommandSet;
  private PacketHandler handlerForThreadReferenceCommandSet;
  private PacketHandler handlerForThreadGroupReferenceCommandSet;
  private PacketHandler handlerForArrayReferenceCommandSet;
  private PacketHandler handlerForClassLoaderReferenceCommandSet;
  private PacketHandler handlerForEventRequestCommandSet;
  private PacketHandler handlerForStackFrameCommandSet;
  private PacketHandler handlerForClassObjectReferenceCommandSet;

  private PacketHandler handlerForEventCommandSet;
  
  private JOPDebugInterface debugInterface;
  
  public MainPacketHandler(PacketQueue inputQueue, 
      PacketQueue outputQueue, JOPDebugInterface debugInterface)
  {
    this("", inputQueue, outputQueue, debugInterface);
  }
  
  public MainPacketHandler(String threadName, PacketQueue inputQueue, 
    PacketQueue outputQueue, JOPDebugInterface debugInterface)
  {
    super(threadName, inputQueue);
    this.outputQueue = outputQueue;
    this.debugInterface = debugInterface;
    
    initializeAllInternalHandlers(inputQueue, outputQueue);
  }

  private void initializeAllInternalHandlers(PacketQueue inputQueue, 
    PacketQueue outputQueue)
  {
    PacketHandler descriptionHandler = new DescriptionPacketHandler(inputQueue, outputQueue);

    handlerForVirtualMachineCommandSet = new VirtualMachinePacketHandler(debugInterface, outputQueue);

    handlerForReferenceTypeCommandSet = new ReferenceTypePacketHandler(debugInterface, outputQueue);
    
    handlerForClassTypeCommandSet = new ClassTypePacketHandler(debugInterface, outputQueue);
    handlerForArrayTypeCommandSet = new ArrayTypePacketHandler(debugInterface, outputQueue);

    handlerForMethodCommandSet = new MethodPacketHandler(debugInterface, outputQueue);

    handlerForObjectReferenceCommandSet = new ObjectReferencePacketHandler(debugInterface, outputQueue);
    handlerForStringReferenceCommandSet = new StringReferencePacketHandler(debugInterface, outputQueue);
    handlerForThreadReferenceCommandSet = new ThreadReferencePacketHandler(debugInterface, outputQueue);
    handlerForThreadGroupReferenceCommandSet = new ThreadGroupReferencePacketHandler(debugInterface, outputQueue);
    handlerForArrayReferenceCommandSet = new ArrayReferencePacketHandler(debugInterface, outputQueue);
    handlerForClassLoaderReferenceCommandSet = new ClassLoaderPacketHandler(debugInterface, outputQueue);
    handlerForEventRequestCommandSet = new EventRequestPacketHandler(debugInterface, outputQueue);
    handlerForStackFrameCommandSet =  new StackFramePacketHandler(debugInterface, outputQueue);
    handlerForClassObjectReferenceCommandSet = new ClassObjectReferencePacketHandler(debugInterface, outputQueue);

    handlerForEventCommandSet = descriptionHandler;
  }
  
  public void handlePacket(PacketWrapper packet) throws IOException
  {
    // this test should happen only once. Probably outside of
    // this class would be a better place, but for now
    // this solution is working. Maybe not as fast as possible,
    // but anyway, it works just fine.
    if(packet.isHandshakePacket())
    {
      // hardwired way to handle this packet :(
      outputQueue.add(packet);
    }
    else
    {
      int commandSet = packet.getCmdSet();
      switch(commandSet)
      {
        // TODO: parei aqui
//        case CommandConstants.
        case CommandConstants.VirtualMachine_Command_Set: // 1
        {
          handleVirtualMachineCommandSet(packet); 
          break;
        }

        case CommandConstants.ReferenceType_Command_Set: // 2
        {
          handleReferenceTypeCommandSet(packet);
          break;
        }

        case CommandConstants.ClassType_Command_Set: // 3
        {
          handleClassTypeCommandSet(packet);
          break;
        }

        case CommandConstants.ArrayType_Command_Set: // 4
        {
          handleArrayTypeCommandSet(packet);
          break;
        }

        case CommandConstants.InterfaceType_Command_Set: // 5
        {
          printPacketInformation(packet);
          break;
        }

        case CommandConstants.Method_Command_Set: // 6
        {
          handleMethodCommandSet(packet); 
          break;
        }
        
        // there is no command set with code "7".
        
        case CommandConstants.Field_Command_Set: // 8
        {
          printPacketInformation(packet); 
          break;
        }

        case CommandConstants.ObjectReference_Command_Set: // 9
        {
          handleObjectReferenceCommandSet(packet);
          break;
        }

        case CommandConstants.StringReference_Command_Set: // 10
        {
          handleStringReferenceCommandSet(packet);
          break;
        }

        case CommandConstants.ThreadReference_Command_Set: // 11
        {
          handleThreadReferenceCommandSet(packet); 
          break;
        }

        case CommandConstants.ThreadGroupReference_Command_Set: // 12
        {
          handleThreadGroupReferenceCommandSet(packet);
          break;
        }

        case CommandConstants.ArrayReference_Command_Set: // 13
        {
          handleArrayReferenceCommandSet(packet);
          break;
        }

        case CommandConstants.ClassLoaderReference_Command_Set: // 14
        {
          handleClassLoaderReferenceCommandSet(packet);
          break;
        }

        case CommandConstants.EventRequest_Command_Set: // 15
        {
          handleEventRequestCommandSet(packet);
          break;
        }

        case CommandConstants.StackFrame_Command_Set: // 16
        {
          handleStackFrameCommandSet(packet);
          break;
        }

        case CommandConstants.ClassObjectReference_Command_Set: // 17
        {
          handleClassObjectReferenceCommandSet(packet);
          break;
        }

        case CommandConstants.Event_Command_Set: // 64
        {
          handleEventCommandSet(packet);
          break;
        }
        
        default:
        {
          printPacketInformation(packet);
          break;
        }
      }
    }
  }
  
  private void handleVirtualMachineCommandSet(PacketWrapper packet) throws IOException
  {
    handlerForVirtualMachineCommandSet.handlePacket(packet);
    printPacketInformation(packet);
  }

  private void handleReferenceTypeCommandSet(PacketWrapper packet) throws IOException
  {
    handlerForReferenceTypeCommandSet.handlePacket(packet);
    printPacketInformation(packet);
  }

  private void handleClassTypeCommandSet(PacketWrapper packet) throws IOException
  {
    handlerForClassTypeCommandSet.handlePacket(packet);
    printPacketInformation(packet);
  }

  private void handleArrayTypeCommandSet(PacketWrapper packet) throws IOException
  {
    handlerForArrayTypeCommandSet.handlePacket(packet);
    printPacketInformation(packet);
  }
  
  // don't need to handle InterfaceType command sets. Should never happen. 

  private void handleMethodCommandSet(PacketWrapper packet) throws IOException
  {
    handlerForMethodCommandSet.handlePacket(packet);
    printPacketInformation(packet);
  }

  // don't need to handle Field command sets. Should never happen.

  private void handleObjectReferenceCommandSet(PacketWrapper packet) throws IOException
  {
    handlerForObjectReferenceCommandSet.handlePacket(packet);
    printPacketInformation(packet);
  }

  private void handleStringReferenceCommandSet(PacketWrapper packet) throws IOException
  {
    handlerForStringReferenceCommandSet.handlePacket(packet);
    printPacketInformation(packet);
  }

  private void handleThreadReferenceCommandSet(PacketWrapper packet) throws IOException
  {
    handlerForThreadReferenceCommandSet.handlePacket(packet);
    printPacketInformation(packet);
  }

  private void handleThreadGroupReferenceCommandSet(PacketWrapper packet) throws IOException
  {
    handlerForThreadGroupReferenceCommandSet.handlePacket(packet);
    printPacketInformation(packet);
  }

  private void handleArrayReferenceCommandSet(PacketWrapper packet) throws IOException
  {
    handlerForArrayReferenceCommandSet.handlePacket(packet);
    printPacketInformation(packet);
  }

  private void handleClassLoaderReferenceCommandSet(PacketWrapper packet) throws IOException
  {
    handlerForClassLoaderReferenceCommandSet.handlePacket(packet);
    printPacketInformation(packet);
  }
  
  private void handleEventRequestCommandSet(PacketWrapper packet) throws IOException
  {
    handlerForEventRequestCommandSet.handlePacket(packet);
    printPacketInformation(packet);
  }

  private void handleStackFrameCommandSet(PacketWrapper packet) throws IOException
  {
    handlerForStackFrameCommandSet.handlePacket(packet);
    printPacketInformation(packet);
  }

  private void handleClassObjectReferenceCommandSet(PacketWrapper packet) throws IOException
  {
    handlerForClassObjectReferenceCommandSet.handlePacket(packet);
    printPacketInformation(packet);
  }

  /**
   * This should not happen since this method is intended to work on the
   * debugee side, where this kind of packet should NEVER arrive.
   * However, this method is here since maybe some subclass in the
   * future wants to stay in the middle of the connection to do its work.
   */
  private void handleEventCommandSet(PacketWrapper packet) throws IOException
  {
//    handlerForEventCommandSet.handlePacket(packet);
    printUnknownPacketType(packet);
  }
  
  /**
   * @param packet
   */
  private void printUnknownPacketType(PacketWrapper packet)
  {
    Debug.println("  Don't know how to handle packet type. Ignoring. ");
    printPacketInformation(packet);
  }

  private void printPacketInformation(PacketWrapper packet)
  {
    packet.printInformation();
  }
}
