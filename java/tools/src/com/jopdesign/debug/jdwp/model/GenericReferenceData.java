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

package com.jopdesign.debug.jdwp.model;

/**
 * GenericReferenceData.java
 * 
 * This object models a collection of data to be transported
 * between the debugger and the machine.
 * 
 * It can model a reference to several items: to a type,
 * an object, a field, a method or an array.
 * 
 * It may also contain field data as well as type data.
 * The type of data represented by each instance can be
 * discovered checking the "tag" propery. 
 *  
 * 
 * @author Paulo Abadie Guedes
 * 23/05/2007 - 18:08:10
 *
 */
public class GenericReferenceData
{
  // the ID for a field or method
  private int fieldOrMethodId;
  private String name;
  private String signature;
  private int modifiers;
  private byte tag;
  
  // the ID of the type for this element
  private long referenceTypeId;
  
  // variables for the content of the field.
  // used for int, short, char, byte, boolean, object and class references
  private int intValue;
  
  private long longValue;
  private float floatValue;
  private double doubleValue;
 
  //------------------------------------------------------------
  // some methods that use the same variable inside.
  // Useful to explicit the semantic difference and to
  // make it easier if it's need to use more fields later.
  //------------------------------------------------------------
  public void setReferenceValue(int value)
  {
    this.intValue = value;
  }
  
  public int getReferenceValue()
  {
    return intValue;
  }

  public void setByteValue(int value)
  {
    this.intValue = (byte) value;
  }
  
  public byte getByteValue()
  {
    return (byte) intValue;
  }
  
  public void setCharValue(int value)
  {
    this.intValue = (char) value;
  }
  
  public char getCharValue()
  {
    return (char) intValue;
  }
  
  public void setShortValue(int value)
  {
    this.intValue = (short) value;
  }
  
  public short getShortValue()
  {
    return (short) intValue;
  }
  
  public void setBooleanValue(boolean value)
  {
    if(value)
    {
      this.intValue = 1;
    }
    else
    {
      this.intValue = 0;
    }
  }
  
  public boolean getBooleanValue()
  {
    boolean result = false;
    
    if(intValue != 0)
    {
      result = true;
    }
    
    return result;
  }
  
  //------------------------------------------------------------
  
  public void setIntValue(int intValue)
  {
    this.intValue = intValue;
  }
  
  public int getIntValue()
  {
    return intValue;
  }
  
  public double getDoubleValue()
  {
    return doubleValue;
  }

  public void setDoubleValue(double doubleValue)
  {
    this.doubleValue = doubleValue;
  }

  public float getFloatValue()
  {
    return floatValue;
  }

  public void setFloatValue(float floatValue)
  {
    this.floatValue = floatValue;
  }


  public void setValue(int value)
  {
    this.intValue = value;
  }

  public GenericReferenceData(int id)
  {
    this.fieldOrMethodId = id;
  }
  
  public int getFieldOrMethodId()
  {
    return fieldOrMethodId;
  }

  public int getModifiers()
  {
    return modifiers;
  }

  public void setModifiers(int modifiers)
  {
    this.modifiers = modifiers;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getSignature()
  {
    return signature;
  }

  public void setSignature(String signature)
  {
    this.signature = signature;
  }

  public byte getTag()
  {
    return tag;
  }

  public void setTag(byte tag)
  {
    this.tag = tag;
  }

  public long getLongValue()
  {
    return longValue;
  }

  public void setLongValue(long longValue)
  {
    this.longValue = longValue;
  }

  public long getReferenceTypeId()
  {
    return referenceTypeId;
  }

  public void setReferenceTypeId(long referenceTypeId)
  {
    this.referenceTypeId = referenceTypeId;
  }
}
