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

package com.jopdesign.debug.jdwp.sniffer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import com.jopdesign.debug.jdwp.constants.JDWPConstants;
import com.jopdesign.debug.jdwp.util.PacketList;
import com.jopdesign.debug.jdwp.util.Util;
import com.sun.tools.jdi.PacketWrapper;
import com.sun.tools.jdi.SocketConnectionWrapper;

/**
 * A class to read JDWP packets from an input stream.
 * Be careful to perform the correct handshake at the
 * beginning of the protocol. 
 * 
 * This class is flexible since it can work with either a socket,
 * an InputStream or even a ConnectionService. Would prefer to
 * avoid such knowledge and restrict coupling to a minnimum, but
 * can't now. And restricting those links to this class 
 * is better than to spread it over three classes.
 * 
 * Usage:
 * - Create the object
 * - Read 14 bytes from it. Check they are correct.
 *   DON'T USE read() AT THIS FIRST STEP, OR YOU WILL 
 *   BREAK THE PROTOCOL.
 * - After the 14 bytes are read, answer the handshake
 *   and start reading packets using readPacket();
 *   
 * - Additionally, to use read() make sure to respect
 *   packet boundaries or strange errors may happen,
 *   such as OutOfMemoryError (trying to read a packet
 *   with readPacket() from the middle, where it will
 *   consider some unexpected data as the packet size).  
 * 
 * @author Paulo Abadie Guedes
 */
public class PacketInputStreamReader extends InputStream
{
  private InputStream input;
  private SocketConnectionWrapper connection;
  
  // a small change to handle the protocol handshake.
  // I don't like this handshake because it does not
  // follow the regular packet structure. Anyway... ;)
  boolean firstRead = true;
  
  public PacketInputStreamReader (Socket socket) throws IOException
  {
    synchronized(this)
    {
      InputStream inputStream = socket.getInputStream();
      initialize(inputStream, socket);
    }
  }

  public PacketInputStreamReader (InputStream inputStream) throws IOException
  {
    // create a socket for read-only
    MockSocket mockSocket;
    synchronized (this)
    {
      mockSocket = new MockSocket(inputStream, null);
      initialize(inputStream, mockSocket);
    }
  }
  
  private synchronized void initialize(InputStream inputStream, Socket socket) 
    throws IOException
  {
    initializeInputStream(inputStream);    
    initializeConnection(socket);
  }
  
  /**
   * @param inputStream
   * @throws IOException
   */
  private synchronized void initializeInputStream(InputStream inputStream) throws IOException
  {
    if(inputStream == null)
    {
      throw new IOException("  Invalid NULL input stream.");
    }
    
    input = inputStream;
  }
  
  private synchronized void initializeConnection(Socket socket) throws IOException
  {
    connection = new SocketConnectionWrapper(socket);
  }
  
  public synchronized int read() throws IOException
  {
    return input.read();
  }
  
  public synchronized PacketWrapper readPacket() throws IOException
  {
    PacketWrapper packet;
    // a convenient way to handle the handshake problem.
    // Only the first one is expected to be a handshake packet.
    // Further handshake packets should NOT arrive after the first one,
    // so this object will not expect them.
    //
    // This protocol, as defined by Sun, is very "fragile": 
    // if a wrong, malformed or unexpected packet format arises, 
    // the program will be completely lost.
    if(firstRead)
    {
      packet = receiveHandshakePacket(input);
      firstRead = false;
    }
    else
    {
      packet = connection.receivePacket();
    }
    
    return packet;
  }
  
  /**
   * Receive a handshake packet from the network.
   * Check if the format of the packet is correct: if it 
   * is not, throw an IOException.
   * 
   * @param input
   * @return
   * @throws IOException
   */
  public synchronized PacketWrapper receiveHandshakePacket(InputStream input) throws IOException
  {
    PacketWrapper packet = new PacketWrapper();
    
    byte[] data = Util.readByteArray(input, JDWPConstants.HANDSHAKE_PACKET_SIZE);
    
    packet.setBytes(data);
    if(packet.isHandshakePacket() == false)
    {
      throw new IOException(" Wrong handshake packet format: " + packet);
    }
    return packet;
  }
  
  /**
   * Read a sequence of packets from a file, build a list of packets
   * and return the created list.
   * 
   *  This method expect the sequence to have been created by a JDWP session.
   *  So, the first packet should be a handshake packet. 
   * 
   * @param name
   * @return
   * @throws FileNotFoundException
   * @throws IOException
   */
  public static PacketList readPacketList(String name)
    throws FileNotFoundException, IOException
  {
    File file = new File(name);
    if(file.exists() == false)
    {
      throw new FileNotFoundException(name); 
    }
    InputStream input = new BufferedInputStream(new FileInputStream(name));
    PacketInputStreamReader inputStream = new PacketInputStreamReader(input);
    
    PacketList list = new PacketList();
    
    // not needed anymore since the handshake now can also be represented
    // by an special type os packet (namely, the first one that arrives).
    // read first bytes
//    int size = CommandConstants.JDWP_HANDSHAKE.length();
//    byte[] data = new byte[size];
//    inputStream.read(data);
    
    // unfortunately there was no other way to read packets other than
    // try over and over again until an exception is thrown.
    // By the way, the method "inputStream.available()" returns 0.
    boolean canRead = true;
    while(canRead)
    {
      PacketWrapper packet;
      
      try
      {
        packet = inputStream.readPacket();
        list.add(packet);
      }
      catch (IOException exception)
      {
        canRead = false;
        break;
      }
//      System.out.println("PacketInputStreamReader -> read it!");
    }
    inputStream.close();
    
    return list;
  }
}
