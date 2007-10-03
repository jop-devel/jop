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
 * ObjectReferenceList.java
 * 
 * 
 * @author Paulo Abadie Guedes
 * 16/05/2007 - 20:18:27
 *
 */
public class ObjectReferenceList
{
  private LinkedList list;
  
  public ObjectReferenceList()
  {
    list = new LinkedList();
  }
  
  public void add(ObjectReference object)
  {
    list.add(object);
  }
  
  public ObjectReference get(int index)
  {
    return (ObjectReference) list.get(index);
  }
  
  public int size()
  {
    return list.size();
  }
}
