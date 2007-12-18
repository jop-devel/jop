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

package com.sun.tools.jdi;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import com.jopdesign.debug.jdwp.constants.CommandConstants;
import com.jopdesign.debug.jdwp.constants.ErrorConstants;
import com.jopdesign.debug.jdwp.constants.JDWPConstants;
import com.jopdesign.debug.jdwp.util.Util;

/**
 * An implementation of a JDWP packet. It can hold both data for 
 * request and for reply packets and provide all basic services
 * for packet manipulation.
 * 
 * The original purpose of this class was to open public
 * access to the original Packet class.
 * Currently it's an independent implementation, with 
 * no dependencies to the original Packet class.
 * 
 * @author Paulo Abadie Guedes
 */
public class PacketWrapper
{
  public static final byte REPLY_FLAG = (byte) 0x80;
  
  // internal static variables to check for handshake packet.
  private static byte[] handshakeBytes = JDWPConstants.getJDWPHandshakeBytes();
  private static final int handshakeLength = handshakeBytes.length; 
  
  private int command;
  private int commandSet;
  private int flags;
  private int error;
  
  private boolean isReply;
  private int packetID;
  
  private byte[] data;
  
  /**
   * Build a new object based on a previously built
   * packet.
   * 
   * @param packet
   */
  public PacketWrapper (PacketWrapper packet)
  {
    copy(packet);
  }
  
  /**
   * Build a new empty object.
   */
  public PacketWrapper ()
  {
    
  }
  
  /**
   * A method to create a handshake packet.
   * 
   * @return
   */
  public static PacketWrapper createHandshakePacket()
  {
    byte[] data = JDWPConstants.getJDWPHandshakeBytes();
    PacketWrapper packet = new PacketWrapper();
    packet.setBytes(data);
    return packet;
  }
  
  /**
   * Set the packet data. Ignore null values.
   * 
   * @param handshakeData
   * @return true if it is the handshake sequence.
   */
  public void setBytes(byte[] byteArray)
  {
    if(byteArray != null)
    {
      data = Util.copyByteArray(byteArray);
    }
  }

  public boolean isHandshakePacket()
  {
    boolean isHandshakePacket = false;
    
    if(data.length == handshakeLength)
    {
      if(Util.contentEquals(handshakeBytes, data))
      {
        isHandshakePacket = true;
      }
    }
    
    return isHandshakePacket;
  }
  
  public void copy(PacketWrapper packet)
  {
    commandSet = packet.commandSet;
    command = packet.command;
    flags = packet.flags;
    error = packet.error;
    isReply = packet.isReply;
    packetID = packet.packetID;
    
    //copying to stay on the safe side.
    data = Util.copyByteArray(packet.data);
  }
  
  /**
   * Create an exact copy of this object and return it.
   * 
   * @return
   */
  public PacketWrapper createCopy()
  {
    PacketWrapper packetWrapper = new PacketWrapper(this);
    return packetWrapper;
  }
  
  public byte[] getData()
  {
    return Util.copyByteArray(data);
  }
  
  public String getDataAsString()
  {
    String data = new String(getData());
    return data;
  }

  public String getDataAsListOfIntegers()
  {
    String data = Util.printByteArray(getData());
    return data;
  }
  
  public int size()
  {
    return 11 + data.length;
  }
  
  public int getCmdSet()
  {
    return commandSet;
  }
  
  public void setCommandSet(int commandSet)
  {
    this.commandSet = (short) commandSet;
  }
  
  public int getCmd()
  {
    return command;
  }
  
  public void setCommand(int command)
  {
    this.command = (short) command;
  }
  
  public int getFlags()
  {
    return flags;
  }
  
  public void setFlags(int flags)
  {
    this.flags = flags;
  }
  
  public int getErrorCode()
  {
    return error;
  }
  
  public int getID()
  {
    return packetID;
  }
  
  public void setID(int ID)
  {
    this.packetID = ID;
  }
  
  public void setID(PacketWrapper packet)
  {
    this.packetID = packet.packetID;
  }
  
  public String getSetDescription()
  {
    return CommandConstants.getSetDescription(getCmdSet());
  }
  
  public String getCommandDescription()
  {
    int set = getCmdSet();
    int cmd = getCmd();
    
    return CommandConstants.getCommandDescription(set, cmd);
  }
  
  /**
   * Check if this is a reply packet.
   * 
   * @return
   */
  public boolean isReply()
  {
    // check if the reply flag is set
    return ((flags & REPLY_FLAG) != 0);
  }
  
  /**
   * Set this as a reply packet changing internal flags. 
   * By default, the object is not created as a reply packet.
   */
  public void setReplyPacket()
  {
    flags = (short) (flags | REPLY_FLAG);
  }
  
  /** 
   * Check if this packet is a reply to the given packet.
   * 
   * Packets are a match if the reply flag inside this
   * object is set and both ID's are the same.
   * 
   * @param packet
   * @return
   */
  public boolean isReplyTo(PacketWrapper packet)
  {
    // assume it's not and try to show otherwise.
    boolean isReply = false;
    
    if(isReply())
    {
      if(packetID == packet.packetID)
      {
        isReply = true;
      }
    }
    else
    {
      // if it's not a reply, the only chance is to be a handshake.
      if(isHandshakePacket() && packet.isHandshakePacket())
      {
        isReply = true;
      }
    }
    
    return isReply;
  }
  
  /**
   * Set the error code into the packet data.
   * 
   * @param error
   */
  public void setError(int error)
  {
    this.error = error;
  }
  
  /**
   * Check if this is a reply package which has no error set.
   * 
   * This method can be used only for reply packets.
   * It has no meaning for regular request packets: in this
   * case, it will always return "false". 
   *  
   * @return
   */
  public boolean hasNoError()
  {
    // check if it's a reply packet and also if there's no error set.
    return isReply() && (getErrorCode() == ErrorConstants.ERROR_NONE);
  }
  
  public String toString()
  {
    ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
    PrintStream printStream = new PrintStream(arrayOutputStream);
    printInformation(printStream);
    
    byte[] bytes = arrayOutputStream.toByteArray();
    
    return new String(bytes);
  }
  
  public void printInformation()
  {
    printInformation(System.out);
  }
  
  public void printInformation(PrintStream stream)
  {
//    System.out.print("  Length: ");
//    System.out.println(packet.size());
    String data = getDataAsString();
    
    if(isHandshakePacket())
    {
      stream.print("  Handshake packet received: \"");
      stream.print(data);
      stream.println("\"");
    }
    else
    {
      int id = getID();
      stream.print("  ID: ");
      stream.print(id);
      
      stream.print("  Size:");
      stream.print(size());
      
      if(isReply() == false)
      {
        int set = getCmdSet();
        stream.print("  Command set: ");
        stream.print(set);
        stream.print("  ");
        stream.print(CommandConstants.getSetDescription(set));
        
        int cmd = getCmd();
        stream.print("  Command: ");
        stream.print(cmd);
        stream.print("  ");
        stream.println(CommandConstants.getCommandDescription(set, cmd));
      }
      else
      {
        stream.print("  Error code: ");
        if(hasNoError())
        {
          stream.println("None");
        }
        else
        {
          int error = getErrorCode();
          stream.println(error);
        }
      }
      
      stream.print("  Data (as String): ");
      stream.println(data);
      
      stream.println("  Data (as list of integers): ");
      stream.print("  ");
      stream.println(getDataAsListOfIntegers());
      //stream.println("");
      
      stream.flush();
    }
  }
}
