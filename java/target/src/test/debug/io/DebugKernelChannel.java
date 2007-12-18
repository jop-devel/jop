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

package debug.io;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import debug.JopDebugKernel;
import debug.constants.EventKindConstants;
import debug.constants.SuspendPolicyConstants;

/**
 * This class provides a communication channel between the
 * Java processor and the debug server.
 * 
 * Its main role is to take care of packet handling,
 * by isolating the inner details of creating, interpreting
 * and transmiting JDWP packets.
 * 
 * It has two internal buffers, for input and output.
 * The input one is used to receive requests from the server
 * and is needed to handle commands. It's intended to be used
 * by an interrupt handler or a debug thread.
 * 
 * The output buffer is used to build both command answers
 * and events. It is intended to be cleared, filled in and
 * then sent. This class also helps in the packaging.    
 * 
 * DebugKernelChannel.java
 * 
 * @author Paulo Abadie Guedes
 *
 * 04/12/2007 - 16:02:52
 *
 */
public final class DebugKernelChannel
{
  // the streams used for debugging
  private OutputStream outputStream;
  private DataInputStream inputStream;
  
  private JavaDebugPacket inputPacket;
  private JavaDebugPacket outputPacket;
  
  /**
   * Create a debug channel for this Java machine.
   * Using plain Input and Output streams allows it to work
   * (at least in theory) over any reliable communication
   * channel.
   * 
   * @param input
   * @param output
   */
  public DebugKernelChannel(InputStream input, OutputStream output)
  {
    // initialize internal objects
    if((input == null) || (output == null))
    {
      throw new NullPointerException("Debug channel not initialized: Null stream(s)!");
    }
    
    inputStream = new DataInputStream(input);
    outputStream = output;
    
    inputPacket = new JavaDebugPacket();
    outputPacket = new JavaDebugPacket();    
  }
  
  /**
   * Create and send a VMStart event packet, to notify that
   * this Java machine started and can accept debug requests.
   * 
   * @throws IOException
   */
  public synchronized void sendVMStartEvent() throws IOException
  {
    createVMStartPacket();
    sendPacket();
  }
  
  /**
   * Create and send a VMDeath event packet, to notify that
   * this Java machine will shut down.
   * 
   * @throws IOException
   */
  public synchronized void sendVMDeathEvent() throws IOException
  {
    createVMDeathPacket();
    sendPacket();
  }
  
  /**
   * Create and send an event packet, to notify that
   * a breakpoint was reached. 
   * 
   * @param typeTag
   * @param classId
   * @param methodId
   * @param methodLocation
   * @throws IOException
   */
  public synchronized void sendBreakpointEvent(int typeTag, int classId, int methodId,
    int methodLocation) throws IOException
  {
    createBreakpointPacket(typeTag, classId, methodId, methodLocation);
    sendPacket();
  }
  
  /**
   * Create a VMStart event packet.
   */
  private void createVMStartPacket()
  {
    createEventHeader();
    writeEventKindAndRequestId(EventKindConstants.VM_START, 0);
    writeDefaultThreadId();
  }
  
  /**
   * Create a VMDeath event packet.
   */
  private void createVMDeathPacket()
  {
    createEventHeader(1, SuspendPolicyConstants.NONE);
    writeEventKindAndRequestId(EventKindConstants.VM_DEATH, 0);
  }
  
  /**
   * Create a breakpoint event packet.
   * 
   * @param typeTag
   * @param classId
   * @param methodId
   * @param methodLocation
   */
  private void createBreakpointPacket(int typeTag, int classId,
    int methodId, int methodLocation)
  {
    createEventHeader(1, SuspendPolicyConstants.EVENT_THREAD);
    writeEventKindAndRequestId(EventKindConstants.BREAKPOINT, 0);
    writeThreadId(1);
    writeExecutableLocation(typeTag, classId, methodId, methodLocation);
  }
  
  /**
   * Write the default ThreadId (currently 1).
   */
  private void writeDefaultThreadId()
  {
    writeThreadId(1);
  }
  
  /**
   * Write the ThreadId.
   * 
   * @param threadId
   */
  private void writeThreadId(int threadId)
  {
    outputPacket.writeInt(0);
    outputPacket.writeInt(threadId);
  }
  
  /**
   * Write an executable location. The location is identified by:
   * 
   * - one byte type tag 
   * - followed by a a classID 
   * - followed by a methodID 
   * - followed by an unsigned eight-byte index, which identifies 
   *   the location within the method.
   * 
   * @param typeTag
   * @param classId
   * @param methodId
   * @param methodLocation
   */
  public void writeExecutableLocation(int typeTag, int classId,
    int methodId, int methodLocation)
  {
    writeTypeTag(typeTag);
    writeClassId(classId);
    writeMethodId(methodId);
    writeMethodLocation(methodLocation);
  }
  
  /**
   * 
   * @param typeTag
   */
  private void writeTypeTag(int typeTag)
  {
    outputPacket.writeByte(typeTag);
  }
  
  private void writeClassId(int classId)
  {
    outputPacket.writeInt(0);
    outputPacket.writeInt(classId);
  }
  
  private void writeFieldId(int fieldId)
  {
    outputPacket.writeInt(fieldId);
  }
  
  private void writeMethodId(int methodId)
  {
    outputPacket.writeInt(methodId);
  }
  
  private void writeObjectId(int objectId)
  {
	outputPacket.writeInt(0);
    outputPacket.writeInt(objectId);
  }
  
  private void writeFrameId(int frameId)
  {
    outputPacket.writeInt(frameId);
  }
  
  private void writeMethodLocation(int location)
  {
    outputPacket.writeInt(0);
    outputPacket.writeInt(location);
  }
  
  /**
   * Write the EventKind and RequestId fields. Useful to avoid issues 
   * with field sizes.
   * 
   * @param eventKind
   * @param requestId
   */
  private void writeEventKindAndRequestId(int eventKind, int requestId)
  {
    outputPacket.writeByte(eventKind);
    outputPacket.writeInt(requestId);
  }
  
  /**
   * Create an event header with one event.
   */
  private void createEventHeader()
  {
    createEventHeader(1, SuspendPolicyConstants.ALL);
  }
  
  /**
   * Create an event header.
   * 
   * @param events
   */
  private void createEventHeader(int events, int suspendPolicy)
  {
    // discard previous data and create a new event header
    outputPacket.createEventHeader();
    
    // write the suspend policy.
    outputPacket.writeByte(suspendPolicy);
    
    // write the number of events. 
    outputPacket.writeInt(events);
  }
  
  /**
   * Send the output packet to the debug server.
   * 
   * @throws IOException
   */
  private synchronized void sendPacket() throws IOException
  {
    outputPacket.writePacket(outputStream);
    
    JopDebugKernel.debugPrint("Packet sent!   ");
    if(JopDebugKernel.shouldPrintInternalMessages())
    {
      printOutputPacketDescription();
    }
  }
  
  /**
   * This method should be called when there's something to be read 
   * from the external environment.
   * 
   * It should be called from somewhere else which will 
   * handle a communication interrupt related to the 
   * corresponding input stream.
   * 
   * @throws IOException 
   */
  public synchronized void receivePacket() throws IOException
  {
    inputPacket.readPacket(inputStream);
    
    JopDebugKernel.debugPrint("Packet arrived!");
    if(JopDebugKernel.shouldPrintInternalMessages())
    {
      printInputPacketDescription();
    }
  }
  
  /**
   * Create an empty reply packet, based on the content of the last 
   * received packet. It will report a successful result.
   * 
   * @throws IOException
   */
  private synchronized void createEmptyReplyPacket() throws IOException
  {
    createEmptyReplyPacket(0);
  }
  
  /**
  * Create an empty reply packet, based on the content of the last 
  * received packet. It can report an error or a successful result.
  * 
  * @throws IOException
  */
  private synchronized void createEmptyReplyPacket(int errorCode) throws IOException
  {
    outputPacket.createReplyHeader(inputPacket);
    outputPacket.setErrorCode(errorCode);
  }
  
  /**
   * Create a reply packet based on the last received packet.
   * Add the given int as internal data.
   * 
   * @param value
   * @throws IOException
   */
  private synchronized void createIntReplyPacket(int value) throws IOException
  {
    // create a reply packet based on the received packet.
    // the content will be the given int.
    createEmptyReplyPacket();
    outputPacket.writeInt(value);
  }
  
  /**
   * Send a reply packet based on the last received packet.
   * Insert no data. Usually it's used just as an acknowledge,
   * to report that the last request was correctly executed.
   * 
   * @param frameCount
   * @throws IOException
   */
  public synchronized void sendReply() throws IOException
  {
    // create a reply packet based on the received packet.
    createEmptyReplyPacket();
    sendPacket();
  }
  
  /**
   * Send a reply packet based on the last received packet.
   * Insert the frame count in the data.
   * 
   * @param frameCount
   * @throws IOException
   */
  public synchronized void sendReplyFrameCount(int frameCount) throws IOException
  {
    // create a reply packet based on the received packet.
    // the content will be the frame count.
    createIntReplyPacket(frameCount);
    sendPacket();
  }
  
  /**
   * Send a reply packet based on the last received packet.
   * Insert the local variable content in the data.
   * 
   * @param value
   * @throws IOException
   */
  public synchronized void sendReplyGetLocalVariable(int value) throws IOException
  {
    // the content will be the local variable value.
    // currently there's support to provide only one local variable.  
    createIntReplyPacket(1);
    outputPacket.writeInt(value);
    
    sendPacket();
  }
  
  /**
   * Send a reply packet based on the last received packet.
   * Insert the number of local variables.
   * 
   * @param localVariableCount
   * @throws IOException
   */
  public synchronized void sendReplyLocalVariableCount(int localVariableCount) throws IOException
  {
    // create a reply packet based on the received packet.
    // the content will be the number of local variables.
    createIntReplyPacket(localVariableCount);
    sendPacket();
  }
  
  /**
   * Send a reply packet based on the last received packet.
   * Insert the overwritten instruction.
   * 
   * @param instruction
   * @throws IOException
   */
  public synchronized void sendReplySetBreakpoint(int instruction) throws IOException
  {
    // create a reply packet based on the received packet.
    // the content will be the overwritten instruction
    createIntReplyPacket(instruction);
    sendPacket();
  }
  
  public synchronized void sendReplyTestJDWPPackets(int numPackets) throws IOException
  {
    // create a reply packet based on the received packet.
    // the content will be the number of packets that will follow.
    createIntReplyPacket(numPackets);
    sendPacket();
  }
  
  /**
   * Send a reply packet with an error code, based on the last received packet.
   * The error code will be sent in the "error" field, to inform the error to
   * the server.
   * 
   * @param frameCount
   * @throws IOException
   */
  public synchronized void sendReplyWithErrorCode(int errorCode) throws IOException
  {
    // create and send a reply packet based on the last received packet.
    createEmptyReplyPacket(errorCode);
    sendPacket();
  }
  
  /**
   * Provide access to the "command set" field of the input packet.
   * 
   * @return
   * @throws IOException
   */
  public synchronized int readInputCommandSet() throws IOException
  {
    return inputPacket.getCommandSet();
  }
  
  /**
   * Provide access to the "command" field of the input packet.
   * 
   * @return
   * @throws IOException
   */
  public synchronized int readInputCommand() throws IOException
  {
    return inputPacket.getCommand();
  }
  
  /**
   * Read one int value from the input packet.
   * 
   * @return
   */
  public synchronized int readIntValue()
  {
    return inputPacket.readInt();
  }
  
  /**
   * Read one short value from the input packet.
   * 
   * @return
   */
  public synchronized int readShortValue()
  {
    return inputPacket.readShort();
  }
  
  /**
   * Read one byte value from the input packet.
   * 
   * @return
   */
  public synchronized int readByteValue()
  {
    return inputPacket.readByte();
  }
  
  /**
   * Skip some bytes from the input packet.
   * 
   * @param numBytes
   */
  public synchronized void skipBytes(int numBytes)
  {
    inputPacket.skipBytes(numBytes);
  }
  
  /**
   * Skip four bytes from the input packet.
   * 
   * @param numBytes
   */
  public synchronized void skipInt()
  {
    skipBytes(4);
  }
  
  /**
   * Print information about the input packet.
   */
  private void printInputPacketDescription()
  {
    inputPacket.printPacketHeader(System.out);
  }
  
  private void printOutputPacketDescription()
  {
    outputPacket.printPacketHeader(System.out);
  }

//  Unfortunately there's no simple way to do this with one method call
//  (and without using dynamic lists), because the stack size changes
//  during execution.
//  So, the debug kernel uses the three methods below. 
//  It does this for every "getstack frame list" command:
//  
// - call prepareStackFrameListPacket once
// - call many times writeStackFrameInformation
// - call sendStackFrameListReply once
// 
  /**
   * Prepare the reply for the "get stack frame list" command.
   * 
   * @param count
   * @throws IOException 
   */
  public void prepareStackFrameListPacket(int count) throws IOException
  {
	// create a reply packet based on the received packet.
    createEmptyReplyPacket();
    
    // write the number of stack frames.
    outputPacket.writeInt(count);
  }
  
  /**
   * Write information related to one stack frame to the output packet.
   * 
   * @param framePointer
   */
  public synchronized void writeStackFrameId(int framePointer)
  {
	outputPacket.writeInt(framePointer);
  }
  
  /**
   * Send the packet with data about stack frmaes to the server.
   * 
   * @throws IOException 
   */
  public synchronized void sendStackFrameListReply() throws IOException
  {
	sendPacket();
  }
}