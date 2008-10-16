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

package com.jopdesign.debug.jdwp;

import java.io.Serializable;
import java.util.Vector;

import com.jopdesign.build.JopMethodInfo;

/**
 * MethodTable.java
 *
 * A table of methods.
 * 
 * @author Paulo Abadie Guedes
 *
 * 08/06/2007 - 11:03:36
 * 
 */
public class MethodTable implements Serializable
{
  // ID for serialization
  private static final long serialVersionUID = 1L;
  
  private Vector methodList;
  
  public MethodTable()
  {
    methodList = new Vector();
  }
  
  public JopMethodInfo getMethod(int methodId)
  {
    JopMethodInfo info = null;
    
    if(isValidIndex(methodId))
    {
      info = (JopMethodInfo) methodList.get(methodId);
    }
    
    return info;
  }

  /**
   * @param methodId
   * @return
   */
  private boolean isValidIndex(int methodId)
  {
    return (methodId >= 0) && (methodId < methodList.size());
  }
  
  /**
   * Add a method to the table. Return its method ID.
   * 
   * @param method
   * @return
   */
  public int addMethod(JopMethodInfo method)
  {
    int methodId = getMethodId(method);
    if(isValidIndex(methodId) == false)
    {
      // increase the size so it can hold the new element.
      // set all intermediate elements to null.
      methodList.setSize(methodId + 1);
    }
    methodList.setElementAt(method, methodId);
    
    return methodId;
  }
  
  /**
   * Return the method ID.
   * 
   * @param methodInfo
   * @return
   */
  public static int getMethodId(JopMethodInfo methodInfo)
  {
    //return methodInfo.getCodeAddress();
    return methodInfo.getStructAddress();
  }
}
