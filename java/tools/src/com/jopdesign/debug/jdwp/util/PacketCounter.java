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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import com.jopdesign.debug.jdwp.constants.CommandConstants;
import com.jopdesign.debug.jdwp.constants.JDWPConstants;
import com.jopdesign.debug.jdwp.sniffer.PacketInputStreamReader;
import com.sun.tools.jdi.PacketWrapper;

/**
 * PacketCounter.java
 * 
 * This class is a small utility to calculate some informations
 * about the number of command sets and commands inside a set of
 * JDWP log files. 
 * 
 * @author Paulo Abadie Guedes
 * 22/05/2007 - 16:14:33
 *
 */
public class PacketCounter
{
  private static int[][] commandTable;
  private static int[][] commandReplyTable;
  private static int maxNumCommandSets;
  private static int maxNumCommands;
  
  private static PacketList list;
  private static boolean shouldContinue = true; 
  
  private static void printData(int[][] table)
  {
    int set, command;
    int num;
    String data = "";
    
    for(set = 0; set < maxNumCommandSets; set++)
    {
      for(command = 0; command < maxNumCommands; command++)
      {
        num = table[set][command];
        if(num > 0)
        {
          System.out.print(num);
          System.out.print(" Packets for ");
          
          // the best method is to call isHandshake()
          if(set == 0 && command == 0)
          {
            System.out.println("Handshake");
          }
//          System.out.print(" CommandSet: ");
//          System.out.print(set);
//          
//          System.out.print(" ");
          data = CommandConstants.getSetDescription(set);
          System.out.print(data);
          
//          System.out.print(" Command: ");
//          System.out.print(command);
          
          System.out.print(" ");
          data = CommandConstants.getCommandDescription(set, command);
          System.out.print(data);
          
          System.out.println();
        }
      }
    }
  }

  private static void initializeCommandTable()
  {
    maxNumCommandSets = CommandConstants.MAX_COMMAND_SET_INDEX + 1;
    maxNumCommands = CommandConstants.MAX_COMMAND_INDEX + 1;
    commandTable = new int[maxNumCommandSets][maxNumCommands];
    commandReplyTable = new int[maxNumCommandSets][maxNumCommands];
  }

  public static void countCommands(String[] files)
  {
    System.out.println("  Count and dump the types and frequency of JDWP commands ");
    System.out.println("  and command sets from a set of JDWP log files.");
    System.out.println();
    String name;
    list = new PacketList();    

    int i, num = files.length;
//    num = 1;
    for(i = 0; i < num; i++)
    {
      name = files[i];
      try
      {
        loadList(name, list);
      }
      catch (FileNotFoundException e)
      {
        System.out.println("  File not found: " + name);
      }
      catch (IOException e)
      {
        System.out.println("  Failure reading file: " + name);
      }
    }
    
    // for debugging only!
//    list = list.getSublist(0, 3000);
    
    countCommands(list);
  }

  private static void loadList(String name, PacketList list) throws FileNotFoundException, IOException
  {
    if(name == null)
    {
      throw new FileNotFoundException();
    }
    PacketList readList = PacketInputStreamReader.readPacketList(name);
    list.add(readList);
  }
  
  private static void countCommands(PacketList list)
  {
    int i, count;
    count = list.size();
    System.out.println("  Total number of Packets: " + count);
    if(count > 5000)
    {
      System.out.println("  This may take a while for the first half. Please be patient.");
    }
    
    for(i = 0; i < count; i++)
    {
      PacketWrapper packet = list.get(i);
      countPacket(packet, list);
//      System.out.print(" Packet: ");
//      System.out.print(i);
//      System.out.println();
//      System.out.print('\r');
//      list.remove(i);
      count = list.size();
      if((i % 2048) == 0)
      {
        System.out.println(i);
//        System.out.print(" ");
//        System.out.println();
      }
    }
  }

  private static void countPacket(PacketWrapper packet, PacketList list)
  {
    int commandSet, command; 
    //int size;
//    PacketList replyList;
    
    if(packet.isReply() == false)
    {
      commandSet = packet.getCmdSet();
      command = packet.getCmd();
      try
      {
        commandTable[commandSet][command] += 1;
        
        countReplyPackets(packet, list);
      }
      catch(Throwable t)
      {
        System.out.println(" Problems: " + packet);
        packet.printInformation();
        System.out.println("commandTablelength: " + commandTable.length);
        System.out.println("commandTable[0].length: " + commandTable[0].length);
      }
    }
  }

  /**
   * @param packet
   * @param list
   */
  private static void countReplyPackets(PacketWrapper packet, PacketList list)
  {
    int commandSet;
    int command;
    int size;
    PacketList replyList;
    //      replyList = list.getAnswerSublist(packet);
    replyList = list.removeAnswerSublist(packet);
    size = replyList.size();
    
    if(size > 0)
    {
//      packet = replyList.get(0);
      commandSet = packet.getCmdSet();
      command = packet.getCmd();
      
      try
      {
        commandReplyTable[commandSet][command] += replyList.size();
      }
      catch(Throwable t)
      {
        System.out.println(" Problems: " + packet);
        packet.printInformation();
        System.out.println("commandTablelength: " + commandTable.length);
        System.out.println("commandTable[0].length: " + commandTable[0].length);
      }
    }
  }
  
  /**
   * A tiny interative prompt to query JDWP packet logs for relevant data.
   * 
   */
  private static void startJDWPShell()
  {
    InputStreamReader inputStreamReader = new InputStreamReader(System.in);
    BufferedReader reader = new BufferedReader(inputStreamReader);
    String line = "";
    
    shouldContinue = true;
    while(line != null && shouldContinue) 
    {
      printCommands();
      try
      {
        line = reader.readLine();
      } 
      catch (IOException exception)
      {
       System.out.println("  Failure: " + exception.getMessage());
        exception.printStackTrace();
      }
      if(line != null)
      {
        try
        {
          executeCommands(line);
        }
        catch (Throwable e) {
          System.out.println("  Failure: " + e.getMessage());
          e.printStackTrace();
        }
      }
    }
  }
  
  /**
   * 
   */
  private static void printCommands()
  {
    String line = "----------------------------------------";
    
    System.out.println();
    System.out.println(line);
    System.out.println("  Commands:");
    System.out.println();
    System.out.println("  <return> - Print a summary of input and output packets.");
    System.out.println();
    System.out.println("  <command set> <command>  - Print packets (request/answer) with the ");
    System.out.println("  same values for command set and command. Sample: 1 1");
    System.out.println();
    System.out.println("  \"input\" - Print incoming packets (debugger -> Java machine)");
    System.out.println();
    System.out.println("  \"output\" - Print outgoing packets (Java machine -> debugger)");
    System.out.println();
    System.out.println("  \"q\", \"quit\", \"x\" or \"exit\" - To exit the program.");
    System.out.println(line);
    System.out.println();
  }

  /**
   * @param line
   */
  private static void executeCommands(String line)
  {
    line = line.toLowerCase();
    line = line.trim();
    
    if("q".equals(line) || "quit".equals(line) ||
       "x".equals(line) || "exit".equals(line))
    {
      shouldContinue = false;
      return;
    }
    
    if("input".equals(line))
    {
      printData(commandTable);
      return;
    }
    
    if("output".equals(line))
    {
      printData(commandReplyTable);
      return;
    }
    
    if("".equals(line))
    {
      printData(commandTable);
      printData(commandReplyTable);
      return;
    }
    
    // if it's not any of the above, it's the last option
    handleCommands(line);
  }
  
  private static void handleCommands(String line)
  {
    int set, command, i;
    
    line = line.trim();
    i = line.indexOf(" ");
    
    try
    {
      String setString = line.substring(0, i).trim();
      String commandString = line.substring(i).trim();
      
      set = Integer.parseInt(setString);
      command = Integer.parseInt(commandString);
      
      System.out.println("  Command set: " + set + " Command: " + command);
      printData(set, command);
    }
    catch(NumberFormatException exception)
    {
      System.out.println(" Invalid number provided. " + exception.getLocalizedMessage());
    }
    catch(StringIndexOutOfBoundsException exception)
    {
      System.out.println("  Wrong format provided, please try again.");
    }
  }
  
  /**
   * @param set
   * @param command
   */
  private static void printData(int set, int command)
  {
    PacketWrapper packet = list.getFirstPacketInCommandSet(set, command);
    
    if(packet == null)
    {
      System.out.println("  No such packet was found. Please search again.");
    }
    else
    {
      PacketWrapper reply = list.getReplyPacket(packet);
      
      System.out.println("----------------------------------------");
      
      System.out.println("   Packet: ");
      packet.printInformation();
      System.out.println();
      
      if(reply == null)
      {
        System.out.println("  No reply packet was found.");
      }
      else
      {
        System.out.println("   Reply Packet: ");
        reply.printInformation();
      }
      
      System.out.println("----------------------------------------");
    }
  }

  /** 
   * @param args
   */
  public static void main(String[] args)
  {
    String [] data;
    
    initializeCommandTable();
    
    if(args.length != 0)
    {
      data = args;
    }
    else
    {
//      data = new String[1];
//      data[0] = JDWPConstants.DEFAULT_SERVER_FILE;

      data = new String[2];
      data[0] = JDWPConstants.DEFAULT_SERVER_FILE;
      data[1] = JDWPConstants.DEFAULT_CLIENT_FILE;
    }
    
    countCommands(data);
    printData(commandTable);
    
    System.out.println();
    System.out.println("  Reply packets:");
    printData(commandReplyTable);
    
    startJDWPShell();
  }
}
