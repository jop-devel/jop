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
import java.io.DataInputStream;
import java.io.IOException;

import com.jopdesign.debug.jdwp.constants.JDWPConstants;
import com.jopdesign.debug.jdwp.constants.TagConstants;
import com.jopdesign.debug.jdwp.model.GenericReferenceData;
import com.jopdesign.debug.jdwp.model.GenericReferenceDataList;
import com.jopdesign.debug.jdwp.model.ReferenceType;
import com.sun.tools.jdi.PacketWrapper;

/**
 * PacketReader.java
 * 
 * This class helps to read content from JDWP packets. 
 * It may be used to read almost any kind of binary data.
 * 
 * @author Paulo Abadie Guedes
 * 16/05/2007 - 13:55:10
 *
 */
public class PacketReader
{
  private ByteArrayInputStream byteArrayInputStream;
  private DataInputStream dataInputStream;

  public PacketReader()
  {
    byte[] empty = new byte[0];
    setBytes(empty);
  }
  
  public void setPacket(PacketWrapper packet)
  {
    byte[] bytes = packet.getData();
    setBytes(bytes);
  }
  
  public void setBytes(byte[] bytes)
  {
    byteArrayInputStream = new ByteArrayInputStream(bytes);
    dataInputStream = new DataInputStream(byteArrayInputStream);
  }
  
  public byte[] readBytes(int size)
  {
    byte[]data = null;
    try
    {
      data = new byte[size];
      dataInputStream.read(data);
    }
    catch (IOException e)
    {
      // this should not happen
      System.out.println("  Failure: readBytes");
      e.printStackTrace();
    }
    return data;
  }
  
  public byte readByte()
  {
    byte data = 0;
    try
    {
      data = dataInputStream.readByte();
    }
    catch (IOException e)
    {
      // this should not happen
      System.out.println("  Failure: readByte");
      e.printStackTrace();
    }
    return data;
  }
  
  public boolean readBoolean()
  {
    int data = readByte();
    boolean result;
    
    if(data == 0)
    {
      result = false;
    }
    else
    {
      result = true;
    }
    
    return result;
  }

  public int readChar()
  {
    int data = 0;
    try
    {
      data = dataInputStream.readChar();
    }
    catch (IOException e)
    {
      // this should not happen
      System.out.println("  Failure: readChar");
      e.printStackTrace();
    }
    return data;
  }

  public int readShort()
  {
    int data = 0;
    try
    {
      data = dataInputStream.readShort();
    }
    catch (IOException e)
    {
      // this should not happen
      System.out.println("  Failure: readShort");
      e.printStackTrace();
    }
    return data;
  }

  public int readInt()
  {
    int data = 0;
    try
    {
      data = dataInputStream.readInt();
    }
    catch (IOException e)
    {
      // this should not happen
      System.out.println("  Failure: readInt");
      e.printStackTrace();
    }
    return data;
  }
  
  /**
   * Skip an int from the packet data. Return true 
   * on success, and false otherwise.
   * 
   * @param numBytes
   * @return
   */
  public boolean skipInt()
  {
	return skip(4);
  }
  /**
   * Skip some bytes from the packet data. Return true
   * on success, and false otherwise.
   * 
   * @param numBytes
   * @return
   */
  public boolean skip(int numBytes)
  {
	boolean result = true;
	
    try
    {
      dataInputStream.skipBytes(numBytes);
    }
    catch(IOException exception)
    {
      // this should not happen
      System.out.println("  Failure: skip");
      exception.printStackTrace();
      result = false;
    }
    
    return result;
  }
  
  public long readLong()
  {
    long data = 0;
    try
    {
      data = dataInputStream.readLong();
    }
    catch (IOException e)
    {
      // this should not happen
      System.out.println("  Failure: readLong");
      e.printStackTrace();
    }
    return data;
  }

  public float readFloat()
  {
    float data = 0;
    try
    {
      data = dataInputStream.readFloat();
    }
    catch (IOException e)
    {
      // this should not happen
      System.out.println("  Failure: readFloat");
      e.printStackTrace();
    }
    return data;
  }

  public double readDouble()
  {
    double data = 0;
    try
    {
      data = dataInputStream.readDouble();
    }
    catch (IOException e)
    {
      // this should not happen
      System.out.println("  Failure: readDouble");
      e.printStackTrace();
    }
    return data;
  }

  public String readJDWPString()
  {
    String data = "";
    try
    {
      int size = readInt();
      byte[] bytes = readBytes(size);
      data = new String(bytes, "UTF8");
    }
    catch (IOException e)
    {
      // this should not happen
      System.out.println("  Failure: readJDWPString");
      e.printStackTrace();
    }
    return data;
  }
  
  public long readID(int idSize)
  {
    int i;
    long id = 0;
    byte data;
    
    if(idSize > 8)
    {
      // this should never happen: all ID's are limited.
      // ID sizes can be up to 8 bytes long. 
      idSize = 8;
    }
    
    for(i = idSize - 1; i >= 0; i--)
    {
      data = readByte();
      id = id | (data << (i * 8));
    }
    
    return id;
  }
  
  public long readFieldId()
  {
    int idSize = JDWPConstants.fieldIDSize;
    return readID(idSize);
  }

  public long readMethodId()
  {
    int idSize = JDWPConstants.methodIDSize;
    return readID(idSize);
  }

  public long readObjectID()
  {
    int idSize = JDWPConstants.objectIDSize;
    return readID(idSize);
  }
  
  public long readReferenceTypeID()
  {
    int idSize = JDWPConstants.referenceTypeIDSize;
    return readID(idSize);
  }
  
  public long readFrameId()
  {
    int idSize = JDWPConstants.frameIDSize;
    return readID(idSize);
  }
  
  public GenericReferenceData readTaggedValue()
  {
    int id = readInt();
    byte tag = readByte();
    
    GenericReferenceData data = new GenericReferenceData(id);
    data.setTag(tag);
    readUntaggedValue(data);
    
    return data;
  }
  
  public GenericReferenceDataList readTaggedValueList(int numValues)
  {
    int index;
    GenericReferenceDataList list;
    GenericReferenceData data;
    
    list = new GenericReferenceDataList();
    
    for(index = 0; index < numValues; index++)
    {
      data = readTaggedValue();
      list.add(data);
    }
    
    return list;
  }
  
  public void readUntaggedValue(GenericReferenceData reference)
  {
    long id;
    int intValue;
    
    int tag = reference.getTag();
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
        id = readID(JDWPConstants.objectIDSize);
        reference.setLongValue(id);
        break;
      }
      
      case TagConstants.BYTE: //66
      {
        intValue = readByte();
        reference.setByteValue(intValue);
        break;
      }
      
      case TagConstants.CHAR: //67
      {
        intValue = readChar();
        reference.setCharValue(intValue);
        break;
      }
      
      case TagConstants.FLOAT: //70
      {
        float floatData = readFloat();
        reference.setFloatValue(floatData);
        break;
      }
      
      case TagConstants.DOUBLE: //68
      {
        double doubleData = readDouble();
        reference.setDoubleValue(doubleData);
        break;
      }
      
      case TagConstants.INT: //73
      {
        intValue = readInt();
        reference.setIntValue(intValue);
        break;
      }
      
      case TagConstants.LONG: //74
      {
        long longValue = readLong(); 
        reference.setLongValue(longValue);
        break;
      }
      
      case TagConstants.SHORT: //83
      {
        intValue = readShort();
        reference.setShortValue(intValue);
        break;
      }
        
      case TagConstants.BOOLEAN: //90
      {
        boolean bool = readBoolean();
        reference.setBooleanValue(bool);
        break;
      }
      
      case TagConstants.VOID: //86
      {
        // this should not happen
        break;
      }
    }
  }

  /**
   * 
   * @param numElements
   * @param type
   * @return
   */
  public GenericReferenceDataList readUntaggedValueList(int numElements, ReferenceType type)
  {
    return readValueList(numElements, type, false);
  }
  
  private GenericReferenceDataList readValueList(int numElements,
    ReferenceType type, boolean shouldReadTag)
  {
    GenericReferenceDataList list;
    GenericReferenceData data;
    int i;
    byte tag = type.getTagConstant();
    
    list = new GenericReferenceDataList();
    for (i = 0; i < numElements; i++)
    {
      data = new GenericReferenceData(i);
      if(shouldReadTag)
      {
        tag = readByte();
      }
      
      data.setTag(tag);
      
      readUntaggedValue(data);
      list.add(data);
    }
    
    return list;
  }
}
