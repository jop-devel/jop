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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

/**
 * An specific kind of socket, which knows how to receive and send
 * JDWP packets.
 * 
 * @author Paulo Abadie Guedes
 */

public class SocketConnectionWrapper
{
  private Socket socket;
  private DataOutputStream output;
  private DataInputStream input;

  /**
   * Create a new SocketConnectionWrapper based on a socket.
   * 
   * @param socket
   * @throws IOException
   */
  public SocketConnectionWrapper(Socket socket) throws IOException
  {
    BufferedOutputStream bufferedOutput;
    BufferedInputStream bufferedInput;
    
    this.socket = socket;
    socket.setTcpNoDelay(true);
    bufferedInput = new BufferedInputStream(socket.getInputStream());
    bufferedOutput = new BufferedOutputStream(socket.getOutputStream());
    
    input = new DataInputStream(bufferedInput);
    output = new DataOutputStream(bufferedOutput);
  }
  
  /**
   * Close the object and its internal objects.
   */
  public void close()
  {
    try
    {
      output.flush();
      output.close();
      input.close();
      socket.close();
    }
    catch (Exception e)
    {
      
    }
  }
  
  public byte receiveByte() throws IOException
  {
    int data = input.read();
    return (byte)data;
  }
  
  public void sendByte(byte b) throws IOException
  {
    output.write(b);
    output.flush();
  }
  
  /**
   * Read a JDWP packet from the socket.
   * 
   * @return the new packet.
   * @throws IOException
   */
  public PacketWrapper receivePacket() throws IOException
  {
    return receivePacket(input);
  }
  
  /**
   * Read a JDWP packet from the data input stream.
   * 
   * @return the new packet.
   * @throws IOException
   */
  public static PacketWrapper receivePacket(DataInputStream input) throws IOException
  {
    PacketWrapper packet = new PacketWrapper();
    
    int length;
    int id;
    int flags;
    
    // read the packet length
    length = input.readInt();
    
    // read and set the packet id
    id = input.readInt();
    packet.setID(id);
    
    // read and set the packet flags
    flags = input.read();
    if(flags < 0)
    {
      throw new EOFException();
    }
    packet.setFlags(flags);
    
    // now that the flags are set, check the packet kind.
    // if it's not a reply, read the command set and command.
    // Otherwise, read the error code.
    if(packet.isReply() == false)
    {
      int commandSet;
      int command;
      
      commandSet = input.read();
      command = input.read();
      
      if(commandSet < 0 || command < 0)
      {
        throw new EOFException();
      }
      packet.setCommandSet(commandSet);
      packet.setCommand(command);
    }
    else
    {
      short error;
      
      error = input.readShort();
      packet.setError(error);
    }

    // subtract the header size here
    length -= 11; 

    if(length < 0)
    {
      // oops! an error! this shouldn't be happening!
      System.err.println("length is " + length);
      System.err.println("Read is " + input.read());
      throw new EOFException();
    }
    
    // read the packet body
    byte[] data = new byte[length];
    int n = 0;
    while (n < data.length)
    {
      int count = input.read(data, n, data.length - n);
      if(count < 0)
      {
        throw new EOFException();
      }
      n += count;
    }
    packet.setBytes(data);
    
    return packet;
  }
  
  /**
   * Send a packet through the socket.
   * 
   * @param packet the object to be sent through the socket.
   */
  public void sendPacket(PacketWrapper packet) throws IOException
  {
    sendPacket(packet, output);
  }
  
  /**
   * Send a packet through the socket.
   * 
   * @param packet the object to be sent through the socket.
   */
  public static void sendPacket(PacketWrapper packet, DataOutputStream output)
    throws IOException
  {
    int length;
    int id;
    int flags;
    
    
    byte[] data;
    
    // get the packet size (including the header) and id
    length = packet.size();
    id = packet.getID();
    flags = packet.getFlags();
    data = packet.getData();
    
    // send the size
    output.writeInt(length);
    
    // send the ID
    output.writeInt(id);
    
    // send the flags
    output.write(flags);
    
    if(packet.isReply() == false)
    {
      int commandSet;
      int command;
      
      commandSet = packet.getCmdSet();
      command = packet.getCmd();
      
      output.write(commandSet);
      output.write(command);
    }
    else
    {
      int error;
      
      error = packet.getErrorCode();
      output.writeShort(error);
    }
    output.write(data);
    
    output.flush();
  }
}
