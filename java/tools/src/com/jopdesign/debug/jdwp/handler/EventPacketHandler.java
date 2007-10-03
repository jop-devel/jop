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

import com.jopdesign.debug.jdwp.JOPDebugInterface;
import com.jopdesign.debug.jdwp.constants.CommandConstants;
import com.jopdesign.debug.jdwp.constants.EventKindConstants;
import com.jopdesign.debug.jdwp.constants.SuspendPolicyConstants;
import com.jopdesign.debug.jdwp.model.Location;
import com.jopdesign.debug.jdwp.util.PacketQueue;
import com.sun.tools.jdi.PacketWrapper;

/**
 * EventPacketHandler.java
 * 
 * A class to handle JDWP commands which belong to the 
 * Eventcommand set.
 * 
 * Compared to all other packet handlers, this class is 
 * very different because it is in charge of sending events
 * from the Java machine to the debugger. It also does not
 * cooperate closely with the MainPacketHandler, as other
 * handlers do.
 * 
 * Almost all other classes basically receive a packet FROM the
 * debugger, handle it and post an answer, 
 * interacting with the machine during the process.
 * 
 * This class is different. It can send events TO the debugger 
 * without a direct request, based on the internal state of the
 * machine and events created as a consequence of previous event
 * requests such as "set breakpoint".
 * 
 * For more information, see the EventRequestPacketHandler class
 * and the "Event Command Set (64)" section in the JDWP 
 * specification.  
 * 
 * @author Paulo Abadie Guedes
 *
 * 09/06/2007 - 10:58:04
 * 
 */
public class EventPacketHandler extends JDWPPacketHandler
{
  private PacketWrapper packet;
  
  /**
   * @param debugInterface
   * @param queue
   */
  public EventPacketHandler(JOPDebugInterface debugInterface, PacketQueue queue)
  {
    super(debugInterface, queue);
    
    packet = new PacketWrapper();
    packet.setCommandSet(CommandConstants.Event_Command_Set);
    packet.setCommand(CommandConstants.Event_Composite);
  }
  
//  public void replyPacket(PacketWrapper packet)
//  {
//    // TODO: finish this method
//  }
//  
  /**
   * Every method which will send an event need to call this method here 
   * as a first action.
   */
  private void startNewPacket(byte suspendPolicy, int numberOfEvents)
  {
    // just to be on the safe side, clear the buffer
    writer.clear();
    
    // suspend policy
    writer.writeByte(suspendPolicy);
    
    // number of events in this set
    writer.writeInt(numberOfEvents);
  }
  
  public void sendVmStartEvent(int threadId) throws JDWPException
  {
    // start a new packet with suspend policy ALL and one event only
    startNewPacket(SuspendPolicyConstants.ALL, 1);
    
    writer.writeByte(EventKindConstants.VM_START);
    
    // event created automatically
    writer.writeInt(0);
    
    // thread ID which created this event.
    writer.writeObjectId(threadId);
    
    sendEventPacket(packet);
  }
  
  public void sendVmDeathEvent(int requestId) throws JDWPException
  {
    // start a new packet with suspend policy ALL and one event only
    startNewPacket(SuspendPolicyConstants.ALL, 1);
    
    writer.writeByte(EventKindConstants.VM_DEATH);
    
    // request ID which created this event.
    writer.writeInt(requestId);
    
    sendEventPacket(packet);
  }
  
  public void sendBreakpointEvent(int requestId, int threadId, 
      Location location) throws JDWPException
  {
    // start a new packet with suspend policy ALL and one event only
    startNewPacket(SuspendPolicyConstants.ALL, 1);
    
    writer.writeByte(EventKindConstants.BREAKPOINT);
    
    // request ID which created this event.
    writer.writeInt(requestId);
    
    // thread ID which created this event.
    writer.writeObjectId(threadId);
    
    // Location at which this event was created.
    writer.writeLocation(location);

    sendEventPacket(packet);
  }
  
  
  
}
