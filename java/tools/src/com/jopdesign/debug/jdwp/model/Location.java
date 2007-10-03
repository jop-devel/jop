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

import com.jopdesign.debug.jdwp.constants.TypeTag;

/**
 * 
 * Location.java
 * 
 * 
 * @author Paulo Abadie Guedes
 * 24/05/2007 - 22:23:53
 *
 */
public class Location
{
  private byte tag;
  private long classId;
  private int methodId;
  private long index;
  
  public Location()
  {
    this(TypeTag.CLASS, 0, 0, 0);
  }

  public Location(byte tag, long classId, int methodId, long index)
  {
    this.tag = tag;
    this.classId = classId;
    this.methodId = methodId;
    this.index = index;
  }

  public long getClassId()
  {
    return classId;
  }

  public long getIndex()
  {
    return index;
  }

  public int getMethodId()
  {
    return methodId;
  }

  public byte getTag()
  {
    return tag;
  }

  public void setClassId(long classId)
  {
    this.classId = classId;
  }

  public void setIndex(long index)
  {
    this.index = index;
  }

  public void setMethodId(int methodId)
  {
    this.methodId = methodId;
  }

  public void setTag(byte tag)
  {
    this.tag = tag;
  }
  
  public String toString()
  {
    StringBuffer buffer = new StringBuffer();
    
    buffer.append("Location: [");
    buffer.append("Tag: ");
    buffer.append(tag);
    
    buffer.append(" ClassID: ");
    buffer.append(classId);
    
    buffer.append(" MethodID: ");
    buffer.append(methodId);
    
    buffer.append(" Index: ");
    buffer.append(index);
    buffer.append("]");
    
    return buffer.toString();
  }
  
  /**
   * Check if an object is the same location as this object.
   */
  public boolean equals(Object object)
  {
    boolean result = false;
    
    if(object instanceof Location)
    {
      Location aux = (Location) object;
      
      result = ((aux.tag == tag) && 
          (aux.classId == classId) &&
          (aux.methodId == methodId) &&
          (aux.index == index));
    }
    
    return result;
  }
}
