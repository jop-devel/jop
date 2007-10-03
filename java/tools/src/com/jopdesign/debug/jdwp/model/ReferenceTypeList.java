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
import java.util.LinkedList;

/**
 * ReferenceTypeList.java
 * 
 * A list of ReferenceType objects.
 *  
 * @author Paulo Abadie Guedes
 * 16/05/2007 - 14:27:14
 *
 */
public class ReferenceTypeList implements Serializable
{
  private static final long serialVersionUID = 1L;
  
  private LinkedList list;
  
  public ReferenceTypeList()
  {
    list = new LinkedList();
  }
  
  public void add(ReferenceType object)
  {
    list.add(object);
  }
  
  public ReferenceType get(int index)
  {
    return (ReferenceType) list.get(index);
  }
  
  public int size()
  {
    return list.size();
  }
  
  public ReferenceTypeList getClassesMatching(String signature)
  {
    ReferenceTypeList list = new ReferenceTypeList();
    ReferenceType object;
    int i, num;
    
    num = size();
    for(i = 0; i < num; i++)
    {
      object = get(i);
      if(object.matchSignature(signature))
      {
        object = object.copy();
        list.add(object);
      }
    }
    
    return list;
  }

  public ReferenceTypeList copy()
  {
    ReferenceTypeList list = new ReferenceTypeList();
    ReferenceType object;
    int i, num;
    
    num = size();
    for(i = 0; i < num; i++)
    {
      object = get(i);
      object = object.copy();
      list.add(object);
    }
    
    return list;
  }
}
