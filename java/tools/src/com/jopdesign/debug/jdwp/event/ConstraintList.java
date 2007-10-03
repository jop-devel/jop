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

package com.jopdesign.debug.jdwp.event;

import java.util.List;
import java.util.Vector;

/**
 * ConstraintList.java
 * 
 * A list of Constraint objects. This object is a composite.
 * So, other constraint lists can be stored inside it as well.
 * 
 * It is itself a Constraint, which can be used to combine
 * the effects of all constraints stored inside it. 
 * 
 * @author Paulo Abadie Guedes
 *
 * 14/06/2007 - 11:45:37
 * 
 */
public class ConstraintList implements Constraint
{
  private List list;
  
  public ConstraintList()
  {
    clear();
  }
  
  public void clear()
  {
    list = new Vector();
  }
  
  public void add(Constraint constraint)
  {
    if(constraint != null)
    {
      list.add(constraint);
    }
  }
  
  public Constraint get(int index)
  {
    return (Constraint) list.get(index);
  }
  public int size()
  {
    return list.size();
  }
  
  /**
   * Check if a given event satisfy all constraints stored into this list.
   * 
   * If there is no constraint stored, there is no restriction.
   * So, the answer is "true".
   */
  public boolean satisfyConstraint(Event event)
  {
    int index, size;
    Constraint constraint;
    boolean result = true;
    
    size = size();
    for(index = 0; index < size; index++)
    {
      constraint = get(index);
      if(constraint.satisfyConstraint(event) == false)
      {
        result = false;
        break;
      }
    }
    
    return result;
  }

}
