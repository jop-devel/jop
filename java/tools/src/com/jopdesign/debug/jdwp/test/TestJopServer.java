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
import com.jopdesign.debug.jdwp.model.FrameList;

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
  
  private JOPDebugChannel debugChannel;
  
  public TestJopServer()
  {
    debugChannel = new JOPDebugChannel();
  }
  
  /**
   * 
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) throws IOException
  {
    TestJopServer testObject = new TestJopServer();
    testObject.testJopSimCommunication();
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
   */
  public void testJopSimCommunication() throws IOException
  {
    int received = 2;
    int methodPointer;
    int stackDepth;
    int index;
    
    try
    {
      debugChannel.connect();
//    handshake();
    }
    catch (Exception exception)
    {
      System.out.println();
      System.out.println("Connection failure. Make sure the server is running.");
      return;
    }
    
    // Code below is working fine
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
    
    
    System.out.println("Will request the machine to call some methods now.");
    System.out.println("If an exception is thrown, check the method addresses.");
    System.out.println("They may change everytime JOPizer run, but are hardwired here.");
    System.out.println("Methods to be called:");
    System.out.println("  helloworld.TestJopDebugKernel.printValue(I)V");
    System.out.println("  helloworld.TestJopDebugKernel.printLine()V");
    // be careful: method pointers change every time source code is changed
    //  9266: debug.TestJopDebugKernel.printValue(I)V
//    methodPointer = 11583;
    methodPointer = 11125;
    debugChannel.invokeStaticMethod(methodPointer, 65);
    debugChannel.invokeStaticMethod(methodPointer, 66);
    debugChannel.invokeStaticMethod(methodPointer, 67);
    debugChannel.invokeStaticMethod(methodPointer, 68);
    
    System.out.println(" Invoking a static method 4 times now...");
    // call the debug module from itself, just to see what happens
    //  9270: debug.TestJopDebugKernel.printLine()V
//    methodPointer = 11587;
    methodPointer = 11129;
    debugChannel.invokeStaticMethod(methodPointer);
    debugChannel.invokeStaticMethod(methodPointer);
    debugChannel.invokeStaticMethod(methodPointer);
    debugChannel.invokeStaticMethod(methodPointer);
    
//    // this does not work, don't try it.
//    System.out.println("Invoking the debug method now");
////    methodPointer = 9207;
////    methodPointer = 9119;
////    methodPointer = 9556;
//    debugChannel.invokeStaticMethod(methodPointer, 65);    
//    System.out.println("Returning");
////    debugChannel.requestExit(0);
    
    FrameList list = debugChannel.getStackFrameList();
    
    System.out.println();
    System.out.println("Frame list:");
    System.out.println(list);
    
//    testEmbeddedprinter();
    
//    System.out.println("Returning");
//    debugChannel.requestExit();
    System.out.println("Resuming");
    debugChannel.resume();
    
//    System.out.print("Available: ");
//    System.out.println(input.available());
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
