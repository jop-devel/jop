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

import java.io.IOException;
import java.io.OutputStream;

import com.sun.tools.jdi.PacketWrapper;
import com.sun.tools.jdi.SocketConnectionWrapper;

public class PacketOutputStreamWriter extends OutputStream
{
  private OutputStream output;
  private SocketConnectionWrapper connection;

  public PacketOutputStreamWriter (OutputStream outputStream) throws IOException
  {
    initializeOutputStream(outputStream);
    
    // create a socket for write-only
    MockSocket mockSocket = new MockSocket(null, output);
    connection = new SocketConnectionWrapper(mockSocket);
  }
  
  /**
   * @param outputStream
   * @throws IOException
   */
  private synchronized void initializeOutputStream(OutputStream outputStream) throws IOException
  {
    if(outputStream == null)
    {
      throw new IOException("  Invalid NULL input stream.");
    }
    
    output = outputStream;
  }
  
  public synchronized void write(int b) throws IOException
  {
    output.write(b);
  }
  
  public synchronized void writePacket(PacketWrapper packet) throws IOException
  {
    // a convenient way to handle the handshake problem.
    if(packet.isHandshakePacket())
    {
      output.write(packet.getData());
    }
    else
    {
      connection.sendPacket(packet);
    }
  }
  
  /**
   * Send a handshake packet and flush the stream.

   * @param input
   * @return
   * @throws IOException
   */
  public synchronized void sendHandshakePacket() throws IOException
  {
    PacketWrapper packet = PacketWrapper.createHandshakePacket();
    writePacket(packet);
    flush();
  }
  
  public synchronized void flush() throws IOException
  {
    output.flush();
  }
}
