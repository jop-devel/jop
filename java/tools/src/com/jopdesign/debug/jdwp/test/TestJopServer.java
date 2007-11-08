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

package com.jopdesign.debug.jdwp.test;

import java.io.IOException;

import com.jopdesign.debug.jdwp.JOPDebugChannel;
import com.jopdesign.debug.jdwp.SymbolManager;
import com.jopdesign.debug.jdwp.model.FrameList;
import com.jopdesign.debug.jdwp.model.LineTable;

/**
 * TestJopServer.java
 * 
 * A class to test the JOP debug module running inside JOP, connected
 * by one JOP debug server.
 * 
 * This class talk to a network server, which in turn talks to JOP
 * sending requests and answers back and forth.
 * This class does not know what's on the other side: a real JOP
 * processor, a cycle-accurate hardware simulator or even
 * the JOP simulator itself (and it does not need to know).
 * 
 * The server has three roles:
 * 1) Provide access to a JOP machine
 * 2) Abstract the communication channel (e.g. serial, USB, net, whatever)
 * 3) Abstract which JOP machine is actually running (for instance, JopSim)
 * 
 * @author Paulo Abadie Guedes
 *
 * 01/06/2007 - 13:25:04
 * 
 */
public class TestJopServer
{
//  private DataInputStream input;
//  private DataOutputStream output;
  
  // class name to be used as reference
  private static final String DEBUG_TEST_JOP_DEBUG_KERNEL = "debug.TestJopDebugKernel";
  
  // methods to be called
  private static final String METHOD_NAME_PRINT_VALUE_I_V = "printValue(I)V";
  private static final String METHOD_NAME_PRINT_LINE_V = "printLine()V";
  
  // the TestObject
  private static final String DEBUG_TEST_OBJECT = "debug.TestObject";
  
  // the method signatures to be called.
  private static final String METHOD_NAME_IDENTITY = "identity(I)I";
  private static final String METHOD_NAME_INCREMENT = "increment(I)I";
  private static final String METHOD_NAME_TEST_INC = "testInc()I";
  private static final String METHOD_NAME_GET_CONSTANT = "getConstant()I";
  
  private JOPDebugChannel debugChannel;
  private SymbolManager manager;
  
  public TestJopServer()
  {
    debugChannel = new JOPDebugChannel();
    manager = new SymbolManager();
  }
  
  /**
   * 
   * @param args
   * @throws IOException
   * @throws ClassNotFoundException 
   */
  public static void main(String[] args) throws IOException
  {
    if(args.length != 1)
    {
      System.out.println("  Usage: TestJopServer <symbol file>");
      System.out.println();
      return;
    }
    
    TestJopServer testObject = new TestJopServer();
    testObject.testJopSimCommunication(args[0]);
  }
  
//  public void basicCommunicationTest() throws IOException
//  {
//    int data;
//    int received;
//    
//    data = 1;
//    System.out.println(" Will send " + data);
//    output.write(data);
//    
//    received = input.read();
//    
//    System.out.println(" Sent: " + data);
//    System.out.println(" Received: " + received);
//    if(data == received)
//    {
//      System.out.println(" Success!!!");
//    }
//  }
  
  /**
   * 
   * @throws IOException
   * @throws ClassNotFoundException 
   */
  public void testJopSimCommunication(String symbolFile) throws IOException
  {
    int received = 2;
    int methodPointer;
    int stackDepth;
    int index;
    
    try
    {
      initialize(symbolFile);
    }
    catch(ClassNotFoundException exception)
    {
      System.out.println();
      System.out.println("  Class not found.");
      System.out.println(exception.getMessage());
      return;      
    }
    catch (Exception exception)
    {
      System.out.println();
      System.out.println("Connection failure. Make sure the server is running.");
      return;
    }
    
    testStackAccess();
    testMethodCalls();
    
//    // this does not work, don't try it.
//    System.out.println("Invoking the debug method now");
////    methodPointer = 9207;
////    methodPointer = 9119;
////    methodPointer = 9556;
//    debugChannel.invokeStaticMethod(methodPointer, 65);    
//    System.out.println("Returning");
////    debugChannel.requestExit(0);
    
    testGetStackFrameList();
    
//    testEmbeddedprinter();
    
//    System.out.println("Returning");
//    debugChannel.requestExit();
    
    // this test worked fine.
//    testSetBreakPoint_getConstant();
    
    received = testSetBreakPoint_identity();
    
    System.out.println("Resuming");
    debugChannel.resume();
//    
////    System.out.println("If the next breakpoint it hit, shoud call 'printLine'now:");
////    testInvokeStatic_2(DEBUG_TEST_JOP_DEBUG_KERNEL, METHOD_NAME_PRINT_LINE_V);
//    
    System.out.println("Will clear the previous breakpoint now:");
    testClearBreakPoint_identity(received);
    
    debugChannel.resume();
//    System.out.print("Available: ");
//    System.out.println(input.available());
  }
  
  /**
   * @throws IOException 
   * 
   */
  private void testSetBreakPoint_getConstant() throws IOException
  {
    String className, methodSignature;
    int methodPointer;
    LineTable table;
    int offset = 0;
    int oldInstruction;
    int i, num;
    
    className = DEBUG_TEST_OBJECT;
    methodSignature = METHOD_NAME_GET_CONSTANT;
    methodPointer = manager.getMethodStructPointer(className, methodSignature);
    table = manager.getLineTable(className, methodSignature);
    
    if(table.numLines() <= 0)
    {
      System.out.println("Failure: empty line table!");
      return;
    }
    
    // get the offset of the first line
    offset = (int) table.getLine(0).getLineCodeIndex();
    offset = 5;
    
    num = manager.getMethodSizeInBytes(className, methodSignature);
    System.out.println("Method size: " + num);
    for(i = 0; i < num; i++)
    {
      offset = i;
      System.out.println("Will set a breakpoint!");
      oldInstruction = debugChannel.setBreakPoint(methodPointer, offset);
      
      System.out.println("Will clear the breakpoint!");
      oldInstruction = debugChannel.clearBreakPoint(methodPointer, offset, oldInstruction);
    }
  }
  
  private int testSetBreakPoint_identity() throws IOException
  {
    String className, methodSignature;
    int methodPointer;
    int offset = 0;
    int oldInstruction;
    
    className = DEBUG_TEST_OBJECT;
    methodSignature = METHOD_NAME_IDENTITY;
    methodPointer = manager.getMethodStructPointer(className, methodSignature);
    
    offset = 0;
    oldInstruction = debugChannel.setBreakPoint(methodPointer, offset);
    
    return oldInstruction;
  }
  
  private void testClearBreakPoint_identity(int oldInstruction) throws IOException
  {
    String className, methodSignature;
    int methodPointer;
    int offset = 0;
    
    className = DEBUG_TEST_OBJECT;
    methodSignature = METHOD_NAME_IDENTITY;
    methodPointer = manager.getMethodStructPointer(className, methodSignature);
    
    offset = 0;
    debugChannel.clearBreakPoint(methodPointer, offset, oldInstruction);
  }

  /**
   * Test requests to invoke static methods through the network.
   * 
   * @throws IOException
   */
  private void testMethodCalls() throws IOException
  {
    System.out.println("Will request the machine to call some methods now.");
    System.out.println("If an exception is thrown, check the method struct addresses.");
    System.out.println("They should be correct since they came from the symbol file.");
    System.out.println();
    System.out.println("Methods to be called:");
    System.out.println("  helloworld.TestJopDebugKernel.printValue(I)V");
    System.out.println("  helloworld.TestJopDebugKernel.printLine()V");
    
    testInvokeStatic_1(DEBUG_TEST_JOP_DEBUG_KERNEL, METHOD_NAME_PRINT_VALUE_I_V);
    testInvokeStatic_2(DEBUG_TEST_JOP_DEBUG_KERNEL, METHOD_NAME_PRINT_LINE_V);
  }
  
  /**
   * @throws IOException
   */
  private void testGetStackFrameList() throws IOException
  {
    FrameList list = debugChannel.getStackFrameList();
    
    System.out.println();
    System.out.println("Frame list:");
    System.out.println(list);
  }
  
  /**
   * Test invokeStatic method call.
   * 
   * @throws IOException
   */
  private void testInvokeStatic_2(String className, String methodName)
    throws IOException
  {
    System.out.println(" Invoking a static method 4 times now...");
    
    int methodPointer;
    // call the debug module from itself, just to see what happens
    //  9270: debug.TestJopDebugKernel.printLine()V
//    methodPointer = 11587;
//    methodPointer = 11129;
    methodPointer = manager.getMethodStructPointer(className, methodName);
    
    debugChannel.invokeStaticMethod(methodPointer);
    debugChannel.invokeStaticMethod(methodPointer);
    debugChannel.invokeStaticMethod(methodPointer);
    debugChannel.invokeStaticMethod(methodPointer);
  }
  
  /**
   * Test invokeStatic method call.
   * 
   * @throws IOException
   */
  private void testInvokeStatic_1(String className, String methodName)
    throws IOException
  {
    // be careful: method pointers change every time source code is changed
    //  9266: debug.TestJopDebugKernel.printValue(I)V
//    methodPointer = 11583;
    //methodPointer = 11125;

    int methodPointer;
    methodPointer = manager.getMethodStructPointer(className, methodName);
    debugChannel.invokeStaticMethod(methodPointer, 65);
    debugChannel.invokeStaticMethod(methodPointer, 66);
    debugChannel.invokeStaticMethod(methodPointer, 67);
    debugChannel.invokeStaticMethod(methodPointer, 68);
  }

  /**
   * Test access to the call stack. It get a local variable value,
   * set it to a new value and query again.
   * Then it print information on the stack frames currently on the stack.
   * 
   * @throws IOException
   */
  private void testStackAccess() throws IOException
  {
    int received;
    int methodPointer;
    int stackDepth;
    int index;
    stackDepth = debugChannel.getStackDepth();
    
    received = debugChannel.getLocalVariableValue(0, 1);
    System.out.println("Local variable(0, 1): " + received);
    
    debugChannel.setLocalVariableValue(0, 1, 32);
    received = debugChannel.getLocalVariableValue(0, 1);
    System.out.println("After setting to 32, value is: " + received);
    
    debugChannel.setLocalVariableValue(0, 1, 48);
    received = debugChannel.getLocalVariableValue(0, 1);
    System.out.println("After setting to 48, value is: " + received);
    
    System.out.print("Stack depth: ");
    System.out.println(stackDepth);
    System.out.println();
    for(index = 0; index <= stackDepth; index++)
    {
      methodPointer = debugChannel.getMethodPointer(index);
      System.out.print("Method pointer at frame ");
      System.out.print(index);
      System.out.print(" is: ");
      System.out.println(methodPointer);
    }
  }

  /**
   * @param symbolFile
   * @throws IOException
   * @throws ClassNotFoundException
   */
  private void initialize(String symbolFile) throws IOException, ClassNotFoundException
  {
    debugChannel.connect();
//    handshake();
    
    // load symbols to know the method addresses 
    manager.loadSymbolTable(symbolFile);
    System.out.println("Loaded symbols.");
  }
  
  /**
   * @throws IOException 
   * 
   */
//  private void testEmbeddedprinter() throws IOException
//  {
//    int index, value;
//    int size = 256;
//    boolean result = true;
//    
//    size = 0x0fffffff;
//    for(index = 0; index < size; index++)
//    {
//      value = input.readInt();
//      
//      if(value != index)
//      {
//        System.out.println(" Value: " + value + " Expected: " + index);
//        result = false;
//      }
//      else
//      {
////        if((index & 63) == 0)
////        {
////          System.out.println(".");
////        }
////        else
////        {
////          System.out.print(".");
////        }
//        // print index at every 1023 elements
//        if((index & 0x3ff) == 0)
//        {
//          System.out.println(index);
//        }
//      }
//    }
//    if(result)
//    {
//      System.out.println("Passed!");
//    }
//    else
//    {
//      System.out.println("Failed.");
//    }
//  }
  
//  private void testEmbeddedPrinterReadByte() throws IOException
//  {
//    int index, value;
//    int size = 256;
//    for(index = 0; index < size; index++)
//    {
//      // the call to readByte() interpret data as a signed byte.
////      value = input.readByte();
//      value = input.read();
//      
//      if(value != index)
//      {
//        System.out.println(" Value: " + value + " Expected: " + index);
//      }
//    }
//    System.out.println("Done.");
//  }
}
