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

package com.jopdesign.debug.jdwp.bcel;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.bcel.classfile.JavaClass;

/**
 * ClassList.java
 * 
 * A list to hold objects from type JavaClass.
 * 
 * @author Paulo Abadie Guedes
 *
 * 28/06/2007 - 11:32:23
 * 
 */
public class ClassList
{
  private Vector list;
  private Hashtable table;
  
  public ClassList()
  {
    list = new Vector();
    table = new Hashtable();
  }
  
  public void add(JavaClass javaClass)
  {
    list.add(javaClass);
    table.put(javaClass.getClassName(), javaClass);
  }
  
  public JavaClass getClass(int index)
  {
    return (JavaClass) list.get(index);
  }
  
  public boolean contains(JavaClass javaClass)
  {
    return table.contains(javaClass);
  }
  
  public boolean contains(String className)
  {
    return table.containsKey(className);
  }

  /**
   * @param className
   * @return
   */
  public JavaClass getClass(String className)
  {
    return (JavaClass) table.get(className);
  }
}
