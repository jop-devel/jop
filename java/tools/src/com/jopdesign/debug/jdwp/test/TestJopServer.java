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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;

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
  
  // a few constants to control interactive testing
  private static final int INVALID_OPTION = -1;
  private static final int DUMP_CALL_STACK = 1;
  private static final int PRINT_STACK_DEPTH = 2;
  private static final int PRINT_STACK_FRAME = 3;
  private static final int GET_LOCAL_VARIABLE = 4;
  private static final int SET_LOCAL_VARIABLE = 5;
  private static final int INVOKE_STATIC_METHOD = 6;
  private static final int RESUME_EXECUTION = 7;
  private static final int EXIT_OPTION = 8;
  
//  private static final int SET_BREAKPOINT = 9;
//  private static final int CLEAR_BREAKPOINT = 10;
  
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
   * @throws ClassNotFoundException 
   */
  public static void main(String[] args) throws IOException
  {
    if(args.length != 1 && args.length != 2)
    {
      println("  Usage:");
      println();
      println("  Default test:     TestJopServer <symbol file>");
      println("  Interactive test: TestJopServer <symbol file> -i");
      println();
      
      return;
    }
    
    TestJopServer testObject = new TestJopServer();
    
    try
    {
      if(args.length == 1)
      {
        testObject.testJopSimCommunication(args[0]);
      }
      else
      {
        if(args.length == 2 && args[1].equals("-i"))
        {
          try
          {
            testObject.testJopDebugKernelInteractively(args[0]);
          }
          catch(SocketException exception)
          {
            println("Interrupted. Connection closed by the server.");
          }
        }
        else
        {
          println("Failure: unexpected parameter -> " + args[1]);
        }
      }
    }
    catch (Exception exception)
    {
      println("Failure: " + exception.getMessage());
      println();
      exception.printStackTrace();
    }
  }
  
//  public void basicCommunicationTest() throws IOException
//  {
//    int data;
//    int received;
//    
//    data = 1;
//    println(" Will send " + data);
//    output.write(data);
//    
//    received = input.read();
//    
//    println(" Sent: " + data);
//    println(" Received: " + received);
//    if(data == received)
//    {
//      println(" Success!!!");
//    }
//  }
  
  public void testJopDebugKernelInteractively(String symbolFile) throws IOException, ClassNotFoundException
  {
    int command;
    boolean shouldContinue;
    initialize(symbolFile);
    
    // create a reader to make it easy to read lines from the input
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    
    // flag to control interactive loop
    shouldContinue = true;
    
    // small loop to print a small menu, read a command from the user, 
    // send it to JOP, read back the answer, present it and continue.
    do
    {
      printCommandMenu();
      
      command = readCommand(reader);
      
      switch(command)
      {
        case GET_LOCAL_VARIABLE:
        {
          interactiveTestGetLocalVariable(reader);
          break;
        }
        
        case SET_LOCAL_VARIABLE:
        {
          interactiveTestSetLocalVariable(reader);
          break;
        }
        
        case PRINT_STACK_DEPTH:
        {
          interactiveTestPrintStackDepth(reader);
          break;
        }
        
        case PRINT_STACK_FRAME:
        {
          interactiveTestPrintStackFrame(reader);
          break;
        }
        
        case DUMP_CALL_STACK:
        {
          interactiveTestDumpCallStack(reader);
          break;
        }
        
        case INVOKE_STATIC_METHOD:
        {
          interactiveTestInvokeStaticMethod(reader);
          break;
        }
        
        case RESUME_EXECUTION:
        {
          interactiveTestResumeExecution(reader);
          break;
        }
        
        case EXIT_OPTION:
        {
          interactiveTestExit(reader);
          // ok, stop execution here.
          shouldContinue = false;
          break;
        }
        // case SET_BREAKPOINT: { break; }
        //        case CLEAR_BREAKPOINT: {               break; }
        
        case INVALID_OPTION:
        default:
        {
          println("Invalid option. Ignoring.");
          println();
        }
      }
    }
    //while(command != EXIT_OPTION && command != RESUME_EXECUTION);
    while(shouldContinue);
  }
  
  /**
   * @param reader 
   * @throws IOException 
   * 
   */
  private void interactiveTestGetLocalVariable(BufferedReader reader) throws IOException
  {
    int frameIndex, variableIndex;
    int value;
    
    // get and test the stack frame index
    print("Please input the stack frame index: ");
    frameIndex = readNonNegativeInteger(reader);
    
    if(isValidStackFrameIndex(frameIndex) == false)
    {
      println("Failure: invalid stack frame index -> " + frameIndex);
      println();
      return;
    }
    
    // get and test the local variable index
    print("Please input the local variable index: ");
    variableIndex = readNonNegativeInteger(reader);
    
    if(isValidLocalVariableIndex(frameIndex, variableIndex) == false)
    {
      println("Failure: invalid local variable index -> " + variableIndex);
      println();
      return;
    }
    
    // get a local variable value
    value = debugChannel.getLocalVariableValue(frameIndex, variableIndex);
    
    print("Value: ");
    println(value);
  }

  /**
   * @param frameIndex
   * @param variableIndex
   * @return
   * @throws IOException 
   */
  private boolean isValidLocalVariableIndex(int frameIndex, int variableIndex)
    throws IOException
  {
    int numOfLocalVariables;
    boolean isValid = false;
    
    if(isValidStackFrameIndex(frameIndex))
    {
      numOfLocalVariables = debugChannel.getNumberOfLocalVariables(frameIndex);
      if((variableIndex < 0) || (variableIndex >= numOfLocalVariables))
      {
        isValid = false;
      }
      else
      {
        isValid = true;
      }
    }
    
    return isValid;
  }
  
  /**
   * @param frameIndex
   * @throws IOException
   */
  private boolean isValidStackFrameIndex(int frameIndex) throws IOException
  {
    int depth;
    boolean isValid = false;
    
    depth = debugChannel.getStackDepth();
    if(frameIndex < 0 || frameIndex >= depth)
    {
      isValid = false;
    }
    else
    {
      isValid = true;
    }
    
    return isValid;
  }

  /**
   * @param reader 
   * @throws IOException 
   * 
   */
  private void interactiveTestSetLocalVariable(BufferedReader reader) throws IOException
  {
    int frameIndex, variableIndex;
    int value;
    
    println("Please input the stack frame index: ");
    frameIndex = readNonNegativeInteger(reader);
    
    if(isValidStackFrameIndex(frameIndex) == false)
    {
      println("Failure: invalid stack frame index -> " + frameIndex);
      println();
      return;
    }
    
    // get and test the local variable index
    print("Please input the local variable index: ");
    variableIndex = readNonNegativeInteger(reader);
    
    if(isValidLocalVariableIndex(frameIndex, variableIndex) == false)
    {
      println("Failure: invalid local variable index -> " + variableIndex);
      println();
      return;
    }
    
    println("Please input the new value: ");
    value = readNonNegativeInteger(reader);
    
    debugChannel.setLocalVariableValue(frameIndex, variableIndex, value);
    
    println("Value updated.");
  }
  
  /**
   * @throws IOException 
   * 
   */
  private void interactiveTestPrintStackDepth(BufferedReader reader) throws IOException
  {
    int stackDepth;
    
    stackDepth = debugChannel.getStackDepth();
    
    println("Stack depth: " + stackDepth);
    println();
  }

  /**
   * @throws IOException 
   * 
   */
  private void interactiveTestPrintStackFrame(BufferedReader reader) throws IOException
  {
    int frameIndex;
    
    // get and test the stack frame index
    print("Please input the stack frame index: ");
    frameIndex = readNonNegativeInteger(reader);
    
    if(isValidStackFrameIndex(frameIndex) == false)
    {
      println("Failure: invalid stack frame index -> " + frameIndex);
      println();
      return;
    }
    
    debugChannel.printStackFrame(frameIndex);
  }

  /**
   * @throws IOException 
   * 
   */
  private void interactiveTestDumpCallStack(BufferedReader reader)
    throws IOException
  {
    debugChannel.printStackFrames();
  }

  /**
   * @throws IOException 
   * 
   */
  private void interactiveTestInvokeStaticMethod(BufferedReader reader)
    throws IOException
  {
    int methodStructPointer;
    
    print("Please input the method pointer : ");
    methodStructPointer = readNonNegativeInteger(reader);
    
    if(isValidMethodPointer(methodStructPointer) == false)
    {
      println("Failure: invalid method pointer -> " + methodStructPointer);
      println();
      return;
    }
    
    debugChannel.invokeStaticMethod(methodStructPointer);
  }
  
  /**
   * Print an object to the standard output and flush the buffer.
   * Created to better interact with Ant.
   * 
   * @param data the object to be printed.
   */
  private static void print(Object data)
  {
    System.out.println(data);
    System.out.flush();
  }
  
  /**
   * Print an int to the standard output and flush the buffer.
   * Created to better interact with Ant.
   * 
   * @param data the int to be printed.
   */
  private static void print(int data)
  {
    System.out.print(data);
    System.out.flush();
  }
  
  /**
   * Print an object to the standard output, a newline and flush the buffer.
   * Created to better interact with Ant.
   * 
   * @param data the object to be printed.
   */
  private static void println(Object data)
  {
    System.out.println(data);
    System.out.flush();
  }
  
  /**
   * Print an int to the standard output, a newline and flush the buffer.
   * Created to better interact with Ant.
   * 
   * @param data the int to be printed.
   */
  private static void println(int data)
  {
    System.out.println(data);
    System.out.flush();
  }
  
  /**
   * Print a newline to the standard output and flush the buffer.
   * Created to better interact with Ant.
   * 
   * @param data the int to be printed.
   */
  private static void println()
  {
    System.out.println("");
    System.out.flush();
  }
  /**
   * @param methodStructPointer
   * @return
   */
  private boolean isValidMethodPointer(int methodStructPointer)
  {
    return manager.isValidMethodStructurePointer(methodStructPointer);
  }
  
  /**
   * @throws IOException 
   * 
   */
  private void interactiveTestResumeExecution(BufferedReader reader)
    throws IOException
  {
    debugChannel.sendResumeCommand();
  }
  
  /**
   * @throws IOException 
   * 
   */
  private void interactiveTestExit(BufferedReader reader) throws IOException
  {
    debugChannel.sendExitCommand(0);
  }
  
  /**
   * 
   */
  private void printCommandMenu()
  {
    printLine();
    
    print(DUMP_CALL_STACK);
    println(". Dump the call stack");
    
    print(PRINT_STACK_DEPTH);
    println(". Print stack depth");
    
    print(PRINT_STACK_FRAME);
    println(". Print a stack frame");
    
    print(GET_LOCAL_VARIABLE);
    println(". Get a local variable");
    
    print(SET_LOCAL_VARIABLE);
    println(". Set a local variable");
    
    print(INVOKE_STATIC_METHOD);
    println(". Invoke a static method");
    
    print(RESUME_EXECUTION);
    println(". Resume execution (return from breakpoint call)");
    
    print(EXIT_OPTION);
    println(". Terminate execution: call System.exit()");
//    print(SET_BREAKPOINT);
//    println(". ");
//    print(CLEAR_BREAKPOINT);
//    println(". ");
    
    println();
    printLine();
  }
  
  /**
   * 
   */
  private void printLine()
  {
    println("----------------------------------------");
  }

  /**
   * @return
   */
  private int readCommand(BufferedReader reader)
  {
    int data;
    
    print("Next command: ");
    data = readNonNegativeInteger(reader);
    
    return data;
  }
  
  private static int readNonNegativeInteger(BufferedReader reader)
  {
    String text;
    int data;
    
    data = INVALID_OPTION;
    
    try
    {
      text = reader.readLine();
      text = text.trim();
      data = Integer.parseInt(text);
    }
    catch(IOException exception)
    {
      exception.printStackTrace();
    }
    catch(NumberFormatException exception)
    {
      data = INVALID_OPTION;
    }
    
    return data;
  }

  /**
   * 
   * @throws IOException
   * @throws ClassNotFoundException 
   * @throws ClassNotFoundException 
   */
  public void testJopSimCommunication(String symbolFile) throws IOException, ClassNotFoundException
  {
    int received = 2;
    int methodPointer;
    int stackDepth;
    int index;
    
    initialize(symbolFile);
    
    testStackAccess();
    testMethodCalls();
    
//    // this does not work, don't try it.
//    println("Invoking the debug method now");
////    methodPointer = 9207;
////    methodPointer = 9119;
////    methodPointer = 9556;
//    debugChannel.invokeStaticMethod(methodPointer, 65);    
//    println("Returning");
////    debugChannel.requestExit(0);
    
    testGetStackFrameList();
    
//    testEmbeddedprinter();
    
//    println("Returning");
//    debugChannel.requestExit();
    
    // this test worked fine.
//    testSetBreakPoint_getConstant();
    
    received = testSetBreakPoint_identity();
    
//    println("Resuming");
//    debugChannel.resume();
//    
////    println("If the next breakpoint it hit, shoud call 'printLine'now:");
////    testInvokeStatic_2(DEBUG_TEST_JOP_DEBUG_KERNEL, METHOD_NAME_PRINT_LINE_V);
//    
    println("Will clear the previous breakpoint now:");
    testClearBreakPoint_identity(received);
    
    debugChannel.sendResumeCommand();
//    print("Available: ");
//    println(input.available());
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
      println("Failure: empty line table!");
      return;
    }
    
    // get the offset of the first line
    offset = (int) table.getLine(0).getLineCodeIndex();
    offset = 5;
    
    num = manager.getMethodSizeInBytes(className, methodSignature);
    println("Method size: " + num);
    for(i = 0; i < num; i++)
    {
      offset = i;
      println("Will set a breakpoint!");
      oldInstruction = debugChannel.setBreakPoint(methodPointer, offset);
      
      println("Will clear the breakpoint!");
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
    println("Will request the machine to call some methods now.");
    println("If an exception is thrown, check the method struct addresses.");
    println("They should be correct since they came from the symbol file.");
    println();
    println("Methods to be called:");
    println("  helloworld.TestJopDebugKernel.printValue(I)V");
    println("  helloworld.TestJopDebugKernel.printLine()V");
    
    testInvokeStatic_1(DEBUG_TEST_JOP_DEBUG_KERNEL, METHOD_NAME_PRINT_VALUE_I_V);
    testInvokeStatic_2(DEBUG_TEST_JOP_DEBUG_KERNEL, METHOD_NAME_PRINT_LINE_V);
  }
  
  /**
   * @throws IOException
   */
  private void testGetStackFrameList() throws IOException
  {
    FrameList list = debugChannel.getStackFrameList();
    
    println();
    println("Frame list:");
    println(list);
  }
  
  /**
   * Test invokeStatic method call.
   * 
   * @throws IOException
   */
  private void testInvokeStatic_2(String className, String methodName)
    throws IOException
  {
    println(" Invoking a static method 4 times now...");
    
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
    println("Local variable(0, 1): " + received);
    
    debugChannel.setLocalVariableValue(0, 1, 32);
    received = debugChannel.getLocalVariableValue(0, 1);
    println("After setting to 32, value is: " + received);
    
    debugChannel.setLocalVariableValue(0, 1, 48);
    received = debugChannel.getLocalVariableValue(0, 1);
    println("After setting to 48, value is: " + received);
    
    print("Stack depth: ");
    println(stackDepth);
    println();
    for(index = 0; index <= stackDepth; index++)
    {
      methodPointer = debugChannel.getMethodPointer(index);
      print("Method pointer at frame ");
      print(index);
      print(" is: ");
      println(methodPointer);
    }
  }

  /**
   * Connect to the server and load the symbol file.
   * 
   * @param symbolFile
   * @throws IOException
   * @throws ClassNotFoundException
   */
  private void initialize(String symbolFile) throws IOException, ClassNotFoundException
  {
    try
    {
      debugChannel.connect();
//    handshake();
    }
    catch (IOException exception)
    {
      println();
      println("Connection failure. Please make sure the server is running.");
      throw exception;
    }
    
    // load symbols to know the method addresses
    try
    {
      manager.loadSymbolTable(symbolFile);
      println("Loaded symbols.");
    }
    catch(ClassNotFoundException exception)
    {
      println();
      println("  Class not found.");
      println(exception.getMessage());
      throw exception;
//      return;      
    }
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
//        println(" Value: " + value + " Expected: " + index);
//        result = false;
//      }
//      else
//      {
////        if((index & 63) == 0)
////        {
////          println(".");
////        }
////        else
////        {
////          print(".");
////        }
//        // print index at every 1023 elements
//        if((index & 0x3ff) == 0)
//        {
//          println(index);
//        }
//      }
//    }
//    if(result)
//    {
//      println("Passed!");
//    }
//    else
//    {
//      println("Failed.");
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
//        println(" Value: " + value + " Expected: " + index);
//      }
//    }
//    println("Done.");
//  }
}
