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
import com.jopdesign.debug.jdwp.constants.JDWPConstants;
import com.jopdesign.debug.jdwp.util.Util;

/**
 * The main purpose of this class is to open public
 * access to the original Packet class. 
 * 
 * @author Paulo Abadie Guedes
 */
public class PacketWrapper extends Packet
{
  public static final byte REPLY_FLAG = (byte) 0x80;
  
  // internal static variables to check for handshake packet.
  private static byte[] handshakeBytes = JDWPConstants.getJDWPHandshakeBytes();
  private static final int handshakeLength = handshakeBytes.length; 
  
  /**
   * Build a new object based on a previously built
   * packet.
   * 
   * @param packet
   */
  public PacketWrapper (Packet packet)
  {
    copy(packet);
  }
  
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
  
  public void copy(Packet packet)
  {
    cmdSet = packet.cmdSet;
    cmd = packet.cmd;
    flags = packet.flags;
    errorCode = packet.errorCode;
    replied = packet.replied;
    id = packet.id;
    
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
  
  /**
   * @param packet
   * @return
   */
  public static PacketWrapper convertToPacketWrapper(Packet packet)
  {
    PacketWrapper wrapper;
    if(packet instanceof PacketWrapper)
    {
      wrapper = (PacketWrapper) packet;
    }
    else
    {
      wrapper = new PacketWrapper(packet);
    }
    return wrapper;
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
    return cmdSet;
  }
  
  public void setCommandSet(int commandSet)
  {
    cmdSet = (short) commandSet;
  }
  
  public int getCmd()
  {
    return cmd;
  }
  
  public void setCommand(int command)
  {
    cmd = (short) command;
  }
  
  public int getFlags()
  {
    return flags;
  }
  
  public int getErrorCode()
  {
    return errorCode;
  }
  
  public int getID()
  {
    return id;
  }
  
  public void setID(int ID)
  {
    this.id = ID;
  }
  
  public void setID(Packet packet)
  {
    this.id = packet.id;
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
      if(id == packet.id)
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
    // ok, this may seem not very elegant at first glance.
    // but is a simple and effective way to return the error
    // and avoid creating another type just for that.
    cmdSet = (short)((error >>> 8) & 0xff);
    cmd = (short)(error & 0xff);
  }
  
  public String toString()
  {
    ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
    PrintStream printStream = new PrintStream(arrayOutputStream);
    printInformation(printStream);
    
    byte[] bytes = arrayOutputStream.toByteArray();
    
    return new String(bytes);
  }
  
  public static void printInformation(Packet thisPacket)
  {
    PacketWrapper packet = convertToPacketWrapper(thisPacket);
    packet.printInformation();
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
