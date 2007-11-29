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

import com.jopdesign.debug.jdwp.constants.JDWPConstants;
import com.jopdesign.debug.jdwp.sniffer.PacketInputStreamReader;
import com.sun.tools.jdi.PacketWrapper;

/**
 * PacketPrinter.java
 * 
 * 
 * @author Paulo Abadie Guedes
 *
 * 08/06/2007 - 16:22:18
 * 
 */
public class PacketPrinter
{
  public static void main(String args[])
  {
    String [] data;
    
    if(args.length > 0)
    {
      data = args;
    }
    else
    {
      data = new String[2];
      data[0] = JDWPConstants.DEFAULT_SERVER_FILE;
      data[1] = JDWPConstants.DEFAULT_CLIENT_FILE;
    }
    
    if(data.length == 1)
    {
      String name = data[0];
      try
      {
        dumpJDWPPacketLog(name);
      }
      catch(FileNotFoundException exception)
      {
        System.out.print(" Failure: file not found -> ");
        System.out.println(name);
      }
      catch(IOException exception)
      {
        System.out.print(" Failure: cannot open file -> ");
        System.out.println(name);
      }
    }
    else
    {
      try
      {
        printRequestAndAnswer(data[0], data[1]);
      }
      catch(IOException exception)
      {
        System.out.println("Failure: " + exception.getMessage());
        exception.printStackTrace();
        // exit with failure
        System.exit(-1);
      }
    }
  }
  
  /**
     * @param name
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void dumpJDWPPacketLog(String name) throws FileNotFoundException, IOException
    {
      PacketList list = PacketInputStreamReader.readPacketList(name);
      
      System.out.println("");
      System.out.print("  Number of JDWP packets read from " + name + ": ");
      System.out.println(list.size());
      
  //    printInfo(list, 20);
      PacketPrinter.printInfo(list, list.size());
    }

  public static void printInfo(PacketList list, int numPackets)
  {
    int i, num;
    num = list.size();
    
    // avoid falling out of the list border
    if(numPackets > num)
    {
      numPackets = num;
    }
    
    for(i = 0; i < numPackets; i++)
    {
      PacketWrapper packet = list.get(i);
      packet.printInformation();
    }
  }

  public static void printInfo(PacketList list)
  {
    printInfo(list, list.size());
  }

  /**
   * Test:
   * 
   * Read the set of messages from the server, build a list of packets.
   * Read the set of messages from the client, build a list of packets.
   * 
   * Print information about packets, matching request and answer.
   * 
   * @throws FileNotFoundException 
   */
  public static void printRequestAndAnswer() throws IOException
  {
    printRequestAndAnswer(JDWPConstants.DEFAULT_SERVER_FILE, 
      JDWPConstants.DEFAULT_CLIENT_FILE);
  }
  
  public static void printRequestAndAnswer(String serverFile, String clientFile)
    throws IOException
  {
    PacketList serverList = PacketInputStreamReader.readPacketList(serverFile);    
    PacketList clientList = PacketInputStreamReader.readPacketList(clientFile);
    
    int i, count;
    count = serverList.size();
    for(i = 0; i < count; i++)
    {
      PacketWrapper packet = serverList.get(i);
      PacketPrinter.printAnswerPackets(packet, clientList);
    }
  }

  public static void printAnswerPackets(PacketWrapper packet, PacketList list)
  {
    PacketList replyList = list.getAnswerSublist(packet);
    System.out.print(" Packet: ");
    
    packet.printInformation();
    
    System.out.println();
    System.out.println("   Packet reply list: ");
    
    printInfo(replyList);
    
    System.out.println("----------------------------------------");
  }
}
