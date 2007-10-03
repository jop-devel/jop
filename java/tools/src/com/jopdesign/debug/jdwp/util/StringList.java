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

package com.jopdesign.debug.jdwp.util;

import java.util.List;
import java.util.Vector;

/**
 * 
 * StringList.java
 * 
 * A list of String ojects.
 * 
 * @author Paulo Abadie Guedes
 * 21/05/2007 - 20:27:37
 *
 */
public class StringList
{
  private List list;
  
  public StringList()
  {
//    list = new LinkedList();
    list = new Vector();
  }
  
  public StringList(String[] elements)
  {
    this();
    add(elements);
  }
  
  public void add(String[] elements)
  {
    int index;
    
    if(elements != null)
    {
      int size = elements.length;
      for (index = 0; index < size; index++)
      {
        add(elements[index]);
      }
    }
  }
  
  public void add(String object)
  {
    if(object != null)
    {
      list.add(object);
    }
  }
  
  public String get(int index)
  {
    return (String) list.get(index);
  }
  
  public int size()
  {
    return list.size();
  }
  
  public StringList copy()
  {
    StringList list = new StringList();
    String object;
    int i, num;
    
    num = size();
    for(i = 0; i < num; i++)
    {
      object = get(i);
      list.add(object);
    }
    
    return list;
  }

  public void removeAll(String[] list)
  {
    for(int i = 0; i < list.length; i++)
    {
      removeAll(list[i]);
    }
  }
  
  public void removeAll(StringList list)
  {
    int size = list.size();
    for(int i = 0; i < size; i++)
    {
      removeAll(list.get(i));
    }
  }
  
  public void removeAll(String value)
  {
    boolean shouldRemove = true;
    while(shouldRemove)
    {
      shouldRemove = list.remove(value);
    }
  }
  
  public String[] toStringArray()
  {
    int index;
    int size = size();
    String[] result = new String[size];
    
    for (index = 0; index < size; index++)
    {
      result[index] = get(index);
    }
    
    return result;
  }

  /**
   * @param settings
   */
  public void removeAllStartingWith(StringList prefixList)
  {
    int index;
    int size = size();
    String value;
    
    for (index = 0; index < size; index++)
    {
      value = prefixList.get(index);
      removeAllStartingWith(value);
    }
  }
  
  /**
   * @param value
   */
  public void removeAllStartingWith(String prefix)
  {
    int index;
    int size = size();
    String value;
    
    for (index = 0; index < size;)
    {
      value = get(index);
      if(value.startsWith(prefix))
      {
        remove(index);
        size --;
      }
      else
      {
        index++;
      }
    }
  }

  public void remove(int index)
  {
    if((index >= 0) && (index < size()))
    {
      list.remove(index);
    }
  }

  /**
   * @param args
   * @return
   */
  public static String[] removeElements(String[] args, StringList elements)
  {
    StringList aux = new StringList(args);
    aux.removeAll(elements);
    
    return aux.toStringArray();
  }
  
  public String toString()
  {
    return this.list.toString();
  }
}
