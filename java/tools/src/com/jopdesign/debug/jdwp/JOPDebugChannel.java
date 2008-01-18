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

package com.jopdesign.debug.jdwp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.jopdesign.debug.jdwp.constants.CommandConstants;
import com.jopdesign.debug.jdwp.constants.JDWPConstants;
import com.jopdesign.debug.jdwp.constants.NetworkConstants;
import com.jopdesign.debug.jdwp.constants.TagConstants;
import com.jopdesign.debug.jdwp.io.HexadecimalInputStream;
import com.jopdesign.debug.jdwp.io.PacketReader;
import com.jopdesign.debug.jdwp.io.PacketWriter;
import com.jopdesign.debug.jdwp.model.Frame;
import com.jopdesign.debug.jdwp.model.FrameList;
import com.jopdesign.debug.jdwp.model.Location;
import com.jopdesign.debug.jdwp.util.Debug;
import com.sun.tools.jdi.PacketWrapper;
import com.sun.tools.jdi.SocketConnectionWrapper;

/**
 * JOPDebugChannel.java
 * 
 * One class to request basic services to a JOP machine through the network.
 * 
 * All requests are synchronous. They return only when the requested action
 * was performed (or an error is returned).
 * 
 * This class need a JopServer already running on the network,
 * to request services.  
 * 
 * @author Paulo Abadie Guedes
 *
 * 16/05/2007 - 19:30:39
 *
 */
public class JOPDebugChannel
{
  private static final int JOP_DEBUG_COMMAND_SET = 100;
  private static final int JOP_DEBUG_COMMAND_TEST_JDWP = 1;
  private static final int JOP_DEBUG_COMMAND_TEST_JDWP_SENT = 2;
  
  // a static variable to create packet ID's. 
  private static int packetCounter = 1;
  
  private DataInputStream input;
  private DataOutputStream output;
  private Socket socket;
  
  private boolean connected = false;
  
  // two objects to help during packet creation and reading.
  private PacketWriter writer;
  private PacketReader reader;
  
  private PacketWrapper lastPacket;
  
  public JOPDebugChannel()
  {
    writer = new PacketWriter();
    reader = new PacketReader();
  }
  
  /**
   * Connect this channel to the debug client (inside JOP),
   * which will have its own communication channel
   * on the other side.
   * 
   * @throws IOException
   */
  public void connect() throws IOException
  {
    connect(NetworkConstants.DEFAULT_HOST,
      NetworkConstants.DEFAULT_JOP_SERVER_PORT_NUMBER);
  }
  
  /**
   * Connect this channel to the debug client (inside JOP),
   * which will have its own communication channel
   * on the other side.
   * 
   * @param host
   * @param port
   * @throws IOException
   */
  public void connect(String host, int port) throws IOException
  {
    if(isConnected())
    {
      close();
    }
    
    socket = new Socket(host, port);
    
//    System.out.println(" Connected!");
//    
    OutputStream outputStream = socket.getOutputStream();
    output = new DataOutputStream(outputStream);
    
    InputStream inputStream = socket.getInputStream();
    HexadecimalInputStream hexInputStream = new HexadecimalInputStream(inputStream); 
    input = new DataInputStream(hexInputStream);
    
    handshake(inputStream, outputStream);
    
    connected = true;
  }
  
  private void handshake(InputStream input, OutputStream output) throws IOException
  {
    // a quick and simple handhshake 
    byte[] handshakeData = new byte[JDWPConstants.JDWP_HANDSHAKE_BYTES.length];
    input.read(handshakeData);
    output.write(JDWPConstants.JDWP_HANDSHAKE_BYTES);
  }
  
  /**
   * Close the communication channel.
   * 
   * @throws IOException
   */
  public void close() throws IOException
  {
    checkConnection();
    
    try
    {
      input.close();
      output.close();
      socket.close();
    }
    finally
    {
      // set the flag even if there is an exception
      connected = false;
      
      input = null;
      output = null;
      socket = null;
    }
  }
  
  /**
   * Test the internal flag to check if it has connected.
   * 
   * This method CANNOT BE USED to test if the server closed
   * the connection. It should be used only to test if 
   * this object started a connection some time ago.
   * Just this. 
   * 
   * @return
   */
  private boolean isConnected()
  {
    return connected;
    
    // the code below can't be used to test if the server closed
    // the connection. It seems that the only way to find out
    // this is to try to write to the socket and look for 
    // an exception.
//    return socket != null && socket.isConnected() && 
//    (socket.isClosed() == false);
  }
  
  /**
   * Check if this object is connected. If it's not, throw an exception.
   * 
   * @throws IOException
   */
  private void checkConnection() throws IOException
  {
    if (isConnected() == false)
    {
      throw new IOException("Not connected!");
    }
  }
  
  /**
   * Send the "exit" command. 
   * Request JOP to stop execution by calling "System.exit();".
   * 
   * @param exitCode the exit code
   * @throws IOException thrown if the connection is not open.
   */
  public void sendExitCommand(int exitCode) throws IOException
  {
    checkConnection();
    //------------------------------------------------------------
    // send "exit" command
    PacketWrapper packet = createRequestExit(exitCode);
    sendPacket(packet);
    
    // receive an answer, just to sync
    packet = receivePacket();
    
    // TODO: later handle properly the VMDeath event.
    // for now, just consume it.
    packet = receivePacket();
  }
  
  /**
   * Request the Java machine to suspend execution and 
   * start listening for JDWP requests.
   * 
   * This method should be called before any other JDWP
   * request can be sent to the Java machine.
   * 
   * @throws IOException
   */
  public void suspendJavaMachine() throws IOException
  {
    checkConnection();
    
    // TODO: check the internal machine state before sending this command.
    //------------------------------------------------------------
    // send "suspend" command
    output.writeByte(1);
    output.writeByte(8);
    
    input.readInt();    
  }
  
  /**
   * Send a request to stop listening for JDWP requests and 
   * resume execution. It's a "resume JVM" command.
   * 
   * This command asks JOP to continue running, until a new
   * "suspend" command is sent, a breakpoint is hit or 
   * execution is done: whatever happens first.
   *  
   * @throws IOException
   */
  public void resumeJavaMachine() throws IOException
  {
    checkConnection();
    //------------------------------------------------------------
    // send "resume virtual machine" command
    PacketWrapper packet = createRequestResumeVirtualMachine();
    sendPacket(packet);
    
    // receive an answer, just to sync
    packet = receivePacket();
  }
  
  /**
   * Send a request to resume execution of one specific Thread.  
   * 
   * This command asks JOP to allow one specific Thread to be
   * scheduled again. This has the effect to allow it 
   * to continue, until a breakpoint it hit, execution
   * finishes or JOP is suspended, whatever happens first.
   * 
   * @throws IOException
   */
  public void sendResumeCommand() throws IOException
  {
    checkConnection();
    //------------------------------------------------------------
    // send "resume thread" command
    PacketWrapper packet = createRequestResumeThread();
    sendPacket(packet);
    
    // receive an answer, just to sync
    packet = receivePacket();
  }
  
  /**
   * Send a "suspend" request toward one specific Thread.
   * If accepted, the Thread will not be scheduled until
   * a new "ResumeCommand" is sent, allowing it to continue.
   * 
   * @throws IOException
   */
  public void suspendThread() throws IOException
  {
    checkConnection();
    //------------------------------------------------------------
    // send "suspend" command
    output.writeByte(11);
    output.writeByte(2);
    
    input.readInt();    
  }
  
  /**
   * Query the current list of stack frames for a given Thread.
   * 
   * @return
   * @throws IOException
   */
  public FrameList getStackFrameList() throws IOException
  {
    int threadId;
    int startFrame;
    int length;
    
    checkConnection();
    //------------------------------------------------------------
    // request stack information
    
    // the default thread ID
    threadId = 1;
    
    // get the index of the top frame
    startFrame = getStackDepth() - 1;
    
    // standard value to request all remaining frames
    length = -1;
    
    PacketWrapper packet;
    packet = createRequestStackFrameList(threadId, startFrame, length);
    sendPacket(packet);
    
    // receive an answer with information about the stack frames
    packet = receivePacket();
    reader.setPacket(packet);
    
    FrameList list = new FrameList();
    if(packet.hasNoError() == false)
    {
      // the answer has an error.
      // TODO: handle the error packet here
      Debug.print("Failure: ");
      Debug.println(packet.getErrorCode());
    }
    else
    {
      // the answer has no error. So, fill the frame list.
      
      // read the number of frames
      int numFrames;
      
      numFrames = reader.readInt();
      
      int frameId;
      
      int classId;
      int methodId;
      int methodIndex;
      
      int index;
      for(index = 0; index < numFrames; index++)
      {
    	Location location = new Location();
    	Frame frame = new Frame(location);
    	
    	frameId = reader.readInt();
    	frame.setFrameId(frameId);
    	
    	// get the tag
    	byte tag = reader.readByte();
    	location.setTag(tag);
    	
    	// read the class ID, method ID and the method index.
    	classId = reader.readInt();
    	methodId = reader.readInt();
    	
    	reader.skip(4);
    	methodIndex = reader.readInt();
    	
    	// set the values...
    	location.setClassId(classId);
    	location.setMethodId(methodId);
    	location.setIndex(methodIndex);
    	
    	// for development
    	Debug.println(frame);
    	
    	list.add(frame);
      }
    }
    
    return list;
  }
  
  /**
   * Query the current stack depth for a given Thread.
   * 
   * @return
   * @throws IOException
   */
  public int getStackDepth() throws IOException
  {
    checkConnection();
    
    int received;
    PacketWrapper packet;
    //------------------------------------------------------------
    // query the stack depth
    packet = createRequestThreadReferenceFrameCount();
    sendPacket(packet);
    
    // receive an answer with the variable value
    packet = receivePacket();
    
    reader.setPacket(packet);
    received = reader.readInt();

    Debug.println("  Stack depth: " + received);
    
    return received;
  }
  
  /**
   * Print information about all the stack frames currently 
   * on the call stack.
   * 
   * @throws IOException
   */
  public void printStackFrames() throws IOException
  {
    checkConnection();
    
    //------------------------------------------------------------
    // request to print all stack frames
    PacketWrapper packet;
    packet = createRequestPrintStackFrames(1);
    sendPacket(packet);
    
    // receive an answer
    packet = receivePacket();
  }
  
  /**
   * Print information about one specific stack frame.
   * The frame for the "main" method has index 0, the next
   * one has index 1 and so on. 
   * 
   * @param frameIndex
   * @return
   * @throws IOException
   */
  public void printStackFrame(int frameIndex) throws IOException
  {
    checkConnection();
    
    //------------------------------------------------------------
    // request to print one stack frame
    PacketWrapper packet;
    packet = createRequestPrintStackFrame(1, frameIndex);
    sendPacket(packet);
    
    // receive an answer
    packet = receivePacket();
  }
  
  /**
   * Query JOP for information on the value of one local variable.
   * All frame and variable indexes start with zero. This can return
   * information about the "this" value, local variables and 
   * parameters.
   * 
   * @param frameIndex
   * @param variableIndex
   * @return
   * @throws IOException
   */
  public int getLocalVariableValue(int frameIndex, int variableIndex) throws IOException
  {
    int received;
    PacketWrapper packet;
    
    checkConnection();
    //------------------------------------------------------------
    // send "stack frame -> get values" command.
    //
    // All indexes are zero based. So, frameIndex = 0 and variableIndex = 1 will
    // query the second local variable (index 1) on the first frame (index 0)
    
    packet = createRequestStackFrameGetValues(1, frameIndex, variableIndex);
    sendPacket(packet);
    
    // receive an answer with the variable value
    packet = receivePacket();
    
    reader.setPacket(packet);
    reader.skip(4);
    received = reader.readInt();
    
    Debug.println("  Variable value: " + received);
    
    return received;
  }
  
  /**
   * This method query JOP for information about one instance 
   * variable of a specific object. Variable index start in zero.
   * 
   * @param objectReference
   * @param variableIndex
   * @return
   * @throws IOException
   */
  public int getInstanceVariableValue(int objectReference, int variableIndex)
    throws IOException
  {
    int numFields = 0;
	int received = 0;
    PacketWrapper packet;
    
    checkConnection();
    //------------------------------------------------------------
    // send "Object reference -> get values (2)" command.
    //
    // All indexes are zero based. So, variableIndex = 1 will
    // query the second instance variable (index 1) on the object pointed
    // by objectReference.
    
    packet = createRequestObjectReferenceGetValues(objectReference, variableIndex);
    sendPacket(packet);
    
    // receive an answer with the variable value
    packet = receivePacket();
    
    reader.setPacket(packet);
    
    // check if there was an error.
    if(packet.hasNoError() == false)
    {
      // the answer has an error.
      // TODO: handle the error packet here
      Debug.print("Failure: ");
      Debug.println(packet.getErrorCode());
    }
    else
    {
      // the number of fields should be 1, since only one was requested.
      numFields = reader.readInt();
      if(numFields != 1)
      {
    	Debug.println("  Failure! number of fields should be 1");
      }
      else
      {
    	received = reader.readInt();
    	Debug.println("  Variable value: " + received);
      }
    }
    
    return received;
  }
  
  /**
   * This method request JOP to set one instance 
   * variable of a specific object. Variable index start in zero.
   * 
   * @param objectReference the object handle
   * @param variableIndex the variable index
   * @param variableValue the new variable value
   * @return the error code (zero when the operation was successfully)
   * @throws IOException
   */
  public int setInstanceVariableValue(int objectReference, int variableIndex,
	  int variableValue) throws IOException
  {
    PacketWrapper packet;
    
    checkConnection();
    //------------------------------------------------------------
    // send "Object reference -> set values (3)" command.
    //
    // All indexes are zero based. So, variableIndex = 1 will
    // reference the second instance variable (index 1) on the object pointed
    // by objectReference.
    
    packet = createRequestObjectReferenceSetValues(objectReference, 
    	variableIndex, variableValue);
    sendPacket(packet);
    
    // receive an answer with the variable value
    packet = receivePacket();
    
    // check if there was an error.
    if(packet.hasNoError() == false)
    {
      // the answer has an error.
      // TODO: handle the error packet here
      Debug.print("Failure: ");
      Debug.println(packet.getErrorCode());
    }
    
    return packet.getErrorCode();
  }
  
  /**
   * Read a chunk of memory and return it.
   * size must be a multiple of 4 (each word is 4 bytes).
   * if it's not, it will be increased to the next multiple of four.
   * 
   * @param address
   * @param size
   * @return
   * @throws IOException
   */
  public byte[] readMemory(int address, int size) throws IOException
  {
    int numWords = size / 4;
    int remainder = size % 4;
    
    // if it's not a multiple of 4, add 1 to complete the last word.
    if(remainder > 0)
    {
      numWords++;
    }
    // the number of bytes to be read. Now we know it's a multiple of 4.
    size = numWords * 4;
    
    byte[] data = new byte[size];
    int read;
    
    // request the machine to read memory
    output.writeByte(2);
    output.writeByte(6);
    
    output.writeInt(address);
    output.writeInt(numWords);
    
    read = 0;
    while(read < size)
    {
      read = input.read(data, read, size - read);
    }
    
    return data;
  }
  
  /**
   * Ask JOP to set the value of one local variable.
   * All frame and variable indexes start with zero. This can set
   * the "this" value, local variables and parameters.
   * 
   * @param frameIndex
   * @param variableIndex
   * @param value
   * @throws IOException
   */
  public void setLocalVariableValue(int frameIndex, int variableIndex,
      int value) throws IOException
  {
    checkConnection();
    //------------------------------------------------------------
    // send "stack frame -> set values" command.
    //
    // All indexes are zero based. So, frameIndex = 0 and variableIndex = 1 will
    // query the second local variable (index 1) on the first frame (index 0)
    
    PacketWrapper packet;
    packet = createRequestStackFrameSetValues(1, frameIndex, variableIndex, value);
    sendPacket(packet);
    
    // receive an answer with the variable value
    packet = receivePacket();
    
    if(packet.hasNoError())
    {
      System.out.println("  Variable value set.");
    }
    else
    {
      System.out.println("  Error during variable setting.");
    }
  }
  
  /**
   * Get the number of local variables in a given stack frame,
   * as reported by JOP (not using the symbol file).
   * 
   * @param frameIndex
   * @return
   * @throws IOException
   */
  public int getNumberOfLocalVariables(int frameIndex) throws IOException
  {
    checkConnection();
    
    int received;
    //------------------------------------------------------------
    // query the number of local variables from the given stack frame
    PacketWrapper packet;
    
    packet = createRequestNumberOfLocalVariables(frameIndex);
    sendPacket(packet);
    
    // receive an answer with the data
    packet = receivePacket();
    reader.setPacket(packet);
    received = reader.readInt();
    
    Debug.println("  Number of local variables: " + received);
    
    return received;
  }
  
  /**
   * Request the machine to invoke a static method.
   * 
   * The static method is supposed to receive one parameter, which is
   * one word wide (like an integer or object reference).
   * 
   * This call also works fine with methods that does not receive 
   * parameters as well.
   * 
   * This call blocks and wait until the called method has finished before
   * returning.
   * 
   *  Warning: if the invoked method does not return, this call will
   *  hang here, since it will wait an answer from the machine.
   *  This in turn may stop the entire program. So, make sure the invoked
   *  method always return.
   * 
   * @param methodStructPointer
   * @param parameter
   * @return
   * @throws IOException
   */
  public void invokeStaticMethod(int methodStructPointer, int parameter)
      throws IOException
  {
    checkConnection();
    
    // ------------------------------------------------------------
    // request machine to invoke one static method with one parameter.
    // wait until it return.
    //
    // some methods for testing (search for the address inside the .jop file):
    //   9218: debug.TestJopDebugKernel.printValue(I)V
    //   9222: debug.TestJopDebugKernel.printLine()V
    
    PacketWrapper packet;
    
    packet = createRequestInvokeStatic(methodStructPointer, parameter);
    sendPacket(packet);
    
    // receive an answer, just to sync
    packet = receivePacket();
  }
  
  /**
   * Convenience method to invoke a static method which receive
   * no parameter.
   * 
   * @param methodStructPointer
   * @throws IOException
   */
  public void invokeStaticMethod(int methodStructPointer)
  throws IOException
  {
    invokeStaticMethod(methodStructPointer, 0);
  }

  /**
   * @param frameId
   * @return
   * @throws IOException 
   */
  public int getMethodPointer(int frameId) throws IOException
  {
    checkConnection();
    
    int methodPointer;
    // ------------------------------------------------------------
    // request machine to return the method pointer in the given
    // stack frame.
    output.writeByte(16);
    output.writeByte(0);
    
    // method ID
    output.writeInt(frameId);
    
    // receive back the argument just to sync
    methodPointer = input.readInt();
    Debug.println("  Method pointer: " + methodPointer);

    return methodPointer ;
  }
  
  /**
   * Set a breakpoint at the given instruction offset inside a method.
   * 
   * @param methodStructPointer
   * @param instructionOffset
   * @return
   * @throws IOException
   */
  public int setBreakPoint(int methodStructPointer, int instructionOffset)
    throws IOException
  {
    checkConnection();
    
    int instruction;
    // ------------------------------------------------------------
    // request machine to set a breakpoint and return the old instruction
    PacketWrapper packet;
    packet = createRequestSetBreakpoint(methodStructPointer, instructionOffset);
    sendPacket(packet);
    
    // receive the old instruction. If the answer is -1,
    // then an error happened (such as an invalid address).
    packet = receivePacket();
    reader.setPacket(packet);
    
    if(packet.hasNoError())
    {
      instruction = reader.readInt();
      Debug.println("  Old instruction: " + instruction);
    }
    else
    {
      Debug.println("  Failure! error code: " + packet.getErrorCode());
      instruction = -1;
    }
    
    // TODO: ensure the instruction is stored, to be restored later.
    return instruction;
  }
  
  /**
   * Clear a breakpoint at the given instruction offset inside a method.
   * 
   * @param methodStructPointer
   * @param instructionOffset
   * @param oldInstruction
   * @return
   * @throws IOException
   */
  public int clearBreakPoint(int methodStructPointer, int instructionOffset,
    int oldInstruction)
    throws IOException
  {
    checkConnection();
    
    int instruction;
    // ------------------------------------------------------------
    // request machine to set a breakpoint and return the old instruction
    PacketWrapper packet;
    packet = createRequestClearBreakpoint(methodStructPointer, 
      instructionOffset, oldInstruction);
    sendPacket(packet);
    
    // receive an answer
    packet = receivePacket();
    reader.setPacket(packet);
    
    if(packet.hasNoError())
    {
      instruction = 0;
      Debug.println("  Old instruction: " + instruction);
    }
    else
    {
      Debug.println("  Failure! error code: " + packet.getErrorCode());
      instruction = -1;
    }
    
    return instruction;
  }
  
  /**
   * Method for development ONLY.
   * Request JOP to send a sample of all possible JDWP packets.
   * @throws IOException 
   */
  public void testJDWPPackets() throws IOException
  {
    // Vendor-defined commands and extensions start at 128.
    checkConnection();
    
    int i, numOfPackets;
    PacketWrapper packet;
    
    // ------------------------------------------------------------
    // request machine to set a breakpoint and return the old instruction
    packet = createRequestTestJDWP();
    sendPacket(packet);
    
    packet = receivePacket();
    reader.setPacket(packet);
    
    // read the number of packets
    numOfPackets = reader.readInt();
    
    System.out.println("Packets to receive: " + numOfPackets);
    
    // receive all packets and print information about them
    for(i = 0; i < numOfPackets; i++)
    {
      packet = receivePacket();
      
      System.out.println("New packet received:");
      packet.printInformation();
    }
    
    Debug.println();
    Debug.println("Done!");
    Debug.println();
  }
  
  /**
   * Method for development ONLY.
   * Request JOP to send a sample of all possible JDWP packets.
   * @throws IOException 
   */
  public void testJDWPPacketsSent() throws IOException
  {
    // Vendor-defined commands and extensions start at 128.
    checkConnection();
    
    int error;
    PacketWrapper packet, reply;
    
    // ------------------------------------------------------------
    // request machine to set a breakpoint and return the old instruction
    packet = createRequestTestJDWPSent();
    sendPacket(packet);
    
    // send a packet to read the number of stack frames
    packet = createRequestThreadReferenceFrameCount();
    sendPacket(packet);
    
    // receive another packet with the answer 
    reply = receivePacket();
    reader.setPacket(reply);
    
    error = reply.getErrorCode();
    if(error != 0)
    {
      Debug.println("Error: " + error);
    }
    else
    {
      int size = reader.readInt();
      Debug.println("Size: " + size);
    }
    
    Debug.println("Content: ");
    reply.printInformation();
  }
  
  private void sendPacket(PacketWrapper packet) throws IOException
  {
    printPacket("Will send a packet:", packet);
    SocketConnectionWrapper.sendPacket(packet, output);
  }
  
  private PacketWrapper receivePacket() throws IOException
  {
    PacketWrapper packet;
    
    packet = SocketConnectionWrapper.receivePacket(input);
    printPacket("Received a packet:", packet);
    
    // store the last packet to handle error status later
    lastPacket = packet;
    return packet;
  }
  
  /**
   * Print information about an incoming or outgoing packet.
   * Just for debugging.
   * 
   * @param packet
   * @param commment
   */
  private static void printPacket(String comment, PacketWrapper packet)
  {
    System.out.println("--------------------------------------------------");
    System.out.println(comment);
    packet.printInformation();
    System.out.println("--------------------------------------------------");
  }
  
  private static int getNextId()
  {
    return packetCounter++;
  }
  
  private PacketWrapper createRequestThreadReferenceFrameCount()
  {
    return createRequestThreadReferenceFrameCount(1);
  }
  
  /**
   * Build a request packet, based on the content previously stored
   * on the packet writer.
   * 
   * @param commandSet
   * @param command
   * @return
   */
  private PacketWrapper buildRequestPacket(int commandSet, int command)
  {
    PacketWrapper packet;
    
    packet = writer.createPacket(commandSet, command, getNextId());

    return packet;
  }
  
  /**
   * Clear the packet writer content and build a new request packet,
   * with only one int inside.
   * 
   * @param commandSet
   * @param command
   * @param value
   * @return
   */
  private PacketWrapper createRequestPacket(int commandSet, int command,
    int value)
  {
    PacketWrapper packet;
    
    writer.clear();
    writer.writeInt(value);
    
    packet = buildRequestPacket(commandSet, command);
    
    return packet;
  }
  
  private PacketWrapper createRequestThreadReferenceFrameCount(int threadId)
  {
    PacketWrapper packet;
    
    packet = createRequestPacket(CommandConstants.ThreadReference_Command_Set,
      CommandConstants.ThreadReference_FrameCount, threadId);
    
    return packet;
  }
  
  private PacketWrapper createRequestExit(int exitCode)
  {
    PacketWrapper packet;
    
    writer.clear();
    writer.writeInt(exitCode);
    
    packet = buildRequestPacket(CommandConstants.VirtualMachine_Command_Set,
      CommandConstants.VirtualMachine_Exit);
    
    return packet;
  }
  
  private PacketWrapper createRequestInvokeStatic(int methodPointer, int value)
  {
    PacketWrapper packet;
    
    writer.clear();
    writer.writeInt(methodPointer);
    writer.writeInt(value);
    
    packet = buildRequestPacket(CommandConstants.ClassType_Command_Set,
      CommandConstants.ClassType_InvokeMethod);
    
    return packet;
  }
  
  private PacketWrapper createRequestResumeVirtualMachine()
  {
    PacketWrapper packet;
    
    writer.clear();
    
    packet = buildRequestPacket(CommandConstants.VirtualMachine_Command_Set,
      CommandConstants.VirtualMachine_Resume);
    
    return packet;
  }
  
  private PacketWrapper createRequestResumeThread()
  {
    PacketWrapper packet;
    
    writer.clear();
    
    packet = buildRequestPacket(CommandConstants.ThreadReference_Command_Set,
      CommandConstants.ThreadReference_Resume);
    
    return packet;
  }
  
  private PacketWrapper createRequestNumberOfLocalVariables(int frameIndex)
  {
    PacketWrapper packet;
    
    writer.clear();
    writer.writeInt(frameIndex);
    
    packet = buildRequestPacket(CommandConstants.StackFrame_Command_Set, 5);
      // TODO: create a class to hold non-standard constants.
      //For instance: StackFrame_GetNumberOfValues = 5
    
    return packet;
  }
  
  /**
   * Build a request for one local variable inside one stack frame. 
   * 
   * @param threadId
   * @param frameIndex
   * @param fieldIndex
   * @return
   */
  private PacketWrapper createRequestStackFrameGetValues(int threadId, 
    int frameIndex, int fieldIndex)
  {
    PacketWrapper packet;
    
    writer.clear();
    writer.writeInt(threadId);
    writer.writeInt(frameIndex);
    writer.writeInt(1);
    
    writer.writeInt(fieldIndex);
    writer.writeByte(TagConstants.INT);
    
    packet = buildRequestPacket(CommandConstants.StackFrame_Command_Set,
      CommandConstants.StackFrame_GetValues);
    
    return packet;
  }
  
  /**
   * Build a request for one instance variable inside one object.
   * 
   * @param objectId
   * @param fieldIndex
   * @return
   */
  private PacketWrapper createRequestObjectReferenceGetValues(int objectId, 
    int fieldIndex)
  {
    PacketWrapper packet;
    
    writer.clear();
    writer.writeInt(objectId);
    writer.writeInt(1);
    writer.writeInt(fieldIndex);
    
    packet = buildRequestPacket(CommandConstants.ObjectReference_Command_Set,
      CommandConstants.ObjectReference_GetValues);
    
    return packet;
  }
  
  /**
   * Build a request to set one instance variable inside one object.
   * 
   * @param objectId
   * @param fieldIndex
   * @param fieldValue
   * @return
   */
  private PacketWrapper createRequestObjectReferenceSetValues(int objectId, 
    int fieldIndex, int fieldValue)
  {
    PacketWrapper packet;
    
    writer.clear();
    writer.writeInt(objectId);
    writer.writeInt(1);
    writer.writeInt(fieldIndex);
    writer.writeInt(fieldValue);
    
    packet = buildRequestPacket(CommandConstants.ObjectReference_Command_Set,
      CommandConstants.ObjectReference_SetValues);
    
    return packet;
  }
  
  /**
   * Build a request to set one local variable inside one stack frame. 
   * 
   * @param threadId
   * @param frameIndex
   * @param fieldIndex
   * @param value
   * @return
   */
  private PacketWrapper createRequestStackFrameSetValues(int threadId, 
    int frameIndex, int fieldIndex, int value)
  {
    PacketWrapper packet;
    
    writer.clear();
    writer.writeInt(threadId);
    writer.writeInt(frameIndex);
    writer.writeInt(1);
    
    writer.writeInt(fieldIndex);
    
    // write the slot ID. Currently, just send "int".
    writer.writeByte(TagConstants.INT);
    writer.writeInt(value);
    
    packet = buildRequestPacket(CommandConstants.StackFrame_Command_Set,
      CommandConstants.StackFrame_SetValues);
    
    return packet;
  }
  
  /**
   * Build a request to print information about the call stack. 
   * 
   * @param threadId
   * @return
   */
  private PacketWrapper createRequestPrintStackFrames(int threadId)
  {
    PacketWrapper packet;
    
    writer.clear();
    writer.writeInt(threadId);
    
    // TODO: extract non-standard constant!
    packet = buildRequestPacket(CommandConstants.ThreadReference_Command_Set,
      13);
    
    return packet;
  }
  
  /**
   * Build a request to print information about one stack frame
   * on the call stack. 
   * 
   * @param threadId
   * @return
   */
  private PacketWrapper createRequestPrintStackFrame(int threadId, int frameIndex)
  {
    PacketWrapper packet;
    
    writer.clear();
    writer.writeInt(threadId);
    writer.writeInt(frameIndex);
    
    // TODO: extract non-standard constant!
    packet = buildRequestPacket(CommandConstants.ThreadReference_Command_Set,
      14);
    
    return packet;
  }
  
  /**
   * Build a request to set a breakpoint.
   * This is NOT a standard JDWP packet (different format), 
   * but is good enough for now, since only breakpoint
   * events can be set/cleared in JOP.
   * 
   * A few other events may be generated without requests. 
   * 
   * @param methodStructPointer
   * @param offset
   * @return
   */
  private PacketWrapper createRequestSetBreakpoint(int methodStructPointer, 
    int instructionOffset)
  {
    PacketWrapper packet;
    
    writer.clear();
    writer.writeInt(methodStructPointer);
    writer.writeInt(instructionOffset);
    
    packet = buildRequestPacket(CommandConstants.EventRequest_Command_Set,
      CommandConstants.EventRequest_Set);
    
    return packet;
  }
  
  /**
   * Build a request to clear a breakpoint.
   * This is NOT a standard JDWP packet (different format), 
   * but is good enough for now, since only breakpoint
   * events can be set/cleared in JOP.
   * 
   * A few other events may be generated without requests. 
   * 
   * @param methodStructPointer
   * @param offset
   * @return
   */
  private PacketWrapper createRequestClearBreakpoint(int methodStructPointer, 
    int instructionOffset, int oldInstruction)
  {
    PacketWrapper packet;
    
    writer.clear();
    writer.writeInt(methodStructPointer);
    writer.writeInt(instructionOffset);
    writer.writeInt(oldInstruction);
    
    packet = buildRequestPacket(CommandConstants.EventRequest_Command_Set,
      CommandConstants.EventRequest_Clear);
    
    return packet;
  }
  
  /**
   * Create a test packet, just to help during development.
   * 
   * @return
   */
  private PacketWrapper createRequestTestJDWP()
  {
    PacketWrapper packet;
    
    writer.clear();
    packet = buildRequestPacket(JOP_DEBUG_COMMAND_SET, 
      JOP_DEBUG_COMMAND_TEST_JDWP);
    
    return packet;
  }
  
  /**
   * Create a test packet, just to help during development.
   * 
   * @return
   */
  private PacketWrapper createRequestTestJDWPSent()
  {
    PacketWrapper packet;
    
    writer.clear();
    packet = buildRequestPacket(JOP_DEBUG_COMMAND_SET, 
      JOP_DEBUG_COMMAND_TEST_JDWP_SENT);
    
    return packet;
  }
  
  /**
   * Create a request for the list of stack frames.
   * 
   * @return
   */
  private PacketWrapper createRequestStackFrameList(int threadId, int startFrame,
	int length)
  {
    PacketWrapper packet;
    
    writer.clear();
    // request all frames at once
//    writer.writeInt(-1);
    
    writer.writeInt(threadId);
    writer.writeInt(startFrame);
    writer.writeInt(length);
    
    packet = buildRequestPacket(CommandConstants.ThreadReference_Command_Set, 
      CommandConstants.ThreadReference_Frames);
    
    return packet;
  }
  
  /**
   * Check if the last request caused an error.
   * 
   * @return
   */
  public boolean lastPacketHasNoError()
  {
	boolean result = true;
	
	if(lastPacket != null)
	{
	  result = lastPacket.hasNoError();
	}
	
	return result;
  }
}
