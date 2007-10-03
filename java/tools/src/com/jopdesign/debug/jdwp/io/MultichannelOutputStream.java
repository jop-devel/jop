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

package com.jopdesign.debug.jdwp.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

/**
 * MultichannelOutputStream.java
 * 
 * This class implement a simple protocol to simulate one or more
 * communication channels over a reliable output stream.
 * 
 * Usage:
 * 1) Create one object
 * 2) Add one or more streams to it
 * 3) Another object which knows the protocol should be
 *    on the other side of the connection in order to
 *    decode all data properly.
 *    
 * Please note that this object need a reliable connection
 * to work properly, as well as a compliant implementation on
 * the other side.
 *   
 * It also does not provide any mechanism to setup or match
 * channels. This has to be fixed or agreed by other ways.
 * 
 * Header format:
 * length - 2 bytes
 * channel - 1 byte
 * payload - variable: depend on the value of the length field
 * 
 * @author Paulo Abadie Guedes
 * 29/05/2007 - 15:22:10
 * 
 */
public class MultichannelOutputStream extends OutputStream
{
  // output streams to be multiplexed
  private Vector streamList;
  private OutputStream currentStream;
  
  // variables to control current state
  private boolean waitingNewPacketHeader = true;
  private int length;
  private int channel;
  private int counter;
  private ByteArrayOutputStream buffer = new ByteArrayOutputStream();
  
  private static int LENGTH_FIELD_SIZE = 2;
  private static int CHANNEL_FIELD_SIZE = 1;
  
  private static int HEADER_SIZE = LENGTH_FIELD_SIZE + CHANNEL_FIELD_SIZE;
  
  public MultichannelOutputStream()
  {
    this(System.out);
  }
  
  public MultichannelOutputStream(OutputStream stream)
  {
    streamList = new Vector();
    registerStream(stream);
  }
  
  public void registerStream(OutputStream stream)
  {
    streamList.add(stream);
  }
  
  /**
   * @throws IOException 
   * 
   */
  private void handleHeader() throws IOException
  {
    byte[] data = buffer.toByteArray();
    buffer.reset();
    
    ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(data);
    DataInputStream inputStream = new DataInputStream(arrayInputStream);
    
    // the length of the content (without this 3 byte header)
    length = inputStream.readShort();
    channel = inputStream.readByte();
    
    currentStream = getStream(channel);
    counter = 0;
  }
  
  private OutputStream getStream(int channel) throws IOException
  {
    if(channel < 0 || channel >= streamList.size())
    {
      throw new IOException("Invalid channel! " + channel);
    }
    
    return (OutputStream) streamList.get(channel);
  }
  
  /* (non-Javadoc)
   * @see java.io.OutputStream#write(int)
   */
  public void write(int b) throws IOException
  {
    if(waitingNewPacketHeader)
    {
      buffer.write(b);
      if(buffer.size() == HEADER_SIZE)
      {
        waitingNewPacketHeader = false;
        handleHeader();
      }
    }
    else
    {
      currentStream.write(b);
      counter ++;
      if(counter >= length)
      {
        // if this packet is done, change state: wait for the next one.
        counter = 0;
        currentStream = null;
        waitingNewPacketHeader = true;
      }
    }
  }
}
