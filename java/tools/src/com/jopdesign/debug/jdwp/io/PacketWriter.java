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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.jopdesign.debug.jdwp.constants.JDWPConstants;
import com.jopdesign.debug.jdwp.constants.TagConstants;
import com.jopdesign.debug.jdwp.model.GenericReferenceData;
import com.jopdesign.debug.jdwp.model.GenericReferenceDataList;
import com.jopdesign.debug.jdwp.model.Location;
import com.sun.tools.jdi.PacketWrapper;

/**
 * 
 * PacketWriter.java
 * 
 * This class helps to build JDWP packets. 
 * It may be used to build almost any kind of binary data.
 * 
 * @author Paulo Abadie Guedes
 * 15/05/2007 - 23:42:25
 *
 */
public class PacketWriter
{
  private ByteArrayOutputStream byteArrayOutputStream;
  private DataOutputStream dataOutputStream;
  
  public PacketWriter()
  {
    createStreams();
  }
  
  private void createStreams()
  {
    byteArrayOutputStream = new ByteArrayOutputStream();
    dataOutputStream = new DataOutputStream(byteArrayOutputStream);
  }

  public void writeBoolean(boolean data)
  {
    if(data)
    {
      writeByte(1);
    }
    else
    {
      writeByte(0);
    }
  }

  public void writeByte(int data)
  {
    try
    {
      dataOutputStream.write(data);
    }
    catch (IOException e)
    {
      // this should not happen
      System.out.println("  Failure: writeByte");
      e.printStackTrace();
    }
  }

  public void writeChar(int data)
  {
    try
    {
      dataOutputStream.writeChar(data);
    }
    catch (IOException e)
    {
      // this should not happen
      System.out.println("  Failure: writeChar");
      e.printStackTrace();
    }
  }
  
  public void writeShort(int data)
  {
    try
    {
      dataOutputStream.writeShort(data);
    }
    catch (IOException e)
    {
      // this should not happen
      System.out.println("  Failure: writeShort");
      e.printStackTrace();
    }
  }
  
  public void writeInt(int data)
  {
    try
    {
      dataOutputStream.writeInt(data);
    }
    catch (IOException e)
    {
      // this should not happen
      System.out.println("  Failure: writeInt");
      e.printStackTrace();
    }
  }
  
  public void writeLong(long data)
  {
    try
    {
      dataOutputStream.writeLong(data);
    }
    catch (IOException e)
    {
      // this should not happen
      System.out.println("  Failure: writeLong");
      e.printStackTrace();
    }
  }
  
  public void writeFloat(float data)
  {
    try
    {
      dataOutputStream.writeFloat(data);
    }
    catch (IOException e)
    {
      // this should not happen
      System.out.println("  Failure: writeFloat");
      e.printStackTrace();
    }
  }
  
  public void writeDouble(double data)
  {
    try
    {
      dataOutputStream.writeDouble(data);
    }
    catch (IOException e)
    {
      // this should not happen
      System.out.println("  Failure: writeDouble");
      e.printStackTrace();
    }
  }
  
  public void writeJDWPString(String data)
  {
    try
    {
      byte[] bytes = data.getBytes("UTF8");
      writeInt(bytes.length);
      writeBytes(bytes);
    }
    catch (IOException e)
    {
      // this should not happen
      System.out.println("  Failure: writeJDWPString");
      e.printStackTrace();
    }
  }
  
  public void writeBytes(byte[] data)
  {
    try
    {
      dataOutputStream.write(data);
    }
    catch (IOException e)
    {
      // this should not happen
      System.out.println("  Failure: writeBytes");
      e.printStackTrace();
    }
  }
  
  public byte[] getBytes()
  {
    byte[] bytes = byteArrayOutputStream.toByteArray();
    return bytes;
  }
  
  /**
   * Clear this object. Useful to reuse it after creating an array of bytes.
   */
  public void clear()
  {
    createStreams();
  }
  
  public PacketWrapper createPacket()
  {
    PacketWrapper packet = new PacketWrapper();
    byte[] bytes = getBytes();
    packet.setBytes(bytes);
    
    return packet;
  }
  
  /**
   * Create a new packet object with the main fields already set.
   * 
   * @param commandSet
   * @param command
   * @param ID
   * @return
   */
  public PacketWrapper createPacket(int commandSet, int command, int packetId)
  {
    PacketWrapper packet = createPacket();
    
    packet.setCommandSet(commandSet);
    packet.setCommand(command);
    packet.setID(packetId);
    
    return packet;
  }
  
  public void writeID(long typeID, int idSize)
  {
    int i;
    byte data;
    
    if(idSize > 8)
    {
      idSize = 8;
    }
    
    for(i = idSize - 1; i >= 0; i--)
    {
      data = (byte) ((typeID >>> (i * 8)) & 0xFF);
      writeByte(data);
    }
  }
  
  public void writeTaggedValue(GenericReferenceData reference)
  {
    int tag = reference.getTag();
    writeByte(tag);
    
    writeUntaggedValue(reference);
  }

  /**
   * @param reference
   * @param tag
   */
  public void writeUntaggedValue(GenericReferenceData reference)
  {
    long id;
    int intValue;
    int data;
    int tag;
    
    tag = reference.getTag();
    switch(tag)
    {
      case TagConstants.ARRAY: //91
      case TagConstants.OBJECT: //76
      case TagConstants.STRING: //115
      case TagConstants.THREAD: //116
      case TagConstants.THREAD_GROUP: //103
      case TagConstants.CLASS_LOADER: //108
      case TagConstants.CLASS_OBJECT: //99
      {
        id = reference.getReferenceValue();
        writeID(id, JDWPConstants.objectIDSize);
        break;
      }
      
      case TagConstants.BYTE: //66
      {
        data = reference.getByteValue();
        writeByte(data);
        break;
      }
      
      case TagConstants.CHAR: //67
      {
        data = reference.getCharValue();
        writeChar(data);
        break;
      }
      
      case TagConstants.FLOAT: //70
      {
        float floatData = reference.getFloatValue();
        writeFloat(floatData);
        break;
      }
      
      case TagConstants.DOUBLE: //68
      {
        double doubleData = reference.getDoubleValue();
        writeDouble(doubleData);
        break;
      }
      
      case TagConstants.INT: //73
      {
        intValue = reference.getIntValue();
        writeInt(intValue);
        break;
      }
      
      case TagConstants.LONG: //74
      {
        long longValue = reference.getLongValue();
        writeLong(longValue);
        break;
      }
      
      case TagConstants.SHORT: //83
      {
        data = reference.getShortValue();
        writeShort(data);
        break;
      }
        
      case TagConstants.BOOLEAN: //90
      {
        boolean value = reference.getBooleanValue();
        writeBoolean(value);
        break;
      }
      
      case TagConstants.VOID: //86
      {
        // this should not happen
        break;
      }
    }
  }

  public void writeFieldId(long id)
  {
    writeID(id, JDWPConstants.fieldIDSize);
  }

  public void writeMethodId(long id)
  {
    writeID(id, JDWPConstants.methodIDSize);
  }

  public void writeObjectId(long id)
  {
    writeID(id, JDWPConstants.objectIDSize);
  }

  public void writeReferenceTypeId(long id)
  {
    writeID(id, JDWPConstants.referenceTypeIDSize);
  }
  
  public void writeFrameId(long id)
  {
    writeID(id, JDWPConstants.frameIDSize);
  }

  /**
   * @param location
   */
  public void writeLocation(Location location)
  {
    long data;
    byte tag;
    
    tag = location.getTag();
    writeByte(tag);
    
    data = location.getClassId();
    writeReferenceTypeId(data);
    
    data = location.getMethodId();
    writeMethodId(data);
    
    data = location.getIndex();
    writeLong(data);
  }
  /**
   * @param list
   */
  public void writeTaggedValueList(GenericReferenceDataList list)
  {
    int index, size;
    size = list.size();
    GenericReferenceData data;
    
    for (index = 0; index < size; index++)
    {
      data = list.get(index);
      writeTaggedValue(data);
    }
  }

  /**
   * @param list
   */
  public void writeUntaggedObjectIdList(GenericReferenceDataList list)
  {
    int index, size;
    size = list.size();
    GenericReferenceData data;
    
    for (index = 0; index < size; index++)
    {
      data = list.get(index);
      writeUntaggedValue(data);
    }
  }
}
