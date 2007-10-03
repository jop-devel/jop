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

import java.io.Serializable;

import com.jopdesign.debug.jdwp.constants.ClassStatusConstants;

/**
 * ReferenceType.java
 * 
 * A class to model reference types.
 * 
 * @author Paulo Abadie Guedes
 * 16/05/2007 - 14:30:43
 *
 */
public class ReferenceType implements Serializable
//implements com.sun.jdi.ReferenceType
{
  private static final long serialVersionUID = 1L;
  
  private String typeName = "";
  private String typeSignature = "";
  
  private byte typeTag;
  private byte tagConstant;
  private int typeID;
  private int status;
  private int modifiers;
  
  public ReferenceType(String type)
  {
    typeName = type;
    setStatus(ClassStatusConstants.INITIALIZED);
  }

  public String getTypeName()
  {
    return typeName;
  }

  public int getStatus()
  {
    return status;
  }

  public void setStatus(int status)
  {
    this.status = status;
  }

  public int getTypeID()
  {
    return typeID;
  }

  public void setTypeID(int typeID)
  {
    this.typeID = typeID;
  }

  public byte getTypeTag()
  {
    return typeTag;
  }

  public void setTypeTag(byte typeTag)
  {
    this.typeTag = typeTag;
  }
  
  public String getTypeSignature()
  {
    return typeSignature;
  }
  
  public void setTypeSignature(String signature)
  {
    typeSignature = signature;
  }

  public boolean matchSignature(String signature)
  {
    return this.typeSignature.equals(signature);
  }

  public ReferenceType copy()
  {
    ReferenceType type = new ReferenceType(this.typeName);
    
    type.typeName = this.typeName;
    type.typeSignature = this.typeSignature;
    
    type.typeTag = this.typeTag;
    type.typeID  = this.typeID;
    type.status = this.status;
    type.modifiers = this.modifiers;
    
    return type;
  }

  public int getModifiers()
  {
    return modifiers;
  }

  public void setModifiers(int modifiers)
  {
    this.modifiers = modifiers;
  }

  public byte getTagConstant()
  {
    return tagConstant;
  }

  public void setTagConstant(byte tagConstant)
  {
    this.tagConstant = tagConstant;
  }
}
