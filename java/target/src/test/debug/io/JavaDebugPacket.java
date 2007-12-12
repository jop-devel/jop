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

package debug.io;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;

import debug.constants.CommandConstants;

/**
 * This class model a JDWP packet for JOP.
 * It is used both to receive and to send JDWP packets.
 * 
 * JavaDebugPacket.java
 * 
 * @author Paulo Abadie Guedes
 *
 * 04/12/2007 - 16:50:43
 *
 */
public final class JavaDebugPacket
{
  private static final byte REPLY_FLAG = (byte) 0x80;
  
  // a static counter to create packet ID's as needed.
  private static int idCounter = 0;
  
  private static final int LENGTH_INDEX = 0;
  private static final int LENGTH_SIZE = 4;
  
  private static final int ID_INDEX = (LENGTH_INDEX + LENGTH_SIZE);
  private static final int ID_SIZE = 4;
  
  private static final int FLAGS_INDEX = (ID_INDEX + ID_SIZE);
  private static final int FLAGS_SIZE = 1;
  
  private static final int COMMAND_SET_INDEX = (FLAGS_INDEX + FLAGS_SIZE);
  private static final int COMMAND_SET_SIZE = 1;

  private static final int COMMAND_INDEX = (COMMAND_SET_INDEX + COMMAND_SET_SIZE);
  private static final int COMMAND_SIZE = 1;
  
  private static final int ERROR_CODE_INDEX = COMMAND_SET_INDEX;
  private static final int ERROR_CODE_SIZE = 2;
  
  // the size of a JDWP packet header
  private static final int JDWP_HEADER_SIZE = 11;
  
  // varibles to hold the packet content
  private RandomAccessByteArrayOutputStream arrayOutputStream;
  
  // an internal index to be used when it's necessary to read data
  // from this stream.
  private int readIndex;
  
  // a buffer to read packets without creating too many new objects
  // for every byte read.
  private static final int READ_BUFFER_SIZE = 512;
  private byte readBuffer[];
  
  /**
   * The default constructor for a debug packet.
   */
  public JavaDebugPacket()
  {
    arrayOutputStream = new RandomAccessByteArrayOutputStream();
    readBuffer = new byte[READ_BUFFER_SIZE];
    
    // allocate at least an empty header
    createEmptyHeader();
    
    rewindReadIndexToDataSection();
  }
  
  /**
   * Set the read index to the beginning of the data section.
   */
  private void rewindReadIndexToDataSection()
  {
    readIndex = JDWP_HEADER_SIZE;
  }
  
  /**
   * Create an empty header for this packet
   */
  public synchronized void createEmptyHeader()
  {
    clear();
    
    // Write a dummy value for the header size.
    // An empty JDWP packet has at least the header (11 bytes). 
    writeInt(JDWP_HEADER_SIZE);
    
    // the ID
    writeInt(0);
    
    //the flags, command set and command
    writeShort(0);
    writeByte(0);
  }
  
  /**
   * Write an int value into this packet content. 
   * 
   * @param value
   */
  public synchronized void writeInt(int value)
  {
    arrayOutputStream.writeInt(value);
  }
  
  /**
   * Write a short value into this packet content.
   * 
   * @param value
   */
  public synchronized void writeShort(int value)
  {
    arrayOutputStream.writeShort(value);
  }
  
  /**
   * Write a byte value into this packet content.
   * 
   * @param value
   */
  public synchronized void writeByte(int value)
  {
    arrayOutputStream.write(value);
  }
  
  /**
   * Read an int value from this packet content. 
   * 
   * @param value
   */
  public synchronized int readInt()
  {
    int value;
    
    value = arrayOutputStream.readInt(this.readIndex);
    readIndex += 4;
    return value;
  }
  
  /**
   * Read a short value from this packet content.
   * 
   * @param value
   */
  public synchronized int readShort()
  {
    int value;
    
    value = arrayOutputStream.readShort(this.readIndex);
    readIndex += 2;
    return value;
  }
  
  /**
   * Read a byte value from this packet content.
   * 
   * @param value
   */
  public synchronized int readByte()
  {
    int value;
    
    value = arrayOutputStream.readByte(this.readIndex);
    readIndex += 1;
    return value;
  }
  
  /**
   * Read an entire packet from the given input and store its content
   * in memory for later access. Discard previous data.
   * 
   * Set the read index to the beginning of the packet data area.
   * 
   * @param input
   * @throws IOException
   */
  public synchronized final void readPacket(DataInputStream input) throws IOException
  {
    int size, count, data;
    int num;
    
    count = 0;
    clear();
    
    // read the size
    size = input.readInt();
    
    // store the packet size
    arrayOutputStream.writeInt(size);
    
    // read the content. We got already 4 bytes (the size, above).
    // So, start with 4.
    for(count = 4; count < size;)
    {
      // calculate how many bytes are still left to be read
      num = size - count;
      if(num > READ_BUFFER_SIZE)
      {
        num = READ_BUFFER_SIZE;
      }
      
      // try to read another set of bytes from the input.
      // throw an exception if can't read it.
      data = input.read(readBuffer, 0, num);
      if(data == -1)
      {
        throw new IOException();
      }
      
      // copy the newly read chunk into the packet content
      arrayOutputStream.write(readBuffer, 0, data);
      
      count = count + data;
    }
    
    // set the read index to the beginning of the data section.
    rewindReadIndexToDataSection();
  }
  
  /**
   * This method write the internal content to an output stream.
   * It's used to avoid allocating and releasing new objects for every
   * packet sent. 
   * 
   * @param outputStream
   * @throws IOException
   */
  public synchronized final void writePacket(OutputStream outputStream)
    throws IOException
  {
    // update the "size" field
    updateSize();
    
    // write the packet content to the output stream
    arrayOutputStream.writeContent(outputStream);
  }
  
  /**
   * Reset the internal buffer, so this object can be reused.
   */
  public synchronized void clear()
  {
    arrayOutputStream.reset();
  }
  
  /**
   * Return the buffer size.
   * 
   * @return
   */
  private synchronized int getInternalBufferSize()
  {
    return arrayOutputStream.size();
  }
  
  /**
   * Update the "length" field inside the JDWP packet.
   */
  private synchronized void updateSize()
  {
    int size = getInternalBufferSize();
    arrayOutputStream.overwriteInt(size, LENGTH_INDEX);
  }
  
  /**
   * Create a new packet ID.
   * Set the ID field inside the JDWP packet.
   */
  public synchronized final void createNewPacketId()
  {
    int id = getNextId();
    setPacketId(id);
  }
  
  /**
   * Set the content of the "ID" field.
   * 
   * @param id
   */
  private synchronized void setPacketId(int id)
  {
    arrayOutputStream.overwriteInt(id, ID_INDEX);
  }
  
  /**
   * Return the next packet ID and increment the internal
   * ID counter.
   * 
   * @return
   */
  private synchronized static int getNextId()
  {
    int nextId;
    
    nextId = idCounter;
    idCounter++;
    
    return nextId;
  }
  
  /**
   * Clear the packet flags.
   * 
   * @param i
   */
  public synchronized void clearFlags()
  {
    setFlags(0);
  }
  
  /**
   * Set the reply flag. 
   * 
   * @param i
   */
  public synchronized void setReplyPacket()
  {
    int flags;
    
    flags = getFlags();
    setFlags(flags | REPLY_FLAG);
  }
  
  /**
   * Check if this is a reply packet.
   * 
   * @return
   */
  public synchronized boolean isReply()
  {
    int flags;
    
    flags = getFlags();
    // check if the reply flag is set
    return ((flags & REPLY_FLAG) != 0);
  }
  
  /**
   * Set the "flags" field.
   * 
   * @param value
   */
  public synchronized void setFlags(int value)
  {
    arrayOutputStream.overwriteInt(value, FLAGS_INDEX);
  }
  
  /**
   * Set the "Command Set" field.
   * 
   * @param value
   */
  public synchronized void setCommandSet(int value)
  {
    arrayOutputStream.overwriteByte(value, COMMAND_SET_INDEX);
  }
  
  /**
   * Set the "Command" field.
   * 
   * @param value
   */
  public synchronized void setCommand(int value)
  {
    arrayOutputStream.overwriteByte(value, COMMAND_INDEX);
  }
  
  /**
   * Set the content of the "Error code" field of this packet.
   * Beware: the error code is present *only* in reply packets.
   * 
   * For regular packets, this method is not valid.
   * Instead, use the "setCommand" and
   * "setCommandSet" methods.
   * 
   * @return
   */
  public synchronized void setErrorCode(int errorCode)
  {
    overwriteBytes(ERROR_CODE_INDEX, ERROR_CODE_SIZE, errorCode);
  }
  
  /**
   * Read a set of bytes from the buffer.
   * 
   * @param location
   * @param size
   * @return
   */
  private synchronized int readBytes(int location, int size)
  {
    int value = 0;
    
    switch(size)
    {
      case 4:
      {
        value = arrayOutputStream.readInt(location);
        break;
      }
      case 2:
      {
        value = arrayOutputStream.readShort(location);
        break;
      }
      case 1:
      {
        value = arrayOutputStream.readByte(location);
        break;
      }
      default:
      {
        throw new ArrayIndexOutOfBoundsException("Wrong size! " + size);
      }
    }
    
    return value;
  }
  
  /**
   * Write a set of bytes on the buffer. 
   * 
   * Be careful: it does not write beyond the current size.
   * Should be used to OVERWRITE ONLY.
   * 
   * @param location
   * @param size
   * @return
   */
  private synchronized void overwriteBytes(int location, int size, int value)
  {
    switch(size)
    {
      case 4:
      {
        arrayOutputStream.overwriteInt(value, location);
        break;
      }
      case 2:
      {
        arrayOutputStream.overwriteShort(value, location);
        break;
      }
      case 1:
      {
        arrayOutputStream.overwriteByte(value, location);
        break;
      }
      default:
      {
        throw new ArrayIndexOutOfBoundsException("Wrong size! " + size);
      }
    }
  }
  
  /**
   * Return the content of the "Length" field of this packet.
   * For complete packets, it's the total number of bytes.
   * 
   * In particular, it does *not* inform the actual length 
   * of the internal buffer, nor how many bytes were 
   * written in case of a partial (not complete)
   * packet.
   * 
   * @return
   */
  public synchronized int getLength()
  {
    return  arrayOutputStream.readInt(LENGTH_INDEX);
  }
  
  /**
   * Return the content of the "ID" field of this packet.
   * 
   * @return
   */
  public synchronized int getId()
  {
    return  arrayOutputStream.readInt(ID_INDEX);
  }
  
  /**
   * Return the content of the "Flags" field of this packet.
   * 
   * @return
   */
  public synchronized int getFlags()
  {
    return  arrayOutputStream.readByte(FLAGS_INDEX);
  }
  
  /**
   * Return the content of the "Error code" field of this packet.
   * Beware: the error code is present *only* in reply packets.
   * 
   * For regular packets, this method is not valid.
   * Instead, use the "getCommand" and
   * "getCommandSet" methods.
   * 
   * @return
   */
  public synchronized int getErrorCode()
  {
    //return  arrayOutputStream.readShort(ERROR_CODE_INDEX);
    return  readBytes(ERROR_CODE_INDEX, ERROR_CODE_SIZE);
  }
  
  /**
   * Return the content of the "Command Set" field of this packet.
   * 
   * @return
   */
  public synchronized int getCommandSet()
  {
    return  readBytes(COMMAND_SET_INDEX, COMMAND_SET_SIZE);
  }
  
  /**
   * Return the content of the "Command" field of this packet.
   * 
   * @return
   */
  public synchronized int getCommand()
  {
    return  readBytes(COMMAND_INDEX, COMMAND_SIZE);
  }
  
  /**
   * Create a header for a debug packet with a brand new ID,
   * the "Event" command set and the "Composite" command. 
   */
  public synchronized void createEventHeader()
  {
    int id, commandSet, command; 
    
    id = getNextId();
    commandSet = CommandConstants.Event_Command_Set;
    command = CommandConstants.Event_Composite;
    createHeader(id, 0, commandSet, command);
  }
  
  /**
   * Create a new packet header, with all the given fields. 
   * 
   * @param id the packet ID.Can be new or old (from the server), for reply packets.
   * @param flags the flags field.
   * @param commandSet the command set.
   * @param command the command.
   */
  public synchronized void createHeader(int id, int flags, int commandSet, int command)
  {
    // clear the packet and create an empty header
    createEmptyHeader();
    
    // create a new ID for the packet
    setPacketId(id);
    
    // set the flags field
    setFlags(flags);
    
    // set the "command set" and "command" fields 
    setCommandSet(commandSet);
    setCommand(command);
  }
  
  /**
   * Create a reply header based on the given parameters
   * 
   * @param id
   * @param commandSet
   * @param command
   */
  private synchronized void createReplyHeader(int id, int commandSet, int command)
  {
    createHeader(id, REPLY_FLAG, commandSet, command);
  }
  
  /**
   * Create a header for reply packet, based on the given packet. 
   * 
   * @param packet
   */
  public synchronized void createReplyHeader(JavaDebugPacket packet)
  {
    int id;
    int commandSet;
    int command;
    
    id = packet.getId();
    commandSet = packet.getCommandSet();
    command = packet.getCommand();
    
    createReplyHeader(id, commandSet, command);
  }
}