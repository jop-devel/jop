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

import java.util.LinkedList;

/**
 * FrameList.java
 * 
 * 
 * @author Paulo Guedes
 * 26/05/2007 - 13:14:46
 * 
 */
public class FrameList
{
  private LinkedList list;
  
  public FrameList()
  {
    list = new LinkedList();
  }
  
  public void add(Frame object)
  {
    list.add(object);
  }
  
  public Frame get(int index)
  {
    return (Frame) list.get(index);
  }
  
  public int size()
  {
    return list.size();
  }
  
  public String toString()
  {
    int index;
    int size = size();
    Object object;
    StringBuffer buffer = new StringBuffer();
    
    for(index = 0; index < size; index++)
    {
      object = list.get(index);
      buffer.append(object);
      if (index < (size - 1))
      {
        buffer.append(", ");
      }
    }
    
    return buffer.toString();
  }
}
