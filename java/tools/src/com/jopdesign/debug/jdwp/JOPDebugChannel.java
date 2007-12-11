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

import com.jopdesign.debug.jdwp.constants.JDWPConstants;
import com.jopdesign.debug.jdwp.constants.NetworkConstants;
import com.jopdesign.debug.jdwp.io.HexadecimalInputStream;
import com.jopdesign.debug.jdwp.model.Frame;
import com.jopdesign.debug.jdwp.model.FrameList;
import com.jopdesign.debug.jdwp.model.Location;

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
  private DataInputStream input;
  private DataOutputStream output;
  private Socket socket;
  
  private boolean connected = false;
  
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
    output.writeByte(1);
    output.writeByte(10);
    
    // does not make much sense here, but...
//    output.writeByte(exitCode);
    
    input.readInt();
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
    // send "resume" command
    output.writeByte(1);
    output.writeByte(9);
    
    input.readInt();    
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
    // send "resume" command
    output.writeByte(11);
    output.writeByte(3);
    
    input.readInt();
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
    int framePointer;
    
    //------------------------------------------------------------
    // request stack information
    output.writeByte(11);
    output.writeByte(6);
    
    // request all frames:
    output.writeInt(-1);
    
    // read the number of frames
    int numFrames = input.readInt();
    FrameList list = new FrameList();
    int index;
    for(index = 0; index < numFrames; index++)
    {
      Location location = new Location();
      Frame frame = new Frame(location);
      
      // read the program counter, method pointer and frame pointer
      int programCounter = input.readInt();
      location.setIndex(programCounter);
      
      int methodPointer = input.readInt();
      location.setMethodId(methodPointer);
      
      framePointer = input.readInt();
      frame.setFrameId(framePointer);
      
//      System.out.println("  Program counter: " + programCounter);
//      System.out.println("  Method pointer: " + methodPointer);
//      System.out.println("  Frame Pointer: " + framePointer);
      System.out.println(frame);
      
      list.add(frame);
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
    //------------------------------------------------------------
    // query the stack depth
    output.writeByte(11);
    output.writeByte(7);
    
    received = input.readInt();
    System.out.println("  Stack depth: " + received);
    
    return received;
  }
  
  /**
   * Print information about all the stack frames currently 
   * on the call stack.
   * 
   * @return
   * @throws IOException
   */
  public int printStackFrames() throws IOException
  {
    checkConnection();
    
    int received;
    //------------------------------------------------------------
    // request to print all stack frames
    output.writeByte(11);
    output.writeByte(13);
    
    received = input.readInt();
//    System.out.println("  Stack depth: " + received);
    
    return received;
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
  public int printStackFrame(int frameIndex) throws IOException
  {
    checkConnection();
    
    int received;
    //------------------------------------------------------------
    // request to print all stack frames
    output.writeByte(11);
    output.writeByte(14);
    
    output.writeInt(frameIndex);
    
    received = input.readInt();
    
    return received;
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
    checkConnection();
    
    int received;
    //------------------------------------------------------------
    // query the second local variable (index 1) on the first frame (index 0)
    output.writeByte(16);
    output.writeByte(1);
    
    // frame 0 (main method call)
//    output.writeInt(0);
    output.writeInt(frameIndex);
    
    // local variable 1 (x variable)
//    output.writeInt(1);
    output.writeInt(variableIndex);

    received = input.readInt();
    System.out.println("  Variable value: " + received);
    
    return received;
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
    
    int received;
    //------------------------------------------------------------
    // set the second local variable (index 1) on the first frame (index 0)
    output.writeByte(16);
    output.writeByte(2);
    
    // frame 0 (main method call)
//    output.writeInt(0);
    output.writeInt(frameIndex);
    
    // local variable 1 (x variable)
//    output.writeInt(1);
    output.writeInt(variableIndex);

    // local variable value to be set: 32 (x variable)
//    output.writeInt(32);
    output.writeInt(value);
    
    received = input.readInt();
    System.out.println("  Variable value: " + received);
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
    // query the second local variable (index 1) on the first frame (index 0)
    output.writeByte(16);
    output.writeByte(5);
    
    // frame 0 (main method call)
//    output.writeInt(0);
    output.writeInt(frameIndex);
    
    received = input.readInt();
    System.out.println("  Number of local variables: " + received);
    
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
  public int invokeStaticMethod(int methodStructPointer, int parameter)
      throws IOException
  {
    checkConnection();
    
    int received;
    // ------------------------------------------------------------
    // request machine to invoke one static method with one parameter.
    // wait until it return.
    output.writeByte(3);
    output.writeByte(3);

    // method ID
    // output.writeInt(622);
    // output.writeInt(9216); // 9218: helloworld.TestJopDebugKernel.printValue(I)V
    // output.writeInt(9222); // 9222: helloworld.TestJopDebugKernel.printLine()V
    output.writeInt(methodStructPointer);

    // method argument to be printed:
    // output.writeInt(65);
    output.writeInt(parameter);

    // receive back the argument just to sync
    received = input.readInt();
    System.out.println("  Variable value: " + received);

    return received;
  }
  
  /**
   * Convenience method to invoke a static method which receive
   * no parameter.
   * 
   * @param methodStructPointer
   * @return
   * @throws IOException
   */
  public int invokeStaticMethod(int methodStructPointer)
  throws IOException
  {
    return invokeStaticMethod(methodStructPointer, 0);
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
    System.out.println("  Method pointer: " + methodPointer);

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
    output.writeByte(15);
    output.writeByte(1);
    
    // method pointer
    output.writeInt(methodStructPointer);
    output.writeInt(instructionOffset);
    
    // receive back the old instruction just to sync. If the answer is -1,
    // then an error happened (such as an invalid address).
    instruction = input.readInt();
    if(instruction != -1)
    {
      System.out.println("  Old instruction: " + instruction);
    }
    else
    {
      System.out.println("  Failure! received: " + instruction);
    }

    return instruction;
  }
  
  /**
   * Clear a breakpoint at the given instruction offset inside a method.
   * 
   * @param methodStructPointer
   * @param instructionOffset
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
    output.writeByte(15);
    output.writeByte(2);
    
    // method pointer
    output.writeInt(methodStructPointer);
    output.writeInt(instructionOffset);
    output.writeInt(oldInstruction);
    
    // receive back the instruction just to sync. If the answer is -1,
    // then an error happened (such as an invalid address).
    // In this case, the instruction SHOULD BE the breakpoint bytecode.
    instruction = input.readInt();
    if(instruction != -1)
    {
      System.out.println("  Old instruction: " + instruction);
    }
    else
    {
      System.out.println("  Failure! received: " + instruction);
    }

    return instruction;
  }
}
