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
 * Variable.java
 * 
 * 
 * @author Paulo Abadie Guedes
 * 24/05/2007 - 14:24:12
 *
 */

public class Variable
{
  private long codeIndex;
  private String name;
  private String signature;
  private int length;
  private int slot;
  
  public Variable()
  {
    
  }
  
  public long getCodeIndex()
  {
    return codeIndex;
  }

  public void setCodeIndex(long codeIndex)
  {
    this.codeIndex = codeIndex;
  }

  public int getLength()
  {
    return length;
  }

  public void setLength(int length)
  {
    this.length = length;
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

  public int getSlot()
  {
    return slot;
  }

  public void setSlot(int slot)
  {
    this.slot = slot;
  }
}
