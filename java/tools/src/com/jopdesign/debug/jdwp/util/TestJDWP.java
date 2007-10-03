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

package com.jopdesign.debug.jdwp.util;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.jopdesign.debug.jdwp.JDWPEventManager;
import com.jopdesign.debug.jdwp.JOPDebugInterface;
import com.jopdesign.debug.jdwp.SymbolManager;
import com.jopdesign.debug.jdwp.constants.JDWPConstants;
import com.jopdesign.debug.jdwp.jop.JopSymbolManager;
import com.jopdesign.debug.jdwp.sniffer.PacketInputStreamReader;
import com.jopdesign.debug.jdwp.test.TestJopServer;

/**
 * A class to test and learn about the JDWP protocol.
 * This class is useful for testing purposes only and 
 * it represents only one of the test approaches used
 * during this project.
 * 
 * Anytime some behaviour needed to be tested, a new
 * quick method was created inside this class and run.
 * 
 * Besides those tests (useful for development),
 * at least two more test approaches were used
 * to build this project as quickly as possible:
 * automatic program analysis tools and unit tests.
 *   
 * I think each approach is useful for a different goal
 * and all of them complement each other.
 * They can also be reproduced so as to check if the
 * observed behaviour is still the same or if something
 * was broken due to some change. 
 * 
 * @author Paulo Abadie Guedes
 */
public class TestJDWP
{
  /**
   * Test:
   * 
   * Read the set of messages from the server and build a list of packets.
   * @throws FileNotFoundException 
   */
  private static void test1_server() throws IOException
  {
    String name = JDWPConstants.DEFAULT_SERVER_FILE;
    PacketList list = PacketInputStreamReader.readPacketList(name);
    
    System.out.println("");
    System.out.print("  Number of JDWP packets read from " + name + ": ");
    System.out.println(list.size());
  }

  /**
   * Test:
   * 
   * Read the set of messages from the client and build a list of packets.
   * @throws FileNotFoundException 
   */
  private static void test2_client() throws IOException
  {
    String name = JDWPConstants.DEFAULT_CLIENT_FILE;
    PacketList list = PacketInputStreamReader.readPacketList(name);
    
    System.out.println("");
    System.out.print("  Number of JDWP packets read from " + name + ": ");
    System.out.println(list.size());
  }
  
  /**
   * Test:
   * 
   * Read the set of messages from the server, build a list of packets.
   * Print information about some of them.
   * 
   * @throws FileNotFoundException 
   */
  private static void test3_information() throws IOException
  {
    System.out.println("  test3_information();");
    String name = JDWPConstants.DEFAULT_SERVER_FILE;
    PacketPrinter.dumpJDWPPacketLog(name);
  }
  
  /**
   * Test:
   * 
   * Read the set of messages from the client, build a list of packets.
   * Print information about some of them.
   * 
   * @throws FileNotFoundException 
   */
  private static void test4_information() throws IOException
  {
    System.out.println("  test4_information();");
    String name = JDWPConstants.DEFAULT_CLIENT_FILE;
    PacketPrinter.dumpJDWPPacketLog(name);
  }

  /**
   * Test: launch a server, expect a connection, expect to 
   * receive JDWP packets and log them to the disk.
   * 
   * Launch the JDWPEventManager to see what happens.
   * 
   * @throws FileNotFoundException 
   */
  private static void test5_JDWPEventManager() throws IOException
  {
    JOPDebugInterface debugInterface = getDebugInterface();
    JDWPEventManager manager = new JDWPEventManager(debugInterface , 8010);
    
    try
    {
      manager.start();
      manager.join();
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
    
    System.out.println("  Done.");
//    printInfo(list, 20);
//    printInfo(list, list.size());
  }
  
  /**
   * @return
   */
  private static JOPDebugInterface getDebugInterface()
  {
    SymbolManager manager = new JopSymbolManager();
    JOPDebugInterface debugInterface = new JOPDebugInterface(manager);
    return debugInterface;
  }
  
  /**
   * Test: launch a server, expect a connection.
   * Create a ConnectionService based on the received
   * socket. Create a VirtualMachineManager and
   * start to test with it.
   *
   * FAILED: this way will not let me read packets
   * easily. Class coupling and dependencies is too high
   * inside JDI package to allow an easy adaptation
   * of its classes for the debugee.
   * 
   * Launch the JDWPEventManager to see what happens.
   * 
   * @throws FileNotFoundException 
   */
//  private static void test6_JDWPEventManager() throws IOException
//  {
//    JOPDebugInterface debugInterface = getDebugInterface();
//    JDWPEventManager manager = new JDWPEventManager(debugInterface, 8010);
//    Socket socket = manager.acceptNewConnection();
//    //manager.start();
//    SocketConnectionWrapper socketConnectionWrapper = 
//      new SocketConnectionWrapper(socket);
//    JOPMachineManagerImpl machineManagerImpl = new JOPMachineManagerImpl();
//    machineManagerImpl.createVirtualMachine(socketConnectionWrapper);
//    
//    try
//    {
//      manager.join();
//    }
//    catch (InterruptedException e)
//    {
//      e.printStackTrace();
//    }
//    
//    System.out.println("  Done.");
////    printInfo(list, 20);
////    printInfo(list, list.size());
//  }
  
  public static void runAllTests() throws IOException
  {
    test1_server();
    test2_client();
    test3_information();
    test5_JDWPEventManager();
  }
  
  private static void test_8_JopSimCommunication() throws IOException
  {
//    TestJopServer.testJopSimCommunication();
    TestJopServer.main(null);
  }
  
  /**
   * A placeholder to run all tests.
   * 
   * @param args
   * @throws FileNotFoundException 
   */
  public static void main(String[] args) throws IOException
  {
    try
    {
//    test1_server();
//    test2_client();
//      test3_information();
      test4_information();
//    test5_JDWPEventManager();
      
      // FAIL: don't use.
//      test6_JDWPEventManager();
      
//      test7_request_answer();
    
//      test_8_JopSimCommunication();
    }
    catch(Throwable t)
    {
      t.printStackTrace();
    }
    finally
    {
//      Debug.println("  Debug summary:");
//      Debug.printExecutionSummary();
    }
  }
}
