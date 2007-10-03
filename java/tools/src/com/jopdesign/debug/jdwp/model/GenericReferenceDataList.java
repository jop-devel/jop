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

import java.util.List;
import java.util.Vector;

/**
 * GenericReferenceDataList.java
 * 
 * A list of GenericReferenceData.
 * 
 * @author Paulo Abadie Guedes
 * 23/05/2007 - 18:09:44
 *
 */
public class GenericReferenceDataList
{
  private List list;
  private byte tag;
  
  public GenericReferenceDataList()
  {
//    list = new LinkedList();
    list = new Vector();
  }
  
  public void add(GenericReferenceData object)
  {
    list.add(object);
  }
  
  public GenericReferenceData get(int index)
  {
    return (GenericReferenceData) list.get(index);
  }
  
  public int size()
  {
    return list.size();
  }

  public byte getTag()
  {
    return tag;
  }

  public void setTag(byte tag)
  {
    this.tag = tag;
  }
}
