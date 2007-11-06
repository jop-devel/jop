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

  public void connect() throws IOException
  {
    connect(NetworkConstants.DEFAULT_HOST,
      NetworkConstants.DEFAULT_JOP_SERVER_PORT_NUMBER);
  }
  
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
  
  public boolean isConnected()
  {
    return connected;
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
  
  public void requestExit(int exitCode) throws IOException
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
  
  public void suspendJavaMachine() throws IOException
  {
    checkConnection();
    //------------------------------------------------------------
    // send "suspend" command
    output.writeByte(1);
    output.writeByte(8);
    
    input.readInt();    
  }
  
  public void resumeJavaMachine() throws IOException
  {
    checkConnection();
    //------------------------------------------------------------
    // send "resume" command
    output.writeByte(1);
    output.writeByte(9);
    
    input.readInt();    
  }
  
  public void resume() throws IOException
  {
    checkConnection();
    //------------------------------------------------------------
    // send "resume" command
    output.writeByte(11);
    output.writeByte(3);
    
    input.readInt();
  }
  
  public void suspendThread() throws IOException
  {
    checkConnection();
    //------------------------------------------------------------
    // send "suspend" command
    output.writeByte(11);
    output.writeByte(2);
    
    input.readInt();    
  }
  
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
  
  // read a chunk of memory and return it.
  // size must be a multiple of 4 (each word is 4 bytes).
  // if it's not, it will be increased to the next multiple of four.
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
  
  /*
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
